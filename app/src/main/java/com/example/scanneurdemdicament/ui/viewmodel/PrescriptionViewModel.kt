package com.example.scanneurdemdicament.ui.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.scanneurdemdicament.data.local.PrescriptionEntity
import com.example.scanneurdemdicament.data.repository.PrescriptionRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class PrescriptionFilter {
    ALL, TO_FETCH, FETCHED
}

class PrescriptionViewModel(
    private val repository: PrescriptionRepository
) : ViewModel() {

    private val _selectedFilter = MutableStateFlow(PrescriptionFilter.ALL)
    val selectedFilter: StateFlow<PrescriptionFilter> = _selectedFilter.asStateFlow()

    @OptIn(ExperimentalCoroutinesApi::class)
    val prescriptionsState: StateFlow<List<PrescriptionEntity>> = _selectedFilter
        .flatMapLatest { filter ->
            repository.getAllPrescriptions().map { list ->
                when (filter) {
                    PrescriptionFilter.ALL -> list
                    PrescriptionFilter.TO_FETCH -> list.filter { !it.isFetched }
                    PrescriptionFilter.FETCHED -> list.filter { it.isFetched }
                }
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun onFilterSelected(filter: PrescriptionFilter) {
        _selectedFilter.value = filter
    }

    fun savePrescription(
        context: Context,
        id: Long = 0,
        title: String,
        doctorName: String? = null,
        rawText: String? = null,
        imagePath: String? = null,
        reminderTimestamp: Long? = null,
        isFetched: Boolean = false
    ) {
        viewModelScope.launch {
            val entity = PrescriptionEntity(
                id = id,
                title = title.ifBlank { "Ordonnance" },
                doctorName = doctorName,
                rawText = rawText,
                imagePath = imagePath,
                reminderTimestamp = reminderTimestamp,
                isReminderSet = reminderTimestamp != null && reminderTimestamp > System.currentTimeMillis() && !isFetched,
                isFetched = isFetched
            )
            repository.savePrescription(context, entity)
        }
    }

    fun setReminder(
        context: Context,
        prescriptionId: Long,
        prescriptionTitle: String,
        reminderTimestamp: Long?
    ) {
        viewModelScope.launch {
            repository.setReminder(context, prescriptionId, prescriptionTitle, reminderTimestamp)
        }
    }

    fun toggleFetched(context: Context, prescriptionId: Long, isFetched: Boolean) {
        viewModelScope.launch {
            repository.toggleFetchedStatus(context, prescriptionId, isFetched)
        }
    }

    fun deletePrescription(context: Context, prescriptionId: Long) {
        viewModelScope.launch {
            repository.deletePrescription(context, prescriptionId)
        }
    }
}