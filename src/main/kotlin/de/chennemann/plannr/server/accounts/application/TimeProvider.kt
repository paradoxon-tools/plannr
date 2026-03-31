package de.chennemann.plannr.server.accounts.application

import org.springframework.stereotype.Component

fun interface TimeProvider {
    operator fun invoke(): Long
}

@Component
class SystemTimeProvider : TimeProvider {
    override fun invoke(): Long = System.currentTimeMillis()
}
