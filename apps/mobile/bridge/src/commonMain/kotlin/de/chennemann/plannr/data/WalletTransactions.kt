package de.chennemann.plannr.data

/** Account feeds contain both sides of a transfer; Wallet displays one row and their combined net change. */
fun List<Transaction>.mergeAccountHistory(): List<Transaction> =
    groupBy { it.id to it.amount.currency }.values.map { copies ->
        copies.first().copy(signedAmount = copies.sumOf { it.signedAmount })
    }.sortedWith(compareByDescending<Transaction> { it.date }.thenByDescending { it.id })

fun Transaction.changeForContract(contractId: Long): Long = when {
    sourceContractId == contractId && destinationContractId == contractId -> 0L
    destinationContractId == contractId -> amount.amount
    sourceContractId == contractId -> -amount.amount
    else -> signedAmount
}
