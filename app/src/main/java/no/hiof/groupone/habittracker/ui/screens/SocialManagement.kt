package no.hiof.groupone.habittracker.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import no.hiof.groupone.habittracker.viewmodel.AuthViewModel
import no.hiof.groupone.habittracker.viewmodel.SocialViewModel

@Composable
fun SocialManagement(
    modifier: Modifier = Modifier,
    navController: NavController = rememberNavController(),
    authViewModel: AuthViewModel = viewModel(),
    socialViewModel: SocialViewModel = viewModel()
) {
    val friendName by socialViewModel.friendName
    val friendsList by socialViewModel.friendsList

    LaunchedEffect(Unit) {
        socialViewModel.loadFriends()
    }

    Column(modifier = modifier.fillMaxSize().padding(16.dp)) {
        Text(text = "Manage Friends", modifier = Modifier.padding(bottom = 16.dp))

        TextField(
            value = friendName,
            onValueChange = { socialViewModel.updateFriendName(it) },
            label = { Text("Friend's Display Name") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        Button(onClick = { socialViewModel.addFriend() }) {
            Text("Add Friend")
        }

        Spacer(modifier = Modifier.height(8.dp))

        Button(onClick = { socialViewModel.removeFriend() }) {
            Text("Remove Friend")
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(text = "Friends List:")
        friendsList.forEach { friend ->
            Text(text = friend)
        }
    }
}
