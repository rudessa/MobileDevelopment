package com.example.weatherapp

import android.content.Context
import android.os.Bundle
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.weatherapp.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val viewModel: WeatherViewModel by viewModels()
    private lateinit var capitalAdapter: CapitalAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        setupSearch()
        setupCapitalsList()
        observeViewModel()

        // Load all capitals weather on start
        viewModel.loadAllCapitals()
        // Show Moscow weather by default
        viewModel.searchCity("Moscow")
    }

    private fun setupSearch() {
        binding.btnSearch.setOnClickListener {
            performSearch()
        }

        binding.editTextCity.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                performSearch()
                true
            } else false
        }
    }

    private fun performSearch() {
        val query = binding.editTextCity.text?.toString()?.trim() ?: ""
        if (query.isBlank()) {
            Toast.makeText(this, "Введите название города", Toast.LENGTH_SHORT).show()
            return
        }
        hideKeyboard()
        viewModel.searchCity(query)
    }

    private fun setupCapitalsList() {
        capitalAdapter = CapitalAdapter { capital ->
            viewModel.searchCity(capital.nameEn)
            binding.editTextCity.setText(capital.name)
        }

        // HORIZONTAL scroll
        binding.recyclerViewCapitals.layoutManager =
            LinearLayoutManager(applicationContext, LinearLayoutManager.HORIZONTAL, false)
        binding.recyclerViewCapitals.adapter = capitalAdapter
        capitalAdapter.submitList(viewModel.capitals)
    }

    private fun observeViewModel() {
        viewModel.weatherState.observe(this) { state ->
            when (state) {
                is WeatherUiState.Idle -> {
                    binding.progressBar.visibility = View.GONE
                    binding.cardWeather.visibility = View.GONE
                    binding.cardError.visibility = View.GONE
                }
                is WeatherUiState.Loading -> {
                    binding.progressBar.visibility = View.VISIBLE
                    binding.cardWeather.visibility = View.GONE
                    binding.cardError.visibility = View.GONE
                }
                is WeatherUiState.Success -> {
                    binding.progressBar.visibility = View.GONE
                    binding.cardError.visibility = View.GONE
                    binding.cardWeather.visibility = View.VISIBLE
                    updateWeatherUI(state.data)
                }
                is WeatherUiState.Error -> {
                    binding.progressBar.visibility = View.GONE
                    binding.cardWeather.visibility = View.GONE
                    binding.cardError.visibility = View.VISIBLE
                    binding.textViewError.text = state.message
                }
            }
        }

        viewModel.capitalWeather.observe(this) { weatherMap ->
            capitalAdapter.updateWeatherMap(weatherMap)
        }
    }

    private fun updateWeatherUI(weather: WeatherData) {
        binding.textViewCityName.text = "${weather.cityName}, ${weather.country}"
        binding.textViewTemperature.text = "${weather.temperature.toInt()}°C"
        binding.textViewFeelsLike.text = "Ощущается как ${weather.feelsLike.toInt()}°C"
        binding.textViewDescription.text = weather.description
        binding.textViewHumidityValue.text = "${weather.humidity}%"
        binding.textViewWindValue.text = "${weather.windSpeed} м/с"
        binding.textViewCloudinessValue.text = "${weather.cloudiness}%"

        // Wind icon
        val windIcon = when {
            weather.windSpeed < 5 -> R.drawable.ic_wind_calm
            weather.windSpeed < 15 -> R.drawable.ic_wind_moderate
            else -> R.drawable.ic_wind_strong
        }
        binding.imageViewWind.setImageResource(windIcon)

        // Cloud icon
        val cloudIcon = when {
            weather.cloudiness < 20 -> R.drawable.ic_cloud_clear
            weather.cloudiness < 60 -> R.drawable.ic_cloud_partial
            else -> R.drawable.ic_cloud_overcast
        }
        binding.imageViewCloud.setImageResource(cloudIcon)
    }

    private fun hideKeyboard() {
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(binding.editTextCity.windowToken, 0)
    }
}