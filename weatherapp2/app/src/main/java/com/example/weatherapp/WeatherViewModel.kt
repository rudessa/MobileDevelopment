package com.example.weatherapp

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

sealed class WeatherUiState {
    object Idle : WeatherUiState()
    object Loading : WeatherUiState()
    data class Success(val data: WeatherData) : WeatherUiState()
    data class Error(val message: String) : WeatherUiState()
}

sealed class ForecastUiState {
    object Idle : ForecastUiState()
    object Loading : ForecastUiState()
    data class Success(val data: ForecastData) : ForecastUiState()
    data class Error(val message: String) : ForecastUiState()
}

class WeatherViewModel : ViewModel() {

    private val repository = WeatherRepository()

    private val _weatherState = MutableLiveData<WeatherUiState>(WeatherUiState.Idle)
    val weatherState: LiveData<WeatherUiState> = _weatherState

    private val _forecastState = MutableLiveData<ForecastUiState>(ForecastUiState.Idle)
    val forecastState: LiveData<ForecastUiState> = _forecastState

    private val _capitalWeather = MutableLiveData<Map<String, WeatherData>>()
    val capitalWeather: LiveData<Map<String, WeatherData>> = _capitalWeather

    val capitals: List<CapitalCity> = repository.getCapitals()

    fun searchCity(cityName: String) {
        if (cityName.isBlank()) return
        _weatherState.value = WeatherUiState.Loading
        _forecastState.value = ForecastUiState.Loading
        viewModelScope.launch {
            val weatherResult = repository.getWeather(cityName)
            _weatherState.value = weatherResult.fold(
                onSuccess = { WeatherUiState.Success(it) },
                onFailure = { WeatherUiState.Error(it.message ?: "Unknown error") }
            )

            val forecastResult = repository.getForecast(cityName)
            _forecastState.value = forecastResult.fold(
                onSuccess = { ForecastUiState.Success(it) },
                onFailure = { ForecastUiState.Error(it.message ?: "Unknown error") }
            )
        }
    }

    fun loadCapitalWeather(capital: CapitalCity) {
        viewModelScope.launch {
            val result = repository.getWeather(capital.nameEn)
            result.onSuccess { data ->
                val current = _capitalWeather.value?.toMutableMap() ?: mutableMapOf()
                current[capital.nameEn] = data
                _capitalWeather.value = current
            }
        }
    }

    fun loadAllCapitals() {
        viewModelScope.launch {
            val weatherByCity = repository.getWeatherForCitiesSequentially(capitals.map { it.nameEn })
            _capitalWeather.value = weatherByCity
        }
    }
}
