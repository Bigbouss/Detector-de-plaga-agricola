package com.capstone.cropcare.tutorials.componentes

import android.util.Log
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.capstone.cropcare.R


@Composable
fun MyButtons(modifier: Modifier){
    Column(modifier = modifier) {

        Button(
            onClick = { Log.i("test", "Boton pulsado")},
            enabled = true,
            shape = RoundedCornerShape(13.dp),
            border = BorderStroke(3.dp, Color.Red),
            colors = ButtonDefaults.buttonColors(contentColor=Color.Red, containerColor = Color.White)


            ) {
            Text("Pulsame")
        }

    }
}


@Composable
fun MyFloatingAcionButton(modifier: Modifier = Modifier) {

    FloatingActionButton(onClick = {

    }) {
        Icon(
            painter = painterResource(R.drawable.ic_launcher_foreground),
            contentDescription = " "
            )
    }
    
}