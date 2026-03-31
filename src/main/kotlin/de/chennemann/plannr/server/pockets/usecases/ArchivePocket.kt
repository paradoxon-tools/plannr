package de.chennemann.plannr.server.pockets.usecases

import de.chennemann.plannr.server.common.error.NotFoundException
import de.chennemann.plannr.server.pockets.domain.Pocket
import de.chennemann.plannr.server.pockets.domain.PocketRepository
import org.springframework.stereotype.Component

interface ArchivePocket {
    suspend operator fun invoke(id: String): Pocket
}

@Component
internal class ArchivePocketUseCase(
    private val pocketRepository: PocketRepository,
) : ArchivePocket {
    override suspend fun invoke(id: String): Pocket {
        val existing = pocketRepository.findById(id.trim())
            ?: throw NotFoundException(
                code = "not_found",
                message = "Pocket not found",
                details = mapOf("id" to id.trim()),
            )

        val updated = existing.copy(isArchived = true)
        return pocketRepository.update(updated)
    }
}
