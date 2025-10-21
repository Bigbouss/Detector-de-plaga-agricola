@file:OptIn(ExperimentalMaterial3Api::class)

package com.capstone.cropcare.view.adminViews.zoneManagement

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel

import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.capstone.cropcare.domain.model.CropModel
import com.capstone.cropcare.domain.model.ZoneModel

@Composable
fun ZoneManagementScreen(
    zoneManagementViewModel: ZoneManagementViewModel = hiltViewModel(),
    navigateToInvitations: () -> Unit = {}
) {
    val uiState by zoneManagementViewModel.uiState.collectAsStateWithLifecycle()

    // Error dialog
    uiState.error?.let { error ->
        AlertDialog(
            onDismissRequest = { zoneManagementViewModel.clearError() },
            title = { Text("Error") },
            text = { Text(error) },
            confirmButton = {
                TextButton(onClick = { zoneManagementViewModel.clearError() }) {
                    Text("OK")
                }
            }
        )
    }

    // Create zone dialog
    if (uiState.showCreateZoneDialog) {
        CreateZoneDialog(
            zoneName = uiState.newZoneName,
            zoneDescription = uiState.newZoneDescription,
            isCreating = uiState.isCreatingZone,
            onZoneNameChanged = { zoneManagementViewModel.onNewZoneNameChanged(it) },
            onZoneDescriptionChanged = { zoneManagementViewModel.onNewZoneDescriptionChanged(it) },
            onConfirm = { zoneManagementViewModel.createZone() },
            onDismiss = { zoneManagementViewModel.hideCreateZoneDialog() }
        )
    }

    // Delete zone dialog
    if (uiState.showDeleteZoneDialog && uiState.zoneToDelete != null) {
        DeleteConfirmationDialog(
            title = "Eliminar Zona",
            message = "¿Estás seguro de eliminar la zona '${uiState.zoneToDelete!!.name}'?\n\nTodos los cultivos asociados también serán eliminados.",
            isDeleting = uiState.isDeletingZone,
            onConfirm = { zoneManagementViewModel.deleteZone() },
            onDismiss = { zoneManagementViewModel.hideDeleteZoneDialog() }
        )
    }

    // Add crop dialog
    if (uiState.showAddCropDialog) {
        AddCropDialog(
            cropName = uiState.newCropName,
            isAdding = uiState.isAddingCrop,
            onCropNameChanged = { zoneManagementViewModel.onNewCropNameChanged(it) },
            onConfirm = { zoneManagementViewModel.addCropToZone() },
            onDismiss = { zoneManagementViewModel.hideAddCropDialog() }
        )
    }

    // Delete crop dialog
    if (uiState.showDeleteCropDialog && uiState.cropToDelete != null) {
        DeleteConfirmationDialog(
            title = "Eliminar Cultivo",
            message = "¿Estás seguro de eliminar el cultivo '${uiState.cropToDelete!!.name}'?",
            isDeleting = uiState.isDeletingCrop,
            onConfirm = { zoneManagementViewModel.deleteCrop() },
            onDismiss = { zoneManagementViewModel.hideDeleteCropDialog() }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Gestión de Zonas") },
                actions = {
                    IconButton(onClick = navigateToInvitations) {
                        Icon(Icons.Default.Email, "Invitaciones")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { zoneManagementViewModel.showCreateZoneDialog() },
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Default.Add, "Crear zona")
            }
        }
    ) { padding ->
        if (uiState.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else if (uiState.zones.isEmpty()) {
            EmptyZonesState(
                modifier = Modifier.padding(padding),
                onCreateZone = { zoneManagementViewModel.showCreateZoneDialog() }
            )
        } else {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                // Left: Zones list
                ZonesList(
                    zones = uiState.zones,
                    selectedZone = uiState.selectedZone,
                    onZoneSelected = { zoneManagementViewModel.selectZone(it) },
                    onDeleteZone = { zoneManagementViewModel.showDeleteZoneDialog(it) },
                    modifier = Modifier.weight(1f)
                )

                // Right: Crops in selected zone
                if (uiState.selectedZone != null) {
                    CropsList(
                        zone = uiState.selectedZone!!,
                        crops = uiState.cropsInSelectedZone,
                        onAddCrop = { zoneManagementViewModel.showAddCropDialog() },
                        onDeleteCrop = { zoneManagementViewModel.showDeleteCropDialog(it) },
                        modifier = Modifier.weight(1f)
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Selecciona una zona para ver sus cultivos",
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun EmptyZonesState(
    modifier: Modifier = Modifier,
    onCreateZone: () -> Unit
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(32.dp)
        ) {
            Icon(
                imageVector = Icons.Default.LocationOn,
                contentDescription = null,
                modifier = Modifier.size(80.dp),
                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
            )
            Spacer(Modifier.height(16.dp))
            Text(
                text = "No hay zonas creadas",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = "Crea tu primera zona de cultivo",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(24.dp))
            Button(onClick = onCreateZone) {
                Icon(Icons.Default.Add, null)
                Spacer(Modifier.width(8.dp))
                Text("Crear Zona")
            }
        }
    }
}

@Composable
fun ZonesList(
    zones: List<ZoneModel>,
    selectedZone: ZoneModel?,
    onZoneSelected: (ZoneModel) -> Unit,
    onDeleteZone: (ZoneModel) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxHeight()) {
        Surface(
            color = MaterialTheme.colorScheme.primaryContainer,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "Zonas (${zones.size})",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(16.dp)
            )
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(zones) { zone ->
                ZoneCard(
                    zone = zone,
                    isSelected = zone.id == selectedZone?.id,
                    onClick = { onZoneSelected(zone) },
                    onDelete = { onDeleteZone(zone) }
                )
            }
        }
    }
}

@Composable
fun ZoneCard(
    zone: ZoneModel,
    isSelected: Boolean,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected)
                MaterialTheme.colorScheme.primaryContainer
            else
                MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isSelected) 4.dp else 2.dp
        )
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
                    fontWeight = FontWeight.Bold
                )
                zone.description?.let { desc ->
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = desc,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            IconButton(
                onClick = onDelete,
                colors = IconButtonDefaults.iconButtonColors(
                    contentColor = MaterialTheme.colorScheme.error
                )
            ) {
                Icon(Icons.Default.Delete, "Eliminar")
            }
        }
    }
}

