package no.hiof.groupone.habittracker.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
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
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import no.hiof.groupone.habittracker.AuthState
import no.hiof.groupone.habittracker.ViewModel

@Composable
fun Home(modifier: Modifier = Modifier, navController: NavController, viewModel: ViewModel) {

    val authState = viewModel.authState.observeAsState()

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

            Text("Display Name: ${it.displayName ?: "N/A"}")
            Text("Phone Number: ${it.phoneNumber ?: "N/A"}")

            // Display photo if available
            it.photoUrl?.let { photoUrl ->
                AsyncImage(
                    model = photoUrl,
                    contentDescription = "Profile Picture",
                    modifier = Modifier.size(100.dp)
                )
            }
        }

        TextButton(onClick = { viewModel.signout() }) {
            Text(text = "Sign out")
        }
    }

}