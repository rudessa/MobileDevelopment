package com.example.companyanalysis

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.companyanalysis.data.AnalysisStats
import com.example.companyanalysis.data.Company
import com.example.companyanalysis.data.CompanyRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MainViewModel(
    private val repository: CompanyRepository
) : ViewModel() {
    val companies: StateFlow<List<Company>> = repository.companies.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = emptyList()
    )

    val analysisStats: StateFlow<AnalysisStats> = repository.analysisStats.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = AnalysisStats()
    )

    private val _input = MutableStateFlow("")
    val input: StateFlow<String> = _input.asStateFlow()

    private val _canRestoreDeletedCompanies = MutableStateFlow(false)
    val canRestoreDeletedCompanies: StateFlow<Boolean> = _canRestoreDeletedCompanies.asStateFlow()

    private var lastDeletedCompanies: List<Company> = emptyList()

    init {
        viewModelScope.launch {
            repository.ensureSeedData()
        }
    }

    fun updateInput(value: String) {
        _input.value = value
    }

    fun deleteCompaniesByInput() {
        val substring = input.value.trim()
        if (substring.isBlank()) return

        viewModelScope.launch {
            val deletedCompanies = repository.deleteBySubstring(substring)
            lastDeletedCompanies = deletedCompanies
            _canRestoreDeletedCompanies.value = deletedCompanies.isNotEmpty()
            if (deletedCompanies.isNotEmpty()) {
                _input.value = ""
            }
        }
    }

    fun restoreDeletedCompanies() {
        if (lastDeletedCompanies.isEmpty()) return

        viewModelScope.launch {
            repository.restoreCompanies(lastDeletedCompanies)
            lastDeletedCompanies = emptyList()
            _canRestoreDeletedCompanies.value = false
        }
    }
}

class MainViewModelFactory(
    private val repository: CompanyRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MainViewModel(repository) as T
        }
        error("Unknown ViewModel class: ${modelClass.name}")
    }
}
