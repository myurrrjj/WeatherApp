package com.example.weatherapp.ui.Screens

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.EaseOutCubic
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.weatherapp.R
import com.example.weatherapp.data.CurrentWeatherResponse
import com.example.weatherapp.data.ForecastItem
import com.example.weatherapp.data.ForecastResponse
import com.example.weatherapp.ui.ViewModel.WeatherUiState
import com.example.weatherapp.ui.ViewModel.WeatherViewModel
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class, ExperimentalAnimationApi::class)
@Composable
fun WeatherScreen(
    viewModel: WeatherViewModel,
    onNavigateTo7day: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    var city by remember { mutableStateOf("") }





    Scaffold(
        bottomBar = {
            // The bottom bar is only shown after the initial data is loaded
            AnimatedVisibility(
                visible = uiState.currentWeather != null,
                enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
                exit = slideOutVertically(targetOffsetY = { it }) + fadeOut()
            ) {
                BottomAppBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    tonalElevation = 8.dp
                ) {
                    SharedSearchBar(city = city, onCityChange = { city = it }, viewModel = viewModel)
                }
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        AnimatedContent(
            targetState = uiState.currentWeather != null,
            modifier = Modifier.padding(paddingValues),
            transitionSpec = {
                fadeIn(animationSpec = tween(600)) togetherWith fadeOut(animationSpec = tween(300))
            },
            label = "contentAnimation"
        ) { dataLoaded ->
            if (dataLoaded) {
                // STATE: Data is loaded, show the full weather UI
                WeatherContentLoaded(uiState = uiState, onNavigateTo7day = onNavigateTo7day)
            } else {
                // STATE: Initial launch, show centered search bar
                InitialPrompt(
                    city = city,
                    onCityChange = { city = it },
                    viewModel = viewModel,
                    isLoading = uiState.isLoading,
                    error = uiState.error
                )
            }
        }
    }
}

@Composable
fun InitialPrompt(
    city: String,
    onCityChange: (String) -> Unit,
    viewModel: WeatherViewModel,
    isLoading: Boolean,
    error: String?
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        if (isLoading) {
            CircularProgressIndicator()
        } else {
            Text(
                text = "Use your location or search for a city to begin",
                style = MaterialTheme.typography.titleLarge,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 32.dp)
            )
            Spacer(modifier = Modifier.height(24.dp))
            SharedSearchBar(city = city, onCityChange = onCityChange, viewModel = viewModel)
            if (error != null) {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = error,
                    color = MaterialTheme.colorScheme.error,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
fun WeatherContentLoaded(uiState: WeatherUiState, onNavigateTo7day: () -> Unit) {
    val state = uiState
    if (state.currentWeather != null && state.forecast != null) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        ) {
            AnimatedVisibility(
                visible = true,
                modifier = Modifier.weight(1f),
                enter = slideInVertically(
                    initialOffsetY = { -it },
                    animationSpec = tween(durationMillis = 600, easing = EaseOutCubic)
                ) + fadeIn(animationSpec = tween(600))
            ) {
                CurrentWeatherCard(state.currentWeather)
            }

            AnimatedVisibility(
                visible = true,
                enter = slideInVertically(
                    initialOffsetY = { it / 2 },
                    animationSpec = tween(durationMillis = 600, delayMillis = 100, easing = EaseOutCubic)
                ) + fadeIn(animationSpec = tween(600, delayMillis = 100))
            ) {
                WeatherDetailsCard(state.currentWeather)
            }

            Spacer(modifier = Modifier.height(16.dp))

            AnimatedVisibility(
                visible = true,
                enter = slideInVertically(
                    initialOffsetY = { it },
                    animationSpec = tween(durationMillis = 600, delayMillis = 200, easing = EaseOutCubic)
                ) + fadeIn(animationSpec = tween(600, delayMillis = 200))
            ) {
                HourlyForecast(state.forecast)
            }

            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = onNavigateTo7day,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("View 7-day forecast")
            }
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@Composable
fun SharedSearchBar(
    city: String,
    onCityChange: (String) -> Unit,
    viewModel: WeatherViewModel
) {
    val keyboardController = LocalSoftwareKeyboardController.current
    val context = LocalContext.current
    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }
    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        if (permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true || permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true) {
            try {
                fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                    if (location != null) {
                        viewModel.fetchWeatherByLocation(location.latitude, location.longitude)
                    }
                }
            } catch (e: SecurityException) {
                // Handle exception
            }
        }
    }

    OutlinedTextField(
        value = city,
        onValueChange = onCityChange,
        label = { Text("Search for a city") },
        modifier = Modifier.fillMaxWidth(),
        singleLine = true,
        keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Search),
        keyboardActions = KeyboardActions(onSearch = {
            if (city.isNotBlank()) {
                viewModel.fetchWeather(city)
                keyboardController?.hide()
            }
        }),
        shape = RoundedCornerShape(24.dp),
        trailingIcon = {
            IconButton(onClick = {
                locationPermissionLauncher.launch(
                    arrayOf(
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    )
                )
            }) {
                Icon(Icons.Default.LocationOn, contentDescription = "Get current location")
            }
        }
    )
}
@Composable
fun CurrentWeatherCard(currentWeather: CurrentWeatherResponse) {
    val targetTemperature = currentWeather.main.temperature
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
                        colors = listOf(
                            MaterialTheme.colorScheme.primary,
                            MaterialTheme.colorScheme.secondary
                        )
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
                            color = MaterialTheme.colorScheme.onPrimary,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = SimpleDateFormat("EEEE, d MMMM", Locale.getDefault()).format(Date()),
                            color = MaterialTheme.colorScheme.onPrimary,
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
                        color = MaterialTheme.colorScheme.onPrimary,
                        fontSize = 72.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = currentWeather.weather.first().main,
                        color = MaterialTheme.colorScheme.onPrimary,
                        fontSize = 22.sp
                    )
                }
            }
        }
    }
}

@Composable
fun WeatherDetailsCard(currentWeather: CurrentWeatherResponse) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        modifier = Modifier.padding(vertical = 16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            DetailItem(
                iconResId = R.drawable.humidity,
                value = "${currentWeather.main.humidity}%",
                label = "Humidity"
            )
            DetailItem(
                iconResId = R.drawable.wind,
                value = "${currentWeather.wind.speed.roundToInt()} km/h",
                label = "Wind"
            )
            DetailItem(
                iconResId = R.drawable.gauge,
                value = "${currentWeather.main.pressure} hPa",
                label = "Pressure"
            )
        }
    }
}

@Composable
fun DetailItem(iconResId: Int, value: String, label: String) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            painter = painterResource(id = iconResId),
            contentDescription = label,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(28.dp)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(text = value, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MaterialTheme.colorScheme.onSurface)
        Text(text = label, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
    }
}


@Composable
fun HourlyForecast(forecast: ForecastResponse) {
    Column {
        Text(
            text = "Hourly Forecast",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(modifier = Modifier.height(8.dp))
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(vertical = 8.dp)
        ) {
            itemsIndexed(
                items = forecast.list.take(8),
                key = { _, item -> item.dateTime }
            ) { index, forecastItem ->
                var visible by remember { mutableStateOf(false) }
                LaunchedEffect(Unit) {
                    delay(index * 100L + 400L)
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
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(text = time, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            AsyncImage(
                model = "https://openweathermap.org/img/wn/${item.weather.first().icon}@2x.png",
                contentDescription = "Hourly Weather Icon",
                modifier = Modifier.size(48.dp)
            )
            Text(
                text = "${item.main.temperature.roundToInt()}°",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}
