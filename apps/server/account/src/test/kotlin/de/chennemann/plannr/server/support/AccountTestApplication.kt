package de.chennemann.plannr.server.support

import de.chennemann.plannr.server.pockets.api.dto.CreatePocketCommand
import de.chennemann.plannr.server.pockets.api.dto.CreateContractCommand
import de.chennemann.plannr.server.pockets.api.dto.Pocket
import de.chennemann.plannr.server.pockets.api.dto.UpdateContractCommand
import de.chennemann.plannr.server.pockets.api.dto.UpdatePocketCommand
import de.chennemann.plannr.server.pockets.service.PocketService
import de.chennemann.plannr.server.contracts.api.dto.Contract
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.context.annotation.Bean
import org.springframework.data.r2dbc.repository.config.EnableR2dbcRepositories

@SpringBootApplication(scanBasePackages = ["de.chennemann.plannr.server"])
@EnableR2dbcRepositories(basePackages = ["de.chennemann.plannr.server"])
class AccountTestApplication {
    @Bean
    fun pocketService(): PocketService =
        object : PocketService {
            override suspend fun create(command: CreatePocketCommand): Pocket = throw UnsupportedOperationException("Not used")
            override suspend fun update(command: UpdatePocketCommand): Pocket = throw UnsupportedOperationException("Not used")
            override suspend fun createContract(pocketId: Long, command: CreateContractCommand): Contract = throw UnsupportedOperationException("Not used")
            override suspend fun updateContract(pocketId: Long, command: UpdateContractCommand): Contract = throw UnsupportedOperationException("Not used")
            override suspend fun archive(id: Long): Pocket = throw UnsupportedOperationException("Not used")
            override suspend fun unarchive(id: Long): Pocket = throw UnsupportedOperationException("Not used")
            override suspend fun archiveForAccount(accountId: Long) = Unit
            override suspend fun unarchiveForAccount(accountId: Long) = Unit
            override suspend fun delete(id: Long) = throw UnsupportedOperationException("Not used")
            override suspend fun list(accountId: Long?, archived: Boolean?): List<Pocket> = throw UnsupportedOperationException("Not used")
            override suspend fun getById(id: Long): Pocket? = throw UnsupportedOperationException("Not used")
        }
}
