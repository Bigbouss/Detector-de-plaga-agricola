package com.capstone.cropcare.tutorials.testeoDeCompose

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

//@Preview
@Composable
fun ComponentBox(){
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center){
        Box(modifier = Modifier.size(200.dp)
            .background(Color.Green)
            , contentAlignment = Alignment.Center){
            Text(text = "Esto es un box!!!")
        }
    }
}


@Composable
fun ComponentColumn(modifier: Modifier = Modifier){
    Column(modifier = modifier.fillMaxSize().verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
        Text (text = "Texto 1", modifier = Modifier.background(Color.Red))
        Text (text = "Texto 2", modifier = Modifier.background(Color.Green))
        Text (text = "Texto 3", modifier = Modifier.background(Color.Blue))
        Text (text = "Texto 4", modifier = Modifier.background(Color.Magenta))


    }
}


@Composable
fun ComponentRow(modifier: Modifier = Modifier){
    Row(modifier = modifier.fillMaxSize(), horizontalArrangement = Arrangement.SpaceAround, verticalAlignment = Alignment.CenterVertically) {
        Text (text = "fila 1")
        Text (text = "fila 2")
        Text (text = "fila 3")
        Text (text = "fila 4")
    }
}


@Composable
fun MyComplexLayout(modifier: Modifier){
    Column(modifier = modifier.fillMaxSize()){
        Box(modifier = Modifier.weight(1f).fillMaxWidth().background(Color.Cyan), contentAlignment = Alignment.Center){
            Text (text="Ejemplo 1")
        }
        Box(modifier = Modifier.weight(1f).fillMaxWidth()){
            Row(){
                Box(modifier = Modifier.weight(1f).fillMaxHeight().background(Color.Red), contentAlignment = Alignment.Center){
                    Text(text= "ejemplo 3")
                }
                Box(modifier = Modifier.weight(1f).fillMaxHeight().background(Color.Green), contentAlignment = Alignment.Center){
                    Text(text= "ejemplo 4")
                }
            }
        }
        Box(modifier = Modifier.weight(1f).fillMaxWidth().background(Color.Magenta), contentAlignment = Alignment.BottomCenter){
            Text(text="Ejemplo 4")
        }
    }

}