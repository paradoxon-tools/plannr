package de.chennemann.plannr.server.partners.service

import de.chennemann.plannr.server.common.error.NotFoundException
import de.chennemann.plannr.server.partners.api.dto.CreatePartnerCommand
import de.chennemann.plannr.server.partners.api.dto.UpdatePartnerCommand
import de.chennemann.plannr.server.support.ApiIntegrationTest
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import java.awt.image.BufferedImage
import java.io.ByteArrayOutputStream
import java.util.Base64
import javax.imageio.ImageIO
import kotlin.test.*

class PartnerLogoServiceTest : ApiIntegrationTest() {
    @Autowired lateinit var logos: PartnerLogoService
    @Autowired lateinit var partners: PartnerService

    @BeforeEach fun clean() { cleanDatabase("partners") }

    @Test
    fun `logo survives partner edits and can be replaced and removed`() = runBlocking {
        val partner = partners.create(CreatePartnerCommand("Logo test", null))
        fun image(rgb: Int): String = Base64.getEncoder().encodeToString(ByteArrayOutputStream().apply {
            val image = BufferedImage(2, 2, BufferedImage.TYPE_INT_ARGB)
            image.setRGB(0, 0, rgb)
            ImageIO.write(image, "png", this)
        }.toByteArray())
        val saved = logos.save(partner.id, image(0xffff0000.toInt()))
        val version = assertNotNull(saved.logoVersion)
        assertTrue(logos.image(partner.id, version).isNotEmpty())
        partners.update(UpdatePartnerCommand(partner.id, "Renamed", null))
        assertEquals(version, partners.getById(partner.id)?.logoVersion)
        assertEquals(version, partners.list(null, false).single().logoVersion)
        val replaced = logos.save(partner.id, image(0xff00ff00.toInt()))
        assertNotEquals(version, replaced.logoVersion)
        assertFailsWith<NotFoundException> { logos.image(partner.id, version) }
        assertNull(logos.remove(partner.id).logoVersion)
        assertFailsWith<NotFoundException> { logos.image(partner.id, requireNotNull(replaced.logoVersion)) }
        assertFailsWith<NotFoundException> { logos.save(-1, image(0)) }
    }
}
