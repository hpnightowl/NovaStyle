package com.hpnightowl.wardrobe.presentation.navigation

/**
 * Defines all the destinations in the app.
 */
sealed class Route(val route: String) {
    object Home : Route("home")
    object Gallery : Route("gallery")
    object AddItem : Route("add_item")
    object Profile : Route("profile")
    object Chat : Route("chat")
}
