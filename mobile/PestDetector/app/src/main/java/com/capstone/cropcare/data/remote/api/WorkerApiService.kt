package com.capstone.cropcare.data.remote.api

import com.capstone.cropcare.data.remote.dto.AssignZonesRequest
import com.capstone.cropcare.data.remote.dto.WorkerResponse
import com.capstone.cropcare.data.remote.dto.WorkerZoneAssignmentResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface WorkersApiService {

    // Listar workers de la empresa del admin
    @GET("orgs/workers/")
    suspend fun getWorkers(): Response<List<WorkerResponse>>

    // Eliminar worker (desvincularlo de la empresa)
    @DELETE("orgs/workers/{id}/")
    suspend fun deleteWorker(
        @Path("id") workerId: Int
    ): Response<Unit>

    // ========== GESTIÓN DE ZONAS ==========

    // Asignar zonas a un trabajador
    @POST("orgs/workers/{id}/assign-zones/")
    suspend fun assignZonesToWorker(
        @Path("id") workerId: Int,
        @Body request: AssignZonesRequest
    ): Response<WorkerZoneAssignmentResponse>

    // Obtener zonas asignadas a un trabajador
    @GET("orgs/workers/{id}/zones/")
    suspend fun getWorkerAssignedZones(
        @Path("id") workerId: Int
    ): Response<List<Int>> // Lista de IDs de zonas

    // Actualizar zonas asignadas (reemplaza las anteriores)
    @PUT("orgs/workers/{id}/zones/")
    suspend fun updateWorkerZones(
        @Path("id") workerId: Int,
        @Body request: AssignZonesRequest
    ): Response<WorkerZoneAssignmentResponse>
}