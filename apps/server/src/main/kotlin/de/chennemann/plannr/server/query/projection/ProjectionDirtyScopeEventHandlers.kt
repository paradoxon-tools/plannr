package de.chennemann.plannr.server.query.projection

import de.chennemann.plannr.server.accounts.events.AccountUpdated
import de.chennemann.plannr.server.common.events.ApplicationEventHandler
import de.chennemann.plannr.server.partners.events.PartnerUpdated
import de.chennemann.plannr.server.pockets.domain.PocketRepository
import de.chennemann.plannr.server.pockets.events.PocketCreated
import de.chennemann.plannr.server.pockets.events.PocketUpdated
import de.chennemann.plannr.server.transactions.events.TransactionArchived
import de.chennemann.plannr.server.transactions.events.TransactionCreated
import de.chennemann.plannr.server.transactions.events.TransactionUnarchived
import de.chennemann.plannr.server.transactions.events.TransactionUpdated
import kotlin.reflect.KClass
import org.springframework.stereotype.Component

@Component
class TransactionCreatedDirtyScopeHandler(
    private val dirtyScopeService: ProjectionDirtyScopeService,
) : ApplicationEventHandler<TransactionCreated> {
    override val eventType: KClass<TransactionCreated> = TransactionCreated::class

    override suspend fun handle(event: TransactionCreated) {
        dirtyScopeService.markAccountDirty(event.transaction.accountId)
        setOfNotNull(event.transaction.sourcePocketId, event.transaction.destinationPocketId, event.transaction.pocketId)
            .forEach { dirtyScopeService.markPocketDirty(it) }
    }
}

@Component
class TransactionUpdatedDirtyScopeHandler(
    private val dirtyScopeService: ProjectionDirtyScopeService,
) : ApplicationEventHandler<TransactionUpdated> {
    override val eventType: KClass<TransactionUpdated> = TransactionUpdated::class

    override suspend fun handle(event: TransactionUpdated) {
        setOf(event.before.accountId, event.after.accountId).forEach { dirtyScopeService.markAccountDirty(it) }
        setOfNotNull(
            event.before.pocketId,
            event.before.sourcePocketId,
            event.before.destinationPocketId,
            event.after.pocketId,
            event.after.sourcePocketId,
            event.after.destinationPocketId,
        ).forEach { dirtyScopeService.markPocketDirty(it) }
    }
}

@Component
class TransactionArchivedDirtyScopeHandler(
    private val dirtyScopeService: ProjectionDirtyScopeService,
) : ApplicationEventHandler<TransactionArchived> {
    override val eventType: KClass<TransactionArchived> = TransactionArchived::class

    override suspend fun handle(event: TransactionArchived) {
        TransactionUpdatedDirtyScopeHandler(dirtyScopeService).handle(TransactionUpdated(event.before, event.after))
    }
}

@Component
class TransactionUnarchivedDirtyScopeHandler(
    private val dirtyScopeService: ProjectionDirtyScopeService,
) : ApplicationEventHandler<TransactionUnarchived> {
    override val eventType: KClass<TransactionUnarchived> = TransactionUnarchived::class

    override suspend fun handle(event: TransactionUnarchived) {
        TransactionUpdatedDirtyScopeHandler(dirtyScopeService).handle(TransactionUpdated(event.before, event.after))
    }
}

@Component
class PocketMetadataDirtyScopeHandler(
    private val dirtyScopeService: ProjectionDirtyScopeService,
) : ApplicationEventHandler<PocketUpdated> {
    override val eventType: KClass<PocketUpdated> = PocketUpdated::class

    override suspend fun handle(event: PocketUpdated) {
        dirtyScopeService.markAccountDirty(event.after.accountId)
        dirtyScopeService.markPocketDirty(event.after.id)
    }
}

@Component
class PocketCreatedDirtyScopeHandler(
    private val dirtyScopeService: ProjectionDirtyScopeService,
) : ApplicationEventHandler<PocketCreated> {
    override val eventType: KClass<PocketCreated> = PocketCreated::class

    override suspend fun handle(event: PocketCreated) {
        dirtyScopeService.markAccountDirty(event.pocket.accountId)
        dirtyScopeService.markPocketDirty(event.pocket.id)
    }
}

@Component
class AccountMetadataDirtyScopeHandler(
    private val dirtyScopeService: ProjectionDirtyScopeService,
    private val pocketRepository: PocketRepository,
) : ApplicationEventHandler<AccountUpdated> {
    override val eventType: KClass<AccountUpdated> = AccountUpdated::class

    override suspend fun handle(event: AccountUpdated) {
        dirtyScopeService.markAccountDirty(event.after.id)
        pocketRepository.findAll(accountId = event.after.id).forEach { dirtyScopeService.markPocketDirty(it.id) }
    }
}

@Component
class PartnerMetadataDirtyScopeHandler(
    private val dirtyScopeService: ProjectionDirtyScopeService,
) : ApplicationEventHandler<PartnerUpdated> {
    override val eventType: KClass<PartnerUpdated> = PartnerUpdated::class

    override suspend fun handle(event: PartnerUpdated) {
        dirtyScopeService.markFullRebuildDirty()
    }
}
