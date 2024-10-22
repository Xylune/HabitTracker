package no.hiof.groupone.habittracker.ui.navigation.navbars

import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.navigation.NavHostController
import no.hiof.groupone.habittracker.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopNavBar(navController: NavHostController) {
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior(rememberTopAppBarState())

    // Remember the state of the menu (open or closed)
    val isMenuExpanded = remember { mutableStateOf(false) }

    CenterAlignedTopAppBar(
        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            titleContentColor = MaterialTheme.colorScheme.primary,
        ),
        title = {
            Text(
                "Habit Tracker",
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        },
        navigationIcon = {
            IconButton(onClick = {
                navController.popBackStack() // Navigate back one screen
            }) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = stringResource(R.string.lbl_back)
                )
            }
        },
        actions = {
            // Menu Icon Button
            IconButton(onClick = { isMenuExpanded.value = true }) {
                Icon(
                    imageVector = Icons.Filled.Menu,
                    contentDescription = stringResource(R.string.open_menu_label)
                )
            }

            // Dropdown Menu
            DropdownMenu(
                expanded = isMenuExpanded.value,
                onDismissRequest = { isMenuExpanded.value = false }, // Close the menu when clicking outside
                modifier = Modifier
                    .wrapContentSize(Alignment.TopEnd) // Align to the top-right corner
            ) {
                // Profile menu item
                DropdownMenuItem(
                    text = { Text(stringResource(R.string.lbl_profile)) },
                    onClick = {
                        navController.navigate("profile")
                        isMenuExpanded.value = false // Close the menu after click
                    }
                )

                // Settings menu item
                DropdownMenuItem(
                    text = { Text(stringResource(R.string.lbl_settings)) },
                    onClick = {
                        navController.navigate("settings")
                        isMenuExpanded.value = false // Close the menu after click
                    }
                )
            }
        },
        scrollBehavior = scrollBehavior
    )
}
