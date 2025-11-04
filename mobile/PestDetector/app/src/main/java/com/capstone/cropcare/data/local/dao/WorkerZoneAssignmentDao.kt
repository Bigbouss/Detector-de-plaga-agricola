package com.capstone.cropcare.data.local.dao

import androidx.room.*
import com.capstone.cropcare.data.local.entity.WorkerZoneAssignmentEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface WorkerZoneAssignmentDao {

    @Query("SELECT * FROM worker_zone_assignments WHERE workerId = :workerId")
    fun getAssignedZoneIds(workerId: String): Flow<List<WorkerZoneAssignmentEntity>>

    @Query("SELECT zoneId FROM worker_zone_assignments WHERE workerId = :workerId")
    suspend fun getZoneIdsForWorker(workerId: String): List<String>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAssignment(assignment: WorkerZoneAssignmentEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAssignments(assignments: List<WorkerZoneAssignmentEntity>)

    @Query("DELETE FROM worker_zone_assignments WHERE workerId = :workerId")
    suspend fun deleteAssignmentsForWorker(workerId: String)

    @Query("DELETE FROM worker_zone_assignments WHERE workerId = :workerId AND zoneId = :zoneId")
    suspend fun deleteAssignment(workerId: String, zoneId: String)

    @Query("DELETE FROM worker_zone_assignments")
    suspend fun deleteAll()

    @Query("""
        SELECT COUNT(*) > 0 
        FROM worker_zone_assignments 
        WHERE workerId = :workerId AND zoneId = :zoneId
    """)
    suspend fun isZoneAssignedToWorker(workerId: String, zoneId: String): Boolean
}