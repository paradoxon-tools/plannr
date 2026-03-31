# Database Schema

This document describes the current SQLDelight database schema defined in `app/src/main/sqldelight/de/chennemann/plannr/android/data/`.

## Overview

Plannr stores personal finance data around these core concepts:

- **Currencies** define supported money formats.
- **Accounts** represent real-world financial accounts.
- **Pockets** partition an account into budgeting buckets.
- **Transactions** move money into, out of, and between pockets.
- **Recurring transactions** define templates for future materialized transactions.
- **Partners** represent payers/payees.
- **Contracts** attach structured metadata to a pocket.
- **Tags** categorize transactions.
- **TransactionTag** links tags to transactions.

## Entity Relationship Summary

```text
Currency (1) ─────< Account
Currency (1) ─────< TransactionRecord
Currency (1) ─────< RecurringTransaction

Account (1) ─────< Pocket
Pocket  (1) ─────  Contract   [1:0..1 via unique pocketId]

Partner (1) ─────< Contract
Partner (1) ─────< TransactionRecord
Partner (1) ─────< RecurringTransaction

Pocket (1) ─────< TransactionRecord.sourcePocketId
Pocket (1) ─────< TransactionRecord.destinationPocketId
Pocket (1) ─────< RecurringTransaction.sourcePocketId
Pocket (1) ─────< RecurringTransaction.destinationPocketId

RecurringTransaction (1) ─────< TransactionRecord

TransactionRecord (1) ─────< TransactionRecord   [parentTransactionId for split children]
TransactionRecord (1) ─────< TransactionRecord   [modifiedById for modified occurrences]

TransactionRecord (1) ─────< TransactionTag >───── (1) Tag
```

## Conventions

- Primary keys are stored as `TEXT`.
- Dates are stored as `TEXT`.
- Timestamps such as `createdAt` are stored as `INTEGER`.
- Boolean values are stored as `INTEGER` (`0` = false, `1` = true).
- Monetary amounts are stored as `INTEGER`, representing the smallest unit of the currency.
- Archiving is implemented as soft delete using `isArchived` where present.

---

## Tables

### Currency

Supported currencies and formatting metadata.

| Column | Type | Null | Key | Notes |
|---|---|---:|---|---|
| `code` | TEXT | No | PK | Currency code, e.g. `EUR`, `USD` |
| `name` | TEXT | No |  | Display name |
| `symbol` | TEXT | No |  | Currency symbol |
| `decimalPlaces` | INTEGER | No |  | Number of fraction digits |
| `symbolPosition` | TEXT | No |  | Formatting hint |

**Referenced by:** `Account.currencyCode`, `TransactionRecord.currencyCode`, `RecurringTransaction.currencyCode`

---

### Account

A real-world financial account such as a bank account.

| Column | Type | Null | Key | Notes |
|---|---|---:|---|---|
| `id` | TEXT | No | PK | Account identifier |
| `name` | TEXT | No |  | User-visible account name |
| `institution` | TEXT | No |  | Bank or institution |
| `currencyCode` | TEXT | No | FK | References `Currency(code)` |
| `weekendHandling` | TEXT | No |  | Default behavior for weekend-due scheduling |
| `isArchived` | INTEGER | No |  | Soft archive flag, default `0` |
| `createdAt` | INTEGER | No |  | Creation timestamp |

**Relationships:**
- One account has many pockets.
- Account balance is derived from non-archived pockets and visible top-level transactions.

**Important behavior from queries:**
- `getAll` excludes archived accounts.
- `calculateAccountBalance` sums all non-archived pockets in the account using `TransactionRecord` and ignores split parents by requiring `parentTransactionId IS NULL`.

---

### Pocket

A budgeting bucket inside an account.

| Column | Type | Null | Key | Notes |
|---|---|---:|---|---|
| `id` | TEXT | No | PK | Pocket identifier |
| `accountId` | TEXT | No | FK | References `Account(id)` with `ON DELETE CASCADE` |
| `name` | TEXT | No |  | Pocket name |
| `description` | TEXT | Yes |  | Optional description |
| `color` | INTEGER | No |  | Display color |
| `isDefault` | INTEGER | No |  | Whether this is the account default pocket |
| `isArchived` | INTEGER | No |  | Soft archive flag, default `0` |
| `createdAt` | INTEGER | No |  | Creation timestamp |

**Relationships:**
- Many pockets belong to one account.
- A pocket can be a source or destination for transactions.
- A pocket can optionally have exactly one contract because `Contract.pocketId` is unique.

**Important behavior from queries:**
- `getDefaultPocketForAccount` retrieves the account’s default pocket.
- `calculateBalance` derives balance from incoming and outgoing transactions up to a provided date.
- Deleting an account cascades to its pockets.

---

### Partner

A person or organization associated with a transaction or contract.

| Column | Type | Null | Key | Notes |
|---|---|---:|---|---|
| `id` | TEXT | No | PK | Partner identifier |
| `name` | TEXT | No |  | Partner name |
| `notes` | TEXT | Yes |  | Optional notes |
| `isArchived` | INTEGER | No |  | Soft archive flag, default `0` |
| `createdAt` | INTEGER | No |  | Creation timestamp |

