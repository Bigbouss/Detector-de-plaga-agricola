@file:OptIn(ExperimentalMaterial3Api::class)

package com.capstone.cropcare.view.adminViews.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.capstone.cropcare.R
import com.capstone.cropcare.domain.model.WorkerModel
import com.capstone.cropcare.view.core.components.CropCardAdmin
import com.capstone.cropcare.view.core.components.CropCardItemListWorker

@Composable
fun HomeAdminScreen(
    goInvitationCode: () -> Unit,
    goAssignZones: (workerId: Int, workerName: String) -> Unit, // ✅ Int, no String
    viewModel: HomeAdminViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    // Dialog de opciones del worker
    if (uiState.showOptionsMenu && uiState.selectedWorker != null) {
        WorkerOptionsDialog(
            worker = uiState.selectedWorker!!,
            onDismiss = { viewModel.hideOptionsMenu() },
            onDelete = {
                viewModel.hideOptionsMenu()
                viewModel.showDeleteDialog(uiState.selectedWorker!!)
            },

            onAssignZones = {
                val worker = uiState.selectedWorker!!
                viewModel.hideOptionsMenu()
                goAssignZones(worker.id, worker.name) // 👈 Navegar a asignación
            }
        )
    }

    // Dialog de confirmación de eliminación
    if (uiState.showDeleteDialog && uiState.workerToDelete != null) {
        DeleteWorkerDialog(
            workerName = uiState.workerToDelete!!.name,
            isDeleting = uiState.isDeleting,
            onConfirm = { viewModel.deleteWorker() },
            onDismiss = { viewModel.hideDeleteDialog() }
        )
    }

    // Error dialog
    uiState.error?.let { error ->
        AlertDialog(
            onDismissRequest = { viewModel.clearError() },
            title = { Text("Error") },
            text = { Text(error) },
            confirmButton = {
                TextButton(onClick = { viewModel.clearError() }) {
                    Text("OK")
                }
            }
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        CropCardAdmin(
            textTitle = stringResource(R.string.admin_home_screen_card_title),
            iconCard = R.drawable.ic_add_worker,
            iconAction = { goInvitationCode() },
            modifier = Modifier
                .padding(vertical = 20.dp)
                .padding(horizontal = 5.dp)
        ) {
            when {
                uiState.isLoading -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }

                uiState.workers.isEmpty() -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.ic_add_worker),
                                contentDescription = null,
                                modifier = Modifier.size(64.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                            )
                            Spacer(Modifier.height(16.dp))
                            Text(
                                text = "No hay trabajadores vinculados",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(Modifier.height(8.dp))
                            Text(
                                text = "Genera un código de invitación para agregar trabajadores a tu equipo",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }

                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxWidth(),
                        contentPadding = PaddingValues(vertical = 8.dp)
                    ) {
                        items(uiState.workers) { worker ->
                            CropCardItemListWorker(
                                nameWorker = worker.name,
                                emailWorker = worker.email,
                                onOptionsClick = { viewModel.showOptionsMenu(worker) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun WorkerOptionsDialog(
    worker: WorkerModel,
    onDismiss: () -> Unit,
    onDelete: () -> Unit,
    onAssignZones: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column {
                Text(
                    text = "Gestionar trabajador",
                    style = MaterialTheme.typography.titleLarge
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = worker.name,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {

                Spacer(Modifier.height(8.dp))

                // Opción: Asignar zonas
                Surface(
                    onClick = onAssignZones,
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.medium
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.ic_crop_zones),
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(Modifier.width(16.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Asignar zonas de cultivo",
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.SemiBold
                            )
                            // Mostrar cantidad de zonas asignadas
                            Text(
                                text = if (worker.assignedZoneIds.isEmpty())
                                    "Sin zonas asignadas"
                                else
                                    "${worker.assignedZoneIds.size} zona(s) asignada(s)",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                Spacer(Modifier.height(8.dp))

                HorizontalDivider()

                Spacer(Modifier.height(8.dp))

                // Opción: Eliminar
                Surface(
                    onClick = onDelete,
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.medium,
                    color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error
                        )
                        Spacer(Modifier.width(16.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Eliminar trabajador",
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.error
                            )
                            Text(
                                text = "Desvincular de la empresa",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.error.copy(alpha = 0.7f)
                            )
                        }
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
fun DeleteWorkerDialog(
    workerName: String,
    isDeleting: Boolean,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = { if (!isDeleting) onDismiss() },
        icon = {
            Icon(
                imageVector = Icons.Default.Delete,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.error,
                modifier = Modifier.size(32.dp)
            )
        },
        title = {
            Text("¿Eliminar trabajador?")
        },
        text = {
            Text(
                "¿Estás seguro que deseas desvincular a $workerName?\n\n" +
                        "Esta acción eliminará su acceso a la empresa y no podrá ser revertida.",
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
                        color = MaterialTheme.colorScheme.onError,
                        strokeWidth = 2.dp
                    )
                    Spacer(Modifier.width(8.dp))
                }
                Text("Eliminar")
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