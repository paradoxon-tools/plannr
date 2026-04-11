# Transaction Materialization & Feed Projection Plan

## Status legend

- `[x]` done
- `[ ]` not done
- `[~]` in progress / partially done

---

## Goal

Introduce a proper **transaction materialization layer** in the server so that:

1. recurring transaction generation is separated from the query/read side,
2. a single canonical transaction store contains both:
   - manually created transactions,
   - recurring-materialized transactions,
   - modified recurring occurrences,
3. account/pocket feeds are derived from that canonical store,
4. read models can be optimized independently,
5. projection synchronization is handled by scheduled/background processes,
6. account and pocket balances **exclude future transactions**,
7. future transactions are still queryable through dedicated read models.

This plan reflects the current implementation analysis and the following agreed refinements:

- use the legacy enums and semantics as the canonical model,
- add `YEARLY` recurrence,
- rely on `isArchived` instead of introducing `isActive`,
- recurring versioning should not require `effectiveFromDate`,
- old recurring versions end **inclusive** on their last generated occurrence,
- future transactions must not affect current balances,
- projection synchronization should be cron/background-job based,
- the client should decide how much of the future horizon it wants to display.

---

## Architectural direction

## Canonical write/materialization side

The server will use a **canonical transaction ledger** as the source of truth for transaction history.

### Recommendation

Use the existing `transactions` table as the canonical ledger and evolve it to support:

- manual transactions,
- recurring-materialized transactions,
- recurring occurrence modifications,
- projection scheduling metadata if needed.

This avoids introducing a second canonical transaction table that duplicates the meaning of `transactions`.

### Canonical ledger responsibilities

The canonical ledger will own:

- transaction lifecycle,
- visibility rules for modified recurring occurrences,
- transaction status,
- linkage to recurring templates,
- recurrence materialization outputs,
- source data for all read projections.

## Query side

The query side becomes fully derived and disposable:

- account summary read model,
- pocket summary read model,
- account historical transaction feed,
- pocket historical transaction feed,
- account future transaction feed,
- pocket future transaction feed.

## Synchronization model

Projection updates will no longer be required to happen synchronously in the command transaction.

Instead:

- command/materialization writes update the canonical ledger,
- affected scopes are marked dirty,
- scheduled projection jobs rebuild affected read models,
- a periodic full rebuild job exists as a safety net.

This creates a cleaner separation between:

- transaction generation / mutation,
- read optimization.

---

## Target domain model

## Enums to adopt from legacy

### TransactionStatus

Use the legacy status model exactly:

- `PENDING`
- `CLEARED`
- `RECONCILED`

### TransactionType

Use the legacy type model exactly:

- `INCOME`
- `EXPENSE`
- `TRANSFER`

### RecurrenceType

Use the legacy recurrence model exactly:

- `NONE`
- `DAILY`
- `WEEKLY`
- `MONTHLY`
- `YEARLY`

### WeekendHandling

Use the legacy weekend handling model exactly:

- `NO_SHIFT`
- `MOVE_BEFORE`
- `MOVE_AFTER`

---

## Core design decisions

### 1. Canonical transaction ledger contains manual + recurring-generated transactions

- manual transactions are written directly by transaction use cases,
- recurring materialization creates normal transaction rows in the same ledger,
- modified recurring occurrences are represented as linked transactions in the same ledger.

### 2. Future transactions do not affect current balances

- `account_query.current_balance` is computed from canonical transactions with `transaction_date <= today`,
- `pocket_query.current_balance` is computed from canonical transactions with `transaction_date <= today`.

### 3. Future transactions get separate feeds

Recommended design:

- keep historical/current feeds separate from future feeds,
- project future transactions into dedicated read models,
- include projected balances on future feed rows,
- allow client-controlled range queries against future feeds.

This is preferred over mixing future rows into the normal feeds because:

- `current_balance` stays aligned with the latest historical row,
- pagination semantics remain simple,
- the client can opt in to future data explicitly,
- future horizon can be extended independently.

### 4. Recurring template archival replaces active flag

- archived recurring templates are ignored by materialization,
- no separate `isActive` flag is introduced.

### 5. Recurring versioning is start-date driven

No `effectiveFromDate` is required.

Instead:

- a new version is created with its own `firstOccurrenceDate`,
- the previous version is closed by setting its `finalOccurrenceDate` to the **last occurrence strictly before** the new version's first occurrence,
- old version end remains inclusive.

Example:

- old monthly version occurs on the 15th,
- new version starts on `2025-06-15`,
- old version ends on `2025-05-15`.

### 6. Projection synchronization is asynchronous

- command-side writes stay transactional against canonical tables,
- feed/query projections are rebuilt asynchronously from canonical state,
- correctness is ensured by idempotent rebuild logic and background jobs.

