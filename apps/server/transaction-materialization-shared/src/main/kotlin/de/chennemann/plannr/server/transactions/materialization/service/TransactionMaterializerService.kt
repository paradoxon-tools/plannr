package de.chennemann.plannr.server.transactions.materialization.service

interface TransactionMaterializerService {
    suspend fun materialize(operation: MaterializationOperation): List<MaterializedTransaction>
}
