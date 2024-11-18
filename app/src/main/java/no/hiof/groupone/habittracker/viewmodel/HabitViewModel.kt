package no.hiof.groupone.habittracker.viewmodel

import androidx.compose.material3.DatePickerState
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
import no.hiof.groupone.habittracker.model.Habit


class HabitViewModel(private val habitListViewModel: HabitListViewModel) : ViewModel() {
    private val _habitName = mutableStateOf("")
    val habitName: State<String> = _habitName
    fun updateHabitName(name: String) {
        _habitName.value = name
    }

    private val _habitDescription = mutableStateOf("")
    val habitDescription: State<String> = _habitDescription
    fun updateHabitDescription(description: String) {
        _habitDescription.value = description
    }

    private val _frequency = mutableStateOf<String?>(null)
    val frequency: State<String?> = _frequency
    fun updateFrequency(frequency: String?) {
        _frequency.value = frequency
    }

    @OptIn(ExperimentalMaterial3Api::class)
    private val _selectedDate = mutableStateOf<DatePickerState?>(null)

    @OptIn(ExperimentalMaterial3Api::class)
    val selectedDate: State<DatePickerState?> = _selectedDate

    @OptIn(ExperimentalMaterial3Api::class)
    fun updateSelectedDate(datePickerState: DatePickerState) {
        _selectedDate.value = datePickerState
    }

    @OptIn(ExperimentalMaterial3Api::class)
    private val _selectedTime = mutableStateOf<TimePickerState?>(null)

    @OptIn(ExperimentalMaterial3Api::class)
    val selectedTime: State<TimePickerState?> = _selectedTime

    @OptIn(ExperimentalMaterial3Api::class)
    fun updateSelectedTime(timePickerState: TimePickerState) {
        _selectedTime.value = timePickerState
    }

    fun createHabit(habit: Habit, onHabitCreated: (Habit) -> Unit): Boolean {
        return try {
            viewModelScope.launch {
                val habitRef = FirebaseFirestore.getInstance()
                    .collection("habits")
                    .document()
                val habitWithId = habit.copy(id = habitRef.id)

                habitListViewModel.addNewHabit(habitWithId)
                onHabitCreated(habitWithId)

                habitRef.set(habitWithId)

                val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: ""
                FirebaseFirestore.getInstance()
                    .collection("users")
                    .document(currentUserId)
                    .update("habits", FieldValue.arrayUnion(habitRef.id))
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}