@Composable
fun CropsList(
    zone: ZoneModel,
    crops: List<CropModel>,
    onAddCrop: () -> Unit,
    onDeleteCrop: (CropModel) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxHeight()) {
        Surface(
            color = MaterialTheme.colorScheme.secondaryContainer,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Cultivos en ${zone.name}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${crops.size} cultivo(s)",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f)
                    )
                }

                IconButton(onClick = onAddCrop) {
                    Icon(Icons.Default.Add, "Agregar cultivo")
                }
            }
        }

        if (crops.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(32.dp)
                ) {
                    Text(
                        text = "No hay cultivos en esta zona",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(8.dp))
                    TextButton(onClick = onAddCrop) {
                        Icon(Icons.Default.Add, null)
                        Spacer(Modifier.width(4.dp))
                        Text("Agregar Cultivo")
                    }
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(crops) { crop ->
                    CropCard(
                        crop = crop,
                        onDelete = { onDeleteCrop(crop) }
                    )
                }
            }
        }
    }
}

@Composable
fun CropCard(
    crop: CropModel,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Icon(
                    imageVector = Icons.Default.Star,
                    contentDescription = null,
                    tint = Color(0xFF4CAF50),
                    modifier = Modifier.size(24.dp)
                )
                Spacer(Modifier.width(12.dp))
                Text(
                    text = crop.name,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium
                )
            }

            IconButton(
                onClick = onDelete,
                colors = IconButtonDefaults.iconButtonColors(
                    contentColor = MaterialTheme.colorScheme.error
                )
            ) {
                Icon(Icons.Default.Delete, "Eliminar", modifier = Modifier.size(20.dp))
            }
        }
    }
}

// Dialogs continúan en el siguiente mensaje...

