package com.capstone.cropcare.domain.usecase


import com.capstone.cropcare.domain.model.ReportModel
import com.capstone.cropcare.domain.repository.ReportRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetReportHistoryUseCase@Inject constructor(
    private val repository: ReportRepository
) {
    operator fun invoke(): Flow<List<ReportModel>> {
        return repository.getAllReports()
    }
}
