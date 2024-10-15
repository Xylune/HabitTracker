package no.hiof.groupone.habittracker.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import no.hiof.groupone.habittracker.model.Habit

sealed class HabitsUiState {
    data object Loading : HabitsUiState()
    data class Success(val habits: List<Habit>) : HabitsUiState()
    data class Error(val exception: String) : HabitsUiState()
}

class HabitListViewModel : ViewModel() {
    private val _uiState = MutableStateFlow<HabitsUiState>(HabitsUiState.Loading)
    val uiState: StateFlow<HabitsUiState> get() = _uiState

    init {
        fetchUserHabits()
    }

    fun refreshHabits() {
        fetchUserHabits()
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