### 7. Derived scoping columns belong only on query tables

- do **not** persist derived scoping columns such as `account_id` or `contract_id` in canonical recurring/materialization tables when they can be derived from canonical relationships,
- canonical tables keep only source-of-truth relationships,
- command-side validation must rely on canonical pocket/account/contract relationships, not duplicated derived columns,
- projection code derives and denormalizes those values into query/read tables,
- any indexing need for those values should be satisfied on query tables rather than by duplicating them in canonical storage.

### 8. Command-side transactions persist pocket references, not derived account/contract scope

- on the command side, transaction rows persist the relevant pocket reference(s) as the canonical scope anchor,
- for non-transfer transactions this means persisting the single transaction `pocketId`,
- for transfers this means persisting `sourcePocketId` and `destinationPocketId`,
- `accountId` and optional `contractId` are derived from the persisted pocket relationship(s) and are not stored as canonical transaction columns,
- projections may denormalize derived `account_id` and `contract_id` into query tables when needed for read performance,
- because each contract has exactly one underlying pocket, contract transaction APIs should query the pocket-based projections by derived `contractId` rather than introducing a separate contract transaction feed.

---

## Implementation phases

# Phase 0 - freeze target semantics

## 0.1 Finalize enum vocabulary and normalization

- `[x]` Replace free-form transaction `type` strings with the legacy enum vocabulary.
- `[x]` Replace free-form transaction `status` strings with the legacy enum vocabulary.
- `[x]` Replace free-form recurring `recurrenceType` strings with the legacy enum vocabulary.
- `[x]` Replace server weekend handling strings with the legacy enum vocabulary.
- `[x]` Normalize API payloads and persistence mappings to use one canonical casing strategy.

Verification:

- `[x]` Domain tests reject non-enum values.
- `[x]` API tests reject invalid enum values.
- `[x]` persistence round-trip tests prove enum mapping is stable.

## 0.2 Lock balance semantics

- `[x]` Define `current_balance` as balance using only `transaction_date <= today`.
- `[x]` Define future feed balances as projections that start from `current_balance` and accumulate future transactions only.
- `[x]` Document that future transactions never alter `current_balance`.

Verification:

- `[x]` summary projection tests prove future transactions do not change `current_balance`.
- `[x]` future feed tests prove projected balances still include future transactions in order.

## 0.3 Lock visibility semantics for modified recurring occurrences

Adopt the legacy visibility rules:

A transaction is visible if:

- it is not archived,
- it is not a hidden original replaced by a modification,
- it is not a split child,
- modified recurring occurrences remain visible.

- `[x]` Document the canonical visibility SQL/logic for the server.
- `[x]` Refactor repository/query code so visibility logic is centralized.

Verification:

- `[x]` modified occurrence tests prove originals are hidden and modifications are visible.
- `[ ]` split-child tests still behave correctly if split support remains relevant.

---

# Phase 1 - strengthen the canonical transaction ledger

## 1.1 Evolve `transactions` into the materialized transaction ledger

- `[x]` Confirm the existing `transactions` table is the canonical materialized ledger.
- `[x]` Add or refine columns needed for materialization and tracing.
- `[x]` Remove canonical transaction `account_id` / `contract_id` columns where they are derivable from persisted pocket relationships.
- `[x]` Standardize canonical transaction persistence around pocket references (`pocket_id` for non-transfers, `source_pocket_id` / `destination_pocket_id` for transfers).
- `[x]` Preserve these link fields:
  - `[x]` `parent_transaction_id`
  - `[x]` `recurring_transaction_id`
  - `[x]` `modified_by_id`
- `[x]` Decide whether to add a `transaction_origin` column with values like:
  - `MANUAL`
  - `RECURRING_MATERIALIZED`
  - `RECURRING_MODIFICATION`
- `[x]` Add indexes needed for materialization, visibility, and future feed projection.

Recommended additional indexes:

- `(pocket_id, transaction_date, created_at, id)` for non-transfer transaction lookup
- `(source_pocket_id, transaction_date, created_at, id)`
- `(destination_pocket_id, transaction_date, created_at, id)`
- `(recurring_transaction_id, transaction_date)`
- `(parent_transaction_id)`
- `(modified_by_id)`

Verification:

- `[x]` migration tests apply cleanly on an empty schema.
- `[x]` migration tests apply cleanly over current server schema.
- `[x]` repository tests verify all new columns round-trip.

## 1.2 Enforce recurrence duplicate protection at the database level

- `[x]` Add a unique constraint or partial unique index that prevents duplicate canonical occurrence rows for the same recurring template and occurrence date.
- `[x]` Ensure modified child transactions do not violate that constraint.
- `[x]` Ensure archived originals still preserve duplicate-prevention semantics.

Recommended invariant:

