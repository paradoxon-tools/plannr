package de.chennemann.plannr.server.support

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.r2dbc.core.DatabaseClient
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.reactive.server.WebTestClient

@SpringBootTest
@AutoConfigureWebTestClient
@ActiveProfiles("test")
abstract class ApiIntegrationTest {
    @Autowired
    protected lateinit var webTestClient: WebTestClient

    @Autowired
    private lateinit var databaseClient: DatabaseClient

    protected fun cleanDatabase(vararg tables: String) {
        DatabaseCleaner(databaseClient).deleteAllFrom(*tables)
    }
}
