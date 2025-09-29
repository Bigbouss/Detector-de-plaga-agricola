package com.capstone.cropcare.view.auth.login


import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.capstone.cropcare.R
import com.capstone.cropcare.view.core.components.CropButtonLogReg
import com.capstone.cropcare.view.core.components.CropTextBody
import com.capstone.cropcare.view.core.components.CropTextLabels
import com.capstone.cropcare.view.core.components.CropTextSubSection
import com.capstone.cropcare.view.core.components.CropTextTitle


@Composable
fun LoginScreen(
    loginViewModel: LoginViewModel = hiltViewModel(),
    navigateToRegister: () -> Unit
) {

    val uiState by loginViewModel.uiState.collectAsStateWithLifecycle() //-> con Lifecycle (cuando la pantalla termina, la subcripcion tambien)

    Scaffold { padding ->


        //BACKGROUND SETTING
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {


            Column(modifier = Modifier.padding(padding)) {
                Spacer(modifier = Modifier.weight(1.2f))

                //TITLE APP
                Box(
                    modifier = Modifier
                        .height(100.dp)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center

                ) { CropTextTitle(text = stringResource(R.string.app_name)) }

                Spacer(modifier = Modifier.weight(0.5f))


                //----------------------------- FORM LOGIN SECTION ------------------------------
                Box(
                    modifier = Modifier
                        .height(460.dp)
                        .fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(horizontal = 25.dp)) {
                        CropTextSubSection(
                            text = stringResource(R.string.log_scr_subtitle),
                            modifier = Modifier.padding(bottom = 15.dp),
                        )

                        //== EMAIL FIELD ==
                        EmailTextField(email = uiState.email) { loginViewModel.onEmailChanged(it) }
                        Spacer(Modifier.height(8.dp))

                        //== PASSWORD FIELD ==
                        PasswordTextField(password = uiState.password) {
                            loginViewModel.onPasswordChanged(
                                it
                            )
                        }
                        Spacer(Modifier.height(16.dp))

                        //== LOGIN BUTTON ==
                        CropButtonLogReg(
                            enabled = uiState.isLoginEnable,
                            onClick = {}, //-> VALIDATION
                            text = stringResource(R.string.log_scr_btn_login)
                        )

                        Spacer(modifier = Modifier.height(20.dp))
                        OrDivider()
                        Spacer(modifier = Modifier.height(20.dp))
                        GoogleLoginButton()
                        Spacer(Modifier.height(20.dp))

                        //------------------ REDIRECT TO REGISTER SECTION -------------------------
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Spacer(Modifier.weight(1f))
                            CropTextBody(text = stringResource(R.string.log_scr_txt_dont_have_account))
                            Spacer(Modifier.width(3.dp))

                            Text(
                                text = stringResource(R.string.log_scr_text_link_register),
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.SemiBold,
                                fontStyle = FontStyle.Italic,
                                textDecoration = TextDecoration.Underline,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.clickable { navigateToRegister() }) // REDIRECT TO REGISTER
                            Spacer(Modifier.weight(1f))
                        }


                    }

                }

                Spacer(modifier = Modifier.weight(0.85f))

            }
        }

    }

}


/**************************************** LOGIN COMPOSABLE SECTION ******************************************
 * Aqui estan los composables no genericos que se usan en LOGIN los cuales son llamados arriba, separo esta *
 * seccion a parte para legibilidad del codigo                                                              *
 ************************************************************************************************************/

//FIELD EMAIL ...........................
@Composable
fun EmailTextField(email: String, onEmailChanged: (String) -> Unit) {
    OutlinedTextField(
        value = email,
        onValueChange = { onEmailChanged(it) },
        modifier = Modifier.fillMaxWidth(),
        label = { CropTextLabels(text = stringResource(R.string.log_scr_txtfield_email)) },
        shape = MaterialTheme.shapes.medium,
        textStyle = MaterialTheme.typography.bodyLarge,


        )
}

//FIELD PASSWORD ...........................
@Composable
fun PasswordTextField(password: String, onPasswordChanged: (String) -> Unit) {
    var showPassword: Boolean by remember { mutableStateOf(false) }
    OutlinedTextField(
        value = password,
        onValueChange = { onPasswordChanged(it) },
        modifier = Modifier.fillMaxWidth(),
        label = { CropTextLabels(text = stringResource(R.string.log_scr_txtfield_password)) },
        singleLine = true,
        shape = MaterialTheme.shapes.medium,
        textStyle = MaterialTheme.typography.bodyLarge,
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Password,
            autoCorrectEnabled = false
        ),
        visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
        trailingIcon = {
            if (showPassword) {
                Icon(
                    painter = painterResource(R.drawable.ic_show_password),
                    contentDescription = "Show Password",
                    modifier = Modifier.clickable { showPassword = !showPassword }
                )
            } else {
                Icon(
                    painter = painterResource(R.drawable.ic_hidden_password),
                    contentDescription = "Hide Password",
                    modifier = Modifier.clickable { showPassword = !showPassword })
            }
        }

    )
}


//BUTTON CONNECT WITH GOOGLE ...........................
@Composable
fun GoogleLoginButton() {
    Button(
        onClick = {},
        modifier = Modifier
            .fillMaxWidth()
            .height(50.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.White,
            contentColor = Color.Black
        ),
        shape = MaterialTheme.shapes.large,

        ) {
        Icon(
            painter = painterResource(R.drawable.logo_google),
            contentDescription = "Google logo",
            tint = Color.Unspecified
        )
        Spacer(Modifier.width(10.dp))
        Text(
            text = stringResource(R.string.log_scr_btn_continue_google),
            style = MaterialTheme.typography.labelMedium
        )
    }
}

// DIVIDER ...........................

@Composable
fun OrDivider(modifier: Modifier = Modifier) {
    Row(modifier = modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        HorizontalDivider(
            modifier = Modifier.weight(1f),
            color = MaterialTheme.colorScheme.primary,
            thickness = 1.dp
        )

        Text(
            text = stringResource(R.string.log_scr_divider),
            modifier = Modifier.padding(horizontal = 8.dp),
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.SemiBold
        )

        HorizontalDivider(
            modifier = Modifier.weight(1f),
            color = MaterialTheme.colorScheme.primary,
            thickness = 1.dp
        )
    }
}
