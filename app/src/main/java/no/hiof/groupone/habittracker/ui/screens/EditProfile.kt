package no.hiof.groupone.habittracker.ui.screens

import android.widget.Toast
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.google.firebase.auth.FirebaseAuthRecentLoginRequiredException
import no.hiof.groupone.habittracker.viewmodel.AuthState
import no.hiof.groupone.habittracker.viewmodel.AuthViewModel
import no.hiof.groupone.habittracker.viewmodel.ProfileViewModel

@Composable
fun EditProfile(
    modifier: Modifier = Modifier,
    navController: NavController = rememberNavController(),
    authViewModel: AuthViewModel = viewModel(),
    profileViewModel: ProfileViewModel = viewModel(),
) {
    val authState = authViewModel.authState.observeAsState()

    LaunchedEffect(authState.value) {
        when (authState.value) {
            is AuthState.Unauthenticated -> {
                navController.navigate("login")
            }
            else -> Unit
        }
    }

    val user = authViewModel.getCurrentUser()
    val context = LocalContext.current

    LaunchedEffect(user) {
        if (user != null) {
            profileViewModel.fetchUserData(user)
        }
    }

    val userName = profileViewModel.userName
    val email = profileViewModel.email
    val isLoading = profileViewModel.loading.value

    var showReAuthDialog by remember { mutableStateOf(false) }
    var originalEmail by remember { mutableStateOf("") }
    var emailToUpdate by remember { mutableStateOf("") }

    if (isLoading) {
        Box(
            modifier = Modifier
                .fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
    } else {
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        ) {
            Text(text = "Edit Profile Screen", modifier = Modifier)

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "User Name: ${if (userName.value.isEmpty()) "No User Name" else userName.value}",
                    modifier = Modifier.weight(1f)
                )

                Button(onClick = { profileViewModel.isEditingUserName.value = true }) {
                    Text(text = "Edit")
                }
            }

            if (profileViewModel.isEditingUserName.value) {
                OutlinedTextField(
                    value = userName.value,
                    onValueChange = { userName.value = it },
                    label = { Text(text = "Display Name") }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Email: ${if (email.value.isEmpty()) "No Email found" else email.value}",
                    modifier = Modifier.weight(1f)
                )
                Button(onClick = {
                    profileViewModel.isEditingEmail.value = true
                    originalEmail = email.value
                }) {
                    Text(text = "Edit")
                }
            }

            if (profileViewModel.isEditingEmail.value) {
                OutlinedTextField(
                    value = email.value,
                    onValueChange = { email.value = it },
                    label = { Text(text = "Email") }
                )
            }

            Button(
                onClick = {
                    if (profileViewModel.isEditingEmail.value) {
                        emailToUpdate = email.value

                        profileViewModel.updateEmail(user, emailToUpdate,
                            onSuccess = {
                                Toast.makeText(context,
                                    "A verification email has been sent to: $emailToUpdate. Please verify it and sign back in.",
                                    Toast.LENGTH_LONG).show()

                                profileViewModel.isEditingEmail.value = false
                                authViewModel.signout()
                            },
                            onFailure = { errorMessage ->
                                if (errorMessage == "Re authentication required") {
                                    emailToUpdate = email.value
                                    showReAuthDialog = true
                                } else {
                                    Toast.makeText(context, "Error: $errorMessage", Toast.LENGTH_SHORT).show()
                                }
                            }
                        )
                    }

                    if (profileViewModel.isEditingUserName.value) {
                        profileViewModel.updateDisplayName(user, userName.value,
                            onSuccess = {
                                Toast.makeText(context, "Display name updated successfully!", Toast.LENGTH_SHORT).show()
                                profileViewModel.isEditingUserName.value = false
                            },
                            onFailure = { errorMessage ->
                                Toast.makeText(context, "Error: $errorMessage", Toast.LENGTH_SHORT).show()
                                profileViewModel.isEditingUserName.value = false
                            }
                            )
                    }
                }
            ) {
                Text(text = "Save Changes")
            }

            Button(onClick = {
                user?.reload()?.addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        profileViewModel.fetchUserData(user)
                        Toast.makeText(context, "Profile refreshed!", Toast.LENGTH_SHORT).show()
                    } else {
                        if (task.exception is FirebaseAuthRecentLoginRequiredException) {
                            showReAuthDialog = true
                        } else {
                            Toast.makeText(context, "${task.exception?.message}", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }) {
                Text(text = "Refresh Profile")
            }

            if (showReAuthDialog) {
                ReAuthDialog(
                    onPasswordEntered = { password ->
                        profileViewModel.reAuthenticateUser(
                            originalEmail,
                            password,
                            onSuccess = {
                                profileViewModel.updateEmail(user, emailToUpdate,
                                    onSuccess = {
                                        Toast.makeText(context,
                                            "A verification email has been sent to: $emailToUpdate. Please verify it and sign back in.",
                                            Toast.LENGTH_LONG).show()

                                        showReAuthDialog = false
                                        authViewModel.signout()
                                    },
                                    onFailure = { retryErrorMessage ->
                                        Toast.makeText(context, "Error: $retryErrorMessage", Toast.LENGTH_SHORT).show()
                                    }
                                )
                            },
                            onFailure = { reAuthError ->
                                Toast.makeText(context, "Re-authentication failed: $reAuthError", Toast.LENGTH_SHORT).show()
                            }
                        )
                    },
                    onDismiss = {
                        showReAuthDialog = false
                    }
                )
            }

        }
    }
}



@Composable
fun ReAuthDialog(
    onPasswordEntered: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    // State to hold the entered password
    var password by remember { mutableStateOf("") }

    // Dialog to show re-authentication
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = "Re-authentication Required") },
        text = {
            Column {
                Text(text = "Please enter your password to continue.")
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text(text = "Password") },
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onPasswordEntered(password)
                    onDismiss()
                }
            ) {
                Text(text = "Confirm")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = "Cancel")
            }
        }
    )
}