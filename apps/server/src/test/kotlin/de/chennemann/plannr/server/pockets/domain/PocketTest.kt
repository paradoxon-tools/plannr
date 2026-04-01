package de.chennemann.plannr.server.pockets.domain

import de.chennemann.plannr.server.common.error.ValidationException
import de.chennemann.plannr.server.pockets.support.PocketFixtures
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull

class PocketTest {
    @Test
    fun `trims id account id and name`() {
        val pocket = PocketFixtures.pocket(
            id = " poc_123 ",
            accountId = " acc_123 ",
            name = " Bills ",
        )

        assertEquals("poc_123", pocket.id)
        assertEquals("acc_123", pocket.accountId)
        assertEquals("Bills", pocket.name)
    }

    @Test
    fun `trims description and turns blank description into null`() {
        val trimmed = PocketFixtures.pocket(description = " Monthly fixed costs ")
        val blank = PocketFixtures.pocket(description = "   ")

        assertEquals("Monthly fixed costs", trimmed.description)
        assertNull(blank.description)
    }

    @Test
    fun `rejects blank id`() {
        assertFailsWith<ValidationException> {
            PocketFixtures.pocket(id = "   ")
        }
    }

    @Test
    fun `rejects blank account id`() {
        assertFailsWith<ValidationException> {
            PocketFixtures.pocket(accountId = "   ")
        }
    }

    @Test
    fun `rejects blank name`() {
        assertFailsWith<ValidationException> {
            PocketFixtures.pocket(name = "   ")
        }
    }

    @Test
    fun `rejects negative color`() {
        assertFailsWith<ValidationException> {
            PocketFixtures.pocket(color = -1)
        }
    }
}
