package com.capstone.cropcare.view.core.components


import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer

import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.capstone.cropcare.R
import com.capstone.cropcare.view.core.navigation.DrawerItem
import com.capstone.cropcare.view.core.navigation.DrawerNavigationItem
import com.capstone.cropcare.view.ui.theme.customBtn

@Composable
fun CropDrawer(
    selectedDrawerNavigationItem: DrawerItem,
    onNavigationItemClick: (DrawerItem) -> Unit,
    onCloseClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxHeight()
            .fillMaxWidth(fraction = 0.6f)
            .background(customBtn)
            .padding(horizontal = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 24.dp)
        ) {
            IconButton(onClick = onCloseClick) {
                Icon(
                    painter = painterResource(R.drawable.ic_arrow_back),
                    contentDescription = "Back Arrow Icon",
                    tint = Color.White
                )
            }
        }
        Spacer(modifier = Modifier.height(24.dp))
        Box(
            modifier = Modifier.size(100.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.primaryContainer)

        ){
            //-> FOTO
        }
        Spacer(modifier = Modifier.height(40.dp))
        DrawerItem.entries.toTypedArray().take(3).forEach { navigationItem ->
            DrawerNavigationItem(
                navigationItem = navigationItem,
                selected = navigationItem == selectedDrawerNavigationItem,
                onClick = { onNavigationItemClick(navigationItem) }
            )
            Spacer(modifier = Modifier.height(4.dp))
        }
        Spacer(modifier = Modifier.weight(1f))
//        DrawerItem.entries.toTypedArray().takeLast(1).forEach { navigationItem ->
//            DrawerNavigationItem(
//                navigationItem = navigationItem,
//                selected = false,
//                onClick = {
//                    when (navigationItem) {
//                        DrawerItem.Profile -> {
//                            onNavigationItemClick(DrawerItem.Profile)
//                        }
//
//                        else -> {}
//                    }
//                }
//            )
//        }
        Spacer(modifier = Modifier.height(24.dp))
    }
}