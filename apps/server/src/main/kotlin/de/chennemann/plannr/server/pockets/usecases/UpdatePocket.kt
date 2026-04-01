package de.chennemann.plannr.server.pockets.usecases

import de.chennemann.plannr.server.accounts.usecases.GetAccount
import de.chennemann.plannr.server.common.error.NotFoundException
import de.chennemann.plannr.server.pockets.domain.Pocket
import de.chennemann.plannr.server.pockets.domain.PocketRepository
import org.springframework.stereotype.Component

interface UpdatePocket {
    suspend operator fun invoke(command: Command): Pocket

    data class Command(
        val id: String,
        val accountId: String,
        val name: String,
        val description: String?,
        val color: Int,
        val isDefault: Boolean,
    )
}

@Component
internal class UpdatePocketUseCase(
    private val pocketRepository: PocketRepository,
    private val getAccount: GetAccount,
) : UpdatePocket {
    override suspend fun invoke(command: UpdatePocket.Command): Pocket {
        val existing = pocketRepository.findById(command.id.trim())
            ?: throw NotFoundException(
                code = "not_found",
                message = "Pocket not found",
                details = mapOf("id" to command.id.trim()),
            )

        getAccount(command.accountId)

        val updated = Pocket(
            id = existing.id,
            accountId = command.accountId,
            name = command.name,
            description = command.description,
            color = command.color,
            isDefault = command.isDefault,
            isArchived = existing.isArchived,
            createdAt = existing.createdAt,
        )

        return pocketRepository.update(updated)
    }
}
