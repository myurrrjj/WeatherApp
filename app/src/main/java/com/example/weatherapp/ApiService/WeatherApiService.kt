package com.example.weatherapp

import com.example.weatherapp.data.CurrentWeatherResponse
import com.example.weatherapp.data.ForecastResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface WeatherApiService {

    companion object {
        const val BASE_URL = "https://api.openweathermap.org/"
    }

    @GET("data/2.5/weather")
    suspend fun getCurrentWeather(@Query("q") city: String): CurrentWeatherResponse

    @GET("data/2.5/forecast")
    suspend fun getForecast(@Query("q") city: String): ForecastResponse

    @GET("data/2.5/weather")
    suspend fun getCurrentWeatherByCoords(@Query("lat") lat: Double, @Query("lon") lon: Double): CurrentWeatherResponse

    @GET("data/2.5/forecast")
    suspend fun getForecastByCoords(@Query("lat") lat: Double, @Query("lon") lon: Double): ForecastResponse
}
