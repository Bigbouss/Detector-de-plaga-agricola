package com.capstone.cropcare.tutorials.navigation.examples

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun LoginScreen(navigateToDetail: () -> Unit) {
    var showView: Boolean by remember { mutableStateOf(false) } //Bandera para vista superpuesta

    Column(
        modifier = Modifier
            .background(Color.Red)
            .fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.weight(1f))
        Text("Login", fontSize = 35.sp)
        Spacer(Modifier.weight(1f))
       // Button(onClick = {navigateToDetail()}) {   //Opcion normal, navegacion a HOME
        Button(onClick = {showView = true}) {       //Opcion abriendo subvista o vista SuperPuesta
            Text("Navegar a HOME")
        }
        Spacer(Modifier.weight(1f))

    }

    if (showView){   //Si showView es true (presionan el boton)

        BackHandler {
            showView = false//Cerramos la subvista, para evitar que el usuario retroceda sin querer y solo cierre el box
        }

        Box(modifier = Modifier.size(300.dp).background(Color.Yellow))
    }

}