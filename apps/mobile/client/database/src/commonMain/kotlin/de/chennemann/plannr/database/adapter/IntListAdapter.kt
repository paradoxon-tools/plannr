package de.chennemann.plannr.database.adapter

import app.cash.sqldelight.ColumnAdapter

internal object IntListAdapter: ColumnAdapter<List<Int>, String> {
    override fun decode(databaseValue: String) =
        if (databaseValue.isEmpty()) {
            emptyList()
        } else {
            databaseValue.split(",").map { it.toInt() }
        }
    override fun encode(value: List<Int>) = value.joinToString(separator = ",")
}