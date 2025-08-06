package com.example.weatherapp.weatherRepository

import com.example.weatherapp.WeatherApiService
import com.example.weatherapp.data.CurrentWeatherResponse
import com.example.weatherapp.data.ForecastResponse


interface WeatherRepository {

    suspend fun getCurrentWeather(city: String): CurrentWeatherResponse

    suspend fun getForecast(city: String): ForecastResponse

    suspend fun getCurrentWeatherByCoords(lat: Double,lon: Double) : CurrentWeatherResponse

    suspend fun getForecastByCoords(lat: Double,lon: Double) : ForecastResponse
}

class DefaultWeatherRepository(private val weatherApiService: WeatherApiService) :
    WeatherRepository {
    override suspend fun getCurrentWeather(city: String): CurrentWeatherResponse {
        return weatherApiService.getCurrentWeather(city)

    }

    override suspend fun getForecast(city: String): ForecastResponse {
        return weatherApiService.getForecast(city)
    }


   override suspend fun getCurrentWeatherByCoords(lat: Double, lon: Double): CurrentWeatherResponse {
        return weatherApiService.getCurrentWeatherByCoords(lat, lon)
    }

   override suspend fun getForecastByCoords(lat: Double, lon: Double): ForecastResponse {
        return weatherApiService.getForecastByCoords(lat, lon)
    }


}




