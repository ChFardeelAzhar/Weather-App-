package com.example.weatherx.viewModel


import android.Manifest
import android.app.Application
import android.content.pm.PackageManager
import android.location.Location
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.weatherx.di.api.WeatherApi
import com.example.weatherx.utils.ResultState
import com.example.weatherx.models.WeatherModel
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class WeatherViewModel @Inject constructor(
    private val weatherApi: WeatherApi,
    private val application: Application
) : ViewModel() {


    private val _weatherState = MutableStateFlow<ResultState<WeatherModel>>(ResultState.Idle)
    val weatherState: StateFlow<ResultState<WeatherModel>> get() = _weatherState

    private val fusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(application)

    private val _locationData = MutableStateFlow<Location?>(null)
    val locationData: StateFlow<Location?> = _locationData


    fun getCurrentLocation() {
        val locationRequest = LocationRequest.Builder(
            Priority.PRIORITY_HIGH_ACCURACY, 1000
        ).build()

        if (ActivityCompat.checkSelfPermission(
                application.applicationContext,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED ||
            ActivityCompat.checkSelfPermission(
                application.applicationContext,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            fusedLocationProviderClient.getCurrentLocation(
                Priority.PRIORITY_HIGH_ACCURACY,
                null
            ).addOnSuccessListener { location ->
                if (location != null) {
                    _locationData.value = location
                    getWeatherByLocation(
                        location.latitude,
                        location.longitude
                    ) // ✅ Auto-fetch weather
                } else {
                    Log.d("location", "Location is null")
                }
            }.addOnFailureListener { exception ->
                Log.d("location", "Failed to get location: ${exception.message}")
            }
        }
    }


    fun getWeather(city: String) {

        _weatherState.value = ResultState.Loading

        viewModelScope.launch {
            try {
                val response = weatherApi.getCurrentWeather(city = city)
                if (response.isSuccessful) {
                    response.body()?.let {
                        _weatherState.value = ResultState.Success(it)
                        Log.d("response", "Response : ${response.body().toString()}")
                    }
                } else {
                    _weatherState.value = ResultState.Failure("Failed to load data")
                    Log.d("response", " Error : ${response.message()}")
                }
            } catch (e: Exception) {
                Log.d("error", e.message.toString())
                _weatherState.value = ResultState.Idle
            }
        }
    }

    fun getWeatherByLocation(latitude: Double, longitude: Double) {
        _weatherState.value = ResultState.Loading

        viewModelScope.launch {
            try {
                val response = weatherApi.getCurrentWeather(
                    city = "$latitude,$longitude"
                )
                if (response.isSuccessful) {
                    response.body()?.let {
                        _weatherState.value = ResultState.Success(it)
                        Log.d("response", "Response : ${response.body().toString()}")
                    }
                } else {
                    _weatherState.value = ResultState.Failure("Failed to load data")
                    Log.d("response", "Error : ${response.message()}")
                }
            } catch (e: Exception) {
                Log.d("error", e.message.toString())
                _weatherState.value = ResultState.Idle
            }
        }
    }


}