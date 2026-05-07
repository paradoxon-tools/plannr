package de.chennemann.plannr.server.support

import de.chennemann.plannr.server.partners.api.dto.CreatePartnerCommand
import de.chennemann.plannr.server.partners.api.dto.Partner
import de.chennemann.plannr.server.partners.api.dto.UpdatePartnerCommand
import de.chennemann.plannr.server.partners.service.PartnerService
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
    fun partnerService(): PartnerService =
        object : PartnerService {
            override suspend fun create(command: CreatePartnerCommand): Partner = throw UnsupportedOperationException("Not used")
            override suspend fun update(command: UpdatePartnerCommand): Partner = throw UnsupportedOperationException("Not used")
            override suspend fun archive(id: Long): Partner = throw UnsupportedOperationException("Not used")
            override suspend fun unarchive(id: Long): Partner = throw UnsupportedOperationException("Not used")
            override suspend fun delete(id: Long) = throw UnsupportedOperationException("Not used")
            override suspend fun list(query: String?, archived: Boolean): List<Partner> = emptyList()
            override suspend fun getById(id: Long): Partner? = null
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
