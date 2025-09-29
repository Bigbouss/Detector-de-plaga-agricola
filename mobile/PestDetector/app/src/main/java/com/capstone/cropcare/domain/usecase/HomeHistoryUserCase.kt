package com.capstone.cropcare.domain.usecase

import com.capstone.cropcare.domain.entity.ReportHistory
import com.capstone.cropcare.domain.repository.ReportHistoryRepository
import javax.inject.Inject

class HomeHistoryUserCase @Inject constructor(
    private val repository: ReportHistoryRepository
) {
    operator fun invoke(): List<ReportHistory> = repository.getReports()
}