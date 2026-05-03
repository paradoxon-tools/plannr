package de.chennemann.plannr.server.currencies.persistence

import org.springframework.data.relational.core.mapping.event.AfterConvertCallback
import org.springframework.stereotype.Component

@Component
internal class CurrencyModelPersistenceCallbacks : AfterConvertCallback<CurrencyModel> {
    override fun onAfterConvert(aggregate: CurrencyModel): CurrencyModel = aggregate.persisted()
}
