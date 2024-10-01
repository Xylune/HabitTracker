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
        viewModelScope.launch {
            _uiState.value = HabitsUiState.Loading
            val currentUserId = FirebaseAuth.getInstance().currentUser?.uid

            if (currentUserId.isNullOrEmpty()) {
                _uiState.value = HabitsUiState.Error("User not logged in")
                return@launch
            }

            try {
                val firestore = FirebaseFirestore.getInstance()

                val userDoc = firestore.collection("users")
                    .document(currentUserId)
                    .get()
                    .await()

                val habitIdsField = userDoc.get("habits")
                val validHabitIds = when (habitIdsField) {
                    is List<*> -> habitIdsField.filterIsInstance<String>()
                    else -> emptyList() // Return empty list if the type is not List<*>
                }

                val habits = mutableListOf<Habit>()
                val batchSize = 10
                val batches = validHabitIds.chunked(batchSize)

                for (batch in batches) {
                    val batchHabits = firestore.collection("habits")
                        .whereIn(FieldPath.documentId(), batch)
                        .get()
                        .await()
                        .documents
                        .mapNotNull { it.toObject(Habit::class.java)?.copy(id = it.id.toInt()) }
                    habits.addAll(batchHabits)
                }

                _uiState.value = HabitsUiState.Success(habits)
            } catch (e: Exception) {
                _uiState.value = HabitsUiState.Error(e.message ?: "Unknown error")

            }

        }
    }
}