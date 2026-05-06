package de.chennemann.plannr.server.contracts.support

import de.chennemann.plannr.server.common.error.NotFoundException
import de.chennemann.plannr.server.contracts.domain.Contract
import de.chennemann.plannr.server.contracts.service.ContractRecurringTransactionCascade
import de.chennemann.plannr.server.partners.api.dto.Partner
import de.chennemann.plannr.server.partners.api.dto.CreatePartnerCommand
import de.chennemann.plannr.server.partners.service.PartnerService
import de.chennemann.plannr.server.partners.api.dto.UpdatePartnerCommand
import de.chennemann.plannr.server.pockets.api.dto.Pocket
import de.chennemann.plannr.server.pockets.api.dto.CreatePocketCommand
import de.chennemann.plannr.server.pockets.service.PocketService
import de.chennemann.plannr.server.pockets.api.dto.UpdatePocketCommand

object ContractTestPockets {
    fun pocket(
        id: String = "poc_123",
        accountId: Long = 1L,
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

    override suspend fun delete(id: String) = throw UnsupportedOperationException("Not used in contract tests")

    override suspend fun list(accountId: Long?, archived: Boolean?): List<Pocket> = pockets.values.toList()

    override suspend fun getById(id: String): Pocket? = pockets[id.trim()]
}

class FakePartnerService(
    initialPartners: Iterable<Partner> = listOf(ContractTestPartners.partner()),
) : PartnerService {
    private val partners = initialPartners.associateByTo(linkedMapOf()) { it.id }

    override suspend fun create(command: CreatePartnerCommand): Partner = throw UnsupportedOperationException("Not used in contract tests")

    override suspend fun update(command: UpdatePartnerCommand): Partner = throw UnsupportedOperationException("Not used in contract tests")

    override suspend fun archive(id: String): Partner = throw UnsupportedOperationException("Not used in contract tests")

    override suspend fun unarchive(id: String): Partner = throw UnsupportedOperationException("Not used in contract tests")

    override suspend fun delete(id: String) = throw UnsupportedOperationException("Not used in contract tests")

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

