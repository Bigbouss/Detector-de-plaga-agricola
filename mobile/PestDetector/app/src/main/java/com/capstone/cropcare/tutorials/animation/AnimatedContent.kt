package com.capstone.cropcare.tutorials.animation

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp


//Cambiar contenido o vistas
@Composable
fun MyAnimatedContent(modifier: Modifier = Modifier) {
    var number: Int by remember { mutableIntStateOf(0) }

    Column(modifier = Modifier
        .fillMaxSize()
        .padding(top = 48.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Button(onClick = {number ++}) {
            Text("Sumar")
        }

        Spacer(modifier = Modifier.height(32.dp))
        AnimatedContent(targetState = number) { Number ->
            Text("El número es $Number")   //se puede usar para cualquier tipo de COmposable como el ejemplo de abajo

//            when(Number){
//                0 -> Box(modifier = Modifier.size(50.dp).background(Color.Red))
//                1 -> Text("asdasdasd")
//                2 -> FloatingActionButton(onClick = {}) {Text("Pulsame") }
//                3 -> Box(modifier = Modifier.size(150.dp).background(Color.Yellow))
//            }

        }


    }
    
}