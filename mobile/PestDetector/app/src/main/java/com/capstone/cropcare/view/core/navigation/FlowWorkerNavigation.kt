package com.capstone.cropcare.view.core.navigation

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.capstone.cropcare.R
import com.capstone.cropcare.view.core.components.CropBottomBar
import com.capstone.cropcare.view.core.components.CropTopAppBar
import com.capstone.cropcare.view.workerViews.CameraScreen
import com.capstone.cropcare.view.workerViews.analysisResult.AnalysisScreen
import com.capstone.cropcare.view.workerViews.analysisResult.AnalysisViewModel
import com.capstone.cropcare.view.workerViews.home.HomeWorkerScreen
import com.capstone.cropcare.view.workerViews.homeHistory.HistoryViewModel
import com.capstone.cropcare.view.workerViews.homeHistory.HomeHistoryScreen
import com.capstone.cropcare.view.workerViews.reports.ReportScreenWorker
import com.capstone.cropcare.view.workerViews.reports.ReportViewModel
import kotlinx.coroutines.delay


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FlowWorkerNavigation() {
    val navController = rememberNavController()
    val activity = LocalActivity.current
    val context = LocalContext.current

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    // Definir qué rutas muestran cada barra
    val routesWithTopBar = listOf(
        HomeWorker::class.qualifiedName,
        HomeHistory::class.qualifiedName,

    )

    val routesWithBottomBar = listOf(
        HomeWorker::class.qualifiedName,
        HomeHistory::class.qualifiedName

    )

    val fullScreenRoutes = listOf(
        CamaraScreen::class.qualifiedName,
        AnalysisResultScreen::class.qualifiedName,
        ReportScreen::class.qualifiedName
    )

    val shouldShowTopBar = currentRoute in routesWithTopBar && currentRoute !in fullScreenRoutes
    val shouldShowBottomBar = currentRoute in routesWithBottomBar && currentRoute !in fullScreenRoutes

    val items = remember {
        listOf(
        NavItems(context.getString(R.string.home_bottom_bar_home), R.drawable.ic_home),
        NavItems(context.getString(R.string.home_bottom_bar_history), R.drawable.ic_history)
    )

    }
    val selectedIndex = when (currentRoute) {
        HomeWorker::class.qualifiedName -> 0
        HomeHistory::class.qualifiedName -> 1
        else -> 0
    }

    val isInHome = currentRoute == HomeWorker::class.qualifiedName
    var backPressedOnce by remember { mutableStateOf(false) }

    if (backPressedOnce) {
        LaunchedEffect(Unit) {
            delay(2000)
            backPressedOnce = false
        }
    }

    BackHandler {
        if (!isInHome) {
            navController.navigate(HomeWorker) {
                popUpTo(HomeWorker) { inclusive = false }
                launchSingleTop = true
            }
            backPressedOnce = false
        } else {
            if (backPressedOnce) {
                activity?.finish()
            } else {
                backPressedOnce = true
                Toast.makeText(context, "Presiona otra vez para salir", Toast.LENGTH_SHORT).show()
            }
        }
    }

    Scaffold(
        topBar = {
            if (shouldShowTopBar) {
                CropTopAppBar()
            }
        },
        bottomBar = {
            if (shouldShowBottomBar) {
                CropBottomBar(
                    itemList = items,
                    selectedIndex = selectedIndex,
                    onItemSelected = { index ->
                        val route = when (index) {
                            0 -> HomeWorker
                            1 -> HomeHistory
                            else -> HomeWorker
                        }

                        if (currentRoute == route::class.qualifiedName) return@CropBottomBar

                        navController.navigate(route) {
                            popUpTo(HomeWorker) { inclusive = false }
                            launchSingleTop = true
                        }
                    }
                )
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = HomeWorker,
            modifier = Modifier.padding(
                // Solo aplicar padding si hay barras visibles
                when {
                    shouldShowTopBar && shouldShowBottomBar -> innerPadding
                    shouldShowTopBar -> PaddingValues(top = innerPadding.calculateTopPadding())
                    shouldShowBottomBar -> PaddingValues(bottom = innerPadding.calculateBottomPadding())
                    else -> PaddingValues(0.dp)
                }
            )
        ) {
            composable<HomeWorker> {
                HomeWorkerScreen(
                    navigateToCamera = {
                        navController.navigate(CamaraScreen)
                    }
                )
            }

            composable<HomeHistory> { backStackEntry ->
                // Esto asegura que el mismo ViewModel se use cada vez
                val parentEntry = remember(backStackEntry) {
                    navController.getBackStackEntry(HomeWorker::class)
                }
                val historyViewModel: HistoryViewModel = hiltViewModel(parentEntry)

                HomeHistoryScreen(homeHistoryViewModel = historyViewModel)
            }


            composable<CamaraScreen> { backStackEntry ->
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
                    navigateToReport = {
                        navController.navigate(ReportScreen)
                    },
                    backToHome = {
                        analysisViewModel.reset()
                        navController.navigate(HomeWorker) {
                            popUpTo(HomeWorker) { inclusive = false }
                        }
                    },
                    backToCamera = {
                        analysisViewModel.reset()
                        navController.popBackStack()
                    }
                )
            }

            composable<ReportScreen> { backStackEntry ->
                val parentEntry = remember(backStackEntry) {
                    navController.getBackStackEntry(HomeWorker::class)
                }
                val analysisViewModel: AnalysisViewModel = hiltViewModel(parentEntry)
                val reportViewModel: ReportViewModel = hiltViewModel()

                ReportScreenWorker(
                    reportViewModel = reportViewModel,
                    analysisViewModel = analysisViewModel,
                    backToHome = {
                        analysisViewModel.clearTemporaryImage()
                        analysisViewModel.reset()
                        navController.navigate(HomeWorker) {
                            popUpTo(HomeWorker) { inclusive = false }
                        }
                    }
                )
            }
        }
    }
}