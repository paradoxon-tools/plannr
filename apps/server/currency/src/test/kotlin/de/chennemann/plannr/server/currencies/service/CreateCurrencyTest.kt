package de.chennemann.plannr.server.currencies.service

import de.chennemann.plannr.server.common.error.ConflictException
import de.chennemann.plannr.server.currencies.support.CurrencyFixtures
import de.chennemann.plannr.server.currencies.support.InMemoryCurrencyTemplateCatalog
import de.chennemann.plannr.server.currencies.support.InMemoryCurrencyRepository
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class CreateCurrencyTest {
    @Test
    fun `creates a new currency`() = runTest {
        val repository = InMemoryCurrencyRepository()
        val currencyService = CurrencyServiceImpl(repository, InMemoryCurrencyTemplateCatalog())

        val created = currencyService.create(CurrencyFixtures.createCurrencyCommand())

        assertEquals(CurrencyFixtures.DEFAULT_CODE, created.code)
        assertEquals(created, repository.findByCode(CurrencyFixtures.DEFAULT_CODE))
    }

    @Test
    fun `rejects duplicate currency code`() = runTest {
        val repository = InMemoryCurrencyRepository()
        val currencyService = CurrencyServiceImpl(repository, InMemoryCurrencyTemplateCatalog())
        repository.save(CurrencyFixtures.currency())

        assertFailsWith<ConflictException> {
            currencyService.create(CurrencyFixtures.createCurrencyCommand())
        }
    }
}
