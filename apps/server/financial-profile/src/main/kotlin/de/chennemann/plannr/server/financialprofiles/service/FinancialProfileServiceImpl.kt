package de.chennemann.plannr.server.financialprofiles.service

import de.chennemann.plannr.server.common.error.ConflictException
import de.chennemann.plannr.server.common.error.NotFoundException
import de.chennemann.plannr.server.common.error.ValidationException
import de.chennemann.plannr.server.common.time.TimeProvider
import de.chennemann.plannr.server.financialprofiles.api.dto.CreateFinancialProfileCommand
import de.chennemann.plannr.server.financialprofiles.api.dto.FinancialProfile
import de.chennemann.plannr.server.financialprofiles.api.dto.UpdateFinancialProfileCommand
import de.chennemann.plannr.server.financialprofiles.domain.FinancialProfileRepository
import de.chennemann.plannr.server.financialprofiles.domain.FinancialProfileUsageRepository
import de.chennemann.plannr.server.financialprofiles.domain.save
import de.chennemann.plannr.server.financialprofiles.persistence.FinancialProfileModel
import de.chennemann.plannr.server.financialprofiles.persistence.toDTO
import de.chennemann.plannr.server.financialprofiles.persistence.toModel
import de.chennemann.plannr.server.transactions.projection.service.TransactionProjectionChangeEvent
import de.chennemann.plannr.server.transactions.projection.service.TransactionProjectionEventQueue
import kotlinx.coroutines.flow.toList
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
@Transactional
internal class FinancialProfileServiceImpl(
    private val financialProfileRepository: FinancialProfileRepository,
    private val financialProfileUsageRepository: FinancialProfileUsageRepository,
    private val timeProvider: TimeProvider,
    private val projectionEventQueue: TransactionProjectionEventQueue? = null,
) : FinancialProfileService {
    override suspend fun create(command: CreateFinancialProfileCommand): FinancialProfile {
        val name = normalizeName(command.name)
        ensureNameAvailable(name, currentId = null)
        val created = saveWithDuplicateHandling(
            FinancialProfileModel(
                id = null,
                name = name,
                description = command.description,
                kind = normalizeKind(command.kind),
                isDefault = false,
                isFallback = false,
                isArchived = false,
                createdAt = timeProvider(),
            ),
        )
        enqueueProjectionChange(created.id)
        return created
    }

    override suspend fun update(command: UpdateFinancialProfileCommand): FinancialProfile {
        val existing = existingProfile(command.id)
        val name = normalizeName(command.name)
        val kind = normalizeKind(command.kind)
        if (existing.isFallback && (name != FALLBACK_NAME || kind != FALLBACK_KIND)) {
            throw ConflictException(
                code = "conflict",
                message = "Fallback financial profile identity cannot be changed",
                details = mapOf("id" to existing.id),
            )
        }
        ensureNameAvailable(name, currentId = existing.id)
        val updated = saveWithDuplicateHandling(
            existing.copy(
                name = name,
                description = command.description,
                kind = kind,
            ).toModel(),
        )
        enqueueProjectionChange(updated.id)
        return updated
    }

    override suspend fun makeDefault(id: Long): FinancialProfile {
        val existing = existingProfile(id)
        if (existing.isArchived) {
            throw ConflictException(
                code = "conflict",
                message = "Archived financial profile cannot be the default",
                details = mapOf("id" to id),
            )
        }
        financialProfileRepository.clearDefault()
        return financialProfileRepository.save(existing.copy(isDefault = true))
    }

    override suspend fun archive(id: Long): FinancialProfile {
        val existing = existingProfile(id)
        ensureNotFallback(existing, "archive")
        ensureNotDefault(existing, "archive")
        val updated = financialProfileRepository.save(existing.copy(isArchived = true))
        enqueueProjectionChange(updated.id)
        return updated
    }

    override suspend fun unarchive(id: Long): FinancialProfile {
        val existing = existingProfile(id)
        val updated = financialProfileRepository.save(existing.copy(isArchived = false))
        enqueueProjectionChange(updated.id)
        return updated
    }

    override suspend fun delete(id: Long) {
        val existing = existingProfile(id)
        ensureNotFallback(existing, "delete")
        var fallback = financialProfileRepository.findFallback()?.toDTO()
            ?: throw ConflictException(
                code = "conflict",
                message = "Fallback financial profile is missing",
            )
        if (existing.isDefault) {
            financialProfileRepository.clearDefault()
            fallback = financialProfileRepository.save(fallback.copy(isDefault = true))
        }
        financialProfileUsageRepository.reassignReferences(
            sourceProfileId = existing.id,
            fallbackProfileId = fallback.id,
            fallbackProfileName = fallback.name,
            fallbackProfileKind = fallback.kind,
        )
        try {
            financialProfileRepository.deleteById(id)
        } catch (_: DataIntegrityViolationException) {
            throw ConflictException(
                code = "conflict",
                message = "Financial profile references could not be reassigned",
                details = mapOf("id" to id),
            )
        }
        enqueueProjectionChange(id)
    }

    override suspend fun list(query: String?, archived: Boolean): List<FinancialProfile> =
        financialProfileRepository
            .findAllByQueryAndArchived(query?.trim()?.takeIf { it.isNotBlank() }, archived)
            .toList()
            .map(FinancialProfileModel::toDTO)

    override suspend fun getById(id: Long): FinancialProfile? =
        financialProfileRepository.findById(id)?.toDTO()

    override suspend fun resolveForAssignment(id: Long?): FinancialProfile {
        val profile = if (id == null) {
            financialProfileRepository.findDefault()?.toDTO()
                ?: throw ConflictException(
                    code = "conflict",
                    message = "Default financial profile is missing",
                )
        } else {
            getById(id)
                ?: throw NotFoundException(
                    code = "not_found",
                    message = "Financial profile not found",
                    details = mapOf("id" to id),
                )
        }
        if (profile.isArchived) {
            throw ValidationException(
                code = "validation_error",
                message = "Archived financial profile cannot be assigned",
                details = mapOf("id" to profile.id),
            )
        }
        return profile
    }

    private suspend fun existingProfile(id: Long): FinancialProfile =
        getById(id)
            ?: throw NotFoundException(
                code = "not_found",
                message = "Financial profile not found",
                details = mapOf("id" to id),
            )

    private suspend fun ensureNameAvailable(name: String, currentId: Long?) {
        val existing = financialProfileRepository.findByNormalizedName(name)?.toDTO()
        if (existing != null && existing.id != currentId) {
            duplicateName(name)
        }
    }

    private suspend fun saveWithDuplicateHandling(model: FinancialProfileModel): FinancialProfile =
        try {
            financialProfileRepository.save(model).toDTO()
        } catch (_: DataIntegrityViolationException) {
            duplicateName(model.name)
        }

    private fun normalizeName(value: String): String =
        value.trim().takeIf { it.isNotBlank() }
            ?: throw ValidationException(
                code = "validation_error",
                message = "Financial profile name must not be blank",
                details = mapOf("field" to "name"),
            )

    private fun normalizeKind(value: String): String {
        val normalized = value.trim().uppercase()
        return FinancialProfileKind.entries.firstOrNull { it.name == normalized }?.name
            ?: throw ValidationException(
                code = "validation_error",
                message = "Financial profile kind is invalid",
                details = mapOf("field" to "kind", "supportedValues" to FinancialProfileKind.entries.map { it.name }),
            )
    }

    private fun ensureNotDefault(profile: FinancialProfile, action: String) {
        if (profile.isDefault) {
            throw ConflictException(
                code = "conflict",
                message = "Default financial profile cannot be ${action}d",
                details = mapOf("id" to profile.id),
            )
        }
    }

    private fun ensureNotFallback(profile: FinancialProfile, action: String) {
        if (profile.isFallback) {
            throw ConflictException(
                code = "conflict",
                message = "Fallback financial profile cannot be ${action}d",
                details = mapOf("id" to profile.id),
            )
        }
    }

    private fun duplicateName(name: String): Nothing =
        throw ConflictException(
            code = "conflict",
            message = "Financial profile name already exists",
            details = mapOf("name" to name),
        )

    private suspend fun enqueueProjectionChange(id: Long) {
        projectionEventQueue?.enqueue(TransactionProjectionChangeEvent.FinancialProfileChanged(id))
    }
}

private enum class FinancialProfileKind {
    PERSON,
    GROUP,
}

private const val FALLBACK_NAME = "Unassigned"
private const val FALLBACK_KIND = "GROUP"
