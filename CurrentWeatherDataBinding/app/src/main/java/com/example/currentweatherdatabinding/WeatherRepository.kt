package com.example.currentweatherdatabinding

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

class WeatherRepository {

    private val apiKey = "56decce1208a6a9fdcf891f4fcf6b121"
    private val baseUrl = "https://api.openweathermap.org/data/2.5/weather"

    suspend fun getWeather(city: String): WeatherResponse? = withContext(Dispatchers.IO) {
        var connection: HttpURLConnection? = null
        try {
            val urlString = "$baseUrl?q=$city&appid=$apiKey"
            Log.d("WeatherRepository", "Requesting URL: $urlString")

            val url = URL(urlString)
            connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "GET"
            connection.connectTimeout = 15000
            connection.readTimeout = 15000

            Log.d("WeatherRepository", "Connecting...")
            val responseCode = connection.responseCode
            Log.d("WeatherRepository", "Response code: $responseCode")

            if (responseCode == HttpURLConnection.HTTP_OK) {
                val reader = BufferedReader(InputStreamReader(connection.inputStream))
                val response = reader.use { it.readText() }
                Log.d("WeatherRepository", "Response body: $response")
                parseWeatherResponse(response)
            } else {
                val errorStream = connection.errorStream
                val errorResponse = errorStream?.bufferedReader()?.use { it.readText() }
                Log.e("WeatherRepository", "Error response: $errorResponse")
                null
            }
        } catch (e: Exception) {
            Log.e("WeatherRepository", "Exception in getWeather", e)
            e.printStackTrace()
            null
        } finally {
            connection?.disconnect()
            Log.d("WeatherRepository", "Connection closed")
        }
    }

    private fun parseWeatherResponse(json: String): WeatherResponse {
        try {
            val jsonObject = JSONObject(json)

            val name = jsonObject.getString("name")

            val mainObject = jsonObject.getJSONObject("main")
            val main = Main(
                temp = mainObject.getDouble("temp"),
                feels_like = mainObject.getDouble("feels_like"),
                humidity = mainObject.getInt("humidity"),
                pressure = mainObject.getInt("pressure")
            )

            val weatherArray = jsonObject.getJSONArray("weather")
            val weather = mutableListOf<Weather>()
            for (i in 0 until weatherArray.length()) {
                val weatherObj = weatherArray.getJSONObject(i)
                weather.add(
                    Weather(
                        description = weatherObj.getString("description"),
                        icon = weatherObj.getString("icon")
                    )
                )
            }

            val windObject = jsonObject.getJSONObject("wind")
            val windDeg = if (windObject.has("deg")) {
                windObject.getInt("deg")
            } else {
                0
            }

            val wind = Wind(
                speed = windObject.getDouble("speed"),
                deg = windDeg
            )

            val result = WeatherResponse(
                name = name,
                main = main,
                weather = weather,
                wind = wind
            )
            Log.d("WeatherRepository", "Parsed response: $result")
            return result
        } catch (e: Exception) {
            Log.e("WeatherRepository", "Error parsing JSON", e)
            throw e
        }
    }
}