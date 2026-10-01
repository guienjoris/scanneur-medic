package com.example.scanneurdemdicament.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface PrescriptionDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(prescription: PrescriptionEntity): Long

    @Query("SELECT * FROM prescriptions ORDER BY createdTimestamp DESC")
    fun getAllPrescriptions(): Flow<List<PrescriptionEntity>>

    @Query("SELECT * FROM prescriptions WHERE id = :id LIMIT 1")
    suspend fun getPrescriptionById(id: Long): PrescriptionEntity?

    @Query("DELETE FROM prescriptions WHERE id = :id")
    suspend fun deletePrescription(id: Long)

    @Query("UPDATE prescriptions SET isFetched = :isFetched, isReminderSet = :isReminderSet WHERE id = :id")
    suspend fun updateFetchedStatus(id: Long, isFetched: Boolean, isReminderSet: Boolean)

    @Query("UPDATE prescriptions SET reminderTimestamp = :reminderTimestamp, isReminderSet = :isReminderSet WHERE id = :id")
    suspend fun updateReminder(id: Long, reminderTimestamp: Long?, isReminderSet: Boolean)
}