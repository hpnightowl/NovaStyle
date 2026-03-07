package com.hpnightowl.wardrobe.data.remote.dto

data class AnalyzeItemRequestDto(
    val imageBase64: String
)

data class AnalyzeItemResponseDto(
    val success: Boolean,
    val name: String?,
    val category: String?,
    val color: String?,
    val style: String?,
    val error: String? = null
)
