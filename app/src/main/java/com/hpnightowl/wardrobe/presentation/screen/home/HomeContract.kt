package com.hpnightowl.wardrobe.presentation.screen.home

import com.hpnightowl.wardrobe.domain.model.Outfit

/**
 * Defines the UI State and Events for the Home screen.
 */

data class HomeUiState(
    val isLoading: Boolean = false,
    val todaysOutfit: Outfit? = null,
    val errorMessage: String? = null
)

sealed interface HomeEvent {
    data class ShowToast(val message: String) : HomeEvent
}