**Relationships:**
- Referenced by `Contract.partnerId`.
- Referenced by `TransactionRecord.partnerId`.
- Referenced by `RecurringTransaction.partnerId`.

**Important behavior from queries:**
- Supports name search and case-insensitive lookup.
- Archived partners are excluded from default listings.

---

### Contract

Structured metadata attached to a pocket, optionally linked to a partner.

| Column | Type | Null | Key | Notes |
|---|---|---:|---|---|
| `id` | TEXT | No | PK | Contract identifier |
| `pocketId` | TEXT | No | Unique, FK | References `Pocket(id)` with `ON DELETE CASCADE` |
| `partnerId` | TEXT | Yes | FK | References `Partner(id)` with `ON DELETE SET NULL` |
| `name` | TEXT | No |  | Contract name |
| `startDate` | TEXT | No |  | Contract start date |
| `notes` | TEXT | Yes |  | Optional notes |
| `isArchived` | INTEGER | No |  | Soft archive flag, default `0` |
| `createdAt` | INTEGER | No |  | Creation timestamp |

**Relationships:**
- One contract belongs to exactly one pocket.
- A pocket can have at most one contract.
- A contract can optionally be associated with one partner.

**Important behavior from queries:**
- Contract detail queries join the contract with its pocket and calculate a current balance for that pocket.
- Non-archived contracts can be fetched by account through the pocket relation.

---

### RecurringTransaction

A template for generating future transactions.

| Column | Type | Null | Key | Notes |
|---|---|---:|---|---|
| `id` | TEXT | No | PK | Template identifier |
| `title` | TEXT | No |  | Display title |
| `description` | TEXT | Yes |  | Optional description |
| `amount` | INTEGER | No |  | Source amount in smallest currency unit |
| `currencyCode` | TEXT | No | FK | References `Currency(code)` |
| `transactionType` | TEXT | No |  | Expense/income/transfer-type enum |
| `partnerId` | TEXT | Yes | FK | References `Partner(id)` with `ON DELETE SET NULL` |
| `sourcePocketId` | TEXT | Yes | FK | References `Pocket(id)` with `ON DELETE SET NULL` |
| `destinationPocketId` | TEXT | Yes | FK | References `Pocket(id)` with `ON DELETE SET NULL` |
| `firstOccurrenceDate` | TEXT | No |  | Start date |
| `finalOccurrenceDate` | TEXT | Yes |  | Optional last date |
| `recurrenceType` | TEXT | No |  | Frequency pattern enum |
| `skipCount` | INTEGER | No |  | Interval/skip configuration, default `0` |
| `daysOfWeek` | TEXT | Yes |  | Serialized recurrence selector |
| `weeksOfMonth` | TEXT | Yes |  | Serialized recurrence selector |
| `daysOfMonth` | TEXT | Yes |  | Serialized recurrence selector |
| `monthsOfYear` | TEXT | Yes |  | Serialized recurrence selector |
| `lastMaterializedDate` | TEXT | Yes |  | Last generated occurrence |
| `isActive` | INTEGER | No |  | Active flag, default `1` |
| `createdAt` | INTEGER | No |  | Creation timestamp |

**Relationships:**
- May reference a partner.
- May reference source and/or destination pockets.
- One recurring transaction can materialize many `TransactionRecord` rows.

**Important behavior from queries:**
- Default listing returns only active templates.
- `getActiveForMaterialization` filters on `isActive = 1` and active date range.
- `updateLastMaterializedDate` tracks generation progress.

---

### TransactionRecord

The central ledger table for all actual transactions.

| Column | Type | Null | Key | Notes |
|---|---|---:|---|---|
| `id` | TEXT | No | PK | Transaction identifier |
| `type` | TEXT | No |  | Transaction type enum |
| `status` | TEXT | No |  | Workflow/status enum |
| `date` | TEXT | No |  | Transaction date |
| `amount` | INTEGER | No |  | Source amount in smallest currency unit |
| `currencyCode` | TEXT | No | FK | References `Currency(code)` |
| `exchangeRate` | TEXT | Yes |  | Exchange rate for cross-currency transfers |
| `destinationAmount` | INTEGER | Yes |  | Amount credited to destination pocket |
| `description` | TEXT | No |  | Description/memo |
| `partnerId` | TEXT | Yes | FK | References `Partner(id)` with `ON DELETE SET NULL` |
| `sourcePocketId` | TEXT | Yes | FK | References `Pocket(id)` with `ON DELETE SET NULL` |
| `destinationPocketId` | TEXT | Yes | FK | References `Pocket(id)` with `ON DELETE SET NULL` |
| `parentTransactionId` | TEXT | Yes | Self FK | References `TransactionRecord(id)` with `ON DELETE CASCADE` |
| `recurringTransactionId` | TEXT | Yes | FK | References `RecurringTransaction(id)` with `ON DELETE SET NULL` |
| `modifiedById` | TEXT | Yes | Self FK | References `TransactionRecord(id)` with `ON DELETE SET NULL` |
| `isArchived` | INTEGER | No |  | Soft archive flag, default `0` |
| `createdAt` | INTEGER | No |  | Creation timestamp |

