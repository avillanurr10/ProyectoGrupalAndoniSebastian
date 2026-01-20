package com.example.gamedeals.api

import com.example.gamedeals.model.LoginRequest
import com.example.gamedeals.model.LoginResponse
import retrofit2.http.Body
import retrofit2.http.POST

interface  AuthApi {

    @POST("login")
    suspend fun login(
        @Body request: LoginRequest
    ): LoginResponse
}