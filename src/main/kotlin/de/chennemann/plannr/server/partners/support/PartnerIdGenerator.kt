package de.chennemann.plannr.server.partners.support

import org.springframework.stereotype.Component
import java.util.UUID

fun interface PartnerIdGenerator {
    operator fun invoke(): String
}

@Component
internal class UuidPartnerIdGenerator : PartnerIdGenerator {
    override fun invoke(): String = "par_${UUID.randomUUID()}"
}
