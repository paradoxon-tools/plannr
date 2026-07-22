package de.chennemann.plannr.server.contracts.support

import de.chennemann.plannr.server.partners.api.dto.CreatePartnerCommand
import de.chennemann.plannr.server.partners.api.dto.Partner
import de.chennemann.plannr.server.partners.api.dto.UpdatePartnerCommand
import de.chennemann.plannr.server.partners.service.PartnerService
import de.chennemann.plannr.server.pockets.api.dto.CreatePocketCommand
import de.chennemann.plannr.server.pockets.api.dto.Pocket
import de.chennemann.plannr.server.pockets.api.dto.UpdatePocketCommand
import de.chennemann.plannr.server.pockets.service.CreatePocketForContractCommand
import de.chennemann.plannr.server.pockets.service.PocketService

object ContractTestPartners {
    fun partner(
        id: Long = 1L,
        name: String = "ACME Corp",
        description: String? = "Preferred partner",
        isArchived: Boolean = false,
        createdAt: Long = 1_710_000_200L,
    ): Partner =
        Partner(id, name, description, isArchived, createdAt)
}

class FakePartnerService(
    initialPartners: Iterable<Partner> = listOf(ContractTestPartners.partner()),
) : PartnerService {
    private val partners = initialPartners.associateByTo(linkedMapOf()) { it.id }

    override suspend fun create(command: CreatePartnerCommand): Partner = throw UnsupportedOperationException("Not used")
    override suspend fun update(command: UpdatePartnerCommand): Partner = throw UnsupportedOperationException("Not used")
    override suspend fun archive(id: Long): Partner = throw UnsupportedOperationException("Not used")
    override suspend fun unarchive(id: Long): Partner = throw UnsupportedOperationException("Not used")
    override suspend fun delete(id: Long) = throw UnsupportedOperationException("Not used")
    override suspend fun list(query: String?, archived: Boolean): List<Partner> = partners.values.toList()
    override suspend fun getById(id: Long): Partner? = partners[id]
}

class FakePocketService(
    initialPockets: Iterable<Pocket> = emptyList(),
) : PocketService {
    val pockets = initialPockets.associateByTo(linkedMapOf()) { it.id }
    val contractCreateCommands = mutableListOf<CreatePocketForContractCommand>()

    override suspend fun createForContract(command: CreatePocketForContractCommand): Pocket {
        contractCreateCommands += command
        if (command.useDefaultPocket) {
            return pockets.values.firstOrNull { it.accountId == command.accountId && it.isDefault }
                ?: throw IllegalStateException("Default pocket not configured")
        }
        val id = (pockets.keys.maxOrNull() ?: 0L) + 1L
        return Pocket(
            id = id,
            accountId = command.accountId,
            name = command.name,
            description = command.description,
            color = command.color,
            isDefault = false,
            isContractPocket = true,
            isArchived = false,
            createdAt = 1_710_000_100L,
        ).also { pockets[id] = it }
    }

    override suspend fun create(command: CreatePocketCommand): Pocket = throw UnsupportedOperationException("Not used")
    override suspend fun update(command: UpdatePocketCommand): Pocket = throw UnsupportedOperationException("Not used")
    override suspend fun archive(id: Long): Pocket = throw UnsupportedOperationException("Not used")
    override suspend fun unarchive(id: Long): Pocket = throw UnsupportedOperationException("Not used")
    override suspend fun archiveForAccount(accountId: Long) = throw UnsupportedOperationException("Not used")
    override suspend fun unarchiveForAccount(accountId: Long) = throw UnsupportedOperationException("Not used")
    override suspend fun delete(id: Long) = throw UnsupportedOperationException("Not used")
    override suspend fun list(accountId: Long?, archived: Boolean?): List<Pocket> = pockets.values.toList()
    override suspend fun getById(id: Long): Pocket? = pockets[id]
}
