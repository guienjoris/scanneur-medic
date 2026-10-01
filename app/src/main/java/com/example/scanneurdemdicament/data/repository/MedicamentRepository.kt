package com.example.scanneurdemdicament.data.repository

import com.example.scanneurdemdicament.data.local.ScannedMedicamentDao
import com.example.scanneurdemdicament.data.local.ScannedMedicamentEntity
import com.example.scanneurdemdicament.data.remote.MedicamentApiService
import com.example.scanneurdemdicament.data.remote.MedicamentDto
import kotlinx.coroutines.flow.Flow

class MedicamentRepository(
    private val apiService: MedicamentApiService,
    private val dao: ScannedMedicamentDao
) {

    fun getHistory(): Flow<List<ScannedMedicamentEntity>> {
        return dao.getAllScanned()
    }

    fun searchHistory(query: String): Flow<List<ScannedMedicamentEntity>> {
        return dao.searchHistory(query)
    }

    suspend fun deleteFromHistory(cip13: String) {
        dao.deleteByCip(cip13)
    }

    suspend fun clearHistory() {
        dao.clearHistory()
    }

    suspend fun fetchMedicamentByCip(
        cip13: String,
        lotNumber: String? = null,
        expirationDate: String? = null
    ): Result<ScannedMedicamentEntity> {
        return try {
            val response = apiService.getMedicamentByCip(cip13)
            if (response.isSuccessful && response.body() != null) {
                val dto = response.body()!!
                val entity = mapDtoToEntity(
                    dto = dto,
                    targetCip13 = cip13,
                    lotNumber = lotNumber,
                    expirationDate = expirationDate
                )
                dao.insertOrUpdate(entity)
                Result.success(entity)
            } else {
                val cached = dao.getScannedByCip(cip13)
                if (cached != null) {
                    Result.success(cached)
                } else {
                    Result.failure(Exception("Medicament non trouvé pour le code CIP: $cip13"))
                }
            }
        } catch (e: Exception) {
            val cached = dao.getScannedByCip(cip13)
            if (cached != null) {
                Result.success(cached)
            } else {
                Result.failure(e)
            }
        }
    }

    suspend fun searchMedicamentsOnline(query: String): Result<List<MedicamentDto>> {
        return try {
            val response = apiService.searchMedicaments(query)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Erreur de recherche: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun saveDtoToHistory(
        dto: MedicamentDto,
        cip13: String
    ): ScannedMedicamentEntity {
        val entity = mapDtoToEntity(dto, cip13)
        dao.insertOrUpdate(entity)
        return entity
    }

    private fun mapDtoToEntity(
        dto: MedicamentDto,
        targetCip13: String,
        lotNumber: String? = null,
        expirationDate: String? = null
    ): ScannedMedicamentEntity {
        val targetCipLong = targetCip13.toLongOrNull()
        val presentation = dto.presentations?.firstOrNull { it.cip13 == targetCipLong }
            ?: dto.presentations?.firstOrNull()

        val activeSubstances = dto.composition?.mapNotNull { comp ->
            val name = comp.substanceName ?: return@mapNotNull null
            val dosage = comp.dosage
            if (!dosage.isNullOrBlank()) "$name ($dosage)" else name
        } ?: emptyList()

        val price = presentation?.publicPrice ?: presentation?.price
        val rate = presentation?.reimbursementRate ?: "Non renseigné"

        return ScannedMedicamentEntity(
            cip13 = targetCip13,
            cis = dto.cis,
            name = dto.name ?: "Médicament inconnu",
            pharmaceuticalForm = dto.pharmaceuticalForm ?: "Non précisée",
            administrationRoutes = dto.administrationRoutes ?: emptyList(),
            activeSubstances = activeSubstances,
            reimbursementRate = if (rate.isBlank()) "Non renseigné" else rate,
            publicPrice = price,
            conditions = dto.conditions ?: emptyList(),
            scannedTimestamp = System.currentTimeMillis(),
            lotNumber = lotNumber,
            expirationDate = expirationDate
        )
    }
}