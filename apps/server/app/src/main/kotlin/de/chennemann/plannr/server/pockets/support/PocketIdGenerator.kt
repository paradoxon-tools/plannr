package de.chennemann.plannr.server.pockets.support

import org.springframework.stereotype.Component
import java.util.UUID

fun interface PocketIdGenerator {
    operator fun invoke(): String
}

@Component
internal class UuidPocketIdGenerator : PocketIdGenerator {
    override fun invoke(): String = "poc_${UUID.randomUUID()}"
}
