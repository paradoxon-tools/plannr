# Transaction materialization and projection design

## Canonical write side

The server uses `transactions` as the single canonical ledger for manual transactions, recurring-materialized occurrences, and modified recurring occurrences.

### Canonical enum vocabulary

The canonical vocabulary is defined in `common/domain/CanonicalEnums.kt`:
- `TransactionType`: `INCOME`, `EXPENSE`, `TRANSFER`
- `TransactionStatus`: `PENDING`, `CLEARED`, `RECONCILED`
- `RecurrenceType`: `NONE`, `DAILY`, `WEEKLY`, `MONTHLY`, `YEARLY`
- `WeekendHandling`: `NO_SHIFT`, `MOVE_BEFORE`, `MOVE_AFTER`
- `TransactionOrigin`: `MANUAL`, `RECURRING_MATERIALIZED`, `RECURRING_MODIFICATION`

Normalization happens at domain boundaries so persistence and APIs stay uppercase and deterministic.

### Canonical transaction scoping

Command-side writes persist pocket references, not denormalized account or contract scope:
- non-transfers use canonical `pocket_id`
- transfers use `source_pocket_id` and `destination_pocket_id`
- `account_id` is derived from selected pocket relationships during resolution/querying
- `contract_id` is derived only on query-side pocket/future feeds from the contract-pocket relationship

This keeps canonical rows minimal and makes projection-side denormalization explicit.

### Visibility rules

Visible transactions satisfy:
- not archived
- not hidden by `modified_by_id`

That rule is centralized in `transactions/domain/TransactionVisibility.kt` and reused by the repository/query pipeline.

## Recurring templates and versioning

Recurring templates live in `recurring_transactions` and support the full recurrence engine including `YEARLY`.

Important rules:
- selector lists are normalized to sorted/distinct/null-when-empty
- `maxRecurrenceCount` is transient input and is normalized into persisted `finalOccurrenceDate`
- versioning is driven by `firstOccurrenceDate`
- creating a new version closes the predecessor on the last generated occurrence before the new start date
- lineage is tracked via `previousVersionId`

## Materialization

`RecurringTransactionMaterializer` creates canonical transaction rows.

Rules:
- only non-archived templates are materialized
- materialized rows are always `PENDING`
- materialized rows use `transactionOrigin = RECURRING_MATERIALIZED`
- weekend handling is applied using the owning account setting
- duplicate root occurrences are prevented by code and DB constraints
- modified occurrence chains block rematerialization for the same root date
- `lastMaterializedDate` advances only when new rows are created

### Horizon policy

Materialization aims to provide useful future coverage by satisfying both:
- all occurrences through the end of next calendar month
- at least the next 5 future occurrences for sparse recurrences

## Modified recurring occurrences

A modified recurring occurrence is represented as:
- original root row kept in the ledger
- original root hidden via `modified_by_id`
- new child row with `parent_transaction_id`
- child retains `recurring_transaction_id`
- child uses `transactionOrigin = RECURRING_MODIFICATION`

## Query projections

The query side is rebuilt from canonical visible transactions.

### Historical/current feeds

Historical feeds include only `transaction_date <= today`:
- `account_transaction_feed`
- `pocket_transaction_feed`

Balances in these feeds determine:
- `account_query.current_balance`
- `pocket_query.current_balance`

### Future feeds

Future feeds include only `transaction_date > today`:
- `account_future_transaction_feed`
- `pocket_future_transaction_feed`

Projected balances start from current balance and accumulate future rows only.
Historical and future feeds are rebuilt independently.

## Synchronization model

Projection synchronization is asynchronous:
- writes mark account/pocket/full dirty scopes
- `ProjectionScheduler.processDirtyScopes()` rebuilds only affected scopes
- `ProjectionScheduler.runFullRebuildSafetyJob()` periodically rebuilds everything
- scheduler logging records dirty-scope processing and full rebuild durations

## Migration/backfill

Migration `V13__normalize_legacy_values_and_backfill.sql` normalizes legacy enum values and recurring selector storage, and backfills denormalized contract ids for pocket read models.
