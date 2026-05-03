package de.chennemann.plannr.server.transactions.recurring.service

import org.springframework.stereotype.Component

@Component
class RecurringTransactionMaterializer(
    private val recurringTransactionService: RecurringTransactionService,
) {
    suspend fun materializeAll() {
        recurringTransactionService.materializeAll()
    }
}
