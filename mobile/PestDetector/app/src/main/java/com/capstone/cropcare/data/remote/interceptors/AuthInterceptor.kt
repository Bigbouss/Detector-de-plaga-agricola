package com.capstone.cropcare.data.remote.interceptors

import com.capstone.cropcare.data.local.preferences.TokenManager
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject

/**
 * Interceptor que agrega el Bearer token a todas las peticiones autenticadas
 */
class AuthInterceptor @Inject constructor(
    private val tokenManager: TokenManager
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()

        // Endpoints que NO requieren token
        val publicEndpoints = listOf(
            "/api/accounts/auth/login/",
            "/api/accounts/auth/register-admin/",
            "/api/accounts/auth/register-worker/",
            "/api/accounts/auth/token/refresh/",
            "/api/joincodes/validate-code/"
        )

        val path = request.url.encodedPath
        val isPublicEndpoint = publicEndpoints.any { path.endsWith(it) }

        // Si es público, no agregar token
        if (isPublicEndpoint) {
            return chain.proceed(request)
        }

        // Agregar token si existe
        val token = tokenManager.getAccessToken()

        val newRequest = if (token != null) {
            request.newBuilder()
                .addHeader("Authorization", "Bearer $token")
                .build()
        } else {
            request
        }

        return chain.proceed(newRequest)
    }
}