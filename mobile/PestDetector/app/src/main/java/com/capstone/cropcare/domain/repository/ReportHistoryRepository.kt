package com.capstone.cropcare.domain.repository

import com.capstone.cropcare.domain.entity.ReportHistory

interface ReportHistoryRepository {
    fun getReports(): List<ReportHistory>
}