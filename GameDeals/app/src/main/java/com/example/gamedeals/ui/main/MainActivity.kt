package com.example.gamedeals.ui.main

import com.example.gamedeals.ExtraScreen
import com.example.gamedeals.ProfileScreen
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.core.tween
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import com.example.gamedeals.BottomNavBar
import com.example.gamedeals.Screen
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
import androidx.lifecycle.viewmodel.compose.viewModel

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

    // tema oscuro
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
    var visible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        visible = true
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = androidx.compose.ui.graphics.Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
                        MaterialTheme.colorScheme.background
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        androidx.compose.animation.AnimatedVisibility(
            visible = visible,
            enter = androidx.compose.animation.fadeIn(animationSpec = androidx.compose.animation.core.tween(1000)) + 
                    androidx.compose.animation.slideInVertically(initialOffsetY = { it / 2 })
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 32.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Surface(
                    modifier = Modifier.size(120.dp),
                    shape = androidx.compose.foundation.shape.CircleShape,
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Gamepad,
                            contentDescription = null,
                            modifier = Modifier.size(80.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = "GameDeals",
                    style = MaterialTheme.typography.displaySmall,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.primary,
                    letterSpacing = 2.sp
                )

                Text(
                    text = "Tu próximo juego al mejor precio",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(48.dp))

                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email") },
                    leadingIcon = { Icon(Icons.Default.Email, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline
                    )
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Contraseña") },
                    leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline
                    )
                )

                Spacer(modifier = Modifier.height(32.dp))

                Button(
                    onClick = { onLogin(email, password) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
                ) {
                    Text("Iniciar sesión", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                }
            }
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
        bottomBar = { BottomNavBar(navController) }
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)) {
            NavHost(
                navController = navController,
                startDestination = Screen.Home.route,
                enterTransition = {
                    androidx.compose.animation.fadeIn(animationSpec = androidx.compose.animation.core.tween(300)) +
                            androidx.compose.animation.scaleIn(initialScale = 0.95f)
                },
                exitTransition = {
                    androidx.compose.animation.fadeOut(animationSpec = androidx.compose.animation.core.tween(300))
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
        }
    }
}
