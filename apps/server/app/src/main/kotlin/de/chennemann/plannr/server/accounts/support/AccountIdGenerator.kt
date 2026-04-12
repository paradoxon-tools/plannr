package de.chennemann.plannr.server.accounts.support

import org.springframework.stereotype.Component
import java.util.UUID

fun interface AccountIdGenerator {
    operator fun invoke(): String
}

@Component
internal class UuidAccountIdGenerator : AccountIdGenerator {
    override fun invoke(): String = "acc_${UUID.randomUUID()}"
}
