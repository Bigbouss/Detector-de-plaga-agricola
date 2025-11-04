package com.capstone.cropcare.view.core.components

import android.annotation.SuppressLint
import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.capstone.cropcare.view.core.navigation.DrawerItem
import com.capstone.cropcare.view.utils.coloredShadow
import kotlin.math.roundToInt

@Composable
fun MainScreenRightDrawer() {
    var drawerState by remember { mutableStateOf(CropDrawerCustomState.Closed) }
    var selectedNavigationItem by remember { mutableStateOf(DrawerItem.Profile) }

    val configuration = LocalConfiguration.current
    val density = LocalDensity.current.density

    val screenWidth = remember {
        derivedStateOf { (configuration.screenWidthDp * density).roundToInt() }
    }
    val offsetValue by remember { derivedStateOf { (screenWidth.value / 4.5).dp } }
    val animatedOffset by animateDpAsState(
        targetValue = if (drawerState.isOpened()) -offsetValue else 0.dp, // 👈 Invertimos
        label = "Animated Offset"
    )
    val animatedScale by animateFloatAsState(
        targetValue = if (drawerState.isOpened()) 0.9f else 1f,
        label = "Animated Scale"
    )

    BackHandler(enabled = drawerState.isOpened()) {
        drawerState = CropDrawerCustomState.Closed
    }

    Box(
        modifier = Modifier
            .background(MaterialTheme.colorScheme.surface)
            .statusBarsPadding()
            .navigationBarsPadding()
            .fillMaxSize()
    ) {
        // 🔹 Drawer alineado a la derecha
        Box(
            modifier = Modifier
                .align(Alignment.CenterEnd)
        ) {
            CropDrawer(
                selectedDrawerNavigationItem = selectedNavigationItem,
                onNavigationItemClick = {
                    selectedNavigationItem = it
                    // Aquí ejecutas logout, por ahora simple:
                    if (it == DrawerItem.Profile) {
                        println("Cierre de sesión")
                    }
                    drawerState = CropDrawerCustomState.Closed
                },
                onCloseClick = { drawerState = CropDrawerCustomState.Closed }
            )
        }

        // 🔹 Contenido principal
        MainContentRightDrawer(
            modifier = Modifier
                .offset(x = animatedOffset)
                .scale(scale = animatedScale)
                .coloredShadow(
                    color = Color.Black,
                    alpha = 0.1f,
                    shadowRadius = 50.dp
                ),
            drawerState = drawerState,
            onDrawerClick = { drawerState = it },
        )
    }
}


@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainContentRightDrawer(
    modifier: Modifier = Modifier,
    drawerState: CropDrawerCustomState,
    onDrawerClick: (CropDrawerCustomState) -> Unit
) {
    Scaffold(
        modifier = modifier
            .clickable(enabled = drawerState == CropDrawerCustomState.Opened) {
                onDrawerClick(CropDrawerCustomState.Closed)
            },
        topBar = {
            TopAppBar(
                title = { Text(text = "Home") },
                navigationIcon = {
                    IconButton(onClick = { onDrawerClick(drawerState.opposite()) }) {
                        Icon(
                            imageVector = Icons.Default.Menu,
                            contentDescription = "Menu Icon"
                        )
                    }
                }
            )
        }
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Home",
                fontSize = MaterialTheme.typography.titleMedium.fontSize,
                fontWeight = FontWeight.Medium
            )
        }
    }
}