package de.chennemann.plannr.server.pockets.usecases

import de.chennemann.plannr.server.pockets.domain.Pocket
import de.chennemann.plannr.server.pockets.domain.PocketRepository
import org.springframework.stereotype.Component

interface ListPockets {
    suspend operator fun invoke(accountId: String? = null, archived: Boolean? = null): List<Pocket>
}

@Component
internal class ListPocketsUseCase(
    private val pocketRepository: PocketRepository,
) : ListPockets {
    override suspend fun invoke(accountId: String?, archived: Boolean?): List<Pocket> =
        pocketRepository.findAll(accountId = accountId?.trim(), archived = archived)
}
