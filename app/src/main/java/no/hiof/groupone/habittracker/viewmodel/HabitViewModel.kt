package no.hiof.groupone.habittracker.viewmodel

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TimePickerState
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import no.hiof.groupone.habittracker.model.Habit


class HabitViewModel(private val habitListViewModel: HabitListViewModel) : ViewModel() {
    private val _habitName = mutableStateOf("")
    val habitName: State<String> = _habitName
    fun updateHabitName(name: String) { _habitName.value = name }

    private val _habitDescription = mutableStateOf("")
    val habitDescription: State<String> = _habitDescription
    fun updateHabitDescription(description: String) { _habitDescription.value = description }

    private val _frequency = mutableStateOf<String?>(null)
    val frequency: State<String?> = _frequency
    fun updateFrequency(frequency: String?) { _frequency.value = frequency }

    private val _selectedDate = mutableStateOf<Long?>(null)
    val selectedDate: State<Long?> = _selectedDate
    fun updateSelectedDate(date: Long?) { _selectedDate.value = date }

    @OptIn(ExperimentalMaterial3Api::class)
    private val _selectedTime = mutableStateOf<TimePickerState?>(null)
    @OptIn(ExperimentalMaterial3Api::class)
    val selectedTime: State<TimePickerState?> = _selectedTime
    @OptIn(ExperimentalMaterial3Api::class)
    fun updateSelectedTime(time: TimePickerState?) { _selectedTime.value = time }

    fun createHabit(habit: Habit): Boolean {
        return try {
        viewModelScope.launch {
            // adding habit to habits collection
            val habitId = FirebaseFirestore.getInstance()
                .collection("habits")
                .add(habit)
                .await()
                .id

            val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: ""

            // adding habit tu users habits
            FirebaseFirestore.getInstance()
                .collection("users")
                .document(currentUserId)
                .update("habits", FieldValue.arrayUnion(habitId))
                .await()
            }
            habitListViewModel.refreshHabits()
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}