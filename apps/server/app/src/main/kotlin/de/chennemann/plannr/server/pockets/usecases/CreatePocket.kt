package de.chennemann.plannr.server.pockets.usecases

import de.chennemann.plannr.server.accounts.domain.AccountRepository
import de.chennemann.plannr.server.common.error.NotFoundException
import de.chennemann.plannr.server.common.events.ApplicationEventBus
import de.chennemann.plannr.server.common.events.NoOpApplicationEventBus
import de.chennemann.plannr.server.common.time.TimeProvider
import de.chennemann.plannr.server.pockets.domain.Pocket
import de.chennemann.plannr.server.pockets.domain.PocketRepository
import de.chennemann.plannr.server.pockets.events.PocketCreated
import de.chennemann.plannr.server.pockets.support.PocketIdGenerator
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

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
@Transactional
internal class CreatePocketUseCase(
    private val pocketRepository: PocketRepository,
    private val accountRepository: AccountRepository,
    private val pocketIdGenerator: PocketIdGenerator,
    private val timeProvider: TimeProvider,
    private val applicationEventBus: ApplicationEventBus = NoOpApplicationEventBus,
) : CreatePocket {
    override suspend fun invoke(command: CreatePocket.Command): Pocket {
        val accountId = command.accountId.trim()
        accountRepository.findById(accountId)
            ?: throw NotFoundException(
                code = "not_found",
                message = "Account not found",
                details = mapOf("id" to accountId),
            )

        val pocket = Pocket(
            id = pocketIdGenerator(),
            accountId = accountId,
            name = command.name,
            description = command.description,
            color = command.color,
            isDefault = command.isDefault,
            isArchived = false,
            createdAt = timeProvider(),
        )

        val created = pocketRepository.save(pocket)
        applicationEventBus.publish(PocketCreated(created))
        return created
    }
}
