package de.chennemann.plannr.server.common.events

import kotlin.reflect.KClass
import kotlin.reflect.full.cast
import org.springframework.stereotype.Component

interface ApplicationEvent

interface ApplicationEventBus {
    suspend fun publish(event: ApplicationEvent)
}

interface ApplicationEventHandler<E : ApplicationEvent> {
    val eventType: KClass<E>

    suspend fun handle(event: E)

    suspend fun handleUntyped(event: ApplicationEvent) {
        if (eventType.isInstance(event)) {
            handle(eventType.cast(event))
        }
    }
}

object NoOpApplicationEventBus : ApplicationEventBus {
    override suspend fun publish(event: ApplicationEvent) = Unit
}

@Component
class InProcessSynchronousApplicationEventBus(
    private val handlers: List<ApplicationEventHandler<out ApplicationEvent>> = emptyList(),
) : ApplicationEventBus {
    override suspend fun publish(event: ApplicationEvent) {
        handlers.forEach { it.handleUntyped(event) }
    }
}
