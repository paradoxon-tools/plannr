package de.chennemann.plannr.server.transactions.materialization.service

import de.chennemann.plannr.server.transactions.templates.domain.TransactionTemplate

sealed interface MaterializationOperation {
    val transactionTemplate: TransactionTemplate

    data class NewTransactionTemplate(
        override val transactionTemplate: TransactionTemplate,
    ) : MaterializationOperation

    data class EndDateChange(
        override val transactionTemplate: TransactionTemplate,
    ) : MaterializationOperation

    data class FullRefresh(
        override val transactionTemplate: TransactionTemplate,
    ) : MaterializationOperation
}
