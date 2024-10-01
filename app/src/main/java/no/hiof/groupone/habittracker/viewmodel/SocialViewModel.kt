package no.hiof.groupone.habittracker.viewmodel

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch
import no.hiof.groupone.habittracker.model.SocialManager

class SocialViewModel : ViewModel() {

    private val _friendName = mutableStateOf("")
    val friendName: State<String> = _friendName
    fun updateFriendName(name: String) { _friendName.value = name }

    private val _friendsList = mutableStateOf<List<String>>(emptyList())
    val friendsList: State<List<String>> = _friendsList

    private val _userHabits = mutableStateOf<List<Pair<String, String>>>(emptyList())
    val userHabits: State<List<Pair<String, String>>> = _userHabits

    private val socialManager = SocialManager()

    fun loadFriends() {
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser == null) {
            return
        }
        viewModelScope.launch {
            socialManager.getFriends(currentUser.uid) { friends ->
                _friendsList.value = friends
            }
        }
    }

    fun addFriend() {
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser == null) {
            return
        }
        viewModelScope.launch {
            val friendDisplayName = _friendName.value
            socialManager.addFriend(currentUser.uid, friendDisplayName) { success ->
                if (success) {
                    _friendsList.value = _friendsList.value + friendDisplayName
                    updateFriendName("")
                }
            }
        }
    }

    fun removeFriend() {
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser == null) {
            return
        }
        viewModelScope.launch {
            val friendDisplayName = _friendName.value
            socialManager.removeFriend(currentUser.uid, friendDisplayName) { success ->
                if (success) {
                    _friendsList.value = _friendsList.value.filter { it != friendDisplayName }
                    updateFriendName("")
                }
            }
        }
    }

    fun loadUserHabits() {
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser == null) {
            return
        }
        viewModelScope.launch {
            socialManager.getUserHabits(currentUser.uid) { habits ->
                _userHabits.value = habits
            }
        }
    }

    fun shareHabit(habitId: String, friendDisplayName: String) {
        viewModelScope.launch {
            socialManager.shareHabit(habitId, friendDisplayName) { success ->
            }
        }
    }
}
