package de.chennemann.plannr.server.development

data class DevelopmentSeedResult(
    val scenario: String,
    val accounts: List<SeededResource>,
    val pockets: List<SeededResource>,
    val partners: List<SeededResource>,
    val contracts: List<SeededResource>,
    val recurringTransactions: List<SeededResource>,
)

data class SeededResource(
    val id: String,
    val name: String,
    val status: SeededResourceStatus,
)

enum class SeededResourceStatus {
    CREATED,
    EXISTING,
    UPDATED,
}
