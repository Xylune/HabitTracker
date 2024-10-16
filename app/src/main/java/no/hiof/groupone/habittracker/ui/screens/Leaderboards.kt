package no.hiof.groupone.habittracker.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import no.hiof.groupone.habittracker.model.LeaderboardManager
import no.hiof.groupone.habittracker.viewmodel.AuthViewModel
import no.hiof.groupone.habittracker.viewmodel.LeaderboardViewModel

@Composable
fun LeaderboardScreen(
    modifier: Modifier = Modifier,
    navController: NavController = rememberNavController(),
    authViewModel: AuthViewModel = viewModel(),
    leaderboardViewModel: LeaderboardViewModel = viewModel()
) {
    var showCreateDialog by remember { mutableStateOf(false) }
    var selectedLeaderboard by remember { mutableStateOf<LeaderboardManager.Leaderboard?>(null) }
    var showLeaderboardSelection by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        leaderboardViewModel.loadFriends()
        leaderboardViewModel.loadUserLeaderboards()
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            Button(
                onClick = { showLeaderboardSelection = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
            ) {
                Text(text = if (selectedLeaderboard == null) "Select Leaderboard" else selectedLeaderboard!!.name)
            }

            Spacer(modifier = Modifier.height(16.dp))

            selectedLeaderboard?.let {
                Text(text = "Leaderboard: ${it.name}", style = MaterialTheme.typography.headlineMedium)
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = "Players:", style = MaterialTheme.typography.titleMedium)
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentPadding = PaddingValues(16.dp)
                ) {
                    items(it.users) { user ->
                        Text(text = "${user.name}: ${user.points} points")
                        Spacer(modifier = Modifier.height(4.dp))
                    }
                }
            } ?: run {
                Text(text = "No leaderboard selected", style = MaterialTheme.typography.bodyMedium)
            }
        }

        Button(
            onClick = { showCreateDialog = true },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(8.dp)
        ) {
            Text(text = "Create New Leaderboard")
        }

        if (showCreateDialog) {
            CreateLeaderboardDialog(
                onDismiss = { showCreateDialog = false },
                onCreate = { leaderboardName, selectedFriends ->
                    leaderboardViewModel.createLeaderboard(
                        leaderboardName = leaderboardName,
                        selectedFriends = selectedFriends
                    )
                    showCreateDialog = false
                },
                friends = leaderboardViewModel.friends
            )
        }

        if (showLeaderboardSelection) {
            LeaderboardSelectionDialog(
                onDismiss = { showLeaderboardSelection = false },
                leaderboards = leaderboardViewModel.leaderboardDetails.value ?: listOf(),
                onSelect = { selectedLeaderboard = it; showLeaderboardSelection = false }
            )
        }
    }
}

@Composable
fun CreateLeaderboardDialog(
    onDismiss: () -> Unit,
    onCreate: (String, List<String>) -> Unit,
    friends: List<String>
) {
    var leaderboardName by remember { mutableStateOf("") }
    val selectedFriends = remember { mutableStateOf(setOf<String>()) }

    AlertDialog(
        onDismissRequest = { onDismiss() },
        title = { Text(text = "Create New Leaderboard") },
        text = {
            Column {
                TextField(
                    value = leaderboardName,
                    onValueChange = { leaderboardName = it },
                    label = { Text("Leaderboard Name") }
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(text = "Select Friends:")

                if (friends.isNotEmpty()) {
                    LazyColumn(modifier = Modifier.height(150.dp)) {
                        items(friends) { friend ->
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Checkbox(
                                    checked = selectedFriends.value.contains(friend),
                                    onCheckedChange = { checked ->
                                        if (checked) {
                                            selectedFriends.value = selectedFriends.value + friend
                                        } else {
                                            selectedFriends.value = selectedFriends.value - friend
                                        }
                                    }
                                )
                                Text(text = friend)
                            }
                        }
                    }
                } else {
                    Text("Loading friends...")
                }
            }
        },
        confirmButton = {
            Button(onClick = { onCreate(leaderboardName, selectedFriends.value.toList()) }) {
                Text(text = "Create")
            }
        },
        dismissButton = {
            Button(onClick = { onDismiss() }) {
                Text(text = "Cancel")
            }
        }
    )
}

@Composable
fun LeaderboardSelectionDialog(
    onDismiss: () -> Unit,
    leaderboards: List<LeaderboardManager.Leaderboard>,
    onSelect: (LeaderboardManager.Leaderboard) -> Unit
) {
    AlertDialog(
        onDismissRequest = { onDismiss() },
        title = { Text(text = "Select Leaderboard") },
        text = {
            if (leaderboards.isNotEmpty()) {
                LazyColumn {
                    items(leaderboards) { leaderboard ->
                        Button(onClick = { onSelect(leaderboard) }) {
                            Text(text = leaderboard.name)
                        }
                    }
                }
            } else {
                Text("No leaderboards available.")
            }
        },
        confirmButton = {},
        dismissButton = {
            Button(onClick = { onDismiss() }) {
                Text(text = "Cancel")
            }
        }
    )
}

//@Composable
//fun LeaderboardAdminPanel(
//    onDismiss: () -> Unit
//) {
//    onDismissRequest = { onDismiss() },
//    title = { Text(text ="Leaderboard Adminpanel") },
//
//}