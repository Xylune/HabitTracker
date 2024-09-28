package no.hiof.groupone.habittracker.model

import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore

class SocialManager {

    private val db = FirebaseFirestore.getInstance()

    fun addFriend(userId: String, friendDisplayName: String) {
        val usersRef = db.collection("users")

        usersRef.whereEqualTo("displayName", friendDisplayName).get()
            .addOnSuccessListener { documents ->
                if (documents.isEmpty) {
                    println("Friend with display name $friendDisplayName not found!")
                    return@addOnSuccessListener
                }
                val friendDoc = documents.first()
                val friendId = friendDoc.id

                val userRef = usersRef.document(userId)
                userRef.update("friends", FieldValue.arrayUnion(friendId))
                    .addOnSuccessListener {
                        println("Friend $friendDisplayName added successfully!")
                    }
                    .addOnFailureListener { e ->
                        println("Error adding friend: ${e.message}")
                    }
            }
            .addOnFailureListener { e ->
                println("Error finding friend: ${e.message}")
            }
    }

    fun removeFriend(userId: String, friendDisplayName: String) {
        val usersRef = db.collection("users")

        usersRef.whereEqualTo("displayName", friendDisplayName).get()
            .addOnSuccessListener { documents ->
                if (documents.isEmpty) {
                    println("Friend with display name $friendDisplayName not found!")
                    return@addOnSuccessListener
                }
                val friendDoc = documents.first()
                val friendId = friendDoc.id

                val userRef = usersRef.document(userId)
                userRef.update("friends", FieldValue.arrayRemove(friendId))
                    .addOnSuccessListener {
                        println("Friend $friendDisplayName removed successfully!")
                    }
                    .addOnFailureListener { e ->
                        println("Error removing friend: ${e.message}")
                    }
            }
            .addOnFailureListener { e ->
                println("Error finding friend: ${e.message}")
            }
    }

    fun getFriends(userId: String, callback: (List<String>) -> Unit) {
        val usersRef = db.collection("users")
        val userRef = usersRef.document(userId)

        userRef.get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    val friendsList = document.get("friends") as? List<String> ?: listOf()

                    if (friendsList.isNotEmpty()) {
                        usersRef.whereIn(FieldPath.documentId(), friendsList).get()
                            .addOnSuccessListener { friendDocs ->
                                val friendNames = friendDocs.documents.mapNotNull { it.getString("displayName") }
                                callback(friendNames)
                            }
                            .addOnFailureListener { e ->
                                println("Error fetching friend names: ${e.message}")
                                callback(emptyList())
                            }
                    } else {
                        callback(emptyList())
                    }
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
}
