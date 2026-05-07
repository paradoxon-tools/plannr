package de.chennemann.plannr.server.support

import de.chennemann.plannr.server.accounts.api.dto.Account
import de.chennemann.plannr.server.accounts.domain.AccountRepository
import de.chennemann.plannr.server.accounts.persistence.toDomain
import de.chennemann.plannr.server.accounts.service.AccountService
import de.chennemann.plannr.server.accounts.api.dto.CreateAccountCommand
import de.chennemann.plannr.server.accounts.api.dto.UpdateAccountCommand
import de.chennemann.plannr.server.common.error.NotFoundException
import de.chennemann.plannr.server.partners.api.dto.Partner
import de.chennemann.plannr.server.partners.domain.PartnerRepository
import de.chennemann.plannr.server.partners.api.dto.CreatePartnerCommand
import de.chennemann.plannr.server.partners.persistence.PartnerModel
import de.chennemann.plannr.server.partners.service.PartnerService
import de.chennemann.plannr.server.partners.api.dto.UpdatePartnerCommand
import de.chennemann.plannr.server.pockets.api.dto.Pocket
import de.chennemann.plannr.server.pockets.api.dto.PocketWithContract
import de.chennemann.plannr.server.pockets.domain.PocketRepository
import de.chennemann.plannr.server.pockets.api.dto.CreatePocketCommand
import de.chennemann.plannr.server.pockets.service.PocketService
import de.chennemann.plannr.server.pockets.api.dto.UpdateContractCommand
import de.chennemann.plannr.server.pockets.api.dto.UpdatePocketCommand
import kotlinx.coroutines.flow.toList
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.context.annotation.Bean
import org.springframework.data.r2dbc.repository.config.EnableR2dbcRepositories

@SpringBootApplication(
    scanBasePackages = [
        "de.chennemann.plannr.server.common",
        "de.chennemann.plannr.server.transactions",
        "de.chennemann.plannr.server.accounts.persistence",
        "de.chennemann.plannr.server.partners.persistence",
        "de.chennemann.plannr.server.pockets.persistence",
        "de.chennemann.plannr.server.contracts.persistence",
    ],
)
@EnableR2dbcRepositories(
    basePackages = [
        "de.chennemann.plannr.server.accounts.domain",
        "de.chennemann.plannr.server.partners.domain",
        "de.chennemann.plannr.server.pockets.domain",
        "de.chennemann.plannr.server.contracts.domain",
    ],
)
class TransactionTestApplication {
    @Bean
    fun accountService(accountRepository: AccountRepository): AccountService =
        object : AccountService {
            override suspend fun create(command: CreateAccountCommand): Account =
                throw UnsupportedOperationException("Not used in transaction tests")

            override suspend fun update(command: UpdateAccountCommand): Account =
                throw UnsupportedOperationException("Not used in transaction tests")

            override suspend fun archive(id: Long): Account =
                throw UnsupportedOperationException("Not used in transaction tests")

            override suspend fun unarchive(id: Long): Account =
                throw UnsupportedOperationException("Not used in transaction tests")

            override suspend fun delete(id: Long) =
                throw UnsupportedOperationException("Not used in transaction tests")

            override suspend fun list(archived: Boolean?): List<Account> =
                throw UnsupportedOperationException("Not used in transaction tests")

            override suspend fun getById(id: Long): Account? =
                accountRepository.findById(id)?.toDomain()
        }

    @Bean
    fun pocketService(pocketRepository: PocketRepository): PocketService =
        object : PocketService {
            override suspend fun create(command: CreatePocketCommand): Pocket =
                throw UnsupportedOperationException("Not used in transaction tests")

            override suspend fun update(command: UpdatePocketCommand): Pocket =
                throw UnsupportedOperationException("Not used in transaction tests")

            override suspend fun updateContract(pocketId: Long, command: UpdateContractCommand): PocketWithContract =
                throw UnsupportedOperationException("Not used in transaction tests")

            override suspend fun archive(id: Long): Pocket =
                throw UnsupportedOperationException("Not used in transaction tests")

            override suspend fun unarchive(id: Long): Pocket =
                throw UnsupportedOperationException("Not used in transaction tests")

            override suspend fun archiveForAccount(accountId: Long) =
                throw UnsupportedOperationException("Not used in transaction tests")

            override suspend fun unarchiveForAccount(accountId: Long) =
                throw UnsupportedOperationException("Not used in transaction tests")

            override suspend fun delete(id: Long) =
                throw UnsupportedOperationException("Not used in transaction tests")

            override suspend fun list(accountId: Long?, archived: Boolean?): List<Pocket> =
                throw UnsupportedOperationException("Not used in transaction tests")

            override suspend fun getById(id: Long): Pocket? =
                pocketRepository.findById(id)?.let { model ->
                    Pocket(
                        id = requireNotNull(model.id),
                        accountId = model.accountId,
                        name = model.name,
                        description = model.description,
                        color = model.color,
                        isDefault = model.isDefault,
                        isContractPocket = model.isContractPocket,
                        isArchived = model.isArchived,
                        createdAt = model.createdAt,
                    )
                }
        }

    @Bean
    fun partnerService(partnerRepository: PartnerRepository): PartnerService =
        object : PartnerService {
            override suspend fun create(command: CreatePartnerCommand): Partner =
                partnerRepository.save(PartnerModel(null, command.name, command.description, false, 1L))
                    .let { Partner(requireNotNull(it.id), it.name, it.description, it.isArchived, it.createdAt) }

            override suspend fun update(command: UpdatePartnerCommand): Partner {
                val existing = partnerRepository.findById(command.id)
                    ?: throw NotFoundException("not_found", "Partner not found", mapOf("id" to command.id))
                return partnerRepository.save(PartnerModel(existing.id, command.name, command.description, existing.isArchived, existing.createdAt))
                    .let { Partner(requireNotNull(it.id), it.name, it.description, it.isArchived, it.createdAt) }
            }

            override suspend fun archive(id: Long): Partner =
                throw UnsupportedOperationException("Not used in transaction tests")

            override suspend fun unarchive(id: Long): Partner =
                throw UnsupportedOperationException("Not used in transaction tests")

            override suspend fun delete(id: Long) =
                throw UnsupportedOperationException("Not used in transaction tests")

            override suspend fun list(query: String?, archived: Boolean): List<Partner> =
                partnerRepository.findAllByQueryAndArchived(query?.trim()?.takeIf { it.isNotBlank() }, archived)
                    .toList()
                    .map { Partner(requireNotNull(it.id), it.name, it.description, it.isArchived, it.createdAt) }

            override suspend fun getById(id: Long): Partner? =
                partnerRepository.findById(id)
                    ?.let { Partner(requireNotNull(it.id), it.name, it.description, it.isArchived, it.createdAt) }
        }
}
