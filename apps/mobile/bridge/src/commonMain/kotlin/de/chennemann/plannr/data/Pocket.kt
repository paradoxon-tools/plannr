package de.chennemann.plannr.data

import kotlinx.serialization.Serializable

@Serializable
data class Pocket(
    val id: PocketId,
    val pocketName: String,
    val balance: Long,
    val color: Int = 0,
    val contractId: Long? = null,
    val isDefault: Boolean = false,
) {
    @Serializable
    data class PocketId(
        val accountId: Long,
        val pocketId: Long
    )
}
