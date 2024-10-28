package no.hiof.groupone.habittracker.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DatePickerState
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TimeInput
import androidx.compose.material3.TimePickerState
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.launch
import no.hiof.groupone.habittracker.R
import no.hiof.groupone.habittracker.ScheduleNotification
import no.hiof.groupone.habittracker.model.Frequency
import no.hiof.groupone.habittracker.model.Habit
import no.hiof.groupone.habittracker.viewmodel.AuthViewModel
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
    authViewModel: AuthViewModel = viewModel(),
    habitListViewModel: HabitListViewModel = viewModel(),
) {
    val habitViewModel: HabitViewModel = viewModel(
        factory = HabitViewModelFactory(habitListViewModel)
    )

    val habitName by habitViewModel.habitName
    val habitDescription by habitViewModel.habitDescription
    val frequency by habitViewModel.frequency
    val selectedDate by habitViewModel.selectedDate
    val selectedTime by habitViewModel.selectedTime

    var expanded by remember { mutableStateOf(false) }
    val frequencyOptions = listOf(null, "Daily", "Weekly", "Monthly")

    val snackbarHostState = remember { SnackbarHostState() }
    var showSnackbar by remember { mutableStateOf(false) }
    var snackbarSuccess by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current

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

        var showModal by remember { mutableStateOf(false) }
        Text(stringResource(R.string.lbl_select_a_date_and_time))
        Button(onClick = { showModal = true }) {
            Text(stringResource(R.string.btn_pick_a_date))
        }

        if (selectedDate != null) {
            // Access the selectedDateMillis from DatePickerState directly
            val dateMillis = selectedDate!!.selectedDateMillis
            if (dateMillis != null) {
                val date = Date(dateMillis) // Convert to Date object
                val formattedDate = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(date)
                Text(stringResource(R.string.lbl_selected_date, formattedDate))
            } else {
                Text(stringResource(R.string.lbl_no_date_selected))
            }
        } else {
            Text(stringResource(R.string.lbl_no_date_selected))
        }

        // Modal for Date Picker
        if (showModal) {
            DatePickerModal(
                onDateSelected = { datePickerState ->
                    habitViewModel.updateSelectedDate(datePickerState) // Pass DatePickerState
                    showModal = false // Close the modal
                },
                onDismiss = { showModal = false }
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

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
                cal.set(Calendar.HOUR_OF_DAY, selectedTime!!.hour)
                cal.set(Calendar.MINUTE, selectedTime!!.minute)
                cal.isLenient = false
                Text(stringResource(R.string.lbl_selected_time, formatter.format(cal.time)))
            } else {
                Text(stringResource(R.string.lbl_no_time_selected))
            }
        }

        when {
            showInputExample -> InputUseStateExample(
                onDismiss = {
                    showInputExample = false
                    showMenu = true
                },
                onConfirm = { time ->
                    habitViewModel.updateSelectedTime(time)
                    showInputExample = false
                    showMenu = true
                }
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Button(onClick = {
            ScheduleNotification().scheduleNotification(context, selectedTime!!, selectedDate!!, habitName)

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

            if (selectedTime != null) {
                calendar.set(Calendar.HOUR_OF_DAY, selectedTime!!.hour)
                calendar.set(Calendar.MINUTE, selectedTime!!.minute)
            }

            val habit = Habit(
                name = habitName,
                description = habitDescription,
                frequency = habitFrequency,
                startTime = calendar.timeInMillis,
                endTime = null,
                basePoints = 0,
                currentStreak = 0
            )

            coroutineScope.launch {
                val success = habitViewModel.createHabit(habit)
                snackbarSuccess = success
                showSnackbar = true
            }
        }) {
            Text(text = stringResource(R.string.btn_create_habit))
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
        )

        LaunchedEffect(key1 = showSnackbar) {
            if (showSnackbar) {
                if (snackbarSuccess) {
                    snackbarHostState.showSnackbar(
                        message = context.getString(R.string.snackbar_habit_created_success),
                        actionLabel = "Dismiss",
                        duration = SnackbarDuration.Short
                    )
                    navController.navigate("home")
                } else {
                    snackbarHostState.showSnackbar(
                        message = context.getString(R.string.snackbar_habit_created_failure),
                        duration = SnackbarDuration.Short
                    )
                }
                showSnackbar = false
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun CreateHabitPreview() {
    CreateHabit()
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