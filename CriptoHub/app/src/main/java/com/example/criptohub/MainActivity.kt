package com.example.criptohub

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.content.ContextCompat
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.criptohub.model.QuoteHistoryItem
import com.example.criptohub.service.CryptoQuoteService
import com.example.criptohub.ui.theme.CriptoHubTheme
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainActivity : ComponentActivity() {

    private var currentSymbol by mutableStateOf("BTC")
    private var latestQuote by mutableStateOf<QuoteHistoryItem?>(null)
    private var history by mutableStateOf(emptyList<QuoteHistoryItem>())
    private var statusMessage by mutableStateOf("Сервис остановлен")

    private val quoteReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            when (intent?.action) {
                CryptoQuoteService.ACTION_QUOTE_UPDATE -> {
                    val symbol = intent.getStringExtra(CryptoQuoteService.EXTRA_SYMBOL) ?: return
                    val usd = intent.getDoubleExtra(CryptoQuoteService.EXTRA_USD, Double.NaN)
                    val jpy = intent.getDoubleExtra(CryptoQuoteService.EXTRA_JPY, Double.NaN)
                    val rub = intent.getDoubleExtra(CryptoQuoteService.EXTRA_RUB, Double.NaN)
                    val timestamp = intent.getLongExtra(CryptoQuoteService.EXTRA_TIMESTAMP, 0L)

                    if (usd.isNaN() || jpy.isNaN() || rub.isNaN() || timestamp == 0L) return

                    val item = QuoteHistoryItem(symbol, timestamp, usd, jpy, rub)
                    latestQuote = item
                    history = listOf(item) + history
                    statusMessage = "Обновлено: ${formatDateTime(timestamp)}"
                }

                CryptoQuoteService.ACTION_QUOTE_ERROR -> {
                    val error = intent.getStringExtra(CryptoQuoteService.EXTRA_ERROR).orEmpty()
                    statusMessage = "Ошибка: $error"
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            CriptoHubTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    QuoteScreen(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                            .padding(16.dp),
                        symbol = currentSymbol,
                        latestQuote = latestQuote,
                        history = history,
                        status = statusMessage,
                        onSymbolChange = { currentSymbol = it.uppercase(Locale.getDefault()) },
                        onStartClick = { startQuoteService() },
                        onStopClick = { stopQuoteService() }
                    )
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        val filter = IntentFilter().apply {
            addAction(CryptoQuoteService.ACTION_QUOTE_UPDATE)
            addAction(CryptoQuoteService.ACTION_QUOTE_ERROR)
        }
        ContextCompat.registerReceiver(
            this,
            quoteReceiver,
            filter,
            ContextCompat.RECEIVER_NOT_EXPORTED
        )
    }

    override fun onStop() {
        unregisterReceiver(quoteReceiver)
        super.onStop()
    }

    private fun startQuoteService() {
        val symbol = currentSymbol.trim().ifEmpty { "BTC" }.uppercase(Locale.getDefault())
        currentSymbol = symbol
        statusMessage = "Сервис запущен для $symbol, запрашиваю котировку..."

        val intent = Intent(this, CryptoQuoteService::class.java).apply {
            putExtra(CryptoQuoteService.EXTRA_SYMBOL, symbol)
        }
        startService(intent)
    }

    private fun stopQuoteService() {
        stopService(Intent(this, CryptoQuoteService::class.java))
        statusMessage = "Сервис остановлен"
    }

    private fun formatDateTime(timestamp: Long): String {
        val formatter = SimpleDateFormat("dd.MM.yyyy HH:mm:ss", Locale.getDefault())
        return formatter.format(Date(timestamp))
    }
}

@Composable
private fun QuoteScreen(
    modifier: Modifier,
    symbol: String,
    latestQuote: QuoteHistoryItem?,
    history: List<QuoteHistoryItem>,
    status: String,
    onSymbolChange: (String) -> Unit,
    onStartClick: () -> Unit,
    onStopClick: () -> Unit
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "Мониторинг криптовалюты",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )

        OutlinedTextField(
            value = symbol,
            onValueChange = onSymbolChange,
            label = { Text("Символ, например BTC") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = onStartClick) {
                Text("Старт")
            }
            Button(onClick = onStopClick) {
                Text("Стоп")
            }
        }

        Text(text = status, style = MaterialTheme.typography.bodyMedium)

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(text = "Текущая котировка", fontWeight = FontWeight.SemiBold)
                if (latestQuote == null) {
                    Text(text = "Данные пока не получены")
                } else {
                    Text(text = "${latestQuote.symbol}: USD ${formatNumber(latestQuote.usd)}")
                    Text(text = "JPY ${formatNumber(latestQuote.jpy)}")
                    Text(text = "RUB ${formatNumber(latestQuote.rub)}")
                    Text(text = formatTimestamp(latestQuote.timestamp))
                }
            }
        }

        Text(
            text = "История котировок (${history.size})",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )

        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(history) { item ->
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text(text = "${item.symbol} | ${formatTimestamp(item.timestamp)}")
                        Text(text = "USD ${formatNumber(item.usd)}")
                        Text(text = "JPY ${formatNumber(item.jpy)}")
                        Text(text = "RUB ${formatNumber(item.rub)}")
                    }
                }
            }
        }
    }
}

private fun formatTimestamp(timestamp: Long): String {
    val formatter = SimpleDateFormat("dd.MM.yyyy HH:mm:ss", Locale.getDefault())
    return formatter.format(Date(timestamp))
}

private fun formatNumber(value: Double): String = String.format(Locale.US, "%.2f", value)