- one root occurrence row per `(recurring_transaction_id, transaction_date)` where `parent_transaction_id IS NULL`.

Verification:

- `[x]` duplicate materialization tests fail at DB level if code regresses.
- `[x]` modified occurrence tests still succeed with the constraint in place.

## 1.3 Implement canonical visibility-aware repository methods

- `[x]` Replace simple `is_archived = FALSE` transaction reads with visibility-aware queries.
- `[x]` Introduce repository methods for:
  - `[x]` visible transactions by account
  - `[x]` visible transactions by pocket
  - `[x]` visible transactions by recurring template
  - `[x]` visible pending transactions
  - `[x]` visible future transactions by account/pocket/date range

Verification:

- `[x]` repository tests prove hidden originals are excluded.
- `[x]` repository tests prove modified occurrences are included.
- `[x]` repository tests prove archived rows are excluded.

---

# Phase 2 - recurring template model and recurrence engine

## 2.1 Extend recurring template model to full legacy recurrence support

- `[x]` Add `YEARLY` recurrence type support.
- `[x]` Add transient support for `maxRecurrenceCount` normalization on create/update requests.
- `[x]` Keep `finalOccurrenceDate` persisted as the normalized inclusive end date.
- `[x]` Normalize selector lists:
  - `[x]` sorted
  - `[x]` duplicate-free
  - `[x]` null when empty

Verification:

- `[x]` domain tests cover all selector combinations.
- `[x]` request mapping tests prove normalization before persistence.
- `[x]` persistence tests prove normalized storage is stable.

## 2.2 Introduce recurrence domain package in the server

Create a dedicated recurrence domain similar in capability to legacy:

- `[x]` `RecurrencePattern`
- `[x]` `RecurrenceCalculator`
- `[x]` `NoRecurrence`
- `[x]` `DailyRecurrence`
- `[x]` `WeeklyRecurrence`
- `[x]` `MonthlyRecurrence`
- `[x]` `YearlyRecurrence`

Required supported behavior:

- `[x]` skip counts
- `[x]` weekly multi-day patterns
- `[x]` monthly day-of-month selectors
- `[x]` negative day-of-month selectors
- `[x]` week-of-month selectors
- `[x]` negative week-of-month selectors
- `[x]` month restrictions
- `[x]` leap-year clamping for yearly recurrences
- `[x]` max-count normalization to `finalOccurrenceDate`

Verification:

- `[~]` port/adapt the legacy recurrence test matrix to server-side unit tests.
- `[x]` prove deterministic date generation across all recurrence types.

## 2.3 Decide recurring selector storage format

Current server stores selectors as comma-separated strings.

Recommended upgrade:

- `[x]` decide whether to keep CSV or migrate to canonical JSON/text array storage.
- `[x]` prefer a format that is deterministic and easy to normalize.

Verification:

- `[x]` persistence tests prove sorted, duplicate-free storage.
- `[x]` migration tests prove old rows are migrated correctly.

---

# Phase 3 - recurring versioning and update model

## 3.1 Replace `effective_from` semantics with start-date-driven versioning

- `[x]` remove or deprecate `effectiveFromDate` from the recurring update API.
- `[x]` replace `effective_from` update mode with a clearer "new version starting at date X" behavior.
- `[x]` retain `previousVersionId` or equivalent lineage tracking.

Verification:

- `[x]` API tests prove no `effectiveFromDate` is required.
- `[x]` lineage tests prove the new version points to the previous one.

## 3.2 Implement closing of old versions by predecessor occurrence

When a new recurring version starts on date `D`:

- calculate the old version's last occurrence strictly before `D`,
- store that occurrence as the old version's inclusive `finalOccurrenceDate`,
- reject the operation if the resulting version boundary would be invalid.

- `[x]` Implement predecessor-occurrence calculation.
- `[x]` Update version-creation use case to close old versions using that calculation.
- `[x]` Reject overlapping versions in the same version chain.

Verification:

- `[x]` monthly versioning tests prove `2025-06-15` start closes old monthly version at `2025-05-15`.
- `[x]` weekly tests prove correct predecessor selection.
- `[x]` yearly tests prove correct predecessor selection.
- `[x]` overlap tests reject invalid boundaries.

## 3.3 Decide which recurring update modes remain

Current server has:

- `overwrite`
- `parallel`
- `effective_from`

Recommended target:

- keep only modes that are easy to reason about and test.

Suggested direction:

- `[x]` retain `overwrite` only for same-template edits where no version split is needed,
- `[x]` replace `effective_from` with explicit new-version creation using `firstOccurrenceDate`,
- `[x]` keep `parallel` only if there is a real business need for overlapping templates.

Verification:

- `[x]` one documented decision exists.
- `[x]` API tests reflect only supported modes.
- `[x]` unsupported mode tests fail with validation errors.

