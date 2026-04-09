# Query Layer Plan

## Status legend

- `[x]` implemented and verified in the codebase
- `[ ]` not implemented yet, only partially scaffolded, or not yet verified by a dedicated test

## Current reality check

This plan is **not fully implemented yet**.

### Verified as implemented

- [x] A separate query layer exists under `src/main/kotlin/de/chennemann/plannr/server/query`
- [x] A synchronous in-process application event bus exists in `common/events/ApplicationEventBus.kt`
- [x] Account, pocket, and partner command use cases publish events inside `@Transactional` use cases
- [x] Query summary tables exist via `V7__create_query_models.sql`
- [x] Read endpoints exist for:
  - [x] `GET /query/accounts/{id}`
  - [x] `GET /query/pockets/{id}`
  - [x] `GET /query/accounts/{id}/transactions`
  - [x] `GET /query/pockets/{id}/transactions`
- [x] Summary projectors exist for `account_query` and `pocket_query`
- [x] Metadata propagation projectors exist for pocket and partner denormalized feed fields
- [x] Integration tests exist for:
  - [x] account query detail
  - [x] pocket query detail
  - [x] account feed pagination
  - [x] pocket feed pagination
  - [x] pocket metadata propagation into feeds
  - [x] partner metadata propagation into feeds

### Verified as not implemented yet

- [x] Transaction command/write handling for materialized transactions in the server app
- [x] Transaction-created / transaction-updated / transaction-archived events
- [x] Automatic account feed row projection from transaction events
- [x] Automatic pocket feed row projection from transaction events
- [x] Running balance recomputation for feeds
- [x] Historical insert/update/archive tail rewrites
- [ ] Full test matrix from this plan

---

## Goal

Design a fast query side for the server that supports:

- [x] account detail by id, including current balance
- [x] pocket detail by id, including current balance
- [x] paginated transaction history for an account in descending order
- [x] paginated transaction history for a pocket in descending order
- [x] historical balance display at any point in the transaction timeline

The query side is intentionally separate from ingress/command handling.

---

## Architectural direction

We will use a CQRS-style split:

- [x] ingress/command side handles writes
- [x] query side serves read-optimized endpoints
- [x] same-transaction projection updates are wired for the implemented account/pocket/partner/transaction flows
- [x] an in-process synchronous event bus exists between command handlers and projectors

### Event flow

1. [x] Command use case writes command-side state.
2. [x] Command use case publishes domain/application events.
3. [x] Query projectors consume those events synchronously.
4. [x] Query models are updated in the same database transaction for the implemented account/pocket/partner/transaction flows.
5. [ ] If projection fails, the whole transaction rolls back is covered by a dedicated verification test.

This gives us:

- [x] separation of concerns
- [x] no eventual consistency lag for transaction-driven feed projection
- [x] a future migration path to outbox/async projection if needed later

---

## Query use cases to support

### 1. Account detail

- [x] Return account details directly from a read model
- [x] Include current balance

### 2. Pocket detail

- [x] Return pocket details directly from a read model
- [x] Include current balance

### 3. Account transaction history

- [x] Return a paginated descending feed of transactions for an account id

### 4. Pocket transaction history

- [x] Return a paginated descending feed of transactions for a pocket id

---

## Core design decisions

### 1. Persist current balances in summary read models

- [x] Store and maintain `account_query.current_balance`
- [x] Store and maintain `pocket_query.current_balance`
- [x] Schema columns exist for both current balances

### 2. Persist running balances on feed rows

- [x] Account feed rows are automatically projected with `balance_after`
- [x] Pocket feed rows are automatically projected with `balance_after`
- [x] Schema columns exist for `balance_after` on both feed tables

### 3. Separate feed tables per read pattern

- [x] Separate `account_transaction_feed` table exists
- [x] Separate `pocket_transaction_feed` table exists

### 4. Denormalize names and colors into feed rows

- [x] Feed schema stores denormalized pocket and partner display fields
- [x] Pocket metadata updates propagate into denormalized feed rows
- [x] Partner metadata updates propagate into denormalized feed rows

