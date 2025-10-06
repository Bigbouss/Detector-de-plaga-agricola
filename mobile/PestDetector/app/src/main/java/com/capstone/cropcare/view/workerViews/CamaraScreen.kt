package com.capstone.cropcare.view.workerViews

import android.graphics.Bitmap
import android.view.ViewGroup
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.ImageProxy
import androidx.camera.view.LifecycleCameraController
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.FabPosition
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.capstone.cropcare.R
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import java.util.concurrent.Executor
import android.util.Log
import androidx.compose.runtime.DisposableEffect

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun CameraScreen(
    modifier: Modifier = Modifier,
    onPhotoTaken: (Bitmap) -> Unit = {}, // Lambda que recibe la foto
) {
    val context = LocalContext.current
    val cameraController = remember { LifecycleCameraController(context) }
    val lifecycle = LocalLifecycleOwner.current

    // 👇 Limpia la cámara cuando el Composable se destruya
    DisposableEffect(cameraController) {
        onDispose {
            Log.d("CameraScreen", "Liberando recursos de cámara")
            cameraController.unbind()
        }
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black),
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    val executor = ContextCompat.getMainExecutor(context)
                    takePicture(cameraController, executor, onPhotoTaken)
                },
                shape = CircleShape,
                containerColor = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(72.dp)
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_camera),
                    contentDescription = "Take picture"
                )
            }
        },
        floatingActionButtonPosition = FabPosition.Center,
    ) { paddingValues ->
        ViewCamera(
            modifier = modifier.padding(paddingValues),
            cameraController = cameraController,
            lifecycle = lifecycle
        )
    }
}


private fun takePicture(
    cameraController: LifecycleCameraController,
    executor: Executor,
    onPhotoTaken: (Bitmap) -> Unit
) {
    cameraController.takePicture(
        executor,
        object : ImageCapture.OnImageCapturedCallback() {
            override fun onCaptureSuccess(image: ImageProxy) {
                val bitmap = image.toBitmap()
                image.close() // ✅ Ya lo tienes, muy bien
                onPhotoTaken(bitmap)
            }

            override fun onError(exception: ImageCaptureException) {
                Log.e("CameraScreen", "Error al tomar foto: ${exception.message}", exception)
            }
        }
    )
}


// VIEW CAMERA
@Composable
fun ViewCamera(
    modifier: Modifier = Modifier,
    cameraController: LifecycleCameraController,
    lifecycle: LifecycleOwner
) {
    // 👇 Vincula y desvincula automáticamente con el ciclo de vida
    DisposableEffect(lifecycle, cameraController) {
        cameraController.bindToLifecycle(lifecycle)

        onDispose {
            // Se limpia automáticamente al desvincular
        }
    }

    AndroidView(
        modifier = modifier,
        factory = { context ->
            PreviewView(context).apply {
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT,
                )
                controller = cameraController
                implementationMode = PreviewView.ImplementationMode.COMPATIBLE // 👈 Opcional: mejor compatibilidad
            }
        }
    )
}
