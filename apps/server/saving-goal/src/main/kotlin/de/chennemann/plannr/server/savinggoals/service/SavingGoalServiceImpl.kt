package de.chennemann.plannr.server.savinggoals.service

import de.chennemann.plannr.server.accounts.api.dto.Account
import de.chennemann.plannr.server.accounts.service.AccountService
import de.chennemann.plannr.server.common.domain.normalizeCurrency
import de.chennemann.plannr.server.common.error.NotFoundException
import de.chennemann.plannr.server.common.error.ValidationException
import de.chennemann.plannr.server.common.time.TimeProvider
import de.chennemann.plannr.server.financialprofiles.service.FinancialProfileService
import de.chennemann.plannr.server.pockets.api.dto.Pocket
import de.chennemann.plannr.server.pockets.service.CreatePocketForSavingGoalCommand
import de.chennemann.plannr.server.pockets.service.PocketService
import de.chennemann.plannr.server.pockets.service.UpdatePocketsForSavingGoalCommand
import de.chennemann.plannr.server.savinggoals.api.dto.CreateSavingGoalCommand
import de.chennemann.plannr.server.savinggoals.api.dto.SavingGoal
import de.chennemann.plannr.server.savinggoals.api.dto.UpdateSavingGoalCommand
import de.chennemann.plannr.server.savinggoals.domain.SavingGoalRepository
import de.chennemann.plannr.server.savinggoals.persistence.SavingGoalModel
import de.chennemann.plannr.server.savinggoals.persistence.toDTO
import de.chennemann.plannr.server.transactions.projection.service.TransactionFeedService
import java.time.LocalDate
import java.time.format.DateTimeParseException
import kotlinx.coroutines.flow.toList
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
@Transactional
internal class SavingGoalServiceImpl(
    private val savingGoalRepository: SavingGoalRepository,
    private val financialProfileService: FinancialProfileService,
    private val accountService: AccountService,
    private val pocketService: PocketService,
    private val transactionFeedService: TransactionFeedService,
    private val timeProvider: TimeProvider,
) : SavingGoalService {
    override suspend fun create(command: CreateSavingGoalCommand): SavingGoal {
        validateName(command.name)
        validateTargetAmount(command.targetAmount)
        validateTargetDate(command.targetDate)
        val currencyCode = normalizeCurrency(command.currencyCode)
        val accounts = resolveAccounts(command.accountIds, currencyCode)
        val persisted = savingGoalRepository.save(
            SavingGoalModel(
                id = null,
                financialProfileId = financialProfileService.resolveForAssignment(command.financialProfileId).id,
                name = command.name,
                description = command.description,
                color = command.color,
                targetAmount = command.targetAmount,
                currencyCode = currencyCode,
                targetDate = command.targetDate,
                isArchived = false,
                createdAt = timeProvider(),
            ),
        )
        accounts.sortedBy(Account::id).forEach { account ->
            pocketService.createForSavingGoal(
                CreatePocketForSavingGoalCommand(
                    accountId = account.id,
                    savingGoalId = requireNotNull(persisted.id),
                    name = persisted.name,
                    description = persisted.description,
                    color = persisted.color,
                ),
            )
        }
        return enrich(persisted)
    }

    override suspend fun update(command: UpdateSavingGoalCommand): SavingGoal {
        val existing = existingGoal(command.id)
        validateName(command.name)
        validateTargetAmount(command.targetAmount)
        validateTargetDate(command.targetDate)
        val currencyCode = normalizeCurrency(command.currencyCode)
        val goalId = requireNotNull(existing.id)
        val pockets = pocketService.listForSavingGoal(goalId)
        resolveAccounts(pockets.map(Pocket::accountId).toSet(), currencyCode)
        val persisted = savingGoalRepository.save(
            SavingGoalModel(
                id = existing.id,
                financialProfileId = financialProfileService.resolveForAssignment(command.financialProfileId).id,
                name = command.name,
                description = command.description,
                color = command.color,
                targetAmount = command.targetAmount,
                currencyCode = currencyCode,
                targetDate = command.targetDate,
                isArchived = existing.isArchived,
                createdAt = existing.createdAt,
            ),
        )
        pocketService.updateForSavingGoal(
            UpdatePocketsForSavingGoalCommand(
                savingGoalId = goalId,
                name = persisted.name,
                description = persisted.description,
                color = persisted.color,
            ),
        )
        return enrich(persisted)
    }

    override suspend fun archive(id: Long): SavingGoal {
        val existing = existingGoal(id)
        val persisted = savingGoalRepository.save(existing.copy(isArchived = true))
        pocketService.archiveForSavingGoal(requireNotNull(persisted.id))
        return enrich(persisted)
    }

    override suspend fun unarchive(id: Long): SavingGoal {
        val existing = existingGoal(id)
        val persisted = savingGoalRepository.save(existing.copy(isArchived = false))
        pocketService.unarchiveForSavingGoal(requireNotNull(persisted.id))
        return enrich(persisted)
    }

    override suspend fun list(accountId: Long?, archived: Boolean): List<SavingGoal> =
        savingGoalRepository.findAllByAccountIdAndArchived(accountId, archived).toList().map { enrich(it) }

    override suspend fun getById(id: Long): SavingGoal? =
        savingGoalRepository.findById(id)?.let { enrich(it) }

    private suspend fun existingGoal(id: Long): SavingGoalModel =
        savingGoalRepository.findById(id)
            ?: throw NotFoundException("not_found", "Saving goal not found", mapOf("id" to id))

    private suspend fun enrich(goal: SavingGoalModel): SavingGoal {
        val pockets = pocketService.listForSavingGoal(requireNotNull(goal.id))
        val currentAmount = pockets.sumOf { pocket ->
            transactionFeedService.getForPocket(pocket.id, cursor = null, limit = 1).currentBalance
        }
        return goal.toDTO(
            currentAmount = currentAmount,
            accountIds = pockets.mapTo(linkedSetOf(), Pocket::accountId),
        )
    }

    private suspend fun resolveAccounts(accountIds: Set<Long>, currencyCode: String): List<Account> {
        if (accountIds.isEmpty()) {
            throw ValidationException(
                "validation_error",
                "Saving goals require at least one account",
                mapOf("field" to "accountIds"),
            )
        }
        return accountIds.map { accountId ->
            val account = accountService.getById(accountId)
                ?: throw NotFoundException("not_found", "Account not found", mapOf("id" to accountId))
            if (account.currencyCode != currencyCode) {
                throw ValidationException(
                    "validation_error",
                    "Saving goal and account currencies must match",
                    mapOf(
                        "field" to "currencyCode",
                        "accountId" to account.id,
                        "accountCurrencyCode" to account.currencyCode,
                    ),
                )
            }
            account
        }
    }

    private fun validateName(name: String) {
        if (name.isBlank()) {
            throw ValidationException(
                "validation_error",
                "Saving goal name must not be blank",
                mapOf("field" to "name"),
            )
        }
    }

    private fun validateTargetAmount(targetAmount: Long) {
        if (targetAmount <= 0) {
            throw ValidationException(
                "validation_error",
                "Saving goal target amount must be greater than zero",
                mapOf("field" to "targetAmount"),
            )
        }
    }

    private fun validateTargetDate(targetDate: String?) {
        if (targetDate == null) return
        try {
            LocalDate.parse(targetDate)
        } catch (_: DateTimeParseException) {
            throw ValidationException(
                "validation_error",
                "Saving goal target date must use ISO-8601 date format",
                mapOf("field" to "targetDate"),
            )
        }
    }
}
