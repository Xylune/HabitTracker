
import android.widget.CalendarView
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.viewmodel.compose.viewModel
import no.hiof.groupone.habittracker.R
import no.hiof.groupone.habittracker.viewmodel.HabitListForDate
import no.hiof.groupone.habittracker.viewmodel.HabitListViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@Composable
fun CalendarScreen(habitListViewModel: HabitListViewModel = viewModel()) {
    var selectedDate by remember { mutableStateOf(Date()) }
    val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 70.dp)
            .padding(bottom = 95.dp)
            .padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        AndroidCalendarView { date ->
            selectedDate = date
        }

            Text(
                text = stringResource(
                    R.string.lbl_selected_date_with_placeholder,
                    dateFormat.format(selectedDate)
                ),
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(top = 16.dp)
            )

        HabitListForDate(selectedDate, habitListViewModel)
    }
}

@Composable
fun AndroidCalendarView(onDateSelected: (Date) -> Unit) {
    val context = LocalContext.current

    AndroidView(
        modifier = Modifier.fillMaxWidth(),
        factory = { CalendarView(context).apply {
            setOnDateChangeListener { _, year, month, dayOfMonth ->
                val calendar = Calendar.getInstance()
                calendar.set(year, month, dayOfMonth)
                onDateSelected(calendar.time)
            }
        }}
    )
}

@Preview(showBackground = true)
@Composable
fun HabitTrackerScreenPreview() {
    CalendarScreen()
}
