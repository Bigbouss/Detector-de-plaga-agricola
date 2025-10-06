package com.capstone.cropcare.view.core.navigation

import android.graphics.Bitmap
import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.capstone.cropcare.view.auth.other.SplashScreen
import com.capstone.cropcare.view.auth.login.LoginScreen
import com.capstone.cropcare.view.auth.register.RegisterScreen
import com.capstone.cropcare.view.workerViews.CameraScreen
import com.capstone.cropcare.view.workerViews.analysisResult.AnalysisScreen
import com.capstone.cropcare.view.workerViews.analysisResult.AnalysisViewModel
import com.capstone.cropcare.view.workerViews.homeHistory.HomeHistoryScreen
import com.capstone.cropcare.view.workerViews.home.HomeWorkerScreen
import com.capstone.cropcare.view.workerViews.reports.ReportScreenWorker
import com.capstone.cropcare.view.workerViews.reports.ReportViewModel


@Composable
fun NavigationWrapper(modifier: Modifier = Modifier) {
    val navController: NavHostController = rememberNavController()

    NavHost(
        navController = navController,
        //startDestination = Splash
        startDestination = HomeHistory
    ) {

        composable<Splash> {
            SplashScreen(navigateToLogin = {
                navController.navigate(Login) {
                    popUpTo(Splash) { inclusive = true } //EVITAR APILAMIENTO
                }
            })
        }

        composable<Login> {
            LoginScreen(navigateToRegister = { navController.navigate(Register) })
        }

        composable<Register> {
            RegisterScreen(navigateBack = { navController.popBackStack() })
        }

        composable<HomeWorker> {
            HomeWorkerScreen(
                navigateToHistory = {
                    navController.navigate(HomeHistory) {
                        popUpTo(HomeWorker) { inclusive = true }
                    }
                },
                navigateToCamera = { navController.navigate(CamaraScreen) },


                )
        }
        composable<CamaraScreen> { backStackEntry ->
            // Obtén una "ruta padre" común - puede ser cualquier ruta que englobe ambas pantallas
            // Si HomeWorker es tu pantalla anterior, usa esa:
            val parentEntry = remember(backStackEntry) {
                navController.getBackStackEntry(HomeWorker::class)
            }

            val analysisViewModel: AnalysisViewModel = hiltViewModel(parentEntry)

            CameraScreen(
                onPhotoTaken = { bitmap ->
                    analysisViewModel.analyzePhoto(bitmap)
                    navController.navigate(AnalysisResultScreen)
                }
            )
        }

        composable<AnalysisResultScreen> { backStackEntry ->
            val parentEntry = remember(backStackEntry) {
                navController.getBackStackEntry(HomeWorker::class)
            }

            val analysisViewModel: AnalysisViewModel = hiltViewModel(parentEntry)

            AnalysisScreen(
                analysisViewModel = analysisViewModel,
                navigateToReport = { navController.navigate(ReportScreen) },
                backToHome = {
                    analysisViewModel.reset()
                    navController.navigate(HomeWorker)
                },
                backToCamera = {
                    analysisViewModel.reset()
                    navController.popBackStack()
                }
            )
        }



        composable<HomeHistory> {
            HomeHistoryScreen(navigateToHome = {
                navController.navigate(HomeWorker) {
                    popUpTo(HomeHistory) { inclusive = true }
                }
            })
        }



        composable<ReportScreen> {
            val parentEntry = remember(it) {
                navController.getBackStackEntry(HomeWorker::class)
            }

            val analysisViewModel: AnalysisViewModel = hiltViewModel(parentEntry)
            val reportViewModel: ReportViewModel = hiltViewModel()

            // Inicializa los datos del reporte con la foto del análisis
            LaunchedEffect(Unit) {
                val tempBitmap = analysisViewModel.tempBitmap.value
                val savedPath = analysisViewModel.savedImagePath.value

                Log.d("ReportScreen", "Bitmap: $tempBitmap, Path: $savedPath")

                if (tempBitmap != null) {
                    reportViewModel.setAnalizedPhoto(tempBitmap)
                }
                if (savedPath != null) {
                    reportViewModel.setLocalPhotoPath(savedPath)
                }

                // También puedes setear el diagnóstico aquí
                reportViewModel.setDiagnostic("Plaga detectada") // O lo que corresponda
                reportViewModel.setWorkerName("Usuario actual") // Debes obtener el nombre real
            }

            ReportScreenWorker(
                reportViewModel = reportViewModel,
                backToHome = {
                    analysisViewModel.clearTemporaryImage() // Limpia la imagen temporal
                    analysisViewModel.reset()
                    navController.navigate(HomeWorker) {
                        popUpTo(HomeWorker) { inclusive = false }
                    }
                }
            )
        }
    }
}