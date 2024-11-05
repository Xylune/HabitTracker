package no.hiof.groupone.habittracker.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
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
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import no.hiof.groupone.habittracker.R
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
    val friendRequests by socialViewModel.friendRequests
    var showFriendRequestsDialog by remember { mutableStateOf(false) }
    var showHabitRequestsDialog by remember { mutableStateOf(false) }
    val habitRequests by socialViewModel.habitRequests.observeAsState(emptyList())

    var shareHabitDialog by remember { mutableStateOf(false) }
    var selectedHabitId by remember { mutableStateOf("") }
    var selectedHabitName by remember { mutableStateOf("") }
    var selectedFriendName by remember { mutableStateOf("") }
    var selectedFriendId by remember { mutableStateOf("") }
    var showFriendsListDialog by remember { mutableStateOf(false) }
    var showSelectFriendDialog by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }
    var showSnackbar by remember { mutableStateOf(false) }
    var snackbarMessage by remember { mutableStateOf("") }
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        socialViewModel.loadFriends()
        socialViewModel.loadUserHabits()
        socialViewModel.loadFriendRequests()
        socialViewModel.loadHabitRequests()
    }


    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = stringResource(R.string.manage_friends_label),
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextField(
                value = friendName,
                onValueChange = { socialViewModel.updateFriendName(it) },
                label = { Text(stringResource(R.string.lbl_friends_name)) },
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 8.dp)
            )

            Button(
                onClick = {
                    socialViewModel.sendFriendRequest()
                    snackbarMessage = context.getString(R.string.snackbar_friend_request_sent)
                    showSnackbar = true
                },
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                    .padding(vertical = 4.dp)
            ) {
                Text(stringResource(R.string.btn_send_friend_request))
            }
        }

        Button(
            onClick = { showFriendRequestsDialog = true },
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier
                .padding(vertical = 8.dp)
        ) {
            Text(stringResource(R.string.btn_view_friend_requests))
        }

        if (showFriendRequestsDialog) {
            AlertDialog(
                onDismissRequest = { showFriendRequestsDialog = false },
                title = { Text(stringResource(R.string.lbl_friend_requests)) },
                text = {
                    Column {
                        if (friendRequests.isEmpty()) {
                            Text(text = stringResource(R.string.no_friend_requests))
                        } else {
                            friendRequests.forEach { (senderId, senderName) ->
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp)
                                ) {
                                    Text(text = senderName)
                                    Row(
                                        horizontalArrangement = Arrangement.End,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Button(
                                            onClick = {
                                                socialViewModel.respondToFriendRequest(senderId, true)
                                                snackbarMessage = context.getString(R.string.snackbar_friend_request_accepted)
                                                showSnackbar = true
                                            }
                                        ) {
                                            Text(stringResource(R.string.btn_accept))
                                        }
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Button(
                                            onClick = {
                                                socialViewModel.respondToFriendRequest(senderId, false)
                                                snackbarMessage = context.getString(R.string.snackbar_friend_request_denied)
                                                showSnackbar = true
                                            }
                                        ) {
                                            Text(stringResource(R.string.btn_deny))
                                        }
                                    }
                                }
                            }
                        }
                    }
                },
                confirmButton = {
                    Button(onClick = { showFriendRequestsDialog = false }) {
                        Text(stringResource(R.string.btn_close))
                    }
                }
            )
        }


        Spacer(modifier = Modifier.height(10.dp))

        Button(
            onClick = { showFriendsListDialog = true },
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier
                .padding(vertical = 8.dp)
        ) {
            Text(stringResource(R.string.btn_friends_list))
        }

        if (showFriendsListDialog) {
            AlertDialog(
                onDismissRequest = { showFriendsListDialog = false },
                title = { Text(stringResource(R.string.lbl_friends_list)) },
                text = {
                    Column {
                        if (friendsList.isEmpty()) {
                            Text(text = stringResource(R.string.no_friends))
                        } else {
                            friendsList.forEach { (friendId, friendName) ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(text = friendName)

                                    Button(
                                        onClick = {
                                            socialViewModel.removeFriend(friendId)
                                            snackbarMessage = context.getString(R.string.snackbar_friend_removed)
                                            showSnackbar = true
                                        }
                                    ) {
                                        Text(stringResource(R.string.btn_remove_friend))
                                    }
                                }
                            }
                        }
                    }
                },
                confirmButton = {
                    Button(onClick = { showFriendsListDialog = false }) {
                        Text(stringResource(R.string.btn_close))
                    }
                }
            )
        }

        Spacer(modifier = Modifier.height(20.dp))
        Text(
            text = stringResource(R.string.share_habit_label),
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        Button(
            onClick = { shareHabitDialog = true },
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier
        ) {
            Text(stringResource(R.string.btn_select_habit))
        }

        if (shareHabitDialog) {
            AlertDialog(
                onDismissRequest = { shareHabitDialog = false },
                title = { Text(stringResource(R.string.lbl_select_habit_to_share)) },
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
                            ) {
                                Text(habitName)
                            }
                        }
                    }
                },
                confirmButton = {
                    Button(onClick = { shareHabitDialog = false }) {
                        Text(stringResource(R.string.btn_cancel))
                    }
                }
            )
        }

        TextField(
            value = selectedHabitName,
            onValueChange = {},
            label = { Text(stringResource(R.string.lbl_selected_habit)) },
            modifier = Modifier
                .fillMaxWidth(),
            enabled = false
        )

        Button(
            onClick = { showSelectFriendDialog = true },
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier
        ) {
            Text(stringResource(R.string.btn_select_friend))
        }

        TextField(
            value = selectedFriendName,
            onValueChange = {},
            label = { Text(stringResource(R.string.lbl_selected_friend)) },
            modifier = Modifier
                .fillMaxWidth(),
            enabled = false
        )

        if (showSelectFriendDialog) {
            AlertDialog(
                onDismissRequest = { showSelectFriendDialog = false },
                title = { Text(stringResource(R.string.lbl_select_a_friend)) },
                text = {
                    Column {
                        friendsList.forEach { (friendId, friendName) ->
                            Button(
                                onClick = {
                                    selectedFriendName = friendName
                                    selectedFriendId = friendId
                                    showSelectFriendDialog = false
                                },
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                            ) {
                                Text(friendName)
                            }
                        }
                    }
                },
                confirmButton = {
                    Button(onClick = { showSelectFriendDialog = false }) {
                        Text(stringResource(R.string.btn_cancel))
                    }
                }
            )
        }

        Button(
            onClick = {
                if (selectedHabitId.isNotEmpty() && selectedFriendId.isNotEmpty()) {
                    socialViewModel.sendHabitRequest(selectedHabitId, selectedHabitName, selectedFriendId)
                    snackbarMessage = context.getString(R.string.snackbar_habit_shared_success)
                    showSnackbar = true
                    selectedHabitId = ""
                    selectedHabitName = ""
                    selectedFriendId = ""
                    selectedFriendName = ""
                }
            },
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier
        ) {
            Text(stringResource(R.string.btn_share))
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )

        Button(
            onClick = { showHabitRequestsDialog = true },
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier.padding(vertical = 8.dp)
        ) {
            Text(stringResource(R.string.btn_view_habit_requests))
        }

        if (showHabitRequestsDialog) {
            AlertDialog(
                onDismissRequest = { showHabitRequestsDialog = false },
                title = { Text(stringResource(R.string.lbl_habit_requests)) },
                text = {
                    Column {
                        if (habitRequests.isEmpty()) {
                            Text(text = stringResource(R.string.no_habit_requests))
                        } else {
                            habitRequests.forEach { request ->
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp)
                                ) {
                                    Text(
                                        text = "${request["senderName"]} requests to share habit: ${request["habitName"]}",
                                        modifier = Modifier.padding(bottom = 8.dp)
                                    )
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Button(
                                            onClick = {
                                                socialViewModel.respondToHabitRequest(
                                                    request["habitId"] as String,
                                                    request["senderId"] as String,
                                                    true
                                                )
                                                snackbarMessage = context.getString(R.string.snackbar_habit_request_accepted)
                                                showSnackbar = true
                                            }
                                        ) {
                                            Text(stringResource(R.string.btn_accept))
                                        }

                                        Button(
                                            onClick = {
                                                socialViewModel.respondToHabitRequest(
                                                    request["habitId"] as String,
                                                    request["senderId"] as String,
                                                    false
                                                )
                                                snackbarMessage = context.getString(R.string.snackbar_habit_request_denied)
                                                showSnackbar = true
                                            }
                                        ) {
                                            Text(stringResource(R.string.btn_deny))
                                        }
                                    }
                                }
                            }
                        }
                    }
                },
                confirmButton = {
                    Button(onClick = { showHabitRequestsDialog = false }) {
                        Text(stringResource(R.string.btn_close))
                    }
                }
            )
        }

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
