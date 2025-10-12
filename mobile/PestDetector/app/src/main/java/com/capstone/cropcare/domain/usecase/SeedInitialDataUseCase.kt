package com.capstone.cropcare.domain.usecase

import com.capstone.cropcare.domain.model.CropModel
import com.capstone.cropcare.domain.model.ZoneModel
import com.capstone.cropcare.domain.repository.CropZoneRepository
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class SeedInitialDataUseCase @Inject constructor(
    private val cropZoneRepository: CropZoneRepository
) {
    suspend operator fun invoke() {
        // Verifica si ya hay datos
        val zones = cropZoneRepository.getAllZones().first()

        if (zones.isEmpty()) {
            insertDefaultData()
        }
    }

    private suspend fun insertDefaultData() {
        // Zonas por defecto
        val zones = listOf(
            ZoneModel(id = "zone_a", name = "Zona A", description = "Sector Norte"),
            ZoneModel(id = "zone_b", name = "Zona B", description = "Sector Sur"),
            ZoneModel(id = "zone_c", name = "Zona C", description = "Sector Este")
        )

        zones.forEach { zone ->
            cropZoneRepository.insertZone(zone)
        }

        // Cultivos por defecto
        val crops = listOf(
            // Zona A
            CropModel(id = "crop_a1", name = "Papas", zoneId = "zone_a"),
            CropModel(id = "crop_a2", name = "Sandías", zoneId = "zone_a"),
            CropModel(id = "crop_a3", name = "Manzanas", zoneId = "zone_a"),

            // Zona B
            CropModel(id = "crop_b1", name = "Tomates", zoneId = "zone_b"),
            CropModel(id = "crop_b2", name = "Lechugas", zoneId = "zone_b"),
            CropModel(id = "crop_b3", name = "Zanahorias", zoneId = "zone_b"),

            // Zona C
            CropModel(id = "crop_c1", name = "Maíz", zoneId = "zone_c"),
            CropModel(id = "crop_c2", name = "Trigo", zoneId = "zone_c"),
            CropModel(id = "crop_c3", name = "Cebada", zoneId = "zone_c")
        )

        crops.forEach { crop ->
            cropZoneRepository.insertCrop(crop)
        }
    }
}