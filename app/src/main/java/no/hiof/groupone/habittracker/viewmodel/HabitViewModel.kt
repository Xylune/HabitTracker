package no.hiof.groupone.habittracker.viewmodel

import androidx.activity.result.launch
import androidx.compose.foundation.layout.add
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import no.hiof.groupone.habittracker.model.Habit

class HabitViewModel : ViewModel() {

    fun createHabit(habit: Habit) {
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
    }
}