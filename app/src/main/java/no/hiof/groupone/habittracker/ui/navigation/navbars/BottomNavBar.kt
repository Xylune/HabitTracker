package no.hiof.groupone.habittracker.ui.navigation.navbars

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.DateRange
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Menu
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun BottomNavBar() {

    BottomAppBar {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            IconButton(onClick = { /* do something */ }) {
                Icon(Icons.Outlined.Home, contentDescription = "Home")
            }
            IconButton(onClick = { /* do something */ }) {
                Icon(
                    Icons.Outlined.Menu,
                    contentDescription = "Habits",
                )
            }
            IconButton(onClick = {  }) {
                Icon(
                    Icons.Outlined.Add,
                    contentDescription = "Add"
                )
            }
            IconButton(onClick = { /* do something */ }) {
                Icon(
                    Icons.Outlined.DateRange,
                    contentDescription = "Calendar"
                )
            }
            IconButton(onClick = { /* do something */ }) {
                Icon(
                    Icons.Outlined.AccountCircle,
                    contentDescription = "Profile"
                )
            }
        }
    }
}