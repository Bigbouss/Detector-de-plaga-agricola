//package com.capstone.cropcare.view.workerViews.quickReport
//
//
//import androidx.compose.foundation.Image
//import androidx.compose.foundation.layout.*
//import androidx.compose.foundation.rememberScrollState
//import androidx.compose.foundation.shape.RoundedCornerShape
//import androidx.compose.foundation.verticalScroll
//import androidx.compose.material.icons.Icons
//import androidx.compose.material.icons.filled.ArrowBack
//import androidx.compose.material.icons.filled.CheckCircle
//import androidx.compose.material.icons.filled.Warning
//import androidx.compose.material3.*
//import androidx.compose.runtime.*
//import androidx.compose.ui.Alignment
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.draw.clip
//import androidx.compose.ui.graphics.Color
//import androidx.compose.ui.layout.ContentScale
//import androidx.compose.ui.text.font.FontWeight
//import androidx.compose.ui.unit.dp
//import androidx.hilt.navigation.compose.hiltViewModel
//import androidx.lifecycle.compose.collectAsStateWithLifecycle
//import coil3.compose.rememberAsyncImagePainter
//import com.capstone.cropcare.view.core.components.CropButtonPrimary
//import com.capstone.cropcare.view.core.components.CropTextField
//
//@OptIn(ExperimentalMaterial3Api::class)
//@Composable
//fun QuickReportScreen(
//    sessionId: String,
//    scanResultId: String,
//    viewModel: QuickReportViewModel = hiltViewModel(),
//    onReportSaved: () -> Unit,
//    onBack: () -> Unit
//) {
//    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
//    val scanResult by viewModel.scanResult.collectAsStateWithLifecycle()
//    val session by viewModel.session.collectAsStateWithLifecycle()
//
//    LaunchedEffect(sessionId, scanResultId) {
//        viewModel.loadData(sessionId, scanResultId)
//    }
//
//    var observations by remember { mutableStateOf("") }
//
//    Scaffold(
//        topBar = {
//            TopAppBar(
//                title = { Text("Reporte Rápido de Plaga") },
//                navigationIcon = {
//                    IconButton(onClick = onBack) {
//                        Icon(Icons.Default.ArrowBack, "Volver")
//                    }
//                }
//            )
//        }
//    ) { padding ->
//        Column(
//            modifier = Modifier
//                .fillMaxSize()
//                .padding(padding)
//                .verticalScroll(rememberScrollState())
//                .padding(16.dp),
//            verticalArrangement = Arrangement.spacedBy(16.dp)
//        ) {
//            // Alerta de plaga detectada
//            Card(
//                colors = CardDefaults.cardColors(
//                    containerColor = Color(0xFFFFEBEE)
//                )
//            ) {
//                Row(
//                    modifier = Modifier
//                        .fillMaxWidth()
//                        .padding(16.dp),
//                    verticalAlignment = Alignment.CenterVertically,
//                    horizontalArrangement = Arrangement.spacedBy(12.dp)
//                ) {
//                    Icon(
//                        imageVector = Icons.Default.Warning,
//                        contentDescription = null,
//                        tint = Color(0xFFF44336),
//                        modifier = Modifier.size(40.dp)
//                    )
//                    Column {
//                        Text(
//                            text = "Plaga Detectada",
//                            style = MaterialTheme.typography.titleMedium,
//                            fontWeight = FontWeight.Bold,
//                            color = Color(0xFFC62828)
//                        )
//                        scanResult?.let {
//                            Text(
//                                text = it.classification,
//                                style = MaterialTheme.typography.bodyMedium
//                            )
//                            Text(
//                                text = "Confianza: ${(it.confidence * 100).toInt()}%",
//                                style = MaterialTheme.typography.bodySmall
//                            )
//                        }
//                    }
//                }
//            }
//
//            // Información del contexto
//            session?.let { sess ->
//                OutlinedCard {
//                    Column(
//                        modifier = Modifier.padding(16.dp),
//                        verticalArrangement = Arrangement.spacedBy(8.dp)
//                    ) {
//                        InfoRow("Zona:", sess.zoneName)
//                        InfoRow("Cultivo:", sess.cropName)
//                        InfoRow("Worker:", sess.workerName)
//                    }
//                }
//            }
//
//            // Foto capturada
//            scanResult?.photoPath?.let { photoPath ->
//                Text(
//                    text = "Foto de la planta",
//                    style = MaterialTheme.typography.titleMedium,
//                    fontWeight = FontWeight.Bold
//                )
//
//                Image(
//                    painter = rememberAsyncImagePainter(photoPath),
//                    contentDescription = "Foto de la planta con plaga",
//                    modifier = Modifier
//                        .fillMaxWidth()
//                        .height(250.dp)
//                        .clip(RoundedCornerShape(12.dp)),
//                    contentScale = ContentScale.Crop
//                )
//            }
//
//            // Campo de observaciones
//            Text(
//                text = "Observaciones adicionales",
//                style = MaterialTheme.typography.titleMedium,
//                fontWeight = FontWeight.Bold
//            )
//
//            CropTextField(
//                value = observations,
//                onValueChange = { observations = it },
//                label = "Describe lo que observas (opcional)",
//                minLines = 4,
//                placeholder = "Ej: Manchas marrones en hojas superiores, algunas hojas caídas..."
//            )
//
//            Text(
//                text = "Tip: Agrega detalles sobre la severidad, ubicación en la planta, etc.",
//                style = MaterialTheme.typography.bodySmall,
//                color = MaterialTheme.colorScheme.onSurfaceVariant
//            )
//
//            Spacer(Modifier.height(16.dp))
//
//            // Botones de acción
//            Column(
//                verticalArrangement = Arrangement.spacedBy(12.dp)
//            ) {
//                CropButtonPrimary(
//                    modifier = Modifier.fillMaxWidth(),
//                    text = if (uiState.isSaving) "Guardando..." else "Guardar Reporte",
//                    enabled = !uiState.isSaving,
//                    onClick = {
//                        viewModel.saveReport(observations) {
//                            onReportSaved()
//                        }
//                    }
//                )
//
//                OutlinedButton(
//                    onClick = onBack,
//                    modifier = Modifier.fillMaxWidth(),
//                    enabled = !uiState.isSaving
//                ) {
//                    Text("Cancelar")
//                }
//            }
//
//            // Mensaje de error
//            uiState.error?.let { error ->
//                Card(
//                    colors = CardDefaults.cardColors(
//                        containerColor = MaterialTheme.colorScheme.errorContainer
//                    )
//                ) {
//                    Text(
//                        text = error,
//                        modifier = Modifier.padding(16.dp),
//                        color = MaterialTheme.colorScheme.onErrorContainer
//                    )
//                }
//            }
//        }
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