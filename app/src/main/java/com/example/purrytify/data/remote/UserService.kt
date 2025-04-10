package com.example.purrytify.data.remote

import com.example.purrytify.data.model.GetProfileResponse
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Streaming
import retrofit2.http.Url

interface UserService {
    @GET("/api/profile")
    suspend fun getProfile(): Response<GetProfileResponse>
}