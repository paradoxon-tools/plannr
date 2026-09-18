package de.chennemann.plannr.ui.screen.finances.management

import de.chennemann.plannr.database.repository.PartnerRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow

class PartnerManagementComponent(private val repository: PartnerRepository) {
    val partners = repository.allPartners
    val loading = MutableStateFlow(false)
    val error = MutableStateFlow<String?>(null)

    suspend fun refresh() {
        loading.value = true
        error.value = null
        try { repository.refreshManagement() }
        catch (cancelled: CancellationException) { throw cancelled }
        catch (_: Exception) { error.value = "Could not load partners. Please try again." }
        finally { loading.value = false }
    }

    suspend fun create(name: String, description: String?) = repository.addPartner(name, description)
    suspend fun save(id: Long, name: String, description: String?) = repository.updatePartner(id, name, description)
    suspend fun setArchived(id: Long, archived: Boolean) = repository.setArchived(id, archived)
    suspend fun previewLogo(website: String) = repository.previewLogo(website)
    suspend fun saveLogo(id: Long, base64: String) = repository.saveLogo(id, base64)
    suspend fun removeLogo(id: Long) = repository.removeLogo(id)
}