**Relationships:**
- Can point to a partner.
- Can point to a source pocket and/or destination pocket.
- Can belong to a recurring transaction template.
- Can have child transactions for split transactions via `parentTransactionId`.
- Can be linked to a modifying transaction through `modifiedById`.
- Can have many tags via `TransactionTag`.

**Key modeling concepts:**
- **Expense/income/transfer** are all stored in the same table.
- **Split transactions** use parent/child rows.
- **Recurring materializations** point back to their template via `recurringTransactionId`.
- **Modified occurrences** are represented using `modifiedById`.

**Important behavior from queries:**
- Most list queries return only **visible** transactions:
  - `isArchived = 0`
  - top-level rows where `parentTransactionId IS NULL AND modifiedById IS NULL`, or
  - child rows whose parent has been modified
- `getChildTransactions` returns split children.
- `existsForRecurringOnDate` prevents duplicate materialization.
- `getActionablePending` returns pending transactions in the actionable horizon.
- `getUpcomingByPocket` returns future transactions for a pocket.

---

### Tag

A reusable label for categorizing transactions.

| Column | Type | Null | Key | Notes |
|---|---|---:|---|---|
| `id` | TEXT | No | PK | Tag identifier |
| `name` | TEXT | No | Unique | Unique tag name |
| `color` | TEXT | No |  | Tag color value |
| `createdAt` | INTEGER | No |  | Creation timestamp |

**Relationships:**
- Many-to-many with transactions through `TransactionTag`.

**Important behavior from queries:**
- Supports exact and case-insensitive lookup by name.
- Supports partial name search.

---

### TransactionTag

Join table linking tags to transactions.

| Column | Type | Null | Key | Notes |
|---|---|---:|---|---|
| `transactionId` | TEXT | No | PK, FK | References `TransactionRecord(id)` with `ON DELETE CASCADE` |
| `tagId` | TEXT | No | PK, FK | References `Tag(id)` with `ON DELETE CASCADE` |

**Primary key:** composite key on (`transactionId`, `tagId`)

**Purpose:**
- Allows each transaction to have multiple tags.
- Allows each tag to be assigned to multiple transactions.

**Important behavior from queries:**
- Uses `INSERT OR IGNORE` when adding a tag to avoid duplicates.
- Can fetch all tags for a transaction or all transactions for a tag.

---

## Referential Integrity and Delete Behavior

| From | To | On Delete |
|---|---|---|
| `Account.currencyCode` | `Currency.code` | Default SQLite behavior |
| `Pocket.accountId` | `Account.id` | `CASCADE` |
| `Contract.pocketId` | `Pocket.id` | `CASCADE` |
| `Contract.partnerId` | `Partner.id` | `SET NULL` |
| `RecurringTransaction.currencyCode` | `Currency.code` | Default SQLite behavior |
| `RecurringTransaction.partnerId` | `Partner.id` | `SET NULL` |
| `RecurringTransaction.sourcePocketId` | `Pocket.id` | `SET NULL` |
| `RecurringTransaction.destinationPocketId` | `Pocket.id` | `SET NULL` |
| `TransactionRecord.currencyCode` | `Currency.code` | Default SQLite behavior |
| `TransactionRecord.partnerId` | `Partner.id` | `SET NULL` |
| `TransactionRecord.sourcePocketId` | `Pocket.id` | `SET NULL` |
| `TransactionRecord.destinationPocketId` | `Pocket.id` | `SET NULL` |
| `TransactionRecord.parentTransactionId` | `TransactionRecord.id` | `CASCADE` |
| `TransactionRecord.modifiedById` | `TransactionRecord.id` | `SET NULL` |
| `TransactionRecord.recurringTransactionId` | `RecurringTransaction.id` | `SET NULL` |
| `TransactionTag.transactionId` | `TransactionRecord.id` | `CASCADE` |
| `TransactionTag.tagId` | `Tag.id` | `CASCADE` |

## Derived Concepts in the Schema

### Balance calculation

Balances are not stored directly. They are calculated from `TransactionRecord`:

- Incoming to a pocket: add `destinationAmount` if present, otherwise `amount`
- Outgoing from a pocket: subtract `amount`
- For balance queries, top-level transactions are used by excluding rows with `parentTransactionId` when appropriate

### Visibility rules for transactions

The query layer distinguishes between raw rows and rows visible in the UI:

- Archived transactions are hidden.
- Split children are normally hidden unless their parent has been modified.
- Modified original occurrences are hidden in favor of their replacement representation.

### Archiving

Soft archive flags exist on:

- `Account`
- `Pocket`
- `Partner`
- `Contract`
- `TransactionRecord`

`Currency`, `RecurringTransaction`, `Tag`, and `TransactionTag` do not use `isArchived`; recurring templates instead use `isActive`.
