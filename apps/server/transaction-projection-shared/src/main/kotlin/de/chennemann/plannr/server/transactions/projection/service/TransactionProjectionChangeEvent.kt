package de.chennemann.plannr.server.transactions.projection.service

sealed interface TransactionProjectionChangeEvent {
    val aggregateId: Long?

    data object FullRebuild : TransactionProjectionChangeEvent {
        override val aggregateId: Long? = null
    }

    data class AccountChanged(
        override val aggregateId: Long,
    ) : TransactionProjectionChangeEvent

    data class PocketChanged(
        override val aggregateId: Long,
    ) : TransactionProjectionChangeEvent

    data class ContractChanged(
        override val aggregateId: Long,
    ) : TransactionProjectionChangeEvent

    data class PartnerChanged(
        override val aggregateId: Long,
    ) : TransactionProjectionChangeEvent

    data class FinancialProfileChanged(
        override val aggregateId: Long,
    ) : TransactionProjectionChangeEvent

    data class TransactionTemplateChanged(
        override val aggregateId: Long,
    ) : TransactionProjectionChangeEvent
}

val TransactionProjectionChangeEvent.eventType: String
    get() = when (this) {
        TransactionProjectionChangeEvent.FullRebuild -> "FULL_REBUILD"
        is TransactionProjectionChangeEvent.AccountChanged -> "ACCOUNT_CHANGED"
        is TransactionProjectionChangeEvent.PocketChanged -> "POCKET_CHANGED"
        is TransactionProjectionChangeEvent.ContractChanged -> "CONTRACT_CHANGED"
        is TransactionProjectionChangeEvent.PartnerChanged -> "PARTNER_CHANGED"
        is TransactionProjectionChangeEvent.FinancialProfileChanged -> "FINANCIAL_PROFILE_CHANGED"
        is TransactionProjectionChangeEvent.TransactionTemplateChanged -> "TRANSACTION_TEMPLATE_CHANGED"
    }
