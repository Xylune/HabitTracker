package no.hiof.groupone.habittracker.ui.screens

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import no.hiof.groupone.habittracker.R
import no.hiof.groupone.habittracker.viewmodel.HabitListViewModel
import no.hiof.groupone.habittracker.model.Habit
import no.hiof.groupone.habittracker.viewmodel.AuthViewModel
import no.hiof.groupone.habittracker.viewmodel.HabitsUiState
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Habits(modifier: Modifier = Modifier,
           navController: NavController,
           authViewModel: AuthViewModel,
           habitListViewModel: HabitListViewModel = viewModel()) {

    val uiState by habitListViewModel.uiState.collectAsState()
    when (uiState) {
        is HabitsUiState.Loading -> {
            LoadingIndicator()
        }
        is HabitsUiState.Success -> {
            val habits = (uiState as HabitsUiState.Success).habits
            HabitList(habits, modifier)
        }
        is HabitsUiState.Error -> {
            val errorMessage = (uiState as HabitsUiState.Error).exception
            ErrorMessage(errorMessage)
        }
    }
}

@Composable
fun HabitList(habits: List<Habit>, modifier: Modifier = Modifier) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .statusBarsPadding()
            .navigationBarsPadding(),
        contentPadding = PaddingValues(16.dp)
    ) {
        items(habits) { habit ->
            HabitListItem(habit)
        }
    }
}

@Composable
fun HabitListItem(habit: Habit) {
    Log.d("HabitListItem", "Habit: $habit")

    // Function to format Unix timestamps (nullable Long)
    fun formatTime(timestamp: Long?): String {
        val sdf = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault())
        return timestamp?.let { sdf.format(Date(it)) } ?: "N/A" // Return "N/A" if timestamp is null
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(text = habit.name, style = MaterialTheme.typography.titleMedium)
            habit.description?.let {
                Text(text = it, style = MaterialTheme.typography.bodyMedium)
            }
            // Display formatted start and end times
            Text(text = stringResource(R.string.lbl_start_time_with_placeholder, formatTime(habit.startTime)), style = MaterialTheme.typography.bodySmall)
            Text(text = stringResource(
                R.string.lbl_end_time_with_placeholder,
                formatTime(habit.endTime)
            ), style = MaterialTheme.typography.bodySmall)
        }
    }
}

@Composable
fun LoadingIndicator() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator()
    }
}

@Composable
fun ErrorMessage(message: String) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(text = message, style = MaterialTheme.typography.bodyMedium)
    }
}
