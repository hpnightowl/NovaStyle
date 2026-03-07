package com.hpnightowl.wardrobe.data.remote

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

    companion object {
        const val BASE_URL = "https://your-api-gateway-id.execute-api.us-east-1.amazonaws.com/prod/"
    }
}
