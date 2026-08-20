package de.chennemann.plannr.data

import kotlinx.serialization.Serializable

@Serializable
data class Amount(
    val amount: Long,
    val currency: String,
)