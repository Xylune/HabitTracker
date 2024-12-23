package no.hiof.groupone.habittracker.ui.navigation

import CalendarScreen
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import no.hiof.groupone.habittracker.screens.LeaderboardScreen
import no.hiof.groupone.habittracker.ui.screens.CreateHabit
import no.hiof.groupone.habittracker.ui.screens.EditHabitScreen
import no.hiof.groupone.habittracker.ui.screens.EditProfile
import no.hiof.groupone.habittracker.ui.screens.Habits
import no.hiof.groupone.habittracker.ui.screens.Home
import no.hiof.groupone.habittracker.ui.screens.Login
import no.hiof.groupone.habittracker.ui.screens.MapScreen
import no.hiof.groupone.habittracker.ui.screens.ProfileScreen
import no.hiof.groupone.habittracker.ui.screens.SettingsScreen
import no.hiof.groupone.habittracker.ui.screens.Signup
import no.hiof.groupone.habittracker.ui.screens.SocialManagement
import no.hiof.groupone.habittracker.viewmodel.AuthViewModel
import no.hiof.groupone.habittracker.viewmodel.HabitListViewModel
import java.time.LocalDate

@RequiresApi(Build.VERSION_CODES.TIRAMISU)
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
            Habits(modifier, navController)
        }
        composable("home") {
            Home(modifier, navController, authViewModel)
        }
        composable("createHabit") {
            CreateHabit(modifier, navController)
        }
        composable("editHabit/{habitId}") { backStackEntry ->
            val habitId = backStackEntry.arguments?.getString("habitId") ?: ""
            EditHabitScreen(
                modifier,
                navController,
                habitId = habitId,
                onSave = { updatedHabit ->
                    navController.popBackStack()
                },
                onCancel = {
                    navController.popBackStack()
                }
            )
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
            val habitListViewModel: HabitListViewModel = viewModel()
            val localDate = LocalDate.now()
            LaunchedEffect(Unit) {
                habitListViewModel.updateSelectedDate(localDate)
            }
            CalendarScreen(habitListViewModel = habitListViewModel)
        }
        composable("editProfile") {
            EditProfile(modifier, navController, authViewModel)
        }
        composable("map") {
            MapScreen()
        }

    })
}