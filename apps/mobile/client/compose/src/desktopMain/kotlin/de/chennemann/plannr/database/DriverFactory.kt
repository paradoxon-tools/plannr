package de.chennemann.plannr.database

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import java.io.File

actual class DriverFactory {
    actual fun createDriver(): SqlDriver {
        val databasePath = File(System.getProperty("user.home"), "plannr.db")
        val driver = JdbcSqliteDriver(url = "jdbc:sqlite:${databasePath.absolutePath}")
        
        // Create the database if it doesn't exist
        if (!databasePath.exists()) {
            PlannrDB.Schema.create(driver)
        }
        
        return driver
    }
}