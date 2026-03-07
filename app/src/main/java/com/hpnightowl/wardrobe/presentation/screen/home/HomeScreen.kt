package com.hpnightowl.wardrobe.presentation.screen.home

import android.widget.Toast
import androidx.compose.animation.core.animateFloat
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.rememberAsyncImagePainter
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.hpnightowl.wardrobe.domain.model.WardrobeItem
import com.hpnightowl.wardrobe.util.LocationHelper
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class, ExperimentalLayoutApi::class)
@Composable
fun HomeScreen(
    onNavigateToAddItem: () -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    
    val locationPermissionState = rememberPermissionState(
        android.Manifest.permission.ACCESS_FINE_LOCATION
    )

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is HomeEvent.ShowToast -> Toast.makeText(context, event.message, Toast.LENGTH_SHORT).show()
            }
        }
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = onNavigateToAddItem) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Add Wardrobe Item")
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp, vertical = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                item {
                    Text(
                        text = "Your Daily Outfit",
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.Bold
                    )
                }

                item {
                    Button(
                        onClick = { 
                            if (locationPermissionState.status.isGranted) {
                                coroutineScope.launch {
                                    val location = LocationHelper.getCurrentLocation(context)
                                    if (location != null) {
                                        viewModel.generateOutfitForToday(location.latitude, location.longitude)
                                    } else {
                                        Toast.makeText(context, "Could not fetch location. Using default.", Toast.LENGTH_SHORT).show()
                                        viewModel.generateOutfitForToday() // Falls back to default
                                    }
                                }
                            } else {
                                locationPermissionState.launchPermissionRequest()
                            }
                        },
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                    ) {
                        Text("Generate AI Outfit", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    }
                }

                if (state.isLoading) {
                    item {
                        androidx.compose.material3.CircularProgressIndicator(modifier = Modifier.padding(32.dp))
                    }
                } else if (state.errorMessage != null) {
                    item {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = state.errorMessage ?: "Unknown Error",
                                color = MaterialTheme.colorScheme.onErrorContainer,
                                modifier = Modifier.padding(16.dp)
                            )
                        }
                    }
                } else if (state.todaysOutfit != null) {
                    val outfit = state.todaysOutfit!!
                    item {
                        val infiniteTransition = androidx.compose.animation.core.rememberInfiniteTransition(label = "border_animation")
                        val rotation by infiniteTransition.animateFloat(
                            initialValue = 0f,
                            targetValue = 360f,
                            animationSpec = androidx.compose.animation.core.infiniteRepeatable(
                                animation = androidx.compose.animation.core.tween(durationMillis = 3000, easing = androidx.compose.animation.core.LinearEasing),
                                repeatMode = androidx.compose.animation.core.RepeatMode.Restart
                            ),
                            label = "rotation"
                        )
                        
                        val sweepGradient = androidx.compose.ui.graphics.Brush.sweepGradient(
                            colors = listOf(
                                Color(0xFF4285F4), // Google Blue
                                Color(0xFF34A853), // Green
                                Color(0xFFFBBC05), // Yellow
                                Color(0xFFEA4335), // Red
                                Color(0xFF4285F4)  // Blue again for smooth wrap
                            )
                        )

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(4.dp)
                        ) {
                            // Animated Glow Layer
                            Box(
                                modifier = Modifier
                                    .matchParentSize()
                                    .blur(maxOf(1.dp, 32.dp))
                                    .drawBehind {
                                        rotate(rotation) {
                                            drawRect(
                                                brush = sweepGradient,
                                                size = size
                                            )
                                        }
                                    }
                            )

                            // Foreground Card Layer
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(4.dp), // Leaves a tiny margin for the glow to peak through
                                colors = CardDefaults.cardColors(containerColor = androidx.compose.ui.graphics.Color(0xFFF7F9F9)),
                                shape = androidx.compose.foundation.shape.RoundedCornerShape(24.dp)
                            ) {
                                Column(modifier = Modifier.padding(24.dp)) {
                                    val locationString = outfit.weatherTarget.locationName?.let { " in $it" } ?: ""
                                    Text(
                                        text = "Weather: ${outfit.weatherTarget.temperatureCelsius}°C - ${outfit.weatherTarget.condition}$locationString",
                                        style = MaterialTheme.typography.titleLarge,
                                        fontWeight = FontWeight.Bold,
                                        color = androidx.compose.ui.graphics.Color(0xFF1E1E1E)
                                    )
                                    Spacer(modifier = Modifier.height(16.dp))
                                    Text(
                                        text = outfit.aiReasoning,
                                        style = MaterialTheme.typography.bodyLarge,
                                        color = androidx.compose.ui.graphics.Color(0xFF4A4A4A),
                                        lineHeight = androidx.compose.ui.unit.TextUnit(24f, androidx.compose.ui.unit.TextUnitType.Sp)
                                    )
                                }
                            }
                        }
                    } // Ends the item { ... } block


                    outfit.top?.let { item ->
                        item {
                            Text("Top", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(8.dp))
                            WardrobeItemCard(item = item)
                        }
                    }
                    
                    outfit.bottom?.let { item ->
                        item {
                            Text("Bottom", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(8.dp))
                            WardrobeItemCard(item = item)
                        }
                    }

                    outfit.shoes?.let { item ->
                        item {
                            Text("Shoes", style = MaterialTheme.typography.titleMedium)
                            WardrobeItemCard(item = item)
                        }
                    }
                } else {
                    item {
                        Box(
                            modifier = Modifier.fillParentMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "No outfit generated for today.\nTap the button to get an AI recommendation based on your wardrobe!",
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun WardrobeItemCard(
    item: WardrobeItem,
    modifier: Modifier = Modifier,
    onEdit: (() -> Unit)? = null,
    onDelete: (() -> Unit)? = null
) {
    Card(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = rememberAsyncImagePainter(model = item.imageUrl),
                contentDescription = item.category,
                modifier = Modifier.size(80.dp),
                contentScale = ContentScale.Crop
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.name, 
                    style = MaterialTheme.typography.titleMedium, 
                    fontWeight = FontWeight.Bold,
                    maxLines = 2,
                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                )
                Text(
                    text = item.color, 
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray
                )
                Spacer(modifier = Modifier.height(6.dp))
                
                @OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)
                androidx.compose.foundation.layout.FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    androidx.compose.material3.AssistChip(
                        onClick = {},
                        label = { Text(item.category, style = MaterialTheme.typography.labelSmall) }
                    )
                    androidx.compose.material3.AssistChip(
                        onClick = {},
                        label = { Text(item.style, style = MaterialTheme.typography.labelSmall) }
                    )
                }
            }
            if (onEdit != null) {
                IconButton(onClick = onEdit) {
                    Icon(imageVector = Icons.Default.Edit, contentDescription = "Edit")
                }
            }
            if (onDelete != null) {
                IconButton(onClick = onDelete) {
                    Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete")
                }
            }
        }
    }
}
