package com.example.sensors

import android.hardware.Sensor
import android.hardware.SensorManager
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.Spinner
import androidx.activity.ComponentActivity

class MainActivity : ComponentActivity() {

    private lateinit var sensorManager: SensorManager
    private lateinit var sensorListAdapter: ArrayAdapter<String>

    private val humanStateTypeConstants = arrayOf(21,31,34)

    private val trackedSensorsByCategory: List<List<TrackedSensor>> by lazy {
        listOf(
            listOf(
                TrackedSensor("Магнитное поле", Sensor.TYPE_MAGNETIC_FIELD),
                TrackedSensor("Освещённость", Sensor.TYPE_LIGHT),
                TrackedSensor("Давление", Sensor.TYPE_PRESSURE),
                TrackedSensor("Относительная влажность", Sensor.TYPE_RELATIVE_HUMIDITY),
                TrackedSensor("Температура", Sensor.TYPE_AMBIENT_TEMPERATURE)
            ),
            listOf(
                TrackedSensor("Акселерометр", Sensor.TYPE_ACCELEROMETER),
                TrackedSensor("Гироскоп", Sensor.TYPE_GYROSCOPE),
                TrackedSensor("Приближение", Sensor.TYPE_PROXIMITY),
                TrackedSensor("Гравитация", Sensor.TYPE_GRAVITY),
                TrackedSensor("Ускорение прямолинейного движения", Sensor.TYPE_LINEAR_ACCELERATION),
                TrackedSensor("Вектор вращения с отклонениями по осям", Sensor.TYPE_ROTATION_VECTOR),
                TrackedSensor("Вектор вращения без геомагнитного влияния", Sensor.TYPE_GAME_ROTATION_VECTOR),
                TrackedSensor("Некалиброванный гироскоп", Sensor.TYPE_GYROSCOPE_UNCALIBRATED),
                TrackedSensor("Значительные колебания", Sensor.TYPE_SIGNIFICANT_MOTION),
                TrackedSensor("Одиночный шаг", Sensor.TYPE_STEP_DETECTOR),
                TrackedSensor("Количество шагов", Sensor.TYPE_STEP_COUNTER),
                TrackedSensor("Движение", Sensor.TYPE_MOTION_DETECT)
            ),
            listOf(
                TrackedSensor("Мониторинг пульса", humanStateTypeConstants[0]),
                TrackedSensor("ЧСС", humanStateTypeConstants[1]),
                TrackedSensor("Удаление устройства от человека", humanStateTypeConstants[2])
            )
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager

        val spinner = findViewById<Spinner>(R.id.spinner_list_sensor)
        val listView = findViewById<ListView>(R.id.list_sensors)

        val categoryAdapter = ArrayAdapter(
            this,
            R.layout.item_spinner_selected,
            resources.getStringArray(R.array.type_sensors)
        )
        categoryAdapter.setDropDownViewResource(R.layout.item_spinner_dropdown)
        spinner.adapter = categoryAdapter

        sensorListAdapter = ArrayAdapter(this, R.layout.item_sensor, mutableListOf())
        listView.adapter = sensorListAdapter

        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                showSensorsForCategory(position)
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                sensorListAdapter.clear()
            }
        }

        spinner.setSelection(0)
        showSensorsForCategory(0)
    }

    private fun showSensorsForCategory(categoryIndex: Int) {
        val trackedSensors = trackedSensorsByCategory.getOrElse(categoryIndex) { emptyList() }

        val availableSensorNames = trackedSensors.flatMap { tracked ->
            sensorManager.getSensorList(tracked.type).map { sensor ->
                "${tracked.label}: ${sensor.name}"
            }
        }

        sensorListAdapter.clear()
        sensorListAdapter.addAll(availableSensorNames)
        sensorListAdapter.notifyDataSetChanged()
    }
}

data class TrackedSensor(
    val label: String,
    val type: Int
)
