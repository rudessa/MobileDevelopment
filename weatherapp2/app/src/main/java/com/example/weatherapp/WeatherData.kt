package com.example.weatherapp

data class WeatherData(
    val cityName: String,
    val country: String,
    val temperature: Double,
    val feelsLike: Double,
    val humidity: Int,
    val windSpeed: Double,
    val cloudiness: Int,
    val description: String,
    val iconCode: String
)

data class CapitalCity(
    val name: String,
    val nameEn: String,
    val country: String
)