package no.hiof.groupone.habittracker.ui.screens

import android.Manifest
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import no.hiof.groupone.habittracker.NotificationService
import no.hiof.groupone.habittracker.R
import no.hiof.groupone.habittracker.viewmodel.AuthState
import no.hiof.groupone.habittracker.viewmodel.AuthViewModel

@OptIn(ExperimentalPermissionsApi::class)
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

    val postNoticationPermission =
        rememberPermissionState(permission = Manifest.permission.POST_NOTIFICATIONS)

    val NotificationService = NotificationService(navController.context)

    LaunchedEffect(key1 = true) {
        if (!postNoticationPermission.status.isGranted) {
            postNoticationPermission.launchPermissionRequest()
        }
    }

    Column(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Button(onClick = {NotificationService.showNotification()}) {
            Text(text = "Show Notification")
        }
        Button(onClick = {NotificationService.showExpandableNotification()}) {
            Text(text = "Show Expandable Notification With Image")
        }
        Button(onClick = {NotificationService.showExpandableNotificationWithText()}) {
            Text(text = "Show Expandable With Text Notification")
        }
        Button(onClick = {NotificationService.showInboxStyleNotification()}) {
            Text(text = "Show Inbox Style Notification")
        }
        Button(onClick = {NotificationService.showNotificationGroup()}) {
            Text(text = "Show Notification Group Notification")
        }

        Text(text = "Home", fontSize = 26.sp)

        user?.let {
            Text(text = stringResource(R.string.lbl_welcome, user.email ?: "N/A"), fontSize = 22.sp)

            Spacer(modifier = Modifier.height(8.dp))

            Text(stringResource(R.string.lbl_display_name_with_placeholder, it.displayName ?: "N/A"))

            Spacer(modifier = Modifier.height(8.dp))

            Text(stringResource(R.string.lbl_phone_number, it.phoneNumber ?: "N/A"))

            Spacer(modifier = Modifier.height(8.dp))

            it.photoUrl?.let { photoUrl ->
                AsyncImage(
                    model = photoUrl,
                    contentDescription = stringResource(R.string.lbl_profile_picture),
                    modifier = Modifier.size(100.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        TextButton(onClick = { navController.navigate("createHabit") }) {
            Text(text = stringResource(R.string.lbl_create_habit))
        }

        TextButton(onClick = { navController.navigate("leaderboards") }) {
            Text(text = stringResource(R.string.lbl_view_leaderboards))
        }

        Spacer(modifier = Modifier.height(16.dp))

        TextButton(onClick = { authViewModel.signout() }) {
            Text(text = stringResource(R.string.lbl_sign_out))
        }
    }
}
