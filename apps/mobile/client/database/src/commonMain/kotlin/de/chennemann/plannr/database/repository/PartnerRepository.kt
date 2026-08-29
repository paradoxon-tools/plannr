package de.chennemann.plannr.database.repository

import de.chennemann.plannr.data.Partner
import de.chennemann.plannr.database.remote.ApiCreatePartnerCommand
import de.chennemann.plannr.database.remote.PlannrApiClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

interface PartnerRepository {
    val partners: StateFlow<List<de.chennemann.plannr.data.Partner>>

    @Throws(NoSuchElementException::class)
    suspend fun getById(partnerId: Long): de.chennemann.plannr.data.Partner
    suspend fun addPartner(name: String): Long

    companion object {
        internal operator fun invoke(apiClient: PlannrApiClient, applicationScope: CoroutineScope): PartnerRepository =
            RemotePartnerRepository(apiClient, applicationScope)
    }
}

private class RemotePartnerRepository(
    private val apiClient: PlannrApiClient,
    private val applicationScope: CoroutineScope
): PartnerRepository {
    private val refreshLock = RefreshLock()

    override val partners = MutableStateFlow<List<Partner>>(emptyList())

    init {
        applicationScope.launchRefresh("PartnerRepository.refresh") {
            refresh()
        }
    }

    override suspend fun getById(partnerId: Long): de.chennemann.plannr.data.Partner {
        return partners.value.firstOrNull { it.partnerId == partnerId }
            ?: run {
                refresh()
                partners.value.firstOrNull { it.partnerId == partnerId }
            }
            ?: throw NoSuchElementException("No partner found for id $partnerId")
    }


    override suspend fun addPartner(name: String): Long {
        val created = apiClient.createPartner(ApiCreatePartnerCommand(name = name))
        refresh()
        return created.id
    }

    private suspend fun refresh() {
        refreshLock.withRefreshLock {
            partners.value = runCatching {
                apiClient.listPartners().map { Partner(it.id, it.name) }
            }.getOrDefault(partners.value)
        }
    }
}
