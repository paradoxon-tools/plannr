package de.chennemann.plannr.data

import kotlinx.serialization.Serializable

@Serializable
enum class RecurrenceType {
    NONE,
    DAILY,
    WEEKLY,
    MONTHLY,
    YEARLY;
}