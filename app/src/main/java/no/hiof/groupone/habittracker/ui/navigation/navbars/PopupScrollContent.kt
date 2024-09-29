package no.hiof.groupone.habittracker.ui.navigation.navbars

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog

@Composable
fun PopupScrollContent(onDismiss: () -> Unit) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = MaterialTheme.shapes.medium,
            color = MaterialTheme.colorScheme.background, //
            modifier = Modifier.fillMaxWidth(0.9f).shadow(8.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    // scroll
                    .verticalScroll(rememberScrollState())
            ) {
                Text("Item 1")
                Text("Item 2")
                Text("Item 3")
                Text("Item 4")
                Text("Item 5")



                Button(onClick = onDismiss) {
                    Text("Close")
                }
            }
        }
    }
}