package de.chennemann.plannr.data

import de.chennemann.plannr.recurrence.RecurrenceCalculator
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import kotlinx.datetime.Month

import kotlinx.serialization.Serializable

@Serializable
data class ProcessableTransactionTemplate(
    val template: TransactionTemplate,
    val nextOccurrence: LocalDate?,
    val lastMaterializedOccurrence: LocalDate?,
)

@Serializable
data class TransactionTemplate(
    val id: Long,
    val title: String,
    val description: String?,
    val contractId: Long?,

    val recurrencePattern: RecurrencePattern,

    val amount: Amount,
    val sourcePocketId: Long?,
    val destinationPocketId: Long?,
    val partnerId: Long?,
) {

    init {
        require(recurrencePattern.skipCount >= 0) { "Skip count cannot be negative" }
        require(recurrencePattern.finalOccurrenceDate == null || recurrencePattern.referenceDate <= recurrencePattern.finalOccurrenceDate) { "Reference date must be before end date" }
    }

    val isRecurring: Boolean
        get() = recurrencePattern.recurrenceType != RecurrenceType.NONE

    /**
     * Calculates transaction dates based on the recurrence pattern.
     *
     * Examples:
     * - Monthly on the 15th with skipCount=1: 15th of every other month
     * - Weekly on Mon/Wed/Fri: Every Monday, Wednesday, and Friday
     * - Monthly on the last day with daysOfMonth=[-1]: Last day of each month
     *
     * @return Sequence of dates when transactions should occur
     */
    fun getTransactionDates(): Sequence<LocalDate> {
        val c = RecurrenceCalculator(recurrencePattern.recurrenceType)
        return c.calculateDates(recurrencePattern)
    }

    /**
     * Value object containing all recurrence constraints and validation logic
     */
    @Serializable
    data class RecurrencePattern(
        val referenceDate: LocalDate,
        val recurrenceType: RecurrenceType = RecurrenceType.NONE,
        val finalOccurrenceDate: LocalDate? = null,
        val maxRecurrenceCount: Int? = null,
        val skipCount: Int = 0,
        val daysOfWeek: List<DayOfWeek>? = null,
        val weeksOfMonth: List<Int>? = null,
        val daysOfMonth: List<Int>? = null,
        val monthsOfYear: List<Month>? = null
    )

    companion object {
        operator fun invoke(id: Long, title: String, referenceDate: LocalDate, amount: Amount, description: String? = null, contractId: Long? = null, sourcePocketId: Long? = null, destinationPocketId: Long? = null, partnerId: Long? = null) = TransactionTemplate(
            id = id,
            title = title,
            description = description,
            contractId = contractId,
            recurrencePattern = RecurrencePattern(
                referenceDate = referenceDate
            ),
            amount = amount,
            sourcePocketId = sourcePocketId,
            destinationPocketId = destinationPocketId,
            partnerId = partnerId
        )
    }
}
