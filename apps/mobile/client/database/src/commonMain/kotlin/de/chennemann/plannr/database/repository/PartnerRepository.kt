package de.chennemann.plannr.database.repository

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import de.chennemann.plannr.data.Partner
import de.chennemann.plannr.database.PlannrDB
import de.chennemann.plannr.database.runGettingLastId
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import migrations.Partners
import kotlin.collections.map

interface PartnerRepository {
    val partners: StateFlow<List<de.chennemann.plannr.data.Partner>>

    @Throws(NoSuchElementException::class)
    suspend fun getById(partnerId: Long): de.chennemann.plannr.data.Partner
    suspend fun addPartner(name: String): Long

    companion object {
        operator fun invoke(plannrDB: PlannrDB, applicationScope: CoroutineScope): PartnerRepository =
            CachingPartnerRepository(plannrDB, applicationScope)
    }
}

private class CachingPartnerRepository(
    private val plannrDB: PlannrDB,
    private val applicationScope: CoroutineScope
): PartnerRepository {

    override val partners: StateFlow<List<de.chennemann.plannr.data.Partner>> =
        plannrDB.partnerQueries.loadPartners()
            .asFlow()
            .mapToList(Dispatchers.IO)
            .map { loadPartners ->
                loadPartners.asDTOs()
            }
            .stateIn(
                scope = applicationScope,
                started = SharingStarted.Eagerly,
                initialValue = emptyList()
            )

    override suspend fun getById(partnerId: Long): de.chennemann.plannr.data.Partner {
        return applicationScope.async(Dispatchers.IO) {
            plannrDB.partnerQueries.getById(partnerId).executeAsOneOrNull()
        }.await()?.asDTO() ?: throw NoSuchElementException("No partner found for id $partnerId")
    }


    override suspend fun addPartner(name: String): Long {
        return applicationScope.async(Dispatchers.IO) {
            plannrDB.runGettingLastId {
                plannrDB.partnerQueries.addPartner(name)
            }
        }.await()
    }
}


private fun Partners.asDTO(): de.chennemann.plannr.data.Partner = Partner(partnerId, name)
private fun List<Partners>.asDTOs(): List<de.chennemann.plannr.data.Partner> = this.map { it.asDTO() }
