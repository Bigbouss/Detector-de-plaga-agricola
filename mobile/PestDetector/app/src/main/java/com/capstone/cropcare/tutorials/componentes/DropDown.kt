package com.capstone.cropcare.tutorials.componentes

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import com.capstone.cropcare.R


@Composable
fun MyDropDownMenu(modifier: Modifier = Modifier) {

    var expanded: Boolean by remember { mutableStateOf(false) }

    Box(modifier = modifier) {
        Button(onClick = { expanded = true }) {
            Text("Ver opciones")
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            offset = DpOffset(16.dp, 16.dp) //eje x e y para despliegue del menu

        ) {

            DropdownMenuItem(text = { Text("opcion 1 ") }, onClick = { expanded = false })
            DropdownMenuItem(text = { Text("opcion 2 ") }, onClick = { expanded = false })
            DropdownMenuItem(text = { Text("opcion 3 ") }, onClick = { expanded = false })
            DropdownMenuItem(text = { Text("opcion 4 ") }, onClick = { expanded = false })
            DropdownMenuItem(text = { Text("opcion 5 ") }, onClick = { expanded = false })
        }

    }

}

@Composable
fun MyDropDownItem(modifier: Modifier = Modifier) {
    Column(modifier = modifier) {
        DropdownMenuItem(
            modifier = Modifier.fillMaxWidth(),
            text = {
                Text("Ejemplo  1")
            },
            leadingIcon = {
                Icon(
                    painter = painterResource(R.drawable.ic_launcher_foreground),
                    contentDescription = ""
                )
            },
            trailingIcon = {
                Icon(
                    painter = painterResource(R.drawable.ic_launcher_foreground),
                    contentDescription = ""
                )

            },
            onClick = {})
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyExposedDropDown(modifier: Modifier = Modifier) {
    var expanded: Boolean by remember { mutableStateOf(false) }
    var selection: String by remember { mutableStateOf("") }

    ExposedDropdownMenuBox(
        modifier = modifier,
        expanded = expanded,
        onExpandedChange = { expanded = !expanded }) {
        TextField(
            value = selection,
            onValueChange = {},
            readOnly = true,
            label = { Text("idioma") },
            modifier = Modifier
                .menuAnchor(MenuAnchorType.PrimaryNotEditable, enabled = true)
                .fillMaxWidth(),
            trailingIcon = {
                ExposedDropdownMenuDefaults.TrailingIcon(expanded)
            }

        )
    }

    DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
        DropdownMenuItem(text = { Text("Opcion 1") }, onClick = {
            selection = "Opcion 1"
            expanded = false
        })

        DropdownMenuItem(text = { Text("Opcion 2") }, onClick = {
            selection = "Opcion 2"
            expanded = false
        })

        DropdownMenuItem(text = { Text("Opcion 3") }, onClick = {
            selection = "Opcion 3"
            expanded = false
        })
    }
}