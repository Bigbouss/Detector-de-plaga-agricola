package com.capstone.cropcare.view.core.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun CropTextField(
    text: String,
    onValueChange: (String) -> Unit,
    label: String,
    placeholder: String = "",
    enabled: Boolean = true,
    singleLine: Boolean = true
) {
    TextField(
        value = text,
        onValueChange = onValueChange,
        label = { CropTextLabels(label) },
        placeholder = { CropTextPlaceHolder(placeholder) },
        singleLine = singleLine,
        modifier = Modifier.fillMaxWidth(),
        enabled = enabled,
        colors = TextFieldDefaults.colors(
            focusedPlaceholderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f),
            unfocusedPlaceholderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f)
        )

    )
}
