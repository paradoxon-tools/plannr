package de.chennemann.plannr.ui.screen.finances.contracts.add

import de.chennemann.plannr.database.repository.AccountRepository
import de.chennemann.plannr.database.repository.ContractRepository
import de.chennemann.plannr.database.repository.PartnerRepository

interface AddContractComponent {
    val accounts: kotlinx.coroutines.flow.StateFlow<List<de.chennemann.plannr.data.Account>>
    val partners: kotlinx.coroutines.flow.StateFlow<List<de.chennemann.plannr.data.Partner>>

    suspend fun addContract(
        accountId: Long,
        partnerId: Long,
        name: String,
        description: String?
    )

    suspend fun addContract(contractName: String)

    class Factory(
        private val contractRepository: ContractRepository,
        private val accountRepository: AccountRepository,
        private val partnerRepository: PartnerRepository,
    ) {
        operator fun invoke(): AddContractComponent =
            DefaultAddContractComponent(
                contractRepository = contractRepository,
                accountRepository = accountRepository,
                partnerRepository = partnerRepository,
            )
    }
}

private class DefaultAddContractComponent(
    private val contractRepository: ContractRepository,
    private val accountRepository: AccountRepository,
    private val partnerRepository: PartnerRepository,
): AddContractComponent {

    override val accounts = accountRepository.accounts
    override val partners = partnerRepository.partners

    override suspend fun addContract(accountId: Long, partnerId: Long, name: String, description: String?) {
        contractRepository.addContract(
            accountId = accountId,
            partnerId = partnerId,
            name = name,
            description = description
        )
    }

    override suspend fun addContract(contractName: String) {
        val accountId = accountRepository.accounts.value.firstOrNull()?.accountId ?: return
        val partnerId = partnerRepository.addPartner(contractName)
        addContract(
            accountId = accountId,
            partnerId = partnerId,
            name = contractName,
            description = null
        )
    }
}
