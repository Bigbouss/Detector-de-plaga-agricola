//package com.capstone.cropcare.data.remote.api
//
//import com.capstone.cropcare.data.remote.dto.WorkerResponse
//import retrofit2.Response
//import retrofit2.http.DELETE
//import retrofit2.http.GET
//import retrofit2.http.Path
//
//interface WorkerApiService {
//
//    @GET("orgs/company-workers/")
//    suspend fun getCompanyWorkers(): Response<List<WorkerResponse>>
//
//    @DELETE("orgs/company-workers/{workerId}/")
//    suspend fun deleteWorker(@Path("workerId") workerId: String): Response<Unit>
//}