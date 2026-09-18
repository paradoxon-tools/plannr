package de.chennemann.plannr.server.partners.service

import de.chennemann.plannr.server.common.error.NotFoundException
import de.chennemann.plannr.server.partners.api.dto.Partner
import de.chennemann.plannr.server.partners.domain.PartnerRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.reactor.awaitSingle
import kotlinx.coroutines.reactor.awaitSingleOrNull
import kotlinx.coroutines.withContext
import org.springframework.r2dbc.core.DatabaseClient
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.security.MessageDigest
import java.util.Base64

@Service
class PartnerLogoService(
    private val database: DatabaseClient,
    private val partners: PartnerRepository,
    private val partnerService: PartnerService,
) {
    @Transactional
    suspend fun save(id: Long, base64: String): Partner {
        if (!partners.existsById(id)) throw NotFoundException("not_found", "Partner not found")
        if (base64.length > 1_400_000) invalidLogo("Choose an image smaller than 1 MB")
        val bytes = withContext(Dispatchers.IO) {
            val decoded = try { Base64.getDecoder().decode(base64) }
                catch (_: IllegalArgumentException) { invalidLogo("Invalid image") }
            normalizeLogo(decoded)
        }
        val version = MessageDigest.getInstance("SHA-256").digest(bytes).joinToString("") { "%02x".format(it) }
        database.sql("INSERT INTO partner_logos(partner_id, image) VALUES (:id, :image) ON CONFLICT (partner_id) DO UPDATE SET image = EXCLUDED.image")
            .bind("id", id).bind("image", bytes).fetch().rowsUpdated().awaitSingle()
        database.sql("UPDATE partners SET logo_version = :version WHERE id = :id")
            .bind("id", id).bind("version", version).fetch().rowsUpdated().awaitSingle()
        return requireNotNull(partnerService.getById(id))
    }

    suspend fun image(id: Long, version: String): ByteArray =
        database.sql("SELECT l.image FROM partner_logos l JOIN partners p ON p.id = l.partner_id WHERE p.id = :id AND p.logo_version = :version")
            .bind("id", id).bind("version", version)
            .map { row, _ -> requireNotNull(row.get("image", ByteArray::class.java)) }.one().awaitSingleOrNull()
            ?: throw NotFoundException("not_found", "Partner logo not found")

    @Transactional
    suspend fun remove(id: Long): Partner {
        if (!partners.existsById(id)) throw NotFoundException("not_found", "Partner not found")
        database.sql("UPDATE partners SET logo_version = NULL WHERE id = :id").bind("id", id).fetch().rowsUpdated().awaitSingle()
        database.sql("DELETE FROM partner_logos WHERE partner_id = :id").bind("id", id).fetch().rowsUpdated().awaitSingle()
        return requireNotNull(partnerService.getById(id))
    }
}