### 5. Use `transfer_*` naming

- [x] Pocket feed uses `transfer_pocket_id`
- [x] Pocket feed uses `transfer_pocket_name`
- [x] Pocket feed uses `transfer_pocket_color`

### 6. Use position-based query keys

- [x] Pagination uses `history_position`
- [x] Feeds are queried with `history_position DESC`
- [x] Historical position rewrites are implemented

---

## Query models

## `account_query`

- [x] One row per account model exists
- [x] Repository exists
- [x] Detail endpoint reads from it directly

Suggested/implemented fields:
- [x] `account_id`
- [x] `name`
- [x] `institution`
- [x] `currency_code`
- [x] `is_archived`
- [x] `created_at`
- [x] `current_balance`
- [x] `weekend_handling`

## `pocket_query`

- [x] One row per pocket model exists
- [x] Repository exists
- [x] Detail endpoint reads from it directly

Suggested/implemented fields:
- [x] `pocket_id`
- [x] `account_id`
- [x] `name`
- [x] `description`
- [x] `color`
- [x] `is_default`
- [x] `is_archived`
- [x] `created_at`
- [x] `current_balance`

## `account_transaction_feed`

- [x] Table exists
- [x] Repository exists
- [x] Query endpoint exists
- [ ] Feed rows are created automatically from transaction events

Fields implemented in schema:
- [x] `account_id`
- [x] `transaction_id`
- [x] `history_position`
- [x] `transaction_date`
- [x] `type`
- [x] `status`
- [x] `description`
- [x] `transaction_amount`
- [x] `signed_amount`
- [x] `balance_after`
- [x] `partner_id`
- [x] `partner_name`
- [x] `source_pocket_id`
- [x] `source_pocket_name`
- [x] `source_pocket_color`
- [x] `destination_pocket_id`
- [x] `destination_pocket_name`
- [x] `destination_pocket_color`
- [x] archive/visibility flag

### Amount semantics

- [x] `transaction_amount` is populated automatically from transaction projection
- [x] `signed_amount` is populated automatically for account scope
- [x] `balance_after` is populated automatically for account scope

### Notes

- [x] Same-account transfer rows are automatically projected with `signed_amount = 0`

## `pocket_transaction_feed`

- [x] Table exists
- [x] Repository exists
- [x] Query endpoint exists
- [ ] Feed rows are created automatically from transaction events

Fields implemented in schema:
- [x] `pocket_id`
- [x] `account_id`
- [x] `transaction_id`
- [x] `history_position`
- [x] `transaction_date`
- [x] `type`
- [x] `status`
- [x] `description`
- [x] `transaction_amount`
- [x] `signed_amount`
- [x] `balance_after`
- [x] `partner_id`
- [x] `partner_name`
- [x] `transfer_pocket_id`
- [x] `transfer_pocket_name`
- [x] `transfer_pocket_color`
- [x] archive/visibility flag

### Amount semantics

- [x] `transaction_amount` is populated automatically from transaction projection
- [x] `signed_amount` is populated automatically for pocket scope
- [x] `balance_after` is populated automatically for pocket scope

### Notes

- [x] Transfer rows are automatically fanned out into positive/negative pocket rows

---

## Pagination strategy

- [x] Paginate using `history_position`
- [x] Order feeds by `history_position DESC`
- [x] Support `limit`
- [x] Support `before`

### Request style

- [x] First page style: `?limit=50`
- [x] Next page style: `?limit=50&before=9182`

### Query shape

Account feed:

```sql
SELECT ...
FROM account_transaction_feed
WHERE account_id = :accountId
  AND history_position < :before
ORDER BY history_position DESC
LIMIT :limit
```

- [x] Repository follows this shape

Pocket feed:

```sql
SELECT ...
FROM pocket_transaction_feed
WHERE pocket_id = :pocketId
  AND history_position < :before
ORDER BY history_position DESC
LIMIT :limit
```

