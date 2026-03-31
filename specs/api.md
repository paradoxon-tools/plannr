# API Outline

This document defines the baseline HTTP JSON API for the personal finance app, focused on **contracts** and their related resources.

It is intentionally high-level and is meant to align implementation before code is written.

## Status

Draft baseline for v1.

## Design goals

- REST-style JSON over HTTP
- Granular resource endpoints
- Contracts are a user-facing abstraction over one or more recurring transactions
- Recurring transactions can also exist independently of contracts
- Clear domain validation rules
- Consistent error responses and HTTP status codes
- Keep the API simple first; defer response expansion/include optimizations

## Scope

Baseline CRUD is defined for:

- `contracts`
- `recurring-transactions`
- `accounts`
- `pockets`
- `partners`
- `currencies`

## Common conventions

### Media type

- Requests: `application/json`
- Responses: `application/json`

### Date format

All business dates use plain ISO dates:

- `YYYY-MM-DD`

No time-of-day or timezone handling is part of this baseline API.

### Money format

Money is represented as:

- integer amount in the smallest unit of the currency
- explicit currency code

Example:

```json
{
  "amount": 1299,
  "currencyCode": "EUR"
}
```

### IDs

All resource IDs are opaque strings.

### Archiving

Archiving is the baseline lifecycle mechanism for all mutable resources in this API.

- archived resources remain addressable unless otherwise noted
- default list endpoints should exclude archived resources unless explicitly requested
- unarchive is supported where archiving is supported

### Error model

The implementation may use typed domain errors internally, but the HTTP API exposes standard status codes plus a consistent JSON error envelope.

Example:

```json
{
  "error": {
    "code": "pocket_already_has_contract",
    "message": "Pocket already has a contract",
    "details": {
      "pocketId": "poc_123"
    }
  }
}
```

Suggested status code mapping:

- `400 Bad Request` - malformed JSON, invalid query params, invalid request shape
- `404 Not Found` - referenced resource does not exist
- `409 Conflict` - uniqueness violations, invalid state transitions, archive conflicts
- `422 Unprocessable Entity` - structurally valid request but business validation failed

Typical error codes include:

- `validation_error`
- `not_found`
- `conflict`
- `pocket_already_has_contract`
- `scheduled_transaction_not_compatible_with_contract`
- `currency_mismatch`
- `resource_archived`

## Domain model overview

### Contract

A contract is a user-facing abstraction representing an ongoing financial relationship or obligation.

Examples:

- gym membership
- insurance contract
- subscription

A contract:

- belongs to exactly one pocket
- may reference one partner
- may have zero or more recurring transactions
- is not restricted to a single income/expense type at the API level
- may have an optional `endDate`

### Recurring transaction

A recurring transaction defines a recurring transaction pattern.

A recurring transaction:

- may belong to a contract
- may exist without a contract
- may represent expense, income, or transfer
- is archiveable
- supports version/history over time

### Recurring transaction history

History/versioning is part of the baseline design.

A long-running recurring transaction may evolve over time, for example:

- price increase after several years
- recurrence rule change
- partner/account/pocket change

The API should support the following update intents:

- **overwrite the whole series**
- **end current version and create a new version effective from a given date**
- **create a parallel recurring transaction**

The concrete persistence model can be decided during implementation, but the API must preserve this semantic distinction.

## Resource summaries

## Contracts

### Shape

A contract contains at least:

- `id`
- `accountId`
- `pocketId`
- `partnerId` (nullable)
- `name`
- `startDate`
- `endDate` (nullable)
- `notes` (nullable)
- `isArchived`
- `createdAt`

### Endpoints

#### `POST /contracts`
Create a contract.

Notes:
- creates contract metadata only
- does not create recurring transactions in the same request

#### `GET /contracts`
List contracts.

Baseline filtering:
- `accountId`
- optional archived filter

#### `GET /contracts/{contractId}`
Get contract detail.

