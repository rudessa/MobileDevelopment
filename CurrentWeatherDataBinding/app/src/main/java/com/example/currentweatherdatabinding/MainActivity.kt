package com.example.currentweatherdatabinding

import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.example.currentweatherdatabinding.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val viewModel: WeatherViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        binding.viewModel = viewModel
        binding.lifecycleOwner = this

        setupCitySpinner()
        setupTemperatureRadioButtons()
        setupWindDirectionCheckbox()
        observeViewModel()

        binding.btnRefresh.setOnClickListener {
            val selectedCity = binding.spinnerCity.selectedItem.toString()
            viewModel.loadWeather(selectedCity)
        }
    }

    private fun setupCitySpinner() {
        binding.spinnerCity.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val selectedCity = parent?.getItemAtPosition(position).toString()
                viewModel.loadWeather(selectedCity)
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    private fun setupTemperatureRadioButtons() {
        binding.radioGroupTemp.setOnCheckedChangeListener { _, checkedId ->
            val isCelsius = checkedId == R.id.radioCelsius
            viewModel.setTemperatureUnit(isCelsius)
            updateTemperatureDisplay()
        }
    }

    private fun setupWindDirectionCheckbox() {
        binding.checkboxWindDirection.setOnCheckedChangeListener { _, isChecked ->
            viewModel.setShowWindDirection(isChecked)
            binding.windDirectionContainer.visibility = if (isChecked) View.VISIBLE else View.GONE

            if (isChecked) {
                updateWindDirection()
            }
        }
    }

    private fun observeViewModel() {
        viewModel.errorMessage.observe(this) { error ->
            error?.let {
                Toast.makeText(this, it, Toast.LENGTH_LONG).show()
            }
        }

        viewModel.weatherData.observe(this) { weatherData ->
            weatherData?.let {
                updateTemperatureDisplay()
                updateWindDirection()
            }
        }

        viewModel.isCelsius.observe(this) {
            updateTemperatureDisplay()
        }

        viewModel.showWindDirection.observe(this) { show ->
            binding.windDirectionContainer.visibility = if (show) View.VISIBLE else View.GONE
        }
    }

    private fun updateTemperatureDisplay() {
        val weatherData = viewModel.weatherData.value
        val isCelsius = viewModel.isCelsius.value ?: true

        weatherData?.let {
            binding.tvTemperature.text = it.getTemperatureDisplay(isCelsius)
            binding.tvFeelsLike.text = "Ощущается как ${it.getFeelsLikeDisplay(isCelsius)}"
        }
    }

    private fun updateWindDirection() {
        val weatherData = viewModel.weatherData.value
        val showWindDirection = viewModel.showWindDirection.value ?: false

        if (showWindDirection && weatherData != null) {
            binding.tvWindDirection.text = weatherData.windDirectionWithDegree
        }
    }
}