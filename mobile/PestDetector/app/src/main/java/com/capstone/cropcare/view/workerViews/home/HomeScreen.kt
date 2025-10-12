@file:OptIn(ExperimentalMaterial3Api::class)

package com.capstone.cropcare.view.workerViews.home

import android.Manifest
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition
import com.capstone.cropcare.R
import com.capstone.cropcare.domain.utils.formatWeatherDescription
import com.capstone.cropcare.domain.utils.getWeatherAnimation
import com.capstone.cropcare.view.core.components.CropBottomBar
import com.capstone.cropcare.view.core.components.CropButtonPrimary
import com.capstone.cropcare.view.core.components.CropCard
import com.capstone.cropcare.view.core.components.CropCardWeather
import com.capstone.cropcare.view.core.components.CropTextCardDescription
import com.capstone.cropcare.view.core.components.CropTextCardDescriptionSmall
import com.capstone.cropcare.view.core.components.CropTextCardTitle
import com.capstone.cropcare.view.core.components.CropTopAppBar
import com.capstone.cropcare.view.core.navigation.NavItems
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import kotlin.collections.listOf

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun HomeWorkerScreen(
    homeViewModel: HomeViewModel = hiltViewModel(),
    navigateToHistory: () -> Unit,
    navigateToCamera: () -> Unit,

    ) {

    val permissionState = rememberPermissionState(permission = Manifest.permission.CAMERA)
    val locationPermissionState =
        rememberPermissionState(permission = Manifest.permission.ACCESS_FINE_LOCATION)
    var shouldNavigateToCamera by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val weatherState by homeViewModel.weather.collectAsState()

    LaunchedEffect(locationPermissionState.status.isGranted) {
        if (locationPermissionState.status.isGranted) {
            val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)

            try {
                fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                    if (location != null) {
                        homeViewModel.loadWeather(location.latitude, location.longitude)
                    } else {
                        // Si no hay lastLocation, pedimos ubicación actual
                        fusedLocationClient
                            .getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
                            .addOnSuccessListener { loc ->
                                loc?.let {
                                    homeViewModel.loadWeather(it.latitude, it.longitude)
                                }
                            }
                    }
                }
            } catch (e: SecurityException) {
                e.printStackTrace()
            }
        } else {
            locationPermissionState.launchPermissionRequest()
        }
    }


    Scaffold(
        topBar = { CropTopAppBar() },
        bottomBar = {
            CropBottomBar(
                itemList = listOf(
                    NavItems(stringResource(R.string.home_bottom_bar_home), R.drawable.ic_home, onClick = {}),
                    NavItems(stringResource(R.string.home_bottom_bar_history), R.drawable.ic_history, onClick = { navigateToHistory() })
                ), selectedIndex = 0, onItemSelected = {}
            )
        })

    { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(paddingValues)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 17.dp)
            ) {

                //CARD SECTION - WEATHER------------------------------------------------------------------
                Spacer(Modifier.height(15.dp))
                CropCardWeather {

                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center,
                    ) {
                        weatherState?.let { weather ->
                            Row(
                                modifier = Modifier
                                    .padding(vertical = 5.dp)
                                    .padding(horizontal = 10.dp)
                                    .fillMaxSize(),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                // Icono del clima
                                Row(
                                    modifier = Modifier
                                        .fillMaxHeight()
                                        .weight(2f),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Start
                                ) {

                                    Box(contentAlignment = Alignment.Center) {

                                        weather.weather.firstOrNull()?.icon?.let { iconCode ->
                                            val animationRes = getWeatherAnimation(iconCode)
                                            val composition by rememberLottieComposition(
                                                LottieCompositionSpec.RawRes(animationRes)
                                            )
                                            val progress by animateLottieCompositionAsState(
                                                composition,
                                                iterations = LottieConstants.IterateForever
                                            )

                                            LottieAnimation(
                                                composition = composition,
                                                progress = { progress },
                                                modifier = Modifier.size(90.dp)
                                            )
                                        }
                                    }

                                    Spacer(Modifier.width(5.dp))

                                    Box(contentAlignment = Alignment.Center) {

                                        CropTextCardTitle(
                                            text = "${weather.main.temp.toInt()}°C",
                                            style = MaterialTheme.typography.displaySmall
                                        )

                                    }
                                }

                                Column(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .weight(1.3f),
                                    horizontalAlignment = Alignment.Start,
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    CropTextCardTitle(
                                        text = weather.name,
                                        style = MaterialTheme.typography.titleMedium
                                    )
                                    CropTextCardTitle(
                                        text = formatWeatherDescription(description = weather.weather.firstOrNull()?.description ?: "", context = context,),

                                        style = MaterialTheme.typography.titleMedium
                                    )
                                }
                            }
                        } ?: CircularProgressIndicator()
                    }
                }

                Spacer(Modifier.height(20.dp))

                //CARD STEPS------------------------------------------------------------------------------
                CropCard(
                    modifier = Modifier.height(300.dp),
                    textTitle = stringResource(R.string.home_worker_guide_card_title)

                ) {

                    Spacer(Modifier.height(15.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 15.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {

                            Icon(
                                painter = painterResource(R.drawable.ic_captura_pantalla),
                                contentDescription = "",
                                tint = Color.Unspecified,
                                modifier = Modifier.size(70.dp)
                            )
                            Box(
                                modifier = Modifier
                                    .width(80.dp)
                                    .height(90.dp)
                                    .padding(top = 8.dp)
                            )
                            {
                                CropTextCardDescriptionSmall(text = stringResource(R.string.home_worker_guide_first_step))
                            }
                        }

                        Column {
                            Box(
                                modifier = Modifier.height(70.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    painterResource(R.drawable.ic_right_arrow),
                                    contentDescription = "",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                            Box(
                                modifier = Modifier
                                    .height(90.dp)
                                    .padding(top = 8.dp)
                            )
                        }

                        Column {
                            Icon(
                                painter = painterResource(R.drawable.ic_plaga),
                                contentDescription = "",
                                tint = Color.Unspecified,
                                modifier = Modifier.size(70.dp)
                            )
                            Box(
                                modifier = Modifier
                                    .width(80.dp)
                                    .height(90.dp)
                                    .padding(top = 8.dp)
                            ) {
                                CropTextCardDescriptionSmall(text = stringResource(R.string.home_worker_guide_second_step))
                            }

                        }
                        Column {
                            Box(
                                modifier = Modifier.height(70.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    painterResource(R.drawable.ic_right_arrow),
                                    contentDescription = "",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                            Box(
                                modifier = Modifier
                                    .height(90.dp)
                                    .padding(top = 8.dp)
                            )
                        }
                        Column {
                            Icon(
                                painter = painterResource(R.drawable.ic_reporte),
                                contentDescription = "",
                                tint = Color.Unspecified,
                                modifier = Modifier.size(70.dp)
                            )
                            Box(
                                modifier = Modifier
                                    .width(80.dp)
                                    .height(90.dp)
                                    .padding(start = 8.dp)
                                    .padding(top = 8.dp)
                            ) {
                                CropTextCardDescriptionSmall(text = stringResource(R.string.home_worker_guide_third_step))
                            }
                        }
                    }
                    //Manage permissions
                    LaunchedEffect(permissionState.status) {
                        if (permissionState.status.isGranted && shouldNavigateToCamera) {
                            navigateToCamera()
                            shouldNavigateToCamera = false
                        }
                    }

                    CropButtonPrimary(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 3.dp)
                            .padding(horizontal = 15.dp),
                        text = stringResource(R.string.home_worker_camara_button),
                        onClick = {
                            if (permissionState.status.isGranted) {
                                navigateToCamera()
                            } else {
                                shouldNavigateToCamera = true
                                permissionState.launchPermissionRequest()
                            }
                        }
                    )
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_image_home),
                        contentDescription = "",
                        tint = Color.Unspecified
                    )
                }
            }
        }
    }
}






