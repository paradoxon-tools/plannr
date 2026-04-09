package de.chennemann.plannr.server.transactions.support

import java.util.UUID
import org.springframework.stereotype.Component

fun interface TransactionIdGenerator {
    operator fun invoke(): String
}

@Component
internal class UuidTransactionIdGenerator : TransactionIdGenerator {
    override fun invoke(): String = "txn_${UUID.randomUUID().toString().replace("-", "")}".take(64)
}
