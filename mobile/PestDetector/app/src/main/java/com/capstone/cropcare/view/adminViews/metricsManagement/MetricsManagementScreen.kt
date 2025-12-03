@file:OptIn(ExperimentalMaterial3Api::class)

package com.capstone.cropcare.view.adminViews.metricsManagement

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.capstone.cropcare.R
import kotlin.math.cos
import kotlin.math.sin
import androidx.compose.ui.graphics.painter.Painter

@Composable
fun MetricsManagementScreen(
    viewModel: MetricsManagementViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var showFilters by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Métricas y Análisis",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Periodo: ${getPeriodLabel(uiState.period)}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // En el header, antes del IconButton de exportar
                if (uiState.isExportingPdf) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        strokeWidth = 2.dp
                    )
                } else {
                    IconButton(onClick = { viewModel.exportPdf() }) {
                        Icon(painter = painterResource(R.drawable.ic_download), "Exportar PDF")
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {

                    IconButton(onClick = { showFilters = !showFilters }) {
                        Icon(
                            painter = painterResource(
                                if (showFilters) R.drawable.ic_close else R.drawable.ic_filter
                            ),
                            contentDescription = "Filtros"
                        )
                    }
                    IconButton(onClick = { viewModel.loadMetrics() }) {
                        Icon(
                            painter = painterResource(R.drawable.ic_reload_2),
                            contentDescription = "Actualizar"
                        )
                    }
                }
            }
        }

        // Filtros
        if (showFilters) {
            item {
                FiltersCard(
                    selectedPeriod = uiState.period,
                    onPeriodChange = { viewModel.setPeriod(it) }
                )
            }
        }

        if (uiState.isLoading) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
        } else if (uiState.error != null) {
            item {
                ErrorCard(
                    error = uiState.error!!,
                    onRetry = { viewModel.loadMetrics() }
                )
            }
        } else if (uiState.metrics != null) {
            val metrics = uiState.metrics!!

            // Resumen general
            item {
                SummaryCards(summary = metrics.summary)
            }

            // Gráfico de torta
            item {
                PieChartCard(distribution = metrics.plagueDistribution)
            }

            // Top enfermedades
            if (metrics.topDiseases.isNotEmpty()) {
                item {
                    TopDiseasesCard(diseases = metrics.topDiseases)
                }
            }

            // Por cultivo
            if (metrics.byCultivo.isNotEmpty()) {
                item {
                    CultivoStatsCard(stats = metrics.byCultivo)
                }
            }

        }
    }
}

@Composable
fun FiltersCard(
    selectedPeriod: String,
    onPeriodChange: (String) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Período de Análisis",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = selectedPeriod == "week",
                    onClick = { onPeriodChange("week") },
                    label = { Text("Semana") }
                )
                FilterChip(
                    selected = selectedPeriod == "month",
                    onClick = { onPeriodChange("month") },
                    label = { Text("Mes") }
                )
                FilterChip(
                    selected = selectedPeriod == "year",
                    onClick = { onPeriodChange("year") },
                    label = { Text("Año") }
                )
            }
        }
    }
}

@Composable
fun SummaryCards(summary: com.capstone.cropcare.data.remote.dto.MetricsSummaryDTO) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            MetricCard(
                title = "Total Reportes",
                value = summary.totalReports.toString(),
                icon = painterResource(R.drawable.ic_description),
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.weight(1f)
            )
            MetricCard(
                title = "Con Plagas",
                value = summary.reportsWithPlagues.toString(),
                icon = painterResource(R.drawable.ic_warning),
                color = Color(0xFFF44336),
                modifier = Modifier.weight(1f)
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            MetricCard(
                title = "Saludables",
                value = summary.reportsHealthy.toString(),
                icon = painterResource(R.drawable.ic_check),
                color = Color(0xFF4CAF50),
                modifier = Modifier.weight(1f)
            )
            MetricCard(
                title = "Confianza Avg",
                value = "${(summary.avgConfidence * 100).toInt()}%",
                icon = painterResource(R.drawable.ic_trending_up),
                color = Color(0xFF2196F3),
                modifier = Modifier.weight(1f)
            )
        }
    }
}



@Composable
fun MetricCard(
    title: String,
    value: String,
    icon: Painter,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.1f))
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(value, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, color = color)
                Text(title, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }

            Icon(
                painter = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(40.dp)
            )
        }
    }
}


