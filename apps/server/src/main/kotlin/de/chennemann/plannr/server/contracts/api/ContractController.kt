package de.chennemann.plannr.server.contracts.api

import de.chennemann.plannr.server.contracts.usecases.ArchiveContract
import de.chennemann.plannr.server.contracts.usecases.CreateContract
import de.chennemann.plannr.server.contracts.usecases.GetContract
import de.chennemann.plannr.server.contracts.usecases.ListContracts
import de.chennemann.plannr.server.contracts.usecases.UnarchiveContract
import de.chennemann.plannr.server.contracts.usecases.UpdateContract
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/contracts")
class ContractController(
    private val createContract: CreateContract,
    private val updateContract: UpdateContract,
    private val getContract: GetContract,
    private val listContracts: ListContracts,
    private val archiveContract: ArchiveContract,
    private val unarchiveContract: UnarchiveContract,
) {
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    suspend fun create(@RequestBody request: CreateContractRequest): ContractResponse =
        ContractResponse.from(createContract(request.toCommand()))

    @GetMapping
    suspend fun list(
        @RequestParam(required = false) accountId: String?,
        @RequestParam(defaultValue = "false") archived: Boolean,
    ): List<ContractResponse> = listContracts(accountId = accountId, archived = archived).map(ContractResponse::from)

    @GetMapping("/{id}")
    suspend fun getById(@PathVariable id: String): ContractResponse =
        ContractResponse.from(getContract(id))

    @PutMapping("/{id}")
    suspend fun update(@PathVariable id: String, @RequestBody request: UpdateContractRequest): ContractResponse =
        ContractResponse.from(updateContract(request.toCommand(id)))

    @PostMapping("/{id}/archive")
    suspend fun archive(@PathVariable id: String): ContractResponse =
        ContractResponse.from(archiveContract(id))

    @PostMapping("/{id}/unarchive")
    suspend fun unarchive(@PathVariable id: String): ContractResponse =
        ContractResponse.from(unarchiveContract(id))
}
