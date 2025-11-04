package com.capstone.cropcare.data.local.dao

import androidx.room.*
import com.capstone.cropcare.data.local.entity.ZoneEntity
import com.capstone.cropcare.data.local.entity.relations.ZoneWithCrops
import kotlinx.coroutines.flow.Flow

@Dao
interface ZoneDao {
    @Query("SELECT * FROM zones ORDER BY zoneName ASC")
    fun getAllZones(): Flow<List<ZoneEntity>>

    @Query("SELECT * FROM zones WHERE zoneId = :zoneId")
    suspend fun getZoneById(zoneId: String): ZoneEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertZone(zone: ZoneEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertZones(zones: List<ZoneEntity>)

    @Delete
    suspend fun deleteZone(zone: ZoneEntity)

    @Query("DELETE FROM zones")
    suspend fun deleteAll()

    @Query("DELETE FROM zones WHERE zoneId = :zoneId")
    suspend fun deleteZone(zoneId: String)

    @Transaction
    @Query("SELECT * FROM zones")
    fun getZonesWithCrops(): Flow<List<ZoneWithCrops>>
}