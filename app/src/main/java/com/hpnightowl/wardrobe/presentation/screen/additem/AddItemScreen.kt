package com.hpnightowl.wardrobe.presentation.screen.additem

import android.Manifest
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.rememberAsyncImagePainter

@Composable
fun AddItemScreen(
    onNavigateBack: () -> Unit,
    viewModel: AddItemViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    // Listen to one-off events (like showing a Toast or navigating back when successful)
    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is AddItemEvent.ShowError -> {
                    Toast.makeText(context, event.message, Toast.LENGTH_SHORT).show()
                }
                is AddItemEvent.ItemSavedSuccessfully -> {
                    Toast.makeText(context, "Item Saved!", Toast.LENGTH_SHORT).show()
                    onNavigateBack()
                }
                is AddItemEvent.NavigateBack -> onNavigateBack()
            }
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        viewModel.onPermissionResult(isGranted)
    }

    LaunchedEffect(Unit) {
        if (!state.hasCameraPermission) {
            permissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    Scaffold(
        topBar = {
            @OptIn(ExperimentalMaterial3Api::class)
            TopAppBar(
                title = { Text("Add Wardrobe Item") }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (!state.hasCameraPermission) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("Camera permission is required to add items.")
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = { permissionLauncher.launch(Manifest.permission.CAMERA) }) {
                        Text("Grant Permission")
                    }
                }
            } else if (state.capturedImageUri == null) {
                // Show Camera
                CameraPreview(
                    onImageCaptured = { uri -> viewModel.onImageCaptured(uri) },
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                // Show Form
                AddItemForm(
                    state = state,
                    onCategoryChanged = viewModel::onCategoryChanged,
                    onColorChanged = viewModel::onColorChanged,
                    onSaveClicked = viewModel::saveItem,
                    onDiscardClicked = viewModel::discardImage,
                    modifier = Modifier.fillMaxSize()
                )
            }
            
            if (state.isSaving) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddItemForm(
    state: AddItemUiState,
    onCategoryChanged: (String) -> Unit,
    onColorChanged: (String) -> Unit,
    onSaveClicked: () -> Unit,
    onDiscardClicked: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Image preview
        Image(
            painter = rememberAsyncImagePainter(model = state.capturedImageUri),
            contentDescription = "Captured Item",
            modifier = Modifier
                .size(250.dp)
                .padding(bottom = 24.dp),
            contentScale = ContentScale.Crop
        )

        OutlinedTextField(
            value = state.selectedCategory,
            onValueChange = onCategoryChanged,
            label = { Text("Category (e.g. Top, Bottom)") },
            modifier = Modifier.fillMaxWidth()
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        OutlinedTextField(
            value = state.selectedColor,
            onValueChange = onColorChanged,
            label = { Text("Color (e.g. Red, Blue)") },
            modifier = Modifier.fillMaxWidth()
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            OutlinedButton(onClick = onDiscardClicked) {
                Text("Discard Photo")
            }
            Button(onClick = onSaveClicked) {
                Text("Save Item")
            }
        }
    }
}
