package de.chennemann.plannr.server.transactions.recurring.usecases

import de.chennemann.plannr.server.contracts.support.InMemoryContractRepository
import de.chennemann.plannr.server.partners.service.PartnerService
import de.chennemann.plannr.server.pockets.service.PocketService

internal fun contextResolver(
    pocketService: PocketService,
    partnerService: PartnerService,
    contractRepository: InMemoryContractRepository,
) = RecurringTransactionContextResolver(
    contractRepository = contractRepository,
    pocketService = pocketService,
    partnerService = partnerService,
)
