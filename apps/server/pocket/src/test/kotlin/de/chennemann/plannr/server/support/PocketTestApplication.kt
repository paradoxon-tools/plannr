package de.chennemann.plannr.server.support

import de.chennemann.plannr.server.pockets.service.PocketAccountLookup
import de.chennemann.plannr.server.transactions.templates.domain.TransactionTemplate
import de.chennemann.plannr.server.transactions.templates.service.TransactionTemplateService
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
    fun transactionTemplateService(): TransactionTemplateService =
        object : TransactionTemplateService {
            override suspend fun create(command: TransactionTemplateService.CreateCommand) = throw UnsupportedOperationException("Not used")
            override suspend fun update(command: TransactionTemplateService.UpdateCommand) = throw UnsupportedOperationException("Not used")
            override suspend fun archive(id: Long) = throw UnsupportedOperationException("Not used")
            override suspend fun unarchive(id: Long) = throw UnsupportedOperationException("Not used")
            override suspend fun archiveForPocket(pocketId: Long) = Unit
            override suspend fun unarchiveForPocket(pocketId: Long) = Unit
            override suspend fun delete(id: Long) = Unit
            override suspend fun list(archived: Boolean?): List<TransactionTemplate> = emptyList()
            override suspend fun getById(id: Long) = null
        }
}
