package com.example.scanneurdemdicament.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.scanneurdemdicament.data.repository.MedicamentRepository
import com.example.scanneurdemdicament.data.repository.PrescriptionRepository

class AppViewModelFactory(
    private val repository: MedicamentRepository,
    private val prescriptionRepository: PrescriptionRepository
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(ScannerViewModel::class.java) -> {
                ScannerViewModel(repository) as T
            }
            modelClass.isAssignableFrom(HistoryViewModel::class.java) -> {
                HistoryViewModel(repository) as T
            }
            modelClass.isAssignableFrom(SearchViewModel::class.java) -> {
                SearchViewModel(repository) as T
            }
            modelClass.isAssignableFrom(PrescriptionViewModel::class.java) -> {
                PrescriptionViewModel(prescriptionRepository) as T
            }
            else -> throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }
}