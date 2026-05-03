package de.chennemann.plannr.server.transactions.domain

/**
 * Canonical visible-transaction rule for the current server:
 * - archived rows are hidden
 * - original rows replaced by a modification are hidden (`modified_by_id IS NULL`)
 * - modified recurring occurrences remain visible
 *
 * Split-child hiding is not represented in the current schema yet, so it is intentionally
 * not part of the SQL fragment below.
 */
object TransactionVisibility {
    const val SQL_PREDICATE = "is_archived = FALSE AND modified_by_id IS NULL"

    fun includes(transaction: TransactionRecord): Boolean =
        !transaction.isArchived && transaction.modifiedById == null
}
