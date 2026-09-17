package de.chennemann.plannr.data

import kotlinx.datetime.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals

class WalletTransactionsTest {
    private fun transaction(id: Long = 1, net: Long = 0, currency: String = "EUR") = Transaction(
        id, "Transfer", null, 1, null, LocalDate(2026, 9, 18), Amount(500, currency),
        1, 2, null, signedAmount = net,
    )

    @Test
    fun transferAppearsOnceAndDoesNotChangeWalletTotal() {
        val result = listOf(transaction(net = -500), transaction(net = 500), transaction(id = 2, net = -100)).mergeAccountHistory()
        assertEquals(2, result.size)
        assertEquals(0, result.single { it.id == 1L }.signedAmount)
        assertEquals(-100, result.sumOf { it.signedAmount })
    }

    @Test
    fun currenciesAreNotCombined() {
        val result = listOf(transaction(net = -500), transaction(net = 500, currency = "USD")).mergeAccountHistory()
        assertEquals(2, result.size)
    }

    @Test
    fun contractChangeUsesDedicatedPockets() {
        assertEquals(500, transaction().copy(destinationContractId = 7).changeForContract(7))
        assertEquals(-500, transaction().copy(sourceContractId = 7).changeForContract(7))
        assertEquals(0, transaction().copy(sourceContractId = 7, destinationContractId = 7).changeForContract(7))
        assertEquals(-500, transaction(net = -500).changeForContract(7))
    }
}
