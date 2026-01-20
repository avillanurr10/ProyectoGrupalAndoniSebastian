

package com.example.gamedeals.ui.main

import ExtraScreen
import ProfileScreen
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Gamepad
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.compose.*
import com.example.gamedeals.BottomNavBar
import com.example.gamedeals.Screen
import com.example.gamedeals.database.AppDatabase
import com.example.gamedeals.database.FavoritesRepository
import com.example.gamedeals.ui.deals.DealsScreen
import com.example.gamedeals.ui.favorites.FavoritesScreen
import com.example.gamedeals.viewmodel.FavoritesViewModel


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            GameDealsApp()
        }
    }
}

@Composable
fun GameDealsApp() {
    var isLoggedIn by remember { mutableStateOf(false) }
    var userEmail by remember { mutableStateOf("") }
    val context = LocalContext.current

    if (!isLoggedIn) {
        LoginScreen { email, password ->

            if (email == "admin@test.com" && password == "1234") {
                userEmail = email
                isLoggedIn = true
            } else {
                Toast.makeText(
                    context,
                    "Credenciales incorrectas",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    } else {
        MainScreen(userEmail)
    }
}

@Composable
fun LoginScreen(onLogin: (String, String) -> Unit) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Logo o Icono
        Icon(
            imageVector = Icons.Default.Gamepad, // Necesitas importar Icons.Default
            contentDescription = null,
            modifier = Modifier.size(80.dp),
            tint = MaterialTheme.colorScheme.primary
        )

        Text(
            text = "GameDeals",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(8.dp))
        Text("Encuentra las mejores ofertas", style = MaterialTheme.typography.bodyMedium)

        Spacer(modifier = Modifier.height(40.dp))

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Contraseña") },
            leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth(),
            shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp)
        )

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = { onLogin(email, password) },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp)
        ) {
            Text("Iniciar sesión", fontSize = 16.sp)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(userEmail: String) {
    // 1. Configuración de Navegación y Datos
    val navController = rememberNavController()
    val context = LocalContext.current

    // Inicialización de base de datos y repositorio (usando lo que ya tienes)
    val db = remember { AppDatabase.getDatabase(context) }
    val repository = remember { FavoritesRepository(db.favoriteDealDao()) }
    val favoritesViewModel = remember { FavoritesViewModel(repository) }

    // 2. Estructura Principal de la Pantalla
    Scaffold(
        topBar = {
            // Barra superior con el nombre de la App
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "GameDeals",
                        fontWeight = FontWeight.ExtraBold,
                        style = MaterialTheme.typography.headlineMedium
                    )
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        },
        bottomBar = {
            // Tu componente de navegación inferior
            BottomNavBar(navController)
        }
    ) { innerPadding ->
        // 3. Contenedor de Navegación (NavHost)
        NavHost(
            navController = navController,
            startDestination = Screen.Home.route,
            modifier = Modifier.padding(innerPadding) // Aplica el padding del Scaffold
        ) {
            // Ruta: Pantalla de Ofertas (Home)
            composable(Screen.Home.route) {
                DealsScreen(viewModel = favoritesViewModel)
            }

            // Ruta: Pantalla de Favoritos
            composable(Screen.Favorites.route) {
                // Asegúrate de que en FavouriteScreen.kt la función se llame FavoritesScreen
                FavoritesScreen(viewModel = favoritesViewModel)
            }

            // Ruta: Perfil de Usuario
            composable(Screen.Profile.route) {
                ProfileScreen(userEmail)
            }

            // Ruta: Pantalla Extra
            composable(Screen.Extra.route) {
                ExtraScreen()
            }
        }
    }
}


