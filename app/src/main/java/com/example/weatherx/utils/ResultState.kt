package com.example.weatherx.utils


sealed class ResultState<out T> {

    data object Idle : ResultState<Nothing>()
    data class Success<R>(val data: R) : ResultState<R>()
    data class Failure(val error: String) : ResultState<Nothing>()
    data object Loading : ResultState<Nothing>()

}