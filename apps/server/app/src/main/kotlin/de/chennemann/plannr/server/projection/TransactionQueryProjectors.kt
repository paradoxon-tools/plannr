package de.chennemann.plannr.server.projection

import de.chennemann.plannr.server.accounts.domain.AccountRepository
import de.chennemann.plannr.server.common.events.ApplicationEventHandler
import de.chennemann.plannr.server.common.time.LocalDateProvider
import de.chennemann.plannr.server.contracts.domain.ContractRepository
import de.chennemann.plannr.server.partners.service.PartnerService
import de.chennemann.plannr.server.pockets.domain.PocketRepository
import de.chennemann.plannr.server.transactions.domain.TransactionRecord
import de.chennemann.plannr.server.transactions.domain.TransactionRepository
import de.chennemann.plannr.server.transactions.events.TransactionArchived
import de.chennemann.plannr.server.transactions.events.TransactionCreated
import de.chennemann.plannr.server.transactions.events.TransactionUnarchived
import de.chennemann.plannr.server.transactions.events.TransactionUpdated
import kotlinx.coroutines.reactor.awaitSingle
import kotlin.reflect.KClass
import org.springframework.r2dbc.core.DatabaseClient
import org.springframework.r2dbc.core.DatabaseClient.GenericExecuteSpec
import org.springframework.stereotype.Component

@Component
class TransactionCreatedQueryProjector(
    private val projectionService: TransactionQueryProjectionService,
) : ApplicationEventHandler<TransactionCreated> {
    override val eventType: KClass<TransactionCreated> = TransactionCreated::class
    override suspend fun handle(event: TransactionCreated) { projectionService.rebuildFor(transaction = event.transaction) }
}

@Component
class TransactionUpdatedQueryProjector(
    private val projectionService: TransactionQueryProjectionService,
) : ApplicationEventHandler<TransactionUpdated> {
    override val eventType: KClass<TransactionUpdated> = TransactionUpdated::class
    override suspend fun handle(event: TransactionUpdated) { projectionService.rebuildFor(before = event.before, after = event.after) }
}

@Component
class TransactionArchivedQueryProjector(
    private val projectionService: TransactionQueryProjectionService,
) : ApplicationEventHandler<TransactionArchived> {
    override val eventType: KClass<TransactionArchived> = TransactionArchived::class
    override suspend fun handle(event: TransactionArchived) { projectionService.rebuildFor(before = event.before, after = event.after) }
}

@Component
class TransactionUnarchivedQueryProjector(
    private val projectionService: TransactionQueryProjectionService,
) : ApplicationEventHandler<TransactionUnarchived> {
    override val eventType: KClass<TransactionUnarchived> = TransactionUnarchived::class
    override suspend fun handle(event: TransactionUnarchived) { projectionService.rebuildFor(before = event.before, after = event.after) }
}

interface ProjectionRebuilder {
    suspend fun rebuildAccountFeed(accountId: String)
    suspend fun rebuildPocketFeed(pocketId: String)
    suspend fun rebuildAll()
}

