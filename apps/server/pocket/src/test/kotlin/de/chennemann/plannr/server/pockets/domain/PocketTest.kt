package de.chennemann.plannr.server.pockets.domain

import de.chennemann.plannr.server.pockets.support.PocketFixtures
import kotlin.test.Test
import kotlin.test.assertEquals

class PocketTest {
    @Test
    fun `keeps id account id and name unchanged`() {
        val pocket = PocketFixtures.pocket(
            id = 1L,
            accountId = 1L,
            name = " Bills ",
        )

        assertEquals(1L, pocket.id)
        assertEquals(1L, pocket.accountId)
        assertEquals(" Bills ", pocket.name)
    }

    @Test
    fun `keeps description unchanged`() {
        val trimmed = PocketFixtures.pocket(description = " Monthly fixed costs ")
        val blank = PocketFixtures.pocket(description = "   ")

        assertEquals(" Monthly fixed costs ", trimmed.description)
        assertEquals("   ", blank.description)
    }
}
