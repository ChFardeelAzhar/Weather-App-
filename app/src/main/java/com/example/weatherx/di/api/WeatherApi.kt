package com.example.weatherx.di.api

import com.example.weatherx.models.WeatherModel
import com.example.weatherx.utils.API_KEY
import com.example.weatherx.utils.ResultState
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface WeatherApi {

    // https://api.weatherapi.com/v1/current.json? *key* =6aea62ca4a564da2a2d123341250602& *q* =London&aqi=no
    // it will take two parameters key (api key), q (query for location)

    @GET("v1/current.json")
    suspend fun getCurrentWeather(
        @Query("key") apiKey: String? = API_KEY,
        @Query("q") city: String?,
    ): Response<WeatherModel>


}