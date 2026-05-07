package de.chennemann.plannr.server.support

import de.chennemann.plannr.server.contracts.service.ContractService
import de.chennemann.plannr.server.pockets.api.dto.CreateContractCommand
import de.chennemann.plannr.server.pockets.api.dto.Pocket
import de.chennemann.plannr.server.pockets.api.dto.PocketWithContract
import de.chennemann.plannr.server.pockets.api.dto.UpdateContractCommand
import de.chennemann.plannr.server.pockets.service.PocketAccountLookup
import de.chennemann.plannr.server.transactions.recurring.service.RecurringTransactionService
import org.springframework.context.annotation.Bean
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.data.r2dbc.repository.config.EnableR2dbcRepositories

@SpringBootApplication(scanBasePackages = ["de.chennemann.plannr.server"])
@EnableR2dbcRepositories(basePackages = ["de.chennemann.plannr.server"])
class PocketTestApplication {
    @Bean
    fun pocketAccountLookup(): PocketAccountLookup =
        PocketAccountLookup { true }

    @Bean
    fun contractService(): ContractService =
        object : ContractService {
            override suspend fun create(pocket: Pocket, command: CreateContractCommand): PocketWithContract = throw UnsupportedOperationException("Not used")
            override suspend fun update(pocket: Pocket, command: UpdateContractCommand): PocketWithContract = throw UnsupportedOperationException("Not used")
            override suspend fun archiveForPocket(pocketId: Long) = Unit
            override suspend fun unarchiveForPocket(pocketId: Long) = Unit
            override suspend fun delete(id: Long) = Unit
            override suspend fun list(accountId: Long?, archived: Boolean): List<PocketWithContract> = emptyList()
        }

    @Bean
    fun recurringTransactionService(): RecurringTransactionService =
        object : RecurringTransactionService {
            override suspend fun create(command: RecurringTransactionService.CreateCommand) = throw UnsupportedOperationException("Not used")
            override suspend fun update(command: RecurringTransactionService.UpdateCommand) = throw UnsupportedOperationException("Not used")
            override suspend fun archive(id: String) = throw UnsupportedOperationException("Not used")
            override suspend fun unarchive(id: String) = throw UnsupportedOperationException("Not used")
            override suspend fun archiveForAccount(accountId: Long) = Unit
            override suspend fun unarchiveForAccount(accountId: Long) = Unit
            override suspend fun archiveForPocket(accountId: Long, pocketId: Long) = Unit
            override suspend fun unarchiveForPocket(accountId: Long, pocketId: Long) = Unit
            override suspend fun delete(id: String) = Unit
        }
}
