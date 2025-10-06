package com.capstone.cropcare.view.core.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp
import com.capstone.cropcare.view.ui.theme.customBtn

@Composable
fun CropButtonLogReg(
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    shape: Shape = MaterialTheme.shapes.large,
    onClick: () -> Unit,
    text: String,
    colors: ButtonColors = ButtonDefaults.buttonColors(
        containerColor = customBtn,
        contentColor = Color.White
    )
) {
    Button(
        modifier = modifier.fillMaxWidth().height(50.dp),
        enabled = enabled,
        shape = shape,
        onClick = onClick,
        colors = colors
    ) {
        CropTextButtonLogReg(
            modifier = Modifier.padding(4.dp),
            text = text,
            color = Color.White
        )
    }
}

@Composable
fun CropButtonPrimary(
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    shape: Shape = MaterialTheme.shapes.medium,
    onClick: () -> Unit,
    text: String,
    colors: ButtonColors = ButtonDefaults.buttonColors(
        containerColor = MaterialTheme.colorScheme.primary,
        contentColor = MaterialTheme.colorScheme.onPrimary
    )
) {
    Button(
        modifier = modifier,
        enabled = enabled,
        shape = shape,
        onClick = onClick,
        colors = colors
    ) {
        CropTextButtonPrincipal(
            modifier = Modifier.padding(4.dp),
            text = text,

        )
    }
}

@Composable
fun CropButtonSecundary(
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    shape: Shape = MaterialTheme.shapes.medium,
    onClick: () -> Unit,
    text: String,
    colors: ButtonColors = ButtonDefaults.buttonColors(
        containerColor = MaterialTheme.colorScheme.secondary,
        contentColor = MaterialTheme.colorScheme.onSecondary
    )
) {
    Button(
        modifier = modifier,
        enabled = enabled,
        shape = shape,
        onClick = onClick,
        colors = colors
    ) {
        CropTextButtonPrincipal(
            modifier = Modifier.padding(4.dp),
            text = text,

            )
    }
}