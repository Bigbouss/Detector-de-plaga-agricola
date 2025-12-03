package com.capstone.cropcare.data.remote.api

import com.capstone.cropcare.data.remote.dto.*
import retrofit2.Response
import retrofit2.http.*

interface WorkersApiService {

    // Listar workers de la empresa del admin
    @GET("api/emprises/workers/")
    suspend fun getWorkers(): Response<List<WorkerResponse>>

    // Eliminar worker
    @DELETE("api/emprises/workers/{id}/")
    suspend fun deleteWorker(
        @Path("id") workerId: Int
    ): Response<Unit>

    // ========== GESTIÓN DE ZONAS ==========

    // ✅ Asignar zonas a un trabajador (usa el endpoint de zonecrop)
    @POST("api/zonecrop/assignments/assign-worker-zones/")
    suspend fun assignZonesToWorker(
        @Body request: AssignZonesRequest
    ): Response<AssignZonesResponse>

    // ✅ Obtener zonas asignadas a un trabajador
    @GET("api/zonecrop/assignments/worker-zones/{worker_id}/")
    suspend fun getWorkerAssignedZones(
        @Path("worker_id") workerId: Int
    ): Response<WorkerZonesResponse>
}