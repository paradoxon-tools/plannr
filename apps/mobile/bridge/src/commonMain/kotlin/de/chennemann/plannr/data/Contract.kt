package de.chennemann.plannr.data

import kotlinx.serialization.Serializable

@Serializable
data class Contract(
    val contractId: ContractId,
    val partner: Partner,
    val name: String,
    val description: String?,
    val balance: Long
) {
    @Serializable
    data class ContractId(
        val contractId: Long,
        val accountId: Long,
        val pocketId: Long
    )
}