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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.capstone.cropcare.R

import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.capstone.cropcare.domain.model.InvitationModel
import kotlinx.coroutines.delay
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

    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("Activos", "Usados")

    // Filtrar invitaciones según tab
    val filteredInvitations = remember(uiState.invitations, selectedTab) {
        val now = System.currentTimeMillis()
        when (selectedTab) {
            0 -> uiState.invitations.filter { !it.isUsed && it.expiresAt > now } // Activos
            1 -> uiState.invitations.filter { it.isUsed || it.expiresAt <= now } // Usados/Expirados
            else -> uiState.invitations
        }
    }

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
            if (selectedTab == 0) { // Solo mostrar FAB en tab "Activos"
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
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Tabs
            TabRow(selectedTabIndex = selectedTab) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = {
                            Text(
                                text = title,
                                style = MaterialTheme.typography.titleMedium
                            )
                        }
                    )
                }
            }

            // Contenido según loading/empty/list
            if (uiState.isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else if (filteredInvitations.isEmpty()) {
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
                            text = if (selectedTab == 0)
                                "No hay códigos activos"
                            else
                                "No hay códigos usados",
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            text = if (selectedTab == 0)
                                "Genera un código para invitar trabajadores"
                            else
                                "Los códigos usados aparecerán aquí",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
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
                    items(filteredInvitations) { invitation ->
                        InvitationCard(
                            invitation = invitation,
                            showUsedBy = selectedTab == 1, // Mostrar quién lo usó en tab "Usados"
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
    showUsedBy: Boolean = false,
    onCopyCode: () -> Unit
) {
    // Estado para tiempo restante
    var remainingTime by remember { mutableStateOf(invitation.expiresAt - System.currentTimeMillis()) }

    LaunchedEffect(invitation.expiresAt, invitation.isUsed) {
        if (!invitation.isUsed) {
            while (remainingTime > 0) {
                remainingTime = invitation.expiresAt - System.currentTimeMillis()
                delay(1000)
            }
        }
    }

    val isExpired = remainingTime <= 0 || invitation.isUsed
    val hours = (remainingTime / (1000 * 60 * 60)).toInt()
    val minutes = ((remainingTime / (1000 * 60)) % 60).toInt()
    val seconds = ((remainingTime / 1000) % 60).toInt()
    val daysUntilExpiration = (remainingTime / (1000 * 60 * 60 * 24)).toInt()

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = when {
                invitation.isUsed -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                isExpired -> MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)
                else -> MaterialTheme.colorScheme.surface
            }
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
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
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        painter = when {
                            invitation.isUsed -> painterResource(R.drawable.ic_check)
                            isExpired -> painterResource(R.drawable.ic_close)
                            else -> painterResource(R.drawable.ic_access_time)
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

            Spacer(Modifier.height(12.dp))

            // Fecha de creación
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    painter = painterResource(R.drawable.ic_calendar),
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.width(4.dp))
                Text(
                    text = "Creado: ${invitation.createdAt.toDateString()}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(Modifier.height(4.dp))

            // Fecha / tiempo restante
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    painter = painterResource(R.drawable.ic_schedule),
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = when {
                        invitation.isUsed || isExpired -> MaterialTheme.colorScheme.error  // ← CAMBIO
                        remainingTime <= 24 * 60 * 60 * 1000 -> Color(0xFFFF9800)
                        else -> MaterialTheme.colorScheme.onSurfaceVariant
                    }
                )
                Spacer(Modifier.width(4.dp))

                Text(
                    text = when {
                        invitation.isUsed -> "Expirado"  // ← CAMBIO: mostrar "Expirado" si está usado
                        isExpired -> "Expiró: ${invitation.expiresAt.toDateString()}"
                        daysUntilExpiration > 0 -> "Expira en ${daysUntilExpiration} día(s)"
                        else -> "⏰ Expira en %02dh:%02dm:%02ds".format(hours, minutes, seconds)
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = when {
                        invitation.isUsed || isExpired -> MaterialTheme.colorScheme.error  // ← CAMBIO
                        remainingTime <= 24 * 60 * 60 * 1000 -> Color(0xFFFF9800)
                        else -> MaterialTheme.colorScheme.onSurfaceVariant
                    },
                    fontWeight = if ((remainingTime <= 24 * 60 * 60 * 1000 && !isExpired) || invitation.isUsed)  // ← CAMBIO
                        FontWeight.Bold else FontWeight.Normal
                )
            }

            if (showUsedBy) {
                Spacer(Modifier.height(12.dp))
                HorizontalDivider()
                Spacer(Modifier.height(12.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        painter = painterResource(R.drawable.ic_person),
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                        tint = if (invitation.isUsed) Color(0xFF4CAF50) else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.width(8.dp))
                    Column {
                        Text(
                            text = if (invitation.isUsed) "Usado por:" else "No utilizado",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        if (invitation.isUsed) {  // ← Solo mostrar email si está usado
                            Spacer(Modifier.height(2.dp))
                            Text(
                                text = invitation.usedBy ?: "Usuario desconocido",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }

            // Botón copiar (solo para activos)
            if (!invitation.isUsed && !isExpired) {
                Spacer(Modifier.height(12.dp))
                OutlinedButton(
                    onClick = onCopyCode,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_content_copy),
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.width(8.dp))
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
                "Código Generado",
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
