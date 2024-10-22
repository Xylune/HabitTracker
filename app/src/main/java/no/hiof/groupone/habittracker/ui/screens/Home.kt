package no.hiof.groupone.habittracker.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import no.hiof.groupone.habittracker.viewmodel.AuthState
import no.hiof.groupone.habittracker.viewmodel.AuthViewModel

@Composable
fun Home(modifier: Modifier = Modifier, navController: NavController, authViewModel: AuthViewModel) {

    val authState = authViewModel.authState.observeAsState()

    LaunchedEffect(authState.value) {
        when (authState.value) {
            is AuthState.Unauthenticated -> {
                navController.navigate("login")
            }
            else -> Unit
        }
    }

    val user = Firebase.auth.currentUser

    Column(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "Home", fontSize = 26.sp)

        user?.let {
            Text(text = "Welcome ${user.email}", fontSize = 22.sp)

            Spacer(modifier = Modifier.height(8.dp))

            Text("Display Name: ${it.displayName ?: "N/A"}")

            Spacer(modifier = Modifier.height(8.dp))

            Text("Phone Number: ${it.phoneNumber ?: "N/A"}")

            Spacer(modifier = Modifier.height(8.dp))

            it.photoUrl?.let { photoUrl ->
                AsyncImage(
                    model = photoUrl,
                    contentDescription = "Profile Picture",
                    modifier = Modifier.size(100.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        TextButton(onClick = { navController.navigate("createHabit") }) {
            Text(text = "Create Habit")
        }

        TextButton(onClick = { navController.navigate("leaderboards") }) {
            Text(text = "View Leaderboards")
        }

        Spacer(modifier = Modifier.height(16.dp))

        TextButton(onClick = { authViewModel.signout() }) {
            Text(text = "Sign out")
        }
    }
}
