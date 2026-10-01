package com.example.scanneurdemdicament.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.scanneurdemdicament.data.local.ScannedMedicamentEntity
import com.example.scanneurdemdicament.data.remote.MedicamentDto
import com.example.scanneurdemdicament.data.repository.MedicamentRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface SearchUiState {
    object Idle : SearchUiState
    object Loading : SearchUiState
    data class Success(val results: List<MedicamentDto>) : SearchUiState
    data class Error(val message: String) : SearchUiState
}

class SearchViewModel(
    private val repository: MedicamentRepository
) : ViewModel() {

    private val _query = MutableStateFlow("")
    val query: StateFlow<String> = _query.asStateFlow()

    private val _uiState = MutableStateFlow<SearchUiState>(SearchUiState.Idle)
    val uiState: StateFlow<SearchUiState> = _uiState.asStateFlow()

    private val _selectedMedicament = MutableStateFlow<ScannedMedicamentEntity?>(null)
    val selectedMedicament: StateFlow<ScannedMedicamentEntity?> = _selectedMedicament.asStateFlow()

    fun onQueryChanged(newQuery: String) {
        _query.value = newQuery
    }

    fun search() {
        val q = _query.value.trim()
        if (q.isBlank()) return

        viewModelScope.launch {
            _uiState.value = SearchUiState.Loading
            val result = repository.searchMedicamentsOnline(q)
            result.fold(
                onSuccess = { list ->
                    _uiState.value = SearchUiState.Success(list)
                },
                onFailure = { err ->
                    _uiState.value = SearchUiState.Error(err.localizedMessage ?: "Aucun résultat trouvé.")
                }
            )
        }
    }

    fun onMedicamentSelected(dto: MedicamentDto) {
        val cip13 = dto.presentations?.firstOrNull()?.cip13?.toString() ?: dto.cis.toString()
        viewModelScope.launch {
            val entity = repository.saveDtoToHistory(dto, cip13)
            _selectedMedicament.value = entity
        }
    }

    fun dismissDetail() {
        _selectedMedicament.value = null
    }
}