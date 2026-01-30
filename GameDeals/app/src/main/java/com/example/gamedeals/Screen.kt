package com.example.gamedeals

sealed class Screen(val route: String, val title: String) {
    object Home : Screen("home", "Deals")
    object Favorites : Screen("favorites", "Favoritos")
    object Profile : Screen("profile", "Perfil")
    object Extra : Screen("extra", "Extra")
    object GameDetail : Screen("gameDetail/{dealID}", "Detalles") {
        fun createRoute(dealID: String) = "gameDetail/$dealID"
    }
}
