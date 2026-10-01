package com.example.scanneurdemdicament.data.repository

import android.content.Context
import com.example.scanneurdemdicament.data.local.PrescriptionDao
import com.example.scanneurdemdicament.data.local.PrescriptionEntity
import com.example.scanneurdemdicament.data.reminder.ReminderScheduler
import kotlinx.coroutines.flow.Flow

class PrescriptionRepository(
    private val prescriptionDao: PrescriptionDao
) {

    fun getAllPrescriptions(): Flow<List<PrescriptionEntity>> {
        return prescriptionDao.getAllPrescriptions()
    }

    suspend fun getPrescriptionById(id: Long): PrescriptionEntity? {
        return prescriptionDao.getPrescriptionById(id)
    }

    suspend fun savePrescription(
        context: Context,
        prescription: PrescriptionEntity
    ): Long {
        val finalEntity = if (prescription.isFetched) {
            prescription.copy(isReminderSet = false)
        } else {
            prescription
        }

        val id = prescriptionDao.insertOrUpdate(finalEntity)

        if (finalEntity.isFetched) {
            ReminderScheduler.cancelReminder(context, id)
        } else if (finalEntity.isReminderSet && finalEntity.reminderTimestamp != null && finalEntity.reminderTimestamp > System.currentTimeMillis()) {
            ReminderScheduler.scheduleReminder(
                context = context,
                prescriptionId = id,
                prescriptionTitle = finalEntity.title,
                reminderTimestamp = finalEntity.reminderTimestamp
            )
        }
        return id
    }

    suspend fun setReminder(
        context: Context,
        prescriptionId: Long,
        prescriptionTitle: String,
        reminderTimestamp: Long?
    ) {
        val isSet = reminderTimestamp != null && reminderTimestamp > System.currentTimeMillis()
        prescriptionDao.updateReminder(
            id = prescriptionId,
            reminderTimestamp = reminderTimestamp,
            isReminderSet = isSet
        )

        if (isSet && reminderTimestamp != null) {
            ReminderScheduler.scheduleReminder(
                context = context,
                prescriptionId = prescriptionId,
                prescriptionTitle = prescriptionTitle,
                reminderTimestamp = reminderTimestamp
            )
        } else {
            ReminderScheduler.cancelReminder(context, prescriptionId)
        }
    }

    suspend fun toggleFetchedStatus(
        context: Context,
        id: Long,
        isFetched: Boolean
    ) {
        if (isFetched) {
            // Cancel alarm notification when marked as fetched
            ReminderScheduler.cancelReminder(context, id)
            prescriptionDao.updateFetchedStatus(
                id = id,
                isFetched = true,
                isReminderSet = false
            )
        } else {
            val existing = prescriptionDao.getPrescriptionById(id)
            val reminderTime = existing?.reminderTimestamp
            val hasValidReminder = reminderTime != null && reminderTime > System.currentTimeMillis()
            prescriptionDao.updateFetchedStatus(
                id = id,
                isFetched = false,
                isReminderSet = hasValidReminder
            )
            if (hasValidReminder && reminderTime != null) {
                ReminderScheduler.scheduleReminder(
                    context = context,
                    prescriptionId = id,
                    prescriptionTitle = existing.title,
                    reminderTimestamp = reminderTime
                )
            }
        }
    }

    suspend fun deletePrescription(context: Context, id: Long) {
        ReminderScheduler.cancelReminder(context, id)
        prescriptionDao.deletePrescription(id)
    }
}