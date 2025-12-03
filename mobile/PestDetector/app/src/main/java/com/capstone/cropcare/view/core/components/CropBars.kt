package com.capstone.cropcare.view.core.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarColors
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.capstone.cropcare.R
import com.capstone.cropcare.view.ui.theme.BottomTextDisabled
import com.capstone.cropcare.view.ui.theme.customBtn
import com.capstone.cropcare.view.core.navigation.NavItems


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CropTopAppBar(
    modifier: Modifier = Modifier,
    navigationIcon: @Composable () -> Unit = {},
    colors: TopAppBarColors = TopAppBarDefaults.topAppBarColors(),
    onAvatarClick: () -> Unit = {}
) {
    TopAppBar(
        title = { CropTextTopBar(text = stringResource(R.string.app_name)) },
        modifier = modifier,
        navigationIcon = navigationIcon,
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = customBtn,
            navigationIconContentColor = Color.White
        ),
        actions = {
            Box(
                modifier = Modifier
                    .padding(end = 16.dp)
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(customBtn)
                    .clickable { onAvatarClick() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_user_placeholder), // Temporal
                    contentDescription = "Profile / Drawer",
                    tint = Color.White
                )
            }
        }
    )
}



@Composable
fun CropBottomBar(
    itemList: List<NavItems>,
    selectedIndex: Int,
    onItemSelected: (Int) -> Unit
) {
    NavigationBar(
        containerColor = customBtn,
        contentColor = Color.White,
        tonalElevation = 10.dp
    ) {
        itemList.forEachIndexed { index, item ->
            ItemsNavMenu(
                navItem = item,
                isSelected = index == selectedIndex
            ) {
                onItemSelected(index)
            }
        }
    }
}

@Composable
fun RowScope.ItemsNavMenu(navItem: NavItems, isSelected: Boolean, onItemClick: () -> Unit) {
    NavigationBarItem(
        selected = isSelected,
        onClick = { onItemClick() },
        colors = NavigationBarItemDefaults.colors(
            selectedIconColor = Color.White,
            selectedTextColor= Color.White,
            unselectedIconColor = BottomTextDisabled,
            unselectedTextColor = BottomTextDisabled
        ),
        icon = {
            Icon(
                painter = painterResource(id = navItem.icon),
                contentDescription = navItem.name
            )
        },
        label = { CropTextLabels(navItem.name) },
        alwaysShowLabel = false)
}

