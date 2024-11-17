package no.hiof.groupone.habittracker.viewmodel

import android.util.Log
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import no.hiof.groupone.habittracker.model.Frequency
import no.hiof.groupone.habittracker.model.Habit
import no.hiof.groupone.habittracker.ui.screens.HabitItem
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.util.Date

sealed class HabitsUiState {
    data object Loading : HabitsUiState()
    data class Success(val habits: List<Habit>) : HabitsUiState()
    data class Error(val exception: String) : HabitsUiState()
}

class HabitListViewModel : ViewModel() {
    private val _uiState = MutableStateFlow<HabitsUiState>(HabitsUiState.Loading)
    val uiState: StateFlow<HabitsUiState> get() = _uiState

    private val _habits = MutableStateFlow<List<Habit>>(emptyList())
    val habits: StateFlow<List<Habit>> get() = _habits

    init {
        fetchUserHabits()
    }

    fun refreshHabits() {
        fetchUserHabits()
    }

    private val _selectedDate = MutableStateFlow<LocalDate?>(null)

    fun updateSelectedDate(date: LocalDate) {
        _selectedDate.value = date
    }

    fun getHabitsForDate(localDate: LocalDate): List<Habit> {
        val habits = when (val state = _uiState.value) {
            is HabitsUiState.Success -> state.habits
            else -> emptyList()
        }

        return habits.filter { habit ->
            val habitDate = habit.startTime?.let {
                Instant.ofEpochMilli(it)
                    .atZone(ZoneId.systemDefault())
                    .toLocalDate()
            }
            habitDate == localDate
        }
    }

    fun markHabitAsComplete(habit: Habit) {
        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid
        if (currentUserId.isNullOrEmpty()) {
            _uiState.value = HabitsUiState.Error("User not logged in")
            return
        }

        val firestore = FirebaseFirestore.getInstance()
        val habitRef = firestore.collection("habits").document(habit.id)

        val updatedCompletedDates = habit.completedDates + System.currentTimeMillis()
        val updates = hashMapOf(
            "isCompleted" to true,
            "completedDates" to updatedCompletedDates,
            "currentStreak" to (habit.currentStreak + 1)
        )

        habitRef.update(updates)
            .addOnSuccessListener {
                fetchUserHabits()
            }
            .addOnFailureListener { e ->
                _uiState.value =
                    HabitsUiState.Error(e.message ?: "Failed to mark habit as complete")
            }
    }

    fun deleteHabit(habit: Habit) {
        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid
        if (currentUserId.isNullOrEmpty()) {
            _uiState.value = HabitsUiState.Error("User not logged in")
            return
        }

        val firestore = FirebaseFirestore.getInstance()

        val userRef = firestore.collection("users").document(currentUserId)
        firestore.runTransaction { transaction ->
            val userDoc = transaction.get(userRef)
            val habitIds = userDoc.get("habits") as? List<String> ?: emptyList()
            val updatedHabits = habitIds.filter { it != habit.id }
            transaction.update(userRef, "habits", updatedHabits)
        }.addOnSuccessListener {
            firestore.collection("habits")
                .document(habit.id)
                .delete()
                .addOnSuccessListener {
                    fetchUserHabits()
                }
                .addOnFailureListener { e ->
                    _uiState.value = HabitsUiState.Error(e.message ?: "Failed to delete habit")
                }
        }.addOnFailureListener { e ->
            _uiState.value = HabitsUiState.Error(e.message ?: "Failed to update user habits")
        }
    }


    private fun fetchUserHabits() {
        _uiState.value = HabitsUiState.Loading
        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid

        if (currentUserId.isNullOrEmpty()) {
            _uiState.value = HabitsUiState.Error("User not logged in")
            return
        }

        val firestore = FirebaseFirestore.getInstance()
        firestore.collection("users")
            .document(currentUserId)
            .get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    val habitIdList = when (val habitIds = document.get("habits")) {
                        is List<*> -> habitIds.mapNotNull { it?.toString() }
                        else -> emptyList()
                    }

                    val habitObjects = mutableListOf<Habit>()

                    if (habitIdList.isEmpty()) {
                        _uiState.value = HabitsUiState.Success(habitObjects)
                        return@addOnSuccessListener
                    }

                    var fetchedCount = 0

                    for (habitId in habitIdList) {
                        firestore.collection("habits")
                            .document(habitId)
                            .get()
                            .addOnSuccessListener { habitDocument ->
                                if (habitDocument != null && habitDocument.exists()) {
                                    try {
                                        val data = habitDocument.data
                                        if (data != null) {
                                            val habit = Habit(
                                                id = habitDocument.id,
                                                name = data["name"] as? String ?: "",
                                                description = data["description"] as? String,
                                                frequency = try {
                                                    (data["frequency"] as? String)?.let {
                                                        Frequency.valueOf(it)
                                                    }
                                                } catch (e: Exception) {
                                                    null
                                                },
                                                startTime = (data["startTime"] as? Long)
                                                    ?: System.currentTimeMillis(),
                                                endTime = data["endTime"] as? Long,
                                                basePoints = (data["basePoints"] as? Number)?.toInt()
                                                    ?: 0,
                                                currentStreak = (data["currentStreak"] as? Number)?.toInt()
                                                    ?: 0,
                                                isCompleted = data["isCompleted"] as? Boolean
                                                    ?: false,
                                                completedDates = (data["completedDates"] as? List<Long>)
                                                    ?: emptyList()
                                            )
                                            habitObjects.add(habit)
                                        }
                                    } catch (e: Exception) {
                                        Log.e(
                                            "HabitListViewModel",
                                            "Error parsing habit: ${e.message}", e
                                        )
                                    }
                                }
                                fetchedCount++

                                if (fetchedCount == habitIdList.size) {
                                    habitObjects.sortBy { it.startTime }
                                    _uiState.value = HabitsUiState.Success(habitObjects)
                                }
                            }
                            .addOnFailureListener { e ->
                                Log.e(
                                    "HabitListViewModel",
                                    "Error fetching habit: ${e.message}", e
                                )
                                fetchedCount++
                                if (fetchedCount == habitIdList.size) {
                                    _uiState.value = HabitsUiState.Success(habitObjects)
                                }
                            }
                    }


                } else {
                    _uiState.value = HabitsUiState.Error("User document does not exist")
                }
            }
            .addOnFailureListener { e ->
                _uiState.value = HabitsUiState.Error(e.message ?: "Unknown error")
            }
    }
}

@Composable
fun HabitListForDate(selectedDate: Date, habitListViewModel: HabitListViewModel) {
    val localDate = selectedDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate()

    val filteredHabits = habitListViewModel.getHabitsForDate(localDate)

    LazyColumn {
        items(filteredHabits) { habit ->
            HabitItem(habit = habit)
        }
    }
}