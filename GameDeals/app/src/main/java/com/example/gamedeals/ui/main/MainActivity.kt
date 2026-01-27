package com.example.gamedeals.ui.main

import com.example.gamedeals.ExtraScreen
import com.example.gamedeals.ProfileScreen
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
import com.example.gamedeals.database.AlertsRepository
import com.example.gamedeals.database.AppDatabase
import com.example.gamedeals.database.FavoritesRepository
import com.example.gamedeals.ui.deals.DealsScreen
import com.example.gamedeals.ui.favorites.FavoritesScreen
import com.example.gamedeals.ui.theme.GameDealsTheme
import com.example.gamedeals.viewmodel.AlertsViewModel
import com.example.gamedeals.viewmodel.FavoritesViewModel
import com.example.gamedeals.viewmodel.ThemeViewModel

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

    // Inicializar ThemeViewModel
    val themeViewModel = remember { ThemeViewModel(context) }
    val isDarkTheme by themeViewModel.isDarkTheme.collectAsState()

    // Envolver toda la app con el tema
    GameDealsTheme(darkTheme = isDarkTheme) {
        if (!isLoggedIn) {
            LoginScreen(
                onLogin = { email, password ->
                    if (email == "admin@test.com" && password == "1234") {
                        userEmail = email
                        isLoggedIn = true
                        Toast.makeText(context, "¡Bienvenido!", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(context, "Credenciales incorrectas (admin@test.com / 1234)", Toast.LENGTH_SHORT).show()
                    }
                }
            )
        } else {
            val db = AppDatabase.getDatabase(context)
            val repository = remember { AlertsRepository(db.priceAlertDao()) }
            val alertsViewModel = remember { AlertsViewModel(repository) }
            MainScreen(userEmail, themeViewModel, alertsViewModel)
        }
    }
}

@Composable
fun LoginScreen(onLogin: (String, String) -> Unit) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    Column(
        modifier = Modifier.fillMaxSize().padding(horizontal = 32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Default.Gamepad,
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

        Spacer(modifier = Modifier.height(40.dp))

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Contraseña") },
            leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = { onLogin(email, password) },
            modifier = Modifier.fillMaxWidth().height(50.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("Iniciar sesión", fontSize = 16.sp)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(userEmail: String, themeViewModel: ThemeViewModel, alertsViewModel: AlertsViewModel) {
    val navController = rememberNavController()
    val context = LocalContext.current

    val db = remember { AppDatabase.getDatabase(context) }
    val repository = remember { FavoritesRepository(db.favoriteDealDao()) }
    val favoritesViewModel = remember { FavoritesViewModel(repository) }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("GameDeals", fontWeight = FontWeight.ExtraBold) },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        },
        bottomBar = { BottomNavBar(navController) }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Home.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Home.route) {
                DealsScreen(
                    favoritesViewModel = favoritesViewModel, 
                    alertsViewModel = alertsViewModel,
                    onDealClick = { dealID -> 
                        navController.navigate(Screen.GameDetail.createRoute(dealID)) 
                    }
                )
            }
            composable(Screen.Favorites.route) {
                FavoritesScreen(viewModel = favoritesViewModel)
            }
            composable(Screen.Profile.route) {
                ProfileScreen(userEmail, themeViewModel)
            }
            composable(Screen.Extra.route) {
                ExtraScreen(alertsViewModel = alertsViewModel)
            }
            composable(
                route = Screen.GameDetail.route,
                arguments = listOf(navArgument("dealID") { type = NavType.StringType })
            ) { backStackEntry ->
                val dealID = backStackEntry.arguments?.getString("dealID") ?: ""
                GameDetailScreen(
                    dealID = dealID,
                    favoritesViewModel = favoritesViewModel,
                    alertsViewModel = alertsViewModel,
                    onBack = { navController.popBackStack() }
                )
            }
        }
    }
}