- [x] Repository follows this shape

### Why this is acceptable

- [x] Historical transaction mutation logic rewrites later `history_position` values

---

## Historical mutation behavior

When a transaction is inserted, updated, or archived in the middle of history, projectors must:

1. [x] place the row(s) in the correct historical position
2. [x] recompute `balance_after` for all later rows in the affected feed(s)
3. [x] recompute `history_position` for all later rows in the affected feed(s)
4. [x] update the summary balances in `account_query` / `pocket_query`

Affected feeds may include:
- [x] account feed
- [x] source pocket feed
- [x] destination pocket feed

---

## Projection/update strategy

### Summary projectors

Maintain:
- [x] `account_query`
- [x] `pocket_query`

Responsibilities:
- [x] current balances
- [x] metadata projection
- [x] archive state projection

### Feed projectors

Maintain:
- [x] `account_transaction_feed`
- [x] `pocket_transaction_feed`

Responsibilities:
- [x] insert/update/remove feed rows
- [x] recompute running balances
- [x] recompute history positions
- [x] keep latest feed row balance aligned with summary current balance

### Metadata propagation projectors

When metadata changes, update denormalized feed fields.

Examples:
- [x] pocket rename/color change updates feed rows referencing that pocket
- [x] partner rename updates feed rows referencing that partner

---

## Event bus design

We will introduce a synchronous in-process application event bus.

### Event characteristics

- [x] in-process
- [x] synchronous
- [x] same transaction as command handling for implemented account/pocket/partner command flows
- [x] typed handlers by event class

### Event payload guidance

Prefer:
- [x] `TransactionCreated(after)`
- [x] `TransactionUpdated(before, after)`
- [x] `TransactionArchived(before, after)` or equivalent
- [x] `PocketUpdated(before, after)`
- [x] `PartnerUpdated(before, after)`
- [x] `AccountCreated(account)`
- [x] `AccountUpdated(before, after)`
- [x] `PocketCreated(pocket)`
- [x] `PartnerCreated(partner)`

Avoid thin events that only contain ids.

- [x] Implemented events carry rich payloads for the supported flows

---

## Endpoint direction

The query side should expose dedicated read endpoints, separate from ingress endpoints.

Candidate endpoints:
- [x] `GET /query/accounts/{id}`
- [x] `GET /query/pockets/{id}`
- [x] `GET /query/accounts/{id}/transactions`
- [x] `GET /query/pockets/{id}/transactions`

These should read only from query models.

- [x] Account detail endpoint reads from `account_query`
- [x] Pocket detail endpoint reads from `pocket_query`
- [x] Account transactions endpoint reads from `account_transaction_feed`
- [x] Pocket transactions endpoint reads from `pocket_transaction_feed`

---

## Why this design

This design aims to give us:

- [x] very fast account detail reads
- [x] very fast pocket detail reads
- [x] very fast paginated descending transaction feeds
- [x] historical balance display with no runtime recalculation
- [x] self-contained feed rows optimized for UI rendering at the schema/API level
- [x] clear command/query separation
- [x] strong consistency through same-transaction projection updates for implemented account/pocket/partner/transaction flows

---

## Accepted tradeoffs

We explicitly accept:

- [x] denormalization across feed rows
- [x] multiple query rows produced by a single transfer through automatic projection
- [x] bulk feed updates when metadata changes
- [x] tail rewrites for historical transaction mutations

---

## Summary of final decisions

1. [x] Use a separate query layer.
2. [x] Use same-transaction projection updates.
3. [x] Introduce an in-process synchronous event bus between command side and query projectors.
4. [x] Store current balances in `account_query` and `pocket_query` and keep them transaction-projected.
5. [x] Store `balance_after` on every account/pocket feed row through transaction projection.
6. [x] Use separate feed tables for account and pocket history.
7. [x] Denormalize names and colors into feed rows for performance.
8. [x] Use `transfer_*` naming in pocket feed rows.
9. [x] Store `transaction_amount`, `signed_amount`, and `balance_after` on both feed tables through transaction projection.
10. [x] Use position-based pagination via `history_position DESC`.
11. [x] On historical inserts/updates/archives, recompute tail balances and tail positions.

