package de.chennemann.plannr.server.pockets.support

import de.chennemann.plannr.server.pockets.api.dto.Pocket
import de.chennemann.plannr.server.pockets.api.dto.CreatePocketCommand
import de.chennemann.plannr.server.pockets.api.dto.UpdatePocketCommand

object PocketFixtures {
    const val DEFAULT_ID = "poc_123"
    const val DEFAULT_ACCOUNT_ID = 1L
    const val DEFAULT_NAME = "Bills"
    const val DEFAULT_DESCRIPTION = "Monthly fixed costs"
    const val DEFAULT_COLOR = 123456
    const val DEFAULT_CREATED_AT = 1_710_000_100L

    fun pocket(
        id: String = DEFAULT_ID,
        accountId: Long = DEFAULT_ACCOUNT_ID,
        name: String = DEFAULT_NAME,
        description: String? = DEFAULT_DESCRIPTION,
        color: Int = DEFAULT_COLOR,
        isDefault: Boolean = false,
        isArchived: Boolean = false,
        createdAt: Long = DEFAULT_CREATED_AT,
    ): Pocket =
        Pocket(id, accountId, name, description, color, isDefault, isArchived, createdAt)

    fun createPocketCommand(
        accountId: Long = DEFAULT_ACCOUNT_ID,
        name: String = DEFAULT_NAME,
        description: String? = DEFAULT_DESCRIPTION,
        color: Int = DEFAULT_COLOR,
        isDefault: Boolean = false,
    ): CreatePocketCommand =
        CreatePocketCommand(accountId, name, description, color, isDefault)

    fun updatePocketCommand(
        id: String = DEFAULT_ID,
        accountId: Long = DEFAULT_ACCOUNT_ID,
        name: String = DEFAULT_NAME,
        description: String? = DEFAULT_DESCRIPTION,
        color: Int = DEFAULT_COLOR,
        isDefault: Boolean = false,
    ): UpdatePocketCommand =
        UpdatePocketCommand(id, accountId, name, description, color, isDefault)

}
