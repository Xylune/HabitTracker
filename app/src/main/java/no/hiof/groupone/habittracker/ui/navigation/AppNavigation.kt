package no.hiof.groupone.habittracker.ui.navigation

import CalendarScreen
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import no.hiof.groupone.habittracker.screens.LeaderboardScreen
import no.hiof.groupone.habittracker.screens.Login
import no.hiof.groupone.habittracker.ui.SocialManagement
import no.hiof.groupone.habittracker.ui.screens.CreateHabit
import no.hiof.groupone.habittracker.ui.screens.EditProfile
import no.hiof.groupone.habittracker.ui.screens.Habits
import no.hiof.groupone.habittracker.ui.screens.Home
import no.hiof.groupone.habittracker.ui.screens.ProfileScreen
import no.hiof.groupone.habittracker.ui.screens.SettingsScreen
import no.hiof.groupone.habittracker.ui.screens.Signup
import no.hiof.groupone.habittracker.viewmodel.AuthViewModel

@Composable
fun AppNavigation(
    modifier: Modifier = Modifier,
    authViewModel: AuthViewModel,
    navController: NavHostController,
    isDarkMode: Boolean,
    onDarkModeToggle: (Boolean) -> Unit
) {
    NavHost(navController = navController, startDestination = "login", builder = {
        composable("login") {
            Login(modifier, navController, authViewModel)
        }
        composable("signup") {
            Signup(modifier, navController, authViewModel)
        }
        composable("habits") {
            Habits(modifier, navController, authViewModel)
        }
        composable("home") {
            Home(modifier, navController, authViewModel)
        }
        composable("createHabit") {
            CreateHabit(modifier, navController, authViewModel)
        }
        composable("SocialManagement") {
            SocialManagement(modifier, navController, authViewModel)
        }
        composable("leaderboards") {
            LeaderboardScreen(modifier, navController, authViewModel)
        }
        composable("settings") {
            SettingsScreen(
                modifier = modifier,
                isDarkMode = isDarkMode,
                onDarkModeToggle = onDarkModeToggle
            )
        }
        composable("profile") {
            ProfileScreen(modifier, navController, authViewModel)
        }
        composable("calendar") {
            CalendarScreen()
        }
        composable("editProfile") {
            EditProfile(modifier, navController, authViewModel)
        }
    })
}