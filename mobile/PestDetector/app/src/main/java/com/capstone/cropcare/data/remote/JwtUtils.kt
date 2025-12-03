package com.capstone.cropcare.data.remote

import android.util.Base64
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException

/**
 * Decodifica el payload de un JWT sin validar la firma
 * @param token JWT access token
 * @return JwtPayload con los datos del usuario
 */
fun decodeJwtPayload(token: String): JwtPayload {
    return try {
        // JWT tiene formato: header.payload.signature
        val parts = token.split(".")
        if (parts.size != 3) {
            throw IllegalArgumentException("Token JWT inválido")
        }

        // Decodificar la parte del payload (segunda parte)
        val payloadJson = String(
            Base64.decode(parts[1], Base64.URL_SAFE or Base64.NO_WRAP)
        )

        // Parsear JSON a objeto
        Gson().fromJson(payloadJson, JwtPayload::class.java)
            ?: throw IllegalArgumentException("No se pudo parsear el payload del JWT")

    } catch (e: Exception) {
        // En caso de error, devolver payload vacío
        JwtPayload(
            userId = null,
            email = null,
            role = null,
            empresaId = null,
            tokenType = null,
            exp = null,
            iat = null,
            jti = null
        )
    }
}

/**
 * Verifica si el token ha expirado
 */
fun isTokenExpired(token: String): Boolean {
    return try {
        val payload = decodeJwtPayload(token)
        val exp = payload.exp ?: return true
        val now = System.currentTimeMillis() / 1000
        exp < now
    } catch (e: Exception) {
        true
    }
}