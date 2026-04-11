package com.example.companyanalysis

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.clickable
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.companyanalysis.data.AnalysisStats
import com.example.companyanalysis.data.Company
import com.example.companyanalysis.data.CompanyDatabase
import com.example.companyanalysis.data.CompanyRepository
import com.example.companyanalysis.ui.theme.CompanyAnalysisTheme
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale

private const val TITLE_COMPANIES =
    "Капитализация российских компаний\nна конец 2025 года"

private const val LABEL_ENTER_PART =
    "Введите часть имени компании"

private const val BUTTON_DELETE =
    "УДАЛИТЬ КОМПАНИИ С ТАКИМИ\nИМЕНАМИ ИЗ БАЗЫ"

private const val BUTTON_ANALYSIS =
    "АНАЛИЗ ДАННЫХ"

private const val BUTTON_RESTORE =
    "ВЕРНУТЬ УДАЛЕННЫЕ КОМПАНИИ"

private const val TITLE_ANALYSIS =
    "Анализ данных"

private const val TITLE_COMPANY_ANALYSIS =
    "Анализ компании"

private const val LABEL_TOTAL =
    "Общая капитализация"

private const val LABEL_ABOVE_AVERAGE =
    "Компаний с капитализацией выше среднего"

private const val LABEL_ENGLISH =
    "Компаний с англоязычными названиями"

private const val LABEL_MAX_CAP =
    "Компания с самой высокой капитализацией"

private const val LABEL_LONGEST =
    "Компания с самым длинным названием"

private const val LABEL_COMPANY_CAPITALIZATION =
    "Капитализация"

private const val LABEL_COMPANY_SHARE =
    "Доля от общей капитализации"

private const val LABEL_COMPANY_RANK =
    "Место в списке по капитализации"

private const val LABEL_COMPANY_LANGUAGE =
    "Тип названия"

private const val LABEL_COMPANY_NAME_LENGTH =
    "Длина названия"

private const val LABEL_COMPANY_VS_AVERAGE =
    "Сравнение со средней"

private const val BUTTON_BACK = "НАЗАД"

private val numberFormatter = DecimalFormat(
    "#0.00",
    DecimalFormatSymbols(Locale.US)
)

private val percentFormatter = DecimalFormat(
    "#0.00",
    DecimalFormatSymbols(Locale.US)
)

class MainActivity : ComponentActivity() {
    private val viewModel: MainViewModel by viewModels {
        MainViewModelFactory(
            CompanyRepository(
                CompanyDatabase.getInstance(applicationContext).companyDao()
            )
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CompanyAnalysisTheme {
                CompanyApp(viewModel = viewModel)
            }
        }
    }
}

@Composable
private fun CompanyApp(viewModel: MainViewModel) {
    val companies by viewModel.companies.collectAsState()
    val analysisStats by viewModel.analysisStats.collectAsState()
    val input by viewModel.input.collectAsState()
    val canRestoreDeletedCompanies by viewModel.canRestoreDeletedCompanies.collectAsState()
    var screen by rememberSaveable { mutableStateOf(Screen.List) }
    var selectedCompany by androidx.compose.runtime.remember { mutableStateOf<Company?>(null) }

    Surface(modifier = Modifier.fillMaxSize()) {
        when (screen) {
            Screen.List -> CompanyListScreen(
                companies = companies,
                input = input,
                onInputChange = viewModel::updateInput,
                onDeleteClick = viewModel::deleteCompaniesByInput,
                onRestoreClick = viewModel::restoreDeletedCompanies,
                canRestoreDeletedCompanies = canRestoreDeletedCompanies,
                onOpenAnalysis = { screen = Screen.Analysis },
                onCompanyClick = { company ->
                    selectedCompany = company
                    screen = Screen.CompanyDetails
                }
            )

            Screen.Analysis -> AnalysisScreen(
                stats = analysisStats,
                onBack = { screen = Screen.List }
            )

            Screen.CompanyDetails -> {
                val company = selectedCompany
                if (company != null) {
                    CompanyDetailsScreen(
                        company = company,
                        companies = companies,
                        onBack = { screen = Screen.List }
                    )
                } else {
                    screen = Screen.List
                }
            }
        }
    }
}

