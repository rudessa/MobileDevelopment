package com.example.criptohub.service

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import okhttp3.HttpUrl
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.logging.HttpLoggingInterceptor
import org.json.JSONObject
import java.io.IOException
import java.util.Locale
import java.util.concurrent.TimeUnit
import kotlin.math.abs

class CryptoQuoteService : Service() {

    private val mainHandler = Handler(Looper.getMainLooper())
    private var isRequestInProgress = false
    private var symbol: String = DEFAULT_SYMBOL
    private var changeThresholdUsd: Double = DEFAULT_CHANGE_THRESHOLD_USD
    private var previousUsdQuote: Double? = null

    private val client: OkHttpClient by lazy {
        val logger = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BASIC
        }
        OkHttpClient.Builder()
            .addInterceptor(logger)
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .build()
    }

    private val quoteTask = object : Runnable {
        override fun run() {
            fetchQuote()
            mainHandler.postDelayed(this, UPDATE_INTERVAL_MS)
        }
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannelIfNeeded()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val newSymbol = intent?.getStringExtra(EXTRA_SYMBOL)
            ?.trim()
            ?.uppercase()
            ?.takeIf { it.isNotEmpty() }
            ?: DEFAULT_SYMBOL

        if (newSymbol != symbol) {
            previousUsdQuote = null
        }
        symbol = newSymbol

        changeThresholdUsd = intent?.getDoubleExtra(
            EXTRA_CHANGE_THRESHOLD_USD,
            DEFAULT_CHANGE_THRESHOLD_USD
        )?.takeIf { it > 0.0 } ?: DEFAULT_CHANGE_THRESHOLD_USD

        mainHandler.removeCallbacks(quoteTask)
        mainHandler.post(quoteTask)

        return START_STICKY
    }

    override fun onDestroy() {
        mainHandler.removeCallbacks(quoteTask)
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun fetchQuote() {
        if (isRequestInProgress) return
        isRequestInProgress = true

        val url = HttpUrl.Builder()
            .scheme("https")
            .host("min-api.cryptocompare.com")
            .addPathSegment("data")
            .addPathSegment("price")
            .addQueryParameter("fsym", symbol)
            .addQueryParameter("tsyms", "USD,JPY,RUB")
            .addQueryParameter("api_key", API_KEY)
            .build()

        val request = Request.Builder()
            .url(url)
            .get()
            .build()

        client.newCall(request).enqueue(object : okhttp3.Callback {
            override fun onFailure(call: okhttp3.Call, e: IOException) {
                isRequestInProgress = false
                broadcastError("Ошибка сети: ${e.message ?: "unknown"}")
            }

            override fun onResponse(call: okhttp3.Call, response: Response) {
                isRequestInProgress = false
                handleResponse(response)
            }
        })
    }

    private fun handleResponse(response: Response) {
        response.use {
            if (!it.isSuccessful) {
                broadcastError("HTTP ${it.code}")
                return
            }

            val body = it.body?.string().orEmpty()
            if (body.isBlank()) {
                broadcastError("Пустой ответ сервера")
                return
            }

            try {
                val json = JSONObject(body)
                if (json.has("Response") && json.optString("Response") == "Error") {
                    broadcastError(json.optString("Message", "API error"))
                    return
                }

                val usd = json.optDouble("USD", Double.NaN)
                val jpy = json.optDouble("JPY", Double.NaN)
                val rub = json.optDouble("RUB", Double.NaN)

                if (usd.isNaN() || jpy.isNaN() || rub.isNaN()) {
                    broadcastError("Не удалось разобрать котировку")
                    return
                }

                val updateIntent = Intent(ACTION_QUOTE_UPDATE).apply {
                    setPackage(packageName)
                    putExtra(EXTRA_SYMBOL, symbol)
                    putExtra(EXTRA_USD, usd)
                    putExtra(EXTRA_JPY, jpy)
                    putExtra(EXTRA_RUB, rub)
                    putExtra(EXTRA_TIMESTAMP, System.currentTimeMillis())
                }
                sendBroadcast(updateIntent)
                maybeNotifyOnChange(usd)
            } catch (e: Exception) {
                broadcastError("Ошибка парсинга: ${e.message ?: "unknown"}")
            }
        }
    }

    private fun broadcastError(message: String) {
        val errorIntent = Intent(ACTION_QUOTE_ERROR).apply {
            setPackage(packageName)
            putExtra(EXTRA_ERROR, message)
        }
        sendBroadcast(errorIntent)
    }

    private fun maybeNotifyOnChange(currentUsd: Double) {
        val previous = previousUsdQuote
        previousUsdQuote = currentUsd

        if (previous == null || previous == 0.0) return

        val changeUsd = currentUsd - previous
        if (abs(changeUsd) < changeThresholdUsd) return

        val directionUp = changeUsd > 0.0
        val arrow = if (directionUp) "↑" else "↓"
        val formattedChangeUsd = String.format(Locale.US, "%.2f", abs(changeUsd))
        val formattedPrice = String.format(Locale.US, "%.2f", currentUsd)

        val title = "$symbol $arrow USD $formattedChangeUsd"
        val message = "Текущая цена: USD $formattedPrice"

        showNotification(title, message)
    }

    private fun showNotification(title: String, message: String) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }

        val notification = NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
            .setSmallIcon(android.R.drawable.stat_notify_more)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .build()

        val notificationId = (System.currentTimeMillis() % Int.MAX_VALUE).toInt()
        NotificationManagerCompat.from(this).notify(notificationId, notification)
    }

    private fun createNotificationChannelIfNeeded() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return

        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channel = NotificationChannel(
            NOTIFICATION_CHANNEL_ID,
            NOTIFICATION_CHANNEL_NAME,
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = NOTIFICATION_CHANNEL_DESCRIPTION
        }
        manager.createNotificationChannel(channel)
    }

    companion object {
        const val ACTION_QUOTE_UPDATE = "com.example.criptohub.ACTION_QUOTE_UPDATE"
        const val ACTION_QUOTE_ERROR = "com.example.criptohub.ACTION_QUOTE_ERROR"

        const val EXTRA_SYMBOL = "extra_symbol"
        const val EXTRA_CHANGE_THRESHOLD_USD = "extra_change_threshold_usd"
        const val EXTRA_USD = "extra_usd"
        const val EXTRA_JPY = "extra_jpy"
        const val EXTRA_RUB = "extra_rub"
        const val EXTRA_TIMESTAMP = "extra_timestamp"
        const val EXTRA_ERROR = "extra_error"

        private const val DEFAULT_SYMBOL = "BTC"
        private const val DEFAULT_CHANGE_THRESHOLD_USD = 1.0
        private const val UPDATE_INTERVAL_MS = 10_000L
        private const val NOTIFICATION_CHANNEL_ID = "crypto_quote_changes"
        private const val NOTIFICATION_CHANNEL_NAME = "Изменения котировок"
        private const val NOTIFICATION_CHANNEL_DESCRIPTION =
            "Уведомления о значимом изменении курса криптовалюты"

        // Demo key from task statement; move to secure storage for production use.
        private const val API_KEY = "f804073b2b932f8421c78afde4ecc42fa96442de3c7eed449adfbaad6d6afe70"
    }
}
