package de.chennemann.plannr.server.currencies.persistence

import de.chennemann.plannr.server.currencies.domain.CurrencyRepository
import de.chennemann.plannr.server.currencies.persistence.toModel
import de.chennemann.plannr.server.currencies.support.CurrencyFixtures
import de.chennemann.plannr.server.support.ApiIntegrationTest
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import kotlin.test.assertEquals
import kotlin.test.assertNull

class R2dbcCurrencyRepositoryTest : ApiIntegrationTest() {
    @Autowired
    lateinit var currencyRepository: CurrencyRepository

    @BeforeEach
    fun setUp() {
        cleanDatabase("currencies")
    }

    @Test
    fun `saves and finds currency by code`() = runBlocking {
        val currency = CurrencyFixtures.currency()

        currencyRepository.save(currency.toModel())

        assertEquals(currency, currencyRepository.findByCode("EUR"))
        assertNull(currencyRepository.findByCode("USD"))
    }

    @Test
    fun `updates and finds currency by code`() = runBlocking {
        currencyRepository.save(CurrencyFixtures.currency().toModel())
        val updated = CurrencyFixtures.currency(name = "Euro Updated", symbol = "EUR", decimalPlaces = 3, symbolPosition = "after")

        currencyRepository.update(updated.toModel())

        assertEquals(updated, currencyRepository.findByCode("EUR"))
    }

}
