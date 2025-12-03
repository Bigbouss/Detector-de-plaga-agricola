package com.capstone.cropcare.data.remote.dto

import com.google.gson.annotations.SerializedName

data class MetricsResponseDTO(
    @SerializedName("summary") val summary: MetricsSummaryDTO,
    @SerializedName("plague_distribution") val plagueDistribution: PlagueDistributionDTO,
    @SerializedName("top_diseases") val topDiseases: List<DiseaseCountDTO>,
    @SerializedName("by_cultivo") val byCultivo: List<CultivoStatsDTO>,
    @SerializedName("by_zona") val byZona: List<ZonaStatsDTO>,
    @SerializedName("timeline") val timeline: List<TimelineDataDTO>
)

data class MetricsSummaryDTO(
    @SerializedName("total_reports") val totalReports: Int,
    @SerializedName("reports_with_plagues") val reportsWithPlagues: Int,
    @SerializedName("reports_healthy") val reportsHealthy: Int,
    @SerializedName("avg_confidence") val avgConfidence: Float,
    @SerializedName("plague_percentage") val plaguePercentage: Float
)

data class PlagueDistributionDTO(
    @SerializedName("healthy") val healthy: Int,
    @SerializedName("with_plague") val withPlague: Int,
    @SerializedName("total") val total: Int
)

data class DiseaseCountDTO(
    @SerializedName("label") val label: String,
    @SerializedName("count") val count: Int
)

data class CultivoStatsDTO(
    @SerializedName("cultivo__nombre") val cultivoNombre: String,
    @SerializedName("total") val total: Int,
    @SerializedName("with_plague") val withPlague: Int,
    @SerializedName("healthy") val healthy: Int
)

data class ZonaStatsDTO(
    @SerializedName("zona__nombre") val zonaNombre: String,
    @SerializedName("total") val total: Int,
    @SerializedName("with_plague") val withPlague: Int,
    @SerializedName("healthy") val healthy: Int
)

data class TimelineDataDTO(
    @SerializedName("date") val date: String?,
    @SerializedName("total") val total: Int,
    @SerializedName("with_plague") val withPlague: Int,
    @SerializedName("healthy") val healthy: Int,
    @SerializedName("avg_confidence") val avgConfidence: Float
)