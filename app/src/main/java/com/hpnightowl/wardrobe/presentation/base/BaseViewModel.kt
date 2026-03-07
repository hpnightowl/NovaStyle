package com.hpnightowl.wardrobe.presentation.base

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * @param State The ui state data class representing what is drawn on screen
 * @param Event One-off actions (like showing a Toast or navigating)
 */
abstract class BaseViewModel<State, Event>(initialState: State) : ViewModel() {

    private val _uiState = MutableStateFlow(initialState)
    val uiState: StateFlow<State> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<Event>()
    val events: SharedFlow<Event> = _events.asSharedFlow()

    /**
     * Update the UI state thread-safely
     */
    protected fun updateState(reducer: State.() -> State) {
        _uiState.value = _uiState.value.reducer()
    }

    /**
     * Send a one-off event to the UI (e.g., "Show Error Toast")
     */
    protected fun sendEvent(event: Event) {
        viewModelScope.launch {
            _events.emit(event)
        }
    }
}
