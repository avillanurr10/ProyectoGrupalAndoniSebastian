package com.example.gamedeals.ui.deals.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun PriceAlertDialog(
    gameTitle: String,
    currentPrice: String,
    onDismiss: () -> Unit,
    onConfirm: (Double) -> Unit
) {
    var targetPrice by remember { mutableStateOf(currentPrice) }
    var isError by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                Icon(
                    Icons.Default.NotificationsActive,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(Modifier.width(8.dp))
                Text("Alerta de Precio", style = MaterialTheme.typography.titleLarge)
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = gameTitle,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    maxLines = 1
                )
                Text(
                    "Te avisaremos cuando baje de tu precio objetivo. Precio actual: $$currentPrice",
                    style = MaterialTheme.typography.bodyMedium
                )
                OutlinedTextField(
                    value = targetPrice,
                    onValueChange = {
                        targetPrice = it
                        isError = it.toDoubleOrNull() == null
                    },
                    label = { Text("Precio deseado ($)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    isError = isError,
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                if (isError) {
                    Text(
                        "Introduce un número válido",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.labelSmall
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val price = targetPrice.toDoubleOrNull()
                    if (price != null) {
                        onConfirm(price)
                    } else {
                        isError = true
                    }
                },
                enabled = !isError && targetPrice.isNotEmpty()
            ) {
                Text("Establecer Alerta")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}
