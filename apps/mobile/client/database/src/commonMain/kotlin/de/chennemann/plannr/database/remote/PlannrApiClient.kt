package de.chennemann.plannr.database.remote

import io.ktor.client.HttpClientConfig
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.accept
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

object PlannrServerConnection {
    var baseUrl: String = "https://plannr-api.local.chennemann.de"
        private set

    fun useLocalDevelopmentServer() {
        baseUrl = "http://localhost:9000"
    }
}

private val DEFAULT_SERVER_URL: String get() = PlannrServerConnection.baseUrl

internal fun HttpClientConfig<*>.configurePlannrHttpClient() {
    expectSuccess = true

    install(ContentNegotiation) {
        json(
            Json {
                ignoreUnknownKeys = true
                explicitNulls = false
            }
        )
    }
}

internal expect fun createPlannrHttpClient(): HttpClient

internal class PlannrApiClient(
    private val httpClient: HttpClient,
) {
    suspend fun previewPartnerLogo(website: String): String =
        httpClient.post("$DEFAULT_SERVER_URL/partners/logo-preview") {
            contentType(ContentType.Application.Json)
            setBody(ApiLogoPreviewCommand(website))
        }.body<ApiLogoImage>().base64

    suspend fun savePartnerLogo(id: Long, base64: String): ApiPartner =
        httpClient.put("$DEFAULT_SERVER_URL/partners/$id/logo") {
            contentType(ContentType.Application.Json)
            setBody(ApiLogoImage(base64))
        }.body()

    suspend fun removePartnerLogo(id: Long): ApiPartner =
        httpClient.delete("$DEFAULT_SERVER_URL/partners/$id/logo").body()

    suspend fun listAccounts(): List<ApiAccount> =
        httpClient.get("$DEFAULT_SERVER_URL/accounts") {
            accept(ContentType.Application.Json)
            parameter("archived", false)
        }.body()

    suspend fun createAccount(command: ApiCreateAccountCommand): ApiAccount =
        httpClient.post("$DEFAULT_SERVER_URL/accounts") {
            accept(ContentType.Application.Json)
            contentType(ContentType.Application.Json)
            setBody(command)
        }.body()

    suspend fun updateAccount(command: ApiUpdateAccountCommand): ApiAccount =
        httpClient.put("$DEFAULT_SERVER_URL/accounts") {
            accept(ContentType.Application.Json)
            contentType(ContentType.Application.Json)
            setBody(command)
        }.body()

    suspend fun listPockets(accountId: Long? = null): List<ApiPocket> =
        httpClient.get("$DEFAULT_SERVER_URL/pockets") {
            accept(ContentType.Application.Json)
            parameter("archived", false)
            if (accountId != null) {
                parameter("accountId", accountId)
            }
        }.body()

    suspend fun listPartners(query: String? = null, archived: Boolean = false): List<ApiPartner> =
        httpClient.get("$DEFAULT_SERVER_URL/partners") {
            accept(ContentType.Application.Json)
            parameter("archived", archived)
            if (query != null) {
                parameter("query", query)
            }
        }.body()

    suspend fun updatePartner(command: ApiUpdatePartnerCommand): ApiPartner =
        httpClient.put("$DEFAULT_SERVER_URL/partners") {
            contentType(ContentType.Application.Json)
            setBody(command)
        }.body()

    suspend fun archivePartner(id: Long, archived: Boolean): ApiPartner =
        httpClient.post("$DEFAULT_SERVER_URL/partners/$id/" + if (archived) "archive" else "unarchive").body()

    suspend fun createPartner(command: ApiCreatePartnerCommand): ApiPartner =
        httpClient.post("$DEFAULT_SERVER_URL/partners") {
            accept(ContentType.Application.Json)
            contentType(ContentType.Application.Json)
            setBody(command)
        }.body()

    suspend fun listContracts(accountId: Long? = null): List<ApiContract> =
        httpClient.get("$DEFAULT_SERVER_URL/contracts") {
            accept(ContentType.Application.Json)
            parameter("archived", false)
            if (accountId != null) {
                parameter("accountId", accountId)
            }
        }.body()

    suspend fun createContract(command: ApiCreateContractCommand): ApiContract =
        httpClient.post("$DEFAULT_SERVER_URL/contracts") {
            accept(ContentType.Application.Json)
            contentType(ContentType.Application.Json)
            setBody(command)
        }.body()

    suspend fun updateContract(command: ApiUpdateContractCommand): ApiContract =
        httpClient.put("$DEFAULT_SERVER_URL/contracts") {
            accept(ContentType.Application.Json)
            contentType(ContentType.Application.Json)
            setBody(command)
        }.body()

    suspend fun getAccountBalance(accountId: Long): Long =
        httpClient.get("$DEFAULT_SERVER_URL/accounts/$accountId/feed") {
            accept(ContentType.Application.Json)
            parameter("limit", 1)
        }.body<ApiTransactionFeedSummary>().currentBalance

    suspend fun getAccountHistory(accountId: Long, cursor: String? = null): ApiTransactionHistory =
        httpClient.get("$DEFAULT_SERVER_URL/accounts/$accountId/feed") {
            accept(ContentType.Application.Json)
            parameter("limit", 100)
            cursor?.let { parameter("cursor", it) }
        }.body()

    suspend fun getPocketBalance(pocketId: Long): Long =
        httpClient.get("$DEFAULT_SERVER_URL/pockets/$pocketId/feed") {
            accept(ContentType.Application.Json)
            parameter("limit", 1)
        }.body<ApiTransactionFeedSummary>().currentBalance

    suspend fun getContractBalance(contractId: Long): Long =
        httpClient.get("$DEFAULT_SERVER_URL/contracts/$contractId/feed") {
            accept(ContentType.Application.Json)
            parameter("limit", 1)
        }.body<ApiTransactionFeedSummary>().currentBalance

    suspend fun getUpcomingTransactionsForAccount(accountId: Long, count: Int = 100): ApiUpcomingTransactionsResponse =
        httpClient.get("$DEFAULT_SERVER_URL/accounts/$accountId/upcoming-transactions") {
            accept(ContentType.Application.Json)
            parameter("count", count)
        }.body()
}

