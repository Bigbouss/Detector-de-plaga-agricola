//package com.capstone.cropcare.view.workerViews.scanning
//
//import android.graphics.Bitmap
//import androidx.compose.foundation.background
//import androidx.compose.foundation.layout.*
//import androidx.compose.foundation.shape.RoundedCornerShape
//import androidx.compose.material.icons.Icons
//import androidx.compose.material.icons.filled.*
//import androidx.compose.material3.*
//import androidx.compose.runtime.*
//import androidx.compose.ui.Alignment
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.graphics.Color
//import androidx.compose.ui.text.font.FontWeight
//import androidx.compose.ui.text.style.TextAlign
//import androidx.compose.ui.unit.dp
//import androidx.compose.ui.window.DialogProperties
//import androidx.hilt.navigation.compose.hiltViewModel
//import androidx.lifecycle.compose.collectAsStateWithLifecycle
//import com.capstone.cropcare.view.core.components.CropButtonPrimary
//
//import com.capstone.cropcare.view.workerViews.CameraScreen
//import java.text.SimpleDateFormat
//import java.util.*
//
//@OptIn(ExperimentalMaterial3Api::class)
//@Composable
//fun ScanningScreen(
//    sessionId: String,
//    viewModel: ScanningViewModel = hiltViewModel(),
//    onNavigateToQuickReport: (sessionId: String, scanResultId: String) -> Unit,
//    onFinishSession: () -> Unit,
//    onBack: () -> Unit
//) {
//    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
//    val session by viewModel.currentSession.collectAsStateWithLifecycle()
//
//    LaunchedEffect(sessionId) {
//        viewModel.loadSession(sessionId)
//    }
//
//    // Diálogos
//    var showExitDialog by remember { mutableStateOf(false) }
//    var showSummaryDialog by remember { mutableStateOf(false) }
//
//    // Diálogo de confirmación para salir
//    if (showExitDialog) {
//        AlertDialog(
//            onDismissRequest = { showExitDialog = false },
//            title = { Text("¿Cancelar escaneo?") },
//            text = { Text("Se perderán los datos de esta sesión.") },
//            confirmButton = {
//                TextButton(
//                    onClick = {
//                        viewModel.cancelSession()
//                        onBack()
//                    }
//                ) {
//                    Text("Sí, cancelar")
//                }
//            },
//            dismissButton = {
//                TextButton(onClick = { showExitDialog = false }) {
//                    Text("Continuar escaneando")
//                }
//            }
//        )
//    }
//
//    // Diálogo de resumen de sesión
//    if (showSummaryDialog && session != null) {
//        SessionSummaryDialog(
//            session = session!!,
//            onDismiss = { showSummaryDialog = false },
//            onConfirm = {
//                viewModel.finishSession()
//                showSummaryDialog = false
//                onFinishSession()
//            }
//        )
//    }
//
//    // UI Principal
//    Box(modifier = Modifier.fillMaxSize()) {
//        // Vista de cámara en el fondo
//        CameraScreen(
//            modifier = Modifier.fillMaxSize(),
//            onPhotoTaken = { bitmap ->
//                viewModel.analyzePhoto(bitmap)
//            }
//        )
//
//        // Overlay con información y controles
//        Column(
//            modifier = Modifier
//                .fillMaxSize()
//                .padding(16.dp),
//            verticalArrangement = Arrangement.SpaceBetween
//        ) {
//            // Top Bar con info de sesión y stats
//            Column(
//                verticalArrangement = Arrangement.spacedBy(8.dp)
//            ) {
//                // Botón de cerrar
//                IconButton(
//                    onClick = { showExitDialog = true },
//                    colors = IconButtonDefaults.iconButtonColors(
//                        containerColor = Color.Black.copy(alpha = 0.5f),
//                        contentColor = Color.White
//                    )
//                ) {
//                    Icon(Icons.Default.Close, "Cancelar")
//                }
//
//                // Info de la sesión
//                session?.let {
//                    Card(
//                        colors = CardDefaults.cardColors(
//                            containerColor = Color.Black.copy(alpha = 0.7f)
//                        )
//                    ) {
//                        Column(
//                            modifier = Modifier.padding(12.dp)
//                        ) {
//                            Text(
//                                text = "${it.cropName} - ${it.zoneName}",
//                                style = MaterialTheme.typography.titleMedium,
//                                fontWeight = FontWeight.Bold,
//                                color = Color.White
//                            )
//                            Spacer(Modifier.height(8.dp))
//                            Row(
//                                modifier = Modifier.fillMaxWidth(),
//                                horizontalArrangement = Arrangement.SpaceEvenly
//                            ) {
//                                StatChip(
//                                    label = "Total",
//                                    value = it.totalScans.toString(),
//                                    color = Color(0xFF2196F3)
//                                )
//                                StatChip(
//                                    label = "Sanas",
//                                    value = it.healthyCount.toString(),
//                                    color = Color(0xFF4CAF50)
//                                )
//                                StatChip(
//                                    label = "Plagas",
//                                    value = it.plagueCount.toString(),
//                                    color = Color(0xFFF44336)
//                                )
//                            }
//                        }
//                    }
//                }
//            }
//
//            // Resultado de clasificación (si hay)
//            if (uiState.lastClassification != null) {
//                ClassificationResultCard(
//                    classification = uiState.lastClassification!!,
//                    confidence = uiState.confidence ?: 0f,
//                    hasPlague = uiState.hasPlague,
//                    onCreateReport = {
//                        uiState.lastScanResultId?.let { scanResultId ->
//                            onNavigateToQuickReport(sessionId, scanResultId)
//                        }
//                    }
//                )
//            }
//
//            // Botón terminar sesión
//            if ((session?.totalScans ?: 0) > 0) {
//                Button(
//                    onClick = { showSummaryDialog = true },
//                    modifier = Modifier.fillMaxWidth(),
//                    colors = ButtonDefaults.buttonColors(
//                        containerColor = Color.Black.copy(alpha = 0.7f)
//                    ),
//                    shape = RoundedCornerShape(16.dp)
//                ) {
//                    Icon(Icons.Default.CheckCircle, null)
//                    Spacer(Modifier.width(8.dp))
//                    Text("Terminar Escaneo")
//                }
//            }
//        }
//
//        // Indicador de procesamiento
//        if (uiState.isProcessing) {
//            Box(
//                modifier = Modifier
//                    .fillMaxSize()
//                    .background(Color.Black.copy(alpha = 0.5f)),
//                contentAlignment = Alignment.Center
//            ) {
//                Card {
//                    Column(
//                        modifier = Modifier.padding(24.dp),
//                        horizontalAlignment = Alignment.CenterHorizontally
//                    ) {
//                        CircularProgressIndicator()
//                        Spacer(Modifier.height(16.dp))
//                        Text("Analizando planta...")
//                    }
//                }
//            }
//        }
//    }
//}
//
//@Composable
//fun StatChip(
//    label: String,
//    value: String,
//    color: Color
//) {
//    Surface(
//        color = color.copy(alpha = 0.2f),
//        shape = RoundedCornerShape(8.dp)
//    ) {
//        Column(
//            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
//            horizontalAlignment = Alignment.CenterHorizontally
//        ) {
//            Text(
//                text = value,
//                style = MaterialTheme.typography.titleLarge,
//                fontWeight = FontWeight.Bold,
//                color = color
//            )
//            Text(
//                text = label,
//                style = MaterialTheme.typography.bodySmall,
//                color = Color.White
//            )
//        }
//    }
//}
//
//@Composable
//fun ClassificationResultCard(
//    classification: String,
//    confidence: Float,
//    hasPlague: Boolean,
//    onCreateReport: () -> Unit
//) {
//    Card(
//        colors = CardDefaults.cardColors(
//            containerColor = if (hasPlague)
//                Color(0xFFFFEBEE).copy(alpha = 0.95f)
//            else
//                Color(0xFFE8F5E9).copy(alpha = 0.95f)
//        )
//    ) {
//        Column(
//            modifier = Modifier
//                .fillMaxWidth()
//                .padding(16.dp),
//            verticalArrangement = Arrangement.spacedBy(12.dp)
//        ) {
//            Row(
//                verticalAlignment = Alignment.CenterVertically,
//                horizontalArrangement = Arrangement.spacedBy(12.dp)
//            ) {
//                Icon(
//                    imageVector = if (hasPlague) Icons.Default.Warning else Icons.Default.CheckCircle,
//                    contentDescription = null,
//                    tint = if (hasPlague) Color(0xFFF44336) else Color(0xFF4CAF50),
//                    modifier = Modifier.size(40.dp)
//                )
//
//                Column(modifier = Modifier.weight(1f)) {
//                    Text(
//                        text = if (hasPlague) "Plaga Detectada" else "Planta Sana",
//                        style = MaterialTheme.typography.titleMedium,
//                        fontWeight = FontWeight.Bold,
//                        color = if (hasPlague) Color(0xFFC62828) else Color(0xFF2E7D32)
//                    )
//                    Text(
//                        text = classification,
//                        style = MaterialTheme.typography.bodyMedium
//                    )
//                    Text(
//                        text = "Confianza: ${(confidence * 100).toInt()}%",
//                        style = MaterialTheme.typography.bodySmall,
//                        color = MaterialTheme.colorScheme.onSurfaceVariant
//                    )
//                }
//            }
//
//            if (hasPlague) {
//                Button(
//                    onClick = onCreateReport,
//                    modifier = Modifier.fillMaxWidth(),
//                    colors = ButtonDefaults.buttonColors(
//                        containerColor = Color(0xFFF44336)
//                    )
//                ) {
//                    Icon(Icons.Default.Build, null)
//                    Spacer(Modifier.width(8.dp))
//                    Text("Crear Reporte de Plaga")
//                }
//            }
//        }
//    }
//}
//
//@OptIn(ExperimentalMaterial3Api::class)
//@Composable
//fun SessionSummaryDialog(
//    session: com.capstone.cropcare.domain.model.ScanSessionModel,
//    onDismiss: () -> Unit,
//    onConfirm: () -> Unit
//) {
//    AlertDialog(
//        onDismissRequest = onDismiss,
//        properties = DialogProperties(usePlatformDefaultWidth = false),
//        modifier = Modifier
//            .fillMaxWidth(0.9f)
//            .wrapContentHeight()
//    ) {
//        Card {
//            Column(
//                modifier = Modifier.padding(24.dp),
//                verticalArrangement = Arrangement.spacedBy(16.dp),
//                horizontalAlignment = Alignment.CenterHorizontally
//            ) {
//                // Icono de éxito
//                Icon(
//                    imageVector = Icons.Default.CheckCircle,
//                    contentDescription = null,
//                    tint = Color(0xFF4CAF50),
//                    modifier = Modifier.size(64.dp)
//                )
//
//                Text(
//                    text = "Resumen de Sesión",
//                    style = MaterialTheme.typography.headlineSmall,
//                    fontWeight = FontWeight.Bold
//                )
//
//                Divider()
//
//                // Estadísticas
//                Row(
//                    modifier = Modifier.fillMaxWidth(),
//                    horizontalArrangement = Arrangement.SpaceEvenly
//                ) {
//                    SummaryStatColumn(
//                        value = session.totalScans.toString(),
//                        label = "Total",
//                        icon = Icons.Default.Star,
//                        color = MaterialTheme.colorScheme.primary
//                    )
//
//                    SummaryStatColumn(
//                        value = session.healthyCount.toString(),
//                        label = "Sanas",
//                        icon = Icons.Default.CheckCircle,
//                        color = Color(0xFF4CAF50)
//                    )
//
//                    SummaryStatColumn(
//                        value = session.plagueCount.toString(),
//                        label = "Plagas",
//                        icon = Icons.Default.Warning,
//                        color = Color(0xFFF44336)
//                    )
//                }
//
//                Divider()
//
//                // Información adicional
//                Column(
//                    modifier = Modifier.fillMaxWidth(),
//                    verticalArrangement = Arrangement.spacedBy(8.dp)
//                ) {
//                    InfoRow("Zona:", session.zoneName)
//                    InfoRow("Cultivo:", session.cropName)
//
//                    val healthyPercentage = if (session.totalScans > 0) {
//                        (session.healthyCount * 100f / session.totalScans).toInt()
//                    } else 0
//
//                    InfoRow("Salud general:", "$healthyPercentage% sanas")
//                }
//
//                // Mensaje
//                if (session.plagueCount > 0) {
//                    Text(
//                        text = "Se detectaron ${session.plagueCount} plantas con plagas. Los reportes han sido guardados.",
//                        style = MaterialTheme.typography.bodySmall,
//                        color = MaterialTheme.colorScheme.onSurfaceVariant,
//                        textAlign = TextAlign.Center
//                    )
//                } else {
//                    Text(
//                        text = "¡Excelente! Todas las plantas están sanas.",
//                        style = MaterialTheme.typography.bodySmall,
//                        color = Color(0xFF4CAF50),
//                        textAlign = TextAlign.Center,
//                        fontWeight = FontWeight.Medium
//                    )
//                }
//
//                // Botones
//                Row(
//                    modifier = Modifier.fillMaxWidth(),
//                    horizontalArrangement = Arrangement.spacedBy(8.dp)
//                ) {
//                    OutlinedButton(
//                        onClick = onDismiss,
//                        modifier = Modifier.weight(1f)
//                    ) {
//                        Text("Continuar")
//                    }
//
//                    Button(
//                        onClick = onConfirm,
//                        modifier = Modifier.weight(1f),
//                        colors = ButtonDefaults.buttonColors(
//                            containerColor = Color(0xFF4CAF50)
//                        )
//                    ) {
//                        Text("Finalizar")
//                    }
//                }
//            }
//        }
//    }
//}
//
//@Composable
//fun SummaryStatColumn(
//    value: String,
//    label: String,
//    icon: androidx.compose.ui.graphics.vector.ImageVector,
//    color: Color
//) {
//    Column(
//        horizontalAlignment = Alignment.CenterHorizontally,
//        verticalArrangement = Arrangement.spacedBy(4.dp)
//    ) {
//        Icon(
//            imageVector = icon,
//            contentDescription = null,
//            tint = color,
//            modifier = Modifier.size(28.dp)
//        )
//        Text(
//            text = value,
//            style = MaterialTheme.typography.headlineMedium,
//            fontWeight = FontWeight.Bold,
//            color = color
//        )
//        Text(
//            text = label,
//            style = MaterialTheme.typography.bodySmall,
//            color = MaterialTheme.colorScheme.onSurfaceVariant
//        )
//    }
//}
//
//@Composable
//fun InfoRow(label: String, value: String) {
//    Row(
//        modifier = Modifier.fillMaxWidth(),
//        horizontalArrangement = Arrangement.SpaceBetween
//    ) {
//        Text(
//            text = label,
//            style = MaterialTheme.typography.bodyMedium,
//            color = MaterialTheme.colorScheme.onSurfaceVariant
//        )
//        Text(
//            text = value,
//            style = MaterialTheme.typography.bodyMedium,
//            fontWeight = FontWeight.Medium
//        )
//    }
//}