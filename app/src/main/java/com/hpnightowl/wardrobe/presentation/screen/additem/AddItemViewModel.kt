package com.hpnightowl.wardrobe.presentation.screen.additem

import android.net.Uri
import androidx.lifecycle.viewModelScope
import com.hpnightowl.wardrobe.domain.model.WardrobeItem
import com.hpnightowl.wardrobe.domain.repository.ItemRepository
import com.hpnightowl.wardrobe.presentation.base.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class AddItemViewModel @Inject constructor(
    private val itemRepository: ItemRepository
) : BaseViewModel<AddItemUiState, AddItemEvent>(AddItemUiState()) {

    fun onPermissionResult(isGranted: Boolean) {
        updateState { copy(hasCameraPermission = isGranted) }
    }

    fun onImageCaptured(uri: Uri) {
        updateState { copy(capturedImageUri = uri.toString()) }
    }

    fun onCategoryChanged(category: String) {
        updateState { copy(selectedCategory = category) }
    }

    fun onColorChanged(color: String) {
        updateState { copy(selectedColor = color) }
    }

    fun discardImage() {
        updateState { copy(capturedImageUri = null) }
    }

    fun saveItem() {
        val currentState = uiState.value
        if (currentState.capturedImageUri == null) {
            sendEvent(AddItemEvent.ShowError("Please take a photo first"))
            return
        }
        if (currentState.selectedCategory.isBlank() || currentState.selectedColor.isBlank()) {
            sendEvent(AddItemEvent.ShowError("Please fill out all fields"))
            return
        }

        updateState { copy(isSaving = true) }
        viewModelScope.launch {
            try {
                val newItem = WardrobeItem(
                    id = UUID.randomUUID().toString(),
                    imageUrl = currentState.capturedImageUri,
                    category = currentState.selectedCategory,
                    color = currentState.selectedColor,
                    style = "Casual" // Defaulting style for now
                )
                itemRepository.saveItem(newItem)
                sendEvent(AddItemEvent.ItemSavedSuccessfully)
                updateState { copy(isSaving = false) }
            } catch (e: Exception) {
                updateState { copy(isSaving = false) }
                sendEvent(AddItemEvent.ShowError(e.message ?: "Failed to save item"))
            }
        }
    }
}