---

# Phase 4 - recurring materialization service

## 4.1 Build a server-side materializer

Introduce a dedicated service, e.g. `RecurringTransactionMaterializer`, responsible for writing canonical transaction rows.

Responsibilities:

- `[x]` find non-archived recurring templates,
- `[x]` calculate materialization targets,
- `[x]` create missing root occurrence transactions,
- `[x]` skip already materialized occurrences,
- `[x]` skip occurrences already represented by modified chains,
- `[x]` update `lastMaterializedDate`.

Verification:

- `[x]` materializer unit tests cover duplicate skipping.
- `[x]` materializer integration tests verify inserted canonical rows.

## 4.2 Implement future horizon policy

Agreed target behavior:

- always materialize all occurrences needed to cover the **full next calendar month**,
- or at least the **next 5 future occurrences**,
- whichever yields the broader useful future set.

Recommended interpretation:

- materialize enough future occurrences so both conditions are satisfied:
  - coverage through the end of next calendar month,
  - at least 5 future visible occurrences if the recurrence is sparse.

Tasks:

- `[x]` Define the exact horizon algorithm in code and docs.
- `[x]` Implement horizon calculation for all recurrence types.
- `[x]` Ensure yearly/sparse recurrences still produce useful future data.

Verification:

- `[x]` daily tests prove next calendar month is fully covered.
- `[x]` monthly tests prove full next month is covered.
- `[x]` yearly tests prove at least 5 future occurrences are materialized when next month would otherwise be too small.
- `[x]` sparse recurrence tests prove the minimum-occurrence rule works.

## 4.3 Apply weekend handling during materialization

- `[x]` Load account weekend handling for the recurring template scope.
- `[x]` Adjust materialized transaction dates according to `NO_SHIFT`, `MOVE_BEFORE`, `MOVE_AFTER`.
- `[x]` Ensure duplicate checks use the final materialized date consistently.

Verification:

- `[x]` Saturday/Sunday materialization tests for all 3 weekend modes.
- `[x]` duplicate tests for shifted dates.

## 4.4 Materialize transactions with canonical status and linkage

For materialized transactions:

- status must be `PENDING`,
- type must use canonical enum values,
- `recurring_transaction_id` must be populated,
- linkage fields must be correct,
- manual transactions must remain unaffected.

- `[x]` Implement canonical row creation from recurring templates.
- `[x]` Preserve partner/source/destination pocket linkage.
- `[x]` Decide whether description is copied from title or kept separately.

Verification:

- `[x]` field-level integration tests for created rows.
- `[x]` status tests prove materialized rows are always `PENDING`.

## 4.5 Update `lastMaterializedDate`

- `[x]` Update `lastMaterializedDate` to the latest successfully materialized occurrence.
- `[x]` Ensure idempotent reruns do not regress the value.
- `[x]` Ensure archived templates are skipped.

Verification:

- `[x]` tests prove `lastMaterializedDate` advances correctly.
- `[x]` rerun tests prove it does not drift backward.

---

# Phase 5 - modified recurring occurrences

## 5.1 Introduce dedicated use case for modifying a recurring occurrence

Add a dedicated command, e.g. `ModifyRecurringOccurrence`, instead of overloading generic update behavior.

Expected behavior:

1. keep the original root occurrence row,
2. set original `modified_by_id` to the new modification,
3. create a new child transaction row,
4. set child `parent_transaction_id` to the original,
5. copy `recurring_transaction_id` to the child,
6. child becomes the visible transaction.

Tasks:

- `[x]` Add use case and API contract for occurrence modification.
- `[x]` Prevent duplicate modification chains from being created accidentally.
- `[x]` Ensure only recurring-materialized root occurrences can be modified this way.

Verification:

- `[x]` use case tests prove original/child linkage.
- `[x]` visibility tests prove original becomes hidden.
- `[x]` repeat-modification tests prove expected behavior is enforced.

## 5.2 Make materializer aware of modified occurrences

- `[x]` Detect that an occurrence is already represented when a root occurrence exists and/or has a modification chain.
- `[x]` Do not materialize duplicates for dates already covered by an existing root occurrence.

Verification:

- `[x]` materialization tests prove modified occurrences block duplicate rematerialization.

---

# Phase 6 - projection scheduling infrastructure

## 6.1 Introduce dirty-scope tracking for projections

Recommended mechanism:

- a table such as `projection_dirty_scope` with scope type and scope id,
- command-side writes mark affected account/pocket scopes dirty,
- scheduler consumes dirty scopes and rebuilds projections.

Tasks:

- `[x]` Create dirty-scope table.
- `[x]` Mark account scope dirty on transaction create/update/archive/unarchive/materialization.
- `[x]` Mark source and destination pocket scopes dirty when affected.
- `[x]` Mark metadata-related scopes dirty when pocket/partner/account metadata changes.

