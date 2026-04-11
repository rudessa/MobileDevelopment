package com.example.companyanalysis.data

data class AnalysisStats(
    val totalCapitalization: Double = 0.0,
    val aboveAverageCount: Int = 0,
    val englishNamesCount: Int = 0,
    val maxCapitalizationCompany: String = "",
    val longestNameCompany: String = ""
)
