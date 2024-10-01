package no.hiof.groupone.habittracker.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import no.hiof.groupone.habittracker.viewmodel.HabitListViewModel
import no.hiof.groupone.habittracker.model.Habit
import no.hiof.groupone.habittracker.viewmodel.AuthViewModel
import no.hiof.groupone.habittracker.viewmodel.HabitsUiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Habits(modifier: Modifier = Modifier,
           navController: NavController,
           authViewModel: AuthViewModel,
           habitListViewModel: HabitListViewModel = viewModel()) {
    when (val uiState = habitListViewModel.uiState.collectAsState().value) {
        is HabitsUiState.Loading -> {
            LoadingIndicator()
        }
        is HabitsUiState.Success -> {
            val habits = (uiState as HabitsUiState.Success).habits
            HabitList(habits)
        }
        is HabitsUiState.Error -> {
            val errorMessage = (uiState as HabitsUiState.Error).exception
            ErrorMessage(errorMessage)
        }
    }
}

@Composable
fun HabitList(habits: List<Habit>) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp)
    ) {
        items(habits) { habit ->
            HabitListItem(habit)
        }
    }
}

@Composable
fun HabitListItem(habit: Habit) {
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
