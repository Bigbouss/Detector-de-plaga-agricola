package com.capstone.cropcare.view.core.navigation


import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.capstone.cropcare.view.adminViews.invitationManagement.InvitationManagementScreen
import com.capstone.cropcare.view.auth.other.SplashScreen
import com.capstone.cropcare.view.auth.login.LoginScreen
import com.capstone.cropcare.view.auth.register.admin.RegisterAdminScreen
import com.capstone.cropcare.view.auth.register.worker.RegisterWorkerScreen



@Composable
fun NavigationWrapper(modifier: Modifier = Modifier) {
    val navController: NavHostController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Splash
        //startDestination = AdminFlow

    ) {
        // ========== SPLASH SCREEN ==========
        composable<Splash> {
            SplashScreen(
                navigateToLogin = {
                    navController.navigate(Login) {
                        popUpTo(Splash) { inclusive = true }
                    }
                },
                navigateToWorkerHome = {
                    navController.navigate(WorkerFlow) {
                        popUpTo(Splash) { inclusive = true }
                    }
                },
                navigateToAdminHome = {
                    navController.navigate(AdminFlow) {
                        popUpTo(Splash) { inclusive = true }
                    }
                }
            )
        }


        // ========== AUTH FLOW ==========
        composable<Login> {
            LoginScreen(
                navigateToRegisterAdmin = {
                    navController.navigate(RegisterAdmin)
                },
                navigateToRegisterWorker = {
                    navController.navigate(RegisterWorker)
                },
                navigateToAdminHome = { // 👈 Nuevo
                    navController.navigate(AdminFlow) {
                        popUpTo(Login) { inclusive = true }
                    }
                },
                navigateToWorkerHome = { // 👈 Nuevo
                    navController.navigate(WorkerFlow) {
                        popUpTo(Login) { inclusive = true }
                    }
                }
            )
        }

        composable<RegisterAdmin> {
            RegisterAdminScreen(
                navigateBack = {
                    navController.popBackStack()
                },
                navigateToHome = {
                    navController.navigate(AdminFlow) {
                        popUpTo(RegisterAdmin) { inclusive = true }
                    }
                }
            )
        }

        composable<RegisterWorker> {
            RegisterWorkerScreen(
                navigateBack = {
                    navController.popBackStack()
                },
                navigateToHome = {
                    navController.navigate(WorkerFlow) {
                        popUpTo(RegisterWorker) { inclusive = true }
                    }
                }
            )
        }

        // ========== WORKER FLOW ==========
        composable <WorkerFlow>{
            FlowWorkerNavigation()
        }

        // ========== ADMIN FLOW ==========
        composable<AdminFlow> {
            FlowAdminNavigation()
        }


    }
}
