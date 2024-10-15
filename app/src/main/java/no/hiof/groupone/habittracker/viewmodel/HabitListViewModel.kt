package no.hiof.groupone.habittracker.viewmodel

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
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
    val selectedDate: StateFlow<LocalDate?> get() = _selectedDate

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
                    val habitIds = document.get("habits") as? List<String> ?: emptyList()
                    val habitObjects = mutableListOf<Habit>()

                    // If you want to update the UI state once all habits are fetched, use a counter or a completion flag.
                    if (habitIds.isEmpty()) {
                        _uiState.value = HabitsUiState.Success(habitObjects)
                        return@addOnSuccessListener
                    }

                    // Use a counter to track how many habits have been fetched
                    var fetchedCount = 0

                    for (habitId in habitIds) {
                        firestore.collection("habits")
                            .document(habitId)
                            .get()
                            .addOnSuccessListener { habitDocument ->
                                if (habitDocument != null && habitDocument.exists()) {
                                    val habit = habitDocument.toObject(Habit::class.java)
                                    if (habit != null) {
                                        habitObjects.add(habit)
                                    }
                                }
                                fetchedCount++

                                // Once all habits have been fetched, update the UI state
                                if (fetchedCount == habitIds.size) {
                                    _uiState.value = HabitsUiState.Success(habitObjects)
                                }
                            }
                            .addOnFailureListener { e ->
                                // Handle errors for each habit fetch
                                _uiState.value = HabitsUiState.Error(e.message ?: "Unknown error")
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
    // Convert the selected date to LocalDate
    val localDate = selectedDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate()

    // Observe the habits from the ViewModel
    val habits by habitListViewModel.habits.collectAsState()

    // Get filtered habits based on the selected date
    val filteredHabits = habitListViewModel.getHabitsForDate(localDate)

    // Display filtered habits in a LazyColumn
    LazyColumn {
        items(filteredHabits) { habit ->
            HabitItem(habit = habit) // Assuming you have a HabitItem composable
        }
    }
}