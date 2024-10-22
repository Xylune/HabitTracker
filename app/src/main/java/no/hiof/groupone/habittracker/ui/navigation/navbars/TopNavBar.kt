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
import androidx.compose.ui.text.style.TextOverflow
import androidx.navigation.NavHostController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopNavBar(navController: NavHostController, screenTitle: String, showBackButton: Boolean = true) {
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior(rememberTopAppBarState())
    val isMenuExpanded = remember { mutableStateOf(false) }

    CenterAlignedTopAppBar(
        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            titleContentColor = MaterialTheme.colorScheme.primary,
        ),
        title = {
            Text(
                screenTitle,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        },
        navigationIcon = {
            if (showBackButton) {  // Show back button based on the parameter
                IconButton(onClick = {
                    navController.popBackStack()
                }) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back"
                    )
                }
            }
        },
        actions = {
            // Menu Icon Button
            IconButton(onClick = { isMenuExpanded.value = true }) {
                Icon(
                    imageVector = Icons.Filled.Menu,
                    contentDescription = "Open menu"
                )
            }

            // Dropdown Menu
            DropdownMenu(
                expanded = isMenuExpanded.value,
                onDismissRequest = { isMenuExpanded.value = false },
                modifier = Modifier
                    .wrapContentSize(Alignment.TopEnd)
            ) {
                // Profile menu item
                DropdownMenuItem(
                    text = { Text("Profile") },
                    onClick = {
                        navController.navigate("profile")
                        isMenuExpanded.value = false
                    }
                )

                // Settings menu item
                DropdownMenuItem(
                    text = { Text("Settings") },
                    onClick = {
                        navController.navigate("settings")
                        isMenuExpanded.value = false
                    }
                )
            }
        },
        scrollBehavior = scrollBehavior
    )
}
