package de.chennemann.plannr.database.adapter

import app.cash.sqldelight.ColumnAdapter
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.isoDayNumber

internal object DayOfWeekListAdapter: ColumnAdapter<List<DayOfWeek>, String> {
    override fun decode(databaseValue: String) =
        if (databaseValue.isEmpty()) {
            emptyList()
        } else {
            databaseValue.split(",").map { DayOfWeek(it.toInt()) }
        }
    override fun encode(value: List<DayOfWeek>) = value.map { it.isoDayNumber }.joinToString(separator = ",")
}