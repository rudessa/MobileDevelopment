package com.example.currentweatherdatabinding

import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.example.currentweatherdatabinding.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val viewModel: WeatherViewModel by viewModels()

    private val cities = listOf(
        // Россия
        "Moscow",
        "Saint Petersburg",
        "Novosibirsk",
        "Yekaterinburg",
        "Kazan",
        "Sochi",

        // Европа
        "London",
        "Paris",
        "Berlin",
        "Rome",
        "Madrid",
        "Amsterdam",
        "Vienna",
        "Prague",
        "Stockholm",
        "Copenhagen",
        "Athens",
        "Lisbon",
        "Warsaw",
        "Budapest",
        "Brussels",

        // Азия
        "Tokyo",
        "Seoul",
        "Beijing",
        "Shanghai",
        "Hong Kong",
        "Singapore",
        "Dubai",
        "Bangkok",
        "Mumbai",
        "Delhi",
        "Istanbul",

        // Северная Америка
        "New York",
        "Los Angeles",
        "Chicago",
        "Toronto",
        "Vancouver",
        "Montreal",
        "Miami",
        "San Francisco",
        "Las Vegas",
        "Mexico City",

        // Южная Америка
        "Buenos Aires",
        "São Paulo",
        "Rio de Janeiro",
        "Lima",
        "Santiago",

        // Африка
        "Cairo",
        "Cape Town",
        "Johannesburg",
        "Nairobi",

        // Океания
        "Sydney",
        "Melbourne",
        "Auckland",
        "Wellington"
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        binding.viewModel = viewModel
        binding.lifecycleOwner = this

        setupCitySpinner()
        observeViewModel()

        binding.btnRefresh.setOnClickListener {
            val selectedCity = binding.spinnerCity.selectedItem.toString()
            viewModel.loadWeather(selectedCity)
        }
    }

    private fun setupCitySpinner() {
        val adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item,
            cities
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerCity.adapter = adapter

        binding.spinnerCity.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                viewModel.loadWeather(cities[position])
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    private fun observeViewModel() {
        viewModel.errorMessage.observe(this) { error ->
            error?.let {
                Toast.makeText(this, it, Toast.LENGTH_LONG).show()
            }
        }
    }
}