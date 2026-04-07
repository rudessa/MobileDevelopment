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
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.example.weatherapp.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val viewModel: WeatherViewModel by viewModels()
    private lateinit var capitalAdapter: CapitalAdapter
    private val useWorkManagerDemo = false

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

        if (useWorkManagerDemo) {
            enqueueWeatherWork()
        } else {
            viewModel.loadAllCapitals()
        }
        viewModel.searchCity("Moscow")
    }

    private fun enqueueWeatherWork() {
        val cities = viewModel.capitals.map { it.nameEn }
        if (cities.isEmpty()) return

        val requests = cities.map { city ->
            OneTimeWorkRequestBuilder<WeatherWorker>()
                .setInputData(workDataOf(WeatherWorker.KEY_CITY to city))
                .build()
        }

        val workManager = WorkManager.getInstance(this)
        var continuation = workManager.beginUniqueWork(
            WEATHER_CHAIN_NAME,
            ExistingWorkPolicy.REPLACE,
            requests.first()
        )

        requests.drop(1).forEach { request ->
            continuation = continuation.then(request)
        }

        continuation.enqueue()

        workManager.getWorkInfosForUniqueWorkLiveData(WEATHER_CHAIN_NAME).observe(this) { infos ->
            if (infos.isNullOrEmpty()) return@observe
            val finished = infos.all { it.state.isFinished }
            if (!finished) return@observe

            val successCount = infos.count {
                it.outputData.getString(WeatherWorker.KEY_OUTPUT_STATUS) == "success"
            }
            val failureCount = infos.count {
                it.outputData.getString(WeatherWorker.KEY_OUTPUT_STATUS) == "failure"
            }

            Toast.makeText(
                this,
                "WorkManager chain finished: success=$successCount, failure=$failureCount",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    private fun setupSearch() {
        binding.btnSearch.setOnClickListener {
            performSearch()
        }

        binding.editTextCity.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                performSearch()
                true
            } else {
                false
            }
        }
    }

    private fun performSearch() {
        val query = binding.editTextCity.text?.toString()?.trim() ?: ""
        if (query.isBlank()) {
            Toast.makeText(this, getString(R.string.enter_city_name), Toast.LENGTH_SHORT).show()
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

        binding.recyclerViewCapitals.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
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
        binding.textViewCityName.text =
            getString(R.string.city_country, weather.cityName, weather.country)
        binding.textViewTemperature.text =
            getString(R.string.temperature_celsius, weather.temperature.toInt())
        binding.textViewFeelsLike.text =
            getString(R.string.feels_like_celsius, weather.feelsLike.toInt())
        binding.textViewDescription.text = weather.description
        binding.textViewHumidityValue.text =
            getString(R.string.humidity_percent, weather.humidity)
        binding.textViewWindValue.text =
            getString(R.string.wind_meters_per_second, weather.windSpeed)
        binding.textViewCloudinessValue.text =
            getString(R.string.humidity_percent, weather.cloudiness)

        val windIcon = when {
            weather.windSpeed < 5 -> R.drawable.ic_wind_calm
            weather.windSpeed < 15 -> R.drawable.ic_wind_moderate
            else -> R.drawable.ic_wind_strong
        }
        binding.imageViewWind.setImageResource(windIcon)

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

    companion object {
        private const val WEATHER_CHAIN_NAME = "weather_forecast_chain"
    }
}
