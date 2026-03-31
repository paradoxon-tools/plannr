package de.chennemann.plannr.server.pockets.usecases

import de.chennemann.plannr.server.common.error.NotFoundException
import de.chennemann.plannr.server.pockets.domain.Pocket
import de.chennemann.plannr.server.pockets.domain.PocketRepository
import org.springframework.stereotype.Component

interface UnarchivePocket {
    suspend operator fun invoke(id: String): Pocket
}

@Component
internal class UnarchivePocketUseCase(
    private val pocketRepository: PocketRepository,
) : UnarchivePocket {
    override suspend fun invoke(id: String): Pocket {
        val existing = pocketRepository.findById(id.trim())
            ?: throw NotFoundException(
                code = "not_found",
                message = "Pocket not found",
                details = mapOf("id" to id.trim()),
            )

        val updated = existing.copy(isArchived = false)
        return pocketRepository.update(updated)
    }
}
