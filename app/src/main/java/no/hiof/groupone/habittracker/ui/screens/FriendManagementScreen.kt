package no.hiof.groupone.habittracker

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import no.hiof.groupone.habittracker.ui.screens.ViewModel
import no.hiof.groupone.habittracker.model.SocialManager

@Composable
fun FriendManagementScreen(modifier: Modifier = Modifier, navController: NavHostController, viewModel: ViewModel) {
    var displayName by remember { mutableStateOf("") }
    val friends = remember { mutableStateListOf<String>() }
    val socialManager = SocialManager()
    val currentUser = viewModel.getCurrentUser()

    LaunchedEffect(Unit) {
        currentUser?.uid?.let { userId ->
            socialManager.getFriends(userId) { friendList ->
                friends.clear()
                friends.addAll(friendList)
            }
        }
    }

    Column(modifier = modifier.fillMaxSize().padding(16.dp)) {
        Text(text = "Manage Friends", modifier = Modifier.padding(bottom = 16.dp))

        TextField(
            value = displayName,
            onValueChange = { displayName = it },
            label = { Text("Friend's Display Name") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        Button(onClick = {
            currentUser?.uid?.let { userId ->
                socialManager.addFriend(userId, displayName)
                friends.add(displayName)
                displayName = ""
            }
        }) {
            Text("Add Friend")
        }

        Spacer(modifier = Modifier.height(8.dp))

        Button(onClick = {
            currentUser?.uid?.let { userId ->
                socialManager.removeFriend(userId, displayName)
                friends.remove(displayName)
                displayName = ""
            }
        }) {
            Text("Remove Friend")
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(text = "Friends List:", modifier = Modifier.padding(bottom = 8.dp))
        friends.forEach { friend ->
            Text(text = friend, modifier = Modifier.padding(vertical = 4.dp))
        }
    }
}
