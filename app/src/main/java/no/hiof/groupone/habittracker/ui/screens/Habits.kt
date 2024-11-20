package no.hiof.groupone.habittracker.ui.screens

import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import no.hiof.groupone.habittracker.R
import no.hiof.groupone.habittracker.formatTime
import no.hiof.groupone.habittracker.model.Habit
import no.hiof.groupone.habittracker.model.HabitCategory
import no.hiof.groupone.habittracker.viewmodel.HabitListViewModel
import no.hiof.groupone.habittracker.viewmodel.HabitsUiState

@Composable
fun Habits(
    modifier: Modifier = Modifier,
    navController: NavController,
    habitListViewModel: HabitListViewModel = viewModel()
) {
    val isOnline by habitListViewModel.isOnline.collectAsState()
    val selectedCategory by habitListViewModel.selectedCategory.collectAsState()
    val uiState by habitListViewModel.uiState.collectAsState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        if (!isOnline) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.error.copy(alpha = 0.1f)
            ) {
                Text(
                    text = stringResource(R.string.lbl_offline_notice),
                    modifier = Modifier.padding(8.dp),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error
                )
            }
        }

        CategoryDropdown(
            selectedCategory = selectedCategory,
            onCategorySelected = { category ->
                habitListViewModel.updateSelectedCategory(category)
            },
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )

        when (uiState) {
            is HabitsUiState.Loading -> {
                LoadingIndicator()
            }
            is HabitsUiState.Success -> {
                val habits = (uiState as HabitsUiState.Success).habits
                val filteredHabits = if (selectedCategory == null) {
                    habits
                } else {
                    habits.filter { it.category == selectedCategory }
                }

                HabitList(filteredHabits, habitListViewModel, navController)
            }
            is HabitsUiState.Error -> {
                val errorMessage = (uiState as HabitsUiState.Error).exception
                ErrorMessage(errorMessage)
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryDropdown(
    selectedCategory: HabitCategory?,
    onCategorySelected: (HabitCategory?) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    val categories = listOf<HabitCategory?>(null) + HabitCategory.entries

    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded }
        ) {
            TextField(
                value = selectedCategory?.getDisplayName(LocalContext.current) ?: stringResource(R.string.lbl_all),
                onValueChange = {},
                readOnly = true,
                label = { Text(stringResource(R.string.lbl_filter_category)) },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
                modifier = Modifier.menuAnchor()
            )

            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                categories.forEach { category ->
                    DropdownMenuItem(
                        text = { Text(category?.getDisplayName(LocalContext.current) ?: stringResource(R.string.lbl_all)) },
                        onClick = {
                            onCategorySelected(category)
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun HabitList(
    habits: List<Habit>,
    viewModel: HabitListViewModel,
    navController: NavController
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(16.dp)
    ) {
        items(habits) { habit ->
            HabitListItem(
                habit = habit,
                onMarkComplete = { viewModel.markHabitAsComplete(it) },
                onDelete = { viewModel.deleteHabit(it) },
                navController
            )
        }
    }
}

@Composable
fun HabitListItem(
    habit: Habit,
    onMarkComplete: (Habit) -> Unit,
    onDelete: (Habit) -> Unit,
    navController: NavController
) {
    Log.d("HabitDebug", "Habit: $habit")

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = habit.name,
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.weight(1f)
                )

                habit.category?.let{ category ->
                    Text(
                        text = category.getDisplayName(LocalContext.current),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.align(Alignment.CenterVertically)

                    )
                }

                if (habit.isCompleted) {
                    Icon(
                        imageVector = Icons.Filled.CheckCircle,
                        contentDescription = stringResource(R.string.lbl_completed),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }

            habit.description?.let {
                Text(text = it, style = MaterialTheme.typography.bodyMedium)
            }

            Text(
                text = "Start: ${formatTime(habit.startTime)}",
                style = MaterialTheme.typography.bodySmall
            )
            Text(
                text = "End: ${formatTime(habit.endTime)}",
                style = MaterialTheme.typography.bodySmall
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Button(
                    onClick = { onMarkComplete(habit) },
                    enabled = !habit.isCompleted,
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 8.dp)
                ) {
                    Text(if (habit.isCompleted) stringResource(R.string.lbl_completed) else stringResource(
                        R.string.lbl_mark_complete
                    )
                    )
                }

                IconButton(
                    onClick = {
                        navController.navigate("editHabit/${habit.id}")
                    }
                ) {
                    Icon(
                        imageVector = Icons.Filled.Edit,
                        contentDescription = stringResource(R.string.lbl_edit_habit),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }

                IconButton(
                    onClick = { onDelete(habit) }
                ) {
                    Icon(
                        imageVector = Icons.Filled.Delete,
                        contentDescription = stringResource(R.string.lbl_delete_habit),
                        tint = MaterialTheme.colorScheme.error
                    )
                }
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
