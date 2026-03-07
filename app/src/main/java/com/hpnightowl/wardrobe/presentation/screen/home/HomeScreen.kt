package com.hpnightowl.wardrobe.presentation.screen.home

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onNavigateToAddItem: () -> Unit,
    onNavigateToGallery: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Closet Copilot") }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onNavigateToAddItem) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Add Wardrobe Item")
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentAlignment = Alignment.Center
        ) {
            Text("Welcome to Closet Copilot!\n\nYour daily outfits will appear here.")
        }
    }
}
