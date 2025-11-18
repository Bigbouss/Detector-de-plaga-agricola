package com.capstone.cropcare.data.repository

import com.capstone.cropcare.data.local.dao.ReportDao
import com.capstone.cropcare.domain.mappers.toDomain
import com.capstone.cropcare.domain.mappers.toEntity
import com.capstone.cropcare.domain.model.ReportModel
import com.capstone.cropcare.domain.repository.ReportRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ReportRepositoryImpl @Inject constructor(
    private val reportDao: ReportDao
) : ReportRepository {

    override suspend fun insertReport(report: ReportModel): Long {
        return reportDao.insertReport(report.toEntity())
    }

    override suspend fun updateReport(report: ReportModel) {
        reportDao.updateReport( report.toEntity())
    }

    override suspend fun deleteReport(report: ReportModel) {
        reportDao.deleteReport(report.toEntity())
    }

    override fun getAllReports(): Flow<List<ReportModel>> {
        return reportDao.getAllReportsWithDetails().map { reportsWithDetails ->
            reportsWithDetails.mapNotNull { it.toDomain() }
        }
    }

    override suspend fun getReportById(id: Int): ReportModel? {
        return reportDao.getReportWithDetails(id)?.toDomain()
    }

    override suspend fun getUnsyncedReports(): List<ReportModel> {
        return reportDao.getUnsyncedReports().map { it.toDomain() }
    }

    override suspend fun markAsSynced(reportId: Int) {
        reportDao.markAsSynced(reportId)
    }
}