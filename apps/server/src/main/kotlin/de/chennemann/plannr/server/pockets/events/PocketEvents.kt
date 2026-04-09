package de.chennemann.plannr.server.pockets.events

import de.chennemann.plannr.server.common.events.ApplicationEvent
import de.chennemann.plannr.server.pockets.domain.Pocket

data class PocketCreated(
    val pocket: Pocket,
) : ApplicationEvent

data class PocketUpdated(
    val before: Pocket,
    val after: Pocket,
) : ApplicationEvent
