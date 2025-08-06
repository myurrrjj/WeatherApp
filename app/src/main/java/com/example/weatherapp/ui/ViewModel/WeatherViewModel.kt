package com.example.weatherapp.ui.ViewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.weatherapp.WeatherApplication
import com.example.weatherapp.data.DataManager
import com.example.weatherapp.weatherRepository.WeatherRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class WeatherViewModel(private val dataManager: DataManager,private  val weatherRepository: WeatherRepository) : ViewModel() {


    private val _uiState = MutableStateFlow(
        WeatherUiState(
            currentCity = ""
        )
    )
    val uiState = _uiState.asStateFlow()

init {
    viewModelScope.launch {
        val lastCity = dataManager.lastCityFlow.first()
        if(lastCity.isNotBlank()){
            fetchWeather(lastCity)
        }
    }
}
    fun fetchWeather(city: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            try {
                val current = weatherRepository.getCurrentWeather(city)
                val forecastData = weatherRepository.getForecast(city)
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    currentWeather = current,
                    forecast = forecastData
                )
                dataManager.saveLastCity(city)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Failed to fetch weather data for '$city'. Please try again."
                )
            }
        }
    }

    fun fetchWeatherByLocation(lat:Double,lon: Double){
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            try{
                val current = weatherRepository.getCurrentWeatherByCoords(lat, lon)
                val forecastData = weatherRepository.getForecastByCoords(lat, lon)
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    currentWeather = current,
                    forecast = forecastData
                )
                dataManager.saveLastCity(current.cityName)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Failed to fetch weather for your location. Please try again."
                )
            }
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = (this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as WeatherApplication)
                val weatherRepository = application.container.weatherRepository
                val dataManager = application.container.dataManager
                WeatherViewModel(
                    weatherRepository = weatherRepository,
                    dataManager = dataManager
                )
            }
        }
    }
}
