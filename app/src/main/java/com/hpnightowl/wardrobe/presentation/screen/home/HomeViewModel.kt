package com.hpnightowl.wardrobe.presentation.screen.home

import androidx.lifecycle.viewModelScope
import com.hpnightowl.wardrobe.domain.repository.OutfitRepository
import com.hpnightowl.wardrobe.presentation.base.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val outfitRepository: OutfitRepository
) : BaseViewModel<HomeUiState, HomeEvent>(HomeUiState()) {

    fun generateOutfitForToday(latitude: Double = 0.0, longitude: Double = 0.0) {
        updateState { copy(isLoading = true, errorMessage = null) }
        
        viewModelScope.launch {
            val result = outfitRepository.generateOutfitForToday(latitude, longitude)
            result.onSuccess { outfit ->
                updateState { copy(isLoading = false, todaysOutfit = outfit) }
            }.onFailure { error ->
                updateState { copy(isLoading = false, errorMessage = error.message) }
                sendEvent(HomeEvent.ShowToast(error.message ?: "Failed to generate outfit"))
            }
        }
    }
}