#### `PUT /contracts/{contractId}`
Replace contract metadata.

Notes:
- updates contract metadata only
- recurring transactions are managed through their own endpoints

#### `POST /contracts/{contractId}/archive`
Archive a contract.

Behavior:
- archiving a contract also archives its recurring transactions

#### `POST /contracts/{contractId}/unarchive`
Unarchive a contract.

Behavior:
- unarchiving policy for recurring transactions should restore previously contract-owned archived recurring transactions; exact implementation detail can be decided later, but the desired API behavior must be documented and consistent

## Recurring transactions

### Shape

A recurring transaction contains at least:

- `id`
- `contractId` (nullable)
- `accountId`
- `sourcePocketId` (nullable)
- `destinationPocketId` (nullable)
- `partnerId` (nullable)
- `title`
- `description` (nullable)
- `amount`
- `currencyCode`
- `transactionType`
- `firstOccurrenceDate`
- `finalOccurrenceDate` (nullable)
- recurrence rule fields
- history/version metadata
- `isArchived`
- `createdAt`

### Recurrence rule support

Baseline API supports the full recurrence model implied by the database draft, including:

- none (one-time transaction)
- daily
- weekly
- monthly
- yearly
- interval/skip-based repetition
- days of week
- weeks of month
- days of month
- months of year

The exact request field names can be finalized during implementation, but the API must support expressing these recurrence patterns.

### Version/history operations

Updates to recurring transactions must support three semantic modes:

1. **overwrite**
   - replaces the whole existing series
   - may require recalculation/rematerialization by implementation

2. **effective_from**
   - ends the previous version
   - creates a new version effective from a specified date
   - preserves history of the series over time

3. **parallel**
   - keeps the existing recurring transaction as-is
   - creates another recurring transaction running alongside it

Because history is baseline behavior, implementation should model lineage/version relationships explicitly.

### Endpoints

#### `POST /recurring-transactions`
Create a recurring transaction.

Notes:
- may be linked to a contract
- may be standalone

#### `GET /recurring-transactions`
List recurring transactions.

Baseline filtering may include:
- `accountId`
- `contractId`
- optional archived filter

#### `GET /recurring-transactions/{recurringTransactionId}`
Get recurring transaction detail.

#### `PUT /recurring-transactions/{recurringTransactionId}`
Update a recurring transaction.

Notes:
- request must specify intended update mode
- baseline modes: `overwrite`, `effective_from`, `parallel`

#### `POST /recurring-transactions/{recurringTransactionId}/archive`
Archive a recurring transaction.

#### `POST /recurring-transactions/{recurringTransactionId}/unarchive`
Unarchive a recurring transaction.

## Accounts

### Shape

An account contains at least:

- `id`
- `name`
- `institution`
- `currencyCode`
- `weekendHandling`
- `isArchived`
- `createdAt`

### Endpoints

#### `POST /accounts`
Create an account.

#### `GET /accounts`
List accounts.

#### `GET /accounts/{accountId}`
Get account detail.

#### `PUT /accounts/{accountId}`
Update an account.

#### `POST /accounts/{accountId}/archive`
Archive an account.

Behavior:
- archive cascades to pockets, contracts, and recurring transactions reachable through that account

#### `POST /accounts/{accountId}/unarchive`
Unarchive an account.

## Pockets

### Shape

A pocket contains at least:

- `id`
- `accountId`
- `name`
- `description` (nullable)
- `color`
- `isDefault`
- `isArchived`
- `createdAt`

### Endpoints

#### `POST /pockets`
Create a pocket.

#### `GET /pockets`
List pockets.

Baseline filtering may include:
- `accountId`
- optional archived filter

#### `GET /pockets/{pocketId}`
Get pocket detail.

#### `PUT /pockets/{pocketId}`
Update a pocket.

#### `POST /pockets/{pocketId}/archive`
Archive a pocket.

