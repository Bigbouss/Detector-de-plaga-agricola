package com.capstone.cropcare.view.adminViews.invitationManagement

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel

import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.capstone.cropcare.domain.model.InvitationModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InvitationManagementScreen(
    invitationViewModel: InvitationViewModel = hiltViewModel(),
    navigateBack: () -> Unit
) {
    val uiState by invitationViewModel.uiState.collectAsStateWithLifecycle()
    val clipboardManager = LocalClipboardManager.current

    // Dialog con código generado
    if (uiState.showGeneratedDialog && uiState.generatedInvitation != null) {
        GeneratedInvitationDialog(
            invitation = uiState.generatedInvitation!!,
            onDismiss = { invitationViewModel.dismissGeneratedDialog() },
            onCopyCode = {
                clipboardManager.setText(AnnotatedString(uiState.generatedInvitation!!.code))
            }
        )
    }

    // Error dialog
    uiState.error?.let { error ->
        AlertDialog(
            onDismissRequest = { invitationViewModel.clearError() },
            title = { Text("Error") },
            text = { Text(error) },
            confirmButton = {
                TextButton(onClick = { invitationViewModel.clearError() }) {
                    Text("OK")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Códigos de Invitación") },
                navigationIcon = {
                    IconButton(onClick = navigateBack) {
                        Icon(Icons.Default.Close, "Volver")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { invitationViewModel.generateInvitation() },
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                if (uiState.isGenerating) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Icon(Icons.Default.Add, "Generar código")
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            if (uiState.isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else if (uiState.invitations.isEmpty()) {
                // Estado vacío
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(32.dp)
                    ) {
                        Text(
                            text = "No hay códigos de invitación",
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            text = "Genera un código para invitar trabajadores a tu equipo",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                // Lista de invitaciones
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(uiState.invitations) { invitation ->
                        InvitationCard(
                            invitation = invitation,
                            onCopyCode = {
                                clipboardManager.setText(AnnotatedString(invitation.code))
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun InvitationCard(
    invitation: InvitationModel,
    onCopyCode: () -> Unit
) {
    val now = System.currentTimeMillis()
    val isExpired = now > invitation.expiresAt
    val daysUntilExpiration = ((invitation.expiresAt - now) / (1000 * 60 * 60 * 24)).toInt()

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = when {
                invitation.isUsed -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                isExpired -> MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)
                else -> MaterialTheme.colorScheme.surface
            }
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Código
                Text(
                    text = invitation.code,
                    style = MaterialTheme.typography.headlineMedium,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    color = when {
                        invitation.isUsed -> MaterialTheme.colorScheme.onSurfaceVariant
                        isExpired -> MaterialTheme.colorScheme.error.copy(alpha = 0.6f)
                        else -> MaterialTheme.colorScheme.primary
                    }
                )

                // Estado
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = when {
                            invitation.isUsed -> Icons.Default.Check
                            isExpired -> Icons.Default.Close
                            else -> Icons.Default.Check
                        },
                        contentDescription = null,
                        tint = when {
                            invitation.isUsed -> Color(0xFF4CAF50)
                            isExpired -> MaterialTheme.colorScheme.error
                            else -> MaterialTheme.colorScheme.primary
                        },
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        text = when {
                            invitation.isUsed -> "Usado"
                            isExpired -> "Expirado"
                            else -> "Activo"
                        },
                        style = MaterialTheme.typography.labelMedium,
                        color = when {
                            invitation.isUsed -> Color(0xFF4CAF50)
                            isExpired -> MaterialTheme.colorScheme.error
                            else -> MaterialTheme.colorScheme.primary
                        },
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(Modifier.height(8.dp))

            // Fecha de creación
            Text(
                text = "Creado: ${invitation.createdAt.toDateString()}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            // Fecha de expiración
            Text(
                text = when {
                    isExpired -> "Expiró: ${invitation.expiresAt.toDateString()}"
                    daysUntilExpiration == 0 -> "⏰ Expira hoy"
                    daysUntilExpiration == 1 -> "⏰ Expira mañana"
                    daysUntilExpiration < 7 -> "Expira en $daysUntilExpiration días"
                    else -> "Válido hasta: ${invitation.expiresAt.toDateString()}"
                },
                style = MaterialTheme.typography.bodySmall,
                color = when {
                    isExpired -> MaterialTheme.colorScheme.error
                    daysUntilExpiration <= 2 -> Color(0xFFFF9800) // Naranja
                    else -> MaterialTheme.colorScheme.onSurfaceVariant
                },
                fontWeight = if (daysUntilExpiration <= 2 && !isExpired) FontWeight.Bold else FontWeight.Normal
            )

            if (!invitation.isUsed && !isExpired) {
                Spacer(Modifier.height(12.dp))

                // Botón copiar
                OutlinedButton(
                    onClick = onCopyCode,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Copiar Código")
                }
            }
        }
    }
}

@Composable
fun GeneratedInvitationDialog(
    invitation: InvitationModel,
    onDismiss: () -> Unit,
    onCopyCode: () -> Unit
) {
    var copied by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                "✅ Código Generado",
                style = MaterialTheme.typography.titleLarge
            )
        },
        text = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Comparte este código con el nuevo trabajador:",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(Modifier.height(16.dp))

                // Código grande y destacado
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Text(
                        text = invitation.code,
                        style = MaterialTheme.typography.displayMedium,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(24.dp)
                    )
                }

                if (copied) {
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = "✓ Código copiado",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF4CAF50),
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onCopyCode()
                    copied = true
                }
            ) {
                Text(if (copied) "Copiado ✓" else "Copiar Código")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cerrar")
            }
        }
    )
}

// Extension function
fun Long.toDateString(): String {
    val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
    return sdf.format(Date(this))
}