package com.capstone.cropcare.view.core.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.capstone.cropcare.R

@Composable
fun CropZoneSelector(
    selectedZone: String,
    onZoneSelected: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val zones = listOf("Zona A", "Zona B", "Zona C")

    Box() {
        OutlinedTextField(
            value = selectedZone,
            onValueChange = {},
            label = { CropTextLabels(stringResource(R.string.report_card_label_crop_zone)) },
            modifier = Modifier.fillMaxWidth(),
            readOnly = true,
            trailingIcon = {
                IconButton (onClick = { expanded = true }) {
                    Icon(Icons.Default.ArrowDropDown, contentDescription = " ")
                }
            }
        )

        DropdownMenu (
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            zones.forEach { zone ->
                DropdownMenuItem(
                    text = { CropTextLabels(zone) },
                    onClick = {
                        onZoneSelected(zone)
                        expanded = false
                    }
                )
            }
        }
    }
}