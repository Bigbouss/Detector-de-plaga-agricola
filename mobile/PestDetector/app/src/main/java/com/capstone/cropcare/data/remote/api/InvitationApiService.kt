package com.capstone.cropcare.data.remote.api

import com.capstone.cropcare.data.remote.dto.CreateJoinCodeRequest
import com.capstone.cropcare.data.remote.dto.JoinCodeResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface InvitationApiService {

    // Listar códigos de la empresa del admin
    @GET("orgs/join-codes/")
    suspend fun getJoinCodes(): Response<List<JoinCodeResponse>>

    // Crear nuevo código de invitación
    @POST("orgs/join-codes/")
    suspend fun createJoinCode(
        @Body request: CreateJoinCodeRequest
    ): Response<JoinCodeResponse>
}