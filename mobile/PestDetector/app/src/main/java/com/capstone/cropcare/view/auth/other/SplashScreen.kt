package com.capstone.cropcare.view.auth.other

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.airbnb.lottie.LottieComposition
import com.airbnb.lottie.compose.*
import com.capstone.cropcare.R
import com.capstone.cropcare.domain.model.UserRole
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
    splashViewModel: SplashViewModel = hiltViewModel(),
    navigateToLogin: () -> Unit,
    navigateToWorkerHome: () -> Unit,
    navigateToAdminHome: () -> Unit
) {
    val uiState by splashViewModel.uiState.collectAsStateWithLifecycle()

    val composition: LottieComposition? by rememberLottieComposition(
        LottieCompositionSpec.RawRes(R.raw.plant_loading)
    )

    val progress by animateLottieCompositionAsState(
        composition = composition,
        iterations = 1,
        isPlaying = true
    )

    var visible by remember { mutableStateOf(true) }
    var shouldNavigate by remember { mutableStateOf(false) }
    var navigationDestination by remember { mutableStateOf<NavigationDestination?>(null) }

    val backgroundColor = MaterialTheme.colorScheme.background

    // Detectar cuando la animación de Lottie termina Y el auth check está completo
    LaunchedEffect(progress, uiState) {
        // Esperar a que:
        // 1. La animación termine (progress == 1f)
        // 2. El estado de auth no sea Loading
        if (progress == 1f && uiState !is SplashState.Loading) {
            // Determina a dónde navegar según el estado de auth
            navigationDestination = when (val state = uiState) {
                is SplashState.NotAuthenticated -> NavigationDestination.Login
                is SplashState.Authenticated -> {
                    when (state.userRole) {
                        UserRole.ADMIN -> NavigationDestination.AdminHome
                        UserRole.WORKER -> NavigationDestination.WorkerHome
                    }
                }
                else -> null
            }

            // Inicia el fadeOut
            visible = false
        }
    }

    // Detectar cuando el fadeOut termina → navegar
    LaunchedEffect(visible) {
        if (!visible && navigationDestination != null) {
            delay(300) // Debe coincidir con el tiempo de fadeOut
            shouldNavigate = true
        }
    }

    // Ejecutar navegación
    LaunchedEffect(shouldNavigate) {
        if (shouldNavigate) {
            when (navigationDestination) {
                NavigationDestination.Login -> navigateToLogin()
                NavigationDestination.AdminHome -> navigateToAdminHome()
                NavigationDestination.WorkerHome -> navigateToWorkerHome()
                null -> { /* No hacer nada */ }
            }
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
            exit = fadeOut(animationSpec = tween(300))
        ) {
            LottieAnimation(
                composition = composition,
                progress = { progress },
                modifier = Modifier.fillMaxSize(0.5f)
            )
        }
    }
}

// Helper sealed class para navegación
private sealed class NavigationDestination {
    object Login : NavigationDestination()
    object AdminHome : NavigationDestination()
    object WorkerHome : NavigationDestination()
}