# Query Layer Plan

## Goal
Design a fast query side for the server that supports:

- account detail by id, including current balance
- pocket detail by id, including current balance
- paginated transaction history for an account in descending order
- paginated transaction history for a pocket in descending order
- historical balance display at any point in the transaction timeline

The query side is intentionally separate from ingress/command handling.

---

## Architectural direction

We will use a CQRS-style split:

- **ingress/command side** handles writes
- **query side** serves read-optimized endpoints

We will keep **same-transaction projection updates** for strong consistency, but introduce an **in-process synchronous event bus** between command handlers and projectors to keep the separation clean.

### Event flow
1. Command use case writes command-side state.
2. Command use case publishes domain/application events.
3. Query projectors consume those events synchronously.
4. Query models are updated in the same database transaction.
5. If projection fails, the whole transaction rolls back.

This gives us:
- separation of concerns
- no eventual consistency lag
- a future migration path to outbox/async projection if needed later

---

## Query use cases to support

### 1. Account detail
Return account details directly from a read model, including:
- account metadata
- current balance

### 2. Pocket detail
Return pocket details directly from a read model, including:
- pocket metadata
- current balance

### 3. Account transaction history
Return a paginated descending feed of transactions for an account id.

### 4. Pocket transaction history
Return a paginated descending feed of transactions for a pocket id.

---

## Core design decisions

### 1. Persist current balances in summary read models
We will store current balances directly in summary query tables:

- `account_query.current_balance`
- `pocket_query.current_balance`

This avoids runtime aggregation for detail endpoints.

### 2. Persist running balances on feed rows
Each feed row will also store the balance **after** that transaction was applied for the relevant scope.

That means:
- account feed rows store `balance_after` for the account
- pocket feed rows store `balance_after` for the pocket

This allows the UI to display changing balances while browsing history.
The latest feed row should always match the current balance of the account/pocket summary model.

### 3. Separate feed tables per read pattern
We will use separate query tables for account and pocket transaction feeds.

A single transfer may therefore fan out into:
- 1 account feed row
- 2 pocket feed rows

This duplication is intentional because these are query models optimized for different access patterns.

### 4. Denormalize names and colors into feed rows
For performance reasons, feed rows should be self-contained and include display data needed by the UI.

Therefore we will denormalize fields such as:
- pocket names
- pocket colors
- partner names

This means metadata changes like pocket rename or color updates must trigger bulk feed updates.

### 5. Use `transfer_*` naming
For pocket feed rows, related pocket information should use `transfer_*` naming rather than `counterpart_*`.

### 6. Use position-based query keys
Pagination will use a position-based query key.

Reasoning:
- historical inserts/updates already require touching all later feed rows because of `balance_after`
- therefore also updating position/query keys for tail rows is acceptable

This gives a very simple descending pagination model.

---

## Query models

## `account_query`
One row per account.

Suggested fields:
- `account_id`
- `name`
- `institution`
- `currency_code`
- `is_archived`
- `created_at`
- `current_balance`
- optional future fields as needed

Purpose:
- serve account detail endpoint directly

---

## `pocket_query`
One row per pocket.

Suggested fields:
- `pocket_id`
- `account_id`
- `name`
- `description`
- `color`
- `is_default`
- `is_archived`
- `created_at`
- `current_balance`

Purpose:
- serve pocket detail endpoint directly

---

## `account_transaction_feed`
One row per transaction visible in account history.

Each row should include at least:
- `account_id`
- `transaction_id`
- `history_position`
- `transaction_date`
- `type`
- `status`
- `description`
- `transaction_amount`
- `signed_amount`
- `balance_after`
- `partner_id`
- `partner_name`
- `source_pocket_id`
- `source_pocket_name`
- `source_pocket_color`
- `destination_pocket_id`
- `destination_pocket_name`
- `destination_pocket_color`
- archive/visibility flags as needed

### Amount semantics
- `transaction_amount`: the original business transaction amount
- `signed_amount`: the effect on the account feed scope
- `balance_after`: account balance after applying this row

### Notes
For transfers within the same account, `signed_amount` will usually be `0`, while the row still appears as account activity.

---

## `pocket_transaction_feed`
One row per transaction visible in a pocket history.

Each row should include at least:
- `pocket_id`
- `account_id`
- `transaction_id`
- `history_position`
- `transaction_date`
- `type`
- `status`
- `description`
- `transaction_amount`
- `signed_amount`
- `balance_after`
- `partner_id`
- `partner_name`
- `transfer_pocket_id`
- `transfer_pocket_name`
- `transfer_pocket_color`
- archive/visibility flags as needed

### Amount semantics
- `transaction_amount`: the original business transaction amount
- `signed_amount`: the effect on this pocket
- `balance_after`: pocket balance after applying this row

