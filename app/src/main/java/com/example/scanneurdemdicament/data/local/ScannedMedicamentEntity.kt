package com.example.scanneurdemdicament.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "scanned_medicaments")
data class ScannedMedicamentEntity(
    @PrimaryKey
    val cip13: String,
    val cis: Long,
    val name: String,
    val pharmaceuticalForm: String,
    val administrationRoutes: List<String>,
    val activeSubstances: List<String>,
    val reimbursementRate: String,
    val publicPrice: Double?,
    val conditions: List<String>,
    val scannedTimestamp: Long,
    val lotNumber: String? = null,
    val expirationDate: String? = null
)