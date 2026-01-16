package com.example.gamedeals

import retrofit2.http.Body
import retrofit2.http.POST

interface AuthApi {

    @POST("login")
    suspend fun login(
        @Body request: LoginRequest
    ): LoginResponse
}
