package com.hpnightowl.wardrobe.presentation.screen.profile

import android.content.Context
import android.net.Uri
import androidx.lifecycle.viewModelScope
import com.hpnightowl.wardrobe.data.remote.AwsOutfitApi
import com.hpnightowl.wardrobe.data.remote.dto.AnalyzeUserRequestDto
import com.hpnightowl.wardrobe.domain.model.UserProfile
import com.hpnightowl.wardrobe.domain.repository.UserProfileRepository
import com.hpnightowl.wardrobe.presentation.base.BaseViewModel
import com.hpnightowl.wardrobe.util.ImageUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val userProfileRepository: UserProfileRepository,
    private val api: AwsOutfitApi
) : BaseViewModel<ProfileUiState, ProfileEvent>(ProfileUiState()) {

    init {
        loadProfile()
    }

    private fun loadProfile() {
        viewModelScope.launch {
            val profile = userProfileRepository.getUserProfile().first()
            updateState { copy(userProfile = profile) }
        }
    }

    fun onPermissionResult(isGranted: Boolean) {
        updateState { copy(hasCameraPermission = isGranted) }
    }

    fun onImageCaptured(uri: Uri) {
        updateState { copy(capturedImageUri = uri.toString()) }
    }

    fun discardImage() {
        updateState { copy(capturedImageUri = null) }
    }

    fun analyzeAndSaveProfile(context: Context) {
        val uriString = uiState.value.capturedImageUri ?: return
        val uri = Uri.parse(uriString)

        updateState { copy(isAnalyzing = true, errorMessage = null) }
        viewModelScope.launch {
            try {
                val base64 = ImageUtils.uriToBase64(context, uri)
                if (base64 == null) {
                    updateState { copy(isAnalyzing = false, errorMessage = "Failed to process image") }
                    return@launch
                }
                
                val response = api.analyzeUser(AnalyzeUserRequestDto(base64))
                
                if (response.success && response.skinTone != null && response.palette != null) {
                    val profile = UserProfile(response.skinTone, response.palette)
                    userProfileRepository.saveUserProfile(profile)
                    updateState { copy(userProfile = profile, isAnalyzing = false, capturedImageUri = null) }
                    sendEvent(ProfileEvent.ShowToast("Profile analyzed and saved!"))
                } else {
                    updateState { copy(isAnalyzing = false, errorMessage = response.error ?: "Failed to analyze.") }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                updateState { copy(isAnalyzing = false, errorMessage = e.message ?: "Network error") }
            }
        }
    }
}