### Notes
A transfer usually creates two pocket feed rows:
- one negative row for the source pocket
- one positive row for the destination pocket

---

## Pagination strategy

We will paginate using `history_position`.

### Ordering
Feeds are ordered by:
- `history_position DESC`

### Request style
Example:
- first page: `?limit=50`
- next page: `?limit=50&before=9182`

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

Pocket feed:
```sql
SELECT ...
FROM pocket_transaction_feed
WHERE pocket_id = :pocketId
  AND history_position < :before
ORDER BY history_position DESC
LIMIT :limit
```

### Why this is acceptable
Because historical inserts/updates already require rewriting later rows for running balances, we can also safely rewrite later `history_position` values.

---

## Historical mutation behavior

Historical changes are important because we want correct `balance_after` values.

When a transaction is inserted, updated, or archived in the middle of history, projectors must:

1. place the row(s) in the correct historical position
2. recompute `balance_after` for all later rows in the affected feed(s)
3. recompute `history_position` for all later rows in the affected feed(s)
4. update the summary balances in `account_query` / `pocket_query`

Affected feeds may include:
- account feed
- source pocket feed
- destination pocket feed

depending on the transaction type.

---

## Projection/update strategy

### Summary projectors
Maintain:
- `account_query`
- `pocket_query`

Responsibilities:
- current balances
- metadata projection
- archive state projection

### Feed projectors
Maintain:
- `account_transaction_feed`
- `pocket_transaction_feed`

Responsibilities:
- insert/update/remove feed rows
- recompute running balances
- recompute history positions
- keep latest feed row balance aligned with summary current balance

### Metadata propagation projectors
When metadata changes, update denormalized feed fields.

Examples:
- pocket rename/color change updates all feed rows referencing that pocket
- partner rename updates all feed rows referencing that partner

---

## Event bus design

We will introduce a synchronous in-process application event bus.

### Event characteristics
- in-process
- synchronous
- same transaction as command handling
- typed handlers by event class

### Event payload guidance
Projection events should be rich enough to support deterministic updates.

Prefer:
- `TransactionCreated(after)`
- `TransactionUpdated(before, after)`
- `TransactionArchived(before, after)` or equivalent
- `PocketUpdated(before, after)`
- `PartnerUpdated(before, after)`

Avoid thin events that only contain ids.

---

## Endpoint direction

The query side should expose dedicated read endpoints, separate from ingress endpoints.

Candidate endpoints:
- `GET /query/accounts/{id}`
- `GET /query/pockets/{id}`
- `GET /query/accounts/{id}/transactions` 
- `GET /query/pockets/{id}/transactions`

These should read only from query models.

---

## Why this design

This design gives us:
- very fast account detail reads
- very fast pocket detail reads
- very fast paginated descending transaction feeds
- historical balance display with no runtime recalculation
- self-contained feed rows optimized for UI rendering
- clear command/query separation
- strong consistency through same-transaction projection updates

---

## Accepted tradeoffs

We explicitly accept:
- denormalization across feed rows
- multiple query rows produced by a single transfer
- bulk feed updates when metadata changes
- tail rewrites for historical transaction mutations

These tradeoffs are acceptable because they optimize the specific read paths we care about.

---

## Summary of final decisions

1. Use a separate query layer.
2. Use same-transaction projection updates.
3. Introduce an in-process synchronous event bus between command side and query projectors.
4. Store current balances in `account_query` and `pocket_query`.
5. Store `balance_after` on every account/pocket feed row.
6. Use separate feed tables for account and pocket history.
7. Denormalize names and colors into feed rows for performance.
8. Use `transfer_*` naming in pocket feed rows.
9. Store `transaction_amount`, `signed_amount`, and `balance_after` on both feed tables.
10. Use position-based pagination via `history_position DESC`.
11. On historical inserts/updates/archives, recompute tail balances and tail positions.

---

## Test coverage plan

The query layer should be covered at multiple levels:
- projector unit tests
- query repository tests
- event bus / transaction boundary tests
- query API integration tests
- end-to-end command-to-query projection tests

Below is the minimum set of relevant cases we should cover.

### A. Summary query model tests

#### Account query projection
- creating an account creates one `account_query` row
- updating account metadata updates the corresponding `account_query` row
- archiving an account updates `account_query.is_archived`
- unarchiving an account updates `account_query.is_archived`
- account detail endpoint returns the projected account metadata
- account detail endpoint returns `not_found` for unknown id

#### Pocket query projection
- creating a pocket creates one `pocket_query` row
- updating pocket metadata updates the corresponding `pocket_query` row
- archiving a pocket updates `pocket_query.is_archived`
- unarchiving a pocket updates `pocket_query.is_archived`
- pocket detail endpoint returns the projected pocket metadata
- pocket detail endpoint returns `not_found` for unknown id

