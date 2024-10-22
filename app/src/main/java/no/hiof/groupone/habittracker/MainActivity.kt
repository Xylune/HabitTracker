package no.hiof.groupone.habittracker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import no.hiof.groupone.habittracker.ui.navigation.AppNavigation
import no.hiof.groupone.habittracker.ui.navigation.navbars.BottomNavBar
import no.hiof.groupone.habittracker.ui.navigation.navbars.PopupScrollContent
import no.hiof.groupone.habittracker.ui.navigation.navbars.TopNavBar
import no.hiof.groupone.habittracker.ui.theme.HabitTrackerTheme
import no.hiof.groupone.habittracker.viewmodel.AuthState
import no.hiof.groupone.habittracker.viewmodel.AuthViewModel

class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalPermissionsApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val authViewModel: AuthViewModel by viewModels()

        setContent {
            val isDarkMode = remember { mutableStateOf(false) }
            val navController = rememberNavController()
            val openDialog = remember { mutableStateOf(false) }
            var screenTitle by remember { mutableStateOf("Habit Tracker") }

            HabitTrackerTheme(darkTheme = isDarkMode.value) {
                LaunchedEffect(navController) {
                    navController.addOnDestinationChangedListener { _, destination, _ ->
                        screenTitle = when (destination.route) {
                            "home" -> "Habit Tracker"
                            "habits" -> "My Habits"
                            "createHabit" -> "Add Habit"
                            "calendar" -> "Calendar"
                            "profile" -> "Profile"
                            "settings" -> "Settings"
                            else -> "Habit Tracker"
                        }
                    }
                }

                val authState = authViewModel.authState.observeAsState()

                when (authState.value) {
                    AuthState.Authenticated -> {
                        Scaffold(
                            modifier = Modifier.fillMaxSize(),
                            topBar = {
                                TopNavBar(
                                    navController = navController,
                                    screenTitle = screenTitle,
                                    showBackButton = navController.previousBackStackEntry != null
                                )
                            },
                            bottomBar = { BottomNavBar(navController) }
                        ) { innerPadding ->
                            AppNavigation(
                                navController = navController,
                                authViewModel = authViewModel,
                                modifier = Modifier
                                    .padding(innerPadding)
                                    .fillMaxSize(),
                                isDarkMode = isDarkMode.value,
                                onDarkModeToggle = { isDarkMode.value = it }
                            )
                            if (openDialog.value) {
                                PopupScrollContent(onDismiss = { openDialog.value = false })
                            }
                        }
                    }
                    else -> {
                        AppNavigation(
                            navController = navController,
                            authViewModel = authViewModel,
                            modifier = Modifier.fillMaxSize(),
                            isDarkMode = isDarkMode.value,
                            onDarkModeToggle = { isDarkMode.value = it }
                        )
                    }
                }
            }
        }
    }
}