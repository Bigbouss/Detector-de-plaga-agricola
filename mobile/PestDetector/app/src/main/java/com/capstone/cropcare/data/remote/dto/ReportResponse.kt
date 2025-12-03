package com.capstone.cropcare.data.remote.dto


import com.google.gson.annotations.SerializedName

data class SessionReportDTO(
    @SerializedName("id") val id: Int,
    @SerializedName("session") val sessionId: String,
    @SerializedName("empresa") val empresaId: Int,
    @SerializedName("zona") val zonaId: Int,
    @SerializedName("cultivo") val cultivoId: Int,
    @SerializedName("owner") val ownerId: Int,

    // Métricas
    @SerializedName("images_count") val imagesCount: Int,
    @SerializedName("detections_count") val detectionsCount: Int,
    @SerializedName("unique_labels") val uniqueLabels: List<String>,
    @SerializedName("top_labels") val topLabels: List<Map<String, Any>>,
    @SerializedName("average_confidence") val averageConfidence: Float?,
    @SerializedName("median_confidence") val medianConfidence: Float?,

    // Geolocalización
    @SerializedName("lat_avg") val latAvg: Double?,
    @SerializedName("lon_avg") val lonAvg: Double?,

    // Banderas
    @SerializedName("low_confidence_flag") val lowConfidenceFlag: Boolean,
    @SerializedName("suspicious_flag") val suspiciousFlag: Boolean,

    // Notas
    @SerializedName("notes") val notes: String?,

    // Timestamps
    @SerializedName("created_at") val createdAt: String,
    @SerializedName("generated_at") val generatedAt: String
)