package de.chennemann.plannr.server.transactions.materialization.service

import de.chennemann.plannr.server.transactions.templates.domain.EffectiveTransactionTemplate

sealed interface MaterializationOperation {
    val transactionTemplate: EffectiveTransactionTemplate

    data class NewTransactionTemplate(override val transactionTemplate: EffectiveTransactionTemplate) : MaterializationOperation {
        constructor(template: de.chennemann.plannr.server.transactions.templates.domain.TransactionTemplate) : this(EffectiveTransactionTemplate(template, template.currentVersion))
    }
    data class EndDateChange(override val transactionTemplate: EffectiveTransactionTemplate) : MaterializationOperation {
        constructor(template: de.chennemann.plannr.server.transactions.templates.domain.TransactionTemplate) : this(EffectiveTransactionTemplate(template, template.currentVersion))
    }
    data class FullRefresh(override val transactionTemplate: EffectiveTransactionTemplate) : MaterializationOperation {
        constructor(template: de.chennemann.plannr.server.transactions.templates.domain.TransactionTemplate) : this(EffectiveTransactionTemplate(template, template.currentVersion))
    }
}
