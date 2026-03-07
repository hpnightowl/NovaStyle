package com.hpnightowl.wardrobe.presentation.screen.gallery

import androidx.lifecycle.viewModelScope
import com.hpnightowl.wardrobe.domain.repository.ItemRepository
import com.hpnightowl.wardrobe.presentation.base.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class GalleryViewModel @Inject constructor(
    private val itemRepository: ItemRepository
) : BaseViewModel<GalleryUiState, GalleryEvent>(GalleryUiState()) {

    init {
        loadItems()
    }

    private fun loadItems() {
        viewModelScope.launch {
            itemRepository.getAllItems()
                .catch { 
                    // Handle error if needed, but for now just stop loading
                    updateState { copy(isLoading = false) }
                }
                .collect { items ->
                    updateState { 
                        copy(
                            items = items,
                            isLoading = false
                        )
                    }
                }
        }
    }
}
