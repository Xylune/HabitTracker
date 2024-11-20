package no.hiof.groupone.habittracker.viewmodel

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Source
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import no.hiof.groupone.habittracker.model.Frequency
import no.hiof.groupone.habittracker.model.Habit
import no.hiof.groupone.habittracker.model.HabitCategory
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

    private val _selectedDate = MutableStateFlow<LocalDate?>(null)

    private val _isOnline = MutableStateFlow(true)
    val isOnline: StateFlow<Boolean> = _isOnline

    private val _selectedCategory = MutableStateFlow<HabitCategory?>(null)
    val selectedCategory: StateFlow<HabitCategory?> = _selectedCategory

    fun updateSelectedCategory(category: HabitCategory?) {
        _selectedCategory.value = category
    }

    fun getFilteredHabits(habits: List<Habit>): List<Habit> {
        return _selectedCategory.value?.let { selectedCategory ->
            habits.filter { it.category == selectedCategory }
        } ?: habits
    }

    init {
        checkConnectivity()
        fetchUserHabits(fromCache = true)
    }

    private fun checkConnectivity() {
        FirebaseFirestore.getInstance().disableNetwork()
            .addOnCompleteListener {
                _isOnline.value = false
                fetchUserHabits(fromCache = true)

                FirebaseFirestore.getInstance().enableNetwork()
                    .addOnSuccessListener {
                        _isOnline.value = true
                        fetchUserHabits(fromCache = false)
                    }
            }
    }

    fun refreshHabits() {
        fetchUserHabits()
    }

    fun updateSelectedDate(date: LocalDate) {
        _selectedDate.value = date
    }

    fun addNewHabit(habit: Habit) {
        val currentState = _uiState.value
        if (currentState is HabitsUiState.Success) {
            val updatedHabits = currentState.habits + habit
            _uiState.value = HabitsUiState.Success(updatedHabits)
        }
    }

    fun getHabitsForDate(localDate: LocalDate): List<Habit> {
        return when (val state = _uiState.value) {
            is HabitsUiState.Success -> state.habits.filter { habit ->
                habit.startTime?.let {
                    Instant.ofEpochMilli(it)
                        .atZone(ZoneId.systemDefault())
                        .toLocalDate() == localDate
                } ?: false
            }
            else -> emptyList()
        }
    }

    fun markHabitAsComplete(habit: Habit) {
        viewModelScope.launch {
            try {
                FirebaseAuth.getInstance().currentUser?.uid ?: return@launch
                val updatedHabit = habit.copy(
                    isCompleted = true,
                    completedDates = habit.completedDates + System.currentTimeMillis(),
                    currentStreak = habit.currentStreak + 1
                )

                updateLocalHabit(updatedHabit)

                val habitRef = FirebaseFirestore.getInstance()
                    .collection("habits")
                    .document(habit.id)

                val updates = mapOf(
                    "isCompleted" to true,
                    "completedDates" to updatedHabit.completedDates,
                    "currentStreak" to updatedHabit.currentStreak
                )

                habitRef.update(updates)
                    .addOnSuccessListener {
                        fetchUserHabits(fromCache = true)
                    }
            } catch (e: Exception) {
                _uiState.value = HabitsUiState.Error(e.message ?: "Failed to mark habit as complete")
            }
        }
    }

    private fun updateLocalHabit(habit: Habit) {
        val currentState = _uiState.value
        if (currentState is HabitsUiState.Success) {
            val updatedHabits = currentState.habits.map {
                if (it.id == habit.id) habit else it
            }
            _uiState.value = HabitsUiState.Success(updatedHabits)
        }
    }

    fun deleteHabit(habit: Habit) {
        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid
        if (currentUserId.isNullOrEmpty()) {
            _uiState.value = HabitsUiState.Error("User not logged in")
            return
        }

        if (habit.id.isEmpty()) {
            _uiState.value = HabitsUiState.Error("Invalid habit ID")
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

    private fun fetchUserHabits(fromCache: Boolean = false) {
        viewModelScope.launch {
            try {
                val source = if (fromCache) Source.CACHE else Source.DEFAULT
                val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: return@launch

                val userDoc = FirebaseFirestore.getInstance()
                    .collection("users")
                    .document(currentUserId)
                    .get(source)
                    .await()

                val habitIds = (userDoc.get("habits") as? List<*>)?.mapNotNull { it?.toString() } ?: emptyList()
                val habits = habitIds.mapNotNull { habitId ->
                    val habitDoc = FirebaseFirestore.getInstance()
                        .collection("habits")
                        .document(habitId)
                        .get(source)
                        .await()

                    val data = habitDoc.data
                    if (data != null) {
                        Habit(
                            id = habitDoc.id,
                            name = data["name"] as? String ?: "",
                            description = data["description"] as? String,
                            frequency = try {
                                (data["frequency"] as? String)?.let {
                                    Frequency.valueOf(it)
                                }
                            } catch (e: Exception) { null },
                            startTime = (data["startTime"] as? Long) ?: System.currentTimeMillis(),
                            endTime = data["endTime"] as? Long,
                            basePoints = (data["basePoints"] as? Number)?.toInt() ?: 0,
                            currentStreak = (data["currentStreak"] as? Number)?.toInt() ?: 0,
                            isCompleted = data["isCompleted"] as? Boolean ?: false,
                            completedDates = (data["completedDates"] as? List<Long>) ?: emptyList(),
                            category = try {
                                (data["category"] as? String)?.let {
                                    HabitCategory.valueOf(it)
                                }
                            } catch (e: Exception) { null }
                        )
                    } else null
                }

                _uiState.value = HabitsUiState.Success(habits)
            } catch (e: Exception) {
                if (_uiState.value !is HabitsUiState.Success) {
                    _uiState.value = HabitsUiState.Error(e.message ?: "Failed to load habits")
                }
            }
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