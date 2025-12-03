package com.capstone.cropcare.view.core.navigation

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.LocalActivity
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.capstone.cropcare.R
import com.capstone.cropcare.view.adminViews.assignZones.AssignZonesScreen
import com.capstone.cropcare.view.adminViews.home.HomeAdminScreen
import com.capstone.cropcare.view.adminViews.home.HomeAdminViewModel
import com.capstone.cropcare.view.adminViews.invitationManagement.InvitationManagementScreen
import com.capstone.cropcare.view.adminViews.metricsManagement.MetricsManagementScreen
import com.capstone.cropcare.view.adminViews.reportManagement.ReportManagementScreen
import com.capstone.cropcare.view.adminViews.zoneManagement.ZoneManagementScreen
import com.capstone.cropcare.view.core.components.*
import com.capstone.cropcare.view.auth.login.AuthViewModel
import com.capstone.cropcare.view.auth.login.LogoutState
import kotlinx.coroutines.delay
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FlowAdminNavigation(
    onLogoutSuccess: () -> Unit,
    authViewModel: AuthViewModel = hiltViewModel()
) {
    val navController = rememberNavController()
    val activity = LocalActivity.current
    val context = LocalContext.current

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    // Observar estado de logout
    val logoutState by authViewModel.logoutState.collectAsStateWithLifecycle()

    // --- Configuración de rutas ---
    val routesWithTopBar = listOf(
        HomeAdmin::class.qualifiedName,
        ZoneManagement::class.qualifiedName,
        ReportManagement::class.qualifiedName,
        MetricsAdmin::class.qualifiedName
    )
    val routesWithBottomBar = routesWithTopBar
    val fullScreenRoutes = listOf(InvitationManagement::class.qualifiedName)

    val shouldShowTopBar = currentRoute in routesWithTopBar && currentRoute !in fullScreenRoutes
    val shouldShowBottomBar = currentRoute in routesWithBottomBar && currentRoute !in fullScreenRoutes

    // --- Items de bottom bar ---
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

    // --- Drawer derecho ---
    var drawerState by remember { mutableStateOf(CropDrawerCustomState.Closed) }
    var selectedDrawerItem by remember { mutableStateOf(DrawerItem.Profile) }

    val configuration = LocalConfiguration.current
    val density = LocalDensity.current.density
    val screenWidth = remember {
        derivedStateOf { (configuration.screenWidthDp * density).roundToInt() }
    }
    val offsetValue by remember { derivedStateOf { (screenWidth.value / 4.5).dp } }

    val animatedOffset by animateDpAsState(
        targetValue = if (drawerState.isOpened()) -offsetValue else 0.dp,
        label = "AnimatedOffset"
    )
    val animatedScale by animateFloatAsState(
        targetValue = if (drawerState.isOpened()) 0.9f else 1f,
        label = "AnimatedScale"
    )

    // --- Observar estado de logout ---
    LaunchedEffect(logoutState) {
        when (logoutState) {
            is LogoutState.Success -> {
                authViewModel.resetLogoutState()
                onLogoutSuccess()
            }
            is LogoutState.Error -> {
                Toast.makeText(context, "Error al cerrar sesión", Toast.LENGTH_SHORT).show()
                authViewModel.resetLogoutState()
            }
            else -> {}
        }
    }

    // --- Estructura visual ---
    Box(
        modifier = Modifier
            .background(MaterialTheme.colorScheme.surface)
            .statusBarsPadding()
            .navigationBarsPadding()
            .fillMaxSize()
    ) {
        // --- Botón atrás UNIFICADO ---
        BackHandler(enabled = true) {
            when {
                drawerState.isOpened() -> {
                    drawerState = CropDrawerCustomState.Closed
                }
                !isInHome -> {
                    navController.navigate(HomeAdmin) {
                        popUpTo(HomeAdmin) { inclusive = false }
                        launchSingleTop = true
                    }
                    backPressedOnce = false
                }
                else -> {
                    if (backPressedOnce) {
                        activity?.finish()
                    } else {
                        backPressedOnce = true
                        Toast.makeText(
                            context,
                            context.getString(R.string.press_again_to_exit),
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
        }

        // Contenido principal con animación al abrir drawer
        Scaffold(
            modifier = Modifier
                .offset(x = animatedOffset)
                .scale(animatedScale),
            topBar = {
                if (shouldShowTopBar) {
                    CropTopAppBar(
                        onAvatarClick = { drawerState = drawerState.opposite() }
                    )
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
                composable<HomeAdmin> {
                    HomeAdminScreen(
                        goInvitationCode = { navController.navigate(InvitationManagement) },
                        goAssignZones = { workerId, workerName ->
                            navController.navigate(AssignZones(workerId, workerName)) // ✅ workerId ya es Int
                        }
                    )
                }
                composable<ZoneManagement> { ZoneManagementScreen() }
                composable<ReportManagement> { ReportManagementScreen() }
                composable<MetricsAdmin> { MetricsManagementScreen() }
                composable<InvitationManagement> {
                    InvitationManagementScreen(navigateBack = { navController.popBackStack() })
                }
                composable<AssignZones> { backStackEntry ->
                    val args = backStackEntry.toRoute<AssignZones>()

                    // Obtenemos el ViewModel del HomeAdmin (usando su back stack)
                    val parentEntry = remember(backStackEntry) {
                        navController.getBackStackEntry(HomeAdmin::class.qualifiedName!!)
                    }
                    val homeViewModel = hiltViewModel<HomeAdminViewModel>(parentEntry)

                    AssignZonesScreen(
                        workerName = args.workerName,
                        workerId = args.workerId,
                        navigateBack = {
                            navController.popBackStack()
                            // Recargamos lista de trabajadores al volver
                            homeViewModel.loadWorkers()
                        }
                    )
                }

            }
        }

        // --- Overlay oscuro + Drawer (clickable fuera para cerrar) ---
        if (drawerState.isOpened()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.5f))
                    .clickable(
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() }
                    ) {
                        drawerState = CropDrawerCustomState.Closed
                    }
            ) {
                Box(
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .clickable(
                            indication = null,
                            interactionSource = remember { MutableInteractionSource() }
                        ) { /* Consumir clicks dentro del drawer */ }
                ) {
                    CropDrawer(
                        selectedDrawerNavigationItem = selectedDrawerItem,
                        onNavigationItemClick = {
                            selectedDrawerItem = it
                            if (it == DrawerItem.Profile) {
                                // 🔹 Ejecutar logout
                                authViewModel.logout()
                                drawerState = CropDrawerCustomState.Closed
                            }
                        },
                        onCloseClick = { drawerState = CropDrawerCustomState.Closed }
                    )
                }

                BackHandler(enabled = true) {
                    drawerState = CropDrawerCustomState.Closed
                }
            }
        }

        // --- Loading overlay durante logout ---
        if (logoutState is LogoutState.Loading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.5f)),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            }
        }
    }
}