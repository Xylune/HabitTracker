package no.hiof.groupone.habittracker.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import no.hiof.groupone.habittracker.FriendManagementScreen
import no.hiof.groupone.habittracker.screens.Home
import no.hiof.groupone.habittracker.screens.Login
import no.hiof.groupone.habittracker.screens.Signup
import no.hiof.groupone.habittracker.ui.screens.CreateHabit
import no.hiof.groupone.habittracker.ui.screens.ProfileScreen
import no.hiof.groupone.habittracker.viewmodel.AuthViewModel

@Composable
fun AppNavigation(
    modifier: Modifier = Modifier,
    authViewModel: AuthViewModel,
    navController: NavHostController
) {

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
        composable("friendManager") {
            FriendManagementScreen(modifier, navController, authViewModel)
        }
        composable("profile") {
            ProfileScreen(modifier, navController, authViewModel)
        }
    })
}