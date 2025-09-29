package com.capstone.cropcare.data.repository

import com.capstone.cropcare.domain.entity.ReportHistory
import com.capstone.cropcare.domain.repository.ReportHistoryRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FakeReportRepository @Inject constructor() : ReportHistoryRepository {
    override fun getReports(): List<ReportHistory> {
        return listOf(
            ReportHistory("Plaga", "Pulgones", "Zona Norte", "Pimientos", "21/05/2025", "14:30"),
            ReportHistory("Enfermedad", "Mildiu", "Zona Sur", "Tomates", "22/05/2025", "10:15"),
            ReportHistory("Deficiencia", "Nitrógeno", "Zona Centro", "Lechugas", "23/05/2025", "09:00"),


        )
    }
}