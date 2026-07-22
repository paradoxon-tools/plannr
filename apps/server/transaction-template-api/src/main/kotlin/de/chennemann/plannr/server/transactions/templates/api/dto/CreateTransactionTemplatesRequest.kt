package de.chennemann.plannr.server.transactions.templates.api.dto

data class CreateTransactionTemplatesRequest(
    val templates: List<CreateTransactionTemplateRequest>,
)