@Composable
private fun CompanyListScreen(
    companies: List<Company>,
    input: String,
    onInputChange: (String) -> Unit,
    onDeleteClick: () -> Unit,
    onRestoreClick: () -> Unit,
    canRestoreDeletedCompanies: Boolean,
    onOpenAnalysis: () -> Unit,
    onCompanyClick: (Company) -> Unit
) {
    CompanyScaffold {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 12.dp)
                .imePadding(),
            verticalArrangement = Arrangement.Top
        ) {
            SectionHeader(
                title = TITLE_COMPANIES,
                caption = "Значения в млрд €"
            )
            Spacer(modifier = Modifier.height(12.dp))
            companies.forEach { company ->
                CompanyRow(
                    company = company,
                    onClick = { onCompanyClick(company) }
                )
                Spacer(modifier = Modifier.height(8.dp))
            }
            Spacer(modifier = Modifier.height(20.dp))
            Text(
                text = LABEL_ENTER_PART,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = input,
                onValueChange = onInputChange,
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Words
                ),
                textStyle = MaterialTheme.typography.bodyLarge.copy(
                    color = MaterialTheme.colorScheme.onSurface
                ),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                    focusedIndicatorColor = MaterialTheme.colorScheme.primary,
                    unfocusedIndicatorColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    focusedTextColor = MaterialTheme.colorScheme.onSurface,
                    unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                    cursorColor = MaterialTheme.colorScheme.primary
                )
            )
            Spacer(modifier = Modifier.height(12.dp))
            MainButton(text = BUTTON_DELETE, onClick = onDeleteClick)
            Spacer(modifier = Modifier.height(12.dp))
            MainButton(
                text = BUTTON_RESTORE,
                onClick = onRestoreClick,
                enabled = canRestoreDeletedCompanies
            )
            Spacer(modifier = Modifier.height(12.dp))
            MainButton(text = BUTTON_ANALYSIS, onClick = onOpenAnalysis)
        }
    }
}

@Composable
private fun CompanyDetailsScreen(
    company: Company,
    companies: List<Company>,
    onBack: () -> Unit
) {
    val totalCapitalization = companies.sumOf { it.capitalization }
    val averageCapitalization = companies.map { it.capitalization }.average().takeIf { !it.isNaN() } ?: 0.0
    val companyRank = companies.indexOfFirst { it.id == company.id }.let { if (it >= 0) it + 1 else 0 }
    val share = if (totalCapitalization == 0.0) 0.0 else company.capitalization / totalCapitalization * 100
    val languageType = if (company.name.matches(Regex("^[A-Za-z].*"))) {
        "Англоязычное"
    } else {
        "Русскоязычное"
    }
    val averageComparison = if (company.capitalization > averageCapitalization) {
        "Выше средней"
    } else if (company.capitalization < averageCapitalization) {
        "Ниже средней"
    } else {
        "Равна средней"
    }

    CompanyScaffold {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 20.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.Top
        ) {
            SectionHeader(
                title = TITLE_COMPANY_ANALYSIS,
                caption = company.name
            )
            Spacer(modifier = Modifier.height(28.dp))
            AnalysisItem(LABEL_COMPANY_CAPITALIZATION, formatCapitalization(company.capitalization))
            AnalysisItem(LABEL_COMPANY_SHARE, formatPercent(share) + "%")
            AnalysisItem(LABEL_COMPANY_RANK, companyRank.toString())
            AnalysisItem(LABEL_COMPANY_LANGUAGE, languageType)
            AnalysisItem(LABEL_COMPANY_NAME_LENGTH, company.name.length.toString())
            AnalysisItem(LABEL_COMPANY_VS_AVERAGE, averageComparison)
            Spacer(modifier = Modifier.height(24.dp))
            MainButton(text = BUTTON_BACK, onClick = onBack)
        }
    }
}

