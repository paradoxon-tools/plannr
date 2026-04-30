package de.chennemann.plannr.server.contracts.persistence

import org.springframework.data.relational.core.mapping.event.AfterConvertCallback
import org.springframework.stereotype.Component

@Component
internal class ContractModelPersistenceCallbacks : AfterConvertCallback<ContractModel> {
    override fun onAfterConvert(aggregate: ContractModel): ContractModel = aggregate.persisted()
}
