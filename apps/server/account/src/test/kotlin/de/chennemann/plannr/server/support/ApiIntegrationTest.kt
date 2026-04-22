package de.chennemann.plannr.server.support

import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.BeforeEach
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ContextConfiguration
import org.springframework.r2dbc.core.DatabaseClient

@SpringBootTest(classes = [AccountTestApplication::class])
@ContextConfiguration(initializers = [PostgresContainerSupport.Companion.Initializer::class])
abstract class ApiIntegrationTest : PostgresContainerSupport() {
    @Autowired
    lateinit var databaseClient: DatabaseClient

    private lateinit var databaseCleaner: DatabaseCleaner

    @BeforeEach
    fun baseSetUp() {
        databaseCleaner = DatabaseCleaner(databaseClient)
    }

    fun cleanDatabase(vararg tables: String) = runBlocking {
        databaseCleaner.clean(*tables)
    }
}
