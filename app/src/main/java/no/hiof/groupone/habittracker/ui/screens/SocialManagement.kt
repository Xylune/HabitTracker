package no.hiof.groupone.habittracker.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
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
    val userHabits by socialViewModel.userHabits
    var shareHabitDialog by remember { mutableStateOf(false) }
    var selectedHabitId by remember { mutableStateOf("") }
    var selectedHabitName by remember { mutableStateOf("") }
    var selectedFriendName by remember { mutableStateOf("") }
    var showFriendsListDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        socialViewModel.loadFriends()
        socialViewModel.loadUserHabits()
    }

    Column(modifier = modifier.fillMaxSize().padding(16.dp)) {
        Text(text = "Manage Friends", modifier = Modifier.padding(bottom = 16.dp))

        TextField(
            value = friendName,
            onValueChange = { socialViewModel.updateFriendName(it) },
            label = { Text("Friend's Name") },
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

        Spacer(modifier = Modifier.height(8.dp))

        Text(text = "Friends List:")
        friendsList.forEach { friend ->
            Text(text = friend)
        }

        Spacer(modifier = Modifier.height(20.dp))

        Button(onClick = { shareHabitDialog = true }) {
            Text("Habit")
        }

        if (shareHabitDialog) {
            AlertDialog(
                onDismissRequest = { shareHabitDialog = false },
                title = { Text("Select Habit to Share") },
                text = {
                    Column {
                        userHabits.forEach { (habitId, habitName) ->
                            Button(
                                onClick = {
                                    selectedHabitId = habitId
                                    selectedHabitName = habitName
                                    shareHabitDialog = false
                                },
                                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                            ) {
                                Text(habitName)
                            }
                        }
                    }
                },
                confirmButton = {
                    Button(onClick = { shareHabitDialog = false }) {
                        Text("Cancel")
                    }
                }
            )
        }

        TextField(
            value = selectedHabitName,
            onValueChange = {},
            label = { Text("Selected Habit") },
            modifier = Modifier.fillMaxWidth(),
            enabled = false
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = { showFriendsListDialog = true }) {
            Text("Friends List")
        }

        TextField(
            value = selectedFriendName,
            onValueChange = {},
            label = { Text("Selected Friend") },
            modifier = Modifier.fillMaxWidth(),
            enabled = false
        )

        Spacer(modifier = Modifier.height(16.dp))

        if (showFriendsListDialog) {
            AlertDialog(
                onDismissRequest = { showFriendsListDialog = false },
                title = { Text("Select a Friend") },
                text = {
                    Column {
                        friendsList.forEach { friend ->
                            Button(
                                onClick = {
                                    selectedFriendName = friend
                                    showFriendsListDialog = false
                                },
                                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                            ) {
                                Text(friend)
                            }
                        }
                    }
                },
                confirmButton = {
                    Button(onClick = { showFriendsListDialog = false }) {
                        Text("Cancel")
                    }
                }
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Button(
            onClick = {
                if (selectedHabitId.isNotEmpty() && selectedFriendName.isNotEmpty()) {
                    socialViewModel.shareHabit(selectedHabitId, selectedFriendName)
                    selectedHabitId = ""
                    selectedHabitName = ""
                    selectedFriendName = ""
                }
            }
        ) {
            Text("Share")
        }
    }
}
