package no.hiof.groupone.habittracker.model

import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore

class SocialManager {

    private val db = FirebaseFirestore.getInstance()

    fun addFriend(userId: String, friendId: String) {
        val userRef = db.collection("users").document(userId)

        userRef.update("friends", FieldValue.arrayUnion(friendId))
            .addOnSuccessListener {
                println("Friend added successfully!")
            }
            .addOnFailureListener { e ->
                println("Error adding friend: ${e.message}")
            }
    }

    fun removeFriend(userId: String, friendId: String) {
        val userRef = db.collection("users").document(userId)

        userRef.update("friends", FieldValue.arrayRemove(friendId))
            .addOnSuccessListener {
                println("Friend removed successfully!")
            }
            .addOnFailureListener { e ->
                println("Error removing friend: ${e.message}")
            }
    }

    fun getFriends(userId: String, callback: (List<String>) -> Unit) {
        val userRef = db.collection("users").document(userId)

        userRef.get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    val friendsList = document.get("friends") as? List<String> ?: listOf()
                    callback(friendsList)
                } else {
                    println("No such user found!")
                    callback(emptyList())
                }
            }
            .addOnFailureListener { e ->
                println("Error getting friends: ${e.message}")
                callback(emptyList())
            }
    }


    fun isFriend(userId: String, friendId: String, callback: (Boolean) -> Unit) {
        val userRef = db.collection("users").document(userId)

        userRef.get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    val friendsList = document.get("friends") as? List<String> ?: listOf()
                    callback(friendsList.contains(friendId))
                } else {
                    println("No such user found!")
                    callback(false)
                }
            }
            .addOnFailureListener { e ->
                println("Error checking friendship: ${e.message}")
                callback(false)
            }
    }

    /*fun inviteToHabit(userId: String, habitId: String, friendId: String) {
            val habitRef = db.collection("users").document(userId).collection("habits").document(habitId)

            habitRef.update("sharedWith", FieldValue.arrayUnion(friendId))
                .addOnSuccessListener {
                    println("User invited to habit successfully!")
                }
                .addOnFailureListener { e ->
                    println("Error inviting to habit: ${e.message}")
                }
        }*/


}