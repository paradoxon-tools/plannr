package de.chennemann.plannr.database.adapter

import app.cash.sqldelight.ColumnAdapter
import kotlinx.datetime.Month
import kotlinx.datetime.number

internal object MonthListAdapter : ColumnAdapter<List<Month>, String> {
    override fun decode(databaseValue: String) =
        if (databaseValue.isEmpty()) {
            emptyList()
        } else {
            databaseValue.split(",").map { Month(it.toInt()) }
        }
    override fun encode(value: List<Month>) = value.map { it.number }.joinToString(separator = ",")
}