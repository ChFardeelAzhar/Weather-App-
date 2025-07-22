package com.example.weatherx

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.outlined.Cloud
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.WaterDrop
import androidx.compose.material.icons.outlined.WindPower
import androidx.compose.material.icons.outlined.WorkspacePremium
import androidx.compose.material3.Card
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.weatherx.viewModel.WeatherViewModel
import com.example.weatherx.models.WeatherModel
import com.example.weatherx.utils.ResultState
import com.example.weatherx.models.HourlyWeatherData
import com.example.weatherx.ui.theme.SkyCloudColor
import com.example.weatherx.utils.formatDateTime
import com.example.weatherx.utils.getBackgroundForCondition
import com.example.weatherx.utils.getCurrentIcon
import com.example.weatherx.utils.getMainCardColor
import com.example.weatherx.utils.getSearchBarBackgroundColor
import com.example.weatherx.utils.getSearchBarBorderColor
import com.example.weatherx.utils.getSearchBarTextColor
import com.example.weatherx.utils.getSingleCardColor
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState

@OptIn(ExperimentalPermissionsApi::class)
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun WeatherScreen(
    weatherViewModel: WeatherViewModel = hiltViewModel()
) {

    val weatherState = weatherViewModel.weatherState.collectAsState()
    val cityName = remember { mutableStateOf("") }
    val showLoadingDialog = remember { mutableStateOf(false) }
    val context: Context = LocalContext.current

    val fetchedDataBySearch = remember { mutableStateOf<WeatherModel?>(null) }

    val weatherCondition =
        remember { mutableStateOf(fetchedDataBySearch.value?.current?.condition?.text ?: "") }

    val backgroundRes =
        remember { mutableIntStateOf(getBackgroundForCondition(weatherCondition.value)) }

    val bgMainContainerColor = remember { mutableStateOf(getMainCardColor(weatherCondition.value)) }

    var locationFetched by remember { mutableStateOf(false) }

    val location by weatherViewModel.locationData.collectAsState()

    val permissionState = rememberPermissionState(
        permission = Manifest.permission.ACCESS_FINE_LOCATION
    )

    LaunchedEffect(Unit) {
        if (permissionState.status.isGranted) {
            weatherViewModel.getCurrentLocation()
            getCurrentIcon(weatherCondition.value)
            getBackgroundForCondition(weatherCondition.value)

            location?.let {
                weatherViewModel.getWeatherByLocation(it.latitude, it.longitude)
            }

        } else {
            permissionState.launchPermissionRequest()
        }
    }




    LaunchedEffect(weatherCondition) {
        getBackgroundForCondition(weatherCondition.value)
        getCurrentIcon(weatherCondition.value)
        getMainCardColor(weatherCondition.value)
    }



    Scaffold {

        Box(
            modifier = Modifier
                .padding(it)
                .fillMaxSize()
        ) {
            // Background Image
            Image(
                painter = painterResource(backgroundRes.intValue),
                contentDescription = "default_bg",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.FillBounds,
            )

            // LazyColumn for scrolling content
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                item {
                    CitySearchBar(
                        modifier = Modifier,
                        cityName = cityName,
                        weatherCondition = weatherCondition.value,
                        onSearchClick = {
                            getCurrentIcon(weatherCondition.value)
                            getBackgroundForCondition(weatherCondition.value)
                            weatherViewModel.getWeather(it)
                        }
                    )
                }

                item {
                    MainCardContainer(
                        modifier = Modifier,
                        weatherData = fetchedDataBySearch.value,
                        weatherConditionImg = getCurrentIcon(weatherCondition.value),
                        color = getMainCardColor(condition = weatherCondition.value)
                    )
                }

                item {
                    DetailWeatherCard(
                        weatherData = fetchedDataBySearch.value,
                        color = getSingleCardColor(weatherCondition.value)
                    )
                }
            }

            // Handling different states (Loading, Success, Failure)
            when (val result = weatherState.value) {
                is ResultState.Loading -> {
                    showLoadingDialog.value = true
                }

                is ResultState.Failure -> {
                    Toast.makeText(context, result.error, Toast.LENGTH_SHORT).show()
                    showLoadingDialog.value = false
                }

                is ResultState.Success -> {
                    fetchedDataBySearch.value = result.data.copy()
                    weatherCondition.value = result.data.current.condition.text
                    backgroundRes.intValue = getBackgroundForCondition(weatherCondition.value)
                    showLoadingDialog.value = false
                }

                is ResultState.Idle -> {}
            }

            // Loading Indicator
            if (showLoadingDialog.value) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
        }

        if (locationFetched) {

            // call a function here for the weather with lat, lon

        } else {

        }
    }
}


