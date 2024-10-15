package no.hiof.groupone.habittracker.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import no.hiof.groupone.habittracker.model.Habit
import no.hiof.groupone.habittracker.viewmodel.AuthViewModel
import no.hiof.groupone.habittracker.viewmodel.ProfileViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


@Composable
fun ProfileScreen(
    modifier: Modifier = Modifier, navController: NavController = rememberNavController(),
    authViewModel: AuthViewModel = viewModel(),
    profileViewModel: ProfileViewModel = viewModel(),

    onEditProfile: () -> Unit = { navController.navigate("edit_profile") }

) {
    val user = authViewModel.getCurrentUser()

    LaunchedEffect(user) {
        if (user != null) {
            profileViewModel.fetchUserData(user)
        }
    }
    val userName = profileViewModel.userName
    val email = profileViewModel.email
    val totalHabits = profileViewModel.totalHabits
    val currentStreak = profileViewModel.currentStreak
    val points = profileViewModel.points
    val habitList = profileViewModel.habitList

    val isLoading = profileViewModel.loading.value

    if (isLoading) {
        Box(
            modifier = Modifier
                .fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
    } else {
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
                .statusBarsPadding()
                .navigationBarsPadding()
        ) {
            // Profile Image
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    imageVector = Icons.Default.AccountCircle,
                    contentDescription = "Profile Picture",
                    modifier = Modifier
                        .size(120.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary)
                )
            }

            // User Info
            Text(
                text = userName.value,
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
            Text(
                text = email.value,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Habit summary
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                ProfileStat("Total Habits", totalHabits.value)
                ProfileStat("Current Streak", currentStreak.value)
                ProfileStat("Points", points.value)
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Buttons
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                // Friend Manager button
                Button(
                    onClick = { navController.navigate("SocialManagement") }
                ) {
                    Text(text = "Friend Manager")
                }

                // Edit Profile button
                Button(
                    onClick = { navController.navigate("editProfile") }
                ) {
                    Text("Edit Profile")
                }
            }


            // Habit List
            Text(
                text = "Your Habits",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(vertical = 8.dp)
            )

            if (habitList.value.isEmpty()) {
                Text("You currently don't have any habits, Create one here!")
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = { navController.navigate("createHabit") },
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                ) {
                    Text("Create Habit")
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxHeight()
                        .weight(1f)
                ) {
                    items(habitList.value) { habit ->
                        HabitItem(habit)
                        HorizontalDivider()
                    }
                }
            }



        }
    }
}

@Composable
fun ProfileStat(label: String, value: Int) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = "$value", style = MaterialTheme.typography.headlineSmall)
        Text(text = label, style = MaterialTheme.typography.bodySmall)
    }
}

@Composable
fun HabitItem(habit: Habit) {
    val date = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale
        .getDefault()).format(Date(habit.startTime ?: 0))

    Column(modifier = Modifier.padding(vertical = 8.dp)) {
        Text(
            text = "Habit: ${habit.id}",
            style = MaterialTheme.typography.titleMedium
        )
        Text(
            text = habit.name,
            style = MaterialTheme.typography.titleMedium
        )
        Text(
            text = "Streak: ${habit.currentStreak}",
            style = MaterialTheme.typography.bodyMedium
        )
        Text(
            text = "Frequency: ${habit.frequency ?: "One time"}",
            style = MaterialTheme.typography.bodyMedium
        )
        Text(
            text = "Start Time: $date",
            style = MaterialTheme.typography.bodyMedium
        )
    }
}


@Preview(showBackground = true)
@Composable
fun ProfileScreenPreview() {
    ProfileScreen(
        onEditProfile = { /* Handle edit profile action */ }
    )
}