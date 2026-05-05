package de.chennemann.plannr.server.pockets.service

import de.chennemann.plannr.server.common.events.NoOpApplicationEventBus
import de.chennemann.plannr.server.pockets.api.dto.Pocket
import de.chennemann.plannr.server.pockets.support.InMemoryPocketRepository
import de.chennemann.plannr.server.pockets.support.PocketFixtures

internal object NoOpPocketArchiveCascade : PocketArchiveCascade {
    override suspend fun archiveFor(pocket: Pocket) = Unit

    override suspend fun unarchiveFor(pocket: Pocket) = Unit
}

internal class RecordingPocketArchiveCascade : PocketArchiveCascade {
    val archivedPocketIds = mutableListOf<String>()
    val unarchivedPocketIds = mutableListOf<String>()

    override suspend fun archiveFor(pocket: Pocket) {
        archivedPocketIds += pocket.id
    }

    override suspend fun unarchiveFor(pocket: Pocket) {
        unarchivedPocketIds += pocket.id
    }
}

internal fun pocketService(
    repository: InMemoryPocketRepository = InMemoryPocketRepository(),
    archiveCascade: PocketArchiveCascade = NoOpPocketArchiveCascade,
    accountLookup: PocketAccountLookup = PocketAccountLookup { true },
): PocketServiceImpl =
    PocketServiceImpl(
        pocketRepository = repository,
        accountLookup = accountLookup,
        archiveCascade = archiveCascade,
        timeProvider = { PocketFixtures.DEFAULT_CREATED_AT },
        applicationEventBus = NoOpApplicationEventBus,
    )
