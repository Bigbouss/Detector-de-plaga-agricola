package com.capstone.cropcare.view.auth.register.worker

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.capstone.cropcare.R
import com.capstone.cropcare.view.core.components.*

@Composable
fun RegisterWorkerScreen(
    registerWorkerViewModel: RegisterWorkerViewModel = hiltViewModel(),
    navigateBack: () -> Unit,
    navigateToHome: () -> Unit
) {
    val uiState by registerWorkerViewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(uiState.registerSuccess) {
        if (uiState.registerSuccess) {
            navigateToHome()
        }
    }

    // Error dialog
    uiState.error?.let { error ->
        AlertDialog(
            onDismissRequest = { registerWorkerViewModel.clearError() },
            title = { Text("Error") },
            text = { Text(error) },
            confirmButton = {
                TextButton(onClick = { registerWorkerViewModel.clearError() }) {
                    Text("OK")
                }
            }
        )
    }

    Scaffold { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(padding)
        ) {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                Spacer(modifier = Modifier.weight(0.5f))

                // TITLE
                Box(
                    modifier = Modifier
                        .height(80.dp)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    CropTextTitle(text = stringResource(R.string.app_name))
                }

                Spacer(modifier = Modifier.weight(0.3f))

                // FORM
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 25.dp)
                ) {
                    CropTextSubSection(
                        text = "Unirme a un Equipo",
                        modifier = Modifier.padding(bottom = 15.dp)
                    )

                    // ============ INVITATION CODE SECTION ============
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Text(
                                text = "Código de Invitación",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )

                            Spacer(Modifier.height(8.dp))

                            OutlinedTextField(
                                value = uiState.invitationCode,
                                onValueChange = { registerWorkerViewModel.onInvitationCodeChanged(it) },
                                modifier = Modifier.fillMaxWidth(),
                                label = { Text("Código") },
                                placeholder = { Text("ABC123XYZ") },
                                shape = MaterialTheme.shapes.medium,
                                textStyle = MaterialTheme.typography.bodyLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = MaterialTheme.typography.bodyLarge.letterSpacing * 1.5
                                ),
                                singleLine = true,
                                isError = uiState.codeError != null,
                                supportingText = {
                                    if (uiState.codeError != null) {
                                        Text(
                                            text = uiState.codeError!!,
                                            color = MaterialTheme.colorScheme.error
                                        )
                                    }
                                }
                            )

                            Spacer(Modifier.height(8.dp))

                            Button(
                                onClick = { registerWorkerViewModel.validateInvitationCode() },
                                modifier = Modifier.fillMaxWidth(),
                                enabled = uiState.invitationCode.length >= 6 && !uiState.isValidatingCode
                            ) {
                                if (uiState.isValidatingCode) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(20.dp),
                                        color = MaterialTheme.colorScheme.onPrimary
                                    )
                                    Spacer(Modifier.width(8.dp))
                                    Text("Validando...")
                                } else {
                                    Text("Validar Código")
                                }
                            }

                            // Organization name display
                            AnimatedVisibility(visible = uiState.isCodeValid && uiState.organizationName != null) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = 12.dp)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            painter = painterResource(R.drawable.ic_check),
                                            contentDescription = null,
                                            tint = Color(0xFF4CAF50),
                                            modifier = Modifier.size(24.dp)
                                        )
                                        Spacer(Modifier.width(8.dp))
                                        Text(
                                            text = "Código válido",
                                            style = MaterialTheme.typography.labelLarge,
                                            color = Color(0xFF4CAF50),
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                    Spacer(Modifier.height(4.dp))
                                    Text(
                                        text = "Te unirás a: ${uiState.organizationName}",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }

                    Spacer(Modifier.height(20.dp))

                    // ============ PERSONAL INFO SECTION (only visible if code is valid) ============
                    AnimatedVisibility(visible = uiState.isCodeValid) {
                        Column {
                            HorizontalDivider(
                                modifier = Modifier.padding(vertical = 8.dp),
                                color = MaterialTheme.colorScheme.outlineVariant
                            )

                            Text(
                                text = "Completa tu información",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(vertical = 8.dp)
                            )

                            // NAME
                            OutlinedTextField(
                                value = uiState.name,
                                onValueChange = { registerWorkerViewModel.onNameChanged(it) },
                                modifier = Modifier.fillMaxWidth(),
                                label = { CropTextLabels(text = "Nombre Completo") },
                                shape = MaterialTheme.shapes.medium,
                                textStyle = MaterialTheme.typography.bodyLarge
                            )
                            Spacer(Modifier.height(8.dp))

                            // PHONE
                            OutlinedTextField(
                                value = uiState.phoneNumber,
                                onValueChange = { registerWorkerViewModel.onPhoneChanged(it) },
                                modifier = Modifier.fillMaxWidth(),
                                label = { CropTextLabels(text = "Número de Teléfono") },
                                shape = MaterialTheme.shapes.medium,
                                textStyle = MaterialTheme.typography.bodyLarge,
                                keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Phone)
                            )
                            Spacer(Modifier.height(8.dp))


                            // EMAIL
                            EmailTextField(
                                email = uiState.email,
                                onEmailChanged = { registerWorkerViewModel.onEmailChanged(it) }
                            )
                            Spacer(Modifier.height(8.dp))

                            // PASSWORD
                            PasswordTextField(
                                password = uiState.password,
                                onPasswordChanged = { registerWorkerViewModel.onPasswordChanged(it) }
                            )

                            // Password requirements
                            Text(
                                text = "La contraseña debe tener al menos 8 caracteres, una mayúscula y un número",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(top = 4.dp, start = 4.dp)
                            )

                            Spacer(Modifier.height(16.dp))

                            // REGISTER BUTTON
                            CropButtonLogReg(
                                enabled = uiState.isRegisterEnabled && !uiState.isLoading,
                                onClick = { registerWorkerViewModel.registerWorker() },
                                text = if (uiState.isLoading) "Registrando..." else "Crear Cuenta"
                            )

                            if (uiState.isLoading) {
                                Box(
                                    modifier = Modifier.fillMaxWidth(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    CircularProgressIndicator(
                                        modifier = Modifier
                                            .padding(top = 16.dp)
                                            .size(24.dp)
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // BACK TO LOGIN
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CropTextBody(text = "¿Ya tienes cuenta? ")
                        Text(
                            text = "Iniciar Sesión",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.SemiBold,
                            textDecoration = TextDecoration.Underline,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.clickable { navigateBack() }
                        )
                    }
                }

                Spacer(modifier = Modifier.weight(1f))
            }
        }
    }
}