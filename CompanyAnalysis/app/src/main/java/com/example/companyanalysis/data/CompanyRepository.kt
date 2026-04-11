package com.example.companyanalysis.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

class CompanyRepository(
    private val companyDao: CompanyDao
) {
    val companies: Flow<List<Company>> = companyDao.observeCompanies()

    val analysisStats: Flow<AnalysisStats> = combine(
        companyDao.observeTotalCapitalization(),
        companyDao.observeAboveAverageCount(),
        companyDao.observeEnglishNamesCount(),
        companyDao.observeMaxCapitalizationCompany(),
        companyDao.observeLongestNameCompany()
    ) { total, aboveAverage, englishCount, maxName, longestName ->
        AnalysisStats(
            totalCapitalization = total,
            aboveAverageCount = aboveAverage,
            englishNamesCount = englishCount,
            maxCapitalizationCompany = maxName.orEmpty(),
            longestNameCompany = longestName.orEmpty()
        )
    }

    suspend fun ensureSeedData() {
        if (companyDao.getCount() == 0) {
            companyDao.insertAll(seedCompanies2025)
        }
        ensureGroupCompaniesPresent()
    }

    suspend fun deleteBySubstring(substring: String): List<Company> {
        if (substring.isBlank()) return emptyList()

        val companiesToDelete = companyDao.getCompaniesBySubstring(substring)
        if (companiesToDelete.isNotEmpty()) {
            companyDao.deleteBySubstring(substring)
        }
        return companiesToDelete
    }

    suspend fun restoreCompanies(companies: List<Company>) {
        if (companies.isNotEmpty()) {
            companyDao.insertAll(companies.map { it.copy(id = 0) })
        }
    }

    private suspend fun ensureGroupCompaniesPresent() {
        val existingNames = companyDao.getAllNames().toSet()
        val missingGroupCompanies = groupCompanies.filter { it.name !in existingNames }
        if (missingGroupCompanies.isNotEmpty()) {
            companyDao.insertAll(missingGroupCompanies)
        }
    }
}

private val seedCompanies2025 = listOf(
    Company(name = "\u0421\u0431\u0435\u0440\u0431\u0430\u043d\u043a", capitalization = 71.57),
    Company(name = "\u0420\u043e\u0441\u043d\u0435\u0444\u0442\u044c", capitalization = 47.35),
    Company(name = "\u041b\u0423\u041a\u041e\u0419\u041b", capitalization = 43.97),
    Company(name = "\u041d\u041e\u0412\u0410\u0422\u042d\u041a", capitalization = 39.52),
    Company(name = "\u041f\u043e\u043b\u044e\u0441", capitalization = 35.44),
    Company(name = "\u0413\u0430\u0437\u043f\u0440\u043e\u043c", capitalization = 32.88),
    Company(name = "\u041d\u043e\u0440\u043d\u0438\u043a\u0435\u043b\u044c", capitalization = 25.13),
    Company(name = "\u0422\u0430\u0442\u043d\u0435\u0444\u0442\u044c", capitalization = 13.89),
    Company(name = "\u0424\u043e\u0441\u0410\u0433\u0440\u043e", capitalization = 9.18),
    Company(name = "\u0421\u0435\u0432\u0435\u0440\u0441\u0442\u0430\u043b\u044c", capitalization = 8.97),
    Company(name = "\u0421\u0443\u0440\u0433\u0443\u0442\u043d\u0435\u0444\u0442\u0435\u0433\u0430\u0437", capitalization = 8.63),
    Company(name = "\u041d\u041b\u041c\u041a", capitalization = 6.92),
    Company(name = "Mobile TeleSystems", capitalization = 4.63),
    Company(name = "\u041c\u0430\u0433\u043d\u0438\u0442", capitalization = 3.40),
    Company(name = "United Heavy Machinery", capitalization = 0.36),
    Company(name = "Mechel PAO", capitalization = 0.34),
    Company(name = "En+ Group", capitalization = 4.12),
    Company(name = "Etalon Group", capitalization = 0.22),
    Company(name = "Mail.Ru Group", capitalization = 1.48),
    Company(name = "X5 Retail Group", capitalization = 8.54)
)

private val groupCompanies = listOf(
    Company(name = "En+ Group", capitalization = 4.12),
    Company(name = "Etalon Group", capitalization = 0.22),
    Company(name = "Mail.Ru Group", capitalization = 1.48),
    Company(name = "X5 Retail Group", capitalization = 8.54)
)
