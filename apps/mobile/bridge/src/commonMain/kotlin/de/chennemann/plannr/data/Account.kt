package de.chennemann.plannr.data

import kotlinx.serialization.Serializable

@Serializable
data class Account(
    val accountId: Long,
    val accountName: String,
    val pockets: List<Pocket>,
    val totalBalance: Long,
    val freeBalance: Long
)
