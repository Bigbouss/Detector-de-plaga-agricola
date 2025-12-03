
package com.capstone.cropcare.view.workerViews.scanConfig


import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.capstone.cropcare.view.core.components.CropButtonPrimary
import com.capstone.cropcare.view.core.components.CropDropdown

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScanConfigScreen(
    viewModel: ScanConfigViewModel = hiltViewModel(),
    navigateToScanning: (sessionId: String) -> Unit,
    onBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val availableZones by viewModel.availableZones.collectAsStateWithLifecycle()
    val availableCrops by viewModel.availableCrops.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Configurar Escaneo") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, "Volver")
                    }
                }
            )
        }
    ) { padding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Título explicativo
            Text(
                text = "Configuración de Actividad",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = "Selecciona la zona y cultivo que vas a inspeccionar",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(Modifier.height(8.dp))

            // Card informativa
            InfoCard(
                icon = Icons.Default.Info,
                title = "¿Cómo funciona?",
                description = "1. Selecciona zona y cultivo\n" +
                        "2. Escanea múltiples plantas\n" +
                        "3. El sistema detectará automáticamente plagas\n" +
                        "4. Solo reportas las plantas con problemas"
            )

            Spacer(Modifier.height(16.dp))

            // Dropdown de Zona
            CropDropdown(
                selected = uiState.selectedZone?.name ?: "",
                options = availableZones.map { it.name },
                label = "Zona de cultivo",
                enabled = !uiState.isLoading,
                onSelect = { zoneName ->
                    val zone = availableZones.find { it.name == zoneName }
                    zone?.let { viewModel.selectZone(it) }
                }
            )

            // Dropdown de Cultivo
            CropDropdown(
                selected = uiState.selectedCrop?.name ?: "",
                options = availableCrops.map { it.name },
                label = "Tipo de cultivo",
                enabled = uiState.selectedZone != null && !uiState.isLoading,
                onSelect = { cropName ->
                    val crop = availableCrops.find { it.name == cropName }
                    crop?.let { viewModel.selectCrop(it) }
                }
            )

            if (uiState.selectedZone == null) {
                Text(
                    text = "Selecciona una zona primero",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error
                )
            } else if (availableCrops.isEmpty()) {
                Text(
                    text = "No hay cultivos en esta zona",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(Modifier.weight(1f))

            // Resumen de selección
            if (uiState.selectedZone != null && uiState.selectedCrop != null) {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "Vas a escanear:",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = "${uiState.selectedCrop!!.name} en ${uiState.selectedZone!!.name}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }

            // Botón iniciar
            CropButtonPrimary(
                modifier = Modifier.fillMaxWidth(),
                text = "Iniciar Escaneo",
                enabled = uiState.selectedZone != null &&
                        uiState.selectedCrop != null &&
                        !uiState.isLoading,
                onClick = {
                    viewModel.startSession { sessionId ->
                        navigateToScanning(sessionId)
                    }
                }
            )
        }
    }
}

@Composable
fun InfoCard(
    icon: ImageVector,
    title: String,
    description: String
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}


