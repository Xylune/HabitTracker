package no.hiof.groupone.habittracker.model

import android.util.Log
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore

class LeaderboardManager {

    private val db = FirebaseFirestore.getInstance()
    private val leaderboardCollection = db.collection("Leaderboards")
    private val userCollection = db.collection("users")

    data class Leaderboard(
        val name: String = "",
        val users: List<User> = listOf(),
        val admin: String = ""
    )

    data class User(
        val name: String = "",
        val points: Int = 0
    )

    fun createNewLeaderboard(
        leaderboardName: String,
        allPlayers: List<String>,
        leaderboardAdmin: String,
        callback: (Boolean, String?) -> Unit
    ) {
        val leaderboardData = hashMapOf(
            "name" to leaderboardName,
            "users" to allPlayers.map { mapOf("name" to it, "points" to 0) },
            "admin" to leaderboardAdmin
        )

        leaderboardCollection.add(leaderboardData)
            .addOnSuccessListener { documentReference ->
                callback(true, documentReference.id)
            }
            .addOnFailureListener { exception ->
                logError("Error adding document", exception)
                callback(false, null)
            }
    }

    fun addLeaderboardToUser(leaderboardId: String, userId: String) {
        updateUserField(userId, "Leaderboards", FieldValue.arrayUnion(leaderboardId)) {
            logError("Failed to add leaderboard ID to user document", it)
        }
    }

    fun getLeaderboardsForUser(userId: String, onComplete: (List<String>) -> Unit) {
        userCollection.document(userId).get()
            .addOnSuccessListener { documentSnapshot ->
                val leaderboardIds = documentSnapshot.get("Leaderboards") as? List<String> ?: emptyList()
                onComplete(leaderboardIds)
            }
            .addOnFailureListener { exception ->
                logError("Failed to get leaderboards for user", exception)
                onComplete(emptyList())
            }
    }

    fun addPlayer(leaderboardId: String, userName: String) {
        updateLeaderboardField(leaderboardId, "users", FieldValue.arrayUnion(mapOf("name" to userName, "points" to 0))) {
            logError("Failed to add player", it)
        }
    }

    fun removePlayer(leaderboardId: String, userName: String) {
        updateLeaderboardField(leaderboardId, "users", FieldValue.arrayRemove(mapOf("name" to userName, "points" to 0))) {
            logError("Failed to remove player", it)
        }
    }

    fun getLeaderboard(leaderboardId: String, callback: (Map<String, Any>?) -> Unit) {
        leaderboardCollection.document(leaderboardId).get()
            .addOnSuccessListener { documentSnapshot ->
                if (documentSnapshot.exists()) {
                    callback(documentSnapshot.data)
                } else {
                    logError("Leaderboard not found")
                    callback(null)
                }
            }
            .addOnFailureListener { exception ->
                logError("Error fetching leaderboard", exception)
                callback(null)
            }
    }

    fun addPointsToUser(leaderboardId: String, userName: String, points: Int) {
        updateLeaderboardField(leaderboardId, "users.${getUserIndex(userName)}.points", points) {
            logError("Failed to add points", it)
        }
    }

    fun removePointsFromUser(leaderboardId: String, userName: String, points: Int) {
        updateLeaderboardField(leaderboardId, "users.${getUserIndex(userName)}.points", points) {
            logError("Failed to remove points", it)
        }
    }

    fun getUserByDisplayName(displayName: String, callback: (String?) -> Unit) {
        userCollection.whereEqualTo("displayName", displayName).get()
            .addOnSuccessListener { querySnapshot ->
                if (!querySnapshot.isEmpty) {
                    val userId = querySnapshot.documents[0].id
                    callback(userId)
                } else {
                    logError("User with display name $displayName not found")
                    callback(null)
                }
            }
            .addOnFailureListener { exception ->
                logError("Failed to retrieve user by display name", exception)
                callback(null)
            }
    }

    private fun getUserIndex(userName: String): String = userName

    private fun updateLeaderboardField(leaderboardId: String, field: String, value: Any, onFailure: (Exception) -> Unit) {
        leaderboardCollection.document(leaderboardId)
            .update(field, value)
            .addOnFailureListener(onFailure)
    }

    private fun updateUserField(userId: String, field: String, value: Any, onFailure: (Exception) -> Unit) {
        userCollection.document(userId)
            .update(field, value)
            .addOnFailureListener(onFailure)
    }

    private fun logError(message: String, exception: Exception? = null) {
        if (exception != null) {
            Log.e("LeaderboardManager", "$message: ${exception.message}")
        } else {
            Log.e("LeaderboardManager", message)
        }
    }
}
