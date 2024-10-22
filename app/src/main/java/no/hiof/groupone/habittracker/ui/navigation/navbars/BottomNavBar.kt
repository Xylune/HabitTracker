package no.hiof.groupone.habittracker.ui.navigation.navbars

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.DateRange
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Menu
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.navigation.NavHostController
import no.hiof.groupone.habittracker.R
import androidx.compose.ui.res.stringResource

@Composable
fun BottomNavBar(navController: NavHostController) {

    var selectedItem by remember { mutableIntStateOf(0) }
    val items = listOf(
        stringResource(R.string.lbl_home),
        stringResource(R.string.lbl_my_habits),
        stringResource(R.string.lbl_add_habit),
        stringResource(R.string.lbl_calendar),
        stringResource(R.string.lbl_profile)
    )

    NavigationBar {
        items.forEachIndexed { index, item ->
            NavigationBarItem(
                icon = {
                    Icon(
                        imageVector = when (index) {
                            0 -> Icons.Outlined.Home
                            1 -> Icons.Outlined.Menu
                            2 -> Icons.Outlined.Add
                            3 -> Icons.Outlined.DateRange
                            4 -> Icons.Outlined.AccountCircle
                            else -> Icons.Outlined.Home
                        },
                        contentDescription = item
                    )
                },
                label = { Text(item) },
                selected = selectedItem == index,
                onClick = {
                    selectedItem = index
                    when (index) {
                        0 -> navController.navigate("home")
                        1 -> navController.navigate("habits")
                        2 -> navController.navigate("createHabit")
                        3 -> navController.navigate("calendar")
                        4 -> navController.navigate("profile")
                    }
                }
            )
        }
    }
}