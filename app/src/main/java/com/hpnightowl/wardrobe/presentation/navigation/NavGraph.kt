package com.hpnightowl.wardrobe.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.hpnightowl.wardrobe.presentation.screen.additem.AddItemScreen
import com.hpnightowl.wardrobe.presentation.screen.chat.ChatScreen
import com.hpnightowl.wardrobe.presentation.screen.gallery.GalleryScreen
import com.hpnightowl.wardrobe.presentation.screen.home.HomeScreen
import com.hpnightowl.wardrobe.presentation.screen.profile.ProfileScreen

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
                onNavigateToAddItem = { navController.navigate(Route.AddItem.route) }
            )
        }
        
        composable(Route.Gallery.route) {
            GalleryScreen()
        }
        
        composable(Route.AddItem.route) {
            AddItemScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
        
        composable(Route.Profile.route) {
            ProfileScreen()
        }
        
        composable(Route.Chat.route) {
            ChatScreen()
        }
    }
}