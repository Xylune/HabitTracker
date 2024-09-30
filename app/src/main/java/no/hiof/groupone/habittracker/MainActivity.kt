package no.hiof.groupone.habittracker

import TopNavBar
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import no.hiof.groupone.habittracker.ui.navigation.AppNavigation
import no.hiof.groupone.habittracker.ui.navigation.navbars.BottomNavBar
import no.hiof.groupone.habittracker.ui.navigation.navbars.PopupScrollContent
import no.hiof.groupone.habittracker.viewmodel.AuthState
import no.hiof.groupone.habittracker.ui.theme.HabitTrackerTheme
import no.hiof.groupone.habittracker.viewmodel.AuthViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val authViewModel: AuthViewModel by viewModels()

        setContent {
            val navController = rememberNavController()
            val openDialog = remember { mutableStateOf(false) }
            HabitTrackerTheme {
                // Observe authentication state changes
                val authState = authViewModel.authState.observeAsState()
                when (authState.value) {
                    AuthState.Authenticated -> {
                        Scaffold(
                            modifier = Modifier.fillMaxSize(),
                            topBar = { TopNavBar(navController = navController, openDialog = { openDialog.value = true }) },
                            bottomBar = { BottomNavBar(navController) }
                        ) { innerPadding ->
                            AppNavigation(
                                navController = navController,
                                authViewModel = authViewModel,
                                modifier = Modifier
                                    .padding(innerPadding)
                                    .fillMaxSize()
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
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
            }
        }
    }
}