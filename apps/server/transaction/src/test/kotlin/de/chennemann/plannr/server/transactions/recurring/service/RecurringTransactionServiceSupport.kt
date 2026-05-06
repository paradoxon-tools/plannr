package de.chennemann.plannr.server.transactions.recurring.service

import de.chennemann.plannr.server.partners.service.PartnerService
import de.chennemann.plannr.server.pockets.service.PocketService

internal fun contextResolver(
    pocketService: PocketService,
    partnerService: PartnerService,
) = RecurringTransactionContextResolver(
    pocketService = pocketService,
    partnerService = partnerService,
)