---

## Test coverage plan

The query layer should be covered at multiple levels:

- [x] projector unit tests
- [ ] query repository tests
- [x] event bus / transaction boundary tests
- [x] query API integration tests
- [x] end-to-end command-to-query projection tests for transaction feeds

Below is the minimum set of relevant cases we should cover.

### A. Summary query model tests

#### Account query projection

- [x] creating an account creates one `account_query` row
- [x] updating account metadata updates the corresponding `account_query` row
- [x] archiving an account updates `account_query.is_archived`
- [x] unarchiving an account updates `account_query.is_archived`
- [x] account detail endpoint returns the projected account metadata
- [x] account detail endpoint returns `not_found` for unknown id

#### Pocket query projection

- [x] creating a pocket creates one `pocket_query` row
- [x] updating pocket metadata updates the corresponding `pocket_query` row
- [x] archiving a pocket updates `pocket_query.is_archived`
- [x] unarchiving a pocket updates `pocket_query.is_archived`
- [x] pocket detail endpoint returns the projected pocket metadata
- [x] pocket detail endpoint returns `not_found` for unknown id

#### Summary balances

- [x] new account starts with zero current balance
- [x] new pocket starts with zero current balance
- [x] latest `account_transaction_feed.balance_after` matches `account_query.current_balance`
- [x] latest `pocket_transaction_feed.balance_after` matches `pocket_query.current_balance`
- [x] account current balance remains correct after multiple transactions
- [x] pocket current balance remains correct after multiple transactions

---

### B. Account feed projection tests

#### Basic projection

- [x] expense transaction creates one account feed row
- [x] income transaction creates one account feed row
- [x] transfer transaction creates one account feed row
- [ ] archived/hidden transactions are excluded or marked according to final visibility rules
- [x] account feed endpoint returns rows in descending `history_position`
- [ ] account feed endpoint returns `not_found` for unknown account id if that is the chosen API behavior

#### Amount semantics

- [x] expense produces negative `signed_amount` in account feed
- [x] income produces positive `signed_amount` in account feed
- [x] internal transfer in same account produces `signed_amount = 0` in account feed
- [ ] `transaction_amount` is preserved as the original business amount
- [x] `balance_after` reflects the account balance after the row was applied

#### Running balance correctness

- [x] first account feed row has correct `balance_after`
- [x] later account feed rows accumulate correctly over multiple expenses/incomes
- [x] transfer rows do not incorrectly change account running balance for same-account transfers
- [x] final account feed row balance matches `account_query.current_balance`

#### Denormalized display data

- [x] account feed row contains source pocket name and color at projection time
- [x] account feed row contains destination pocket name and color at projection time
- [ ] account feed row contains partner name at projection time
- [x] pocket rename updates all account feed rows where pocket is source
- [x] pocket rename updates all account feed rows where pocket is destination
- [x] pocket color change updates all account feed rows where pocket is source
- [x] pocket color change updates all account feed rows where pocket is destination
- [x] partner rename updates all account feed rows referencing that partner

---

### C. Pocket feed projection tests

#### Basic projection

- [x] expense transaction creates one pocket feed row for the source pocket
- [x] income transaction creates one pocket feed row for the destination pocket
- [x] transfer transaction creates two pocket feed rows
- [x] pocket feed endpoint returns rows in descending `history_position`
- [ ] pocket feed endpoint returns `not_found` for unknown pocket id if that is the chosen API behavior

#### Amount semantics

- [x] expense source row has negative `signed_amount`
- [x] income destination row has positive `signed_amount`
- [x] transfer source row has negative `signed_amount`
- [x] transfer destination row has positive `signed_amount`
- [ ] `transaction_amount` is preserved on all pocket feed rows
- [x] `balance_after` reflects the pocket balance after the row was applied

