package de.chennemann.plannr.data

import kotlinx.datetime.LocalDate

data class Transaction(
    val id: Long,
    val title: String,
    val description: String?,
    val templateId: Long?,
    val contractId: Long?,
    val date: LocalDate,
    val amount: Amount,
    val sourcePocketId: Long?,
    val destinationPocketId: Long?,
    val partnerId: Long?,
    val sourceName: String? = null,
    val destinationName: String? = null,
    val sourceContractId: Long? = null,
    val destinationContractId: Long? = null,
    val signedAmount: Long = 0,
)
