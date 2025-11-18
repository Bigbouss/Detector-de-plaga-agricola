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
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.*
import com.capstone.cropcare.R
import com.capstone.cropcare.view.auth.login.AuthViewModel
import com.capstone.cropcare.view.auth.login.LogoutState
import com.capstone.cropcare.view.core.components.CropBottomBar
import com.capstone.cropcare.view.core.components.CropDrawer
import com.capstone.cropcare.view.core.components.CropDrawerCustomState
import com.capstone.cropcare.view.core.components.CropTopAppBar
import com.capstone.cropcare.view.core.components.isOpened
import com.capstone.cropcare.view.core.components.opposite
import com.capstone.cropcare.view.testy.ScanCropScreen
import com.capstone.cropcare.view.testy.ScanCropViewModel
import com.capstone.cropcare.view.testy.ScanZoneScreen
import com.capstone.cropcare.view.testy.ScanZoneViewModel
import com.capstone.cropcare.view.workerViews.CameraScreen
import com.capstone.cropcare.view.workerViews.analysisResult.AnalysisScreen
import com.capstone.cropcare.view.workerViews.analysisResult.AnalysisViewModel
import com.capstone.cropcare.view.workerViews.home.HomeWorkerScreen
import com.capstone.cropcare.view.workerViews.homeHistory.HistoryViewModel
import com.capstone.cropcare.view.workerViews.homeHistory.HomeHistoryScreen
import com.capstone.cropcare.view.workerViews.reports.ReportScreenWorker
import com.capstone.cropcare.view.workerViews.reports.ReportViewModel
//import com.capstone.cropcare.view.workerViews.scanConfig.ScanConfigScreen
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FlowWorkerNavigation(
    onLogoutSuccess: () -> Unit,
    authViewModel: AuthViewModel = hiltViewModel()
) {
    val navController = rememberNavController()
    val activity = LocalActivity.current
    val context = LocalContext.current

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    // --- 🔹 Configuración de rutas ---
    val routesWithTopBar = listOf(
        HomeWorker::class.qualifiedName,
        HomeHistory::class.qualifiedName,

        //ScanConfig::class.qualifiedName
    )
    val routesWithBottomBar = routesWithTopBar
    val fullScreenRoutes = listOf(
        CamaraScreen::class.qualifiedName,
        AnalysisResultScreen::class.qualifiedName,
        ReportScreen::class.qualifiedName,
//        ScanZone::class.qualifiedName,     // <- AGREGAR
//        ScanCrop::class.qualifiedName
    )

    val shouldShowTopBar = currentRoute in routesWithTopBar && currentRoute !in fullScreenRoutes
    val shouldShowBottomBar = currentRoute in routesWithBottomBar && currentRoute !in fullScreenRoutes
    val logoutState by authViewModel.logoutState.collectAsStateWithLifecycle()

    // --- 🔹 Drawer derecho ---
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

    // --- 🔹 Estados de navegación y back ---
    val isInHome = currentRoute == HomeWorker::class.qualifiedName
    var backPressedOnce by remember { mutableStateOf(false) }

    if (backPressedOnce) {
        LaunchedEffect(Unit) {
            delay(2000)
            backPressedOnce = false
        }
    }

    // --- 🔹 Observar estado de logout ---
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

    // --- 🔹 BackHandler global ---
    BackHandler(enabled = true) {
        when {
            drawerState.isOpened() -> {
                drawerState = CropDrawerCustomState.Closed
            }
            !isInHome -> {
                navController.navigate(HomeWorker) {
                    popUpTo(HomeWorker) { inclusive = false }
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

    // --- 🔹 Estructura visual ---
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
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
                    val items = listOf(
                        NavItems(
                            context.getString(R.string.home_bottom_bar_home),
                            R.drawable.ic_home
                        ),
                        NavItems(
                            context.getString(R.string.home_bottom_bar_history),
                            R.drawable.ic_history
                        )
                    )
                    val selectedIndex = when (currentRoute) {
                        HomeWorker::class.qualifiedName -> 0
                        HomeHistory::class.qualifiedName -> 1
                        else -> 0
                    }

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
//            NavHost(
//                navController = navController,
//                startDestination = HomeWorker,
//                modifier = Modifier.padding(
//                    when {
//                        shouldShowTopBar && shouldShowBottomBar -> innerPadding
//                        shouldShowTopBar -> PaddingValues(top = innerPadding.calculateTopPadding())
//                        shouldShowBottomBar -> PaddingValues(bottom = innerPadding.calculateBottomPadding())
//                        else -> PaddingValues(0.dp)
//                    }
//                )
//            ) {
//                composable<HomeWorker> {
//                    HomeWorkerScreen(
//                        navigateToCamera = { navController.navigate(ScanZone) },
//                        navigateToActivity = { navController.navigate(ScanZone) }
//                    )
//
//
////                    HomeWorkerScreen(
////                        navigateToCamera = { navController.navigate(CamaraScreen)} ,
////                        navigateToActivity = { navController.navigate(ScanConfig) })
//
//                }
//                composable<HomeHistory> {
//                    val parentEntry =
//                        remember(it) { navController.getBackStackEntry(HomeWorker::class) }
//                    val historyViewModel: HistoryViewModel = hiltViewModel(parentEntry)
//                    HomeHistoryScreen(homeHistoryViewModel = historyViewModel)
//                }
//
//                composable<ScanZone> {
//                    val zoneVM: ScanZoneViewModel = hiltViewModel()
//
//                    ScanZoneScreen(
//                        viewModel = zoneVM,
//                        onZoneSelected = { zoneId ->
//                            navController.navigate(ScanCrop(zoneId))
//                        }
//                    )
//                }
//
//                composable<ScanCrop> { entry ->
//                    val zoneId = entry.arguments?.getString("zoneId")!!
//                    val cropVM: ScanCropViewModel = hiltViewModel()
//
//                    // Obtenemos AnalysisViewModel del parent (HomeWorker)
//                    val parentEntry =
//                        remember(entry) { navController.getBackStackEntry(HomeWorker::class) }
//                    val analysisVM: AnalysisViewModel = hiltViewModel(parentEntry)
//
//                    ScanCropScreen(
//                        zoneId = zoneId,
//                        viewModel = cropVM,
//                        onCropSelected = { cropName ->
//                            analysisVM.initClassifier(cropName) // 🔥 Carga modelo dinámico
//                            navController.navigate(CamaraScreen)
//                        }
//                    )
//
//
//                    composable<CamaraScreen> {
//                        val parentEntry =
//                            remember(it) { navController.getBackStackEntry(HomeWorker::class) }
//                        val analysisViewModel: AnalysisViewModel = hiltViewModel(parentEntry)
//
//                        CameraScreen(onPhotoTaken = { bitmap ->
//                            analysisViewModel.analyzePhoto(bitmap)
//                            navController.navigate(AnalysisResultScreen)
//                        })
//                    }
//
//
////                composable<CamaraScreen> {
////                    val parentEntry = remember(it) { navController.getBackStackEntry(HomeWorker::class) }
////                    val analysisViewModel: AnalysisViewModel = hiltViewModel(parentEntry)
////
////                    LaunchedEffect(Unit) {
////                        analysisViewModel.initClassifier("corn")   // <- usa el modelo que vayas a probar hoy
////                    }
////
////                    CameraScreen(onPhotoTaken = { bitmap ->
////                        analysisViewModel.analyzePhoto(bitmap)
////                        navController.navigate(AnalysisResultScreen)
////                    })
////                }
//                    composable<AnalysisResultScreen> {
//                        val parentEntry =
//                            remember(it) { navController.getBackStackEntry(HomeWorker::class) }
//                        val analysisViewModel: AnalysisViewModel = hiltViewModel(parentEntry)
//                        AnalysisScreen(
//                            analysisViewModel = analysisViewModel,
//                            navigateToReport = { navController.navigate(ReportScreen) },
//                            backToHome = {
//                                analysisViewModel.reset()
//                                navController.navigate(HomeWorker) {
//                                    popUpTo(HomeWorker) { inclusive = false }
//                                }
//                            },
//                            backToCamera = {
//                                analysisViewModel.reset()
//                                navController.popBackStack()
//                            }
//                        )
//                    }
//
//                    //
////                composable<ScanConfig>{
////                    ScanConfigScreen(
////                        navigateToScanning = {
////                            navController.navigate(ScanSession) {
////                                popUpTo(ScanConfig) { inclusive = false }
////                            }
////                        }
////                    ) {}
////                }
//                    //
//                    composable<ReportScreen> {
//                        val parentEntry =
//                            remember(it) { navController.getBackStackEntry(HomeWorker::class) }
//                        val analysisViewModel: AnalysisViewModel = hiltViewModel(parentEntry)
//                        val reportViewModel: ReportViewModel = hiltViewModel()
//                        ReportScreenWorker(
//                            reportViewModel = reportViewModel,
//                            analysisViewModel = analysisViewModel,
//                            backToHome = {
//                                analysisViewModel.clearTemporaryImage()
//                                analysisViewModel.reset()
//                                navController.navigate(HomeWorker) {
//                                    popUpTo(HomeWorker) { inclusive = false }
//                                }
//                            }
//                        )
//                    }
//                }
//            }
            NavHost(
                navController = navController,
                startDestination = HomeWorker,
                modifier = Modifier.padding(
                    when {
                        shouldShowTopBar && shouldShowBottomBar -> innerPadding
                        shouldShowTopBar -> PaddingValues(top = innerPadding.calculateTopPadding())
                        shouldShowBottomBar -> PaddingValues(bottom = innerPadding.calculateBottomPadding())
                        else -> PaddingValues(0.dp)
                    }
                )
            ) {

                // -------------------------
                // HOME SCREEN
                // -------------------------
                composable<HomeWorker> {
                    HomeWorkerScreen(
                        navigateToCamera = { navController.navigate(ScanZone) },
                        navigateToActivity = { navController.navigate(ScanZone) }
                    )
                }

                // -------------------------
                // HISTORY
                // -------------------------
                composable<HomeHistory> {
                    val parentEntry = remember(it) { navController.getBackStackEntry(HomeWorker::class) }
                    val historyViewModel: HistoryViewModel = hiltViewModel(parentEntry)
                    HomeHistoryScreen(homeHistoryViewModel = historyViewModel)
                }

                // -------------------------
                // SELECT ZONE
                // -------------------------
                composable<ScanZone> {
                    val zoneVM: ScanZoneViewModel = hiltViewModel()
                    ScanZoneScreen(
                        viewModel = zoneVM,
                        onZoneSelected = { zoneId ->
                            navController.navigate(ScanCrop(zoneId))
                        }
                    )
                }

                // -------------------------
                // SELECT CROP
                // -------------------------
                composable<ScanCrop> { entry ->
                    val zoneId = entry.arguments?.getString("zoneId")!!
                    val cropVM: ScanCropViewModel = hiltViewModel()

                    // Obtenemos el AnalysisVM desde HomeWorker para que sobreviva
                    val parentEntry = remember(entry) { navController.getBackStackEntry(HomeWorker::class) }
                    val analysisVM: AnalysisViewModel = hiltViewModel(parentEntry)

                    ScanCropScreen(
                        zoneId = zoneId,
                        viewModel = cropVM,
                        onCropSelected = { cropName ->
                            analysisVM.initClassifier(cropName)
                            navController.navigate(CamaraScreen)
                        }
                    )
                }

                // -------------------------
                // CAMERA SCREEN
                // -------------------------
                composable<CamaraScreen> {
                    val parentEntry = remember(it) { navController.getBackStackEntry(HomeWorker::class) }
                    val analysisViewModel: AnalysisViewModel = hiltViewModel(parentEntry)

                    CameraScreen(
                        onPhotoTaken = { bitmap ->
                            analysisViewModel.analyzePhoto(bitmap)
                            navController.navigate(AnalysisResultScreen)
                        }
                    )
                }

                // -------------------------
                // ANALYSIS RESULT
                // -------------------------
                composable<AnalysisResultScreen> {
                    val parentEntry = remember(it) { navController.getBackStackEntry(HomeWorker::class) }
                    val analysisViewModel: AnalysisViewModel = hiltViewModel(parentEntry)

                    AnalysisScreen(
                        analysisViewModel = analysisViewModel,
                        navigateToReport = { navController.navigate(ReportScreen) },
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

                // -------------------------
                // REPORT
                // -------------------------
                composable<ReportScreen> {
                    val parentEntry = remember(it) { navController.getBackStackEntry(HomeWorker::class) }
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


            // --- 🔹 Overlay oscuro + Drawer ---
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
                    // Drawer
                    Box(
                        modifier = Modifier
                            .align(Alignment.CenterEnd)
                            .clickable(
                                indication = null,
                                interactionSource = remember { MutableInteractionSource() }
                            ) { /* consume clicks */ }
                    ) {
                        CropDrawer(
                            selectedDrawerNavigationItem = selectedDrawerItem,
                            onNavigationItemClick = {
                                selectedDrawerItem = it
                                if (it == DrawerItem.Profile) {

                                    authViewModel.logout()
                                    drawerState = CropDrawerCustomState.Closed
                                }
                                drawerState = CropDrawerCustomState.Closed
                            },
                            onCloseClick = { drawerState = CropDrawerCustomState.Closed }
                        )
                    }

                    // 👇 BackHandler local (prioritario sobre el global)
                    BackHandler(enabled = true) {
                        drawerState = CropDrawerCustomState.Closed
                    }
                }
            }
        }
    }}