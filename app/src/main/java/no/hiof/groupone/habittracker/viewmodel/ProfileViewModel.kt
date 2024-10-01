package no.hiof.groupone.habittracker.viewmodel

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import no.hiof.groupone.habittracker.model.Habit

class ProfileViewModel : ViewModel() {

    private val _userName = mutableStateOf("")
    val userName = _userName

    private val _email = mutableStateOf("")
    val email = _email

    private val _totalHabits = mutableStateOf(0)
    val totalHabits = _totalHabits

    private val _currentStreak = mutableStateOf(0)
    val currentStreak = _currentStreak

    private val _points = mutableStateOf(0)
    val points = _points

    private val _habitList = mutableStateOf<List<Habit>>(emptyList())
    val habitList = _habitList

    fun fetchUserData(user: FirebaseUser?) {
        if (user != null) {
            _userName.value = user.displayName ?: ""
            _email.value = user.email ?: ""

            val db = FirebaseFirestore.getInstance()
            val userId = user.uid
            db.collection("users")
                .document(userId)
                .get()
                .addOnSuccessListener { document ->
                    if (document != null && document.exists()) {
                        val habitIds = document.get("habits") as? List<String> ?: emptyList()
                        _totalHabits.value = habitIds.size

                        val habitObjects = mutableListOf<Habit>()
                        for (habitId in habitIds) {
                            db.collection("habits")
                                .document(habitId)
                                .get()
                                .addOnSuccessListener { habitDocument ->
                                    if (habitDocument != null && habitDocument.exists()) {
                                        val habit = habitDocument.toObject(Habit::class.java)
                                        if (habit != null) {
                                            habitObjects.add(habit)
                                        }
                                    }
                                    _habitList.value = habitObjects
                                }
                        }
                    }
                }
        }
    }
}