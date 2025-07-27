package com.example.weatherapp.Dependency

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import com.example.weatherapp.WeatherAppTheme
import com.example.weatherapp.WeatherScreen
import com.example.weatherapp.weatherRepository.WeatherViewModel

//class MainActivity : ComponentActivity() {
//    private val weatherViewModel: WeatherViewModel by viewModels()
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        setContent {
//            WeatherAppTheme {
//                WeatherScreen(weatherViewModel)
//            }
//        }
//    }
//}