@Serializable
internal data class ApiAccount(
    val id: Long,
    val name: String,
    val institution: String,
    val currencyCode: String,
    val weekendHandling: String,
)

@Serializable
internal data class ApiCreateAccountCommand(
    val name: String,
    val institution: String,
    val currencyCode: String,
    val weekendHandling: String,
)

@Serializable
internal data class ApiUpdateAccountCommand(
    val id: Long,
    val name: String,
    val institution: String,
    val currencyCode: String,
    val weekendHandling: String,
)

@Serializable
internal data class ApiPocket(
    val id: Long,
    val accountId: Long,
    val contractId: Long? = null,
    val savingGoalId: Long? = null,
    val name: String,
    val isDefault: Boolean,
    val color: Int = 0,
)

@Serializable
internal data class ApiPartner(
    val id: Long,
    val name: String,
    val description: String? = null,
    val logoVersion: String? = null,
    val isArchived: Boolean = false,
) {
    fun toPartner() = de.chennemann.plannr.data.Partner(id, name,
        logoVersion?.let { "$DEFAULT_SERVER_URL/partners/$id/logo/$it" }, description, isArchived)
}

@Serializable
internal data class ApiCreatePartnerCommand(
    val name: String,
    val description: String? = null,
)

@Serializable
internal enum class ApiContractType {
    ACCUMULATING,
    NON_ACCUMULATING,
}

@Serializable
internal data class ApiContract(
    val id: Long,
    val financialProfileId: Long,
    val partnerId: Long? = null,
    val name: String,
    val description: String? = null,
    val color: Int,
    val type: ApiContractType,
    val signingDate: String? = null,
    val expirationDate: String? = null,
    val lastCancellationDate: String? = null,
)

@Serializable
internal data class ApiCreateContractCommand(
    val name: String,
    val description: String? = null,
    val color: Int,
    val type: ApiContractType,
    val accountIds: Set<Long>,
    val financialProfileId: Long? = null,
    val partnerId: Long? = null,
    val signingDate: String? = null,
    val expirationDate: String? = null,
    val lastCancellationDate: String? = null,
)

@Serializable
internal data class ApiUpdateContractCommand(
    val id: Long,
    val financialProfileId: Long,
    val partnerId: Long? = null,
    val name: String,
    val description: String? = null,
    val color: Int,
    val type: ApiContractType,
    val signingDate: String? = null,
    val expirationDate: String? = null,
    val lastCancellationDate: String? = null,
)

@Serializable
internal data class ApiTransactionFeedSummary(
    val currentBalance: Long,
)

@Serializable
internal data class ApiTransactionHistory(
    val transactions: List<ApiHistoryItem>,
    val nextCursor: String? = null,
    val hasMore: Boolean,
)

@Serializable
internal data class ApiHistoryReference(val id: Long, val name: String)

@Serializable
internal data class ApiHistoryItem(
    val transactionId: Long,
    val transactionTemplateId: Long,
    val transactionDate: String,
    val title: String,
    val description: String? = null,
    val transactionAmount: Long,
    val signedAmount: Long,
    val sourcePocket: ApiHistoryReference? = null,
    val destinationPocket: ApiHistoryReference? = null,
    val partner: ApiHistoryReference? = null,
)

@Serializable
internal data class ApiUpcomingTransactionsResponse(
    val transactions: List<ApiUpcomingTransactionItem>,
)

@Serializable
internal data class ApiUpcomingTransactionItem(
    val transactionTemplateId: Long,
    val contractId: Long? = null,
    val occurrenceDate: String,
    val sourcePocketId: Long? = null,
    val destinationPocketId: Long? = null,
    val partnerId: Long? = null,
    val title: String,
    val description: String? = null,
    val amount: Long,
    val currencyCode: String,
)
    
@Serializable
internal data class ApiLogoPreviewCommand(val website: String)
@Serializable
internal data class ApiLogoImage(val base64: String)

@Serializable
internal data class ApiUpdatePartnerCommand(val id: Long, val name: String, val description: String? = null)
