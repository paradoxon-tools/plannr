package de.chennemann.plannr.server.support

import de.chennemann.plannr.server.contracts.service.FakePartnerService
import de.chennemann.plannr.server.contracts.service.FakeFinancialProfileService
import de.chennemann.plannr.server.contracts.service.FakePocketService
import de.chennemann.plannr.server.contracts.service.FakeTransactionTemplateService
import de.chennemann.plannr.server.financialprofiles.service.FinancialProfileService
import de.chennemann.plannr.server.partners.service.PartnerService
import de.chennemann.plannr.server.pockets.service.PocketService
import de.chennemann.plannr.server.transactions.templates.service.TransactionTemplateService
import de.chennemann.plannr.server.common.time.TimeProvider
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.context.annotation.Bean
import org.springframework.data.r2dbc.repository.config.EnableR2dbcRepositories

@SpringBootApplication(scanBasePackages = ["de.chennemann.plannr.server.contracts"])
@EnableR2dbcRepositories(basePackages = ["de.chennemann.plannr.server.contracts"])
class ContractTestApplication {
    @Bean
    fun timeProvider(): TimeProvider = { 1_710_000_100L }

    @Bean
    fun financialProfileService(): FinancialProfileService =
        FakeFinancialProfileService()

    @Bean
    fun partnerService(): PartnerService =
        FakePartnerService(emptyList())

    @Bean
    fun pocketService(): PocketService =
        FakePocketService()

    @Bean
    fun transactionTemplateService(): TransactionTemplateService =
        FakeTransactionTemplateService()
}
