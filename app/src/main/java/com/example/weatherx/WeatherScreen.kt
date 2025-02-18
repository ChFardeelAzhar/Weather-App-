package com.example.weatherx

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


import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material3.Card
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.weatherx.viewModel.WeatherViewModel
import com.example.weatherx.models.WeatherModel
import com.example.weatherx.utils.ResultState
import com.example.weatherx.models.HourlyWeatherData
import com.example.weatherx.ui.theme.SkyCloudColor
import com.example.weatherx.utils.formatDateTime
import com.example.weatherx.utils.getBackgroundForCondition
import com.example.weatherx.utils.getCurrentIcon

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun WeatherScreen(
    weatherViewModel: WeatherViewModel = hiltViewModel()
) {

    var cityName = remember { mutableStateOf("") }
    var showLoadingDialog = remember { mutableStateOf(false) }
    val context: Context = LocalContext.current
    val weatherState = weatherViewModel.weatherState.collectAsState()

    val fetchedDataBySearch = remember { mutableStateOf<WeatherModel?>(null) }

    val weatherCondition =
        remember { mutableStateOf(fetchedDataBySearch.value?.current?.condition?.text ?: "") }
    val backgroundRes =
        remember { mutableIntStateOf(getBackgroundForCondition(weatherCondition.value)) }


    LaunchedEffect(weatherCondition) {
        getBackgroundForCondition(weatherCondition.value)
        getCurrentIcon(weatherCondition.value)
    }


    Scaffold(
        modifier = Modifier.fillMaxSize()
    ) {
        ConstraintLayout {

            val (bg, body) = createRefs()

            DefaultBackground(
                modifier = Modifier.constrainAs(bg) {
                    top.linkTo(parent.top)
                    start.linkTo(parent.start)
                    end.linkTo(parent.end)
                    bottom.linkTo(parent.bottom)
                },
                bgImage = backgroundRes
            )

            LazyColumn(
                modifier = Modifier
                    .constrainAs(body)
                    {
                        top.linkTo(parent.top)
                        start.linkTo(parent.start)
                        end.linkTo(parent.end)
                    }
                    .padding(it)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                item {
                    CitySearchBar(
                        modifier = Modifier,
                        cityName = cityName,
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
                        weatherData = fetchedDataBySearch.value, // Initially null, updates dynamically
                        weatherConditionImg = getCurrentIcon(weatherCondition.value)
                    )
                }


            }

            /*

            CitySearchBar(
                modifier = Modifier
                    .constrainAs(citySearchBar) {
                        top.linkTo(bg.top)
                        start.linkTo(bg.start)
                        end.linkTo(bg.end)

                    }
                    .padding(it),
                cityName = cityName,
                onSearchClick = {
                    weatherViewModel.getWeather(it)
                }
            )

            MainCardContainer(
                modifier = Modifier.constrainAs(detailCard) {
                    top.linkTo(citySearchBar.bottom, margin = 20.dp) // Ensuring proper spacing
                    start.linkTo(bg.start)
                    end.linkTo(bg.end)
                    bottom.linkTo(citySearchBar.top)
                },
                weatherData = fetchedDataBySearch.value // Initially null, updates dynamically
            )


             */

            // Weather Data or Error/Loading States
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

            if (showLoadingDialog.value) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
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
    onSearchClick: (String) -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {

        // Search bar with rounded corners
        OutlinedTextField(
            value = cityName.value,
            onValueChange = { cityName.value = it },
            placeholder = { Text("Search City") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            shape = RoundedCornerShape(30.dp),
            trailingIcon = {
                IconButton(
                    onClick = {
                        if (cityName.value.isNotEmpty()) {
                            onSearchClick(cityName.value)
                        }

                    }
                ) {
                    Icon(imageVector = Icons.Default.Search, contentDescription = "Search")
                }
            },
            textStyle = TextStyle(color = Color.DarkGray.copy(alpha = 0.7f))
        )

    }

}

@Composable
fun MainCardContainer(
    modifier: Modifier = Modifier,
    weatherData: WeatherModel?,
    weatherConditionImg: Int
) {

    weatherData?.let { data ->
        Card(
            modifier = modifier
                .fillMaxWidth()
                .padding(20.dp),
            elevation = CardDefaults.cardElevation(4.dp),
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
                    text = "Today",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(top = 10.dp),
                    color = Color.White
                )

                Spacer(Modifier.size(15.dp))

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
                        modifier = Modifier.size(80.dp)
                    )

                    Spacer(Modifier.size(15.dp))

                    Text(
                        text = "${data.current.temp_c}°",
                        fontSize = 65.sp,
                        color = Color.White,
                    )
                }

                Spacer(Modifier.size(25.dp))

                Text(
                    text = data.current.condition.text,
                    color = Color.White,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )

                Spacer(Modifier.size(25.dp))

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
                        fontWeight = FontWeight.Bold
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

                Spacer(Modifier.size(25.dp))

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
        elevation = CardDefaults.cardElevation(4.dp),
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
                    SingleWeatherDetail(it)
                }
            }
        }
    }
}


@Composable
fun SingleWeatherDetail(weather: HourlyWeatherData) {

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