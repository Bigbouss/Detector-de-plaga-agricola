@file:OptIn(ExperimentalMaterial3Api::class)

package com.capstone.cropcare.view.adminViews.reportManagement

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.capstone.cropcare.data.remote.dto.SessionReportDTO
import com.capstone.cropcare.data.remote.dto.SessionWithReportDTO
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun ReportManagementScreen(
    viewModel: ReportManagementViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    var showFilters by remember { mutableStateOf(false) }
    var selectedReport by remember { mutableStateOf<SessionReportDTO?>(null) }
    var selectedSession by remember { mutableStateOf<SessionWithReportDTO?>(null) }
    var expandedSession by remember { mutableStateOf<String?>(null) }
    var showReportsScreen by remember { mutableStateOf(false) }

    // Pantalla de reportes individuales
    if (showReportsScreen && selectedSession != null) {
        SessionReportsScreen(
            session = selectedSession!!,
            onDismiss = {
                showReportsScreen = false
                selectedSession = null
            }
        )
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Historial de Reportes",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                IconButton(onClick = { viewModel.loadSessions() }) {
                    Icon(Icons.Default.Refresh, "Actualizar")
                }
                IconButton(onClick = { showFilters = !showFilters }) {
                    Icon(
                        imageVector = if (showFilters) Icons.Default.Clear else Icons.Default.Build,
                        contentDescription = "Filtros"
                    )
                }
            }
        }

        // Filtros
        if (showFilters) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Filtros y Agrupación",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.height(12.dp))

                    // Agrupar por
                    Text(
                        text = "Agrupar por:",
                        style = MaterialTheme.typography.labelMedium,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        FilterChip(
                            selected = uiState.groupBy == GroupBy.DATE,
                            onClick = { viewModel.setGroupBy(GroupBy.DATE) },
                            label = { Text("Fecha") }
                        )
                        FilterChip(
                            selected = uiState.groupBy == GroupBy.WEEK,
                            onClick = { viewModel.setGroupBy(GroupBy.WEEK) },
                            label = { Text("Semana") }
                        )
                        FilterChip(
                            selected = uiState.groupBy == GroupBy.MONTH,
                            onClick = { viewModel.setGroupBy(GroupBy.MONTH) },
                            label = { Text("Mes") }
                        )
                        FilterChip(
                            selected = uiState.groupBy == GroupBy.WORKER,
                            onClick = { viewModel.setGroupBy(GroupBy.WORKER) },
                            label = { Text("Worker") }
                        )
                    }

                    Spacer(Modifier.height(12.dp))

                    // Filtrar por plagas
                    Text(
                        text = "Mostrar:",
                        style = MaterialTheme.typography.labelMedium,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        FilterChip(
                            selected = uiState.filterWithPlagues == null,
                            onClick = { viewModel.filterByPlagueStatus(null) },
                            label = { Text("Todas") }
                        )
                        FilterChip(
                            selected = uiState.filterWithPlagues == true,
                            onClick = { viewModel.filterByPlagueStatus(true) },
                            label = { Text("Solo con plagas") }
                        )
                        FilterChip(
                            selected = uiState.filterWithPlagues == false,
                            onClick = { viewModel.filterByPlagueStatus(false) },
                            label = { Text("Sin plagas") }
                        )
                    }
                }
            }
        }

        // Lista de sesiones
        if (uiState.isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else if (uiState.error != null) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(48.dp)
                    )
                    Text(
                        text = uiState.error ?: "Error desconocido",
                        color = MaterialTheme.colorScheme.error,
                        textAlign = TextAlign.Center
                    )
                    Button(onClick = { viewModel.loadSessions() }) {
                        Text("Reintentar")
                    }
                }
            }
        } else if (uiState.groupedSessions.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(48.dp)
                    )
                    Text(
                        text = "No hay reportes registrados",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                uiState.groupedSessions.forEach { (groupLabel, sessions) ->
                    // Grupo header
                    item {
                        Text(
                            text = groupLabel,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    }

                    // Sesiones del grupo
                    items(sessions) { session ->
                        SessionCard(
                            session = session,
                            isExpanded = expandedSession == session.sessionId,
                            onToggleExpand = {
                                expandedSession = if (expandedSession == session.sessionId) null else session.sessionId
                            },
                            onReportClick = { report ->
                                selectedReport = report
                                selectedSession = session
                            }
                        )
                    }
                }
            }
        }
    }

    // Dialog de detalles del reporte
    if (selectedReport != null && selectedSession != null) {
        ReportDetailsDialog(
            report = selectedReport!!,
            session = selectedSession!!,
            onDismiss = {
                selectedReport = null
                selectedSession = null
            },
            onViewReports = {
                selectedReport = null
                showReportsScreen = true
            }
        )
    }
}

