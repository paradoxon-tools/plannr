package de.chennemann.plannr.server.recurringtransactions.support

import org.springframework.stereotype.Component
import java.util.UUID

fun interface RecurringTransactionIdGenerator {
    operator fun invoke(): String
}

@Component
internal class UuidRecurringTransactionIdGenerator : RecurringTransactionIdGenerator {
    override fun invoke(): String = "rtx_${UUID.randomUUID()}"
}
