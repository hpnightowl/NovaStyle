package com.hpnightowl.wardrobe.presentation.screen.additem

/**
 * Defines the UI State and Events for the Add Item screen.
 */

data class AddItemUiState(
    val hasCameraPermission: Boolean = false,
    val capturedImageUri: String? = null,
    val selectedCategory: String = "",
    val selectedColor: String = "",
    val isSaving: Boolean = false
)

sealed interface AddItemEvent {
    object NavigateBack : AddItemEvent
    data class ShowError(val message: String) : AddItemEvent
    object ItemSavedSuccessfully : AddItemEvent
}
