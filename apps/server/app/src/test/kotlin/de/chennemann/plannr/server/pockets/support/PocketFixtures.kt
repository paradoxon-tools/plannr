package de.chennemann.plannr.server.pockets.support

import de.chennemann.plannr.server.pockets.api.CreatePocketRequest
import de.chennemann.plannr.server.pockets.api.UpdatePocketRequest
import de.chennemann.plannr.server.pockets.domain.Pocket
import de.chennemann.plannr.server.pockets.usecases.CreatePocket
import de.chennemann.plannr.server.pockets.usecases.UpdatePocket

object PocketFixtures {
    const val DEFAULT_ID = "poc_123"
    const val DEFAULT_ACCOUNT_ID = "acc_123"
    const val DEFAULT_NAME = "Bills"
    const val DEFAULT_DESCRIPTION = "Monthly fixed costs"
    const val DEFAULT_COLOR = 123456
    const val DEFAULT_CREATED_AT = 1_710_000_100L

    fun pocket(
        id: String = DEFAULT_ID,
        accountId: String = DEFAULT_ACCOUNT_ID,
        name: String = DEFAULT_NAME,
        description: String? = DEFAULT_DESCRIPTION,
        color: Int = DEFAULT_COLOR,
        isDefault: Boolean = false,
        isArchived: Boolean = false,
        createdAt: Long = DEFAULT_CREATED_AT,
    ): Pocket =
        Pocket(
            id = id,
            accountId = accountId,
            name = name,
            description = description,
            color = color,
            isDefault = isDefault,
            isArchived = isArchived,
            createdAt = createdAt,
        )

    fun createPocketCommand(
        accountId: String = DEFAULT_ACCOUNT_ID,
        name: String = DEFAULT_NAME,
        description: String? = DEFAULT_DESCRIPTION,
        color: Int = DEFAULT_COLOR,
        isDefault: Boolean = false,
    ): CreatePocket.Command =
        CreatePocket.Command(
            accountId = accountId,
            name = name,
            description = description,
            color = color,
            isDefault = isDefault,
        )

    fun updatePocketCommand(
        id: String = DEFAULT_ID,
        accountId: String = DEFAULT_ACCOUNT_ID,
        name: String = DEFAULT_NAME,
        description: String? = DEFAULT_DESCRIPTION,
        color: Int = DEFAULT_COLOR,
        isDefault: Boolean = false,
    ): UpdatePocket.Command =
        UpdatePocket.Command(
            id = id,
            accountId = accountId,
            name = name,
            description = description,
            color = color,
            isDefault = isDefault,
        )

    fun createPocketRequest(
        accountId: String = DEFAULT_ACCOUNT_ID,
        name: String = DEFAULT_NAME,
        description: String? = DEFAULT_DESCRIPTION,
        color: Int = DEFAULT_COLOR,
        isDefault: Boolean = false,
    ): CreatePocketRequest =
        CreatePocketRequest(
            accountId = accountId,
            name = name,
            description = description,
            color = color,
            isDefault = isDefault,
        )

    fun updatePocketRequest(
        accountId: String = DEFAULT_ACCOUNT_ID,
        name: String = DEFAULT_NAME,
        description: String? = DEFAULT_DESCRIPTION,
        color: Int = DEFAULT_COLOR,
        isDefault: Boolean = false,
    ): UpdatePocketRequest =
        UpdatePocketRequest(
            accountId = accountId,
            name = name,
            description = description,
            color = color,
            isDefault = isDefault,
        )
}
