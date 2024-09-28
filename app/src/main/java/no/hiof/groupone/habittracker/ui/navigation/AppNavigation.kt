package no.hiof.groupone.habittracker.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import no.hiof.groupone.habittracker.FriendManagementScreen
import no.hiof.groupone.habittracker.ui.screens.ViewModel
import no.hiof.groupone.habittracker.ui.screens.Home
import no.hiof.groupone.habittracker.ui.screens.Login
import no.hiof.groupone.habittracker.ui.screens.Signup

@Composable
fun AppNavigation(modifier: Modifier = Modifier, viewModel: ViewModel) {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "login", builder = {
        composable("login") {
            Login(modifier, navController, viewModel)
        }
        composable("signup") {
            Signup(modifier, navController, viewModel)
        }
        composable("home") {
            Home(modifier, navController, viewModel)
        }
        composable("friendManager") {
            FriendManagementScreen(modifier, navController, viewModel)
        }
    })
}