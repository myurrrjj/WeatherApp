package com.example.weatherapp

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.weatherapp.ui.Screens.SevenDayForecastScreen
import com.example.weatherapp.ui.Screens.WeatherScreen
import com.example.weatherapp.ui.ViewModel.WeatherViewModel

enum class AppDestinations {
    MainScreen, SevenDayScreen

}

@Composable
fun AppNavHost(navController: NavHostController, modifier: Modifier = Modifier) {
    val viewModel: WeatherViewModel = viewModel(factory = WeatherViewModel.Companion.Factory)
    NavHost(
        navController = navController, startDestination = AppDestinations.MainScreen.name,
        enterTransition = {
            fadeIn(animationSpec = tween(700)) + slideIntoContainer(
                AnimatedContentTransitionScope.SlideDirection.Start,
                animationSpec = tween(700)
            )
        },
        exitTransition = {
            fadeOut(animationSpec = tween(700)) + slideOutOfContainer(
                AnimatedContentTransitionScope.SlideDirection.Start,
                animationSpec = tween(700)
            )
        },
        popEnterTransition = {
            fadeIn(animationSpec = tween(700)) + slideIntoContainer(
                AnimatedContentTransitionScope.SlideDirection.End,
                animationSpec = tween(700)
            )
        },
        popExitTransition = {
            fadeOut(animationSpec = tween(700)) + slideOutOfContainer(
                AnimatedContentTransitionScope.SlideDirection.End,
                animationSpec = tween(700)
            )
        }) {
        composable(route = AppDestinations.MainScreen.name) {
            WeatherScreen(
                viewModel,
                onNavigateTo7day = { navController.navigate(AppDestinations.SevenDayScreen.name) })
        }
        composable(route = AppDestinations.SevenDayScreen.name) {
            SevenDayForecastScreen(viewModel, navigateBack = {navController.navigateUp()})
        }

    }

}