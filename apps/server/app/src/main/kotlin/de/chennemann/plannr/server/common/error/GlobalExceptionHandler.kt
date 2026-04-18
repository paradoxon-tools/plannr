package de.chennemann.plannr.server.common.error

import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.http.converter.HttpMessageConversionException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.server.ServerWebInputException

@RestControllerAdvice
class GlobalExceptionHandler {
    private val logger = LoggerFactory.getLogger(GlobalExceptionHandler::class.java)

    @ExceptionHandler(HttpMessageConversionException::class)
    fun handleHttpMessageConversionException(exception: HttpMessageConversionException): ResponseEntity<ErrorResponse> =
        badRequest()

    @ExceptionHandler(ServerWebInputException::class)
    fun handleServerWebInputException(exception: ServerWebInputException): ResponseEntity<ErrorResponse> =
        badRequest()

    private fun badRequest(): ResponseEntity<ErrorResponse> =
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
    fun handleUnexpectedException(exception: Exception): ResponseEntity<ErrorResponse> {
        logger.error("Unexpected API error", exception)
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(
                ErrorResponse(
                    error = ApiError(
                        code = "internal_error",
                        message = "An unexpected error occurred",
                    ),
                ),
            )
    }
}
