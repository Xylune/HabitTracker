package no.hiof.groupone.habittracker.viewmodel

import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import no.hiof.groupone.habittracker.model.LeaderboardManager
import no.hiof.groupone.habittracker.model.SocialManager

class LeaderboardViewModel(
    private val socialManager: SocialManager = SocialManager()
) : ViewModel() {

    private val leaderboardManager = LeaderboardManager()
    val friends = mutableStateListOf<String>()

    private val _leaderboardDetails = MutableLiveData<List<LeaderboardManager.Leaderboard>>()
    val leaderboardDetails: LiveData<List<LeaderboardManager.Leaderboard>> = _leaderboardDetails

    private val _userLeaderboards = MutableLiveData<List<String>>()
    val userLeaderboards: LiveData<List<String>> = _userLeaderboards

    fun isAdmin(leaderboard: LeaderboardManager.Leaderboard?): Boolean {
        val currentUser = FirebaseAuth.getInstance().currentUser
        return currentUser?.uid == leaderboard?.admin
    }

    fun loadUserLeaderboards() {
        val currentUser = FirebaseAuth.getInstance().currentUser
        currentUser?.let { user ->
            leaderboardManager.getLeaderboardsForUser(user.uid) { leaderboardIds ->
                _userLeaderboards.value = leaderboardIds
                loadLeaderboardDetails(leaderboardIds)
            }
        }
    }

    private fun loadLeaderboardDetails(leaderboardIds: List<String>) {
        val details = mutableListOf<LeaderboardManager.Leaderboard>()
        var pending = leaderboardIds.size

        leaderboardIds.forEach { leaderboardId ->
            leaderboardManager.getLeaderboard(leaderboardId) { leaderboardData ->
                leaderboardData?.let {
                    val leaderboard = LeaderboardManager.Leaderboard(
                        name = it["name"] as? String ?: "Unknown",
                        users = (it["users"] as? List<Map<String, Any>>)?.map { userMap ->
                            LeaderboardManager.User(
                                name = userMap["name"] as? String ?: "Unknown",
                                points = userMap["points"] as? Int ?: 0
                            )
                        } ?: listOf(),
                        admin = it["admin"] as? String ?: "Unknown"
                    )
                    details.add(leaderboard)
                }
                pending--
                if (pending == 0) {
                    _leaderboardDetails.value = details
                }
            }
        }
    }

    fun loadFriends() {
        val currentUser = FirebaseAuth.getInstance().currentUser
        currentUser?.let { user ->
            socialManager.getFriends(user.uid) { friendList ->
                friends.clear()
                friends.addAll(friendList)
            }
        }
    }

    fun createLeaderboard(leaderboardName: String, selectedFriends: List<String>) {
        val currentUser = FirebaseAuth.getInstance().currentUser
        currentUser?.let { user ->
            val allPlayers = mutableListOf(user.displayName ?: "Unknown").apply {
                addAll(selectedFriends)
            }


            leaderboardManager.createNewLeaderboard(leaderboardName, allPlayers, user.uid) { success, leaderboardId ->
                if (success && leaderboardId != null) {
                    addPlayersToLeaderboard(leaderboardId, allPlayers)
                    loadUserLeaderboards()
                } else {
                    println("Failed to create leaderboard.")
                }
            }
        } ?: run {
            println("Error: Current user is null, cannot create leaderboard")
        }
    }

    private fun addPlayersToLeaderboard(leaderboardId: String, players: List<String>) {
        players.forEach { playerName ->
            leaderboardManager.addPlayer(leaderboardId, playerName)
            leaderboardManager.getUserByDisplayName(playerName) { userId ->
                userId?.let {
                    leaderboardManager.addLeaderboardToUser(leaderboardId, it)
                }
            }
        }
    }

    fun updatePoints(leaderboardId: String, userName: String, points: Int, addPoints: Boolean) {
        if (addPoints) {
            leaderboardManager.addPointsToUser(leaderboardId, userName, points)
        } else {
            leaderboardManager.removePointsFromUser(leaderboardId, userName, points)
        }
    }

    fun modifyPlayerInLeaderboard(leaderboardId: String, userName: String, addPlayer: Boolean) {
        if (addPlayer) {
            leaderboardManager.addPlayer(leaderboardId, userName)
        } else {
            leaderboardManager.removePlayer(leaderboardId, userName)
        }
    }
}
