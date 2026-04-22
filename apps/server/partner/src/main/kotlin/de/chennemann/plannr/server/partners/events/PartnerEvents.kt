package de.chennemann.plannr.server.partners.events

import de.chennemann.plannr.server.common.events.ApplicationEvent
import de.chennemann.plannr.server.partners.domain.Partner

data class PartnerCreated(
    val partner: Partner,
) : ApplicationEvent

data class PartnerUpdated(
    val before: Partner,
    val after: Partner,
) : ApplicationEvent
