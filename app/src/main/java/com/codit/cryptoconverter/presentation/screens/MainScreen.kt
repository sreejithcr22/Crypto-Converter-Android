package com.codit.cryptoconverter.presentation.screens

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.codit.cryptoconverter.di.AppContainer
import com.codit.cryptoconverter.presentation.navigation.Screen
import com.codit.cryptoconverter.presentation.navigation.bottomNavItems
import com.codit.cryptoconverter.presentation.viewmodel.ConverterViewModel
import com.codit.cryptoconverter.presentation.viewmodel.MarketViewModel
import com.codit.cryptoconverter.presentation.viewmodel.SettingsViewModel

@Composable
fun MainScreen(container: AppContainer) {
    val navController = rememberNavController()
    val snackbarHostState = remember { SnackbarHostState() }
    val backStack by navController.currentBackStackEntryAsState()
    val currentRoute = backStack?.destination?.route

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            NavigationBar {
                bottomNavItems.forEach { screen ->
                    NavigationBarItem(
                        selected = currentRoute == screen.route,
                        onClick = {
                            navController.navigate(screen.route) {
                                popUpTo(navController.graph.startDestinationId) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = { Icon(screen.icon, contentDescription = screen.label) },
                        label = { Text(screen.label) }
                    )
                }
            }
        }
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Converter.route,
            modifier = Modifier.padding(padding)
        ) {
            composable(Screen.Converter.route) {
                val vm: ConverterViewModel = viewModel(
                    factory = ConverterViewModel.Factory(container)
                )
                ConverterScreen(
                    viewModel = vm,
                    onNavigateToMarket = { navController.navigate(Screen.Market.route) },
                    snackbarHostState = snackbarHostState
                )
            }
            composable(Screen.Market.route) {
                val vm: MarketViewModel = viewModel(
                    factory = MarketViewModel.Factory(container)
                )
                MarketScreen(viewModel = vm, snackbarHostState = snackbarHostState)
            }
            composable(Screen.Settings.route) {
                val vm: SettingsViewModel = viewModel(
                    factory = SettingsViewModel.Factory(container)
                )
                SettingsScreen(viewModel = vm)
            }
        }
    }
}