@Composable
fun PieChartCard(distribution: com.capstone.cropcare.data.remote.dto.PlagueDistributionDTO) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Distribución de Plagas",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(Modifier.height(16.dp))

            PieChart(
                healthy = distribution.healthy,
                withPlague = distribution.withPlague,
                modifier = Modifier.size(200.dp)
            )

            Spacer(Modifier.height(16.dp))

            // Leyenda
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                LegendItem(
                    color = Color(0xFF4CAF50),
                    label = "Sanas",
                    value = distribution.healthy
                )
                LegendItem(
                    color = Color(0xFFF44336),
                    label = "Con Plagas",
                    value = distribution.withPlague
                )
            }
        }
    }
}

@Composable
fun PieChart(
    healthy: Int,
    withPlague: Int,
    modifier: Modifier = Modifier
) {
    val total = healthy + withPlague
    val healthyAngle by animateFloatAsState(
        targetValue = if (total > 0) (healthy.toFloat() / total * 360f) else 0f,
        animationSpec = tween(1000),
        label = "healthyAngle"
    )

    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val radius = size.minDimension / 2
            val center = Offset(size.width / 2, size.height / 2)

            // Plaga (rojo)
            drawArc(
                color = Color(0xFFF44336),
                startAngle = 0f,
                sweepAngle = 360f - healthyAngle,
                useCenter = true,
                topLeft = Offset(center.x - radius, center.y - radius),
                size = Size(radius * 2, radius * 2)
            )

            // Sanas (verde)
            drawArc(
                color = Color(0xFF4CAF50),
                startAngle = 360f - healthyAngle,
                sweepAngle = healthyAngle,
                useCenter = true,
                topLeft = Offset(center.x - radius, center.y - radius),
                size = Size(radius * 2, radius * 2)
            )

            // Círculo blanco interior
            drawCircle(
                color = Color.White,
                radius = radius * 0.5f,
                center = center
            )
        }

        // Porcentaje en el centro
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "${if (total > 0) (withPlague.toFloat() / total * 100).toInt() else 0}%",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Plagas",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun LegendItem(
    color: Color,
    label: String,
    value: Int
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Box(
            modifier = Modifier
                .size(16.dp)
                .clip(CircleShape)
                .background(color)
        )
        Column {
            Text(
                text = value.toString(),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun TopDiseasesCard(diseases: List<com.capstone.cropcare.data.remote.dto.DiseaseCountDTO>) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Top 10 Enfermedades",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(Modifier.height(12.dp))

            diseases.take(10).forEach { disease ->
                DiseaseBar(
                    label = disease.label.replace("_", " "),
                    count = disease.count,
                    maxCount = diseases.first().count
                )
                Spacer(Modifier.height(8.dp))
            }
        }
    }
}

@Composable
fun DiseaseBar(
    label: String,
    count: Int,
    maxCount: Int
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.weight(1f)
            )
            Text(
                text = count.toString(),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFF44336)
            )
        }

        Spacer(Modifier.height(4.dp))

        val progress by animateFloatAsState(
            targetValue = if (maxCount > 0) count.toFloat() / maxCount else 0f,
            animationSpec = tween(1000),
            label = "progress"
        )

        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp)),
            color = Color(0xFFF44336),
            trackColor = Color(0xFFF44336).copy(alpha = 0.2f)
        )
    }
}

@Composable
fun CultivoStatsCard(stats: List<com.capstone.cropcare.data.remote.dto.CultivoStatsDTO>) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Estadísticas por Cultivo",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(Modifier.height(12.dp))

            stats.forEach { cultivo ->
                CultivoRow(cultivo = cultivo)
                Spacer(Modifier.height(8.dp))
            }
        }
    }
}

@Composable
fun CultivoRow(cultivo: com.capstone.cropcare.data.remote.dto.CultivoStatsDTO) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surfaceVariant,
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = cultivo.cultivoNombre,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.weight(1f)
            )

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                StatBadge(
                    label = "Total",
                    value = cultivo.total.toString(),
                    color = MaterialTheme.colorScheme.primary
                )
                StatBadge(
                    label = "Plagas",
                    value = cultivo.withPlague.toString(),
                    color = Color(0xFFF44336)
                )
                StatBadge(
                    label = "Sanas",
                    value = cultivo.healthy.toString(),
                    color = Color(0xFF4CAF50)
                )
            }
        }
    }
}

@Composable
fun StatBadge(
    label: String,
    value: String,
    color: Color
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
            color = color
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun ErrorCard(error: String, onRetry: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_error),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.error,
                modifier = Modifier.size(48.dp)
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = error,
                color = MaterialTheme.colorScheme.onErrorContainer,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(8.dp))
            Button(onClick = onRetry) {
                Text("Reintentar")
            }
        }
    }
}

private fun getPeriodLabel(period: String): String {
    return when (period) {
        "week" -> "Última Semana"
        "month" -> "Último Mes"
        "year" -> "Último Año"
        else -> period
    }
}