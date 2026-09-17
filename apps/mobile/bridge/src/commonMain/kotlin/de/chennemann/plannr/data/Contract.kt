package de.chennemann.plannr.data

import kotlinx.serialization.Serializable

@Serializable
data class Contract(
    val contractId: ContractId,
    val partner: Partner,
    val name: String,
    val description: String?,
    val balance: Long,
    val color: Int = 0x00A896,
    val signingDate: String? = null,
    val expirationDate: String? = null,
    val lastCancellationDate: String? = null,
) {
    @Serializable
    data class ContractId(
        val contractId: Long,
        val accountId: Long,
        val pocketId: Long
    )
}
