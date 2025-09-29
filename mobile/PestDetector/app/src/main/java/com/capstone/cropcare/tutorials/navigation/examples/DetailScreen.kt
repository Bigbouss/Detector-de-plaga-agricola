package com.capstone.cropcare.tutorials.navigation.examples

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.sp

@Composable
fun DetailScreen(id: String, navigateStart:() -> Unit) {



    Column(modifier = Modifier.background(Color.Blue).fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally) {
        Spacer(Modifier.weight(1f))
        Text("DETAIL: $id", fontSize = 35.sp)
        Spacer(Modifier.weight(1f))
        Button(onClick = {navigateStart()}) {
            Text("Navegar a Login")
        }
        Spacer(Modifier.weight(1f))

    }
    
}