package com.hpnightowl.wardrobe.presentation.screen.gallery

import com.hpnightowl.wardrobe.domain.model.WardrobeItem

data class GalleryUiState(
    val items: List<WardrobeItem> = emptyList(),
    val isLoading: Boolean = true
)

sealed interface GalleryEvent {
    object NavigateBack : GalleryEvent
}
