package com.hpnightowl.wardrobe.data.remote.dto

data class AnalyzeUserRequestDto(
    val imageBase64: String
)

data class AnalyzeUserResponseDto(
    val success: Boolean,
    val skinTone: String? = null,
    val palette: String? = null,
    val error: String? = null
)
