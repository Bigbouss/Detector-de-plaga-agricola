package com.capstone.cropcare.view.adminViews.assignZones


import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.capstone.cropcare.domain.model.ZoneModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AssignZonesScreen(
    workerName: String,
    workerId: String,
    navigateBack: () -> Unit,
    viewModel: AssignZonesViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(workerId) {
        viewModel.loadData(workerId)
    }

    // Navegar de vuelta si se guardó exitosamente
    LaunchedEffect(uiState.saveSuccess) {
        if (uiState.saveSuccess) {
            navigateBack()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Asignar zonas")
                        Text(
                            text = workerName,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = navigateBack) {
                        Icon(Icons.Default.ArrowBack, "Volver")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        bottomBar = {
            Surface(
                shadowElevation = 8.dp,
                tonalElevation = 0.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = navigateBack,
                        modifier = Modifier.weight(1f),
                        enabled = !uiState.isSaving
                    ) {
                        Text("Cancelar")
                    }
                    Button(
                        onClick = { viewModel.saveAssignments(workerId) },
                        modifier = Modifier.weight(1f),
                        enabled = !uiState.isSaving && uiState.hasChanges
                    ) {
                        if (uiState.isSaving) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                color = MaterialTheme.colorScheme.onPrimary,
                                strokeWidth = 2.dp
                            )
                            Spacer(Modifier.width(8.dp))
                        }
                        Text("Guardar")
                    }
                }
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when {
                uiState.isLoading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center)
                    )
                }

                uiState.error != null -> {
                    Column(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Error: ${uiState.error}",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.error
                        )
                        Spacer(Modifier.height(16.dp))
                        Button(onClick = { viewModel.loadData(workerId) }) {
                            Text("Reintentar")
                        }
                    }
                }

                uiState.allZones.isEmpty() -> {
                    Text(
                        text = "No hay zonas disponibles.\nCrea zonas primero para asignarlas.",
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(32.dp),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        item {
                            Text(
                                text = "Selecciona las zonas a las que tendrá acceso",
                                style = MaterialTheme.typography.titleMedium,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                            Text(
                                text = "${uiState.selectedZoneIds.size} de ${uiState.allZones.size} zonas seleccionadas",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(bottom = 16.dp)
                            )
                        }

                        items(uiState.allZones) { zone ->
                            ZoneCheckboxItem(
                                zone = zone,
                                isSelected = uiState.selectedZoneIds.contains(zone.id),
                                onToggle = { viewModel.toggleZoneSelection(zone.id) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ZoneCheckboxItem(
    zone: ZoneModel,
    isSelected: Boolean,
    onToggle: () -> Unit
) {
    Surface(
        onClick = onToggle,
        shape = MaterialTheme.shapes.medium,
        color = if (isSelected)
            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
        else
            MaterialTheme.colorScheme.surface,
        border = if (isSelected)
            null
        else
            androidx.compose.foundation.BorderStroke(
                1.dp,
                MaterialTheme.colorScheme.outlineVariant
            ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = zone.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                )
                zone.description?.let { desc ->
                    Text(
                        text = desc,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }

            Checkbox(
                checked = isSelected,
                onCheckedChange = { onToggle() }
            )
        }
    }
}