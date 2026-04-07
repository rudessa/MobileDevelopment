package com.example.weatherapp

import android.app.AlertDialog
import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.text.InputType
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.example.weatherapp.databinding.ActivityMainBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.IOException
import java.util.Date
import java.util.concurrent.TimeUnit
import kotlin.io.path.Path
import kotlin.io.path.exists

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val viewModel: WeatherViewModel by viewModels()
    private lateinit var capitalAdapter: CapitalAdapter
    private val useWorkManagerDemo = false
    private val cacheRefreshLock = Any()
    private val refreshingCacheCities = mutableSetOf<String>()
    private val cacheHttpClient = OkHttpClient.Builder()
        .connectTimeout(20, TimeUnit.SECONDS)
        .readTimeout(25, TimeUnit.SECONDS)
        .writeTimeout(25, TimeUnit.SECONDS)
        .retryOnConnectionFailure(true)
        .build()

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
        ensureCacheSettings()
        checkAndRefreshWeatherCache(getLastCacheCity())

        if (useWorkManagerDemo) {
            enqueueWeatherWork()
        } else {
            viewModel.loadAllCapitals()
        }
        viewModel.searchCity(getLastCacheCity())
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menu.add(0, MENU_SETTINGS, 0, "Cache settings")
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == MENU_SETTINGS) {
            showCacheSettingsDialog()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    private fun showCacheSettingsDialog() {
        val input = EditText(this).apply {
            inputType = InputType.TYPE_CLASS_NUMBER
            setText(getCacheTtlMinutes().toString())
            hint = "Minutes"
        }

        AlertDialog.Builder(this)
            .setTitle("Cache TTL (minutes)")
            .setView(input)
            .setPositiveButton("Save") { _, _ ->
                val minutes = input.text?.toString()?.toLongOrNull()
                if (minutes == null || minutes <= 0) {
                    Toast.makeText(this, "Enter a valid number (> 0)", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }
                setCacheTtlMinutes(minutes)
                Toast.makeText(this, "Saved: $minutes min", Toast.LENGTH_SHORT).show()
                checkAndRefreshWeatherCache(getLastCacheCity())
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun ensureCacheSettings() {
        val prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
        if (!prefs.contains(KEY_CACHE_MINUTES)) {
            prefs.edit().putLong(KEY_CACHE_MINUTES, DEFAULT_CACHE_MINUTES).apply()
            Log.d(TAG_CACHE, "Cache ttl initialized: $DEFAULT_CACHE_MINUTES min")
        }
        if (!prefs.contains(KEY_LAST_CITY)) {
            prefs.edit().putString(KEY_LAST_CITY, DEFAULT_CACHE_CITY).apply()
            Log.d(TAG_CACHE, "Cache city initialized: $DEFAULT_CACHE_CITY")
        }
    }

    private fun checkAndRefreshWeatherCache(city: String) {
        lifecycleScope.launch(Dispatchers.IO) {
            val normalizedCity = city.trim().ifBlank { DEFAULT_CACHE_CITY }
            if (!tryStartCacheRefresh(normalizedCity)) {
                Log.d(TAG_CACHE, "Skip duplicate cache refresh for city=$normalizedCity")
                return@launch
            }
            val cacheFile = File(filesDir, buildCacheFileName(normalizedCity))
            val ttlMinutes = getCacheTtlMinutes()
            try {
                Log.d(TAG_CACHE, "Cache file name for city=$normalizedCity is ${cacheFile.name}")
                val exists = cacheFileExists(cacheFile)
                Log.d(
                    TAG_CACHE,
                    "Cache check started. city=$normalizedCity, exists=$exists, path=${cacheFile.absolutePath}, ttlMin=$ttlMinutes"
                )

                if (!exists) {
                    Log.d(TAG_CACHE, "Cache file missing. Downloading fresh JSON.")
                    downloadAndSaveWeatherJson(normalizedCity, cacheFile)
                    return@launch
                }

                val ageMs = Date().time - cacheFile.lastModified()
                val ageMinutes = ageMs / 60_000
                val isStale = ageMs > ttlMinutes * 60_000
                Log.d(TAG_CACHE, "Cache age=$ageMinutes min, stale=$isStale")

                if (isStale) {
                    Log.d(TAG_CACHE, "Cache is stale. Downloading fresh JSON.")
                    downloadAndSaveWeatherJson(normalizedCity, cacheFile)
                } else {
                    Log.d(TAG_CACHE, "Cache is fresh. Download skipped.")
                }
            } finally {
                finishCacheRefresh(normalizedCity)
            }
        }
    }

    private fun tryStartCacheRefresh(city: String): Boolean = synchronized(cacheRefreshLock) {
        if (refreshingCacheCities.contains(city)) return false
        refreshingCacheCities.add(city)
        true
    }

    private fun finishCacheRefresh(city: String) = synchronized(cacheRefreshLock) {
        refreshingCacheCities.remove(city)
    }

    private fun setCacheTtlMinutes(value: Long) {
        getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
            .edit()
            .putLong(KEY_CACHE_MINUTES, value)
            .apply()
        Log.d(TAG_CACHE, "Cache ttl updated: $value min")
    }

    private fun getCacheTtlMinutes(): Long {
        val prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
        return prefs.getLong(KEY_CACHE_MINUTES, DEFAULT_CACHE_MINUTES)
    }

    private fun getLastCacheCity(): String {
        val city = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
            .getString(KEY_LAST_CITY, DEFAULT_CACHE_CITY)
            .orEmpty()
            .trim()
        return if (city.isBlank()) DEFAULT_CACHE_CITY else city
    }

    private fun setLastCacheCity(city: String) {
        val normalized = city.trim().ifBlank { return }
        getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
            .edit()
            .putString(KEY_LAST_CITY, normalized)
            .apply()
    }

    private fun buildCacheFileName(city: String): String {
        // filesDir is app-internal storage (/data/user/0/<package>/files), not visible in regular file managers.
        val cityHash = city.lowercase().trim().hashCode().toUInt().toString(16)
        return "weather_forecast_cache_${cityHash}.json"
    }

    private fun cacheFileExists(file: File): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Path(file.absolutePath).exists()
        } else {
            file.exists()
        }
    }

    private suspend fun downloadAndSaveWeatherJson(city: String, cacheFile: File) {
        try {
            val apiKey = BuildConfig.OPEN_WEATHER_API_KEY
            if (apiKey.isBlank()) {
                Log.e(TAG_CACHE, "OPEN_WEATHER_API_KEY is empty. Cannot download JSON.")
                return
            }

            val encodedCity = Uri.encode(city)
            val url =
                "https://api.openweathermap.org/data/2.5/forecast?q=$encodedCity&appid=$apiKey&units=metric&lang=ru"
            val safeUrl =
                "https://api.openweathermap.org/data/2.5/forecast?q=$encodedCity&appid=***&units=metric&lang=ru"
            Log.d(TAG_CACHE, "Downloading JSON from OpenWeatherMap: $safeUrl")

            var lastError: Exception? = null
            for (attempt in 1..CACHE_DOWNLOAD_RETRIES) {
                try {
                    val request = Request.Builder().url(url).get().build()
                    cacheHttpClient.newCall(request).execute().use { response ->
                        if (!response.isSuccessful) {
                            throw IOException("HTTP ${response.code}")
                        }

                        val body = response.body?.string()
                            ?: throw IOException("Empty response body")

                        cacheFile.writeText(body, Charsets.UTF_8)
                        Log.d(
                            TAG_CACHE,
                            "Cache saved: ${cacheFile.absolutePath}, bytes=${body.toByteArray().size}, attempt=$attempt"
                        )
                        return
                    }
                } catch (e: Exception) {
                    lastError = e
                    Log.w(TAG_CACHE, "Cache download attempt $attempt/$CACHE_DOWNLOAD_RETRIES failed: ${e.message}")
                }

                if (attempt < CACHE_DOWNLOAD_RETRIES) {
                    delay(CACHE_RETRY_DELAY_MS * attempt)
                }
            }

            throw lastError ?: IOException("Unknown cache download error")
        } catch (e: Exception) {
            Log.e(TAG_CACHE, "Failed to refresh weather cache: ${e.message}", e)
        }
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
        setLastCacheCity(query)
        checkAndRefreshWeatherCache(query)
        hideKeyboard()
        viewModel.searchCity(query)
    }

    private fun setupCapitalsList() {
        capitalAdapter = CapitalAdapter { capital ->
            setLastCacheCity(capital.nameEn)
            checkAndRefreshWeatherCache(capital.nameEn)
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
        private const val TAG_CACHE = "WeatherCache"
        private const val PREFS_NAME = "weather_settings"
        private const val KEY_CACHE_MINUTES = "cache_minutes"
        private const val KEY_LAST_CITY = "cache_last_city"
        private const val DEFAULT_CACHE_MINUTES = 30L
        private const val DEFAULT_CACHE_CITY = "Moscow"
        private const val CACHE_DOWNLOAD_RETRIES = 3
        private const val CACHE_RETRY_DELAY_MS = 1500L
        private const val MENU_SETTINGS = 1001
    }
}
