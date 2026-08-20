package de.chennemann.plannr.database.adapter

import app.cash.sqldelight.ColumnAdapter
import kotlin.time.Instant;
import kotlinx.datetime.format.DateTimeComponents
import kotlinx.datetime.parse

internal object InstantAdapter: ColumnAdapter<Instant, String> {
    override fun decode(databaseValue: String): Instant {
        return Instant.parse(databaseValue, DateTimeComponents.Formats.ISO_DATE_TIME_OFFSET)
    }
    override fun encode(value: Instant): String {
        return value.toString()
    }
}