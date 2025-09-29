package com.capstone.cropcare.view.auth.other

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.airbnb.lottie.LottieComposition
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition
import com.capstone.cropcare.R
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(navigateToLogin:() -> Unit) {
    val composition: LottieComposition? by rememberLottieComposition(
        LottieCompositionSpec.RawRes(R.raw.plant_loading)
    )

    val progress by animateLottieCompositionAsState(
        composition = composition,
        iterations = 1,
        isPlaying = true
    )

    var visible by remember { mutableStateOf(true) }
    val backgroundColor = MaterialTheme.colorScheme.background

    // Detectar cuando la animación de Lottie termina
    LaunchedEffect(progress) {
        if (progress == 1f) {
            // Inicia el fadeOut
            visible = false
        }
    }

    // Detectar cuando el fadeOut termina → navegar al login
    LaunchedEffect(visible) {
        if (!visible) {
            delay(300) // debe coincidir con el tiempo de fadeOut
            navigateToLogin()
        }
    }

    // Fondo persistente
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor),
        contentAlignment = Alignment.Center
    ) {
        AnimatedVisibility(
            visible = visible,
            enter = fadeIn(animationSpec = tween(300)),
            exit = fadeOut(animationSpec = tween(300)) // se va hasta 0% de opacidad
        ) {
            LottieAnimation(
                composition = composition,
                progress = { progress },
                modifier = Modifier.fillMaxSize(0.5f)
            )
        }
    }
}

