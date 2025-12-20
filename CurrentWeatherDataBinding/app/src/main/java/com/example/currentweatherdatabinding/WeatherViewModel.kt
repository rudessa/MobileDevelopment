package com.example.currentweatherdatabinding

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import java.util.Locale

class WeatherViewModel : ViewModel() {

    private val repository = WeatherRepository()

    private val _weatherData = MutableLiveData<WeatherData?>()
    val weatherData: LiveData<WeatherData?> = _weatherData

    private val _isLoading = MutableLiveData<Boolean>(false)
    val isLoading: LiveData<Boolean> = _isLoading

    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> = _errorMessage

    fun loadWeather(city: String) {
        Log.d("WeatherViewModel", "loadWeather called for city: $city")
        Log.d("WeatherViewModel", "isLoading current value: ${_isLoading.value}")

        if (_isLoading.value == true) {
            Log.d("WeatherViewModel", "Already loading, returning")
            return
        }

        viewModelScope.launch {
            try {
                Log.d("WeatherViewModel", "Starting to load weather")
                _isLoading.value = true
                _errorMessage.value = null

                Log.d("WeatherViewModel", "Calling repository.getWeather")
                val response = repository.getWeather(city)
                Log.d("WeatherViewModel", "Response received: $response")

                if (response != null) {
                    val weatherData = WeatherData(
                        city = response.name,
                        temperature = response.main.temp,
                        feelsLike = response.main.feels_like,
                        description = response.weather.firstOrNull()?.description?.replaceFirstChar {
                            if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString()
                        } ?: "N/A",
                        humidity = response.main.humidity,
                        pressure = response.main.pressure,
                        windSpeed = response.wind.speed,
                        icon = response.weather.firstOrNull()?.icon ?: ""
                    )
                    Log.d("WeatherViewModel", "Setting weatherData: $weatherData")
                    _weatherData.value = weatherData
                } else {
                    Log.e("WeatherViewModel", "Response is null")
                    _errorMessage.value = "Не удалось загрузить данные о погоде"
                    _weatherData.value = null
                }

            } catch (e: Exception) {
                Log.e("WeatherViewModel", "Exception occurred", e)
                _errorMessage.value = "Ошибка: ${e.message}"
                _weatherData.value = null
            } finally {
                _isLoading.value = false
                Log.d("WeatherViewModel", "Loading finished")
            }
        }
    }
}