package com.example.gamedeals

import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState

@Composable
fun BottomNavBar(navController: NavController) {
    // Definimos los elementos de navegación basados en tu clase Screen
    val items = listOf(
        Screen.Home,
        Screen.Favorites,
        Screen.Profile,
        Screen.Extra
    )

    // Diseño contenedor con bordes redondeados y elevación
    NavigationBar(
        modifier = Modifier
            .height(80.dp)
            .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)), // Bordes superiores curvos
        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.9f),
        tonalElevation = 8.dp
    ) {
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentRoute = navBackStackEntry?.destination?.route

        items.forEach { screen ->
            val isSelected = currentRoute == screen.route

            NavigationBarItem(
                icon = {
                    Icon(
                        imageVector = when (screen) {
                            Screen.Home -> Icons.Default.Home
                            Screen.Favorites -> Icons.Default.Favorite
                            Screen.Profile -> Icons.Default.Person
                            Screen.Extra -> Icons.Default.Add
                            Screen.GameDetail -> Icons.Default.Info // Not used in bottom nav but required for exhaustiveness
                        },
                        contentDescription = screen.title,
                        // Animación sutil de escala si está seleccionado
                        modifier = Modifier.graphicsLayer {
                            scaleX = if (isSelected) 1.2f else 1.0f
                            scaleY = if (isSelected) 1.2f else 1.0f
                        }
                    )
                },
                label = {
                    Text(
                        text = screen.title,
                        style = if (isSelected)
                            MaterialTheme.typography.labelLarge
                        else
                            MaterialTheme.typography.labelMedium
                    )
                },
                selected = isSelected,
                onClick = {
                    if (currentRoute != screen.route) {
                        navController.navigate(screen.route) {
                            // Evita acumular copias de la misma pantalla en la pila
                            popUpTo(navController.graph.startDestinationId) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = MaterialTheme.colorScheme.primary,
                    selectedTextColor = MaterialTheme.colorScheme.primary,
                    indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )
        }
    }
}