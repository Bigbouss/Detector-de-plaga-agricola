package com.capstone.cropcare.tutorials.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.capstone.cropcare.tutorials.navigation.examples.DetailScreen
import com.capstone.cropcare.tutorials.navigation.examples.HomeScreen
import com.capstone.cropcare.tutorials.navigation.examples.LoginScreen

@Composable
fun MyNavigationWrapper(modifier: Modifier = Modifier) {
    val navController: NavHostController = rememberNavController()
    NavHost(navController = navController, startDestination = Login){

        composable<Login>{
            LoginScreen(navigateToDetail = {navController.navigate(Home)})
        }

        composable<Home>{
            HomeScreen(
                navigateBack = {navController.popBackStack()},
                navigateToDetail = {id -> navController.navigate(Detail(id = id))})
        }

        composable <Detail>{ navBackStackEntry ->
            val detail = navBackStackEntry.toRoute<Detail>()
            DetailScreen(
                id =detail.id,
                navigateStart = {navController.navigate(Login){
                    popUpTo<Login>{inclusive = true }  //de esta manera se evita el apilamiento de navegación
                } }
            )
        }
    }
}