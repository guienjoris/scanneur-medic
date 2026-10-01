package com.example.scanneurdemdicament.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.scanneurdemdicament.data.local.ScannedMedicamentEntity
import com.example.scanneurdemdicament.data.parser.ParsedGS1Data
import com.example.scanneurdemdicament.data.repository.MedicamentRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface ScannerUiState {
    object Idle : ScannerUiState
    object Loading : ScannerUiState
    data class Success(val medicament: ScannedMedicamentEntity) : ScannerUiState
    data class Error(val message: String) : ScannerUiState
}

class ScannerViewModel(
    private val repository: MedicamentRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<ScannerUiState>(ScannerUiState.Idle)
    val uiState: StateFlow<ScannerUiState> = _uiState.asStateFlow()

    fun onBarcodeScanned(parsed: ParsedGS1Data) {
        val cip = parsed.cip13
        if (cip.isNullOrBlank()) {
            _uiState.value = ScannerUiState.Error("Code-barres scanné non reconnu comme médicament CIP.")
            return
        }

        fetchMedicament(cip, parsed.lotNumber, parsed.expirationDate)
    }

    fun fetchMedicamentByManualCip(cip: String) {
        val cleanCip = cip.trim()
        if (cleanCip.isBlank()) return
        fetchMedicament(cleanCip)
    }

    private fun fetchMedicament(cip13: String, lot: String? = null, exp: String? = null) {
        viewModelScope.launch {
            _uiState.value = ScannerUiState.Loading
            val result = repository.fetchMedicamentByCip(cip13, lot, exp)
            result.fold(
                onSuccess = { entity ->
                    _uiState.value = ScannerUiState.Success(entity)
                },
                onFailure = { error ->
                    _uiState.value = ScannerUiState.Error(
                        error.localizedMessage ?: "Erreur lors de la récupération du médicament"
                    )
                }
            )
        }
    }

    fun dismissState() {
        _uiState.value = ScannerUiState.Idle
    }
}