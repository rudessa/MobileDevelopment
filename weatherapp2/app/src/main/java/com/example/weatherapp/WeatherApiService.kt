package com.example.weatherapp

import retrofit2.http.GET
import retrofit2.http.Query

interface WeatherApiService {

    @GET("weather")
    suspend fun getWeatherByCity(
        @Query("q") city: String,
        @Query("appid") apiKey: String,
        @Query("units") units: String = "metric",
        @Query("lang") lang: String = "ru"
    ): WeatherResponse

    @GET("forecast")
    suspend fun getForecastByCity(
        @Query("q") city: String,
        @Query("appid") apiKey: String,
        @Query("units") units: String = "metric",
        @Query("lang") lang: String = "ru"
    ): ForecastResponse
}

data class WeatherResponse(
    val name: String,
    val sys: SysResponse,
    val main: MainResponse,
    val wind: WindResponse,
    val clouds: CloudsResponse,
    val weather: List<WeatherDescription>
)

data class SysResponse(val country: String)

data class MainResponse(
    val temp: Double,
    val feels_like: Double,
    val humidity: Int
)

data class WindResponse(val speed: Double)

data class CloudsResponse(val all: Int)

data class WeatherDescription(
    val description: String,
    val icon: String
)

data class ForecastResponse(
    val city: ForecastCityResponse,
    val list: List<ForecastItemResponse>
)

data class ForecastCityResponse(
    val name: String,
    val country: String
)

data class ForecastItemResponse(
    val dt_txt: String,
    val main: MainResponse,
    val weather: List<WeatherDescription>,
    val wind: WindResponse,
    val clouds: CloudsResponse
)
