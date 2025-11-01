package com.growtracker.app.ui.settings

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp

@Composable
fun PinDialog(onDismiss: () -> Unit, onSave: (String) -> Unit) {
    var pin1 by remember { mutableStateOf("") }
    var pin2 by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("PIN festlegen/ändern") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = pin1,
                    onValueChange = { pin1 = it.filter { ch -> ch.isDigit() }.take(4) },
                    label = { Text("Neuer PIN (genau 4 Ziffern)") },
                    visualTransformation = PasswordVisualTransformation()
                )
                OutlinedTextField(
                    value = pin2,
                    onValueChange = { pin2 = it.filter { ch -> ch.isDigit() }.take(4) },
                    label = { Text("PIN bestätigen") },
                    visualTransformation = PasswordVisualTransformation()
                )
                if (error != null) Text(error!!, color = MaterialTheme.colorScheme.error)
            }
        },
        confirmButton = {
            TextButton(onClick = {
                error = null
                if (pin1.length != 4) {
                    error = "Der PIN muss genau 4 Ziffern haben."
                } else if (pin1 != pin2) {
                    error = "PINs stimmen nicht überein."
                } else {
                    onSave(pin1)
                }
            }) { Text("Speichern") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Abbrechen") }
        }
    )
}
