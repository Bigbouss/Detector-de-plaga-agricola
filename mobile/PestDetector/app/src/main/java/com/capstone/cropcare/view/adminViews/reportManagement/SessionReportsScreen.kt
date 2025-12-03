@file:OptIn(ExperimentalMaterial3Api::class)

package com.capstone.cropcare.view.adminViews.reportManagement

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.capstone.cropcare.R
import com.capstone.cropcare.data.remote.dto.ScanResultDTO
import com.capstone.cropcare.data.remote.dto.SessionWithReportDTO
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun SessionReportsScreen(
    session: SessionWithReportDTO,
    onDismiss: () -> Unit
) {
    var selectedScanResult by remember { mutableStateOf<ScanResultDTO?>(null) }

    val totalScans = session.scanResults?.size ?: session.totalScans
    val healthyCount = session.scanResults?.count { !it.hasPlague } ?: session.healthyCount
    val plagueCount = session.scanResults?.count { it.hasPlague } ?: session.plagueCount

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Reportes de Sesión") },
                navigationIcon = {
                    IconButton(onClick = onDismiss) {
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
                .padding(16.dp)
        ) {
            // Header con info de sesión
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = session.workerName,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = "${session.zonaName} • ${session.cultivoName}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = formatDateTime(session.finishedAt ?: session.startedAt),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Resumen con valores calculados
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                StatBadge(
                    label = "Total",
                    value = totalScans.toString(),
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.weight(1f)
                )
                StatBadge(
                    label = "Sanas",
                    value = healthyCount.toString(),
                    color = Color(0xFF4CAF50),
                    modifier = Modifier.weight(1f)
                )
                StatBadge(
                    label = "Plagas",
                    value = plagueCount.toString(),
                    color = Color(0xFFF44336),
                    modifier = Modifier.weight(1f)
                )
            }

            // Lista de escaneos
            Text(
                text = "Escaneos Realizados (${session.scanResults?.size ?: 0})",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            if (session.scanResults.isNullOrEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No hay escaneos registrados",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(session.scanResults) { scanResult ->
                        ScanResultCard(
                            scanResult = scanResult,
                            onClick = { selectedScanResult = it }
                        )
                    }
                }
            }
        }
    }

    // Dialog de detalles del escaneo individual
    selectedScanResult?.let { scanResult ->
        ScanResultDetailsDialog(
            scanResult = scanResult,
            onDismiss = { selectedScanResult = null }
        )
    }
}

@Composable
fun ScanResultCard(
    scanResult: ScanResultDTO,
    onClick: (ScanResultDTO) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick(scanResult) },
        colors = CardDefaults.cardColors(
            containerColor = if (scanResult.hasPlague)
                Color(0xFFF44336).copy(alpha = 0.1f)
            else
                Color(0xFF4CAF50).copy(alpha = 0.1f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(
                    imageVector = if (scanResult.hasPlague)
                        Icons.Default.Warning
                    else
                        Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = if (scanResult.hasPlague)
                        Color(0xFFF44336)
                    else
                        Color(0xFF4CAF50),
                    modifier = Modifier.size(32.dp)
                )

                Column {
                    Text(
                        text = scanResult.classification.replace("_", " "),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = formatTime(scanResult.scannedAt),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "${(scanResult.confidence * 100).toInt()}%",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (scanResult.confidence > 0.7)
                        Color(0xFF4CAF50)
                    else if (scanResult.confidence > 0.5)
                        Color(0xFFFF9800)
                    else
                        Color(0xFFF44336)
                )
                Text(
                    text = "Confianza",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun ScanResultDetailsDialog(
    scanResult: ScanResultDTO,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                imageVector = if (scanResult.hasPlague)
                    Icons.Default.Warning
                else
                    Icons.Default.CheckCircle,
                contentDescription = null,
                tint = if (scanResult.hasPlague)
                    Color(0xFFF44336)
                else
                    Color(0xFF4CAF50)
            )
        },
        title = {
            Text("Detalle del Escaneo")
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.verticalScroll(rememberScrollState())
            ) {
                scanResult.imageUrl?.let { imageUrl ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        AsyncImage(
                            model = imageUrl,
                            contentDescription = "Imagen del escaneo",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop,
                            error = painterResource(R.drawable.ic_error_image),
                            placeholder = painterResource(R.drawable.ic_image_placeholder)
                        )
                    }
                }

                DetailSection(title = "Clasificación") {
                    Text(
                        text = scanResult.classification.replace("_", " "),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                DetailSection(title = "Confianza") {
                    Text(
                        text = "${(scanResult.confidence * 100).toInt()}%",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (scanResult.confidence > 0.7)
                            Color(0xFF4CAF50)
                        else if (scanResult.confidence > 0.5)
                            Color(0xFFFF9800)
                        else
                            Color(0xFFF44336)
                    )
                }

                DetailSection(title = "Estado") {
                    Surface(
                        color = if (scanResult.hasPlague)
                            Color(0xFFF44336).copy(alpha = 0.2f)
                        else
                            Color(0xFF4CAF50).copy(alpha = 0.2f),
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = if (scanResult.hasPlague) "⚠️ PLAGA DETECTADA" else "✅ PLANTA SANA",
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.labelMedium,
                            color = if (scanResult.hasPlague)
                                Color(0xFFF44336)
                            else
                                Color(0xFF4CAF50),
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                DetailSection(title = "Fecha y Hora") {
                    Text(
                        text = formatDateTime(scanResult.scannedAt),
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                if (scanResult.imageUrl == null) {
                    DetailSection(title = "Ubicación de Foto") {
                        Text(
                            text = scanResult.photoPath,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Cerrar")
            }
        }
    )
}

@Composable
fun StatBadge(
    label: String,
    value: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        color = color.copy(alpha = 0.15f),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = color
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = color
            )
        }
    }
}

private fun formatDateTime(isoDate: String): String {
    return try {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
        val outputFormat = SimpleDateFormat("dd MMM yyyy HH:mm", Locale.getDefault())
        val date = inputFormat.parse(isoDate)
        outputFormat.format(date ?: Date())
    } catch (e: Exception) {
        isoDate
    }
}

private fun formatTime(isoDate: String): String {
    return try {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
        val outputFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
        val date = inputFormat.parse(isoDate)
        outputFormat.format(date ?: Date())
    } catch (e: Exception) {
        isoDate
    }
}