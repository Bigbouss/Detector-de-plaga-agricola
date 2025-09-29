package com.capstone.cropcare.tutorials.animation

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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

@Composable
fun MyAnimatedContentSize(modifier: Modifier = Modifier) {
    var expanded: Boolean by remember { mutableStateOf(false) }


    Box(
        Modifier
            .fillMaxWidth()
            .background(Color.Red) //Importante dejar el background antes que el animated para evitar problemas de reconstruccion.
            .animateContentSize() //-> Solo agregando esto se crea animación para content size
            .height(if (expanded) 400.dp else 200.dp)
            .clickable{expanded = !expanded},
        contentAlignment = Alignment.Center
    ) {
        Text("Hoooooooooola")
    }
}