@Component
class TransactionQueryProjectionService(
    private val transactionRepository: TransactionRepository,
    private val pocketRepository: PocketRepository,
    private val partnerService: PartnerService,
    private val contractRepository: ContractRepository,
    private val accountRepository: AccountRepository,
    private val localDateProvider: LocalDateProvider,
    private val databaseClient: DatabaseClient,
) : ProjectionRebuilder {
    suspend fun rebuildFor(transaction: TransactionRecord) { rebuildFor(after = transaction) }

    suspend fun rebuildFor(before: TransactionRecord? = null, after: TransactionRecord? = null) {
        setOfNotNull(before?.accountId, after?.accountId).forEach { rebuildAccountFeed(it) }
        setOfNotNull(before?.pocketId, before?.sourcePocketId, before?.destinationPocketId, after?.pocketId, after?.sourcePocketId, after?.destinationPocketId)
            .forEach { rebuildPocketFeed(it) }
    }

    override suspend fun rebuildAccountFeed(accountId: String) {
        val today = localDateProvider().toString()
        val visible = transactionRepository.findVisibleByAccountId(accountId)
        val historical = visible.filter { it.transactionDate <= today }
        val future = visible.filter { it.transactionDate > today }

        databaseClient.sql("DELETE FROM account_transaction_feed WHERE account_id = :accountId").bind("accountId", accountId).fetch().rowsUpdated().awaitSingle()
        databaseClient.sql("DELETE FROM account_future_transaction_feed WHERE account_id = :accountId").bind("accountId", accountId).fetch().rowsUpdated().awaitSingle()

        var currentBalance = 0L
        historical.forEachIndexed { index, transaction ->
            currentBalance += transaction.accountSignedAmount()
            val sourcePocket = transaction.sourcePocketId?.let { pocketRepository.findById(it) }
            val destinationPocket = transaction.destinationPocketId?.let { pocketRepository.findById(it) }
            val partner = transaction.partnerId?.let { partnerService.getById(it) }
            var spec = databaseClient.sql(
                """
                INSERT INTO account_transaction_feed (
                    account_id, transaction_id, history_position, transaction_date, type, status, description,
                    transaction_amount, signed_amount, balance_after, partner_id, partner_name,
                    source_pocket_id, source_pocket_name, source_pocket_color,
                    destination_pocket_id, destination_pocket_name, destination_pocket_color, is_archived
                ) VALUES (
                    :accountId, :transactionId, :historyPosition, :transactionDate, :type, :status, :description,
                    :transactionAmount, :signedAmount, :balanceAfter, :partnerId, :partnerName,
                    :sourcePocketId, :sourcePocketName, :sourcePocketColor,
                    :destinationPocketId, :destinationPocketName, :destinationPocketColor, FALSE
                )
                """.trimIndent(),
            )
                .bind("accountId", accountId)
                .bind("transactionId", transaction.id)
                .bind("historyPosition", (index + 1).toLong())
                .bind("transactionDate", transaction.transactionDate)
                .bind("type", transaction.type)
                .bind("status", transaction.status)
                .bind("description", transaction.description)
                .bind("transactionAmount", transaction.amount)
                .bind("signedAmount", transaction.accountSignedAmount())
                .bind("balanceAfter", currentBalance)
            spec = bindNullable(spec, "partnerId", partner?.id, String::class.java)
            spec = bindNullable(spec, "partnerName", partner?.name, String::class.java)
            spec = bindNullable(spec, "sourcePocketId", sourcePocket?.id, String::class.java)
            spec = bindNullable(spec, "sourcePocketName", sourcePocket?.name, String::class.java)
            spec = bindNullable(spec, "sourcePocketColor", sourcePocket?.color, Int::class.javaObjectType)
            spec = bindNullable(spec, "destinationPocketId", destinationPocket?.id, String::class.java)
            spec = bindNullable(spec, "destinationPocketName", destinationPocket?.name, String::class.java)
            spec = bindNullable(spec, "destinationPocketColor", destinationPocket?.color, Int::class.javaObjectType)
            spec.fetch().rowsUpdated().awaitSingle()
        }

        var projectedBalance = currentBalance
        future.forEachIndexed { index, transaction ->
            projectedBalance += transaction.accountSignedAmount()
            val sourcePocket = transaction.sourcePocketId?.let { pocketRepository.findById(it) }
            val destinationPocket = transaction.destinationPocketId?.let { pocketRepository.findById(it) }
            val partner = transaction.partnerId?.let { partnerService.getById(it) }
            var spec = databaseClient.sql(
                """
                INSERT INTO account_future_transaction_feed (
                    account_id, transaction_id, future_position, transaction_date, type, status, description,
                    transaction_amount, signed_amount, projected_balance_after, partner_id, partner_name,
                    source_pocket_id, source_pocket_name, source_pocket_color,
                    destination_pocket_id, destination_pocket_name, destination_pocket_color
                ) VALUES (
                    :accountId, :transactionId, :futurePosition, :transactionDate, :type, :status, :description,
                    :transactionAmount, :signedAmount, :projectedBalanceAfter, :partnerId, :partnerName,
                    :sourcePocketId, :sourcePocketName, :sourcePocketColor,
                    :destinationPocketId, :destinationPocketName, :destinationPocketColor
                )
                """.trimIndent(),
            )
                .bind("accountId", accountId)
                .bind("transactionId", transaction.id)
                .bind("futurePosition", (index + 1).toLong())
                .bind("transactionDate", transaction.transactionDate)
                .bind("type", transaction.type)
                .bind("status", transaction.status)
                .bind("description", transaction.description)
                .bind("transactionAmount", transaction.amount)
                .bind("signedAmount", transaction.accountSignedAmount())
                .bind("projectedBalanceAfter", projectedBalance)
            spec = bindNullable(spec, "partnerId", partner?.id, String::class.java)
            spec = bindNullable(spec, "partnerName", partner?.name, String::class.java)
            spec = bindNullable(spec, "sourcePocketId", sourcePocket?.id, String::class.java)
            spec = bindNullable(spec, "sourcePocketName", sourcePocket?.name, String::class.java)
            spec = bindNullable(spec, "sourcePocketColor", sourcePocket?.color, Int::class.javaObjectType)
            spec = bindNullable(spec, "destinationPocketId", destinationPocket?.id, String::class.java)
            spec = bindNullable(spec, "destinationPocketName", destinationPocket?.name, String::class.java)
            spec = bindNullable(spec, "destinationPocketColor", destinationPocket?.color, Int::class.javaObjectType)
            spec.fetch().rowsUpdated().awaitSingle()
        }

        updateAccountCurrentBalance(accountId, currentBalance)
    }

    override suspend fun rebuildPocketFeed(pocketId: String) {
        val today = localDateProvider().toString()
        val visible = transactionRepository.findVisibleByPocketId(pocketId)
        val historical = visible.filter { it.transactionDate <= today }
        val future = visible.filter { it.transactionDate > today }
        val contractId = contractRepository.findByPocketId(pocketId)?.id

        databaseClient.sql("DELETE FROM pocket_transaction_feed WHERE pocket_id = :pocketId").bind("pocketId", pocketId).fetch().rowsUpdated().awaitSingle()
        databaseClient.sql("DELETE FROM pocket_future_transaction_feed WHERE pocket_id = :pocketId").bind("pocketId", pocketId).fetch().rowsUpdated().awaitSingle()

        var currentBalance = 0L
        historical.forEachIndexed { index, transaction ->
            currentBalance += transaction.pocketSignedAmount(pocketId)
            val partner = transaction.partnerId?.let { partnerService.getById(it) }
            val transferPocket = transaction.transferPocketIdFor(pocketId)?.let { pocketRepository.findById(it) }
            var spec = databaseClient.sql(
                """
                INSERT INTO pocket_transaction_feed (
                    pocket_id, account_id, contract_id, transaction_id, history_position, transaction_date, type, status, description,
                    transaction_amount, signed_amount, balance_after, partner_id, partner_name,
                    transfer_pocket_id, transfer_pocket_name, transfer_pocket_color, is_archived
                ) VALUES (
                    :pocketId, :accountId, :contractId, :transactionId, :historyPosition, :transactionDate, :type, :status, :description,
                    :transactionAmount, :signedAmount, :balanceAfter, :partnerId, :partnerName,
                    :transferPocketId, :transferPocketName, :transferPocketColor, FALSE
                )
                """.trimIndent(),
            )
                .bind("pocketId", pocketId)
                .bind("accountId", transaction.accountId)
                .bind("transactionId", transaction.id)
                .bind("historyPosition", (index + 1).toLong())
                .bind("transactionDate", transaction.transactionDate)
                .bind("type", transaction.type)
                .bind("status", transaction.status)
                .bind("description", transaction.description)
                .bind("transactionAmount", transaction.amount)
                .bind("signedAmount", transaction.pocketSignedAmount(pocketId))
                .bind("balanceAfter", currentBalance)
            spec = bindNullable(spec, "contractId", contractId, String::class.java)
            spec = bindNullable(spec, "partnerId", partner?.id, String::class.java)
            spec = bindNullable(spec, "partnerName", partner?.name, String::class.java)
            spec = bindNullable(spec, "transferPocketId", transferPocket?.id, String::class.java)
            spec = bindNullable(spec, "transferPocketName", transferPocket?.name, String::class.java)
            spec = bindNullable(spec, "transferPocketColor", transferPocket?.color, Int::class.javaObjectType)
            spec.fetch().rowsUpdated().awaitSingle()
        }

        var projectedBalance = currentBalance
        future.forEachIndexed { index, transaction ->
            projectedBalance += transaction.pocketSignedAmount(pocketId)
            val partner = transaction.partnerId?.let { partnerService.getById(it) }
            val transferPocket = transaction.transferPocketIdFor(pocketId)?.let { pocketRepository.findById(it) }
            var spec = databaseClient.sql(
                """
                INSERT INTO pocket_future_transaction_feed (
                    pocket_id, account_id, contract_id, transaction_id, future_position, transaction_date, type, status, description,
                    transaction_amount, signed_amount, projected_balance_after, partner_id, partner_name,
                    transfer_pocket_id, transfer_pocket_name, transfer_pocket_color
                ) VALUES (
                    :pocketId, :accountId, :contractId, :transactionId, :futurePosition, :transactionDate, :type, :status, :description,
                    :transactionAmount, :signedAmount, :projectedBalanceAfter, :partnerId, :partnerName,
                    :transferPocketId, :transferPocketName, :transferPocketColor
                )
                """.trimIndent(),
            )
                .bind("pocketId", pocketId)
                .bind("accountId", transaction.accountId)
                .bind("transactionId", transaction.id)
                .bind("futurePosition", (index + 1).toLong())
                .bind("transactionDate", transaction.transactionDate)
                .bind("type", transaction.type)
                .bind("status", transaction.status)
                .bind("description", transaction.description)
                .bind("transactionAmount", transaction.amount)
                .bind("signedAmount", transaction.pocketSignedAmount(pocketId))
                .bind("projectedBalanceAfter", projectedBalance)
            spec = bindNullable(spec, "contractId", contractId, String::class.java)
            spec = bindNullable(spec, "partnerId", partner?.id, String::class.java)
            spec = bindNullable(spec, "partnerName", partner?.name, String::class.java)
            spec = bindNullable(spec, "transferPocketId", transferPocket?.id, String::class.java)
            spec = bindNullable(spec, "transferPocketName", transferPocket?.name, String::class.java)
            spec = bindNullable(spec, "transferPocketColor", transferPocket?.color, Int::class.javaObjectType)
            spec.fetch().rowsUpdated().awaitSingle()
        }

        updatePocketCurrentBalance(pocketId, currentBalance)
    }

    override suspend fun rebuildAll() {
        accountRepository.findAll().forEach { rebuildAccountFeed(it.id) }
        pocketRepository.findAll().forEach { rebuildPocketFeed(it.id) }
    }

    private suspend fun updateAccountCurrentBalance(accountId: String, currentBalance: Long) {
        databaseClient.sql("UPDATE account_query SET current_balance = :currentBalance WHERE account_id = :accountId")
            .bind("accountId", accountId)
            .bind("currentBalance", currentBalance)
            .fetch().rowsUpdated().awaitSingle()
    }

    private suspend fun updatePocketCurrentBalance(pocketId: String, currentBalance: Long) {
        databaseClient.sql("UPDATE pocket_query SET current_balance = :currentBalance WHERE pocket_id = :pocketId")
            .bind("pocketId", pocketId)
            .bind("currentBalance", currentBalance)
            .fetch().rowsUpdated().awaitSingle()
    }

    private fun <T : Any> bindNullable(spec: GenericExecuteSpec, name: String, value: T?, type: Class<T>): GenericExecuteSpec =
        if (value == null) spec.bindNull(name, type) else spec.bind(name, value)
}

private fun TransactionRecord.accountSignedAmount(): Long = when (type) {
    "EXPENSE" -> -amount
    "INCOME" -> destinationAmount ?: amount
    "TRANSFER" -> 0L
    else -> 0L
}

private fun TransactionRecord.pocketSignedAmount(pocketId: String): Long = when {
    sourcePocketId == pocketId -> -amount
    destinationPocketId == pocketId -> destinationAmount ?: amount
    else -> 0L
}

private fun TransactionRecord.transferPocketIdFor(pocketId: String): String? = when {
    sourcePocketId == pocketId -> destinationPocketId
    destinationPocketId == pocketId -> sourcePocketId
    else -> null
}
