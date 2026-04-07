package com.example.sensors

import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.view.ViewGroup
import android.widget.RadioButton
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.databinding.DataBindingUtil
import com.example.sensors.databinding.ActivityMainBinding
import java.util.Locale

class MainActivity : ComponentActivity(), SensorEventListener {

    private lateinit var dataSen: ActivityMainBinding
    private lateinit var sensorManager: SensorManager

    private var lightSensor: Sensor? = null
    private var rotationVectorSensor: Sensor? = null
    private var accelerometerSensor: Sensor? = null

    private var lightValue = 0f
    private var rotationValues = floatArrayOf(0f, 0f, 0f)
    private var rotationScalar = 0f
    private var accelerometerValues = floatArrayOf(0f, 0f, 0f)

    private var dataSensor: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        dataSen = DataBindingUtil.setContentView(this, R.layout.activity_main)

        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        lightSensor = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT)
        rotationVectorSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)
        accelerometerSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

        ensureRadioButtonsVisible()

        dataSen.radioGroup.setOnCheckedChangeListener { _, checkedId ->
            showMissingToastIfNeeded(checkedId)
            updateDisplayedData(checkedId)
        }

        dataSen.l.isChecked = true
        updateDisplayedData(R.id.l)
    }

    override fun onResume() {
        super.onResume()

        registerIfExists(lightSensor)
        registerIfExists(rotationVectorSensor)
        registerIfExists(accelerometerSensor)

        showMissingToastIfNeeded(dataSen.radioGroup.checkedRadioButtonId)
        updateDisplayedData(dataSen.radioGroup.checkedRadioButtonId)
    }

    override fun onPause() {
        super.onPause()
        sensorManager.unregisterListener(this)
    }

    override fun onSensorChanged(event: SensorEvent) {
        when (event.sensor.type) {
            Sensor.TYPE_LIGHT -> {
                lightValue = event.values[0]
                if (dataSen.radioGroup.checkedRadioButtonId == R.id.l) {
                    updateDisplayedData(R.id.l)
                }
            }

            Sensor.TYPE_ROTATION_VECTOR -> {
                rotationValues = floatArrayOf(
                    event.values.getOrElse(0) { 0f },
                    event.values.getOrElse(1) { 0f },
                    event.values.getOrElse(2) { 0f }
                )
                rotationScalar = event.values.getOrElse(3) { 0f }
                if (dataSen.radioGroup.checkedRadioButtonId == R.id.r) {
                    updateDisplayedData(R.id.r)
                }
            }

            Sensor.TYPE_ACCELEROMETER -> {
                accelerometerValues = floatArrayOf(
                    event.values.getOrElse(0) { 0f },
                    event.values.getOrElse(1) { 0f },
                    event.values.getOrElse(2) { 0f }
                )
                if (dataSen.radioGroup.checkedRadioButtonId == R.id.a) {
                    updateDisplayedData(R.id.a)
                }
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) = Unit

    private fun registerIfExists(sensor: Sensor?) {
        sensor?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_NORMAL)
        }
    }

    private fun showMissingToastIfNeeded(checkedId: Int) {
        when (checkedId) {
            R.id.l -> if (lightSensor == null) {
                Toast.makeText(this, getString(R.string.sensorAbsentL), Toast.LENGTH_SHORT).show()
            }

            R.id.r -> if (rotationVectorSensor == null) {
                Toast.makeText(this, getString(R.string.sensorAbsentR), Toast.LENGTH_SHORT).show()
            }

            R.id.a -> if (accelerometerSensor == null) {
                Toast.makeText(this, getString(R.string.sensorAbsentA), Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun updateDisplayedData(checkedId: Int) {
        dataSensor = when (checkedId) {
            R.id.l -> {
                String.format(
                    Locale.getDefault(),
                    "Освещённость: %.1f",
                    lightValue
                )
            }

            R.id.r -> {
                String.format(
                    Locale.getDefault(),
                    "Проекции вектора по осям:\nOX(%s),\nOY(%s),\nOZ(%s)\nскалярная мера угла поворота: %s",
                    rotationValues[0].toString(),
                    rotationValues[1].toString(),
                    rotationValues[2].toString(),
                    rotationScalar.toString()
                )
            }

            R.id.a -> {
                String.format(
                    Locale.getDefault(),
                    "Динамическое ускорение по осям:\nOX(%s),\nOY(%s),\nOZ(%s)",
                    accelerometerValues[0].toString(),
                    accelerometerValues[1].toString(),
                    accelerometerValues[2]
                )
            }

            else -> ""
        }

        dataSen.sensText = dataSensor
    }

    private fun ensureRadioButtonsVisible() {
        if (dataSen.radioGroup.childCount > 0) return

        val labels = listOf(
            R.id.l to getString(R.string.light),
            R.id.r to getString(R.string.rotor),
            R.id.a to getString(R.string.accelerometer)
        )

        labels.forEach { (id, text) ->
            val button = RadioButton(this).apply {
                this.id = id
                this.text = text
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                )
            }
            dataSen.radioGroup.addView(button)
        }
    }
}
