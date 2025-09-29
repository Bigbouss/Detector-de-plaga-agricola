package com.capstone.cropcare.view.core.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.capstone.cropcare.view.auth.other.SplashScreen
import com.capstone.cropcare.view.auth.login.LoginScreen
import com.capstone.cropcare.view.auth.register.RegisterScreen
import com.capstone.cropcare.view.workerViews.CameraScreen
import com.capstone.cropcare.view.workerViews.analysisResult.AnalysisScreen
import com.capstone.cropcare.view.workerViews.homeHistory.HomeHistoryScreen
import com.capstone.cropcare.view.workerViews.home.HomeWorkerScreen
import com.capstone.cropcare.view.workerViews.reports.ReportScreenWorker


@Composable
fun NavigationWrapper(modifier: Modifier = Modifier) {
    val navController: NavHostController = rememberNavController()

    NavHost(
        navController = navController,
        //startDestination = Splash
        startDestination = ReportScreen
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
                navigateToCamera = {navController.navigate(CamaraScreen)},


            )
        }
        composable<CamaraScreen> {
            CameraScreen(
                onPhotoTaken = { bitmap ->
                    println("Foto tomada! tamaño: ${bitmap.width}x${bitmap.height}")
                },

            )

        }

        composable<HomeHistory> {
            HomeHistoryScreen(navigateToHome = {
                navController.navigate(HomeWorker) {
                    popUpTo(HomeHistory) { inclusive = true }
                }
            })
        }

        composable<AnalysisResultScreen>{
            AnalysisScreen(
                navigateToReport = { navController.navigate(ReportScreen)},
                backToHome = {navController.navigate(HomeWorker)},
                backToCamera = { navController.popBackStack() }

            )
        }

        composable<ReportScreen>{
            ReportScreenWorker()
        }
    }

}