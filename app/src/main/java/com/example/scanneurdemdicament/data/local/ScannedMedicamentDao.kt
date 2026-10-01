package com.example.scanneurdemdicament.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ScannedMedicamentDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(medicament: ScannedMedicamentEntity)

    @Query("SELECT * FROM scanned_medicaments ORDER BY scannedTimestamp DESC")
    fun getAllScanned(): Flow<List<ScannedMedicamentEntity>>

    @Query("SELECT * FROM scanned_medicaments WHERE cip13 = :cip13 LIMIT 1")
    suspend fun getScannedByCip(cip13: String): ScannedMedicamentEntity?

    @Query("SELECT * FROM scanned_medicaments WHERE name LIKE '%' || :query || '%' OR cip13 LIKE '%' || :query || '%' ORDER BY scannedTimestamp DESC")
    fun searchHistory(query: String): Flow<List<ScannedMedicamentEntity>>

    @Query("DELETE FROM scanned_medicaments WHERE cip13 = :cip13")
    suspend fun deleteByCip(cip13: String)

    @Query("DELETE FROM scanned_medicaments")
    suspend fun clearHistory()
}