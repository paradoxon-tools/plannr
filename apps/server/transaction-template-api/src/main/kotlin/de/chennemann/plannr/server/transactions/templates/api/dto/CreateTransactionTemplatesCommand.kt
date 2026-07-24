package de.chennemann.plannr.server.transactions.templates.api.dto

data class CreateTransactionTemplatesCommand(
    val templates: List<CreateTransactionTemplateCommand>,
)
