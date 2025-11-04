package com.capstone.cropcare.view.core.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun DrawerNavigationItem(
    navigationItem: DrawerItem,
    selected: Boolean,
    onClick: () -> Unit

){
    Row (
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(size = 99.dp))
            .clickable{ onClick() }
            .background(
                color = if (selected) MaterialTheme.colorScheme.surfaceColorAtElevation(4.dp)
                else Color.Unspecified,
                shape = RoundedCornerShape(99.dp)
            )
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ){
        Icon(
            painter = painterResource(navigationItem.icon),
            contentDescription = "Navigation Item icon",
            tint = if (selected) MaterialTheme.colorScheme.primary
            else MaterialTheme.colorScheme.onSurface
        )
        Spacer(Modifier.width(12.dp))
        Text(
            text = navigationItem.title,
            color = if (selected) MaterialTheme.colorScheme.primary
            else MaterialTheme.colorScheme.surface,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
            lineHeight = 20.sp
        )

    }

}