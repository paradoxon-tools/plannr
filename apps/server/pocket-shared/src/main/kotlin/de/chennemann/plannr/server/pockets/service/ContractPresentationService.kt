package de.chennemann.plannr.server.pockets.service

interface ContractPresentationService {
    suspend fun updatePresentation(contractId: Long, name: String, description: String?, color: Int)
}
