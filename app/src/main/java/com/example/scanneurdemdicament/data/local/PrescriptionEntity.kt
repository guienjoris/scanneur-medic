package com.example.scanneurdemdicament.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "prescriptions")
data class PrescriptionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val doctorName: String? = null,
    val rawText: String? = null,
    val imagePath: String? = null,
    val reminderTimestamp: Long? = null,
    val isReminderSet: Boolean = false,
    val isFetched: Boolean = false,
    val createdTimestamp: Long = System.currentTimeMillis()
)