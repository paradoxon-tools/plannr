package de.chennemann.plannr.server.pockets.usecases

import de.chennemann.plannr.server.pockets.domain.PocketQuery
import de.chennemann.plannr.server.pockets.domain.PocketQueryRepository
import org.springframework.stereotype.Component

interface ListPocketQueries {
    suspend operator fun invoke(accountId: String? = null, archived: Boolean = false): List<PocketQuery>
}

@Component
internal class ListPocketQueriesUseCase(
    private val pocketQueryRepository: PocketQueryRepository,
) : ListPocketQueries {
    override suspend fun invoke(accountId: String?, archived: Boolean): List<PocketQuery> =
        pocketQueryRepository.findAll(accountId?.trim()?.takeIf { it.isNotBlank() }, archived)
}
