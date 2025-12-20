package com.example.currentweatherdatabinding

data class WeatherData(
    val city: String,
    val temperature: Double,
    val feelsLike: Double,
    val description: String,
    val humidity: Int,
    val pressure: Int,
    val windSpeed: Double,
    val windDeg: Int,
    val icon: String,
    val isCelsius: Boolean = true
) {
    val temperatureCelsius: String
        get() = "${(temperature - 273.15).toInt()}°C"

    val temperatureFahrenheit: String
        get() = "${((temperature - 273.15) * 9/5 + 32).toInt()}°F"

    val feelsLikeCelsius: String
        get() = "${(feelsLike - 273.15).toInt()}°C"

    val feelsLikeFahrenheit: String
        get() = "${((feelsLike - 273.15) * 9/5 + 32).toInt()}°F"

    fun getTemperatureDisplay(isCelsius: Boolean): String {
        return if (isCelsius) temperatureCelsius else temperatureFahrenheit
    }

    fun getFeelsLikeDisplay(isCelsius: Boolean): String {
        return if (isCelsius) feelsLikeCelsius else feelsLikeFahrenheit
    }

    val descriptionCapitalized: String
        get() = description.replaceFirstChar { it.uppercase() }

    val humidityPercent: String
        get() = "$humidity%"

    val pressureHpa: String
        get() = "$pressure hPa"

    val windSpeedMs: String
        get() = "%.1f м/с".format(windSpeed)

    val windDirection: String
        get() = getWindDirection(windDeg)

    val windDirectionWithDegree: String
        get() = "${getWindDirection(windDeg)} ($windDeg°)"

    val iconUrl: String
        get() = "https://openweathermap.org/img/wn/${icon}@2x.png"

    private fun getWindDirection(degrees: Int): String {
        return when {
            degrees in 0..22 || degrees in 338..360 -> "Север"
            degrees in 23..67 -> "Северо-Восток"
            degrees in 68..112 -> "Восток"
            degrees in 113..157 -> "Юго-Восток"
            degrees in 158..202 -> "Юг"
            degrees in 203..247 -> "Юго-Запад"
            degrees in 248..292 -> "Запад"
            degrees in 293..337 -> "Северо-Запад"
            else -> "N/A"
        }
    }
}

// Response models for API
data class WeatherResponse(
    val name: String,
    val main: Main,
    val weather: List<Weather>,
    val wind: Wind
)

data class Main(
    val temp: Double,
    val feels_like: Double,
    val humidity: Int,
    val pressure: Int
)

data class Weather(
    val description: String,
    val icon: String
)

data class Wind(
    val speed: Double,
    val deg: Int = 0
)