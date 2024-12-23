package no.hiof.groupone.habittracker.viewmodel

import android.util.Log
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
import kotlinx.coroutines.tasks.await
import no.hiof.groupone.habittracker.model.Habit
import no.hiof.groupone.habittracker.model.HabitCategory


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

    private val _selectedCategory = mutableStateOf<HabitCategory?>(null)
    val selectedCategory: State<HabitCategory?> = _selectedCategory
    fun updateCategory(category: HabitCategory?) {
        _selectedCategory.value = category
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
        Log.d("HabitDebug", "Creating Habit: $habit")
        return try {
            viewModelScope.launch {
                val habitRef = FirebaseFirestore.getInstance()
                    .collection("habits")
                    .document()
                val habitWithId = habit.copy(id = habitRef.id)

                habitListViewModel.addNewHabit(habitWithId)
                onHabitCreated(habitWithId)

                habitRef.set(habitWithId)
                    .addOnSuccessListener {
                        Log.d("HabitDebug", "Habit successfully written to Firestore: $habitWithId")
                    }
                    .addOnFailureListener{ e ->
                        Log.e("HabitDebug", "Error writing habit to Firestore: ${e.message}")
                    }

                val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: ""
                FirebaseFirestore.getInstance()
                    .collection("users")
                    .document(currentUserId)
                    .update("habits", FieldValue.arrayUnion(habitRef.id))
            }
            habitListViewModel.refreshHabits()
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }


    suspend fun getHabitById(habitId: String): Habit? {
        val db = FirebaseFirestore.getInstance()
        return try {
            val documentSnapshot = db.collection("habits")
                .document(habitId)
                .get()
                .await()

            if (documentSnapshot.exists()) {
                documentSnapshot.toObject(Habit::class.java)
            } else {
                null
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }


    fun updateHabit(updatedHabit: Habit) {
        viewModelScope.launch {
            try {
                FirebaseFirestore.getInstance()
                    .collection("habits")
                    .document(updatedHabit.id)
                    .set(updatedHabit)
                    .await()

                val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: ""
                FirebaseFirestore.getInstance()
                    .collection("users")
                    .document(currentUserId)
                    .update("habits", FieldValue.arrayUnion(updatedHabit.id))
                    .await()

                habitListViewModel.refreshHabits()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

}