Verification:

- `[x]` tests prove all relevant write flows enqueue dirty scopes.
- `[x]` duplicate dirty marks are deduplicated safely.

## 6.2 Implement scheduled projection job

- `[x]` Add a scheduled job that processes dirty scopes on a short interval.
- `[x]` Rebuild only affected account/pocket read models.
- `[x]` Make the job idempotent.
- `[x]` Ensure failures leave scopes dirty for retry.

Verification:

- `[x]` scheduler tests prove dirty scopes are consumed.
- `[x]` failure tests prove retry behavior.

## 6.3 Add periodic full rebuild safety job

- `[x]` Add a cron-driven full rebuild job for all transaction-derived projections.
- `[x]` Keep it safe to rerun repeatedly.
- `[x]` Add observability/logging around rebuild duration and counts.

Verification:

- `[x]` deterministic rebuild tests prove repeat rebuilds produce identical results.
- `[x]` integration test proves a full rebuild can recover from intentionally stale/missing feed rows.

---

# Phase 7 - account and pocket transaction read models

## 7.1 Preserve current/historical feeds for `transaction_date <= today`

- `[x]` Keep `account_transaction_feed` for current+historical rows only.
- `[x]` Keep `pocket_transaction_feed` for current+historical rows only.
- `[x]` Make `pocket_transaction_feed` queryable by derived `contract_id` as well as `pocket_id`.
- `[x]` Rebuild these feeds from canonical visible transactions with `transaction_date <= today`.
- `[x]` Derive `contract_id` for pocket feed rows during projection from the pocket relationship.
- `[x]` Keep deterministic `history_position` ordering.
- `[x]` Keep running `balance_after` values.

Verification:

- `[x]` current balances equal latest historical feed row balance.
- `[x]` future canonical transactions do not appear in historical feeds.

## 7.2 Introduce future feed tables

Recommended new read models:

- `[x]` `account_future_transaction_feed`
- `[x]` `pocket_future_transaction_feed`

Suggested fields:

- scope ids (`account_id` / `pocket_id`)
- optional derived `contract_id` on pocket-scoped rows
- `transaction_id`
- `future_position`
- `transaction_date`
- `type`
- `status`
- `description`
- `transaction_amount`
- `signed_amount`
- `projected_balance_after`
- denormalized pocket/partner display fields
- optional `is_future_transaction = TRUE`

Tasks:

- `[x]` create future feed schema,
- `[x]` add repositories,
- `[x]` add projection logic,
- `[x]` derive `contract_id` for pocket-scoped future rows so they can be queried through contract APIs without a separate feed,
- `[x]` add date-range query support,
- `[x]` add pagination support appropriate for future ordering.

Verification:

- `[x]` future feed tests prove only `transaction_date > today` rows are included.
- `[x]` projected balances start from current balance and accumulate future rows correctly.
- `[x]` same-account transfer tests prove account projected balance stays neutral while pocket projections change.

## 7.3 Keep current and future feeds independent

- `[x]` ensure historical feed rebuild does not rely on future feed tables,
- `[x]` ensure future feed rebuild does not mutate `current_balance`,
- `[x]` ensure both feeds can be rebuilt independently or together.

Verification:

- `[x]` tests prove deleting future feed rows and rebuilding them does not affect historical feed rows.
- `[x]` tests prove deleting historical feed rows and rebuilding them does not affect future feed rows.

---

# Phase 8 - summary read models

## 8.1 Update account summary projection

- `[x]` compute `account_query.current_balance` from canonical visible transactions with `transaction_date <= today`.
- `[x]` keep metadata projection behavior.
- `[x]` keep archive-state projection behavior.

Verification:

- `[x]` tests prove future transactions do not alter `account_query.current_balance`.
- `[x]` tests prove metadata changes remain projected correctly.

## 8.2 Update pocket summary projection

- `[x]` compute `pocket_query.current_balance` from canonical visible transactions with `transaction_date <= today`.
- `[x]` keep metadata projection behavior.
- `[x]` keep archive-state projection behavior.

Verification:

- `[x]` tests prove future transactions do not alter `pocket_query.current_balance`.
- `[x]` tests prove metadata changes remain projected correctly.

---

# Phase 9 - API changes

## 9.1 Transaction ingress API

- `[x]` align transaction request/response DTOs with legacy enums.
- `[x]` make pocket references the command-side scope input (`pocketId` for non-transfers, `sourcePocketId` / `destinationPocketId` for transfers).
- `[x]` stop requiring canonical transaction `accountId` / `contractId` on command-side writes when they are derivable from the selected pocket(s).
- `[x]` expose linkage fields where useful:
  - `[x]` `parentTransactionId`
  - `[x]` `recurringTransactionId`
  - `[x]` `modifiedById`
