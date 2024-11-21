package no.hiof.groupone.habittracker.ui.screens

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import no.hiof.groupone.habittracker.R
import no.hiof.groupone.habittracker.viewmodel.AuthState
import no.hiof.groupone.habittracker.viewmodel.AuthViewModel
import no.hiof.groupone.habittracker.viewmodel.HabitListViewModel
import no.hiof.groupone.habittracker.viewmodel.HabitsUiState
import no.hiof.groupone.habittracker.viewmodel.LeaderboardViewModel
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@RequiresApi(Build.VERSION_CODES.TIRAMISU)
@Composable
fun Home(
    modifier: Modifier = Modifier,
    navController: NavController,
    authViewModel: AuthViewModel,
    habitListViewModel: HabitListViewModel = viewModel(),
    leaderboardViewModel: LeaderboardViewModel = viewModel()
) {
    val authState = authViewModel.authState.observeAsState()
    val currentDate = remember { mutableStateOf(LocalDate.now()) }
    val habitsUiState by habitListViewModel.uiState.collectAsState()
    val leaderboardDetails by leaderboardViewModel.leaderboardDetails.observeAsState(emptyList())

    LaunchedEffect(Unit) {
        leaderboardViewModel.loadUserLeaderboards()
    }

    LaunchedEffect(currentDate.value) {
        habitListViewModel.updateSelectedDate(currentDate.value)
    }

    LaunchedEffect(authState.value) {
        when (authState.value) {
            is AuthState.Unauthenticated -> {
                navController.navigate("login")
            }

            else -> Unit
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconButton(onClick = {
                                currentDate.value = currentDate.value.minusDays(1)
                            }) {
                                Icon(
                                    Icons.AutoMirrored.Filled.ArrowBack,
                                    stringResource(R.string.lbl_previous_day)
                                )
                            }

                            Text(
                                text = currentDate.value.format(DateTimeFormatter.ofPattern("MMMM d, yyyy")),
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Medium
                            )

                            IconButton(onClick = {
                                currentDate.value = currentDate.value.plusDays(1)
                            }) {
                                Icon(
                                    Icons.AutoMirrored.Filled.ArrowForward,
                                    stringResource(R.string.lbl_next_day)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        when (habitsUiState) {
                            is HabitsUiState.Loading -> {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(200.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    CircularProgressIndicator()
                                }
                            }

                            is HabitsUiState.Success -> {
                                val habits = habitListViewModel.getHabitsForDate(currentDate.value)
                                if (habits.isEmpty()) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 32.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = stringResource(R.string.lbl_no_habits_scheduled),
                                            style = MaterialTheme.typography.bodyLarge,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                } else {
                                    LazyColumn(
                                        modifier = Modifier.heightIn(max = 300.dp)
                                    ) {
                                        items(habits) { habit ->
                                            ListItem(
                                                headlineContent = {
                                                    Text(
                                                        text = habit.name,
                                                        style = if (habit.isCompleted) {
                                                            MaterialTheme.typography.bodyLarge.copy(
                                                                textDecoration = TextDecoration.LineThrough,
                                                                color = MaterialTheme.colorScheme.onSurface.copy(
                                                                    alpha = 0.6f
                                                                )
                                                            )
                                                        } else {
                                                            MaterialTheme.typography.bodyLarge
                                                        }
                                                    )
                                                },
                                                supportingContent = habit.description?.let {
                                                    {
                                                        Text(
                                                            text = it,
                                                            style = if (habit.isCompleted) {
                                                                MaterialTheme.typography.bodyMedium.copy(
                                                                    color = MaterialTheme.colorScheme.onSurface.copy(
                                                                        alpha = 0.6f
                                                                    )
                                                                )
                                                            } else {
                                                                MaterialTheme.typography.bodyMedium
                                                            }
                                                        )
                                                    }
                                                },
                                                leadingContent = {
                                                    Checkbox(
                                                        checked = habit.isCompleted,
                                                        onCheckedChange = { isChecked ->
                                                            if (isChecked) {
                                                                habitListViewModel.markHabitAsComplete(
                                                                    habit
                                                                )
                                                            }
                                                        },
                                                        enabled = !habit.isCompleted
                                                    )
                                                },
                                                trailingContent = if (habit.isCompleted) {
                                                    {
                                                        Text(
                                                            text = "✓ Done",
                                                            style = MaterialTheme.typography.bodySmall,
                                                            color = MaterialTheme.colorScheme.primary
                                                        )
                                                    }
                                                } else null
                                            )
                                            HorizontalDivider()
                                        }
                                    }
                                }
                            }

                            is HabitsUiState.Error -> {
                                Text(
                                    text = (habitsUiState as HabitsUiState.Error).exception,
                                    color = MaterialTheme.colorScheme.error
                                )
                            }
                        }
                    }
                }

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.lbl_leaderboard),
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )

                        if (leaderboardDetails.isEmpty()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 32.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = stringResource(R.string.lbl_no_leaderboards_available),
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        } else {
                            val topUsers = leaderboardDetails.firstOrNull()?.users
                                ?.sortedByDescending { it.points }
                                ?.take(5) ?: emptyList()

                            Column {
                                Text(
                                    text = leaderboardDetails.firstOrNull()?.name ?: stringResource(
                                        R.string.lbl_leaderboard
                                    ),
                                    style = MaterialTheme.typography.titleMedium,
                                    modifier = Modifier.padding(bottom = 8.dp)
                                )

                                LazyColumn(
                                    modifier = Modifier.heightIn(max = 250.dp)
                                ) {
                                    items(topUsers) { user ->
                                        ListItem(
                                            headlineContent = { Text(user.name) },
                                            supportingContent = { Text("${user.points} points") },
                                            leadingContent = {
                                                val index = topUsers.indexOf(user)
                                                Text(
                                                    text = "#${index + 1}",
                                                    fontWeight = FontWeight.Bold,
                                                    color = when (index) {
                                                        0 -> Color(0xFFFFD700) // Gold
                                                        1 -> Color(0xFFC0C0C0) // Silver
                                                        2 -> Color(0xFFCD7F32) // Bronze
                                                        else -> MaterialTheme.colorScheme.onSurface
                                                    }
                                                )
                                            }
                                        )
                                        if (topUsers.indexOf(user) < topUsers.size - 1) {
                                            HorizontalDivider()
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    TextButton(
                        onClick = { navController.navigate("leaderboards") }
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.List,
                            contentDescription = stringResource(R.string.lbl_view_leaderboards)
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(stringResource(R.string.lbl_all_leaderboards))
                    }
                }

            }
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 120.dp)
                .zIndex(10F),
            contentAlignment = Alignment.BottomEnd
        ) {
            FloatingActionButton(
                onClick = { navController.navigate("createHabit") },
                modifier = Modifier.padding(end = 16.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = stringResource(R.string.lbl_create_habit)
                )
            }
        }
    }