package de.chennemann.plannr.server.support

import org.springframework.test.web.reactive.server.WebTestClient

fun WebTestClient.BodyContentSpec.expectApiError(
    code: String,
    message: String? = null,
    details: Map<String, Any> = emptyMap(),
): WebTestClient.BodyContentSpec {
    jsonPath("$.error.code").isEqualTo(code)
    message?.let { jsonPath("$.error.message").isEqualTo(it) }
    details.forEach { (key, value) ->
        jsonPath("$.error.details.$key").isEqualTo(value)
    }
    return this
}