- `[x]` decide whether to expose `transactionOrigin`.

Verification:

- `[x]` API integration tests cover normal, materialized, and modified occurrence rows.

## 9.2 Recurring transaction API

- `[x]` add `YEARLY` recurrence support to request validation.
- `[x]` add transient `maxRecurrenceCount` input support.
- `[x]` remove/deprecate `effectiveFromDate` if versioning is replaced.
- `[x]` expose lineage fields if they remain part of the model.

Verification:

- `[x]` create/update recurring API tests cover all recurrence types.
- `[x]` version-creation tests prove boundary calculation works.

## 9.3 Query API

- `[x]` keep historical feed endpoints:
  - `[x]` `GET /query/accounts/{id}/transactions`
  - `[x]` `GET /query/pockets/{id}/transactions`
- `[x]` add contract-facing historical transaction endpoint backed by `pocket_transaction_feed` filtered by derived `contractId`, e.g.:
  - `[x]` `GET /query/contracts/{id}/transactions`
- `[x]` add future feed endpoints, e.g.:
  - `[x]` `GET /query/accounts/{id}/future-transactions`
  - `[x]` `GET /query/pockets/{id}/future-transactions`
- `[x]` add contract-facing future transaction endpoint backed by pocket future feed filtered by derived `contractId`, e.g.:
  - `[x]` `GET /query/contracts/{id}/future-transactions`
- `[x]` support client-controlled date range / limit for future feeds.

Verification:

- `[x]` API tests prove historical and future feeds are clearly separated.
- `[x]` API tests prove future date range filtering works.

---

# Phase 10 - migration and backfill

## 10.1 Schema migrations

- `[x]` create migrations for enum normalization changes.
- `[x]` create migrations for recurring template schema changes.
- `[x]` create migrations for future feed tables.
- `[x]` create migrations for dirty-scope/projection infrastructure.
- `[x]` create migrations for new indexes/constraints.

Verification:

- `[x]` migration tests run from empty schema.
- `[x]` migration tests run from current repository schema state.

## 10.2 Data backfill

- `[x]` backfill enum values where current strings differ from legacy vocabulary.
- `[x]` backfill recurring template rows to normalized selector storage.
- `[x]` backfill read models from canonical transactions after migration.

Verification:

- `[x]` backfill tests prove canonical transactions project correctly after migration.

---

## Suggested task order

## Step A - semantics and invariants

- `[x]` finalize enum vocabulary
- `[x]` finalize visibility rules
- `[x]` finalize current-balance semantics
- `[x]` remove derived recurring and transaction-side `account_id` / `contract_id` from canonical schema and document projection-side denormalization

## Step B - canonical ledger and recurrence engine

- `[x]` strengthen `transactions` as canonical ledger
- `[x]` add duplicate-prevention constraints
- `[x]` implement recurrence engine with `YEARLY`
- `[x]` implement recurring template normalization

## Step C - recurring generation

- `[x]` implement materializer
- `[x]` implement version-splitting by new `firstOccurrenceDate`
- `[x]` implement modified occurrence use case

## Step D - async projection pipeline

- `[x]` add dirty-scope tracking
- `[x]` add scheduled targeted rebuild job
- `[x]` add full rebuild safety job

## Step E - future read models

- `[x]` rebuild current/historical feeds from canonical `<= today`
- `[x]` add future feed read models
- `[x]` update summary balances
- `[x]` add future feed APIs

## Step F - migration hardening

- `[x]` backfill old data
- `[ ]` run full regression suite
- `[x]` run deterministic rebuild checks

---

## Extensive test plan

The list below is intentionally broad. Each item should become an automated test unless explicitly documented otherwise.

# A. Enum and validation tests

## Transaction enums

- `[x]` reject unknown `TransactionType`
- `[x]` reject unknown `TransactionStatus`
- `[x]` accept `INCOME`
- `[x]` accept `EXPENSE`
- `[x]` accept `TRANSFER`
- `[x]` accept `PENDING`
- `[x]` accept `CLEARED`
- `[x]` accept `RECONCILED`

## Recurrence enums

- `[x]` reject unknown `RecurrenceType`
- `[x]` accept `NONE`
- `[x]` accept `DAILY`
- `[x]` accept `WEEKLY`
- `[x]` accept `MONTHLY`
- `[x]` accept `YEARLY`

## Weekend handling enums

- `[x]` reject unknown `WeekendHandling`
- `[x]` accept `NO_SHIFT`
- `[x]` accept `MOVE_BEFORE`
- `[x]` accept `MOVE_AFTER`

# B. Recurrence engine tests

## Common

- `[x]` negative `skipCount` rejected
- `[x]` `finalOccurrenceDate < firstOccurrenceDate` handled correctly
- `[x]` max-count normalization derives final date correctly
- `[x]` explicit final date overrides max-count input

