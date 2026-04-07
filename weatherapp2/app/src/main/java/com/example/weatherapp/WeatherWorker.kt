package com.example.weatherapp

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import androidx.work.workDataOf

class WeatherWorker(
    context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        val city = inputData.getString(KEY_CITY)?.trim().orEmpty()
        if (city.isBlank()) {
            return Result.success(
                workDataOf(
                    KEY_OUTPUT_CITY to "unknown",
                    KEY_OUTPUT_STATUS to "failure",
                    KEY_ERROR to "City is missing"
                )
            )
        }

        val repository = WeatherRepository()
        val result = repository.getForecast(city)

        return result.fold(
            onSuccess = { forecast ->
                Log.d(TAG, "Forecast loaded for ${forecast.cityName}")
                Result.success(
                    workDataOf(
                        KEY_OUTPUT_CITY to forecast.cityName,
                        KEY_OUTPUT_STATUS to "success"
                    )
                )
            },
            onFailure = { error ->
                Log.e(TAG, "Failed for $city: ${error.message}")
                // Return success with failure status so chain continues with next city.
                Result.success(
                    workDataOf(
                        KEY_OUTPUT_CITY to city,
                        KEY_OUTPUT_STATUS to "failure",
                        KEY_ERROR to (error.message ?: "Unknown error")
                    )
                )
            }
        )
    }

    companion object {
        const val KEY_CITY = "city"
        const val KEY_OUTPUT_CITY = "output_city"
        const val KEY_OUTPUT_STATUS = "output_status"
        const val KEY_ERROR = "error"
        private const val TAG = "WeatherWorker"
    }
}