@Composable
private fun AnalysisScreen(
    stats: AnalysisStats,
    onBack: () -> Unit
) {
    CompanyScaffold {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 20.dp),
            verticalArrangement = Arrangement.Top
        ) {
            SectionHeader(
                title = TITLE_ANALYSIS,
                caption = "Актуально для текущего списка"
            )
            Spacer(modifier = Modifier.height(36.dp))
            AnalysisItem(LABEL_TOTAL, formatCapitalization(stats.totalCapitalization))
            AnalysisItem(LABEL_ABOVE_AVERAGE, stats.aboveAverageCount.toString())
            AnalysisItem(LABEL_ENGLISH, stats.englishNamesCount.toString())
            AnalysisItem(LABEL_MAX_CAP, stats.maxCapitalizationCompany)
            AnalysisItem(LABEL_LONGEST, stats.longestNameCompany)
            Spacer(modifier = Modifier.height(24.dp))
            MainButton(text = BUTTON_BACK, onClick = onBack)
        }
    }
}

@Composable
private fun AnalysisItem(title: String, value: String) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 14.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun CompanyScaffold(content: @Composable () -> Unit) {
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                modifier = Modifier.statusBarsPadding(),
                title = {
                    Text(
                        text = "Company Analysis",
                        color = MaterialTheme.colorScheme.onPrimary,
                        fontWeight = FontWeight.SemiBold
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background,
        content = { innerPadding ->
            Column(modifier = Modifier.padding(innerPadding)) {
                content()
            }
        }
    )
}

@Composable
private fun MainButton(
    text: String,
    onClick: () -> Unit,
    enabled: Boolean = true
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = Modifier.fillMaxWidth(),
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
            disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant,
            disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant
        ),
        contentPadding = PaddingValues(vertical = 12.dp)
    ) {
        Text(
            text = text,
            fontWeight = FontWeight.Bold
        )
    }
}

private enum class Screen {
    List,
    Analysis,
    CompanyDetails
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun CompanyListScreenPreview() {
    CompanyAnalysisTheme {
        CompanyListScreen(
            companies = listOf(
                Company(name = "Сбербанк", capitalization = 71.57),
                Company(name = "Роснефть", capitalization = 47.35),
                Company(name = "Mobile TeleSystems", capitalization = 4.63)
            ),
            input = "Tele",
            onInputChange = {},
            onDeleteClick = {},
            onRestoreClick = {},
            canRestoreDeletedCompanies = true,
            onOpenAnalysis = {},
            onCompanyClick = {}
        )
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun AnalysisScreenPreview() {
    CompanyAnalysisTheme {
        AnalysisScreen(
            stats = AnalysisStats(
                totalCapitalization = 352.18,
                aboveAverageCount = 7,
                englishNamesCount = 3,
                maxCapitalizationCompany = "Сбербанк",
                longestNameCompany =
                    "United Heavy Machinery"
            ),
            onBack = {}
        )
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun CompanyDetailsScreenPreview() {
    CompanyAnalysisTheme {
        CompanyDetailsScreen(
            company = Company(name = "Сбербанк", capitalization = 71.57),
            companies = listOf(
                Company(name = "Сбербанк", capitalization = 71.57),
                Company(name = "Роснефть", capitalization = 47.35),
                Company(name = "Mobile TeleSystems", capitalization = 4.63)
            ),
            onBack = {}
        )
    }
}

@Composable
private fun SectionHeader(title: String, caption: String) {
    Column {
        Row {
            Box(
                modifier = Modifier
                    .width(6.dp)
                    .height(54.dp)
                    .padding(top = 4.dp)
            ) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.primary,
                    content = {}
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = caption,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun CompanyRow(
    company: Company,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = company.name,
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = formatCapitalization(company.capitalization),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

private fun formatCapitalization(value: Double): String = numberFormatter.format(value)

private fun formatPercent(value: Double): String = percentFormatter.format(value)
