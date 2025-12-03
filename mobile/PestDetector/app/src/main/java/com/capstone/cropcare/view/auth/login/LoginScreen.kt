package com.capstone.cropcare.view.auth.login


import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.capstone.cropcare.R
import com.capstone.cropcare.domain.model.UserRole
import com.capstone.cropcare.view.core.components.*
import com.capstone.cropcare.view.core.components.CropButtonLogReg
import com.capstone.cropcare.view.core.components.CropTextSubSection
import com.capstone.cropcare.view.core.components.CropTextTitle

@Composable
fun LoginScreen(
    loginViewModel: LoginViewModel = hiltViewModel(),
    navigateToRegisterAdmin: () -> Unit,
    navigateToRegisterWorker: () -> Unit,
    navigateToAdminHome: () -> Unit,
    navigateToWorkerHome: () -> Unit
) {
    val uiState by loginViewModel.uiState.collectAsStateWithLifecycle()

    // Handle navigation
    LaunchedEffect(uiState.loginSuccess, uiState.user) {
        if (uiState.loginSuccess && uiState.user != null) {
            when (uiState.user!!.role) {
                UserRole.ADMIN -> navigateToAdminHome()
                UserRole.WORKER -> navigateToWorkerHome()
            }
        }
    }

    // Show error dialog
    uiState.error?.let { error ->
        AlertDialog(
            onDismissRequest = { loginViewModel.clearError() },
            title = { Text("Error") },
            text = { Text(error) },
            confirmButton = {
                TextButton(onClick = { loginViewModel.clearError() }) {
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
            Column {
                Spacer(modifier = Modifier.weight(1.2f))

                // TITLE APP
                Box(
                    modifier = Modifier
                        .height(100.dp)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    CropTextTitle(text = stringResource(R.string.app_name))
                }

                Spacer(modifier = Modifier.weight(0.5f))

                // FORM LOGIN SECTION
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentHeight()
                ) {
                    Column(modifier = Modifier.padding(horizontal = 25.dp)) {
                        CropTextSubSection(
                            text = "Iniciar Sesión",
                            modifier = Modifier.padding(bottom = 15.dp)
                        )

                        // EMAIL FIELD
                        EmailTextField(
                            email = uiState.email,
                            onEmailChanged = { loginViewModel.onEmailChanged(it) }
                        )
                        Spacer(Modifier.height(8.dp))

                        // PASSWORD FIELD
                        PasswordTextField(
                            password = uiState.password,
                            onPasswordChanged = { loginViewModel.onPasswordChanged(it) }
                        )
                        Spacer(Modifier.height(16.dp))

                        // LOGIN BUTTON
                        CropButtonLogReg(
                            enabled = uiState.isLoginEnabled && !uiState.isLoading,
                            onClick = { loginViewModel.login() },
                            text = if (uiState.isLoading) "Ingresando..." else "Iniciar Sesión"
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

                        Spacer(modifier = Modifier.height(24.dp))

                        // DIVIDER
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            HorizontalDivider(modifier = Modifier.weight(1f))
                            Text(
                                text = "O",
                                modifier = Modifier.padding(horizontal = 16.dp),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            HorizontalDivider(modifier = Modifier.weight(1f))
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        // REGISTER OPTIONS
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "¿No tienes cuenta?",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            Spacer(Modifier.height(8.dp))

                            Text(
                                text = "Crear cuenta de Administrador",
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.SemiBold,
                                textDecoration = TextDecoration.Underline,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.clickable { navigateToRegisterAdmin() }
                            )

                            Spacer(Modifier.height(8.dp))

                            Text(
                                text = "¿Tienes un código de invitación?",
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.SemiBold,
                                fontStyle = FontStyle.Italic,
                                textDecoration = TextDecoration.Underline,
                                color = MaterialTheme.colorScheme.secondary,
                                modifier = Modifier.clickable { navigateToRegisterWorker() }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.weight(0.85f))
            }
        }
    }
}
