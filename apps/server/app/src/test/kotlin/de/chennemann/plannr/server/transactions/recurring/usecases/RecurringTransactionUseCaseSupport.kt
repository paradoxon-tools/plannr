package de.chennemann.plannr.server.transactions.recurring.usecases

import de.chennemann.plannr.server.contracts.support.InMemoryContractRepository
import de.chennemann.plannr.server.partners.service.PartnerService
import de.chennemann.plannr.server.pockets.support.InMemoryPocketRepository

internal fun contextResolver(
    pocketRepository: InMemoryPocketRepository,
    partnerService: PartnerService,
    contractRepository: InMemoryContractRepository,
) = RecurringTransactionContextResolver(
    contractRepository = contractRepository,
    pocketRepository = pocketRepository,
    partnerService = partnerService,
)
