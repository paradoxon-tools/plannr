package de.chennemann.plannr.server.transactions.events

import de.chennemann.plannr.server.common.events.ApplicationEvent
import de.chennemann.plannr.server.transactions.domain.TransactionRecord

data class TransactionCreated(
    val transaction: TransactionRecord,
) : ApplicationEvent

data class TransactionUpdated(
    val before: TransactionRecord,
    val after: TransactionRecord,
) : ApplicationEvent

data class TransactionArchived(
    val before: TransactionRecord,
    val after: TransactionRecord,
) : ApplicationEvent

data class TransactionUnarchived(
    val before: TransactionRecord,
    val after: TransactionRecord,
) : ApplicationEvent
