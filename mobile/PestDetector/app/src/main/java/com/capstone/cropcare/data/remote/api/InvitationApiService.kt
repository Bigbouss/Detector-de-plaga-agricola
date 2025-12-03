package com.capstone.cropcare.data.remote.api

import com.capstone.cropcare.data.remote.dto.CreateJoinCodeRequest
import com.capstone.cropcare.data.remote.dto.JoinCodeResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface InvitationApiService {

    /**
     * Listar códigos de la empresa del usuario autenticado
     * GET /api/joincodes/joincodes/
     *
     * Nota: El backend filtra automáticamente por la empresa del usuario
     * según el token JWT
     */
    @GET("api/joincodes/joincodes/")
    suspend fun getJoinCodes(): Response<List<JoinCodeResponse>>

    /**
     * Crear nuevo código de invitación
     * POST /api/joincodes/joincodes/
     *
     * El código se crea automáticamente para la empresa del usuario autenticado
     */
    @POST("api/joincodes/joincodes/")
    suspend fun createJoinCode(
        @Body request: CreateJoinCodeRequest
    ): Response<JoinCodeResponse>
}