package com.hpnightowl.wardrobe.presentation.screen.profile

import com.hpnightowl.wardrobe.domain.model.UserProfile

data class ProfileUiState(
    val userProfile: UserProfile? = null,
    val isLoading: Boolean = false,
    val capturedImageUri: String? = null,
    val hasCameraPermission: Boolean = false,
    val isAnalyzing: Boolean = false,
    val errorMessage: String? = null
)

sealed interface ProfileEvent {
    data class ShowToast(val message: String) : ProfileEvent
    object NavigateBack : ProfileEvent
}
