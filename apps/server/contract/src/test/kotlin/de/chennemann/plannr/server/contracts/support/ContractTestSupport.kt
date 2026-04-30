package de.chennemann.plannr.server.contracts.support

import de.chennemann.plannr.server.common.error.NotFoundException
import de.chennemann.plannr.server.contracts.domain.Contract
import de.chennemann.plannr.server.contracts.usecases.ContractRecurringTransactionCascade
import de.chennemann.plannr.server.partners.domain.Partner
import de.chennemann.plannr.server.partners.service.CreatePartnerCommand
import de.chennemann.plannr.server.partners.service.PartnerService
import de.chennemann.plannr.server.partners.service.UpdatePartnerCommand
import de.chennemann.plannr.server.pockets.domain.Pocket
import de.chennemann.plannr.server.pockets.domain.PocketQuery
import de.chennemann.plannr.server.pockets.service.CreatePocketCommand
import de.chennemann.plannr.server.pockets.service.PocketService
import de.chennemann.plannr.server.pockets.service.UpdatePocketCommand

object ContractTestPockets {
    fun pocket(
        id: String = "poc_123",
        accountId: String = "acc_123",
        name: String = "Bills",
        description: String? = "Monthly fixed costs",
        color: Int = 123456,
        isDefault: Boolean = false,
        isArchived: Boolean = false,
        createdAt: Long = 1_710_000_100L,
    ): Pocket =
        Pocket(id, accountId, name, description, color, isDefault, isArchived, createdAt)
}

object ContractTestPartners {
    fun partner(
        id: String = "par_123",
        name: String = "ACME Corp",
        notes: String? = "Preferred partner",
        isArchived: Boolean = false,
        createdAt: Long = 1_710_000_200L,
    ): Partner =
        Partner(id, name, notes, isArchived, createdAt)
}

class FakePocketService(
    initialPockets: Iterable<Pocket> = listOf(ContractTestPockets.pocket()),
) : PocketService {
    private val pockets = initialPockets.associateByTo(linkedMapOf()) { it.id }

    override suspend fun create(command: CreatePocketCommand): Pocket = throw UnsupportedOperationException("Not used in contract tests")

    override suspend fun update(command: UpdatePocketCommand): Pocket = throw UnsupportedOperationException("Not used in contract tests")

    override suspend fun archive(id: String): Pocket = throw UnsupportedOperationException("Not used in contract tests")

    override suspend fun unarchive(id: String): Pocket = throw UnsupportedOperationException("Not used in contract tests")

    override suspend fun list(accountId: String?, archived: Boolean?): List<Pocket> = pockets.values.toList()

    override suspend fun getById(id: String): Pocket? = pockets[id.trim()]

    override suspend fun listQueries(accountId: String?, archived: Boolean): List<PocketQuery> =
        pockets.values.map {
            PocketQuery(
                pocketId = it.id,
                accountId = it.accountId,
                name = it.name,
                description = it.description,
                color = it.color,
                isDefault = it.isDefault,
                isArchived = it.isArchived,
                createdAt = it.createdAt,
                currentBalance = 0,
            )
        }

    override suspend fun getQuery(id: String): PocketQuery {
        val pocket = pockets[id.trim()]
            ?: throw NotFoundException("not_found", "Pocket not found", mapOf("id" to id.trim()))
        return PocketQuery(
            pocketId = pocket.id,
            accountId = pocket.accountId,
            name = pocket.name,
            description = pocket.description,
            color = pocket.color,
            isDefault = pocket.isDefault,
            isArchived = pocket.isArchived,
            createdAt = pocket.createdAt,
            currentBalance = 0,
        )
    }
}

class FakePartnerService(
    initialPartners: Iterable<Partner> = listOf(ContractTestPartners.partner()),
) : PartnerService {
    private val partners = initialPartners.associateByTo(linkedMapOf()) { it.id }

    override suspend fun create(command: CreatePartnerCommand): Partner = throw UnsupportedOperationException("Not used in contract tests")

    override suspend fun update(command: UpdatePartnerCommand): Partner = throw UnsupportedOperationException("Not used in contract tests")

    override suspend fun archive(id: String): Partner = throw UnsupportedOperationException("Not used in contract tests")

    override suspend fun unarchive(id: String): Partner = throw UnsupportedOperationException("Not used in contract tests")

    override suspend fun list(query: String?, archived: Boolean): List<Partner> = partners.values.toList()

    override suspend fun getById(id: String): Partner? = partners[id.trim()]
}

class RecordingContractRecurringTransactionCascade : ContractRecurringTransactionCascade {
    val archivedContracts = mutableListOf<String>()
    val unarchivedContracts = mutableListOf<String>()

    override suspend fun archiveFor(contract: Contract) {
        archivedContracts += contract.id
    }

    override suspend fun unarchiveFor(contract: Contract) {
        unarchivedContracts += contract.id
    }
}