@Composable
fun CreateZoneDialog(
    zoneName: String,
    zoneDescription: String,
    isCreating: Boolean,
    onZoneNameChanged: (String) -> Unit,
    onZoneDescriptionChanged: (String) -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = { if (!isCreating) onDismiss() },
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.LocationOn,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(Modifier.width(8.dp))
                Text("Nueva Zona de Cultivo")
            }
        },
        text = {
            Column {
                Text(
                    text = "Crea una nueva zona para organizar tus cultivos",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(Modifier.height(16.dp))

                OutlinedTextField(
                    value = zoneName,
                    onValueChange = onZoneNameChanged,
                    label = { Text("Nombre de la zona *") },
                    placeholder = { Text("Ej: Zona A, Sector Norte") },
                    enabled = !isCreating,
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    isError = zoneName.isNotBlank() && zoneName.length < 3,
                    supportingText = {
                        if (zoneName.isNotBlank() && zoneName.length < 3) {
                            Text(
                                text = "Mínimo 3 caracteres",
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                )

                Spacer(Modifier.height(8.dp))

                OutlinedTextField(
                    value = zoneDescription,
                    onValueChange = onZoneDescriptionChanged,
                    label = { Text("Descripción (opcional)") },
                    placeholder = { Text("Ej: Área cercana al riego principal") },
                    enabled = !isCreating,
                    maxLines = 3,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                enabled = zoneName.length >= 3 && !isCreating
            ) {
                if (isCreating) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                    Spacer(Modifier.width(8.dp))
                    Text("Creando...")
                } else {
                    Text("Crear Zona")
                }
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                enabled = !isCreating
            ) {
                Text("Cancelar")
            }
        }
    )
}

@Composable
fun AddCropDialog(
    cropName: String,
    isAdding: Boolean,
    onCropNameChanged: (String) -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = { if (!isAdding) onDismiss() },
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Star,
                    contentDescription = null,
                    tint = Color(0xFF4CAF50)
                )
                Spacer(Modifier.width(8.dp))
                Text("Agregar Cultivo")
            }
        },
        text = {
            Column {
                Text(
                    text = "Agrega un nuevo tipo de cultivo a esta zona",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(Modifier.height(16.dp))

                OutlinedTextField(
                    value = cropName,
                    onValueChange = onCropNameChanged,
                    label = { Text("Nombre del cultivo *") },
                    placeholder = { Text("Ej: Papas, Tomates, Maíz") },
                    enabled = !isAdding,
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    isError = cropName.isNotBlank() && cropName.length < 3,
                    supportingText = {
                        if (cropName.isNotBlank() && cropName.length < 3) {
                            Text(
                                text = "Mínimo 3 caracteres",
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = null,
                            tint = Color(0xFF4CAF50)
                        )
                    }
                )

                Spacer(Modifier.height(8.dp))

                // Ejemplos comunes
                Text(
                    text = "Cultivos comunes:",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(4.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    listOf("Papas", "Tomates", "Maíz", "Trigo", "Lechugas", "Sandías").forEach { example ->
                        SuggestionChip(
                            onClick = { onCropNameChanged(example) },
                            label = { Text(example, style = MaterialTheme.typography.labelSmall) },
                            enabled = !isAdding
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                enabled = cropName.length >= 3 && !isAdding
            ) {
                if (isAdding) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                    Spacer(Modifier.width(8.dp))
                    Text("Agregando...")
                } else {
                    Text("Agregar")
                }
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                enabled = !isAdding
            ) {
                Text("Cancelar")
            }
        }
    )
}

@Composable
fun DeleteConfirmationDialog(
    title: String,
    message: String,
    isDeleting: Boolean,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = { if (!isDeleting) onDismiss() },
        icon = {
            Icon(
                imageVector = Icons.Default.Warning,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.error,
                modifier = Modifier.size(48.dp)
            )
        },
        title = {
            Text(
                text = title,
                color = MaterialTheme.colorScheme.error
            )
        },
        text = {
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium
            )
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                enabled = !isDeleting,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error
                )
            ) {
                if (isDeleting) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        color = MaterialTheme.colorScheme.onError
                    )
                    Spacer(Modifier.width(8.dp))
                    Text("Eliminando...")
                } else {
                    Icon(Icons.Default.Delete, null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Eliminar")
                }
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                enabled = !isDeleting
            ) {
                Text("Cancelar")
            }
        }
    )
}