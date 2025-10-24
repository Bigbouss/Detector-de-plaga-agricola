@file:OptIn(ExperimentalMaterial3Api::class)

package com.capstone.cropcare.view.adminViews.zoneManagement

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
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
import androidx.compose.ui.draw.rotate
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
    if (uiState.showAddCropDialog && uiState.selectedZoneForCrop != null) {
        AddCropDialog(
            cropName = uiState.newCropName,
            zoneName = uiState.selectedZoneForCrop!!.name,
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
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(
                    items = uiState.zones,
                    key = { it.id }
                ) { zone ->
                    ExpandableZoneCard(
                        zone = zone,
                        crops = uiState.cropsPerZone[zone.id] ?: emptyList(),
                        isExpanded = uiState.expandedZoneIds.contains(zone.id),
                        showMenu = uiState.selectedZoneForMenu?.id == zone.id,
                        onToggleExpand = { zoneManagementViewModel.toggleZoneExpansion(zone.id) },
                        onShowMenu = { zoneManagementViewModel.showZoneOptionsMenu(zone) },
                        onHideMenu = { zoneManagementViewModel.hideZoneOptionsMenu() },
                        onAddCrop = { zoneManagementViewModel.showAddCropDialog(zone) },
                        onDeleteZone = { zoneManagementViewModel.showDeleteZoneDialog(zone) },
                        onDeleteCrop = { zoneManagementViewModel.showDeleteCropDialog(it) }
                    )
                }
            }
        }
    }
}

@Composable
fun ExpandableZoneCard(
    zone: ZoneModel,
    crops: List<CropModel>,
    isExpanded: Boolean,
    showMenu: Boolean,
    onToggleExpand: () -> Unit,
    onShowMenu: () -> Unit,
    onHideMenu: () -> Unit,
    onAddCrop: () -> Unit,
    onDeleteZone: () -> Unit,
    onDeleteCrop: (CropModel) -> Unit
) {
    val rotationAngle by animateFloatAsState(
        targetValue = if (isExpanded) 180f else 0f,
        label = "rotation"
    )

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            // Header de la zona
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onToggleExpand)
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowDown,
                        contentDescription = if (isExpanded) "Colapsar" else "Expandir",
                        modifier = Modifier
                            .size(24.dp)
                            .rotate(rotationAngle),
                        tint = MaterialTheme.colorScheme.primary
                    )

                    Spacer(Modifier.width(12.dp))

                    Column {
                        Text(
                            text = zone.name,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        zone.description?.let { desc ->
                            Text(
                                text = desc,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        // Contador de cultivos
                        Text(
                            text = "${crops.size} cultivo(s)",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                // Menú de opciones
                Box {
                    IconButton(onClick = onShowMenu) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "Opciones"
                        )
                    }

                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = onHideMenu
                    ) {
                        DropdownMenuItem(
                            text = { Text("Agregar cultivo") },
                            onClick = onAddCrop,
                            leadingIcon = {
                                Icon(
                                    Icons.Default.Add,
                                    contentDescription = null,
                                    tint = Color(0xFF4CAF50)
                                )
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Eliminar zona") },
                            onClick = onDeleteZone,
                            leadingIcon = {
                                Icon(
                                    Icons.Default.Delete,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.error
                                )
                            }
                        )
                    }
                }
            }

            // Lista expandible de cultivos
            AnimatedVisibility(
                visible = isExpanded,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                ) {
                    if (crops.isEmpty()) {
                        // Estado vacío
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "No hay cultivos en esta zona",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    } else {
                        // Lista de cultivos
                        crops.forEach { crop ->
                            CropItem(
                                crop = crop,
                                onDelete = { onDeleteCrop(crop) }
                            )
                            if (crop != crops.last()) {
                                HorizontalDivider(
                                    modifier = Modifier.padding(horizontal = 16.dp),
                                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CropItem(
    crop: CropModel,
    onDelete: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
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
                modifier = Modifier.size(20.dp)
            )
            Spacer(Modifier.width(12.dp))
            Text(
                text = crop.name,
                style = MaterialTheme.typography.bodyMedium
            )
        }

        IconButton(
            onClick = onDelete,
            modifier = Modifier.size(36.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Delete,
                contentDescription = "Eliminar",
                tint = MaterialTheme.colorScheme.error,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}@Composable
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
                text = "Crea tu primera zona de cultivo para comenzar",
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
    zoneName: String,
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
                    text = "Agrega un nuevo cultivo a $zoneName",
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

                Spacer(Modifier.height(12.dp))

                // Sugerencias rápidas
                Text(
                    text = "Sugerencias:",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(8.dp))

                Column(
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        SuggestionChip(
                            onClick = { onCropNameChanged("Papas") },
                            label = { Text("Papas", style = MaterialTheme.typography.labelSmall) },
                            enabled = !isAdding
                        )
                        SuggestionChip(
                            onClick = { onCropNameChanged("Tomates") },
                            label = { Text("Tomates", style = MaterialTheme.typography.labelSmall) },
                            enabled = !isAdding
                        )
                        SuggestionChip(
                            onClick = { onCropNameChanged("Maíz") },
                            label = { Text("Maíz", style = MaterialTheme.typography.labelSmall) },
                            enabled = !isAdding
                        )
                    }
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        SuggestionChip(
                            onClick = { onCropNameChanged("Trigo") },
                            label = { Text("Trigo", style = MaterialTheme.typography.labelSmall) },
                            enabled = !isAdding
                        )
                        SuggestionChip(
                            onClick = { onCropNameChanged("Lechugas") },
                            label = { Text("Lechugas", style = MaterialTheme.typography.labelSmall) },
                            enabled = !isAdding
                        )
                        SuggestionChip(
                            onClick = { onCropNameChanged("Sandías") },
                            label = { Text("Sandías", style = MaterialTheme.typography.labelSmall) },
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