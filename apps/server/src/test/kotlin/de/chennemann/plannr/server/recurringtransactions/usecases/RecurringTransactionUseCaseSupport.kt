package de.chennemann.plannr.server.recurringtransactions.usecases

import de.chennemann.plannr.server.contracts.support.InMemoryContractRepository
import de.chennemann.plannr.server.contracts.usecases.GetContractUseCase
import de.chennemann.plannr.server.partners.support.InMemoryPartnerRepository
import de.chennemann.plannr.server.partners.usecases.GetPartnerUseCase
import de.chennemann.plannr.server.pockets.support.InMemoryPocketRepository
import de.chennemann.plannr.server.pockets.usecases.GetPocketUseCase

internal fun contextResolver(
    pocketRepository: InMemoryPocketRepository,
    partnerRepository: InMemoryPartnerRepository,
    contractRepository: InMemoryContractRepository,
) = RecurringTransactionContextResolver(
    getContract = GetContractUseCase(contractRepository),
    getPocket = GetPocketUseCase(pocketRepository),
    getPartner = GetPartnerUseCase(partnerRepository),
)
