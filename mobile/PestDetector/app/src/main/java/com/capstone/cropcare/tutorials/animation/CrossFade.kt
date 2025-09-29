package com.capstone.cropcare.tutorials.animation

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.capstone.cropcare.tutorials.navigation.examples.DetailScreen
import com.capstone.cropcare.tutorials.navigation.examples.HomeScreen
import com.capstone.cropcare.tutorials.navigation.examples.LoginScreen


//usualmente se usa para cambiar entre vistas o pantallas / navegacion
@Composable
fun MyCrossFade(modifier: Modifier = Modifier) {

    var currentScreen: String by remember { mutableStateOf("") }

    Column {
        Row {
            Text(text = "Home", modifier = Modifier.clickable{currentScreen = "Home"})
            Text(text = "Detail", modifier = Modifier.clickable{currentScreen = "Detail"})
            Text(text = "Login", modifier = Modifier.clickable{currentScreen = "Login"})

        }

        Crossfade(targetState = currentScreen) { screen ->
            when (screen){
                "Home" -> HomeScreen(navigateBack = {}, navigateToDetail = {})
                "Detail" -> DetailScreen(id = "", navigateStart = {})
                "Login" -> LoginScreen {  }
            }
        }
    }
    
}