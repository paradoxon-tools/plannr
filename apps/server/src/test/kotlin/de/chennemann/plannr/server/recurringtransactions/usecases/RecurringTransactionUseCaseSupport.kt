package de.chennemann.plannr.server.recurringtransactions.usecases

import de.chennemann.plannr.server.contracts.support.InMemoryContractRepository
import de.chennemann.plannr.server.partners.support.InMemoryPartnerRepository
import de.chennemann.plannr.server.pockets.support.InMemoryPocketRepository

internal fun contextResolver(
    pocketRepository: InMemoryPocketRepository,
    partnerRepository: InMemoryPartnerRepository,
    contractRepository: InMemoryContractRepository,
) = RecurringTransactionContextResolver(
    contractRepository = contractRepository,
    pocketRepository = pocketRepository,
    partnerRepository = partnerRepository,
)
