package com.capstone.pestdetector.view.core.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.capstone.pestdetector.view.auth.login.LoginScreen

@Composable
fun NavigationWrapper(modifier: Modifier = Modifier) {
    val navController: NavHostController = rememberNavController()

    NavHost(navController = navController, startDestination = Login){
        composable<Login> {
            LoginScreen()
        }

        composable<Register> {
            //Register
        }
    }

}