package com.capstone.cropcare.tutorials.componentes

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun MyDivider(modifier: Modifier = Modifier) {

    Column {
        Text("Texto arriba")
        VerticalDivider() //EN COLUMNAS USAR VERTICAL, POR EL CONTRARIO USAR HORIZONTAL (Se pueden modificar parametros)
        Text("Texto abajo")
    }
}