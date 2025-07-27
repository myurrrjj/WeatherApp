package com.example.weatherapp.ui.Screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
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
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.time.format.TextStyle
import java.util.*
import kotlin.math.roundToInt


@OptIn(ExperimentalMaterial3Api::class, ExperimentalAnimationApi::class)
@Composable
fun WeatherScreen(viewModel: WeatherViewModel) {
    val uiState by viewModel.uiState.collectAsState()
    var city by remember { mutableStateOf("Delhi") }
    val keyboardController = LocalSoftwareKeyboardController.current

    LaunchedEffect(Unit) {
        viewModel.fetchWeather(city)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Weather App", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent,
                    titleContentColor = Color(0xFF333333)
                )
            )
        },
        bottomBar = {
            BottomAppBar(
                containerColor = Color.White,
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                tonalElevation = 8.dp
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
            }
        },
        containerColor = Color(0xFFF0F4F8)
    ) { paddingValues ->
        // TODO: ANIMATION - Crossfade between loading, error, and content states.
        Crossfade(targetState = uiState, animationSpec = tween(500), label = "main_crossfade") { state ->
            when {
                state.isLoading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
                state.error != null -> {
                    Text(
                        text = state.error!!,
                        color = MaterialTheme.colorScheme.error,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(paddingValues)
                            .padding(16.dp)
                    )
                }
                state.currentWeather != null && state.forecast != null -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(paddingValues)
                            .padding(horizontal = 16.dp)
                    ) {
                        Spacer(modifier = Modifier.height(16.dp))
                        // TODO: ANIMATION - Main weather card slides and fades in from the top.
                        AnimatedVisibility(
                            visible = true,
                            modifier = Modifier.weight(1f),
                            enter = slideInVertically(
                                initialOffsetY = { -it },
                                animationSpec = tween(durationMillis = 600, easing = EaseOutCubic)
                            ) + fadeIn(animationSpec = tween(600))
                        ) {
                            CurrentWeatherCard(state.currentWeather!!)
                        }
                        Spacer(modifier = Modifier.height(24.dp))
                        // TODO: ANIMATION - Hourly forecast section slides and fades in from the bottom.
                        AnimatedVisibility(
                            visible = true,
                            enter = slideInVertically(
                                initialOffsetY = { it },
                                animationSpec = tween(durationMillis = 600, delayMillis = 200, easing = EaseOutCubic)
                            ) + fadeIn(animationSpec = tween(600, delayMillis = 200))
                        ) {
                            HourlyForecast(state.forecast!!)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CurrentWeatherCard(currentWeather: CurrentWeatherResponse) {
    val targetTemperature = currentWeather.main.temperature
    // TODO: ANIMATION - Temperature value animates as a counter from 0 to the target value.
    val animatedTemperature = remember { Animatable(0f) }

    LaunchedEffect(targetTemperature) {
        animatedTemperature.animateTo(
            targetValue = targetTemperature.toFloat(),
            animationSpec = tween(durationMillis = 1500, easing = LinearOutSlowInEasing)
        )
    }

    Card(
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxSize(),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.linearGradient(
                        colors = listOf(Color(0xFF3F51B5), Color(0xFF5C6BC0))
                    )
                )
                .padding(24.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxHeight(),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
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
                        model = "https://openweathermap.org/img/wn/${currentWeather.weather.first().icon}@4x.png",
                        contentDescription = "Weather Icon",
                        modifier = Modifier.size(80.dp)
                    )
                }

                Column {
                    Text(
                        text = "${animatedTemperature.value.roundToInt()}°",
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
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(vertical = 8.dp)
        ) {
            // TODO: ANIMATION - Each forecast item animates in with a staggered delay.
            itemsIndexed(
                items = forecast.list.take(10),
                key = { _, item -> item.dateTime }
            ) { index, forecastItem ->
                var visible by remember { mutableStateOf(false) }
                LaunchedEffect(Unit) {
                    delay(index * 100L + 400L) // Staggered delay
                    visible = true
                }
                AnimatedVisibility(
                    visible = visible,
                    enter = fadeIn(animationSpec = tween(500)) + slideInVertically(animationSpec = tween(500))
                ) {
                    HourlyForecastItem(forecastItem)
                }
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
        border = BorderStroke(1.dp, Color(0xFFE0E0E0))
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

