package de.chennemann.plannr.server.banking

import org.springframework.http.CacheControl
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.time.LocalDate
import java.util.UUID

@RestController
@RequestMapping("/banking")
class BankingController(
    private val connections: BankConnectionService,
    private val sync: BankSyncService,
    private val reconciliation: ReconciliationService,
) {
    @GetMapping("/institutions")
    suspend fun institutions(@RequestParam country: String) = connections.institutions(country)

    @PostMapping("/connections")
    suspend fun connect(@RequestBody command: StartConnection) = connections.start(command)

    @GetMapping("/connections")
    suspend fun connections() = connections.list()

    @GetMapping("/connections/{id}")
    suspend fun connection(@PathVariable id: UUID) = connections.get(id)

    @PostMapping("/connections/{id}/refresh")
    suspend fun refresh(@PathVariable id: UUID) = connections.refresh(id)

    @PostMapping("/authorizations/complete")
    suspend fun complete(@RequestBody command: CompleteAuthorization) = connections.complete(command)

    @GetMapping("/callback")
    suspend fun callback(@RequestParam state: String, @RequestParam(required = false) code: String?, @RequestParam(required = false) error: String?): ResponseEntity<BankConnection> =
        ResponseEntity.ok().cacheControl(CacheControl.noStore()).header("Referrer-Policy", "no-referrer")
            .body(connections.complete(CompleteAuthorization(state, code, error)))

    @DeleteMapping("/connections/{id}")
    suspend fun disconnect(@PathVariable id: UUID) = connections.disconnect(id)

    @GetMapping("/accounts")
    suspend fun accounts(@RequestParam(required = false) connectionId: UUID?) = connections.accounts(connectionId)

    @PutMapping("/accounts/{id}/link")
    suspend fun link(@PathVariable id: Long, @RequestBody command: AccountLink) = connections.link(id, command.accountId)

    @DeleteMapping("/accounts/{id}/link")
    suspend fun unlink(@PathVariable id: Long) = connections.link(id, null)

    @PostMapping("/accounts/{id}/sync")
    suspend fun sync(@PathVariable id: Long, @RequestBody command: SyncRequest) = sync.sync(id, command)

    @GetMapping("/accounts/{id}/transactions")
    suspend fun transactions(@PathVariable id: Long, @RequestParam(required = false) status: String?,
        @RequestParam(required = false) dateFrom: LocalDate?, @RequestParam(required = false) dateTo: LocalDate?,
        @RequestParam(defaultValue = "100") limit: Int, @RequestParam(defaultValue = "0") offset: Int) =
        reconciliation.transactions(id, status, dateFrom, dateTo, limit, offset)

    @GetMapping("/transactions/{id}")
    suspend fun transaction(@PathVariable id: Long) = reconciliation.get(id)

    @GetMapping("/transactions/{id}/suggestions")
    suspend fun suggestions(@PathVariable id: Long, @RequestParam(defaultValue = "7") days: Int) = reconciliation.suggestions(id, days)

    @PutMapping("/transactions/{id}/reconciliation")
    suspend fun reconcile(@PathVariable id: Long, @RequestBody command: ReconcileRequest) = reconciliation.reconcile(id, command)

    @GetMapping("/transactions/{id}/reconciliation")
    suspend fun reconciliation(@PathVariable id: Long) = reconciliation.reconciliation(id)

    @DeleteMapping("/transactions/{id}/reconciliation")
    suspend fun undo(@PathVariable id: Long) = reconciliation.undo(id)

    @PutMapping("/transactions/{id}/ignored")
    suspend fun ignore(@PathVariable id: Long, @RequestBody command: IgnoreRequest) = reconciliation.ignore(id, command)

    @GetMapping("/planning-accounts/{id}/transactions")
    suspend fun planned(@PathVariable id: Long, @RequestParam dateFrom: LocalDate, @RequestParam dateTo: LocalDate,
        @RequestParam(defaultValue = "100") limit: Int, @RequestParam(defaultValue = "0") offset: Int) =
        reconciliation.planned(id, dateFrom, dateTo, limit, offset)

    @GetMapping("/planning-accounts/{id}/reconciliations")
    suspend fun reconciliations(@PathVariable id: Long, @RequestParam(defaultValue = "100") limit: Int,
        @RequestParam(defaultValue = "0") offset: Int) = reconciliation.reconciliations(id, limit, offset)
}
