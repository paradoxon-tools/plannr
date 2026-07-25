package de.chennemann.plannr.server.support

import de.chennemann.plannr.server.accounts.api.dto.Account
import de.chennemann.plannr.server.accounts.api.dto.CreateAccountCommand
import de.chennemann.plannr.server.accounts.api.dto.UpdateAccountCommand
import de.chennemann.plannr.server.accounts.service.AccountService
import de.chennemann.plannr.server.transactions.templates.api.dto.CreateTransactionTemplateCommand
import de.chennemann.plannr.server.transactions.templates.api.dto.UpdateTransactionTemplateCommand
import de.chennemann.plannr.server.transactions.templates.domain.TransactionTemplate
import de.chennemann.plannr.server.transactions.templates.service.TransactionTemplateService
import org.springframework.context.annotation.Bean
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.data.r2dbc.repository.config.EnableR2dbcRepositories

@SpringBootApplication(scanBasePackages = ["de.chennemann.plannr.server"])
@EnableR2dbcRepositories(basePackages = ["de.chennemann.plannr.server"])
class PocketTestApplication {
    @Bean
    fun accountService(): AccountService =
        object : AccountService {
            override suspend fun create(command: CreateAccountCommand): Account = unsupported()
            override suspend fun update(command: UpdateAccountCommand): Account = unsupported()
            override suspend fun archive(id: Long): Account = unsupported()
            override suspend fun unarchive(id: Long): Account = unsupported()
            override suspend fun delete(id: Long) = unsupported<Unit>()
            override suspend fun list(archived: Boolean?): List<Account> = emptyList()
            override suspend fun getById(id: Long): Account =
                Account(
                    id = id,
                    name = "Account $id",
                    institution = "Test",
                    currencyCode = "EUR",
                    weekendHandling = "NO_SHIFT",
                    isArchived = false,
                    createdAt = 0L,
                )

            private fun <T> unsupported(): T = throw UnsupportedOperationException("Not used")
        }

    @Bean
    fun transactionTemplateService(): TransactionTemplateService =
        object : TransactionTemplateService {
            override suspend fun create(command: CreateTransactionTemplateCommand) = throw UnsupportedOperationException("Not used")
            override suspend fun createBatch(commands: List<CreateTransactionTemplateCommand>) = throw UnsupportedOperationException("Not used")
            override suspend fun update(command: UpdateTransactionTemplateCommand) = throw UnsupportedOperationException("Not used")
            override suspend fun archive(id: Long) = throw UnsupportedOperationException("Not used")
            override suspend fun unarchive(id: Long) = throw UnsupportedOperationException("Not used")
            override suspend fun archiveForPocket(pocketId: Long) = Unit
            override suspend fun unarchiveForPocket(pocketId: Long) = Unit
            override suspend fun refreshFinancialProfilesForPocket(pocketId: Long) = Unit
            override suspend fun delete(id: Long) = Unit
            override suspend fun list(archived: Boolean?): List<TransactionTemplate> = emptyList()
            override suspend fun getById(id: Long) = null
        }
}
