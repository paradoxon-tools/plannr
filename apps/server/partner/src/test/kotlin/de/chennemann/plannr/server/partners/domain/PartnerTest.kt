package de.chennemann.plannr.server.partners.domain

import de.chennemann.plannr.server.common.error.ValidationException
import de.chennemann.plannr.server.partners.support.PartnerFixtures
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull

class PartnerTest {
    @Test
    fun `trims id name and notes`() {
        val partner = PartnerFixtures.partner(
            id = " par_123 ",
            name = " ACME Corp ",
            notes = " Preferred partner ",
        )

        assertEquals("par_123", partner.id)
        assertEquals("ACME Corp", partner.name)
        assertEquals("Preferred partner", partner.notes)
    }

    @Test
    fun `turns blank notes into null`() {
        val partner = PartnerFixtures.partner(notes = "   ")

        assertNull(partner.notes)
    }

    @Test
    fun `rejects blank id`() {
        assertFailsWith<ValidationException> {
            PartnerFixtures.partner(id = "   ")
        }
    }

    @Test
    fun `rejects blank name`() {
        assertFailsWith<ValidationException> {
            PartnerFixtures.partner(name = "   ")
        }
    }
}
