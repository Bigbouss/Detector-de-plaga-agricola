package com.capstone.cropcare.data.local.dao

import androidx.room.*
import com.capstone.cropcare.data.local.entity.ReportEntity
import com.capstone.cropcare.data.local.entity.relations.ReportWithDetails
import kotlinx.coroutines.flow.Flow

@Dao
interface ReportDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReport(report: ReportEntity): Long

    @Update
    suspend fun updateReport(report: ReportEntity)

    @Delete
    suspend fun deleteReport(report: ReportEntity)

    @Transaction
    @Query("SELECT * FROM reports ORDER BY timestamp DESC")
    fun getAllReportsWithDetails(): Flow<List<ReportWithDetails>>

    @Transaction
    @Query("SELECT * FROM reports WHERE reportId = :reportId")
    suspend fun getReportWithDetails(reportId: Int): ReportWithDetails?

    @Query("SELECT * FROM reports WHERE syncedWithBackend = 0")
    suspend fun getUnsyncedReports(): List<ReportEntity>

    @Query("UPDATE reports SET syncedWithBackend = 1 WHERE reportId = :reportId")
    suspend fun markAsSynced(reportId: Int)
}