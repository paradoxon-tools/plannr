package de.chennemann.plannr.server.pockets.usecases

import de.chennemann.plannr.server.common.error.NotFoundException
import de.chennemann.plannr.server.pockets.domain.PocketQuery
import de.chennemann.plannr.server.pockets.domain.PocketQueryRepository
import org.springframework.stereotype.Component

interface GetPocketQuery {
    suspend operator fun invoke(pocketId: String): PocketQuery
}

@Component
internal class GetPocketQueryUseCase(
    private val pocketQueryRepository: PocketQueryRepository,
) : GetPocketQuery {
    override suspend fun invoke(pocketId: String): PocketQuery =
        pocketQueryRepository.findById(pocketId.trim())
            ?: throw NotFoundException(
                code = "not_found",
                message = "Pocket not found",
                details = mapOf("id" to pocketId.trim()),
            )
}
