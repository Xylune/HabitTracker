package no.hiof.groupone.habittracker.viewmodel

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthRecentLoginRequiredException
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.userProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore
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

    private val _totalHabits = mutableStateOf(0)
    val totalHabits = _totalHabits

    private val _currentStreak = mutableStateOf(0)
    val currentStreak = _currentStreak

    private val _points = mutableStateOf(0)
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

        // Fetch user data
        db.collection("users")
            .document(userId)
            .get()
            .addOnSuccessListener { document ->
                if (document == null || !document.exists()) {
                    stopLoading()
                    return@addOnSuccessListener
                }

                val habitIds = document.get("habits") as? List<String> ?: emptyList()
                _totalHabits.value = habitIds.size

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
                    val habit = habitDocument?.toObject(Habit::class.java)
                    if (habit != null) {
                        habitObjects.add(habit)
                    }
                    if (habitObjects.size == habitIds.size) {
                        _habitList.value = habitObjects
                        stopLoading()
                    }
                }
                .addOnFailureListener {
                    stopLoading()
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
