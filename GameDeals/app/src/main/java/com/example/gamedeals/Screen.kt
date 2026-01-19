package com.example.gamedeals

sealed class Screen(val route: String, val title: String) {
    object Home : Screen("home", "Deals")
    object Favorites : Screen("favorites", "Favoritos")
    object Profile : Screen("profile", "Perfil")
    object Extra : Screen("extra", "Extra")
}