#### Summary balances
- new account starts with zero current balance
- new pocket starts with zero current balance
- latest `account_transaction_feed.balance_after` matches `account_query.current_balance`
- latest `pocket_transaction_feed.balance_after` matches `pocket_query.current_balance`
- account current balance remains correct after multiple transactions
- pocket current balance remains correct after multiple transactions

---

### B. Account feed projection tests

#### Basic projection
- expense transaction creates one account feed row
- income transaction creates one account feed row
- transfer transaction creates one account feed row
- archived/hidden transactions are excluded or marked according to final visibility rules
- account feed endpoint returns rows in descending `history_position`
- account feed endpoint returns `not_found` for unknown account id if that is the chosen API behavior

#### Amount semantics
- expense produces negative `signed_amount` in account feed
- income produces positive `signed_amount` in account feed
- internal transfer in same account produces `signed_amount = 0` in account feed
- `transaction_amount` is preserved as the original business amount
- `balance_after` reflects the account balance after the row was applied

#### Running balance correctness
- first account feed row has correct `balance_after`
- later account feed rows accumulate correctly over multiple expenses/incomes
- transfer rows do not incorrectly change account running balance for same-account transfers
- final account feed row balance matches `account_query.current_balance`

#### Denormalized display data
- account feed row contains source pocket name and color at projection time
- account feed row contains destination pocket name and color at projection time
- account feed row contains partner name at projection time
- pocket rename updates all account feed rows where pocket is source
- pocket rename updates all account feed rows where pocket is destination
- pocket color change updates all account feed rows where pocket is source
- pocket color change updates all account feed rows where pocket is destination
- partner rename updates all account feed rows referencing that partner

---

### C. Pocket feed projection tests

#### Basic projection
- expense transaction creates one pocket feed row for the source pocket
- income transaction creates one pocket feed row for the destination pocket
- transfer transaction creates two pocket feed rows
- pocket feed endpoint returns rows in descending `history_position`
- pocket feed endpoint returns `not_found` for unknown pocket id if that is the chosen API behavior

#### Amount semantics
- expense source row has negative `signed_amount`
- income destination row has positive `signed_amount`
- transfer source row has negative `signed_amount`
- transfer destination row has positive `signed_amount`
- `transaction_amount` is preserved on all pocket feed rows
- `balance_after` reflects the pocket balance after the row was applied

#### Running balance correctness
- first pocket feed row has correct `balance_after`
- later pocket feed rows accumulate correctly over multiple transactions
- source pocket running balance is correct across multiple outgoing transactions
- destination pocket running balance is correct across multiple incoming transactions
- final pocket feed row balance matches `pocket_query.current_balance`

#### Denormalized display data
- transfer row contains `transfer_pocket_id`, `transfer_pocket_name`, and `transfer_pocket_color`
- partner name is projected onto pocket feed rows
- pocket rename updates all pocket feed rows where pocket appears as `transfer_pocket_*`
- pocket color change updates all pocket feed rows where pocket appears as `transfer_pocket_*`
- partner rename updates all pocket feed rows referencing that partner

---

### D. Pagination tests

#### Basic pagination behavior
- first page returns at most `limit` rows
- second page returns rows with `history_position < before`
- pages do not overlap
- concatenating all pages yields the same order as a full query
- last page returns fewer than `limit` rows when exhausted
- empty feed returns empty page

#### Ordering guarantees
- account feed is always ordered by `history_position DESC`
- pocket feed is always ordered by `history_position DESC`
- ties or gaps do not break ordering assumptions
- `history_position` values are unique within a feed scope

#### Cursor behavior under history rewrites
- pagination still works after a historic insert rewrites tail positions
- pagination still works after a historic update rewrites tail positions
- pagination still works after a historic archive rewrites tail positions

---

### E. Historical mutation tests

These are especially important because the whole design depends on tail rewrites.

#### Historic insert
- inserting a transaction in the middle of account history inserts the row at the correct position
- inserting a transaction in the middle of account history recomputes all later account `balance_after` values
- inserting a transaction in the middle of account history recomputes all later account `history_position` values
- inserting a transaction in the middle of pocket history recomputes all later pocket `balance_after` values
- inserting a transaction in the middle of pocket history recomputes all later pocket `history_position` values
- latest balances remain correct after a historic insert

#### Historic update
- updating transaction date moves account feed row to the correct position
- updating transaction date moves pocket feed row(s) to the correct position
- updating transaction amount recomputes all later account balances
- updating transaction amount recomputes all later pocket balances
- updating transaction type recomputes affected account and pocket feed rows correctly
- updating source or destination pocket rewrites affected pocket histories correctly
- latest balances remain correct after a historic update

