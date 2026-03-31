package de.chennemann.plannr.server.partners.api

import de.chennemann.plannr.server.partners.usecases.ArchivePartner
import de.chennemann.plannr.server.partners.usecases.CreatePartner
import de.chennemann.plannr.server.partners.usecases.GetPartner
import de.chennemann.plannr.server.partners.usecases.ListPartners
import de.chennemann.plannr.server.partners.usecases.UnarchivePartner
import de.chennemann.plannr.server.partners.usecases.UpdatePartner
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/partners")
class PartnerController(
    private val createPartner: CreatePartner,
    private val updatePartner: UpdatePartner,
    private val getPartner: GetPartner,
    private val listPartners: ListPartners,
    private val archivePartner: ArchivePartner,
    private val unarchivePartner: UnarchivePartner,
) {
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    suspend fun create(@RequestBody request: CreatePartnerRequest): PartnerResponse =
        PartnerResponse.from(createPartner(request.toCommand()))

    @GetMapping
    suspend fun list(
        @RequestParam(required = false) query: String?,
        @RequestParam(defaultValue = "false") archived: Boolean,
    ): List<PartnerResponse> =
        listPartners(query = query, archived = archived).map(PartnerResponse::from)

    @GetMapping("/{id}")
    suspend fun getById(@PathVariable id: String): PartnerResponse =
        PartnerResponse.from(getPartner(id))

    @PutMapping("/{id}")
    suspend fun update(@PathVariable id: String, @RequestBody request: UpdatePartnerRequest): PartnerResponse =
        PartnerResponse.from(updatePartner(request.toCommand(id)))

    @PostMapping("/{id}/archive")
    suspend fun archive(@PathVariable id: String): PartnerResponse =
        PartnerResponse.from(archivePartner(id))

    @PostMapping("/{id}/unarchive")
    suspend fun unarchive(@PathVariable id: String): PartnerResponse =
        PartnerResponse.from(unarchivePartner(id))
}
