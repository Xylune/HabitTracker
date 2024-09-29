package no.hiof.groupone.habittracker.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TimeInput
import androidx.compose.material3.TimePickerState
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import no.hiof.groupone.habittracker.model.Frequency
import no.hiof.groupone.habittracker.model.Habit
import no.hiof.groupone.habittracker.viewmodel.AuthViewModel
import no.hiof.groupone.habittracker.viewmodel.HabitViewModel
import org.threeten.bp.Instant
import org.threeten.bp.LocalDateTime
import org.threeten.bp.ZoneId
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateHabit(modifier: Modifier = Modifier, navController: NavController = rememberNavController(),
                authViewModel: AuthViewModel = AuthViewModel(),
                habitViewModel: HabitViewModel = HabitViewModel()
) {
    var name by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }

    var expanded by remember { mutableStateOf(false) }
    var frequency by remember { mutableStateOf<String?>(null) }
    val frequencyOptions = listOf(null, "Daily", "Weekly", "Monthly")

    var startTime by remember { mutableStateOf<LocalDateTime?>(null) }
    var endTime by remember { mutableStateOf<LocalDateTime?>(null) }

    Column(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "Create Habit", fontSize = 26.sp)

        Spacer(modifier = Modifier.height(16.dp))


        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text(text = "Habit name") },
            singleLine = true
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = description,
            onValueChange = { description = it },
            label = { Text(text = "Habit description") },
            singleLine = true
        )

        Spacer(modifier = Modifier.height(12.dp))

        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded }
        ) {
            TextField(
                value = frequency ?: "One time",
                onValueChange = {},
                readOnly = true,
                label = { Text("Frequency") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                modifier = Modifier.menuAnchor()
            )

            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                frequencyOptions.forEach { option ->
                    DropdownMenuItem(
                        text = { Text(option ?: "One-time") },
                        onClick = {
                            frequency = option
                            expanded = false
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        var showModal by remember { mutableStateOf(false) }
        var selectedDate by remember { mutableStateOf<Long?>(null) }

        Text("Select a date and time:")
        Button(onClick = { showModal = true }) {
            Text("Pick a date")
        }
        if (selectedDate != null) {
            val date = Date(selectedDate!!)
            val formattedDate = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(date)
            Text("Selected date: $formattedDate")
        } else {
            Text("No date selected")
        }

        if (showModal) {
            DatePickerModal(
                onDateSelected = {
                    selectedDate = it
                    showModal = false
                },
                onDismiss = { showModal = false }
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        var showMenu by remember { mutableStateOf(true) }
        var showInputExample by remember { mutableStateOf(false) }
        var selectedTime: TimePickerState? by remember { mutableStateOf(null) }
        val formatter = remember { SimpleDateFormat("hh:mm a", Locale.getDefault()) }

        if (showMenu) {
                Button(onClick = {
                    showInputExample = true
                    showMenu = false
                }) {
                    Text("Pick a time")
                }
                if (selectedTime != null) {
                    val cal = Calendar.getInstance()
                    cal.set(Calendar.HOUR_OF_DAY, selectedTime!!.hour)
                    cal.set(Calendar.MINUTE, selectedTime!!.minute)
                    cal.isLenient = false
                    Text("Selected time = ${formatter.format(cal.time)}")
                } else {
                    Text("No time selected.")
                }

        }

        when {
            showInputExample -> InputUseStateExample(
                onDismiss = {
                    showInputExample = false
                    showMenu = true
                },
                onConfirm = {
                        time ->
                    selectedTime = time
                    showInputExample = false
                    showMenu = true
                },
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text(text = "End time")

        Spacer(modifier = Modifier.height(12.dp))

        Text(text = "Base points")

        Spacer(modifier = Modifier.height(12.dp))

        Button(onClick = {
            val habitFrequency = when (frequency) {
                "Daily" -> Frequency.DAILY
                "Weekly" -> Frequency.WEEKLY
                "Monthly" -> Frequency.MONTHLY
                else -> null
            }

            val calendar = Calendar.getInstance()
            if (selectedDate != null) {
                calendar.timeInMillis = selectedDate!!
            }
            if (selectedTime != null) {
                calendar.set(Calendar.HOUR_OF_DAY, selectedTime!!.hour)
                calendar.set(Calendar.MINUTE, selectedTime!!.minute)
            }

            val habit = Habit(
                id = 0,
                name = name,
                description = description,
                frequency = habitFrequency,
                startTime = LocalDateTime.ofInstant(
                    Instant.ofEpochMilli(calendar.timeInMillis),
                    ZoneId.of(calendar.timeZone.id)
                ),
                endTime = null,
                basePoints = 0,
                currentStreak = 0
            )

            habitViewModel.createHabit(habit)
        }) {
            Text(text = "Create habit")
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
    onDateSelected: (Long?) -> Unit,
    onDismiss: () -> Unit
) {
    val datePickerState = rememberDatePickerState()

    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = {
                onDateSelected(datePickerState.selectedDateMillis)
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