@Composable
fun SessionCard(
    session: SessionWithReportDTO,
    isExpanded: Boolean,
    onToggleExpand: () -> Unit,
    onReportClick: (SessionReportDTO) -> Unit
) {

    val totalScans = session.scanResults?.size ?: session.totalScans
    val healthyCount = session.scanResults?.count { !it.hasPlague } ?: session.healthyCount
    val plagueCount = session.scanResults?.count { it.hasPlague } ?: session.plagueCount

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onToggleExpand() },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header: Worker + Fecha
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
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

                Icon(
                    imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = null
                )
            }

            Spacer(Modifier.height(12.dp))

            // Resumen de escaneos con valores calculados
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                ScanSummaryBadge(
                    label = "Total",
                    value = totalScans.toString(),
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.weight(1f)
                )
                ScanSummaryBadge(
                    label = "Sanas",
                    value = healthyCount.toString(),
                    color = Color(0xFF4CAF50),
                    modifier = Modifier.weight(1f)
                )
                ScanSummaryBadge(
                    label = "Plagas",
                    value = plagueCount.toString(),
                    color = Color(0xFFF44336),
                    modifier = Modifier.weight(1f)
                )
            }

            // Reporte expandido
            if (isExpanded && session.report != null) {
                Spacer(Modifier.height(16.dp))
                Divider()
                Spacer(Modifier.height(12.dp))

                // Título de reportes
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Reporte de Plagas",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold
                    )

                    if (session.report.suspiciousFlag) {
                        Surface(
                            color = Color(0xFFF44336).copy(alpha = 0.2f),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Warning,
                                    contentDescription = null,
                                    tint = Color(0xFFF44336),
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = "ALERTA",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = Color(0xFFF44336),
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }

                Spacer(Modifier.height(8.dp))

                // Tarjeta del reporte clickeable
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onReportClick(session.report) },
                    color = MaterialTheme.colorScheme.surface,
                    shape = RoundedCornerShape(8.dp),
                    tonalElevation = 1.dp
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        // Métricas del reporte
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            ReportMetric(
                                label = "Detecciones",
                                value = session.report.detectionsCount.toString(),
                                modifier = Modifier.weight(1f)
                            )
                            ReportMetric(
                                label = "Confianza",
                                value = "${((session.report.averageConfidence ?: 0f) * 100).toInt()}%",
                                modifier = Modifier.weight(1f)
                            )
                        }

                        if (session.report.uniqueLabels.isNotEmpty()) {
                            Spacer(Modifier.height(8.dp))
                            Text(
                                text = "Clasificaciones:",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = session.report.uniqueLabels.take(3).joinToString(", "),
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Medium
                            )
                        }

                        Spacer(Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            Text(
                                text = "Ver detalles →",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ScanSummaryBadge(
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
            modifier = Modifier.padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
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

@Composable
fun ReportMetric(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun ReportDetailsDialog(
    report: SessionReportDTO,
    session: SessionWithReportDTO,
    onDismiss: () -> Unit,
    onViewReports: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                imageVector = if (report.suspiciousFlag) Icons.Default.Warning else Icons.Default.Info,
                contentDescription = null,
                tint = if (report.suspiciousFlag) Color(0xFFF44336) else MaterialTheme.colorScheme.primary
            )
        },
        title = {
            Text("Detalle del Reporte")
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                // Información general
                DetailSection(title = "Información General") {
                    DetailRow("Worker", session.workerName)
                    DetailRow("Zona", session.zonaName)
                    DetailRow("Cultivo", session.cultivoName)
                    DetailRow("Generado", formatDateTime(report.generatedAt))
                }

                // Métricas
                DetailSection(title = "Métricas de Escaneo") {
                    DetailRow("Imágenes", report.imagesCount.toString())
                    DetailRow("Detecciones", report.detectionsCount.toString())
                    report.averageConfidence?.let {
                        DetailRow("Confianza Promedio", "${(it * 100).toInt()}%")
                    }
                    report.medianConfidence?.let {
                        DetailRow("Confianza Mediana", "${(it * 100).toInt()}%")
                    }
                }

                // Clasificaciones
                if (report.uniqueLabels.isNotEmpty()) {
                    DetailSection(title = "Clasificaciones Detectadas") {
                        report.uniqueLabels.forEach { label ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 2.dp)
                            ) {
                                Icon(
                                    imageVector = if ("Healthy" in label)
                                        Icons.Default.CheckCircle
                                    else
                                        Icons.Default.Warning,
                                    contentDescription = null,
                                    tint = if ("Healthy" in label)
                                        Color(0xFF4CAF50)
                                    else
                                        Color(0xFFF44336),
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    text = label.replace("_", " "),
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }
                    }
                }

                // Alertas
                if (report.suspiciousFlag || report.lowConfidenceFlag) {
                    DetailSection(title = "Alertas") {
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            if (report.suspiciousFlag) {
                                AlertBadge(
                                    text = "Plagas Detectadas",
                                    color = Color(0xFFF44336)
                                )
                            }
                            if (report.lowConfidenceFlag) {
                                AlertBadge(
                                    text = "Confianza Baja",
                                    color = Color(0xFFFF9800)
                                )
                            }
                        }
                    }
                }

                // Notas
                report.notes?.let { notes ->
                    if (notes.isNotBlank()) {
                        DetailSection(title = "Notas") {
                            Text(
                                text = notes,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TextButton(onClick = onViewReports) {
                    Icon(
                        imageVector = Icons.Default.List,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.width(4.dp))
                    Text("Ver Escaneos")
                }
                TextButton(onClick = onDismiss) {
                    Text("Cerrar")
                }
            }
        }
    )
}

@Composable
fun DetailSection(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Column {
        Text(
            text = title,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(Modifier.height(4.dp))
        content()
    }
}

@Composable
fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
fun AlertBadge(text: String, color: Color) {
    Surface(
        color = color.copy(alpha = 0.2f),
        shape = RoundedCornerShape(4.dp)
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelSmall,
            color = color,
            fontWeight = FontWeight.Bold
        )
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