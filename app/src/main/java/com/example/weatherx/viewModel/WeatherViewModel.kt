package com.example.weatherx.viewModel


import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.weatherx.di.api.WeatherApi
import com.example.weatherx.utils.ResultState
import com.example.weatherx.models.WeatherModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class WeatherViewModel @Inject constructor(
    private val weatherApi: WeatherApi
) : ViewModel() {


    private val apikey = "6aea62ca4a564da2a2d123341250602"
    private val _weatherState = MutableStateFlow<ResultState<WeatherModel>>(ResultState.Idle)
    val weatherState: StateFlow<ResultState<WeatherModel>> get() = _weatherState

    fun getWeather(city: String) {

        _weatherState.value = ResultState.Loading

        viewModelScope.launch {
            try {

                val response = weatherApi.getCurrentWeather(apiKey = apikey, city = city)
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

}