## NONE

- `[x]` emits only first date
- `[x]` emits nothing if final date is before first date

## DAILY

- `[x]` daily recurrence with `skipCount = 0`
- `[x]` daily recurrence with `skipCount = 1`
- `[x]` daily recurrence honors final date

## WEEKLY

- `[x]` weekly default weekday when `daysOfWeek` absent
- `[x]` weekly multiple weekdays in ascending order
- `[x]` weekly skip whole weeks
- `[x]` weekly first emitted date moves forward to next matching day

## MONTHLY

- `[x]` monthly default day-of-month behavior
- `[x]` monthly `daysOfMonth` precedence
- `[x]` negative `daysOfMonth` support
- `[x]` `weeksOfMonth + daysOfWeek` support
- `[x]` negative `weeksOfMonth` support
- `[x]` month restrictions
- `[x]` clamping to month length

## YEARLY

- `[x]` yearly same-date recurrence
- `[x]` leap-day clamping to Feb 28 in non-leap years
- `[x]` yearly with month restrictions
- `[x]` yearly with days-of-month restrictions
- `[x]` yearly with skip count

# C. Recurring template persistence tests

- `[x]` create recurring template with yearly recurrence
- `[x]` normalize unordered selector lists on save
- `[x]` store null for empty selector lists
- `[x]` persist normalized final date from max-count input
- `[x]` round-trip all recurrence selectors
- `[x]` archived template excluded from materialization queries

# D. Recurring versioning tests

- `[x]` new version starting on monthly occurrence closes old version at prior monthly occurrence
- `[x]` new weekly version closes prior version at correct predecessor weekday
- `[x]` new yearly version closes prior version at correct predecessor occurrence
- `[x]` reject overlapping versions
- `[x]` allow non-overlapping versions in same chain
- `[x]` preserve lineage via `previousVersionId`

# E. Materialization tests

## Canonical creation

- `[x]` materialize daily recurring expense into canonical transaction row
- `[x]` materialize income into canonical transaction row
- `[x]` materialize transfer into canonical transaction row
- `[x]` materialized transaction status is `PENDING`
- `[x]` manual transaction status is not forced to `PENDING`

## Duplicate handling

- `[x]` duplicate run does not create duplicate root occurrences
- `[x]` DB constraint prevents duplicate occurrence rows
- `[x]` modified occurrence blocks duplicate rematerialization

## Horizon logic

- `[x]` full next calendar month is materialized for dense recurrences
- `[x]` at least 5 future occurrences are materialized for sparse recurrences
- `[x]` yearly recurrence gets 5 future occurrences when next month would otherwise add too little
- `[x]` one-off recurrence only materializes once

## Weekend handling

- `[x]` `NO_SHIFT` leaves Saturday unchanged
- `[x]` `MOVE_BEFORE` shifts Saturday to Friday
- `[x]` `MOVE_BEFORE` shifts Sunday to Friday
- `[x]` `MOVE_AFTER` shifts Saturday to Monday
- `[x]` `MOVE_AFTER` shifts Sunday to Monday

## `lastMaterializedDate`

- `[x]` advances after successful materialization
- `[x]` does not advance when nothing new is materialized
- `[x]` remains stable on idempotent reruns

# F. Modified occurrence tests

- `[x]` modifying recurring occurrence creates child transaction
- `[x]` original root gets `modified_by_id`
- `[x]` child gets `parent_transaction_id`
- `[x]` child keeps `recurring_transaction_id`
- `[x]` root becomes invisible in visible queries
- `[x]` child becomes visible in visible queries
- `[x]` future reruns do not recreate original occurrence for same date

# G. Visibility tests

- `[x]` archived transaction excluded from visible queries
- `[x]` hidden original excluded from visible queries
- `[x]` modified child included in visible queries
- `[ ]` split child excluded if split semantics remain supported
- `[x]` root recurring occurrence included when not modified

# H. Historical/current feed projection tests

## Account feed

- `[x]` includes only `transaction_date <= today`
- `[x]` excludes future canonical transactions
- `[x]` orders by deterministic history position descending
- `[x]` running balances are correct after historic inserts
- `[x]` running balances are correct after historic updates
- `[x]` running balances are correct after historic archives
- `[x]` same-account transfer has account `signed_amount = 0`
- `[x]` account latest historical row balance equals `account_query.current_balance`

## Pocket feed

- `[x]` includes only `transaction_date <= today`
- `[x]` excludes future canonical transactions
- `[x]` rows are queryable by derived `contract_id` when the pocket belongs to a contract
- `[x]` running balances are correct for outgoing transactions
- `[x]` running balances are correct for incoming transactions
- `[x]` transfer source row is negative
- `[x]` transfer destination row is positive
- `[x]` latest historical row balance equals `pocket_query.current_balance`

