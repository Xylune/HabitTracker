package no.hiof.groupone.habittracker.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
    var showSelectFriendDialog by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }
    var showSnackbar by remember { mutableStateOf(false) }
    var snackbarMessage by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        socialViewModel.loadFriends()
        socialViewModel.loadUserHabits()
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Manage Friends",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        TextField(
            value = friendName,
            onValueChange = { socialViewModel.updateFriendName(it) },
            label = { Text("Friend's Name") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
        )

        Button(
            onClick = {
                socialViewModel.addFriend()
                snackbarMessage = "Friend added successfully"
                showSnackbar = true
            },
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
        ) {
            Text("Add Friend")
        }

        Button(
            onClick = {
                socialViewModel.removeFriend()
                snackbarMessage = "Friend removed successfully"
                showSnackbar = true
            },
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
        ) {
            Text("Remove Friend")
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = { showFriendsListDialog = true },
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
        ) {
            Text("Friends List")
        }

        if (showFriendsListDialog) {
            AlertDialog(
                onDismissRequest = { showFriendsListDialog = false },
                title = { Text("Friends List") },
                text = {
                    Column {
                        friendsList.forEach { friend ->
                            Text(
                                text = friend,
                                modifier = Modifier
                                    .padding(vertical = 4.dp)
                            )
                        }
                    }
                },
                confirmButton = {
                    Button(onClick = { showFriendsListDialog = false }) {
                        Text("Close")
                    }
                }
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        Button(
            onClick = { shareHabitDialog = true },
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
        ) {
            Text("Select Habit")
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
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
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
            modifier = Modifier
                .fillMaxWidth(),
            enabled = false
        )

        Button(
            onClick = { showSelectFriendDialog = true },
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier
                .fillMaxWidth()
        ) {
            Text("Select Friend")
        }

        TextField(
            value = selectedFriendName,
            onValueChange = {},
            label = { Text("Selected Friend") },
            modifier = Modifier
                .fillMaxWidth(),
            enabled = false
        )

        if (showSelectFriendDialog) {
            AlertDialog(
                onDismissRequest = { showSelectFriendDialog = false },
                title = { Text("Select a Friend") },
                text = {
                    Column {
                        friendsList.forEach { friend ->
                            Button(
                                onClick = {
                                    selectedFriendName = friend
                                    showSelectFriendDialog = false
                                },
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                            ) {
                                Text(friend)
                            }
                        }
                    }
                },
                confirmButton = {
                    Button(onClick = { showSelectFriendDialog = false }) {
                        Text("Cancel")
                    }
                }
            )
        }

        Button(
            onClick = {
                if (selectedHabitId.isNotEmpty() && selectedFriendName.isNotEmpty()) {
                    socialViewModel.shareHabit(selectedHabitId, selectedFriendName)
                    snackbarMessage = "Habit shared successfully"
                    showSnackbar = true
                    selectedHabitId = ""
                    selectedHabitName = ""
                    selectedFriendName = ""
                }
            },
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier
                .fillMaxWidth()
        ) {
            Text("Share")
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )

        LaunchedEffect(key1 = showSnackbar) {
            if (showSnackbar) {
                snackbarHostState.showSnackbar(
                    message = snackbarMessage,
                    duration = SnackbarDuration.Short
                )
                showSnackbar = false
            }
        }
    }
}
