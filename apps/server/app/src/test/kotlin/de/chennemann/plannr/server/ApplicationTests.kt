package de.chennemann.plannr.server

import de.chennemann.plannr.server.support.PostgresContainerSupport
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles

@Tag("integration")
@SpringBootTest
@ActiveProfiles("test")
class ApplicationTests : PostgresContainerSupport() {
    @Test
    fun contextLoads() {
    }
}
