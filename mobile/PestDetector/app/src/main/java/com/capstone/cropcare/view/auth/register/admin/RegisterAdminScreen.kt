package com.capstone.cropcare.view.auth.register.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.capstone.cropcare.R
import com.capstone.cropcare.view.core.components.*

@Composable
fun RegisterAdminScreen(
    registerViewModel: RegisterViewModel = hiltViewModel(),
    navigateBack: () -> Unit,
    navigateToHome: () -> Unit
) {
    val uiState by registerViewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(uiState.registerSuccess) {
        if (uiState.registerSuccess) {
            navigateToHome()
        }
    }

    uiState.error?.let { error ->
        AlertDialog(
            onDismissRequest = { registerViewModel.clearError() },
            title = { Text("Error") },
            text = { Text(error) },
            confirmButton = {
                TextButton(onClick = { registerViewModel.clearError() }) {
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
                Spacer(modifier = Modifier.weight(0.8f))

                // TITLE
                Box(
                    modifier = Modifier
                        .height(100.dp)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    CropTextTitle(text = stringResource(R.string.app_name))
                }

                Spacer(modifier = Modifier.weight(0.3f))

                // FORM
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentHeight()
                ) {
                    Column(modifier = Modifier.padding(horizontal = 25.dp)) {
                        CropTextSubSection(
                            text = "Crear Cuenta de Administrador",
                            modifier = Modifier.padding(bottom = 15.dp)
                        )

                        // ORGANIZATION NAME
                        OutlinedTextField(
                            value = uiState.organizationName,
                            onValueChange = { registerViewModel.onOrganizationNameChanged(it) },
                            modifier = Modifier.fillMaxWidth(),
                            label = { CropTextLabels(text = "Nombre de la Empresa") },
                            shape = MaterialTheme.shapes.medium,
                            textStyle = MaterialTheme.typography.bodyLarge
                        )
                        Spacer(Modifier.height(8.dp))

                        // ✅ FIRST NAME
                        OutlinedTextField(
                            value = uiState.firstName,
                            onValueChange = { registerViewModel.onFirstNameChanged(it) },
                            modifier = Modifier.fillMaxWidth(),
                            label = { CropTextLabels(text = "Nombre") },
                            shape = MaterialTheme.shapes.medium,
                            textStyle = MaterialTheme.typography.bodyLarge
                        )
                        Spacer(Modifier.height(8.dp))

                        // ✅ LAST NAME
                        OutlinedTextField(
                            value = uiState.lastName,
                            onValueChange = { registerViewModel.onLastNameChanged(it) },
                            modifier = Modifier.fillMaxWidth(),
                            label = { CropTextLabels(text = "Apellido") },
                            shape = MaterialTheme.shapes.medium,
                            textStyle = MaterialTheme.typography.bodyLarge
                        )
                        Spacer(Modifier.height(8.dp))

                        // EMAIL
                        EmailTextField(
                            email = uiState.email,
                            onEmailChanged = { registerViewModel.onEmailChanged(it) }
                        )
                        Spacer(Modifier.height(8.dp))

                        //PHONE (NO OPCIONAL)
                        OutlinedTextField(
                            value = uiState.phone,
                            onValueChange = { registerViewModel.onPhoneChanged(it) },
                            modifier = Modifier.fillMaxWidth(),
                            label = { CropTextLabels(text = "Teléfono") },
                            shape = MaterialTheme.shapes.medium,
                            textStyle = MaterialTheme.typography.bodyLarge,
                            placeholder = { Text("+56912345678") }
                        )
                        Spacer(Modifier.height(8.dp))

                        //TAX ID (RUT)
                        OutlinedTextField(
                            value = uiState.taxId,
                            onValueChange = { registerViewModel.onTaxIdChanged(it) },
                            modifier = Modifier.fillMaxWidth(),
                            label = { CropTextLabels(text = "RUT de la Empresa") },
                            shape = MaterialTheme.shapes.medium,
                            textStyle = MaterialTheme.typography.bodyLarge,
                            placeholder = { Text("12345678-9") }
                        )
                        Spacer(Modifier.height(8.dp))

                        // PASSWORD
                        PasswordTextField(
                            password = uiState.password,
                            onPasswordChanged = { registerViewModel.onPasswordChanged(it) }
                        )
                        Spacer(Modifier.height(16.dp))

                        // REGISTER BUTTON
                        CropButtonLogReg(
                            enabled = uiState.isRegisterEnabled && !uiState.isLoading,
                            onClick = { registerViewModel.registerAdmin() },
                            text = if (uiState.isLoading) "Creando cuenta..." else "Crear Cuenta"
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

                        // BACK TO LOGIN
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center
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
                }

                Spacer(modifier = Modifier.weight(0.85f))
            }
        }
    }
}