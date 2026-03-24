package com.example.zhdun

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.graphics.RenderEffect
import android.graphics.Shader
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.zhdun.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    companion object {
        const val TOAST_TEXT = "Ждун отдыхает. Он подождёт ещё… когда-нибудь."
        const val CHANNEL_ID = "zhdun_battery_channel"
        const val NOTIFICATION_ID = 1
        const val REQUEST_NOTIFICATION_PERMISSION = 100
    }

    private lateinit var binding: ActivityMainBinding
    private var minuteCount = 0
    private var isReceiverRegistered = false

    private val timeTickReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == Intent.ACTION_TIME_TICK) {
                minuteCount++
                binding.tvStatus.text = getString(R.string.contemplation_time, minuteCount)
                binding.tvStatus.setTextColor(getColor(R.color.text_primary))
                updateMoodByMinutes()
            }
        }
    }

    private val batteryLowReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == Intent.ACTION_BATTERY_LOW) {
                showBatteryNotification()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        applyBlurToBackground()
        createNotificationChannel()
        requestNotificationPermission()

        binding.tvStatus.text = getString(R.string.initial_status)
        registerReceivers()

        setMoodEmoji(getString(R.string.mood_calm))

        binding.btnStop.setOnClickListener {
            unregisterReceivers()
            Toast.makeText(this, TOAST_TEXT, Toast.LENGTH_LONG).show()
        }

        binding.btnTestBattery.setOnClickListener {
            showBatteryNotification()
        }

        binding.btnTestBattery.setOnLongClickListener {
            android.app.AlertDialog.Builder(this)
                .setTitle("ℹ️ Тест батареи")
                .setMessage(
                    "Это искусственная проверка — симулирует уведомление о низком заряде батареи.\n\n" +
                            "В реальности уведомление придёт автоматически когда заряд упадёт ниже 15%.\n\n" +
                            "Обычное нажатие на кнопку отправляет тестовое уведомление прямо сейчас."
                )
                .setPositiveButton("Понятно", null)
                .show()
            true
        }
    }

    private fun applyBlurToBackground() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            binding.ivZhdunBg.setRenderEffect(
                RenderEffect.createBlurEffect(18f, 18f, Shader.TileMode.CLAMP)
            )
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Zhdun Notifications",
                NotificationManager.IMPORTANCE_HIGH
            )
            channel.description = "Battery low notifications"
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    private fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(android.Manifest.permission.POST_NOTIFICATIONS),
                    REQUEST_NOTIFICATION_PERMISSION
                )
            }
        }
    }

    private fun showBatteryNotification() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS)
            != PackageManager.PERMISSION_GRANTED
        ) return

        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_alert)
            .setContentTitle("Zhdun is hungry!")
            .setContentText(getString(R.string.battery_low_text))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        NotificationManagerCompat.from(this).notify(NOTIFICATION_ID, notification)
    }

    private fun registerReceivers() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(
                timeTickReceiver,
                IntentFilter(Intent.ACTION_TIME_TICK),
                RECEIVER_NOT_EXPORTED
            )
            registerReceiver(
                batteryLowReceiver,
                IntentFilter(Intent.ACTION_BATTERY_LOW),
                RECEIVER_NOT_EXPORTED
            )
        } else {
            registerReceiver(timeTickReceiver, IntentFilter(Intent.ACTION_TIME_TICK))
            registerReceiver(batteryLowReceiver, IntentFilter(Intent.ACTION_BATTERY_LOW))
        }
        isReceiverRegistered = true
    }

    private fun unregisterReceivers() {
        if (isReceiverRegistered) {
            unregisterReceiver(timeTickReceiver)
            unregisterReceiver(batteryLowReceiver)
            isReceiverRegistered = false
        }
    }

    private fun setMoodEmoji(emoji: String) {
        binding.tvMood.text = emoji
        val paint = binding.tvMood.paint
        val colorMatrix = android.graphics.ColorMatrix()
        colorMatrix.setSaturation(0f)
        paint.colorFilter = android.graphics.ColorMatrixColorFilter(colorMatrix)
        binding.tvMood.invalidate()
    }

    private fun updateMoodByMinutes() {
        val emoji = when {
            minuteCount < 5  -> getString(R.string.mood_calm)
            minuteCount < 15 -> getString(R.string.mood_patient)
            minuteCount < 30 -> getString(R.string.mood_deep)
            else             -> getString(R.string.mood_eternal)
        }
        setMoodEmoji(emoji)
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceivers()
    }
}