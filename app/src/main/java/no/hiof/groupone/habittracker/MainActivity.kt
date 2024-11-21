package no.hiof.groupone.habittracker

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.core.content.ContextCompat
import androidx.navigation.compose.rememberNavController
import no.hiof.groupone.habittracker.ui.navigation.AppNavigation
import no.hiof.groupone.habittracker.ui.navigation.navbars.BottomNavBar
import no.hiof.groupone.habittracker.ui.navigation.navbars.PopupScrollContent
import no.hiof.groupone.habittracker.ui.navigation.navbars.TopNavBar
import no.hiof.groupone.habittracker.ui.theme.HabitTrackerTheme
import no.hiof.groupone.habittracker.viewmodel.AuthState
import no.hiof.groupone.habittracker.viewmodel.AuthViewModel

class MainActivity : ComponentActivity() {
    private lateinit var requestPermissionsLauncher: ActivityResultLauncher<Array<String>>

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val authViewModel: AuthViewModel by viewModels()

        val permissions = arrayOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )

        requestPermissionsLauncher = registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) {
        }

        setContent {
            val hasPermissions by remember { mutableStateOf(checkPermissions(permissions)) }
            val topNavBarHeight = remember { mutableIntStateOf(0) }

            LaunchedEffect(hasPermissions) {
                if (!hasPermissions) {
                    requestPermissionsLauncher.launch(permissions)
                }
            }

            val isDarkMode = remember { mutableStateOf(false) }
            val navController = rememberNavController()
            val openDialog = remember { mutableStateOf(false) }
            var screenTitle by remember { mutableStateOf("Habit Tracker") }
            val context = LocalContext.current

            val notificationCount by NotificationReceiver.notificationCount.observeAsState(0)

            HabitTrackerTheme(darkTheme = isDarkMode.value) {
                LaunchedEffect(navController) {
                    navController.addOnDestinationChangedListener { _, destination, _ ->
                        screenTitle = when (destination.route) {
                            "home" -> "Habit Tracker"
                            "habits" -> context.getString(R.string.lbl_my_habits)
                            "createHabit" -> context.getString(R.string.lbl_add_habit)
                            "calendar" -> context.getString(R.string.lbl_calendar)
                            "profile" -> context.getString(R.string.lbl_profile)
                            "settings" -> context.getString(R.string.lbl_settings)
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
                                    notificationCount = notificationCount,
                                    onNotificationClick = {
                                        NotificationReceiver.notificationCount.value = 0
                                    },
                                    showBackButton = navController.previousBackStackEntry != null,
                                    onHeightChange = { height -> topNavBarHeight.intValue = height }
                                )
                            },
                            bottomBar = { BottomNavBar(navController) }
                        ) { innerPadding ->
                            AppNavigation(
                                navController = navController,
                                authViewModel = authViewModel,
                                modifier = Modifier
                                    .padding(
                                        top = with(LocalDensity.current) { topNavBarHeight.intValue.toDp() },
                                        bottom = innerPadding.calculateBottomPadding()
                                    )
                                    .fillMaxSize(),
                                isDarkMode = isDarkMode.value,
                                onDarkModeToggle = { isDarkMode.value = it },
                                //topNavBarHeight = topNavBarHeight.intValue
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
                            onDarkModeToggle = { isDarkMode.value = it },
                            //topNavBarHeight = topNavBarHeight.intValue
                        )
                    }
                }
            }
        }
    }

    private fun checkPermissions(permissions: Array<String>): Boolean {
        return permissions.all { permission ->
            ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED
        }
    }
}