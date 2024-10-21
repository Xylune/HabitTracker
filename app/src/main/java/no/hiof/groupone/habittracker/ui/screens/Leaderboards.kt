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
    var showAdminPanel by remember { mutableStateOf(false) }
    var isAdmin by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        leaderboardViewModel.loadFriends()
//        leaderboardViewModel.loadUserLeaderboards()
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
                onClick = {
                    leaderboardViewModel.loadUserLeaderboards()
                    showLeaderboardSelection = true
                },
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

                LaunchedEffect(selectedLeaderboard) {
                    isAdmin = leaderboardViewModel.isAdmin(it)
                }
            } ?: run {
                Text(text = "No leaderboard selected", style = MaterialTheme.typography.bodyMedium)
            }
        }

        if (isAdmin) {
            Button(
                onClick = { showAdminPanel = true },
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .padding(8.dp)
            ) {
                Text(text = "Manage Leaderboard")
            }
        }

        Button(
            onClick = { showCreateDialog = true },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(bottom = if (isAdmin) 72.dp else 8.dp)
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
                onSelect = {
                    selectedLeaderboard = it
                    showLeaderboardSelection = false
                }
            )
        }

        if (showAdminPanel && selectedLeaderboard != null) {
            LeaderboardAdminPanel(
                leaderboard = selectedLeaderboard!!,
                onDismiss = { showAdminPanel = false },
                leaderboardViewModel = leaderboardViewModel
            )
        }
    }
}

@Composable
fun LeaderboardAdminPanel(
    leaderboard: LeaderboardManager.Leaderboard,
    onDismiss: () -> Unit,
    leaderboardViewModel: LeaderboardViewModel
) {
    AlertDialog(
        onDismissRequest = { onDismiss() },
        title = { Text(text = "Manage ${leaderboard.name}") },
        text = {
            Column {
                Text(text = "Current Players:")
                LazyColumn(modifier = Modifier.height(150.dp)) {
                    items(leaderboard.users) { user ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth().padding(4.dp)
                        ) {
                            Text(text = user.name)
                            Spacer(modifier = Modifier.weight(1f))
                            Button(onClick = {
                                leaderboardViewModel.modifyPlayerInLeaderboard(
                                    leaderboardId = leaderboard.name,  // You can also pass the leaderboard ID if needed
                                    userName = user.name,
                                    addPlayer = false // Removing player
                                )
                            }) {
                                Text(text = "Remove")
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = { /* Add new player functionality */ },
                    modifier = Modifier.fillMaxWidth().padding(8.dp)
                ) {
                    Text(text = "Add Player")
                }
            }
        },
        confirmButton = {
            Button(onClick = { onDismiss() }) {
                Text(text = "Close")
            }
        }
    )
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
