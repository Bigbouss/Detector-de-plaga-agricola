package com.capstone.cropcare.data.remote.api

import com.capstone.cropcare.data.remote.dto.CreateCropRequest
import com.capstone.cropcare.data.remote.dto.CreateZoneRequest
import com.capstone.cropcare.data.remote.dto.CropDto
import com.capstone.cropcare.data.remote.dto.ZoneResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface ZonesApiService {

    // Listar zonas de la empresa del admin
    @GET("zonas/")
    suspend fun getZones(): Response<List<ZoneResponse>>

    // Crear nueva zona
    @POST("zonas/")
    suspend fun createZone(
        @Body request: CreateZoneRequest
    ): Response<ZoneResponse>

    // Eliminar zona
    @DELETE("zonas/{id}/")
    suspend fun deleteZone(
        @Path("id") zoneId: Int
    ): Response<Unit>

    // Crear cultivo
    @POST("cultivos/")
    suspend fun createCrop(
        @Body request: CreateCropRequest
    ): Response<CropDto>

    // Eliminar cultivo
    @DELETE("cultivos/{id}/")
    suspend fun deleteCrop(
        @Path("id") cropId: Int
    ): Response<Unit>
}