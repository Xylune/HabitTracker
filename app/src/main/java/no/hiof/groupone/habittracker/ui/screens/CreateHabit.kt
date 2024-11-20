package no.hiof.groupone.habittracker.ui.screens

import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.launch
import no.hiof.groupone.habittracker.R
import no.hiof.groupone.habittracker.model.Frequency
import no.hiof.groupone.habittracker.model.Habit
import no.hiof.groupone.habittracker.model.HabitCategory
import no.hiof.groupone.habittracker.viewmodel.HabitListViewModel
import no.hiof.groupone.habittracker.viewmodel.HabitViewModel
import no.hiof.groupone.habittracker.viewmodel.HabitViewModelFactory
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateHabit(
    modifier: Modifier = Modifier,
    navController: NavController = rememberNavController(),
    habitListViewModel: HabitListViewModel = viewModel(),
) {
    val habitViewModel: HabitViewModel = viewModel(
        factory = HabitViewModelFactory(habitListViewModel)
    )

    var showDialog by remember { mutableStateOf(false) }
    var dialogMessage by remember { mutableStateOf("") }

    val habitName by habitViewModel.habitName
    val habitDescription by habitViewModel.habitDescription
    val frequency by habitViewModel.frequency
    val selectedDate by habitViewModel.selectedDate
    val selectedTime by habitViewModel.selectedTime

    val selectedCategory by habitViewModel.selectedCategory

    var expanded by remember { mutableStateOf(false) }
    val frequencyOptions = listOf(null, "Daily", "Weekly", "Monthly")

    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current

    var recentlyCreatedHabit by remember { mutableStateOf<Habit?>(null) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = stringResource(R.string.lbl_create_habit), fontSize = 26.sp)
        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = habitName,
            onValueChange = { habitViewModel.updateHabitName(it) },
            label = { Text(text = stringResource(R.string.lbl_habit_name)) },
            singleLine = true
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = habitDescription,
            onValueChange = { habitViewModel.updateHabitDescription(it) },
            label = { Text(text = stringResource(R.string.lbl_habit_description)) },
            singleLine = true
        )

        Spacer(modifier = Modifier.height(12.dp))

        CategoryDropdown(
            selectedCategory = selectedCategory,
            onCategorySelected = { category ->
                habitViewModel.updateCategory(category)
            }
        )

        Spacer(modifier = Modifier.height(12.dp))

        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded }
        ) {
            TextField(
                value = frequency ?: stringResource(R.string.lbl_one_time),
                onValueChange = {},
                readOnly = true,
                label = { Text(stringResource(R.string.lbl_frequency)) },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                modifier = Modifier.menuAnchor()
            )

            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                frequencyOptions.forEach { option ->
                    DropdownMenuItem(
                        text = { Text(option ?: stringResource(R.string.lbl_one_time)) },
                        onClick = {
                            habitViewModel.updateFrequency(option)
                            expanded = false
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        DatePickerSection(
            selectedDate = selectedDate,
            onDateSelected = { habitViewModel.updateSelectedDate(it) }
        )

        Spacer(modifier = Modifier.height(12.dp))

        TimePickerSection(
            selectedTime = selectedTime,
            onTimeSelected = { habitViewModel.updateSelectedTime(it) }
        )

        Spacer(modifier = Modifier.height(12.dp))

        Button(onClick = {
            try {
                Log.d("HabitDebug", "Selected Category: $selectedCategory")
                if (selectedDate == null || selectedTime == null) {
                    throw IllegalStateException(context.getString(R.string.error_date_time_required))
                }

                val habitFrequency = when (frequency) {
                    "Daily" -> Frequency.DAILY
                    "Weekly" -> Frequency.WEEKLY
                    "Monthly" -> Frequency.MONTHLY
                    else -> null
                }

                val calendar = Calendar.getInstance()
                selectedDate?.selectedDateMillis?.let { dateMillis ->
                    calendar.timeInMillis = dateMillis
                }
                calendar.set(Calendar.HOUR_OF_DAY, selectedTime!!.hour)
                calendar.set(Calendar.MINUTE, selectedTime!!.minute)

                val habit = Habit(
                    name = habitName,
                    description = habitDescription,
                    frequency = habitFrequency,
                    startTime = calendar.timeInMillis,
                    endTime = null,
                    basePoints = 0,
                    currentStreak = 0,
                    category = selectedCategory
                )

                coroutineScope.launch {
                    val success = habitViewModel.createHabit(habit) { createdHabit ->
                        recentlyCreatedHabit = createdHabit
                    }

                    val result = snackbarHostState.showSnackbar(
                        message = if (success) context.getString(R.string.snackbar_habit_created_success) else context.getString(R.string.snackbar_habit_created_failure),
                        actionLabel = "Undo"
                    )

                    if (result == SnackbarResult.ActionPerformed && recentlyCreatedHabit != null) {
                        habitListViewModel.deleteHabit(recentlyCreatedHabit!!)
                        recentlyCreatedHabit = null
                    }
                }
            } catch (e: IllegalStateException) {
                dialogMessage = e.message ?: context.getString(R.string.snackbar_habit_created_failure)
                showDialog = true
            }
        }) {
            Text(text = stringResource(R.string.btn_create_habit))
        }

        if (showDialog) {
            AlertDialog(
                onDismissRequest = { showDialog = false },
                confirmButton = {
                    TextButton(onClick = { showDialog = false }) {
                        Text(text = "Ok")
                    }
                },
                title = { Text(text = stringResource(R.string.snackbar_habit_created_failure)) },
                text = { Text(text = dialogMessage) }
            )
        }

        SnackbarHost(hostState = snackbarHostState)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryDropdown(
    selectedCategory: HabitCategory?,
    onCategorySelected: (HabitCategory) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded }
    ) {
        TextField(
            value = selectedCategory?.displayName ?: stringResource(R.string.lbl_select_category),
            onValueChange = {},
            readOnly = true,
            label = { Text(stringResource(R.string.lbl_category)) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier.menuAnchor()
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            HabitCategory.entries.forEach { category ->
                DropdownMenuItem(
                    text = { Text(text = category.displayName) },
                    onClick = {
                        onCategorySelected(category)
                        expanded = false
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DatePickerSection(
    selectedDate: DatePickerState?,
    onDateSelected: (DatePickerState) -> Unit
) {
    var showModal by remember { mutableStateOf(false) }

    Text(stringResource(R.string.lbl_select_a_date_and_time))
    Button(onClick = { showModal = true }) {
        Text(stringResource(R.string.btn_pick_a_date))
    }

    if (showModal) {
        DatePickerModal(
            onDateSelected = {
                onDateSelected(it)
                showModal = false
            },
            onDismiss = { showModal = false }
        )
    }

    if (selectedDate != null && selectedDate.selectedDateMillis != null) {
        val date = Date(selectedDate.selectedDateMillis!!)
        val formattedDate = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(date)
        Text(stringResource(R.string.lbl_selected_date, formattedDate))
    } else {
        Text(stringResource(R.string.lbl_no_date_selected))
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimePickerSection(
    selectedTime: TimePickerState?,
    onTimeSelected: (TimePickerState) -> Unit
) {
    var showMenu by remember { mutableStateOf(true) }
    var showInputExample by remember { mutableStateOf(false) }
    val formatter = SimpleDateFormat("HH:mm", Locale.getDefault())

    if (showMenu) {
        Button(onClick = {
            showInputExample = true
            showMenu = false
        }) {
            Text(stringResource(R.string.lbl_pick_a_time))
        }

        if (selectedTime != null) {
            val cal = Calendar.getInstance()
            cal.set(Calendar.HOUR_OF_DAY, selectedTime.hour)
            cal.set(Calendar.MINUTE, selectedTime.minute)
            Text(stringResource(R.string.lbl_selected_time, formatter.format(cal.time)))
        } else {
            Text(stringResource(R.string.lbl_no_time_selected))
        }
    }

    if (showInputExample) {
        InputUseStateExample(
            onDismiss = {
                showInputExample = false
                showMenu = true
            },
            onConfirm = {
                onTimeSelected(it)
                showInputExample = false
                showMenu = true
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DatePickerModal(
    onDateSelected: (DatePickerState) -> Unit,
    onDismiss: () -> Unit
) {
    val datePickerState = rememberDatePickerState()

    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = {
                onDateSelected(datePickerState)
                onDismiss()
            }) {
                Text("OK")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    ) {
        DatePicker(state = datePickerState)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InputUseStateExample(
    onConfirm: (TimePickerState) -> Unit,
    onDismiss: () -> Unit,
) {
    val currentTime = Calendar.getInstance()

    val timePickerState = rememberTimePickerState(
        initialHour = currentTime.get(Calendar.HOUR_OF_DAY),
        initialMinute = currentTime.get(Calendar.MINUTE),
        is24Hour = true,
    )

    Column {
        TimeInput(
            state = timePickerState,
        )
        Button(onClick = onDismiss) {
            Text("Dismiss picker")
        }
        Button(onClick = { onConfirm(timePickerState) }) {
            Text("Confirm selection")
        }
    }
}