package com.capstone.cropcare.tutorials.testeodeStates

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.setValue

@Composable
/*fun myState(modifier: Modifier) {
    val example1 = remember { mutableStateOf(0) }
    val example2 = rememberSaveable { mutableStateOf(0) }
    var example3 by rememberSaveable { mutableStateOf(0) }
    Column{
        //Example1 - solo usa remember -> se borrara el valor al girar pantalla (muere la vista)
        Text(text = "Ejemplo con 'remember' : ${example1.value}", modifier = modifier.clickable{example1.value += 1})

        //Example2 - con rememberSaveable la vista guarda el valor y lo puede recordar, solucionando lo de remember.
        Text(text = "Ejemplo con 'rememberSaveable': ${example2.value}", modifier = modifier.clickable{example2.value+=2})

        //Example 3 - usando by se agiliza el codigo, no es necesario colocar .value pero se usa un var.
        Text(text = "Ejemplo 3 usando 'by': $example3", modifier = modifier.clickable{example3 +=3})

    }
}*/

fun MyState(modifier: Modifier) {
    var example by rememberSaveable { mutableIntStateOf(0) }
    Column(modifier = modifier){

        ExampleState1(example = example, onClick = {example +=1})
        ExampleState2(example) {example +=1}

    }
}



@Composable
fun ExampleState1(example: Int , onClick: ()-> Unit){
    Text(text = "Ejemplo 1 usando 'by': $example", modifier = Modifier.clickable{onClick() })
}

@Composable
fun ExampleState2(example:Int , onClick: ()-> Unit){
    Text(text = "Ejemplo 2 usando 'by': $example", modifier = Modifier.clickable{onClick() })
}