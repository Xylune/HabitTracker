package no.hiof.groupone.habittracker.ui.screens

//noinspection UsingMaterialAndMaterial3Libraries
//noinspection UsingMaterialAndMaterial3Libraries
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import no.hiof.groupone.habittracker.viewmodel.AuthViewModel

@Composable
fun SettingsScreen(
    modifier: Modifier = Modifier,
    navController: NavHostController,
    authViewModel: AuthViewModel,
    isDarkMode: Boolean,
    onDarkModeToggle: (Boolean) -> Unit
) {
//    Column(
//        modifier = modifier
//            .fillMaxSize()
//            .padding(16.dp)
//    ) {
//        Text(
//            text = "Settings",
//            style = MaterialTheme.typography.h5,
//            modifier = Modifier.padding(bottom = 16.dp)
//        )
//
//        // Dark mode toggle switch
//        Row(
//            modifier = Modifier
//                .fillMaxWidth()
//                .padding(vertical = 8.dp),
//            verticalAlignment = Alignment.CenterVertically
//        ) {
//            Text(text = "Dark Mode")
//            Spacer(modifier = Modifier.weight(1f))
//            Switch(
//                checked = isDarkMode,
//                onCheckedChange = { isChecked ->
//                    onDarkModeToggle(isChecked) // Update the dark mode state
//                }
//            )
//        }
//    }
}

