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

    // ✅ CORRECCIÓN: Agregar prefijo /api/zonecrop/
    @GET("api/zonecrop/zonas/")
    suspend fun getZones(): Response<List<ZoneResponse>>

    @POST("api/zonecrop/zonas/")
    suspend fun createZone(
        @Body request: CreateZoneRequest
    ): Response<ZoneResponse>

    @DELETE("api/zonecrop/zonas/{id}/")
    suspend fun deleteZone(
        @Path("id") zoneId: Int
    ): Response<Unit>

    @POST("api/zonecrop/cultivos/")
    suspend fun createCrop(
        @Body request: CreateCropRequest
    ): Response<CropDto>

    @DELETE("api/zonecrop/cultivos/{id}/")
    suspend fun deleteCrop(
        @Path("id") cropId: Int
    ): Response<Unit>
}