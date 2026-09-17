package de.chennemann.plannr.server.partners.api

import de.chennemann.plannr.server.partners.api.dto.Partner
import de.chennemann.plannr.server.partners.api.dto.PartnerLogoImage
import de.chennemann.plannr.server.partners.api.dto.PreviewPartnerLogoCommand
import de.chennemann.plannr.server.partners.service.PartnerLogoLoader
import de.chennemann.plannr.server.partners.service.PartnerLogoService
import org.springframework.http.CacheControl
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.time.Duration
import java.util.Base64

@RestController
@RequestMapping("/partners")
class PartnerLogoController(private val loader: PartnerLogoLoader, private val logos: PartnerLogoService) {
    @PostMapping("/logo-preview")
    suspend fun preview(@RequestBody command: PreviewPartnerLogoCommand): ResponseEntity<PartnerLogoImage> =
        ResponseEntity.ok().cacheControl(CacheControl.noStore())
            .body(PartnerLogoImage(Base64.getEncoder().encodeToString(loader.preview(command.website))))

    @PutMapping("/{id}/logo")
    suspend fun save(@PathVariable id: Long, @RequestBody image: PartnerLogoImage): Partner =
        logos.save(id, image.base64)

    @DeleteMapping("/{id}/logo")
    suspend fun remove(@PathVariable id: Long): Partner = logos.remove(id)

    @GetMapping("/{id}/logo/{version}", produces = [MediaType.IMAGE_PNG_VALUE])
    suspend fun image(@PathVariable id: Long, @PathVariable version: String): ResponseEntity<ByteArray> =
        ResponseEntity.ok().contentType(MediaType.IMAGE_PNG)
            .cacheControl(CacheControl.maxAge(Duration.ofDays(365)).cachePublic().immutable())
            .body(logos.image(id, version))
}
