package com.hpnightowl.wardrobe.presentation.screen.additem

import android.net.Uri
import android.content.Context
import androidx.lifecycle.viewModelScope
import com.hpnightowl.wardrobe.data.remote.AwsOutfitApi
import com.hpnightowl.wardrobe.data.remote.dto.AnalyzeItemRequestDto
import com.hpnightowl.wardrobe.domain.model.WardrobeItem
import com.hpnightowl.wardrobe.domain.repository.ItemRepository
import com.hpnightowl.wardrobe.presentation.base.BaseViewModel
import com.hpnightowl.wardrobe.util.ImageUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class AddItemViewModel @Inject constructor(
    private val itemRepository: ItemRepository,
    private val api: AwsOutfitApi
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

    fun saveItem(context: Context) {
        val currentState = uiState.value
        if (currentState.capturedImageUri == null) {
            sendEvent(AddItemEvent.ShowError("Please take a photo first"))
            return
        }

        updateState { copy(isSaving = true) }
        viewModelScope.launch {
            try {
                val uri = Uri.parse(currentState.capturedImageUri)
                val base64 = ImageUtils.uriToBase64(context, uri)
                
                var finalCategory = currentState.selectedCategory
                var finalColor = currentState.selectedColor
                var finalStyle = "Casual"
                
                if (base64 != null) {
                    try {
                        val response = api.analyzeItem(AnalyzeItemRequestDto(base64))
                        if (response.success) {
                            if (!response.category.isNullOrBlank()) finalCategory = response.category
                            if (!response.color.isNullOrBlank()) finalColor = response.color
                            if (!response.style.isNullOrBlank()) finalStyle = response.style
                        }
                    } catch (apiError: Exception) {
                        apiError.printStackTrace()
                        // Proceed with fallback values
                    }
                }
                
                if (finalCategory.isBlank() || finalColor.isBlank()) {
                    updateState { copy(isSaving = false) }
                    sendEvent(AddItemEvent.ShowError("Please fill out all fields or use AI"))
                    return@launch
                }

                val newItem = WardrobeItem(
                    id = UUID.randomUUID().toString(),
                    imageUrl = currentState.capturedImageUri,
                    category = finalCategory,
                    color = finalColor,
                    style = finalStyle
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
