
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.PagerState
import com.google.accompanist.pager.rememberPagerState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.TextStyle
import java.util.Locale

@OptIn(ExperimentalPagerApi::class)
@Composable
fun CalendarScreen() {
    val pagerState = rememberPagerState(initialPage = 6)
    val coroutineScope = rememberCoroutineScope()

    Scaffold(
        topBar = { TopNavBar() },  // Placeholder for your TopNavBar composable
        bottomBar = { BottomNavBar() }  // Placeholder for your BottomNavBar composable
    ) { paddingValues ->
        // The calendar content goes inside the Scaffold content area
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),  // Ensures content is not hidden under top or bottom bars
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Month navigation with arrows and swipeable calendar
            SwipeableCalendar(pagerState, coroutineScope, onDateSelected = { /* Handle date selected */ })
        }
    }
}

@OptIn(ExperimentalPagerApi::class)
@Composable
fun SwipeableCalendar(
    pagerState: PagerState,
    coroutineScope: CoroutineScope,
    onDateSelected: (LocalDate) -> Unit
) {
    val currentYearMonth by remember {
        derivedStateOf {
            YearMonth.now().plusMonths(pagerState.currentPage.toLong() - 6)
        }
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Month and navigation arrows
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Left arrow
            Text(
                text = "<",
                fontSize = 24.sp,
                modifier = Modifier.clickable {
                    coroutineScope.launch {
                        if (pagerState.currentPage > 0) {
                            pagerState.animateScrollToPage(pagerState.currentPage - 1)
                        }
                    }
                }
            )

            // Current month and year
            Text(
                text = "${currentYearMonth.month.getDisplayName(TextStyle.FULL, Locale.getDefault())} ${currentYearMonth.year}",
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            // Right arrow
            Text(
                text = ">",
                fontSize = 24.sp,
                modifier = Modifier.clickable {
                    coroutineScope.launch {
                        if (pagerState.currentPage < pagerState.pageCount - 1) {
                            pagerState.animateScrollToPage(pagerState.currentPage + 1)
                        }
                    }
                }
            )
        }

        // Days of the week row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun").forEach { day ->
                Text(
                    text = day,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.weight(1f),
                    fontSize = 16.sp
                )
            }
        }

        // Spacer to ensure separation between days of the week and the calendar grid
        Spacer(modifier = Modifier.height(8.dp))

        // Constrain the height of the calendar grid
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp)  // Set the height explicitly for the grid
        ) {
            HorizontalPager(
                count = 12,
                state = pagerState,
                modifier = Modifier.fillMaxSize()
            ) { page ->
                val displayedYearMonth = YearMonth.now().plusMonths(page.toLong() - 6)

                // Days grid
                LazyVerticalGrid(
                    columns = GridCells.Fixed(7),
                    modifier = Modifier.fillMaxSize()  // Fill the constrained height
                ) {
                    val daysInMonth = displayedYearMonth.lengthOfMonth()
                    val firstDayOfMonth = displayedYearMonth.atDay(1).dayOfWeek.value

                    // Spacer for empty days
                    items(firstDayOfMonth - 1) {
                        Spacer(modifier = Modifier.size(40.dp))
                    }

                    // Display days
                    items(daysInMonth) { day ->
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clickable {
                                    onDateSelected(displayedYearMonth.atDay(day + 1))
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = (day + 1).toString(),
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopNavBar() {
    TopAppBar(
        title = { Text(text = "Habit Tracker") },
        modifier = Modifier.background(MaterialTheme.colorScheme.primary)
    )
}

@Composable
fun BottomNavBar() {
    BottomAppBar(
        modifier = Modifier.background(MaterialTheme.colorScheme.primary)
    ) {
        // Add your BottomNavBar content here
        Text(text = "Bottom Nav Bar", modifier = Modifier.padding(16.dp))
    }
}

@Preview(showBackground = true)
@Composable
fun HabitTrackerScreenPreview() {
    CalendarScreen()
}