@Composable
fun DefaultBackground(modifier: Modifier = Modifier, bgImage: MutableState<Int>) {

    Image(
        painter = painterResource(bgImage.value),
        contentDescription = "default_bg",
        modifier = modifier.fillMaxSize(),
        contentScale = ContentScale.FillBounds,
    )

}


@Composable
fun CitySearchBar(
    modifier: Modifier = Modifier,
    cityName: MutableState<String>,
    weatherCondition: String,
    onSearchClick: (String) -> Unit
) {
    val keyboardController = LocalSoftwareKeyboardController.current
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {

        // Search bar with rounded corners and dynamic colors
        OutlinedTextField(
            value = cityName.value,
            onValueChange = { cityName.value = it },
            placeholder = { 
                Text(
                    "Search City",
                    color = getSearchBarTextColor(weatherCondition).copy(alpha = 0.6f)
                )
            },
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    color = getSearchBarBackgroundColor(weatherCondition),
                    shape = RoundedCornerShape(30.dp)
                ),
            shape = RoundedCornerShape(30.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = getSearchBarBackgroundColor(weatherCondition),
                unfocusedContainerColor = getSearchBarBackgroundColor(weatherCondition),
                focusedBorderColor = getSearchBarBorderColor(weatherCondition),
                unfocusedBorderColor = getSearchBarBorderColor(weatherCondition).copy(alpha = 0.5f),
                focusedTextColor = getSearchBarTextColor(weatherCondition),
                unfocusedTextColor = getSearchBarTextColor(weatherCondition)
            ),
            trailingIcon = {
                IconButton(
                    onClick = {
                        if (cityName.value.isNotEmpty()) {
                            onSearchClick(cityName.value)
                            // Clear text to help dismiss keyboard
                            cityName.value = ""
                        }
                        keyboardController?.hide()
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search",
                        tint = getSearchBarBorderColor(weatherCondition)
                    )
                }
            },
            textStyle = TextStyle(
                color = getSearchBarTextColor(weatherCondition),
                fontSize = 16.sp
            )
        )

    }

}

@Composable
fun MainCardContainer(
    modifier: Modifier = Modifier,
    weatherData: WeatherModel?,
    weatherConditionImg: Int,
    color: Color
) {

    weatherData?.let { data ->

        Card(
            modifier = modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp)
            ,
            colors = CardDefaults.cardColors(containerColor = color),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Today",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(top = 10.dp),
                    color = Color.White
                )

                Spacer(Modifier.size(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {

                    /*
                    AsyncImage(
                        model = "https:${data.current.condition.icon}".replace("64x64", "128x128"),
                        contentDescription = "Weather Icon",
                        modifier = Modifier.size(100.dp)
                    )


                     */

                    Image(
                        painter = painterResource(weatherConditionImg),
                        contentDescription = "Weather Icon",
                        modifier = Modifier.size(80.dp),
                        colorFilter = ColorFilter.tint(color = Color.White)
                    )

                    Spacer(Modifier.size(15.dp))

                    Text(
                        text = "${data.current.temp_c}°",
                        fontSize = 65.sp,
                        color = Color.White,
                    )
                }

                Spacer(Modifier.size(15.dp))

                Text(
                    text = data.current.condition.text,
                    color = Color.White,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )

                Spacer(Modifier.size(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {

                    Image(
                        imageVector = Icons.Outlined.LocationOn,
                        contentDescription = "location icon",
                        colorFilter = ColorFilter.tint(
                            Color.White
                        ),
                        modifier = Modifier
                            .size(20.dp)
                            .align(alignment = Alignment.CenterVertically)

                    )

                    Spacer(Modifier.size(1.dp))

                    Text(
                        text = data.location.name,
                        color = Color.White,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        maxLines = 2
                    )

                    Spacer(Modifier.size(5.dp))

                    Text(
                        text = data.location.country,
                        color = Color.White.copy(alpha = .5f),
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier
                            .align(alignment = Alignment.CenterVertically)

                    )

                }

                Spacer(Modifier.size(15.dp))

                Text(
                    text = "${formatDateTime(data.current.last_updated).first}  |  ${
                        formatDateTime(
                            data.current.last_updated
                        ).second
                    }",
                    color = Color.White,
                    style = MaterialTheme.typography.titleMedium
                )

                Spacer(Modifier.size(15.dp))

                Text(
                    text = "Feels Like ${data.current.feelslike_c}",
                    color = Color.White,
                    style = MaterialTheme.typography.titleMedium
                )

            }
        }
    }
}


