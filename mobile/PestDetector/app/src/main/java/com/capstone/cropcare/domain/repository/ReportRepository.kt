package com.capstone.cropcare.domain.repository

import com.capstone.cropcare.domain.model.ReportModel
import kotlinx.coroutines.flow.Flow

interface ReportRepository {
    suspend fun insertReport(report: ReportModel): Long
    suspend fun updateReport(report: ReportModel)
    suspend fun deleteReport(report: ReportModel)
    fun getAllReports(): Flow<List<ReportModel>>
    suspend fun getReportById(id: Int): ReportModel?
    suspend fun getUnsyncedReports(): List<ReportModel>
    suspend fun markAsSynced(reportId: Int)
}