#### Running balance correctness

- [ ] first pocket feed row has correct `balance_after`
- [ ] later pocket feed rows accumulate correctly over multiple transactions
- [ ] source pocket running balance is correct across multiple outgoing transactions
- [ ] destination pocket running balance is correct across multiple incoming transactions
- [x] final pocket feed row balance matches `pocket_query.current_balance`

#### Denormalized display data

- [x] transfer row contains `transfer_pocket_id`, `transfer_pocket_name`, and `transfer_pocket_color`
- [ ] partner name is projected onto pocket feed rows
- [x] pocket rename updates all pocket feed rows where pocket appears as `transfer_pocket_*`
- [x] pocket color change updates all pocket feed rows where pocket appears as `transfer_pocket_*`
- [x] partner rename updates all pocket feed rows referencing that partner

---

### D. Pagination tests

#### Basic pagination behavior

- [x] first page returns at most `limit` rows
- [x] second page returns rows with `history_position < before`
- [x] pages do not overlap
- [ ] concatenating all pages yields the same order as a full query
- [x] last page returns fewer than `limit` rows when exhausted
- [x] empty feed returns empty page

#### Ordering guarantees

- [x] account feed is always ordered by `history_position DESC`
- [x] pocket feed is always ordered by `history_position DESC`
- [ ] ties or gaps do not break ordering assumptions
- [x] `history_position` values are unique within a feed scope

#### Cursor behavior under history rewrites

- [x] pagination still works after a historic insert rewrites tail positions
- [x] pagination still works after a historic update rewrites tail positions
- [x] pagination still works after a historic archive rewrites tail positions

---

### E. Historical mutation tests

These are especially important because the whole design depends on tail rewrites.

#### Historic insert

- [x] inserting a transaction in the middle of account history inserts the row at the correct position
- [x] inserting a transaction in the middle of account history recomputes all later account `balance_after` values
- [x] inserting a transaction in the middle of account history recomputes all later account `history_position` values
- [ ] inserting a transaction in the middle of pocket history recomputes all later pocket `balance_after` values
- [ ] inserting a transaction in the middle of pocket history recomputes all later pocket `history_position` values
- [x] latest balances remain correct after a historic insert

#### Historic update

- [x] updating transaction date moves account feed row to the correct position
- [ ] updating transaction date moves pocket feed row(s) to the correct position
- [x] updating transaction amount recomputes all later account balances
- [ ] updating transaction amount recomputes all later pocket balances
- [ ] updating transaction type recomputes affected account and pocket feed rows correctly
- [x] updating source or destination pocket rewrites affected pocket histories correctly
- [x] latest balances remain correct after a historic update

#### Historic archive / unarchive / deletion semantics

- [x] archiving a historical transaction removes or hides the affected account feed row correctly
- [ ] archiving a historical transaction removes or hides affected pocket feed rows correctly
- [x] archiving a historical transaction recomputes later `balance_after` values
- [x] archiving a historical transaction recomputes later `history_position` values
- [x] unarchiving or restoring a historical transaction reinserts it at the correct position
- [x] latest balances remain correct after archive/unarchive behavior

---

### F. Transfer-specific tests

- [x] same-account transfer creates one account row and two pocket rows
- [x] same-account transfer keeps account `signed_amount = 0`
- [x] same-account transfer changes source and destination pocket balances correctly
- [x] same-account transfer projects correct `transfer_pocket_*` values on both pocket rows
- [ ] sequence of transfers between the same two pockets keeps both histories consistent
- [ ] transfer touching archived pockets behaves according to final business rules

---

### G. Metadata propagation tests

#### Pocket metadata changes

- [x] renaming a source pocket updates all account feed rows referencing it as source
- [x] renaming a destination pocket updates all account feed rows referencing it as destination
- [x] renaming a transfer pocket updates all pocket feed rows referencing it as transfer pocket
- [x] changing a pocket color updates all affected account feed rows
- [x] changing a pocket color updates all affected pocket feed rows
- [x] pocket query detail reflects the latest metadata after change

