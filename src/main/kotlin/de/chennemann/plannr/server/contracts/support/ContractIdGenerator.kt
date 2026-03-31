package de.chennemann.plannr.server.contracts.support

import org.springframework.stereotype.Component
import java.util.UUID

fun interface ContractIdGenerator {
    operator fun invoke(): String
}

@Component
internal class UuidContractIdGenerator : ContractIdGenerator {
    override fun invoke(): String = "con_${UUID.randomUUID()}"
}
