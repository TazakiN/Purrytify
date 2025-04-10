package com.example.purrytify.data.model

import com.google.gson.annotations.SerializedName

class GetProfileResponse {
    @SerializedName("id")
    var id: Int = 0

    @SerializedName("username")
    var username: String = ""

    @SerializedName("email")
    var email: String = ""

    @SerializedName("location")
    var location: String = ""

    @SerializedName("profilePhoto")
    var profilePhoto: String = ""

    @SerializedName("createdAt")
    var createdAt: String = ""

    @SerializedName("updatedAt")
    var updatedAt: String = ""

    override fun toString(): String {
        return "GetProfileResponse(id=$id, username='$username', email='$email', location='$location', profilePhoto='$profilePhoto', createdAt='$createdAt', updatedAt='$updatedAt')"
    }
}