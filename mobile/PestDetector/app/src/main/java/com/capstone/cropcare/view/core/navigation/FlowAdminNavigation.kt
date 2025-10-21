package com.capstone.cropcare.view.core.navigation

import android.util.Log
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
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.capstone.cropcare.R
import com.capstone.cropcare.view.adminViews.home.HomeAdminScreen
import com.capstone.cropcare.view.adminViews.invitationManagement.InvitationManagementScreen
import com.capstone.cropcare.view.adminViews.metricsManagement.MetricsAdminScreen
import com.capstone.cropcare.view.adminViews.reportManagement.ReportManagementScreen
import com.capstone.cropcare.view.adminViews.zoneManagement.ZoneManagementScreen

import com.capstone.cropcare.view.core.components.CropBottomBar
import com.capstone.cropcare.view.core.components.CropTopAppBar
import kotlinx.coroutines.delay


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FlowAdminNavigation() {
    val navController = rememberNavController()
    val activity = LocalActivity.current
    val context = LocalContext.current

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    // --- 🔹 Definir qué pantallas tienen TopBar o BottomBar ---
    val routesWithTopBar = listOf(
        HomeAdmin::class.qualifiedName,
        ZoneManagement::class.qualifiedName,
        ReportManagement::class.qualifiedName,
        MetricsAdmin::class.qualifiedName
    )

    val routesWithBottomBar = listOf(
        HomeAdmin::class.qualifiedName,
        ZoneManagement::class.qualifiedName,
        ReportManagement::class.qualifiedName,
        MetricsAdmin::class.qualifiedName
    )

    // Pantallas fullscreen (sin barras)
    val fullScreenRoutes = listOf(
        InvitationManagement::class.qualifiedName
    )

    val shouldShowTopBar = currentRoute in routesWithTopBar && currentRoute !in fullScreenRoutes
    val shouldShowBottomBar = currentRoute in routesWithBottomBar && currentRoute !in fullScreenRoutes

    // --- 🔹 Items de la barra inferior ---
    val items = remember {
        listOf(
            NavItems(context.getString(R.string.home_bottom_bar_home), R.drawable.ic_home),
            NavItems(context.getString(R.string.home_admin_bottom_bar_zones), R.drawable.ic_crop_zones),
            NavItems(context.getString(R.string.home_admin_bottom_bar_reports), R.drawable.ic_reports),
            NavItems(context.getString(R.string.home_admin_bottom_bar_metrics), R.drawable.ic_metrics)
        )
    }

    val selectedIndex = when (currentRoute) {
        HomeAdmin::class.qualifiedName -> 0
        ZoneManagement::class.qualifiedName -> 1
        ReportManagement::class.qualifiedName -> 2
        MetricsAdmin::class.qualifiedName -> 3
        else -> 0
    }

    val isInHome = currentRoute == HomeAdmin::class.qualifiedName
    var backPressedOnce by remember { mutableStateOf(false) }

    if (backPressedOnce) {
        LaunchedEffect(Unit) {
            delay(2000)
            backPressedOnce = false
        }
    }

    // --- 🔹 Control del botón “atrás” ---
    BackHandler {
        if (!isInHome) {
            navController.navigate(HomeAdmin) {
                popUpTo(HomeAdmin) { inclusive = false }
                launchSingleTop = true
            }
            backPressedOnce = false
        } else {
            if (backPressedOnce) {
                activity?.finish()
            } else {
                backPressedOnce = true
                Toast.makeText(context, context.getString(R.string.press_again_to_exit), Toast.LENGTH_SHORT).show()
            }
        }
    }

    // --- 🔹 Estructura general del flujo ---
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
                            0 -> HomeAdmin
                            1 -> ZoneManagement
                            2 -> ReportManagement
                            3 -> MetricsAdmin
                            else -> HomeAdmin
                        }

                        if (currentRoute == route::class.qualifiedName) return@CropBottomBar

                        navController.navigate(route) {
                            popUpTo(HomeAdmin) { inclusive = false }
                            launchSingleTop = true
                        }
                    }
                )
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = HomeAdmin,
            modifier = Modifier.padding(
                when {
                    shouldShowTopBar && shouldShowBottomBar -> innerPadding
                    shouldShowTopBar -> PaddingValues(top = innerPadding.calculateTopPadding())
                    shouldShowBottomBar -> PaddingValues(bottom = innerPadding.calculateBottomPadding())
                    else -> PaddingValues(0.dp)
                }
            )
        ) {
            composable<HomeAdmin> { HomeAdminScreen(
                goInvitationCode = { navController.navigate(InvitationManagement) }
            ) }

            composable<ZoneManagement> {
                ZoneManagementScreen()
            }

            composable<ReportManagement> { ReportManagementScreen() }
            composable<MetricsAdmin> { MetricsAdminScreen() }

            composable<InvitationManagement> {
                InvitationManagementScreen(
                    navigateBack = { navController.popBackStack() }
                )
            }
        }
    }
}
