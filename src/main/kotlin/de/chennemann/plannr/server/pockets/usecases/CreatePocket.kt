package de.chennemann.plannr.server.pockets.usecases

import de.chennemann.plannr.server.accounts.usecases.GetAccount
import de.chennemann.plannr.server.common.time.TimeProvider
import de.chennemann.plannr.server.pockets.domain.Pocket
import de.chennemann.plannr.server.pockets.domain.PocketRepository
import de.chennemann.plannr.server.pockets.support.PocketIdGenerator
import org.springframework.stereotype.Component

interface CreatePocket {
    suspend operator fun invoke(command: Command): Pocket

    data class Command(
        val accountId: String,
        val name: String,
        val description: String?,
        val color: Int,
        val isDefault: Boolean,
    )
}

@Component
internal class CreatePocketUseCase(
    private val pocketRepository: PocketRepository,
    private val getAccount: GetAccount,
    private val pocketIdGenerator: PocketIdGenerator,
    private val timeProvider: TimeProvider,
) : CreatePocket {
    override suspend fun invoke(command: CreatePocket.Command): Pocket {
        getAccount(command.accountId)

        val pocket = Pocket(
            id = pocketIdGenerator(),
            accountId = command.accountId,
            name = command.name,
            description = command.description,
            color = command.color,
            isDefault = command.isDefault,
            isArchived = false,
            createdAt = timeProvider(),
        )

        return pocketRepository.save(pocket)
    }
}
