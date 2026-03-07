package com.hpnightowl.wardrobe.presentation.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.hpnightowl.wardrobe.presentation.screen.additem.AddItemScreen
import com.hpnightowl.wardrobe.presentation.screen.home.HomeScreen

@Composable
fun WardrobeNavGraph(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = Route.Home.route,
        modifier = modifier
    ) {
        composable(Route.Home.route) {
            HomeScreen(
                onNavigateToAddItem = { navController.navigate(Route.AddItem.route) },
                onNavigateToGallery = { navController.navigate(Route.Gallery.route) }
            )
        }
        
        composable(Route.Gallery.route) {
            PlaceholderScreen(title = "Gallery Screen")
        }
        
        composable(Route.AddItem.route) {
            AddItemScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}

@Composable
private fun PlaceholderScreen(title: String) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(text = title)
    }
}
