package com.example.weatherapp.Dependency

import android.content.Context
import com.example.weatherapp.WeatherApiService
import com.example.weatherapp.WeatherApiService.Companion.BASE_URL
import com.example.weatherapp.data.DataManager
import com.example.weatherapp.weatherRepository.DefaultWeatherRepository
import com.example.weatherapp.weatherRepository.WeatherRepository
import okhttp3.HttpUrl
import okhttp3.OkHttpClient
import okhttp3.Request
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory




interface AppContainer {
    val weatherApiService: WeatherApiService
    val weatherRepository: WeatherRepository
    val dataManager: DataManager
}

class DefaultAppContainer(private val context: Context) : AppContainer {

    private val API_KEY = "9cd2c2b080d64d14bdfc8392917b4e8b"

    override val weatherApiService: WeatherApiService by lazy {
        Retrofit.Builder()
            .addConverterFactory(GsonConverterFactory.create())
            .baseUrl(BASE_URL).client(okHttpClient)
            .build()
            .create(WeatherApiService::class.java)

    }

    private val okHttpClient: OkHttpClient = OkHttpClient.Builder().addInterceptor { chain ->
        val original: Request = chain.request()
        val originalHttpUrl: HttpUrl = original.url
        val url = originalHttpUrl.newBuilder()
            .addQueryParameter("appid", API_KEY)
            .addQueryParameter("units", "metric").build()
        val requestBuilder: Request.Builder = original
            .newBuilder()
            .url(url)
        val request: Request = requestBuilder.build()
        chain.proceed(request)
    }.build()


    override val weatherRepository: WeatherRepository by lazy {
        DefaultWeatherRepository(weatherApiService)
    }
    override val dataManager: DataManager by lazy {
        DataManager(context)
    }

}



