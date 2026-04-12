package de.chennemann.plannr.server.common.time

import org.springframework.stereotype.Component

fun interface TimeProvider {
    operator fun invoke(): Long
}

@Component
internal class SystemTimeProvider : TimeProvider {
    override fun invoke(): Long = System.currentTimeMillis()
}
