package com.capstone.cropcare.tutorials.componentes

import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import com.capstone.cropcare.R

@Composable
fun MyBadge (modifier: Modifier = Modifier) {

    Badge(){
        Text("4")
    }
    
}

@Composable
fun MyBadgeBox(modifier: Modifier = Modifier) {

    BadgedBox(badge = { MyBadge() }) {
        Icon(painter = painterResource(R.drawable.ic_launcher_foreground), contentDescription = "")
    }

}