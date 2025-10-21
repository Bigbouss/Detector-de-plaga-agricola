package com.capstone.cropcare.view.core.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.capstone.cropcare.R

@Composable
fun CropTextField(
    modifier: Modifier = Modifier,
    text: String,
    onValueChange: (String) -> Unit,
    label: String,
    placeholder: String = "",
    enabled: Boolean = true,
    singleLine: Boolean = true,

) {
    TextField(
        value = text,
        onValueChange = onValueChange,
        label = { CropTextLabels(label) },
        placeholder = { CropTextPlaceHolder(placeholder) },
        singleLine = singleLine,
        modifier = modifier.fillMaxWidth(),
        enabled = enabled,
        colors = TextFieldDefaults.colors(
            focusedPlaceholderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f),
            unfocusedPlaceholderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f)
        ),


    )
}

@Composable
fun CropTextFieldObservation(
    modifier: Modifier = Modifier,
    text: String,
    onValueChange: (String) -> Unit,
    label: String,
    placeholder: String = "",
    enabled: Boolean = true,
    singleLine: Boolean = true,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    maxLines: Int = if (singleLine) 1 else 5,
    minLines: Int = if (singleLine) 1 else 3,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default
) {
    TextField(
        value = text,
        onValueChange = onValueChange,
        label = { CropTextLabels(label) },
        placeholder = { CropTextPlaceHolder(placeholder) },
        singleLine = singleLine,
        maxLines = maxLines,
        minLines = minLines,
        modifier = modifier.fillMaxWidth(),
        enabled = enabled,
        interactionSource = interactionSource,
        keyboardOptions = keyboardOptions,
        keyboardActions = keyboardActions,
        colors = TextFieldDefaults.colors(
            focusedPlaceholderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f),
            unfocusedPlaceholderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f)
        )
    )
}

@Composable
fun EmailTextField(
    email: String,
    onEmailChanged: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = email,
        onValueChange = onEmailChanged,
        modifier = modifier.fillMaxWidth(),
        label = { CropTextLabels(text = stringResource(R.string.reg_scr_txtfield_email)) },
        placeholder = { CropTextPlaceHolder(text = "ejemplo@correo.com") },
        shape = MaterialTheme.shapes.medium,
        textStyle = MaterialTheme.typography.bodyLarge,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
        singleLine = true
    )
}

@Composable
fun PasswordTextField(
    password: String,
    onPasswordChanged: (String) -> Unit,
    modifier: Modifier = Modifier,
    label: String = stringResource(R.string.reg_scr_txtfield_password)
) {
    var showPassword by remember { mutableStateOf(false) }

    OutlinedTextField(
        value = password,
        onValueChange = onPasswordChanged,
        modifier = modifier.fillMaxWidth(),
        label = { CropTextLabels(text = label) },
        placeholder = { CropTextPlaceHolder(text = "••••••••") },
        singleLine = true,
        shape = MaterialTheme.shapes.medium,
        textStyle = MaterialTheme.typography.bodyLarge,
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Password,
            autoCorrectEnabled = false
        ),
        visualTransformation = if (showPassword) {
            VisualTransformation.None
        } else {
            PasswordVisualTransformation()
        },
        trailingIcon = {
            Icon(
                painter = painterResource(
                    if (showPassword) R.drawable.ic_show_password
                    else R.drawable.ic_hidden_password
                ),
                contentDescription = if (showPassword) "Ocultar contraseña" else "Mostrar contraseña",
                modifier = Modifier.clickable { showPassword = !showPassword }
            )
        }
    )
}
