package de.chennemann.plannr.server.common.error

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.server.ServerWebInputException

@RestControllerAdvice
class GlobalExceptionHandler {
    @ExceptionHandler(ServerWebInputException::class)
    fun handleServerWebInputException(exception: ServerWebInputException): ResponseEntity<ErrorResponse> =
        ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .body(
                ErrorResponse(
                    error = ApiError(
                        code = "bad_request",
                        message = "Request body is malformed or invalid",
                    ),
                ),
            )

    @ExceptionHandler(ApiException::class)
    fun handleApiException(exception: ApiException): ResponseEntity<ErrorResponse> =
        ResponseEntity.status(exception.status)
            .body(
                ErrorResponse(
                    error = ApiError(
                        code = exception.code,
                        message = exception.message,
                        details = exception.details,
                    ),
                ),
            )

    @ExceptionHandler(Exception::class)
    fun handleUnexpectedException(exception: Exception): ResponseEntity<ErrorResponse> =
        ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(
                ErrorResponse(
                    error = ApiError(
                        code = "internal_error",
                        message = "An unexpected error occurred",
                    ),
                ),
            )
}
