package com.capstone.cropcare.data.local.dao

import androidx.room.*
import com.capstone.cropcare.data.local.entity.CropEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CropDao {
    @Query("SELECT * FROM crops WHERE zoneId = :zoneId ORDER BY cropName ASC")
    fun getCropsByZone(zoneId: String): Flow<List<CropEntity>>

    @Query("SELECT * FROM crops WHERE cropId = :cropId")
    suspend fun getCropById(cropId: String): CropEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCrop(crop: CropEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCrops(crops: List<CropEntity>)

    @Delete
    suspend fun deleteCrop(crop: CropEntity)

    @Query("DELETE FROM crops")
    suspend fun deleteAll()

    @Query("DELETE FROM crops WHERE zoneId = :zoneId")
    suspend fun deleteCropsByZone(zoneId: String)
}