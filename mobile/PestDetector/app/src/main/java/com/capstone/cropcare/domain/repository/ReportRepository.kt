package com.capstone.cropcare.domain.repository

import com.capstone.cropcare.domain.model.ReportModel
import kotlinx.coroutines.flow.Flow

interface ReportRepository {

    suspend fun insertReport(report: ReportModel)
    suspend fun updateReport(report: ReportModel)
    suspend fun deleteReport(report: ReportModel)

    fun getAllReport(): Flow<List<ReportModel>>
    fun getReportById(id: Int): Flow<ReportModel?>
    fun getReportBetween(from: Long, to: Long): Flow<List<ReportModel>>
}