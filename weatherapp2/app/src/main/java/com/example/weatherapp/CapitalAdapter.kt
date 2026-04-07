package com.example.weatherapp

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.weatherapp.databinding.ItemCapitalBinding

class CapitalAdapter(
    private val onClick: (CapitalCity) -> Unit
) : ListAdapter<CapitalCity, CapitalAdapter.CapitalViewHolder>(DiffCallback()) {

    private var weatherMap: Map<String, WeatherData> = emptyMap()

    fun updateWeatherMap(map: Map<String, WeatherData>) {
        weatherMap = map
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CapitalViewHolder {
        val binding = ItemCapitalBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CapitalViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CapitalViewHolder, position: Int) {
        holder.bind(getItem(position), weatherMap)
    }

    inner class CapitalViewHolder(private val binding: ItemCapitalBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(capital: CapitalCity, weatherMap: Map<String, WeatherData>) {
            binding.textCapitalName.text = capital.name
            binding.textCapitalCountry.text = capital.country

            val weather = weatherMap[capital.nameEn]
            if (weather != null) {
                binding.textCapitalTemp.text =
                    binding.root.context.getString(R.string.temperature_celsius, weather.temperature.toInt())
                binding.textCapitalHumidity.text =
                    binding.root.context.getString(R.string.capital_humidity, weather.humidity)

                val cloudIcon = when {
                    weather.cloudiness < 20 -> R.drawable.ic_cloud_clear
                    weather.cloudiness < 60 -> R.drawable.ic_cloud_partial
                    else -> R.drawable.ic_cloud_overcast
                }
                binding.imageCapitalCloud.setImageResource(cloudIcon)
            } else {
                binding.textCapitalTemp.text = "..."
                binding.textCapitalHumidity.text = ""
            }

            binding.root.setOnClickListener { onClick(capital) }
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<CapitalCity>() {
        override fun areItemsTheSame(oldItem: CapitalCity, newItem: CapitalCity) =
            oldItem.nameEn == newItem.nameEn

        override fun areContentsTheSame(oldItem: CapitalCity, newItem: CapitalCity) =
            oldItem == newItem
    }
}