#### Partner metadata changes

- [x] renaming a partner updates all account feed rows referencing that partner
- [x] renaming a partner updates all pocket feed rows referencing that partner

#### Account metadata changes

- [x] changing account metadata updates `account_query`
- [x] account feed remains readable and associated with the correct account after account metadata changes

---

### H. Event bus and transactional consistency tests

- [ ] publishing an event invokes all registered synchronous handlers
- [x] handlers are executed inside the same transaction as the command write
- [x] if a projector fails, command-side writes are rolled back
- [x] if a projector fails, query-side partial writes are rolled back
- [ ] if multiple projectors react to one event, either all succeed or none are committed
- [ ] duplicate event publication is handled according to final idempotency strategy

---

### I. Rebuild / deterministic projection tests

Even if rebuild support is implemented later, the projector logic should be deterministic.

- [ ] replaying the same sequence of events produces the same `account_query` state
- [ ] replaying the same sequence of events produces the same `pocket_query` state
- [ ] replaying the same sequence of events produces the same account feed ordering and balances
- [ ] replaying the same sequence of events produces the same pocket feed ordering and balances

---

### J. Query repository tests

#### Account query repository

- [x] find account by id
- [x] return `null` / not found when missing
- [x] map current balance correctly

#### Pocket query repository

- [x] find pocket by id
- [x] return `null` / not found when missing
- [x] map current balance correctly

#### Account feed repository

- [x] fetch first page in descending order
- [x] fetch next page using `before`
- [x] respect `limit`
- [x] filter by account id only
- [x] map denormalized fields correctly
- [x] map `transaction_amount`, `signed_amount`, and `balance_after` correctly

#### Pocket feed repository

- [x] fetch first page in descending order
- [x] fetch next page using `before`
- [x] respect `limit`
- [x] filter by pocket id only
- [x] map denormalized fields correctly
- [x] map `transaction_amount`, `signed_amount`, and `balance_after` correctly

---

### K. Query API integration tests

#### Account detail API

- [x] returns projected account detail
- [x] returns correct current balance
- [x] returns updated metadata after command-side changes
- [x] returns `404` for unknown id

#### Pocket detail API

- [x] returns projected pocket detail
- [x] returns correct current balance
- [x] returns updated metadata after command-side changes
- [x] returns `404` for unknown id

#### Account transactions API

- [x] returns transactions in descending order
- [x] returns denormalized fields expected by the UI
- [x] returns valid pagination cursor / `before` behavior
- [x] returns correct running balances per row
- [ ] returns correct amounts for expense, income, and transfer rows

#### Pocket transactions API

- [x] returns transactions in descending order
- [x] returns denormalized fields expected by the UI
- [x] returns valid pagination cursor / `before` behavior
- [x] returns correct running balances per row
- [ ] returns correct amounts for expense, income, and transfer rows

---

### L. Edge cases worth covering

- [ ] zero-amount transaction if allowed by business rules
- [x] multiple transactions on the same date
- [ ] multiple historical inserts on the same date
- [x] repeated updates of the same historical transaction
- [ ] renaming a pocket that appears in a large number of rows
- [ ] renaming a partner that appears in a large number of rows
- [ ] transactions at the beginning of history
- [ ] transactions at the end of history
- [ ] account with no pockets in feed yet
- [ ] pocket with no transactions
- [ ] archived account with existing feed data
- [ ] archived pocket with existing feed data

---

### M. High-value invariants to assert everywhere

These invariants should be asserted repeatedly across projector and integration tests:

- [ ] feed rows are strictly ordered by descending `history_position`
- [x] latest feed row `balance_after` equals summary current balance
- [x] `history_position` is unique within a feed scope
- [ ] account feed contains exactly the visible account transactions expected
- [ ] pocket feed contains exactly the visible pocket transactions expected
- [x] denormalized metadata matches the latest projected values after metadata changes
- [x] a same-account transfer creates exactly 1 account feed row and 2 pocket feed rows
