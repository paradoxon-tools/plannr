package de.chennemann.plannr.server.common.error

data class ErrorResponse(
    val error: ApiError,
)

data class ApiError(
    val code: String,
    val message: String,
    val details: Map<String, Any?>? = null,
)
