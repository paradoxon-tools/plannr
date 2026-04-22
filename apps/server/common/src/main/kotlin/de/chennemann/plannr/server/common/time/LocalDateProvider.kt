package de.chennemann.plannr.server.common.time

import java.time.LocalDate
import org.springframework.stereotype.Component

fun interface LocalDateProvider {
    operator fun invoke(): LocalDate
}

@Component
internal class SystemLocalDateProvider : LocalDateProvider {
    override fun invoke(): LocalDate = LocalDate.now()
}
