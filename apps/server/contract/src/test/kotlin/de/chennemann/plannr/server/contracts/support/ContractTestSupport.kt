package de.chennemann.plannr.server.contracts.support

import de.chennemann.plannr.server.common.error.NotFoundException
import de.chennemann.plannr.server.partners.api.dto.Partner
import de.chennemann.plannr.server.partners.api.dto.CreatePartnerCommand
import de.chennemann.plannr.server.partners.service.PartnerService
import de.chennemann.plannr.server.partners.api.dto.UpdatePartnerCommand
import de.chennemann.plannr.server.pockets.api.dto.Pocket

object ContractTestPockets {
    fun pocket(
        id: Long = 1L,
        accountId: Long = 1L,
        name: String = "Bills",
        description: String? = "Monthly fixed costs",
        color: Int = 123456,
        isDefault: Boolean = false,
        isContractPocket: Boolean = false,
        isArchived: Boolean = false,
        createdAt: Long = 1_710_000_100L,
    ): Pocket =
        Pocket(id, accountId, name, description, color, isDefault, isContractPocket, isArchived, createdAt)
}

object ContractTestPartners {
    fun partner(
        id: Long = 1L,
        name: String = "ACME Corp",
        notes: String? = "Preferred partner",
        isArchived: Boolean = false,
        createdAt: Long = 1_710_000_200L,
    ): Partner =
        Partner(id, name, notes, isArchived, createdAt)
}

class FakePartnerService(
    initialPartners: Iterable<Partner> = listOf(ContractTestPartners.partner()),
) : PartnerService {
    private val partners = initialPartners.associateByTo(linkedMapOf()) { it.id }

    override suspend fun create(command: CreatePartnerCommand): Partner = throw UnsupportedOperationException("Not used in contract tests")

    override suspend fun update(command: UpdatePartnerCommand): Partner = throw UnsupportedOperationException("Not used in contract tests")

    override suspend fun archive(id: Long): Partner = throw UnsupportedOperationException("Not used in contract tests")

    override suspend fun unarchive(id: Long): Partner = throw UnsupportedOperationException("Not used in contract tests")

    override suspend fun delete(id: Long) = throw UnsupportedOperationException("Not used in contract tests")

    override suspend fun list(query: String?, archived: Boolean): List<Partner> = partners.values.toList()

    override suspend fun getById(id: Long): Partner? = partners[id]
}

