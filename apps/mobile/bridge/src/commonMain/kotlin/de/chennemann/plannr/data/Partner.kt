package de.chennemann.plannr.data

import kotlinx.serialization.Serializable

@Serializable
data class Partner(
    val partnerId: Long,
    val name: String
)
