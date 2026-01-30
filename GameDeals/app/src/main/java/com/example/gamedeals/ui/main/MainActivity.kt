package com.example.gamedeals.ui.main

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.background
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.firebase.auth.FirebaseAuth

// Imports de pantallas y componentes
import com.example.gamedeals.BottomNavBar
import com.example.gamedeals.Screen
import com.example.gamedeals.ExtraScreen
import com.example.gamedeals.ProfileScreen
import com.example.gamedeals.database.AlertsRepository
import com.example.gamedeals.database.AppDatabase
import com.example.gamedeals.database.FavoritesRepository
import com.example.gamedeals.ui.deals.DealsScreen
import com.example.gamedeals.ui.deals.GameDetailScreen
import com.example.gamedeals.ui.favorites.FavoritesScreen
import com.example.gamedeals.ui.theme.GameDealsTheme
import com.example.gamedeals.viewmodel.AlertsViewModel
import com.example.gamedeals.viewmodel.DealsViewModel
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
    val context = LocalContext.current
    val auth = FirebaseAuth.getInstance()

    // Estados de autenticación
    var isLoggedIn by remember { mutableStateOf(auth.currentUser != null) }
    var userEmail by remember { mutableStateOf(auth.currentUser?.email ?: "") }
    var currentScreen by remember { mutableStateOf("login") }

    // Inicializar ThemeViewModel para el modo oscuro (de rama main)
    val themeViewModel: ThemeViewModel = remember { ThemeViewModel(context) }
    val isDarkTheme by themeViewModel.isDarkTheme.collectAsState()

    GameDealsTheme(darkTheme = isDarkTheme) {
        if (!isLoggedIn) {
            when (currentScreen) {
                "login" -> LoginScreen(
                    onLoginSuccess = { email ->
                        userEmail = email
                        isLoggedIn = true
                    },
                    onNavigateToRegister = { currentScreen = "register" }
                )
                "register" -> RegisterScreen(
                    onRegisterSuccess = { email ->
                        userEmail = email
                        isLoggedIn = true
                    },
                    onNavigateToLogin = { currentScreen = "login" }
                )
            }
        } else {
            val db = AppDatabase.getDatabase(context)
            val alertsRepository = remember { AlertsRepository(db.priceAlertDao()) }
            val alertsViewModel = remember { AlertsViewModel(alertsRepository) }

            MainScreen(
                userEmail = userEmail,
                themeViewModel = themeViewModel,
                alertsViewModel = alertsViewModel,
                onLogout = {
                    auth.signOut()
                    isLoggedIn = false
                    userEmail = ""
                    currentScreen = "login"
                }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    userEmail: String,
    themeViewModel: ThemeViewModel,
    alertsViewModel: AlertsViewModel,
    onLogout: () -> Unit
) {
    val navController = rememberNavController()
    val context = LocalContext.current

    val db = remember { AppDatabase.getDatabase(context) }
    val repository = remember { FavoritesRepository(db.favoriteDealDao()) }

    // ViewModel de favoritos adaptado al usuario actual
    val favoritesViewModel = remember(userEmail) {
        FavoritesViewModel(repository, userEmail)
    }
    val dealsViewModel: DealsViewModel = viewModel()

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        "GameDeals",
                        fontWeight = FontWeight.ExtraBold,
                        style = MaterialTheme.typography.headlineSmall
                    )
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.primary
                )
            )
        },
        // bottomBar argument removed to allow floating overlay
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding).fillMaxSize()) {
            NavHost(
                navController = navController,
                startDestination = Screen.Home.route,
                enterTransition = {
                    fadeIn(animationSpec = tween(300)) + scaleIn(initialScale = 0.95f)
                },
                exitTransition = {
                    fadeOut(animationSpec = tween(300))
                }
            ) {
                composable(Screen.Home.route) {
                    DealsScreen(
                        favoritesViewModel = favoritesViewModel,
                        dealsViewModel = dealsViewModel,
                        alertsViewModel = alertsViewModel,
                        onDealClick = { dealID ->
                            navController.navigate(Screen.GameDetail.createRoute(dealID))
                        }
                    )
                }
                composable(Screen.Favorites.route) {
                    FavoritesScreen(
                        viewModel = favoritesViewModel,
                        onDealClick = { dealID ->
                            navController.navigate(Screen.GameDetail.createRoute(dealID))
                        }
                    )
                }
                composable(Screen.Profile.route) {
                    // Pasamos themeViewModel para el switch y onLogout para Firebase
                    ProfileScreen(userEmail, themeViewModel, onLogout)
                }
                composable(Screen.Extra.route) {
                    ExtraScreen(alertsViewModel = alertsViewModel)
                }
                composable(
                    route = Screen.GameDetail.route,
                    arguments = listOf(navArgument("dealID") { type = NavType.StringType })
                ) { backStackEntry ->
                    val dealID = backStackEntry.arguments?.getString("dealID") ?: ""
                    val storeMap by dealsViewModel.storeMap.collectAsState()
                    GameDetailScreen(
                        dealID = dealID,
                        storeMap = storeMap,
                        favoritesViewModel = favoritesViewModel,
                        alertsViewModel = alertsViewModel,
                        onBack = { navController.popBackStack() }
                    )
                }
            }
            
            // Floating Bottom Navigation Bar
            BottomNavBar(
                navController = navController,
                modifier = Modifier.align(Alignment.BottomCenter)
            )
        }
    }
}