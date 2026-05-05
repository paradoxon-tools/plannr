package de.chennemann.plannr.server.support

import de.chennemann.plannr.server.accounts.api.dto.Account
import de.chennemann.plannr.server.accounts.service.AccountArchiveCascade
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.context.annotation.Bean
import org.springframework.data.r2dbc.repository.config.EnableR2dbcRepositories

@SpringBootApplication(scanBasePackages = ["de.chennemann.plannr.server"])
@EnableR2dbcRepositories(basePackages = ["de.chennemann.plannr.server"])
class AccountTestApplication {
    @Bean
    fun accountArchiveCascade(): AccountArchiveCascade =
        object : AccountArchiveCascade {
            override suspend fun archiveFor(account: Account) = Unit

            override suspend fun unarchiveFor(account: Account) = Unit
        }
}
