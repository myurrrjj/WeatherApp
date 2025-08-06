package com.example.weatherapp.ui.ViewModel

import com.example.weatherapp.data.CurrentWeatherResponse
import com.example.weatherapp.data.ForecastResponse

data class WeatherUiState(
    val currentWeather: CurrentWeatherResponse? = null,
    var currentCity : String,
    val forecast: ForecastResponse? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)
