package de.chennemann.plannr.server.partners.api

import org.springframework.context.annotation.Configuration
import org.springframework.http.codec.ServerCodecConfigurer
import org.springframework.web.reactive.config.WebFluxConfigurer

@Configuration
class PartnerLogoCodecConfiguration : WebFluxConfigurer {
    override fun configureHttpMessageCodecs(configurer: ServerCodecConfigurer) {
        // Allows a <=1 MB image encoded as JSON base64, while keeping request buffering bounded.
        configurer.defaultCodecs().maxInMemorySize(2 * 1024 * 1024)
    }
}
