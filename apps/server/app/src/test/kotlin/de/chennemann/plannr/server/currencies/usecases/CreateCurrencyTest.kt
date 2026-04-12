package de.chennemann.plannr.server.currencies.usecases

import de.chennemann.plannr.server.common.error.ConflictException
import de.chennemann.plannr.server.currencies.support.CurrencyFixtures
import de.chennemann.plannr.server.currencies.support.InMemoryCurrencyRepository
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class CreateCurrencyTest {
    @Test
    fun `creates a new currency`() = runTest {
        val repository = InMemoryCurrencyRepository()
        val createCurrency = CreateCurrencyUseCase(repository)

        val created = createCurrency(CurrencyFixtures.createCurrencyCommand())

        assertEquals(CurrencyFixtures.DEFAULT_CODE, created.code)
        assertEquals(created, repository.findByCode(CurrencyFixtures.DEFAULT_CODE))
    }

    @Test
    fun `rejects duplicate currency code`() = runTest {
        val repository = InMemoryCurrencyRepository()
        val createCurrency = CreateCurrencyUseCase(repository)
        repository.save(CurrencyFixtures.currency())

        assertFailsWith<ConflictException> {
            createCurrency(CurrencyFixtures.createCurrencyCommand())
        }
    }
}
