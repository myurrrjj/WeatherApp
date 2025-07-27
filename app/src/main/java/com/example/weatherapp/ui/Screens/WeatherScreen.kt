package com.example.weatherapp

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.weatherapp.data.CurrentWeatherResponse
import com.example.weatherapp.data.ForecastItem
import com.example.weatherapp.data.ForecastResponse
import com.example.weatherapp.weatherRepository.WeatherViewModel
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.roundToInt

@Composable
fun WeatherScreen(viewModel: WeatherViewModel) {
    val uiState by viewModel.uiState.collectAsState()
    var city by remember { mutableStateOf("Delhi") }
    val keyboardController = LocalSoftwareKeyboardController.current

    LaunchedEffect(Unit) {
        viewModel.fetchWeather(city)
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color(0xFFF0F4F8)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                OutlinedTextField(
                    value = city,
                    onValueChange = { city = it },
                    label = { Text("Search for a city") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Search),
                    keyboardActions = KeyboardActions(onSearch = {
                        viewModel.fetchWeather(city)
                        keyboardController?.hide()
                    }),
                    shape = RoundedCornerShape(24.dp),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White,
                        disabledContainerColor = Color.White,
                        focusedIndicatorColor = Color(0xFF5C6BC0),
                    )
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Content based on state
                when {
                    uiState.isLoading -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    }
                    uiState.error != null -> {
                        Text(
                            text = uiState.error!!,
                            color = MaterialTheme.colorScheme.error,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth().padding(16.dp)
                        )
                    }
                    uiState.currentWeather != null && uiState.forecast != null -> {
                        CurrentWeatherCard(uiState.currentWeather!!)
                        Spacer(modifier = Modifier.height(24.dp))
                        HourlyForecast(uiState.forecast!!)
                    }
                }
            }
        }
    }
}

@Composable
fun CurrentWeatherCard(currentWeather: CurrentWeatherResponse) {
    Card(
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Box(
            modifier = Modifier
                .background(
                    Brush.linearGradient(
                        colors = listOf(Color(0xFF3F51B5), Color(0xFF5C6BC0))
                    )
                )
                .padding(24.dp)
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = currentWeather.cityName,
                            color = Color.White,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = SimpleDateFormat("EEEE, d MMMM", Locale.getDefault()).format(Date()),
                            color = Color.White,
                            fontSize = 16.sp
                        )
                    }
                    AsyncImage(
                        model = "https://openweathermap.org/img/wn/${currentWeather.weather.first().icon}@2x.png",
                        contentDescription = "Weather Icon",
                        modifier = Modifier.size(80.dp)
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "${currentWeather.main.temperature.roundToInt()}°",
                    color = Color.White,
                    fontSize = 72.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = currentWeather.weather.first().main,
                    color = Color.White,
                    fontSize = 22.sp
                )
            }
        }
    }
}

@Composable
fun HourlyForecast(forecast: ForecastResponse) {
    Column {
        Text(
            text = "Hourly Forecast",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF333333)
        )
        Spacer(modifier = Modifier.height(8.dp))
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(forecast.list.take(8)) { forecastItem ->
                HourlyForecastItem(forecastItem)
            }
        }
    }
}

@Composable
fun HourlyForecastItem(item: ForecastItem) {
    val timeFormatter = SimpleDateFormat("HH:mm", Locale.getDefault())
    timeFormatter.timeZone = TimeZone.getDefault()
    val time = timeFormatter.format(Date(item.dateTime * 1000L))

    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(text = time, fontSize = 14.sp, color = Color.DarkGray)
            AsyncImage(
                model = "https://openweathermap.org/img/wn/${item.weather.first().icon}@2x.png",
                contentDescription = "Hourly Weather Icon",
                modifier = Modifier.size(48.dp)
            )
            Text(
                text = "${item.main.temperature.roundToInt()}°",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF333333)
            )
        }
    }
}
