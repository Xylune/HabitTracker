package no.hiof.groupone.habittracker.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import no.hiof.groupone.habittracker.ui.screens.CreateHabit
import no.hiof.groupone.habittracker.viewmodel.AuthViewModel
import no.hiof.groupone.habittracker.ui.screens.Home
import no.hiof.groupone.habittracker.ui.screens.Login
import no.hiof.groupone.habittracker.ui.screens.Signup

@Composable
fun AppNavigation(modifier: Modifier = Modifier, authViewModel: AuthViewModel) {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "login", builder = {
        composable("login") {
            Login(modifier, navController, authViewModel)
        }
        composable("signup") {
            Signup(modifier, navController, authViewModel)
        }
        composable("home") {
            Home(modifier, navController, authViewModel)
        }
        composable("createHabit") {
            CreateHabit(modifier, navController, authViewModel)
        }
    })
}