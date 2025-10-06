package com.capstone.cropcare.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.capstone.cropcare.data.local.entity.ReportEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ReportDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(report: ReportEntity): Long

    @Update
    suspend fun update(report: ReportEntity): Int

    @Delete
    suspend fun delete(report: ReportEntity): Int

    @Query("DELETE FROM report_form")
    suspend fun deleteAll()

    //-> OBTENER HISTORIAL
    @Query("SELECT * FROM report_form ORDER BY timestamp DESC")
    fun getAllReportsFlow(): Flow<List<ReportEntity>>

    //-> OBTENER REPORT POR 'ID'
    @Query("SELECT * FROM report_form WHERE id = :id LIMIT 1")
    fun getReportByIdFlow(id: Int): Flow<ReportEntity?>

    //-> BUSCAR POR NOMBRE DE 'TRABAJOR' O 'DIAGNOSTICO'
    @Query("SELECT * FROM report_form WHERE worker_name LIKE '%' || :q || '%' OR diagnostic LIKE '%' || :q || '%' ORDER BY timestamp DESC ")
    fun search(q: String): Flow<List<ReportEntity>>

    //-> FILTRO POR RANGO DE FECHAS
    @Query("SELECT * FROM report_form WHERE timestamp BETWEEN :from AND :to ORDER BY timestamp DESC")
    fun getBetween(from: Long, to: Long): Flow<List<ReportEntity>>

    // CONTEO
    @Query("SELECT COUNT(*) FROM report_form")
    suspend fun count(): Int


}