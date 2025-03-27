package com.example.purrytify.data.model

import com.google.gson.annotations.SerializedName

data class LoginRequestDTO(
    @SerializedName("email") val email: String,
    @SerializedName("password") val password: String
)