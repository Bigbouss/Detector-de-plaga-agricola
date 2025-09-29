package com.capstone.cropcare.tutorials.componentes

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.airbnb.lottie.LottieComposition
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.rememberLottieComposition
import com.capstone.cropcare.R

@Composable
fun Progress(modifier: Modifier) {
    Column(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        CircularProgressIndicator() //Se puede editar los parametros
        //LinearProgressIndicator() -> modo linear

    }
}

@Composable
fun AdvanceProgressBar(modifier: Modifier) {
    var progressValue: Float by remember { mutableFloatStateOf(0.5f) }
    var isLoading: Boolean by remember { mutableStateOf(value = false) }
    val animatedProgress: Float by animateFloatAsState(targetValue = progressValue)

    Column(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (isLoading) {

            CircularProgressIndicator(
                progress = { animatedProgress },
                modifier.size(50.dp),

                )
        }
        Spacer(Modifier.height(24.dp))
        Row(modifier.padding(24.dp)) {
            Button(onClick = { progressValue -= 0.1f }) { Text(text = "<-") }
            Spacer(modifier.width(24.dp))
            Button(onClick = { progressValue += 0.1f }) { Text(text = "->") }
        }

        Button(onClick = {isLoading = !isLoading}) { Text(text = "Show/Hide") }
    }
}


@Composable
fun ProgressAnimation(modifier: Modifier){
    val composition: LottieComposition? by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.plant_loading))

    Column(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ){
        LottieAnimation(composition = composition, iterations = LottieConstants.IterateForever)

    }

}