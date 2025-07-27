package com.example.weatherapp

import com.example.weatherapp.data.CurrentWeatherResponse
import com.example.weatherapp.data.ForecastResponse
import okhttp3.HttpUrl
import okhttp3.OkHttpClient
import okhttp3.Request
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

interface WeatherApiService {

    @GET("data/2.5/weather")
    suspend fun getCurrentWeather(
        @Query("q") city: String
    ): CurrentWeatherResponse

    @GET("data/2.5/forecast")
    suspend fun getForecast(
        @Query("q") city: String
    ): ForecastResponse
}

object RetrofitClient {

    private const val API_KEY = "YOUR_API_KEY"
    private const val BASE_URL = "https://api.openweathermap.org/"

    private val okHttpClient: OkHttpClient = OkHttpClient.Builder().addInterceptor { chain ->
            val original: Request = chain.request()
            val originalHttpUrl: HttpUrl = original.url

            val url = originalHttpUrl.newBuilder()
                .addQueryParameter("appid", "9cd2c2b080d64d14bdfc8392917b4e8b")

                .addQueryParameter("units", "metric").build()

            val requestBuilder: Request.Builder = original.newBuilder().url(url)

            val request: Request = requestBuilder.build()
            chain.proceed(request)
        }.build()

    val apiService: WeatherApiService by lazy {
        Retrofit.Builder().baseUrl(BASE_URL).client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create()).build()
            .create(WeatherApiService::class.java)
    }
}
