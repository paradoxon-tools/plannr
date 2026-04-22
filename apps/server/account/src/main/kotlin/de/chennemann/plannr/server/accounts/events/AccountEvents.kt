package de.chennemann.plannr.server.accounts.events

import de.chennemann.plannr.server.accounts.domain.Account
import de.chennemann.plannr.server.common.events.ApplicationEvent

data class AccountCreated(
    val account: Account,
) : ApplicationEvent

data class AccountUpdated(
    val before: Account,
    val after: Account,
) : ApplicationEvent
