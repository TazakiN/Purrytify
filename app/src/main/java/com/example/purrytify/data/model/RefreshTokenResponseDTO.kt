package com.example.purrytify.data.model

import com.google.gson.annotations.SerializedName

data class RefreshTokenResponseDTO(
    @SerializedName("accessToken") val accessToken: String,
    @SerializedName("refreshToken") val refreshToken: String,
)