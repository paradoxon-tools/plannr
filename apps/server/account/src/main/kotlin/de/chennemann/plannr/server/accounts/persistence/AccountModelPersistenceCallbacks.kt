package de.chennemann.plannr.server.accounts.persistence

import org.springframework.data.relational.core.mapping.event.AfterConvertCallback
import org.springframework.stereotype.Component

@Component
internal class AccountModelPersistenceCallbacks : AfterConvertCallback<AccountModel> {
    override fun onAfterConvert(aggregate: AccountModel): AccountModel = aggregate.persisted()
}
