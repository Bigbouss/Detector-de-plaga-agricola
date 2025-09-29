package com.capstone.cropcare.tutorials.animation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun MyAnimatedVisibility(modifier: Modifier = Modifier) {
    var showView: Boolean by remember { mutableStateOf(true) }

    Column (modifier = Modifier
        .fillMaxSize()
        .background(Color.White)){
        Spacer(modifier = Modifier.height(50.dp))
        Button(onClick = {showView = !showView}) {
            Text("Mostrar / Ocultar")
        }
        Spacer(modifier = Modifier.height(50.dp))

        AnimatedVisibility (showView){  //cambiar un if por AnimatedVisibility para animación compose y se puede usar parametros para modificar
            Box(modifier = Modifier
                .fillMaxWidth()
                .height(150.dp)
                .background(Color.Red))

        }
    }

}