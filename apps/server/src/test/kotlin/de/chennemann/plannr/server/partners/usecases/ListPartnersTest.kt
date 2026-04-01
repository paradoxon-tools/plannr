package de.chennemann.plannr.server.partners.usecases

import de.chennemann.plannr.server.partners.support.InMemoryPartnerRepository
import de.chennemann.plannr.server.partners.support.PartnerFixtures
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class ListPartnersTest {
    @Test
    fun `returns empty list when there are no partners`() = runTest {
        val listPartners = ListPartnersUseCase(InMemoryPartnerRepository())

        val result = listPartners()

        assertEquals(emptyList(), result)
    }

    @Test
    fun `excludes archived partners by default and supports name search`() = runTest {
        val repository = InMemoryPartnerRepository()
        val first = PartnerFixtures.partner(id = "par_1", name = "ACME Corp", createdAt = 1)
        val second = PartnerFixtures.partner(id = "par_2", name = "Beta GmbH", createdAt = 2, isArchived = true)
        val third = PartnerFixtures.partner(id = "par_3", name = "Acme Services", createdAt = 3)
        repository.save(first)
        repository.save(second)
        repository.save(third)
        val listPartners = ListPartnersUseCase(repository)

        val defaultResult = listPartners()
        val queryResult = listPartners(query = "acme")
        val archivedResult = listPartners(archived = true)

        assertEquals(listOf(first, third), defaultResult)
        assertEquals(listOf(first, third), queryResult)
        assertEquals(listOf(second), archivedResult)
    }
}
