package de.chennemann.plannr.database

import app.cash.sqldelight.TransactionWithReturn

fun PlannrDB.runGettingLastId(
    query: context(PlannrDB) TransactionWithReturn<Long>.() -> Unit
): Long {
    val database = this@runGettingLastId
    return database.transactionWithResult {
        query(database, this@transactionWithResult)
        utilQueries.lastInsertedRowId().executeAsOne()
    }
}


