@file:OptIn(ExperimentalMaterial3Api::class)

package com.capstone.cropcare.view.adminViews.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.capstone.cropcare.R
import com.capstone.cropcare.view.core.components.CropCardAdmin
import com.capstone.cropcare.view.core.components.CropCardItemListWorker

@Composable
fun HomeAdminScreen(goInvitationCode: () -> Unit) {

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {

        CropCardAdmin(
            textTitle = stringResource(R.string.admin_home_screen_card_title),
            iconCard = R.drawable.ic_add_worker,
            iconAction = {goInvitationCode()},//aqui va para generar el code de invitation
            modifier = Modifier
                .padding(vertical = 20.dp)
                .padding(horizontal = 5.dp)
        ) {
            CropCardItemListWorker(
                nameWorker = "Test",
                emailWorker = "email.com"
            )
        }


    }
}

//
//@file:OptIn(ExperimentalMaterial3Api::class)
//
//package com.capstone.cropcare.view.adminViews.home
//
//import androidx.compose.foundation.background
//import androidx.compose.foundation.layout.*
//import androidx.compose.foundation.lazy.LazyColumn
//import androidx.compose.foundation.lazy.items
//import androidx.compose.material3.*
//import androidx.compose.runtime.Composable
//import androidx.compose.runtime.getValue
//import androidx.compose.ui.Alignment
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.res.painterResource
//import androidx.compose.ui.res.stringResource
//import androidx.compose.ui.text.style.TextAlign
//import androidx.compose.ui.unit.dp
//import androidx.hilt.navigation.compose.hiltViewModel
//import androidx.lifecycle.compose.collectAsStateWithLifecycle
//import com.capstone.cropcare.R
//import com.capstone.cropcare.view.core.components.CropCardAdmin
//import com.capstone.cropcare.view.core.components.CropCardItemListWorker
//
//@Composable
//fun HomeAdminScreen(
//    goInvitationCode: () -> Unit,
//    viewModel: HomeAdminViewModel = hiltViewModel()
//) {
//    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
//
//    // Dialog de eliminación
//    if (uiState.showDeleteDialog && uiState.workerToDelete != null) {
//        DeleteWorkerDialog(
//            workerName = uiState.workerToDelete!!.name,
//            isDeleting = uiState.isDeleting,
//            onConfirm = { viewModel.deleteWorker() },
//            onDismiss = { viewModel.hideDeleteDialog() }
//        )
//    }
//
//    // Dialog de asignar zonas (placeholder para el futuro)
//    if (uiState.showAssignZonesDialog && uiState.selectedWorker != null) {
//        AssignZonesDialog(
//            workerName = uiState.selectedWorker!!.name,
//            onDismiss = { viewModel.hideAssignZonesDialog() }
//        )
//    }
//
//    // Error dialog
//    uiState.error?.let { error ->
//        AlertDialog(
//            onDismissRequest = { viewModel.clearError() },
//            title = { Text("Error") },
//            text = { Text(error) },
//            confirmButton = {
//                TextButton(onClick = { viewModel.clearError() }) {
//                    Text("OK")
//                }
//            }
//        )
//    }
//
//    Box(
//        modifier = Modifier
//            .fillMaxSize()
//            .background(MaterialTheme.colorScheme.background)
//    ) {
//        CropCardAdmin(
//            textTitle = stringResource(R.string.admin_home_screen_card_title),
//            iconCard = R.drawable.ic_add_worker,
//            iconAction = { goInvitationCode() },
//            modifier = Modifier
//                .padding(vertical = 20.dp)
//                .padding(horizontal = 5.dp)
//        ) {
//            when {
//                uiState.isLoading -> {
//                    Box(
//                        modifier = Modifier
//                            .fillMaxWidth()
//                            .padding(32.dp),
//                        contentAlignment = Alignment.Center
//                    ) {
//                        CircularProgressIndicator()
//                    }
//                }
//
//                uiState.workers.isEmpty() -> {
//                    Box(
//                        modifier = Modifier
//                            .fillMaxWidth()
//                            .padding(32.dp),
//                        contentAlignment = Alignment.Center
//                    ) {
//                        Column(
//                            horizontalAlignment = Alignment.CenterHorizontally
//                        ) {
//                            Text(
//                                text = "No hay trabajadores vinculados",
//                                style = MaterialTheme.typography.titleMedium,
//                                color = MaterialTheme.colorScheme.onSurfaceVariant
//                            )
//                            Spacer(Modifier.height(8.dp))
//                            Text(
//                                text = "Genera un código de invitación para agregar trabajadores",
//                                style = MaterialTheme.typography.bodySmall,
//                                color = MaterialTheme.colorScheme.onSurfaceVariant,
//                                textAlign = TextAlign.Center
//                            )
//                        }
//                    }
//                }
//
//                else -> {
//                    LazyColumn(
//                        modifier = Modifier.fillMaxWidth(),
//                        contentPadding = PaddingValues(vertical = 8.dp)
//                    ) {
//                        items(uiState.workers) { worker ->
//                            CropCardItemListWorker(
//                                nameWorker = worker.name,
//                                emailWorker = worker.email,
//                                onDeleteClick = { viewModel.showDeleteDialog(worker) },
//                                onAssignZonesClick = { viewModel.showAssignZonesDialog(worker) }
//                            )
//                        }
//                    }
//                }
//            }
//        }
//    }
//}
//
//@Composable
//fun DeleteWorkerDialog(
//    workerName: String,
//    isDeleting: Boolean,
//    onConfirm: () -> Unit,
//    onDismiss: () -> Unit
//) {
//    AlertDialog(
//        onDismissRequest = { if (!isDeleting) onDismiss() },
//        icon = {
//            Icon(
//                painter = painterResource(R.drawable.ic_close),
//                contentDescription = null,
//                tint = MaterialTheme.colorScheme.error
//            )
//        },
//        title = {
//            Text("Eliminar trabajador")
//        },
//        text = {
//            Text(
//                "¿Estás seguro que deseas eliminar a $workerName?\n\n" +
//                        "Esta acción eliminará su acceso a la empresa y no podrá ser revertida."
//            )
//        },
//        confirmButton = {
//            Button(
//                onClick = onConfirm,
//                enabled = !isDeleting,
//                colors = ButtonDefaults.buttonColors(
//                    containerColor = MaterialTheme.colorScheme.error
//                )
//            ) {
//                if (isDeleting) {
//                    CircularProgressIndicator(
//                        modifier = Modifier.size(16.dp),
//                        color = MaterialTheme.colorScheme.onError,
//                        strokeWidth = 2.dp
//                    )
//                    Spacer(Modifier.width(8.dp))
//                }
//                Text("Eliminar")
//            }
//        },
//        dismissButton = {
//            TextButton(
//                onClick = onDismiss,
//                enabled = !isDeleting
//            ) {
//                Text("Cancelar")
//            }
//        }
//    )
//}
//
//@Composable
//fun AssignZonesDialog(
//    workerName: String,
//    onDismiss: () -> Unit
//) {
//    AlertDialog(
//        onDismissRequest = onDismiss,
//        title = { Text("Asignar zonas a $workerName") },
//        text = {
//            Text(
//                "Esta funcionalidad estará disponible cuando implementes la gestión de zonas de cultivo.",
//                style = MaterialTheme.typography.bodyMedium
//            )
//        },
//        confirmButton = {
//            TextButton(onClick = onDismiss) {
//                Text("Entendido")
//            }
//        }
//    )
//}
