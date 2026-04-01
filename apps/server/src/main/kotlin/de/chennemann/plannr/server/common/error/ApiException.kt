package de.chennemann.plannr.server.common.error

import org.springframework.http.HttpStatus

open class ApiException(
    val status: HttpStatus,
    val code: String,
    override val message: String,
    val details: Map<String, Any?>? = null,
) : RuntimeException(message)

class NotFoundException(
    code: String,
    message: String,
    details: Map<String, Any?>? = null,
) : ApiException(HttpStatus.NOT_FOUND, code, message, details)

class ConflictException(
    code: String,
    message: String,
    details: Map<String, Any?>? = null,
) : ApiException(HttpStatus.CONFLICT, code, message, details)

class ValidationException(
    code: String,
    message: String,
    details: Map<String, Any?>? = null,
) : ApiException(HttpStatus.UNPROCESSABLE_ENTITY, code, message, details)
