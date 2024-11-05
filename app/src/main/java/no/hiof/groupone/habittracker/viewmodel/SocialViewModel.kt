package no.hiof.groupone.habittracker.viewmodel

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch
import no.hiof.groupone.habittracker.model.SocialManager

class SocialViewModel : ViewModel() {

    private val _friendName = mutableStateOf("")
    val friendName: State<String> = _friendName
    fun updateFriendName(name: String) { _friendName.value = name }

    private val _friendsList = mutableStateOf<List<Pair<String, String>>>(emptyList())
    val friendsList: State<List<Pair<String, String>>> = _friendsList

    private val _userHabits = mutableStateOf<List<Pair<String, String>>>(emptyList())
    val userHabits: State<List<Pair<String, String>>> = _userHabits

    private val _habitRequests = MutableLiveData<List<Map<String, Any>>>()
    val habitRequests: LiveData<List<Map<String, Any>>> = _habitRequests

    private val _friendRequests = mutableStateOf<List<Pair<String, String>>>(emptyList())
    val friendRequests: State<List<Pair<String, String>>> = _friendRequests

    private val socialManager = SocialManager()



    fun loadFriends() {
        val currentUser = FirebaseAuth.getInstance().currentUser ?: return
        viewModelScope.launch {
            socialManager.getFriends(currentUser.uid) { friends ->
                _friendsList.value = friends
            }
        }
    }

    fun loadUserHabits() {
        val currentUser = FirebaseAuth.getInstance().currentUser ?: return
        viewModelScope.launch {
            socialManager.getUserHabits(currentUser.uid) { habits ->
                _userHabits.value = habits
            }
        }
    }

    fun loadHabitRequests() {
        val currentUser = FirebaseAuth.getInstance().currentUser ?: return
        socialManager.getHabitRequests(currentUser.uid) { requests ->
            _habitRequests.value = requests
        }
    }

    fun sendHabitRequest(habitId: String, habitName: String, recipientId: String) {
        val currentUser = FirebaseAuth.getInstance().currentUser ?: return

        socialManager.sendHabitRequest(habitId, habitName, recipientId, currentUser.uid, currentUser.displayName ?: "") { success ->
            if (success) {
                // Evt legge til return
            }
        }
    }

    fun respondToHabitRequest(habitId: String, senderId: String, accept: Boolean) {
        val currentUser = FirebaseAuth.getInstance().currentUser ?: return

        socialManager.respondToHabitRequest(currentUser.uid, habitId, senderId, accept) { success ->
            if (success) {
                loadHabitRequests()
            }
        }
    }



    fun sendFriendRequest() {
        val currentUser = FirebaseAuth.getInstance().currentUser ?: return

        viewModelScope.launch {
            val friendDisplayName = _friendName.value
            socialManager.sendFriendRequest(currentUser.uid, currentUser.displayName ?: "", friendDisplayName) { success ->
                if (success) {
                    updateFriendName("")
                }
            }
        }
    }


    fun loadFriendRequests() {
        val currentUser = FirebaseAuth.getInstance().currentUser ?: return

        viewModelScope.launch {
            socialManager.getFriendRequests(currentUser.uid) { requests ->
                _friendRequests.value = requests.map { Pair(it["senderId"] as String, it["senderDisplayName"] as String) }
            }
        }
    }

    fun respondToFriendRequest(senderId: String, accept: Boolean) {
        val currentUser = FirebaseAuth.getInstance().currentUser ?: return

        viewModelScope.launch {
            socialManager.respondToFriendRequest(currentUser.uid, senderId, accept) { success ->
                if (success) {
                    _friendRequests.value = _friendRequests.value.filter { it.first != senderId }
                    if (accept) loadFriends()
                }
            }
        }
    }

    fun removeFriend(friendId: String) {
        val currentUser = FirebaseAuth.getInstance().currentUser ?: return

        viewModelScope.launch {
            socialManager.removeFriend(currentUser.uid, friendId) { success ->
                if (success) {
                    loadFriends()
                }
            }
        }
    }

}
