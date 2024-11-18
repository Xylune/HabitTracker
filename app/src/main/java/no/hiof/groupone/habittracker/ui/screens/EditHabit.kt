package no.hiof.groupone.habittracker.ui.screens

import android.widget.Toast
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import no.hiof.groupone.habittracker.model.Habit
import no.hiof.groupone.habittracker.viewmodel.HabitListViewModel
import no.hiof.groupone.habittracker.viewmodel.HabitViewModel
import no.hiof.groupone.habittracker.viewmodel.HabitViewModelFactory
import androidx.compose.ui.Alignment


@Composable
fun EditHabitScreen(
    modifier: Modifier = Modifier,
    navController: NavController = rememberNavController(),
    habitId: String,
    onSave: (Habit) -> Unit,
    onCancel: () -> Unit,
    habitListViewModel: HabitListViewModel = viewModel(),
) {
    val habitViewModel: HabitViewModel = viewModel(
        factory = HabitViewModelFactory(habitListViewModel)
    )

    val context = LocalContext.current
    var habit by remember { mutableStateOf<Habit?>(null) }
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(habitId) {
        try {
            val loadedHabit = habitViewModel.getHabitById(habitId)
            if (loadedHabit != null) {
                habit = loadedHabit
                title = loadedHabit.name
                description = loadedHabit.description
            } else {
                Toast.makeText(context, "Habit not found", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "Failed to load habit: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Column(
            modifier = Modifier
                .align(Alignment.Center)
                .fillMaxWidth()
        ) {
            if (habit == null) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
            } else {
                Text(text = "Edit Habit", style = MaterialTheme.typography.titleLarge)
                Spacer(modifier = Modifier.height(16.dp))

                TextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Title") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                TextField(
                    value = description ?: "",
                    onValueChange = { description = it },
                    label = { Text("Description") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        if (title.isNotBlank()) {
                            val updatedHabit = habit?.copy(
                                name = title,
                                description = description ?: ""
                            )
                            updatedHabit?.let {
                                habitViewModel.updateHabit(it)
                                onSave(it)
                                navController.navigate("habits")
                            }
                        } else {
                            Toast.makeText(context, "Title cannot be empty", Toast.LENGTH_SHORT).show()
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(text = "Save")
                }

                Spacer(modifier = Modifier.height(8.dp))

                TextButton(
                    onClick = {
                        onCancel()
                        navController.navigate("habits")
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Cancel")
                }
            }
        }
    }
}