# I. Future feed projection tests

## Account future feed

- `[x]` includes only `transaction_date > today`
- `[x]` future rows are ordered ascending by date/position
- `[x]` projected balances start from current account balance
- `[x]` same-account transfers keep account projected balance neutral
- `[x]` date-range query returns only requested future rows

## Pocket future feed

- `[x]` includes only `transaction_date > today`
- `[x]` rows are queryable by derived `contract_id` when the pocket belongs to a contract
- `[x]` projected balances start from current pocket balance
- `[x]` future transfer source row decreases projected balance
- `[x]` future transfer destination row increases projected balance
- `[x]` date-range query returns only requested future rows

# J. Summary projection tests

- `[x]` account current balance ignores all future canonical rows
- `[x]` pocket current balance ignores all future canonical rows
- `[x]` current balance includes today's rows
- `[x]` current balance excludes tomorrow's rows
- `[x]` metadata projection still updates summaries correctly

# K. Dirty-scope and scheduler tests

- `[x]` create transaction marks account scope dirty
- `[x]` create transaction marks source pocket scope dirty
- `[x]` create transfer marks both pockets dirty
- `[x]` update transaction marks old and new scopes dirty
- `[x]` archive transaction marks affected scopes dirty
- `[x]` materialization marks affected scopes dirty
- `[x]` projection job rebuilds and clears dirty scopes
- `[x]` failed projection leaves dirty scopes for retry
- `[x]` repeated projector runs are idempotent

# L. Full rebuild tests

- `[x]` deleting all feeds and running full rebuild restores historical feeds
- `[x]` deleting all future feeds and running full rebuild restores future feeds
- `[x]` deleting summaries and running full rebuild restores balances
- `[x]` two full rebuilds in a row produce identical rows

# M. API integration tests

## Transactions API

- `[x]` create manual transaction with canonical enums
- `[x]` create non-transfer transaction using `pocketId` only on the command side
- `[x]` create transfer transaction using `sourcePocketId` and `destinationPocketId` only on the command side
- `[x]` reject command-side writes that require canonical transaction `accountId` / `contractId` when those values are derivable from pocket selection
- `[x]` update manual transaction with canonical enums
- `[x]` archive/unarchive manual transaction
- `[x]` modify recurring occurrence through dedicated endpoint/use case

## Recurring API

- `[x]` create yearly recurring transaction
- `[x]` create recurring transaction with max-count normalization
- `[x]` create recurring version with new first occurrence date
- `[x]` archive recurring transaction excludes it from materialization

## Query API

- `[x]` historical account feed excludes future rows
- `[x]` historical pocket feed excludes future rows
- `[x]` historical contract feed is served from `pocket_transaction_feed` filtered by derived `contractId`
- `[x]` future account feed returns future rows only
- `[x]` future pocket feed returns future rows only
- `[x]` future contract feed is served from pocket future feed filtered by derived `contractId`
- `[x]` future feed date range works
- `[x]` current summary balances stay stable while future feed still grows

# N. Regression tests from current server behavior

- `[x]` historical inserts still rewrite account feed positions correctly
- `[x]` historical updates still rewrite pocket feed positions correctly
- `[x]` metadata propagation for partner rename still works
- `[x]` metadata propagation for pocket rename/color still works
- `[x]` rollback tests still prove failed projections do not partially commit query rows

---

## Deliverables checklist

### Architecture and domain

- `[x]` recurring/materialization design documented
- `[x]` canonical enums implemented
- `[x]` visibility semantics centralized

### Persistence

- `[x]` canonical transaction ledger hardened
- `[x]` recurring schema upgraded
- `[x]` future feed tables created
- `[x]` dirty-scope table created

### Use cases and services

- `[x]` recurrence engine implemented
- `[x]` recurring versioning implemented
- `[x]` materializer implemented
- `[x]` modified occurrence use case implemented
- `[x]` async projection scheduler implemented

### Query side

- `[x]` historical feeds rebuilt from canonical ledger
- `[x]` future feeds implemented
- `[x]` current balances exclude future rows

### Quality

- `[x]` migration suite updated
- `[x]` unit tests added
- `[x]` repository tests added
- `[x]` integration tests added
- `[x]` deterministic rebuild tests added
- `[x]` rollback/retry tests added

---

## Recommended first milestone

A good first milestone is:

- `[x]` canonical enums adopted
- `[x]` visibility-aware canonical transaction repository implemented
- `[x]` recurrence engine with `YEARLY` implemented
- `[x]` materializer creates canonical `PENDING` transactions
- `[x]` current balances ignore future rows
- `[x]` future feed tables exist and can be rebuilt from canonical transactions

This milestone would prove the core architecture before finishing every API and migration detail.
