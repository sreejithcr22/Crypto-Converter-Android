package com.codit.cryptoconverter.presentation.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(val route: String, val label: String, val icon: ImageVector) {
    data object Converter : Screen("converter", "Convert", Icons.Filled.Refresh)
    data object Market : Screen("market", "Market", Icons.Filled.ShoppingCart)
    data object Settings : Screen("settings", "Settings", Icons.Filled.Info)
}

val bottomNavItems = listOf(Screen.Converter, Screen.Market, Screen.Settings)
