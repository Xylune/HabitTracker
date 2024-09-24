package no.hiof.groupone.habittracker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import no.hiof.groupone.habittracker.ui.navigation.AppNavigation
import no.hiof.groupone.habittracker.ui.navigation.navbars.BottomNavBar
import no.hiof.groupone.habittracker.ui.theme.HabitTrackerTheme
import no.hiof.groupone.habittracker.ui.navigation.navbars.TopNavBar
import no.hiof.groupone.habittracker.ui.screens.ViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val viewModel : ViewModel by viewModels()
        setContent {
            HabitTrackerTheme {
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    topBar = { TopNavBar() },
                    bottomBar = { BottomNavBar() }) { innerPadding ->
                        AppNavigation(viewModel = viewModel, modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }
}


