package de.chennemann.plannr.server.projection

import de.chennemann.plannr.server.common.events.ApplicationEventHandler
import de.chennemann.plannr.server.partners.events.PartnerUpdated
import de.chennemann.plannr.server.pockets.events.PocketUpdated
import kotlinx.coroutines.reactor.awaitSingle
import kotlin.reflect.KClass
import org.springframework.r2dbc.core.DatabaseClient
import org.springframework.stereotype.Component

@Component
class AccountFeedPocketMetadataProjector(
    private val databaseClient: DatabaseClient,
) : ApplicationEventHandler<PocketUpdated> {
    override val eventType: KClass<PocketUpdated> = PocketUpdated::class

    override suspend fun handle(event: PocketUpdated) {
        if (event.before.name == event.after.name && event.before.color == event.after.color) {
            return
        }

        databaseClient.sql(
            """
            UPDATE account_transaction_feed
            SET source_pocket_name = :name,
                source_pocket_color = :color
            WHERE source_pocket_id = :pocketId
            """.trimIndent(),
        )
            .bind("pocketId", event.after.id)
            .bind("name", event.after.name)
            .bind("color", event.after.color)
            .fetch()
            .rowsUpdated()
            .awaitSingle()

        databaseClient.sql(
            """
            UPDATE account_transaction_feed
            SET destination_pocket_name = :name,
                destination_pocket_color = :color
            WHERE destination_pocket_id = :pocketId
            """.trimIndent(),
        )
            .bind("pocketId", event.after.id)
            .bind("name", event.after.name)
            .bind("color", event.after.color)
            .fetch()
            .rowsUpdated()
            .awaitSingle()
    }
}

@Component
class PocketFeedPocketMetadataProjector(
    private val databaseClient: DatabaseClient,
) : ApplicationEventHandler<PocketUpdated> {
    override val eventType: KClass<PocketUpdated> = PocketUpdated::class

    override suspend fun handle(event: PocketUpdated) {
        if (event.before.name == event.after.name && event.before.color == event.after.color) {
            return
        }

        databaseClient.sql(
            """
            UPDATE pocket_transaction_feed
            SET transfer_pocket_name = :name,
                transfer_pocket_color = :color
            WHERE transfer_pocket_id = :pocketId
            """.trimIndent(),
        )
            .bind("pocketId", event.after.id)
            .bind("name", event.after.name)
            .bind("color", event.after.color)
            .fetch()
            .rowsUpdated()
            .awaitSingle()
    }
}

@Component
class FeedPartnerMetadataProjector(
    private val databaseClient: DatabaseClient,
) : ApplicationEventHandler<PartnerUpdated> {
    override val eventType: KClass<PartnerUpdated> = PartnerUpdated::class

    override suspend fun handle(event: PartnerUpdated) {
        if (event.before.name == event.after.name) {
            return
        }

        databaseClient.sql(
            """
            UPDATE account_transaction_feed
            SET partner_name = :name
            WHERE partner_id = :partnerId
            """.trimIndent(),
        )
            .bind("partnerId", event.after.id)
            .bind("name", event.after.name)
            .fetch()
            .rowsUpdated()
            .awaitSingle()

        databaseClient.sql(
            """
            UPDATE pocket_transaction_feed
            SET partner_name = :name
            WHERE partner_id = :partnerId
            """.trimIndent(),
        )
            .bind("partnerId", event.after.id)
            .bind("name", event.after.name)
            .fetch()
            .rowsUpdated()
            .awaitSingle()
    }
}
