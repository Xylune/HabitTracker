package no.hiof.groupone.habittracker.viewmodel

import android.util.Log
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthRecentLoginRequiredException
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.userProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore
import no.hiof.groupone.habittracker.model.Frequency
import no.hiof.groupone.habittracker.model.Habit

class ProfileViewModel : ViewModel() {
    var loading = mutableStateOf(true)
        private set

    private val _userName = mutableStateOf("")
    val userName = _userName
    var isEditingUserName = mutableStateOf(false)
        private set

    private val _email = mutableStateOf("")
    val email = _email
    var isEditingEmail = mutableStateOf(false)
        private set

    private val _totalHabits = mutableIntStateOf(0)
    val totalHabits = _totalHabits

    private val _currentStreak = mutableIntStateOf(0)
    val currentStreak = _currentStreak

    private val _points = mutableIntStateOf(0)
    val points = _points

    private val _habitList = mutableStateOf<List<Habit>>(emptyList())
    val habitList = _habitList


    fun fetchUserData(user: FirebaseUser?) {
        if (user == null) {
            loading.value = false
            return
        }

        loading.value = true // Start loading
        _userName.value = user.displayName ?: ""
        println("User Name Fetched: ${_userName.value}")
        _email.value = user.email ?: ""

        val db = FirebaseFirestore.getInstance()
        val userId = user.uid

        db.collection("users")
            .document(userId)
            .get()
            .addOnSuccessListener { document ->
                if (document == null || !document.exists()) {
                    stopLoading()
                    return@addOnSuccessListener
                }

                val habitIds = document.get("habits") as? List<String> ?: emptyList()
                _totalHabits.intValue = habitIds.size

                if (habitIds.isEmpty()) {
                    stopLoading()
                    return@addOnSuccessListener
                }
                loadHabits(db, habitIds)
            }
            .addOnFailureListener {
                stopLoading()
            }
    }

    private fun loadHabits(db: FirebaseFirestore, habitIds: List<String>) {
        val habitObjects = mutableListOf<Habit>()

        habitIds.forEach { habitId ->
            db.collection("habits")
                .document(habitId)
                .get()
                .addOnSuccessListener { habitDocument ->
                    if (habitDocument != null && habitDocument.exists()) {
                        try {
                            val data = habitDocument.data
                            if (data != null) {
                                val habit = Habit(

                                    name = data["name"] as? String ?: "",
                                    description = data["description"] as? String,
                                    frequency = try {
                                        (data["frequency"] as? String)?.let {
                                            Frequency.valueOf(it)
                                        }
                                    } catch (e: Exception) { null },
                                    startTime = (data["startTime"] as? Long)
                                        ?: System.currentTimeMillis(),
                                    endTime = data["endTime"] as? Long,
                                    basePoints = (data["basePoints"] as? Number)?.toInt() ?: 0,
                                    currentStreak = (data["currentStreak"] as? Number)?.toInt()
                                        ?: 0,
                                    isCompleted = data["isCompleted"] as? Boolean ?: false,
                                    completedDates = (data["completedDates"] as? List<Long>)
                                        ?: emptyList()
                                )
                                habitObjects.add(habit)
                            }
                        } catch (e: Exception) {
                            Log.e("ProfileViewModel", "Error parsing habit: ${e.message}", e)
                        }
                    }

                    if (habitObjects.size == habitIds.size) {
                        _habitList.value = habitObjects
                        stopLoading()
                    }
                }
                .addOnFailureListener {
                    Log.e("ProfileViewModel", "Error loading habit: ${it.message}", it)
                    if (habitObjects.size == habitIds.size) {
                        _habitList.value = habitObjects
                        stopLoading()
                    }
                }
        }
    }

    private fun stopLoading() {
        loading.value = false
    }

    fun updateEmail(user: FirebaseUser?, newEmail: String, onSuccess: () -> Unit, onFailure: (String) -> Unit) {
        user?.let {
            it.verifyBeforeUpdateEmail(newEmail)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        onSuccess()
                    } else {
                        if (task.exception is FirebaseAuthRecentLoginRequiredException) {
                            onFailure("Re authentication required")
                        } else {
                            onFailure("Failed to update email: ${task.exception?.message}")
                        }
                    }
                }
        }

    }

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    fun reAuthenticateUser(
        email: String,
        password: String,
        onSuccess: () -> Unit,
        onFailure: (String) -> Unit
    ) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    onSuccess()
                } else {
                    onFailure("Re-authentication failed: ${task.exception?.message}")
                }
            }
    }

    fun updateDisplayName(user: FirebaseUser?, newDisplayName: String, onSuccess: () -> Unit, onFailure: (String) -> Unit) {
        user?.let {
            val profileUpdates = userProfileChangeRequest {
                displayName = newDisplayName
            }

            it.updateProfile(profileUpdates).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val db = FirebaseFirestore.getInstance()
                    db.collection("users")
                        .document(it.uid)
                        .update("displayName", newDisplayName)
                        .addOnSuccessListener {
                            _userName.value = newDisplayName
                            onSuccess()
                        }
                        .addOnFailureListener { exception ->
                            onFailure("Failed to update display name in Firestore: ${exception.message}")
                        }
                } else {
                    onFailure("Failed to update display name: ${task.exception?.message}")
                }
            }
        }
    }
}