#### Historic archive / unarchive / deletion semantics
- archiving a historical transaction removes or hides the affected account feed row correctly
- archiving a historical transaction removes or hides affected pocket feed rows correctly
- archiving a historical transaction recomputes later `balance_after` values
- archiving a historical transaction recomputes later `history_position` values
- unarchiving or restoring a historical transaction reinserts it at the correct position
- latest balances remain correct after archive/unarchive behavior

---

### F. Transfer-specific tests

- same-account transfer creates one account row and two pocket rows
- same-account transfer keeps account `signed_amount = 0`
- same-account transfer changes source and destination pocket balances correctly
- same-account transfer projects correct `transfer_pocket_*` values on both pocket rows
- sequence of transfers between the same two pockets keeps both histories consistent
- transfer touching archived pockets behaves according to final business rules

---

### G. Metadata propagation tests

#### Pocket metadata changes
- renaming a source pocket updates all account feed rows referencing it as source
- renaming a destination pocket updates all account feed rows referencing it as destination
- renaming a transfer pocket updates all pocket feed rows referencing it as transfer pocket
- changing a pocket color updates all affected account feed rows
- changing a pocket color updates all affected pocket feed rows
- pocket query detail reflects the latest metadata after change

#### Partner metadata changes
- renaming a partner updates all account feed rows referencing that partner
- renaming a partner updates all pocket feed rows referencing that partner

#### Account metadata changes
- changing account metadata updates `account_query`
- account feed remains readable and associated with the correct account after account metadata changes

---

### H. Event bus and transactional consistency tests

- publishing an event invokes all registered synchronous handlers
- handlers are executed inside the same transaction as the command write
- if a projector fails, command-side writes are rolled back
- if a projector fails, query-side partial writes are rolled back
- if multiple projectors react to one event, either all succeed or none are committed
- duplicate event publication is handled according to final idempotency strategy

---

### I. Rebuild / deterministic projection tests

Even if rebuild support is implemented later, the projector logic should be deterministic.

- replaying the same sequence of events produces the same `account_query` state
- replaying the same sequence of events produces the same `pocket_query` state
- replaying the same sequence of events produces the same account feed ordering and balances
- replaying the same sequence of events produces the same pocket feed ordering and balances

---

### J. Query repository tests

#### Account query repository
- find account by id
- return `null` / not found when missing
- map current balance correctly

#### Pocket query repository
- find pocket by id
- return `null` / not found when missing
- map current balance correctly

#### Account feed repository
- fetch first page in descending order
- fetch next page using `before`
- respect `limit`
- filter by account id only
- map denormalized fields correctly
- map `transaction_amount`, `signed_amount`, and `balance_after` correctly

#### Pocket feed repository
- fetch first page in descending order
- fetch next page using `before`
- respect `limit`
- filter by pocket id only
- map denormalized fields correctly
- map `transaction_amount`, `signed_amount`, and `balance_after` correctly

---

### K. Query API integration tests

#### Account detail API
- returns projected account detail
- returns correct current balance
- returns updated metadata after command-side changes
- returns `404` for unknown id

#### Pocket detail API
- returns projected pocket detail
- returns correct current balance
- returns updated metadata after command-side changes
- returns `404` for unknown id

#### Account transactions API
- returns transactions in descending order
- returns denormalized fields expected by the UI
- returns valid pagination cursor / `before` behavior
- returns correct running balances per row
- returns correct amounts for expense, income, and transfer rows

#### Pocket transactions API
- returns transactions in descending order
- returns denormalized fields expected by the UI
- returns valid pagination cursor / `before` behavior
- returns correct running balances per row
- returns correct amounts for expense, income, and transfer rows

---

### L. Edge cases worth covering

- zero-amount transaction if allowed by business rules
- multiple transactions on the same date
- multiple historical inserts on the same date
- repeated updates of the same historical transaction
- renaming a pocket that appears in a large number of rows
- renaming a partner that appears in a large number of rows
- transactions at the beginning of history
- transactions at the end of history
- account with no pockets in feed yet
- pocket with no transactions
- archived account with existing feed data
- archived pocket with existing feed data

---

### M. High-value invariants to assert everywhere

These invariants should be asserted repeatedly across projector and integration tests:

- feed rows are strictly ordered by descending `history_position`
- latest feed row `balance_after` equals summary current balance
- `history_position` is unique within a feed scope
- account feed contains exactly the visible account transactions expected
- pocket feed contains exactly the visible pocket transactions expected
- denormalized metadata matches the latest projected values after metadata changes
- a same-account transfer creates exactly 1 account feed row and 2 pocket feed rows
