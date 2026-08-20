package de.chennemann.plannr.database

import app.cash.sqldelight.EnumColumnAdapter
import app.cash.sqldelight.adapter.primitive.IntColumnAdapter
import app.cash.sqldelight.db.SqlDriver
import de.chennemann.plannr.database.adapter.DayOfWeekListAdapter
import de.chennemann.plannr.database.adapter.InstantAdapter
import de.chennemann.plannr.database.adapter.IntListAdapter
import de.chennemann.plannr.database.adapter.MonthListAdapter
import migrations.TransactionTemplateOccurrences
import migrations.TransactionTemplates
import migrations.Transactions

/**
 * Factory for creating the SQLDelight database with platform-specific implementations.
 */
expect class DriverFactory {
    fun createDriver(): SqlDriver
}

fun createDatabase(driver: SqlDriver): PlannrDB {
    val plannrDB = PlannrDB(
        driver,
        transactionsAdapter = Transactions.Adapter(
            dateAdapter = InstantAdapter
        ),
        transactionTemplatesAdapter =  TransactionTemplates.Adapter(
            recurrenceTypeAdapter = EnumColumnAdapter(),
            referenceDateAdapter = InstantAdapter,
            finalOccurrenceDateAdapter = InstantAdapter,
            maxRecurrenceCountAdapter = IntColumnAdapter,
            skipCountAdapter = IntColumnAdapter,
            daysOfWeekAdapter = DayOfWeekListAdapter,
            weeksOfMonthAdapter = IntListAdapter,
            daysOfMonthAdapter = IntListAdapter,
            monthsOfYearAdapter = MonthListAdapter
        ),
        transactionTemplateOccurrencesAdapter = TransactionTemplateOccurrences.Adapter(
            lastMaterializedOccurrenceAdapter = InstantAdapter,
            nextOccurrenceAdapter = InstantAdapter
        )
    )
    return plannrDB
}
