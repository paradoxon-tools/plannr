package de.chennemann.plannr.server.contracts.service

import de.chennemann.plannr.server.financialprofiles.api.dto.CreateFinancialProfileCommand
import de.chennemann.plannr.server.financialprofiles.api.dto.FinancialProfile
import de.chennemann.plannr.server.financialprofiles.api.dto.UpdateFinancialProfileCommand
import de.chennemann.plannr.server.financialprofiles.service.FinancialProfileService
import de.chennemann.plannr.server.partners.api.dto.CreatePartnerCommand
import de.chennemann.plannr.server.partners.api.dto.Partner
import de.chennemann.plannr.server.partners.api.dto.UpdatePartnerCommand
import de.chennemann.plannr.server.partners.service.PartnerService
import de.chennemann.plannr.server.pockets.api.dto.CreatePocketCommand
import de.chennemann.plannr.server.pockets.api.dto.Pocket
import de.chennemann.plannr.server.pockets.api.dto.UpdatePocketCommand
import de.chennemann.plannr.server.pockets.service.CreatePocketForContractCommand
import de.chennemann.plannr.server.pockets.service.PocketService
import de.chennemann.plannr.server.transactions.templates.api.dto.CreateTransactionTemplateCommand
import de.chennemann.plannr.server.transactions.templates.api.dto.UpdateTransactionTemplateCommand
import de.chennemann.plannr.server.transactions.templates.domain.TransactionTemplate
import de.chennemann.plannr.server.transactions.templates.service.TransactionTemplateService

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

class FakeFinancialProfileService(
    private val profile: FinancialProfile = FinancialProfile(
        id = 1L,
        name = "Household",
        description = null,
        isDefault = true,
        isFallback = true,
        isArchived = false,
        createdAt = 1_710_000_000L,
    ),
) : FinancialProfileService {
    override suspend fun resolveForAssignment(id: Long?): FinancialProfile =
        profile.copy(id = id ?: profile.id)

    override suspend fun getById(id: Long): FinancialProfile? = profile.takeIf { it.id == id }
    override suspend fun create(command: CreateFinancialProfileCommand): FinancialProfile = unsupported()
    override suspend fun update(command: UpdateFinancialProfileCommand): FinancialProfile = unsupported()
    override suspend fun makeDefault(id: Long): FinancialProfile = unsupported()
    override suspend fun archive(id: Long): FinancialProfile = unsupported()
    override suspend fun unarchive(id: Long): FinancialProfile = unsupported()
    override suspend fun delete(id: Long) = unsupported<Unit>()
    override suspend fun list(query: String?, archived: Boolean): List<FinancialProfile> = listOf(profile)

    private fun <T> unsupported(): T = throw UnsupportedOperationException("Not used")
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

class FakeTransactionTemplateService : TransactionTemplateService {
    val refreshedContractIds = mutableListOf<Long>()

    override suspend fun refreshFinancialProfilesForPocket(pocketId: Long) {
    }
    override suspend fun refreshFinancialProfilesForContract(contractId: Long) {
        refreshedContractIds += contractId
    }

    override suspend fun create(command: CreateTransactionTemplateCommand): TransactionTemplate = unsupported()
    override suspend fun createBatch(commands: List<CreateTransactionTemplateCommand>): List<TransactionTemplate> = unsupported()
    override suspend fun update(command: UpdateTransactionTemplateCommand): TransactionTemplate = unsupported()
    override suspend fun archive(id: Long): TransactionTemplate = unsupported()
    override suspend fun unarchive(id: Long): TransactionTemplate = unsupported()
    override suspend fun archiveForPocket(pocketId: Long) = unsupported<Unit>()
    override suspend fun unarchiveForPocket(pocketId: Long) = unsupported<Unit>()
    override suspend fun delete(id: Long) = unsupported<Unit>()
    override suspend fun list(archived: Boolean?): List<TransactionTemplate> = emptyList()
    override suspend fun getById(id: Long): TransactionTemplate? = null

    private fun <T> unsupported(): T = throw UnsupportedOperationException("Not used")
}

class FakePocketService(
    initialPockets: Iterable<Pocket> = emptyList(),
) : PocketService {
    val pockets = initialPockets.associateByTo(linkedMapOf()) { it.id }
    val contractCreateCommands = mutableListOf<CreatePocketForContractCommand>()

    override suspend fun createForContract(command: CreatePocketForContractCommand): Pocket {
        contractCreateCommands += command
        val id = (pockets.keys.maxOrNull() ?: 0L) + 1L
        return Pocket(
            id = id,
            accountId = command.accountId,
            contractId = command.contractId,
            name = "Contract ${command.contractId}",
            description = null,
            color = 0,
            isDefault = false,
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
