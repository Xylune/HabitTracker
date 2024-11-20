package no.hiof.groupone.habittracker.ui.navigation.navbars

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.ui.res.painterResource
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.navigation.NavHostController
import no.hiof.groupone.habittracker.R
import androidx.compose.ui.res.stringResource
import androidx.navigation.compose.currentBackStackEntryAsState

@Composable
fun BottomNavBar(navController: NavHostController) {

    var selectedItem by remember { mutableIntStateOf(0) }
    val items = listOf(
        stringResource(R.string.lbl_home),
        stringResource(R.string.lbl_calendar),
        stringResource(R.string.lbl_my_habits),
        stringResource(R.string.lbl_map),
        stringResource(R.string.lbl_profile)
    )

    // Observe the current route and set the selected item accordingly
    val currentRoute = navController.currentBackStackEntryAsState().value?.destination?.route
    selectedItem = when (currentRoute) {
        "home" -> 0

        "calendar" -> 1

        "habits" -> 2
        "createHabit" -> 2
        "editHabit/{habitId}" -> 2

        "map" -> 3

        "profile" -> 4
        "editProfile" -> 4
        "settings" -> 4
        "SocialManagement" -> 4

        else -> 0 // Default to the home screen if no match
    }

    NavigationBar {
        items.forEachIndexed { index, item ->
            NavigationBarItem(
                icon = {
                    Icon(
                        painter = when (index) {
                            0 -> painterResource(R.drawable.outline_home_24)
                            1 -> painterResource(R.drawable.outline_calendar_month_24)
                            2 -> painterResource(R.drawable.outline_menu_24)
                            3 -> painterResource(R.drawable.outline_map_24)
                            4 -> painterResource(R.drawable.outline_account_circle_24)
                            else -> painterResource(R.drawable.outline_home_24)
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
                        1 -> navController.navigate("calendar")
                        2 -> navController.navigate("habits")
                        3 -> navController.navigate("map")
                        4 -> navController.navigate("profile")
                    }
                }
            )
        }
    }
}