@Composable
fun HourlyWeatherDetailCard(hourlyData: List<HourlyWeatherData>) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(20.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = SkyCloudColor),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Next 24 Hours",
                style = MaterialTheme.typography.titleMedium,
                color = Color.White,
                modifier = Modifier.padding(bottom = 10.dp)
            )

            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Start
            ) {
                items(hourlyData) {
                    SingleHourlyWeatherDetail(it)
                }
            }
        }
    }
}


@Composable
fun SingleHourlyWeatherDetail(weather: HourlyWeatherData) {

    Column(
        modifier = Modifier.padding(7.dp),
        horizontalAlignment = Alignment.Start,
        verticalArrangement = Arrangement.Center
    ) {

        Text(
            text = weather.time,
            style = MaterialTheme.typography.titleMedium,
            color = Color.White
        )


        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = painterResource(weather.icon),
                contentDescription = null,
                modifier = Modifier.size(20.dp),
            )

            Spacer(Modifier.size(7.dp))

            Text(
                text = "${weather.temp}°",
                style = MaterialTheme.typography.titleMedium,
                color = Color.White
            )

        }
    }
}

@Composable
fun WeatherConditions(
    icon: ImageVector,
    title: String,
    value: String,
    color: Color
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape = RoundedCornerShape(16.dp)),
        colors = CardDefaults.cardColors(containerColor = color)

    ) {

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.Top
        ) {

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Start,
                verticalAlignment = Alignment.CenterVertically
            ) {

                Image(
                    imageVector = icon,
                    contentDescription = "icon",
                    colorFilter = ColorFilter.tint(Color.White.copy(alpha = 0.7f)),
                    modifier = Modifier.size(25.dp)
                )

                Spacer(modifier = Modifier.size(4.dp))

                Text(
                    text = title,
                    color = Color.White.copy(alpha = 0.7f),
                    style = MaterialTheme.typography.titleSmall
                )

            }

            Spacer(Modifier.size(15.dp))

            Text(
                value,
                color = Color.White,
                style = MaterialTheme.typography.titleLarge
            )
        }


    }

}

@Composable
fun DetailWeatherCard(weatherData: WeatherModel?, color: Color) {

    weatherData?.let { data ->

        val weatherConditions = listOf(
            WeatherConditionData(
                Icons.Outlined.Cloud,
                "Precipitation",
                "${data.current.precip_mm} mm"
            ),
            WeatherConditionData(
                Icons.Outlined.WindPower,
                "Wind Speed",
                "${data.current.wind_kph} km/h"
            ),
            WeatherConditionData(
                Icons.Outlined.WorkspacePremium,
                "UV Index",
                "${data.current.uv}"
            ),
            WeatherConditionData(
                Icons.Outlined.WaterDrop,
                "Humidity",
                "${data.current.humidity}%"
            ),
            WeatherConditionData(
                Icons.Outlined.Cloud,
                "Visibility",
                "${data.current.vis_km} km"
            ),
            WeatherConditionData(
                Icons.Outlined.WorkspacePremium,
                "Pressure",
                "${String.format("%.1f", data.current.pressure_mb)} mb"
            ),
            WeatherConditionData(
                Icons.Outlined.WaterDrop,
                "Dew Point",
                "${String.format("%.1f", data.current.dewpoint_c)}°C"
            ),
            WeatherConditionData(
                Icons.Outlined.Cloud,
                "Cloud Cover",
                "${data.current.cloud}%"
            ),
            WeatherConditionData(
                Icons.Outlined.WindPower,
                "Wind Direction",
                "${data.current.wind_dir} (${data.current.wind_degree}°)"
            ),
            WeatherConditionData(
                Icons.Outlined.WorkspacePremium,
                "Heat Index",
                "${String.format("%.1f", data.current.heatindex_c)}°C"
            ),
            WeatherConditionData(
                Icons.Outlined.WindPower,
                "Wind Gust",
                "${String.format("%.1f", data.current.gust_kph)} km/h"
            ),
            WeatherConditionData(
                Icons.Outlined.WaterDrop,
                "Wind Chill",
                "${String.format("%.1f", data.current.windchill_c)}°C"
            )
        )

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            colors = CardDefaults.cardColors(containerColor = Color.Transparent),
            shape = RoundedCornerShape(20.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(4.dp)
            ) {
                // Header
                Text(
                    text = "Weather Details",
                    color = Color.White,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                // Enhanced grid layout for more items
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.height(650.dp) // Adjust height for 6 rows
                ) {
                    items(weatherConditions.size) { index ->
                        WeatherConditions(
                            icon = weatherConditions[index].icon,
                            title = weatherConditions[index].title,
                            value = weatherConditions[index].value,
                            color = color
                        )
                    }
                }
            }
        }
    }
}

data class WeatherConditionData(
    val icon: ImageVector,
    val title: String,
    val value: String
)