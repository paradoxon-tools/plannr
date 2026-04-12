package de.chennemann.plannr.server.query.projection

import de.chennemann.plannr.server.accounts.domain.Account
import de.chennemann.plannr.server.accounts.events.AccountCreated
import de.chennemann.plannr.server.accounts.events.AccountUpdated
import de.chennemann.plannr.server.common.events.ApplicationEventHandler
import de.chennemann.plannr.server.pockets.domain.Pocket
import de.chennemann.plannr.server.pockets.events.PocketCreated
import de.chennemann.plannr.server.pockets.events.PocketUpdated
import de.chennemann.plannr.server.query.accounts.domain.AccountQuery
import de.chennemann.plannr.server.query.accounts.domain.AccountQueryRepository
import de.chennemann.plannr.server.query.pockets.domain.PocketQuery
import de.chennemann.plannr.server.query.pockets.domain.PocketQueryRepository
import kotlin.reflect.KClass
import org.springframework.stereotype.Component

@Component
class AccountSummaryProjector(
    private val accountQueryRepository: AccountQueryRepository,
) : ApplicationEventHandler<AccountCreated> {
    override val eventType: KClass<AccountCreated> = AccountCreated::class

    override suspend fun handle(event: AccountCreated) {
        accountQueryRepository.saveOrUpdate(event.account.toQuery())
    }
}

@Component
class AccountSummaryUpdateProjector(
    private val accountQueryRepository: AccountQueryRepository,
) : ApplicationEventHandler<AccountUpdated> {
    override val eventType: KClass<AccountUpdated> = AccountUpdated::class

    override suspend fun handle(event: AccountUpdated) {
        accountQueryRepository.saveOrUpdate(event.after.toQuery())
    }
}

@Component
class PocketSummaryProjector(
    private val pocketQueryRepository: PocketQueryRepository,
) : ApplicationEventHandler<PocketCreated> {
    override val eventType: KClass<PocketCreated> = PocketCreated::class

    override suspend fun handle(event: PocketCreated) {
        pocketQueryRepository.saveOrUpdate(event.pocket.toQuery())
    }
}

@Component
class PocketSummaryUpdateProjector(
    private val pocketQueryRepository: PocketQueryRepository,
) : ApplicationEventHandler<PocketUpdated> {
    override val eventType: KClass<PocketUpdated> = PocketUpdated::class

    override suspend fun handle(event: PocketUpdated) {
        pocketQueryRepository.saveOrUpdate(event.after.toQuery())
    }
}

private fun Account.toQuery(): AccountQuery = AccountQuery(
    accountId = id,
    name = name,
    institution = institution,
    currencyCode = currencyCode,
    weekendHandling = weekendHandling,
    isArchived = isArchived,
    createdAt = createdAt,
    currentBalance = 0,
)

private fun Pocket.toQuery(): PocketQuery = PocketQuery(
    pocketId = id,
    accountId = accountId,
    name = name,
    description = description,
    color = color,
    isDefault = isDefault,
    isArchived = isArchived,
    createdAt = createdAt,
    currentBalance = 0,
)
