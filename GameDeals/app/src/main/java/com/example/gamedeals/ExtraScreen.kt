package com.example.gamedeals

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.gamedeals.database.PriceAlert
import com.example.gamedeals.preferences.UserPreferences
import com.example.gamedeals.viewmodel.AlertsViewModel
import kotlinx.coroutines.launch

@Composable
fun ExtraScreen(alertsViewModel: AlertsViewModel) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val userPreferences = remember { UserPreferences(context) }
    
    val notificationsEnabled by userPreferences.notificationsEnabled.collectAsState(initial = true)
    val selectedLanguage by userPreferences.selectedLanguage.collectAsState(initial = "Español")
    val priceAlerts by alertsViewModel.alerts.collectAsState(initial = emptyList())
    
    var showLanguageDialog by remember { mutableStateOf(false) }
    var showInfoDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                Text(
                    text = "Ajustes",
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(bottom = 32.dp)
        ) {
            item { SettingsHeader("Preferencias") }

            item {
                SettingsToggleItem(
                    title = "Notificaciones",
                    description = "Recibir alertas de nuevas ofertas",
                    icon = Icons.Default.Notifications,
                    checked = notificationsEnabled,
                    onCheckedChange = { 
                        scope.launch { userPreferences.setNotificationsEnabled(it) }
                    }
                )
            }

            item {
                SettingsArrowItem(
                    title = "Idioma",
                    description = selectedLanguage,
                    icon = Icons.Default.Language,
                    onClick = { showLanguageDialog = true }
                )
            }

            if (priceAlerts.isNotEmpty()) {
                item { Spacer(modifier = Modifier.height(8.dp)) }
                item { SettingsHeader("Mis Alertas de Precio") }
                items(priceAlerts.size) { index ->
                    val alert = priceAlerts[index]
                    AlertItem(
                        title = alert.gameTitle,
                        targetPrice = alert.targetPrice,
                        onDelete = { alertsViewModel.removeAlert(alert) }
                    )
                }
            }

            item { Spacer(modifier = Modifier.height(8.dp)) }
            item { SettingsHeader("Almacenamiento") }

            item {
                SettingsActionItem(
                    title = "Limpiar caché",
                    description = "Libera espacio borrando datos temporales",
                    icon = Icons.Default.DeleteSweep,
                    onClick = {
                        Toast.makeText(context, "Caché limpiada con éxito", Toast.LENGTH_SHORT).show()
                    }
                )
            }

            item { Spacer(modifier = Modifier.height(8.dp)) }
            item { SettingsHeader("Información") }

            item {
                SettingsActionItem(
                    title = "Acerca de la app",
                    description = "GameDeals v1.2.0 • Hecho con ❤️",
                    icon = Icons.Default.Info,
                    onClick = { showInfoDialog = true }
                )
            }
        }
    }

    if (showLanguageDialog) {
        AlertDialog(
            onDismissRequest = { showLanguageDialog = false },
            title = { Text("Seleccionar Idioma") },
            text = {
                Column {
                    listOf("Español", "English").forEach { lang ->
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .clickable {
                                    scope.launch { userPreferences.setSelectedLanguage(lang) }
                                    showLanguageDialog = false
                                }
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(selected = (lang == selectedLanguage), onClick = null)
                            Spacer(Modifier.width(12.dp))
                            Text(lang, style = MaterialTheme.typography.bodyLarge)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showLanguageDialog = false }) {
                    Text("Cerrar")
                }
            }
        )
    }

    if (showInfoDialog) {
        AlertDialog(
            onDismissRequest = { showInfoDialog = false },
            title = { Text("GameDeals") },
            text = {
                Text("Desarrollado para encontrar las mejores ofertas de videojuegos en tiempo real.\n\nVersión 1.2.0\n© 2026 Proyecto Grupal")
            },
            confirmButton = {
                Button(onClick = { showInfoDialog = false }) {
                    Text("Aceptar")
                }
            }
        )
    }
}

@Composable
fun SettingsHeader(title: String) {
    Text(
        text = title.uppercase(),
        style = MaterialTheme.typography.labelMedium,
        fontWeight = FontWeight.Bold,
        letterSpacing = 1.sp,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(start = 12.dp, bottom = 4.dp)
    )
}

@Composable
fun SettingsToggleItem(
    title: String,
    description: String,
    icon: ImageVector,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 2.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            SettingsIcon(icon)
            Spacer(modifier = Modifier.width(20.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = title, fontWeight = FontWeight.Bold, fontSize = 17.sp)
                Text(text = description, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Switch(
                checked = checked, 
                onCheckedChange = onCheckedChange,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = MaterialTheme.colorScheme.primary,
                    checkedTrackColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        }
    }
}

@Composable
fun SettingsArrowItem(
    title: String,
    description: String,
    icon: ImageVector,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 2.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            SettingsIcon(icon)
            Spacer(modifier = Modifier.width(20.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = title, fontWeight = FontWeight.Bold, fontSize = 17.sp)
                Text(text = description, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
fun SettingsActionItem(
    title: String,
    description: String,
    icon: ImageVector,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 2.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            SettingsIcon(icon)
            Spacer(modifier = Modifier.width(20.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = title, fontWeight = FontWeight.Bold, fontSize = 17.sp)
                Text(text = description, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
fun AlertItem(
    title: String,
    targetPrice: Double,
    onDelete: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 2.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            SettingsIcon(Icons.Default.NotificationsActive)
            Spacer(modifier = Modifier.width(20.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = title, fontWeight = FontWeight.Bold, fontSize = 17.sp, maxLines = 1)
                Text(text = "Avisar si baja de $$targetPrice", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = "Borrar alerta", tint = MaterialTheme.colorScheme.error)
            }
        }
    }
}

@Composable
fun SettingsIcon(icon: ImageVector) {
    Surface(
        modifier = Modifier.size(44.dp),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(24.dp),
                tint = MaterialTheme.colorScheme.primary
            )
        }
    }
}
