package de.chennemann.plannr.server.accounts.api

import de.chennemann.plannr.server.accounts.usecases.CreateAccount
import de.chennemann.plannr.server.accounts.usecases.GetAccount
import de.chennemann.plannr.server.accounts.usecases.ListAccounts
import de.chennemann.plannr.server.accounts.usecases.UpdateAccount
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/accounts")
class AccountController(
    private val createAccount: CreateAccount,
    private val updateAccount: UpdateAccount,
    private val getAccount: GetAccount,
    private val listAccounts: ListAccounts,
) {
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    suspend fun create(@RequestBody request: CreateAccountRequest): AccountResponse =
        AccountResponse.from(createAccount(request.toCommand()))

    @PutMapping("/{id}")
    suspend fun update(@PathVariable id: String, @RequestBody request: UpdateAccountRequest): AccountResponse =
        AccountResponse.from(updateAccount(request.toCommand(id)))

    @GetMapping("/{id}")
    suspend fun getById(@PathVariable id: String): AccountResponse =
        AccountResponse.from(getAccount(id))

    @GetMapping
    suspend fun list(): List<AccountResponse> = listAccounts().map(AccountResponse::from)
}
