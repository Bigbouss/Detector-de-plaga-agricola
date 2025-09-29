package com.capstone.cropcare.tutorials.componentes

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun MySlider(modifier: Modifier = Modifier){

    var myValue: Float by remember { mutableFloatStateOf(0f) }

    Column(modifier = modifier.padding(horizontal = 30.dp)) {
        Slider(value = myValue, onValueChange = {myValue = it})
        Text(myValue.toString())
    }

}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MySliderAdvance(modifier: Modifier = Modifier){
    val state: SliderState = remember { SliderState(
        value = 5f,
        valueRange = 0f..10f,
        steps = 9,
        onValueChangeFinished = {}
    ) }

    Column(modifier = modifier.padding(horizontal = 32.dp)) {
        Slider(state)

    }

}