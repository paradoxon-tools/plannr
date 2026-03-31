package de.chennemann.plannr.server.accounts.application

import org.springframework.stereotype.Component
import java.util.UUID

fun interface AccountIdGenerator {
    operator fun invoke(): String
}

@Component
class UuidAccountIdGenerator : AccountIdGenerator {
    override fun invoke(): String = "acc_${UUID.randomUUID()}"
}
