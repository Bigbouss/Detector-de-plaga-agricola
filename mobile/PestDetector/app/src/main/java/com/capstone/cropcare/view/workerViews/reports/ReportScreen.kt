@file:OptIn(ExperimentalMaterial3Api::class)

package com.capstone.cropcare.view.workerViews.reports

import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.capstone.cropcare.R
import com.capstone.cropcare.view.core.components.CropButtonPrimary
import com.capstone.cropcare.view.core.components.CropDropdown
import com.capstone.cropcare.view.core.components.CropTextField
import com.capstone.cropcare.view.core.components.CropTextFieldObservation
import com.capstone.cropcare.view.core.components.CropTopAppBar
import com.capstone.cropcare.view.workerViews.analysisResult.AnalysisViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File

@Composable
fun ReportScreenWorker(
    reportViewModel: ReportViewModel = hiltViewModel(),
    analysisViewModel: AnalysisViewModel,
    backToHome: () -> Unit
) {
    val state by reportViewModel.state.collectAsState()
    val availableZones by reportViewModel.availableZones.collectAsState()
    val availableCrops by reportViewModel.availableCrops.collectAsState()
    val isLoading by reportViewModel.isLoading.collectAsState()
    val errorMessage by reportViewModel.errorMessage.collectAsState()

    val tempBitmap by analysisViewModel.tempBitmap.collectAsState()
    val savedPath by analysisViewModel.savedImagePath.collectAsState()

    val focusManager = LocalFocusManager.current
    val bringIntoViewRequester = remember { BringIntoViewRequester() }
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    // Mostrar errores en Snackbar
    LaunchedEffect(errorMessage) {
        errorMessage?.let { message ->
            snackbarHostState.showSnackbar(message)
            reportViewModel.clearError()
        }
    }

    // Inicializa datos solo una vez
    LaunchedEffect(tempBitmap, savedPath) {
        tempBitmap?.let {
            if (state.analizedPhoto == null) {
                reportViewModel.setAnalizedPhoto(it)
            }
        }
        savedPath?.let {
            if (state.localPhotoPath == null) {
                reportViewModel.setLocalPhotoPath(it)
            }
        }
        val info = analysisViewModel.diseaseInfo.value

        val diagnosticText = when {
            info == null -> "Sin información"
            info.isHealthy -> "Planta saludable"
            else -> info.diseaseName  // 🧠 Nombre real de la enfermedad
        }

        reportViewModel.setDiagnostic(diagnosticText)

    }

    Scaffold(
        topBar = { CropTopAppBar() },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        modifier = Modifier
            .fillMaxSize()
            .imePadding()
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize()) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .background(MaterialTheme.colorScheme.background),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Título
                item {
                    Text(
                        text = stringResource(R.string.report_card_title),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }

                // Worker name
                item {
                    CropTextField(
                        text = state.workerName,
                        onValueChange = {},
                        label = stringResource(R.string.report_card_label_name),
                        enabled = false
                    )
                }

                // Diagnostic
                item {
                    CropTextField(
                        text = state.diagnostic,
                        onValueChange = {},
                        label = stringResource(R.string.report_card_label_diagnostic),
                        enabled = false
                    )
                }

                // ZONA
                item {
                    Column {
                        CropDropdown(
                            selected = state.selectedZone?.name ?: "",
                            options = availableZones.map { it.name },
                            label = "Zona de cultivo",
                            enabled = !isLoading && availableZones.isNotEmpty(),
                            onSelect = { zoneName ->
                                val zone = availableZones.find { it.name == zoneName }
                                zone?.let { reportViewModel.selectZone(it) }
                            }
                        )

                        when {
                            isLoading -> {
                                Text(
                                    text = "Cargando zonas asignadas...",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(start = 16.dp, top = 4.dp)
                                )
                            }
                            availableZones.isEmpty() -> {
                                Text(
                                    text = "No tienes zonas asignadas. Contacta al administrador.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.padding(start = 16.dp, top = 4.dp)
                                )
                            }
                        }
                    }
                }

                // CULTIVO
                item {
                    Column {
                        CropDropdown(
                            selected = state.selectedCrop?.name ?: "",
                            options = availableCrops.map { it.name },
                            label = "Tipo de cultivo",
                            enabled = state.selectedZone != null && !isLoading,
                            onSelect = { cropName ->
                                val crop = availableCrops.find { it.name == cropName }
                                crop?.let { reportViewModel.selectCrop(it) }
                            }
                        )

                        if (state.selectedZone == null) {
                            Text(
                                text = "Selecciona una zona primero",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.error,
                                modifier = Modifier.padding(start = 16.dp, top = 4.dp)
                            )
                        } else if (availableCrops.isEmpty() && !isLoading) {
                            Text(
                                text = "No hay cultivos en esta zona",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(start = 16.dp, top = 4.dp)
                            )
                        }
                    }
                }

                // Photo preview
                item {
                    val imageModel = state.analizedPhoto ?: state.localPhotoPath?.let { File(it) }

                    if (imageModel != null) {
                        AsyncImage(
                            model = ImageRequest.Builder(LocalContext.current)
                                .data(imageModel)
                                .crossfade(true)
                                .build(),
                            contentDescription = "Foto analizada",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp)
                                .clip(MaterialTheme.shapes.medium)
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp)
                                .clip(MaterialTheme.shapes.medium)
                                .background(MaterialTheme.colorScheme.surfaceVariant),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    painter = painterResource(R.drawable.ic_no_photo),
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(48.dp)
                                )
                                Spacer(Modifier.height(8.dp))
                                Text(
                                    "No hay foto seleccionada",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }

                // Observation
                item {
                    val maxChars = 200
                    val interactionSource = remember { MutableInteractionSource() }
                    val isFocused by interactionSource.collectIsFocusedAsState()

                    Column {
                        CropTextFieldObservation(
                            text = state.observation,
                            onValueChange = { newText ->
                                val filtered = newText
                                    .replace("\n", " ")
                                    .replace("\r", " ")
                                    .take(maxChars)
                                reportViewModel.setObservation(filtered)
                            },
                            label = stringResource(R.string.report_card_label_observation),
                            placeholder = stringResource(R.string.report_card_placeholder_observation),
                            singleLine = false,
                            maxLines = 4,
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 100.dp, max = 120.dp)
                                .bringIntoViewRequester(bringIntoViewRequester),
                            interactionSource = interactionSource,
                            keyboardOptions = KeyboardOptions(
                                capitalization = KeyboardCapitalization.Sentences,
                                imeAction = ImeAction.Done,
                                keyboardType = KeyboardType.Text
                            ),
                            keyboardActions = KeyboardActions(
                                onDone = { focusManager.clearFocus() }
                            )
                        )

                        Text(
                            text = "${state.observation.length} / $maxChars",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (state.observation.length >= maxChars)
                                MaterialTheme.colorScheme.error
                            else
                                MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier
                                .align(Alignment.End)
                                .padding(top = 4.dp)
                        )
                    }

                    LaunchedEffect(isFocused) {
                        if (isFocused) {
                            delay(300)
                            bringIntoViewRequester.bringIntoView()
                        }
                    }
                }

                // Save button
                item {
                    CropButtonPrimary(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 16.dp, bottom = 32.dp),
                        text = stringResource(R.string.report_card_button),
                        enabled = state.selectedZone != null &&
                                state.selectedCrop != null &&
                                !isLoading,
                        onClick = {
                            focusManager.clearFocus()
                            reportViewModel.saveReport()
                            analysisViewModel.clearTemporaryImage()
                            analysisViewModel.reset()
                            backToHome()
                        }
                    )
                }
            }

            // Loading overlay
            if (isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.background.copy(alpha = 0.7f)),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
        }
    }
}