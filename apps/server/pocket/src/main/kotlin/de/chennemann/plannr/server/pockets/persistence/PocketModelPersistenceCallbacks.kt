package de.chennemann.plannr.server.pockets.persistence

import org.springframework.data.relational.core.mapping.event.AfterConvertCallback
import org.springframework.stereotype.Component

@Component
internal class PocketModelPersistenceCallbacks : AfterConvertCallback<PocketModel> {
    override fun onAfterConvert(aggregate: PocketModel): PocketModel = aggregate.persisted()
}
