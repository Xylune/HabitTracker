package no.hiof.groupone.habittracker.model

import com.google.android.gms.tasks.Tasks
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore

class SocialManager {

    private val db = FirebaseFirestore.getInstance()

    fun sendFriendRequest(senderId: String, senderDisplayName: String, recipientDisplayName: String, onComplete: (Boolean) -> Unit) {
        val usersRef = db.collection("users")

        usersRef.whereEqualTo("displayName", recipientDisplayName).get()
            .addOnSuccessListener { documents ->
                if (documents.isEmpty) {
                    onComplete(false)
                    return@addOnSuccessListener
                }
                val recipientDoc = documents.first()
                val recipientId = recipientDoc.id

                val friendRequest = mapOf(
                    "senderId" to senderId,
                    "senderDisplayName" to senderDisplayName,
                    "status" to "pending"
                )

                usersRef.document(recipientId).update("friendRequests", FieldValue.arrayUnion(friendRequest))
                    .addOnSuccessListener {
                        onComplete(true)
                    }
                    .addOnFailureListener {
                        onComplete(false)
                    }
            }
            .addOnFailureListener {
                onComplete(false)
            }
    }

    fun respondToFriendRequest(userId: String, senderId: String, accept: Boolean, onComplete: (Boolean) -> Unit) {
        val usersRef = db.collection("users")
        val userRef = usersRef.document(userId)

        userRef.get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    val friendRequests = document.get("friendRequests") as? List<Map<String, Any>> ?: emptyList()
                    val updatedRequests = friendRequests.filterNot { it["senderId"] == senderId }

                    userRef.update("friendRequests", updatedRequests)
                        .addOnSuccessListener {
                            if (accept) {

                                addFriendRelationship(userId, senderId, onComplete)
                            } else {
                                onComplete(true)
                            }
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

    private fun addFriendRelationship(userId: String, friendId: String, onComplete: (Boolean) -> Unit) {
        val usersRef = db.collection("users")

        val userUpdate = usersRef.document(userId).update("friends", FieldValue.arrayUnion(friendId))
        val friendUpdate = usersRef.document(friendId).update("friends", FieldValue.arrayUnion(userId))

        Tasks.whenAll(userUpdate, friendUpdate)
            .addOnSuccessListener { onComplete(true) }
            .addOnFailureListener { onComplete(false) }
    }

    fun removeFriend(userId: String, friendId: String, onComplete: (Boolean) -> Unit) {
        val usersRef = db.collection("users")

        val userUpdate = usersRef.document(userId).update("friends", FieldValue.arrayRemove(friendId))
        val friendUpdate = usersRef.document(friendId).update("friends", FieldValue.arrayRemove(userId))

        Tasks.whenAll(userUpdate, friendUpdate)
            .addOnSuccessListener {
                onComplete(true)
            }
            .addOnFailureListener {
                onComplete(false)
            }
    }

    fun getFriendRequests(userId: String, callback: (List<Map<String, Any>>) -> Unit) {
        db.collection("users").document(userId).get()
            .addOnSuccessListener { document ->
                val requests = document.get("friendRequests") as? List<Map<String, Any>> ?: emptyList()
                callback(requests)
            }
            .addOnFailureListener {
                callback(emptyList())
            }
    }

    fun getFriends(userId: String, onFriendsRetrieved: (List<Pair<String, String>>) -> Unit) {
        val userRef = db.collection("users").document(userId)

        userRef.get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    val friendsList = document.get("friends") as? List<String> ?: emptyList()

                    if (friendsList.isEmpty()) {
                        onFriendsRetrieved(emptyList())
                    } else {
                        val friendPairs = mutableListOf<Pair<String, String>>()
                        val tasks = friendsList.map { friendId ->
                            db.collection("users").document(friendId).get()
                                .addOnSuccessListener { friendDoc ->
                                    if (friendDoc != null && friendDoc.exists()) {
                                        val displayName = friendDoc.getString("displayName") ?: "Unknown"
                                        friendPairs.add(Pair(friendId, displayName))
                                    }
                                }
                        }

                        Tasks.whenAll(tasks).addOnCompleteListener {
                            onFriendsRetrieved(friendPairs)
                        }
                    }
                } else {
                    onFriendsRetrieved(emptyList())
                }
            }
            .addOnFailureListener {
                onFriendsRetrieved(emptyList())
            }
    }

    fun shareHabit(habitId: String, friendId: String, onComplete: (Boolean) -> Unit) {
        val usersRef = db.collection("users")

        usersRef.document(friendId).update("habits", FieldValue.arrayUnion(habitId))
            .addOnSuccessListener {
                onComplete(true)
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
