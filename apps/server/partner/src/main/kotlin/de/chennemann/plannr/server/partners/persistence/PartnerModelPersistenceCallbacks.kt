package de.chennemann.plannr.server.partners.persistence

import org.springframework.data.relational.core.mapping.event.AfterConvertCallback
import org.springframework.stereotype.Component

@Component
internal class PartnerModelPersistenceCallbacks : AfterConvertCallback<PartnerModel> {
    override fun onAfterConvert(aggregate: PartnerModel): PartnerModel = aggregate.persisted()
}
