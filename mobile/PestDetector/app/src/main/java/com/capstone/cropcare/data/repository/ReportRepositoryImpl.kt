package com.capstone.cropcare.data.repository

import com.capstone.cropcare.data.local.dao.ReportDao
import com.capstone.cropcare.domain.mappers.toEntity
import com.capstone.cropcare.domain.mappers.toModel
import com.capstone.cropcare.domain.model.ReportModel
import com.capstone.cropcare.domain.repository.ReportRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class ReportRepositoryImpl @Inject constructor(private val reportDao: ReportDao) :
    ReportRepository {
    override suspend fun insertReport(report: ReportModel) {
        reportDao.insert(report.toEntity())
    }

    override suspend fun updateReport(report: ReportModel) {
        reportDao.update(report.toEntity())
    }

    override suspend fun deleteReport(report: ReportModel) {
        reportDao.delete(report.toEntity())
    }

    override fun getAllReport(): Flow<List<ReportModel>> {
        return reportDao.getAllReportsFlow().map { list -> list.map { it.toModel() } }
    }

    override fun getReportById(id: Int): Flow<ReportModel?> =
        reportDao.getReportByIdFlow(id).map {
            it?.toModel()
        }


    override fun getReportBetween(from: Long, to: Long): Flow<List<ReportModel>> {
        return reportDao.getBetween(from, to).map { list -> list.map { it.toModel() } }
    }
}