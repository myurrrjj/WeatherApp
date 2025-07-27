package com.example.weatherapp.weatherRepository

import com.example.weatherapp.RetrofitClient
import com.example.weatherapp.data.CurrentWeatherResponse
import com.example.weatherapp.data.ForecastResponse


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class WeatherUiState(
    val currentWeather: CurrentWeatherResponse? = null,
    val forecast: ForecastResponse? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)

class WeatherRepository {
    private val apiService = RetrofitClient.apiService

    suspend fun getCurrentWeather(city: String): CurrentWeatherResponse {
        return apiService.getCurrentWeather(city)
    }

    suspend fun getForecast(city: String): ForecastResponse {
        return apiService.getForecast(city)
    }
}

class WeatherViewModel : ViewModel() {

    private val repository = WeatherRepository()

    private val _uiState = MutableStateFlow(WeatherUiState())
    val uiState = _uiState.asStateFlow()

    fun fetchWeather(city: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            try {
                val current = repository.getCurrentWeather(city)
                val forecastData = repository.getForecast(city)
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    currentWeather = current,
                    forecast = forecastData
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Failed to fetch weather data for '$city'. Please try again."
                )
            }
        }
    }
}

