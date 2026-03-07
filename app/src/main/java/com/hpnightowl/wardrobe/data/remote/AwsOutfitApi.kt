package com.hpnightowl.wardrobe.data.remote

import com.hpnightowl.wardrobe.BuildConfig
import com.hpnightowl.wardrobe.data.remote.dto.AnalyzeItemRequestDto
import com.hpnightowl.wardrobe.data.remote.dto.AnalyzeItemResponseDto
import com.hpnightowl.wardrobe.data.remote.dto.AnalyzeUserRequestDto
import com.hpnightowl.wardrobe.data.remote.dto.AnalyzeUserResponseDto
import com.hpnightowl.wardrobe.data.remote.dto.OutfitRequestDto
import com.hpnightowl.wardrobe.data.remote.dto.OutfitResponseDto
import retrofit2.http.Body
import retrofit2.http.POST

interface AwsOutfitApi {

    /**
     * Endpoint targeting your AWS API Gateway -> Lambda.
     */
    @POST("generate-outfit")
    suspend fun generateOutfitForToday(
        @Body request: OutfitRequestDto
    ): OutfitResponseDto

    @POST("analyze-item")
    suspend fun analyzeItem(
        @Body request: AnalyzeItemRequestDto
    ): AnalyzeItemResponseDto

    @POST("analyze-user")
    suspend fun analyzeUser(
        @Body request: AnalyzeUserRequestDto
    ): AnalyzeUserResponseDto

    companion object {
        const val BASE_URL = BuildConfig.BASE_URL
    }
}
