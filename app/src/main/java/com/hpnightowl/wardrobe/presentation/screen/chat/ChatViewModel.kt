package com.hpnightowl.wardrobe.presentation.screen.chat

import androidx.lifecycle.viewModelScope
import com.hpnightowl.wardrobe.domain.repository.UserProfileRepository
import com.hpnightowl.wardrobe.data.remote.AwsOutfitApi
import com.hpnightowl.wardrobe.data.remote.dto.ChatRequestDto
import com.hpnightowl.wardrobe.data.remote.dto.UserProfileDto
import com.hpnightowl.wardrobe.domain.repository.ItemRepository
import com.hpnightowl.wardrobe.presentation.base.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import javax.inject.Inject
import com.hpnightowl.wardrobe.domain.model.WardrobeItem

data class ChatUiState(
    val messages: List<ChatMessage> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

data class ChatOutfit(
    val top: WardrobeItem?,
    val bottom: WardrobeItem?,
    val shoes: WardrobeItem?
)

data class ChatMessage(
    val id: String = java.util.UUID.randomUUID().toString(),
    val text: String,
    val isUser: Boolean,
    val outfits: List<ChatOutfit>? = null
)

sealed interface ChatEvent {
    // Empty for now
}

@HiltViewModel
class ChatViewModel @Inject constructor(
    private val awsApi: AwsOutfitApi,
    private val itemRepository: ItemRepository,
    private val userProfileRepository: UserProfileRepository
) : BaseViewModel<ChatUiState, ChatEvent>(
    ChatUiState(
        messages = listOf(
            ChatMessage(
                text = "Hi! I'm your Closet Copilot. Need packing advice for a vacation, or wondering what to wear to an event? Ask me anything!",
                isUser = false
            )
        )
    )
) {

    fun sendMessage(text: String) {
        if (text.isBlank()) return

        val userMessage = ChatMessage(text = text, isUser = true)
        updateState { copy(messages = messages + userMessage, isLoading = true, error = null) }

        viewModelScope.launch {
            try {
                val wardrobe = itemRepository.getAllItems().firstOrNull() ?: emptyList()
                val profile = userProfileRepository.getUserProfile().firstOrNull()
                
                var userProfileDto: UserProfileDto? = null
                if (profile?.skinTone?.isNotEmpty() == true) {
                    userProfileDto = UserProfileDto(skinTone = profile.skinTone, palette = profile.palette)
                }

                val request = ChatRequestDto(
                    message = text,
                    wardrobe = wardrobe,
                    userProfile = userProfileDto
                )

                val response = awsApi.sendChat(request)
                
                if (response.success && response.reply != null) {
                    val parsedOutfits = response.outfits?.map { dto ->
                        ChatOutfit(
                            top = wardrobe.find { it.id == dto.topId },
                            bottom = wardrobe.find { it.id == dto.bottomId },
                            shoes = wardrobe.find { it.id == dto.shoesId }
                        )
                    }?.filter { it.top != null || it.bottom != null || it.shoes != null } // Only keep if at least 1 item matched
                    
                    val aiMessage = ChatMessage(text = response.reply, isUser = false, outfits = parsedOutfits)
                    updateState { copy(messages = messages + aiMessage, isLoading = false) }
                } else {
                    updateState { copy(error = response.error ?: "Failed to get a response.", isLoading = false) }
                }
            } catch (e: Exception) {
                updateState { copy(error = "Network error: ${e.message}", isLoading = false) }
            }
        }
    }
}
