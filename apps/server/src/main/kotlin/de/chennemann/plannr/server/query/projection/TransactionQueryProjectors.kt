package de.chennemann.plannr.server.query.projection

import de.chennemann.plannr.server.common.events.ApplicationEventHandler
import de.chennemann.plannr.server.partners.domain.PartnerRepository
import de.chennemann.plannr.server.pockets.domain.PocketRepository
import de.chennemann.plannr.server.transactions.domain.TransactionRecord
import de.chennemann.plannr.server.transactions.domain.TransactionRepository
import de.chennemann.plannr.server.accounts.domain.AccountRepository
import java.time.LocalDate
import de.chennemann.plannr.server.transactions.events.TransactionArchived
import de.chennemann.plannr.server.transactions.events.TransactionCreated
import de.chennemann.plannr.server.transactions.events.TransactionUnarchived
import de.chennemann.plannr.server.transactions.events.TransactionUpdated
import kotlinx.coroutines.reactor.awaitSingle
import kotlin.reflect.KClass
import org.springframework.r2dbc.core.DatabaseClient
import org.springframework.stereotype.Component

@Component
class TransactionCreatedQueryProjector(
    private val projectionService: TransactionQueryProjectionService,
) : ApplicationEventHandler<TransactionCreated> {
    override val eventType: KClass<TransactionCreated> = TransactionCreated::class

    override suspend fun handle(event: TransactionCreated) {
        projectionService.rebuildFor(transaction = event.transaction)
    }
}

@Component
class TransactionUpdatedQueryProjector(
    private val projectionService: TransactionQueryProjectionService,
) : ApplicationEventHandler<TransactionUpdated> {
    override val eventType: KClass<TransactionUpdated> = TransactionUpdated::class

    override suspend fun handle(event: TransactionUpdated) {
        projectionService.rebuildFor(before = event.before, after = event.after)
    }
}

@Component
class TransactionArchivedQueryProjector(
    private val projectionService: TransactionQueryProjectionService,
) : ApplicationEventHandler<TransactionArchived> {
    override val eventType: KClass<TransactionArchived> = TransactionArchived::class

    override suspend fun handle(event: TransactionArchived) {
        projectionService.rebuildFor(before = event.before, after = event.after)
    }
}

@Component
class TransactionUnarchivedQueryProjector(
    private val projectionService: TransactionQueryProjectionService,
) : ApplicationEventHandler<TransactionUnarchived> {
    override val eventType: KClass<TransactionUnarchived> = TransactionUnarchived::class

    override suspend fun handle(event: TransactionUnarchived) {
        projectionService.rebuildFor(before = event.before, after = event.after)
    }
}

@Component
class TransactionQueryProjectionService(
    private val transactionRepository: TransactionRepository,
    private val pocketRepository: PocketRepository,
    private val partnerRepository: PartnerRepository,
    private val accountRepository: AccountRepository,
    private val databaseClient: DatabaseClient,
) {
    suspend fun rebuildFor(transaction: TransactionRecord) {
        rebuildFor(after = transaction)
    }

    suspend fun rebuildFor(before: TransactionRecord? = null, after: TransactionRecord? = null) {
        setOfNotNull(before?.accountId, after?.accountId)
            .forEach { rebuildAccountFeed(it) }

        setOfNotNull(
            before?.sourcePocketId,
            before?.destinationPocketId,
            after?.sourcePocketId,
            after?.destinationPocketId,
        ).forEach { rebuildPocketFeed(it) }
    }

    suspend fun rebuildAccountFeed(accountId: String) {
        val today = LocalDate.now().toString()
        val transactions = transactionRepository.findVisibleByAccountId(accountId)
            .filter { it.transactionDate <= today }
        databaseClient.sql("DELETE FROM account_transaction_feed WHERE account_id = :accountId")
            .bind("accountId", accountId)
            .fetch()
            .rowsUpdated()
            .awaitSingle()

        var runningBalance = 0L
        transactions.forEachIndexed { index, transaction ->
            runningBalance += transaction.accountSignedAmount()
            val sourcePocket = transaction.sourcePocketId?.let { pocketRepository.findById(it) }
            val destinationPocket = transaction.destinationPocketId?.let { pocketRepository.findById(it) }
            val partner = transaction.partnerId?.let { partnerRepository.findById(it) }
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
                .bind("balanceAfter", runningBalance)
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

        updateAccountCurrentBalance(accountId, runningBalance)
    }

    suspend fun rebuildPocketFeed(pocketId: String) {
        val today = LocalDate.now().toString()
        val transactions = transactionRepository.findVisibleByPocketId(pocketId)
            .filter { it.transactionDate <= today }
        databaseClient.sql("DELETE FROM pocket_transaction_feed WHERE pocket_id = :pocketId")
            .bind("pocketId", pocketId)
            .fetch()
            .rowsUpdated()
            .awaitSingle()

        var runningBalance = 0L
        transactions.forEachIndexed { index, transaction ->
            runningBalance += transaction.pocketSignedAmount(pocketId)
            val partner = transaction.partnerId?.let { partnerRepository.findById(it) }
            val transferPocket = transaction.transferPocketIdFor(pocketId)?.let { pocketRepository.findById(it) }
            var spec = databaseClient.sql(
                """
                INSERT INTO pocket_transaction_feed (
                    pocket_id, account_id, transaction_id, history_position, transaction_date, type, status, description,
                    transaction_amount, signed_amount, balance_after, partner_id, partner_name,
                    transfer_pocket_id, transfer_pocket_name, transfer_pocket_color, is_archived
                ) VALUES (
                    :pocketId, :accountId, :transactionId, :historyPosition, :transactionDate, :type, :status, :description,
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
                .bind("balanceAfter", runningBalance)
            spec = bindNullable(spec, "partnerId", partner?.id, String::class.java)
            spec = bindNullable(spec, "partnerName", partner?.name, String::class.java)
            spec = bindNullable(spec, "transferPocketId", transferPocket?.id, String::class.java)
            spec = bindNullable(spec, "transferPocketName", transferPocket?.name, String::class.java)
            spec = bindNullable(spec, "transferPocketColor", transferPocket?.color, Int::class.javaObjectType)
            spec.fetch().rowsUpdated().awaitSingle()
        }

        updatePocketCurrentBalance(pocketId, runningBalance)
    }

    suspend fun rebuildAll() {
        accountRepository.findAll().forEach { rebuildAccountFeed(it.id) }
        pocketRepository.findAll().forEach { rebuildPocketFeed(it.id) }
    }

    private suspend fun updateAccountCurrentBalance(accountId: String, currentBalance: Long) {
        databaseClient.sql(
            """
            UPDATE account_query
            SET current_balance = :currentBalance
            WHERE account_id = :accountId
            """.trimIndent(),
        )
            .bind("accountId", accountId)
            .bind("currentBalance", currentBalance)
            .fetch()
            .rowsUpdated()
            .awaitSingle()
    }

    private suspend fun updatePocketCurrentBalance(pocketId: String, currentBalance: Long) {
        databaseClient.sql(
            """
            UPDATE pocket_query
            SET current_balance = :currentBalance
            WHERE pocket_id = :pocketId
            """.trimIndent(),
        )
            .bind("pocketId", pocketId)
            .bind("currentBalance", currentBalance)
            .fetch()
            .rowsUpdated()
            .awaitSingle()
    }

    private fun <T : Any> bindNullable(
        spec: DatabaseClient.GenericExecuteSpec,
        name: String,
        value: T?,
        type: Class<T>,
    ): DatabaseClient.GenericExecuteSpec = if (value == null) spec.bindNull(name, type) else spec.bind(name, value)
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