Behavior:
- archive cascades to its contract, if present
- archive also cascades to recurring transactions owned through that contract
- direct transaction-side handling can be defined later if needed

#### `POST /pockets/{pocketId}/unarchive`
Unarchive a pocket.

## Partners

### Shape

A partner contains at least:

- `id`
- `name`
- `notes` (nullable)
- `isArchived`
- `createdAt`

### Endpoints

#### `POST /partners`
Create a partner.

#### `GET /partners`
List partners.

#### `GET /partners/{partnerId}`
Get partner detail.

#### `PUT /partners/{partnerId}`
Update a partner.

#### `POST /partners/{partnerId}/archive`
Archive a partner.

#### `POST /partners/{partnerId}/unarchive`
Unarchive a partner.

## Currencies

### Shape

A currency contains at least:

- `code`
- `name`
- `symbol`
- `decimalPlaces`
- `symbolPosition`

### Endpoints

#### `POST /currencies`
Create a currency.

#### `GET /currencies`
List currencies.

#### `GET /currencies/{code}`
Get currency detail.

#### `PUT /currencies/{code}`
Update a currency.

#### `POST /currencies/{code}/archive`
Archive a currency.

#### `POST /currencies/{code}/unarchive`
Unarchive a currency.

Note:
- the current database draft does not yet model currency archiving; implementation may need schema changes or alternative lifecycle handling

## Validation and business rules

The API should enforce the following baseline rules.

### Contract rules

- every contract must reference exactly one pocket
- every contract belongs to exactly one account through its pocket
- a pocket can have at most one contract
- `accountId` in responses is derived from the pocket's account
- a contract may exist without recurring transactions
- contract archive cascades to contract-owned recurring transactions
- `startDate` must be a plain date
- `endDate`, if present, must not be before `startDate`

### Recurring transaction rules

- a recurring transaction may exist without a contract
- a recurring transaction linked to a contract must be compatible with that contract's account/pocket context
- transfer recurring transactions may exist independently of contracts
- if a recurring transaction is attached to a contract, the implementation must define and enforce the allowed compatibility rules between contract pocket and transaction source/destination pockets
- all money amounts are integer minor units paired with `currencyCode`
- recurrence rules must be internally consistent
- history/version updates must preserve lineage semantics for `effective_from`

### Account and pocket rules

- pocket belongs to exactly one account
- account archive cascades to child pockets, contracts, and recurring transactions
- pocket archive cascades to child contract and related recurring transactions
- if `isDefault` is enforced as unique per account, that should be validated at the API boundary

### Partner rules

- archived partners are excluded from default list responses
- partner references from contracts or recurring transactions must be validated if supplied

### Currency rules

- currency code is the canonical identifier
- decimal places define how integer amounts are interpreted
- if currency archiving is supported, implementation must define behavior for resources already referencing that currency

## Default list behavior

Unless specified otherwise:

- archived resources are excluded by default
- clients may request archived resources explicitly via query parameter

## Deferred topics

The following are intentionally out of scope for this baseline document and can be added later:

- occurrence preview endpoints
- materialized transaction endpoints
- dynamic include/expansion of related resources
- pagination details
- sorting details beyond basic list support
- authentication and user ownership
- idempotency semantics
- optimistic locking/concurrency control
- exact request/response JSON examples for every endpoint

## Open implementation notes

- The current database draft separates `Contract` and `RecurringTransaction`; implementation will likely need schema evolution to support a contract-to-many recurring transaction relation and explicit recurring transaction history/versioning.
- The current database draft uses `isActive` for recurring transactions; the API baseline standardizes on `isArchived`, so persistence and query logic should be aligned accordingly.
- The current database draft does not model archived currencies; if currency CRUD with archiving is implemented, the schema will need to support it.
- The API should keep the service/domain layer explicit about domain failures while still returning proper HTTP status codes and structured error responses.
