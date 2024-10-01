package no.hiof.groupone.habittracker.model

import com.google.android.gms.tasks.Tasks
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore

class SocialManager {

    private val db = FirebaseFirestore.getInstance()

    fun addFriend(userId: String, friendDisplayName: String, onComplete: (Boolean) -> Unit) {
        val usersRef = db.collection("users")

        usersRef.whereEqualTo("displayName", friendDisplayName).get()
            .addOnSuccessListener { documents ->
                if (documents.isEmpty) {
                    onComplete(false)
                    return@addOnSuccessListener
                }
                val friendDoc = documents.first()
                val friendId = friendDoc.id

                val userRef = usersRef.document(userId)
                userRef.update("friends", FieldValue.arrayUnion(friendId))
                    .addOnSuccessListener {
                        onComplete(true)
                    }
                    .addOnFailureListener { e ->
                        onComplete(false)
                    }
            }
            .addOnFailureListener { e ->
                onComplete(false)
            }
    }

    fun removeFriend(userId: String, friendDisplayName: String, onComplete: (Boolean) -> Unit) {
        val usersRef = db.collection("users")

        usersRef.whereEqualTo("displayName", friendDisplayName).get()
            .addOnSuccessListener { documents ->
                if (documents.isEmpty) {
                    onComplete(false)
                    return@addOnSuccessListener
                }
                val friendDoc = documents.first()
                val friendId = friendDoc.id

                val userRef = usersRef.document(userId)
                userRef.update("friends", FieldValue.arrayRemove(friendId))
                    .addOnSuccessListener {
                        onComplete(true)
                    }
                    .addOnFailureListener { e ->
                        onComplete(false)
                    }
            }
            .addOnFailureListener { e ->
                onComplete(false)
            }
    }

    fun getFriends(userId: String, onFriendsRetrieved: (List<String>) -> Unit) {
        val userRef = db.collection("users").document(userId)

        userRef.get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    val friendsList = document.get("friends") as? List<String> ?: emptyList()

                    if (friendsList.isEmpty()) {
                        onFriendsRetrieved(emptyList())
                    } else {
                        val displayNames = mutableListOf<String>()
                        val tasks = friendsList.map { friendId ->
                            db.collection("users").document(friendId).get()
                                .addOnSuccessListener { friendDoc ->
                                    if (friendDoc != null && friendDoc.exists()) {
                                        val displayName = friendDoc.getString("displayName") ?: "Unknown"
                                        displayNames.add(displayName)
                                    }
                                }
                        }

                        Tasks.whenAll(tasks).addOnCompleteListener {
                            onFriendsRetrieved(displayNames)
                        }
                    }
                } else {
                    onFriendsRetrieved(emptyList())
                }
            }
            .addOnFailureListener { e ->
                onFriendsRetrieved(emptyList())
            }
    }

    fun shareHabit(habitId: String, displayName: String, onComplete: (Boolean) -> Unit) {
        val usersRef = db.collection("users")

        usersRef.whereEqualTo("displayName", displayName).get()
            .addOnSuccessListener { querySnapshot ->
                if (!querySnapshot.isEmpty) {
                    val friendDoc = querySnapshot.documents.first()
                    val friendId = friendDoc.id

                    usersRef.document(friendId).update("habits", FieldValue.arrayUnion(habitId))
                        .addOnSuccessListener {
                            onComplete(true)
                        }
                        .addOnFailureListener {
                            onComplete(false)
                        }
                } else {
                    onComplete(false)
                }
            }
            .addOnFailureListener {
                onComplete(false)
            }
    }
    fun getUserHabits(userId: String, onComplete: (List<Pair<String, String>>) -> Unit) {
        val userRef = db.collection("users").document(userId)

        userRef.get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    val habitsList = document.get("habits") as? List<String> ?: emptyList()
                    val habitNames = mutableListOf<Pair<String, String>>()

                    val tasks = habitsList.map { habitId ->
                        db.collection("habits").document(habitId).get()
                            .addOnSuccessListener { habitDoc ->
                                if (habitDoc != null && habitDoc.exists()) {
                                    val habitName = habitDoc.getString("name") ?: "Unknown"
                                    habitNames.add(Pair(habitId, habitName))
                                }
                            }
                    }

                    Tasks.whenAll(tasks).addOnCompleteListener {
                        onComplete(habitNames)
                    }
                } else {
                    onComplete(emptyList())
                }
            }
            .addOnFailureListener {
                onComplete(emptyList())
            }
    }
}
