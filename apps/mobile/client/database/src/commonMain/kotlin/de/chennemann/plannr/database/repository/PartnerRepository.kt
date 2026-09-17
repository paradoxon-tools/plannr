package de.chennemann.plannr.database.repository

import de.chennemann.plannr.data.Partner
import de.chennemann.plannr.database.remote.ApiUpdatePartnerCommand
import de.chennemann.plannr.database.remote.ApiCreatePartnerCommand
import de.chennemann.plannr.database.remote.PlannrApiClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

interface PartnerRepository {
    val partners: StateFlow<List<de.chennemann.plannr.data.Partner>>
    val allPartners: StateFlow<List<Partner>>
    suspend fun refreshManagement()
    suspend fun updatePartner(id: Long, name: String, description: String?)
    suspend fun setArchived(id: Long, archived: Boolean)

    @Throws(NoSuchElementException::class)
    suspend fun getById(partnerId: Long): de.chennemann.plannr.data.Partner
    suspend fun addPartner(name: String, description: String? = null): Long
    suspend fun previewLogo(website: String): String
    suspend fun saveLogo(partnerId: Long, base64: String)
    suspend fun removeLogo(partnerId: Long)

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
    override val allPartners = MutableStateFlow<List<Partner>>(emptyList())

    init {
        applicationScope.launchRefresh("PartnerRepository.refresh") {
            refresh()
        }
    }

    override suspend fun getById(partnerId: Long): de.chennemann.plannr.data.Partner {
        return allPartners.value.firstOrNull { it.partnerId == partnerId }
            ?: run {
                refresh()
                allPartners.value.firstOrNull { it.partnerId == partnerId }
            }
            ?: throw NoSuchElementException("No partner found for id $partnerId")
    }


    override suspend fun addPartner(name: String, description: String?): Long {
        require(name.isNotBlank()) { "Partner name is required" }
        val created = apiClient.createPartner(ApiCreatePartnerCommand(name = name.trim(), description = description?.trim()?.ifBlank { null }))
        publish(created.toPartner())
        return created.id
    }

    override suspend fun previewLogo(website: String): String = apiClient.previewPartnerLogo(website)

    override suspend fun saveLogo(partnerId: Long, base64: String) {
        val updated = apiClient.savePartnerLogo(partnerId, base64).toPartner()
        publish(updated)
    }

    override suspend fun removeLogo(partnerId: Long) {
        val updated = apiClient.removePartnerLogo(partnerId).toPartner()
        publish(updated)
    }

    override suspend fun refreshManagement() {
        refreshLock.withRefreshLock {
            val active = apiClient.listPartners().map { it.toPartner() }
            val archived = apiClient.listPartners(archived = true).map { it.toPartner() }
            allPartners.value = (active + archived).sortedBy { it.name.lowercase() }
            partners.value = active
        }
    }

    override suspend fun updatePartner(id: Long, name: String, description: String?) {
        require(name.isNotBlank()) { "Partner name is required" }
        publish(apiClient.updatePartner(ApiUpdatePartnerCommand(id, name.trim(), description?.trim()?.ifBlank { null })).toPartner())
    }

    override suspend fun setArchived(id: Long, archived: Boolean) {
        publish(apiClient.archivePartner(id, archived).toPartner())
    }

    private fun publish(partner: Partner) {
        allPartners.value = (allPartners.value.filterNot { it.partnerId == partner.partnerId } + partner).sortedBy { it.name.lowercase() }
        partners.value = allPartners.value.filterNot { it.isArchived }
    }

    private suspend fun refresh() = refreshManagement()
}
