package com.example.criptohub.model

data class QuoteHistoryItem(
    val symbol: String,
    val timestamp: Long,
    val usd: Double,
    val jpy: Double,
    val rub: Double
)
