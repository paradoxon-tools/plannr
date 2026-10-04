# Enable Banking and reconciliation

The server can connect personal bank accounts, store their transactions and balance observations locally, link them to existing planning accounts, and reconcile booked transactions with planned occurrences. This is an account-information integration: it cannot initiate payments.

## Configure and connect

1. Register an application in the [Enable Banking control panel](https://enablebanking.com/sign-in/). Start with a sandbox application. For your own live accounts, follow their production registration and account-whitelisting process.
2. Register the exact callback URL, for example `http://localhost:9000/banking/callback` locally or `https://your-server.example/banking/callback` in deployment.
3. Mount an RSA private key in PKCS#8 PEM format (`BEGIN PRIVATE KEY`) into the server. Set:

   ```bash
   export ENABLE_BANKING_APPLICATION_ID='your-application-id'
   export ENABLE_BANKING_PRIVATE_KEY_PATH='/absolute/path/to/private-key.pem'
   export ENABLE_BANKING_REDIRECT_URL='http://localhost:9000/banking/callback'
   ```

   Convert an existing PKCS#1 key if necessary: `openssl pkcs8 -topk8 -nocrypt -in original.key -out private-key.pem`. Keep the key outside the repository and client applications. No secrets are required to start the server; unconfigured provider operations return `503 banking_not_configured`.
4. Run the server normally. Flyway applies V44 and V45. The existing deployment is a single-user server without application authentication. Protect it with an authenticated reverse proxy/private network before using real bank data. Allow the registered callback through that proxy. The banking APIs share the existing trust boundary; they do not add a multi-user authorization model. Disable query-string logging on the callback at the proxy because it receives a one-use authorization code.
5. Open the **Banking** folder in the server's Bruno collection. Run **List institutions** for the desired country. Use the exact bank name, country and, if needed, one of its supported authentication methods. Select a consent expiry within the bank's advertised maximum; the server accepts at most 180 days.
6. Run **Start connection**. Open `authorizationUrl` in a browser and finish bank consent. The callback returns the connection's status as JSON. An app can poll `GET /banking/connections/{id}` while the browser is open. For a client-owned registered redirect, forward `state` plus `code` (or `error`) to `POST /banking/authorizations/complete`. Callback state expires after 30 minutes and can be consumed only once. A failed/uncertain exchange requires a new connection attempt.
7. Run **List bank accounts** and the existing `GET /accounts`. Select the existing planning account using bank account name, IBAN and currency, then **Link account** with its numeric `accountId`. Linking is explicit; account names alone do not prove identity. Create a planning account using the existing account API if necessary. Each bank account links to at most one planning account, and vice versa; currencies must agree.
8. Run **Sync account** with an inclusive UTC date range. Repeating or overlapping ranges is supported. The response includes the number of received transaction observations and the successful sync time. Bank accounts expose the last successful sync, a safe error code, and the latest balance observations. Importing is allowed before linking.
9. Run **List transactions** (optionally `status=UNMATCHED`), then **Match suggestions**. Confirm a match using the suggested `materializationId`. A differing amount requires `acceptAmountDifference=true`. Use **Ignore transaction** for bank entries that do not belong to the plan; restore with `ignored=false`. Undo a match before correcting it.

Renew consent by starting another connection to the same bank. Matching identification hashes reuse the local bank account and preserve its link and transaction IDs even when the provider's session/account UID changes. Disconnect the old connection when it is no longer needed. Disconnect revokes its provider session and stops future imports, retaining local history and reconciliation. Refresh checks the provider's current consent state; expired or revoked consent requires renewal.

## HTTP API

All routes below are relative to `/banking`. JSON uses camelCase. IDs for local bank accounts, actual transactions, planning accounts and materializations are separate numeric namespaces; connection IDs are UUIDs. Money in banking responses is an exact signed decimal in **major currency units**, with debits negative and credits positive. Existing planning APIs continue to use minor units; reconciliation converts using the planning currency's precision. Times are Unix milliseconds; dates are ISO dates. Provider balance objects retain Enable Banking's field names.

| Method | Route | Purpose/body |
| --- | --- | --- |
| GET | `/institutions?country=DE` | Provider bank catalogue, including bank-specific auth metadata |
| POST | `/connections` | `{bankName,country,validUntil,authMethod?}` → `{connectionId,authorizationUrl}` |
| GET | `/connections` | Local connections and effective consent status |
| GET | `/connections/{id}` | Poll connection status |
| POST | `/connections/{id}/refresh` | Check current provider session status |
| GET | `/callback?state=...&code=...` | Browser callback; accepts `error` instead of `code` |
| POST | `/authorizations/complete` | `{state,code}` or `{state,error}` for a client-owned callback |
| DELETE | `/connections/{id}` | Revoke/disconnect; retain history |
| GET | `/accounts?connectionId=...` | Discovered accounts, planning links, balances and sync status; filter optional |
| PUT | `/accounts/{id}/link` | `{accountId}` links an existing planning account |
| DELETE | `/accounts/{id}/link` | Unlink; undo that account's reconciliations first |
| POST | `/accounts/{id}/sync` | `{dateFrom,dateTo}`; maximum span 366 days |
| GET | `/accounts/{id}/transactions` | Local history; optional `status`, `dateFrom`, `dateTo`, `limit`, `offset` |
| GET | `/transactions/{id}` | One local transaction |
| GET | `/transactions/{id}/suggestions?days=7` | Up to 20 candidates, sorted by amount difference then date distance; days 0–31 |
| PUT | `/transactions/{id}/reconciliation` | `{materializationId,acceptAmountDifference?,note?}` |
| GET | `/transactions/{id}/reconciliation` | Match, planned snapshot, actual amount and signed difference |
| DELETE | `/transactions/{id}/reconciliation` | Undo a match; returns the unmatched transaction |
| PUT | `/transactions/{id}/ignored` | `{ignored:true}` or `{ignored:false}` |
| GET | `/planning-accounts/{id}/transactions?dateFrom=...&dateTo=...` | Materialized planned occurrences with reconciliation state |
| GET | `/planning-accounts/{id}/reconciliations` | Confirmed matches, including snapshots whose plan was later deleted |

List transaction, planned transaction and reconciliation endpoints accept `limit` (default 100, maximum 500) and `offset` (default 0), with stable ordering. Date-filtered history excludes entries with no bank-supplied date; omit the date filters to include these. Fetching candidates examines at most 500 occurrences in the requested window; use the planned transaction endpoint for manual selection in unusually large plans.

Validation errors return 422, unknown resources 404, competing links/matches or inactive consent 409, provider failures 502 and provider rate limiting 429. `409 banking_busy` means another banking mutation is in progress; retry after it completes. A database advisory lock serializes banking writes across server instances, suitable for this personal-account workload. Provider calls time out after 45 seconds; pagination is capped at 100 pages/100,000 rows per sync. No automatic retry of one-use authorization exchanges occurs.

## Data and state model

- `bank_connections` tracks authorization attempts, hashed callback state, session ID, consent expiry and safe error codes. Private signing keys are never stored in the database.
- `bank_accounts` is the durable local bank account identity. `primary_identification_hash` stores the provider's primary account identity. Alternative hashes are fuzzy matches and are never used to merge accounts; session-specific UIDs may change. `account_id` is the explicit mapping to a manually configured `accounts.id`.
- `bank_transactions` stores exact signed amount, original booking status, dates, counterparty, remittance, raw provider JSON and observation timestamps. It does not change planned transactions or account opening balances. Latest bank balance snapshots are stored separately on the bank account.
- `bank_reconciliations` links one actual transaction to one account leg of a planned occurrence, using template-version ID and occurrence date. A snapshot survives template edits, deletion and materialization rebuilds. A transfer can be matched once on each account; transfers between pockets of the same account have no external bank leg. Foreign keys prevent deleting linked planning accounts and silently discarding their attribution; unlink and undo first, or archive instead.

Booking status (`BOOK`, `PDNG`, `HOLD`, `SCHD`, `CNCL`, `RJCT`, `OTHR`) is independent of review state (`UNMATCHED`, `RECONCILED`, `IGNORED`). Only current `BOOK` entries may be reconciled. Planned occurrences expose `PLANNED` or `RECONCILED`. Suggestions never apply automatically. A match records the planned amount at confirmation time and reports the actual-minus-planned difference; it does not rewrite the budget to make the values equal.

Stable entry references are deduplicated per durable bank account, including across renewals. Enable Banking explicitly says `transaction_id` is not a stable unique identifier, so it is never used for deduplication. Without `entry_reference`, a normalized fingerprint plus occurrence number retains identical legitimate transactions instead of collapsing them. These rows expose `identityQuality=FINGERPRINT`: identity across changed bank descriptions/dates cannot be guaranteed. Review them before matching. After a complete successful fetch, missing dated provisional/fingerprint observations in that range become `isCurrent=false`; they remain available in local history. Undated provisional observations cannot be safely expired from a date-filtered fetch and require review. Stable booked history is never deleted just because a bank omits an older entry.

Synchronization fetches every page and balances before publishing changes in one database transaction. Repeat imports preserve review decisions. A provider change to a confirmed transaction's amount, currency or booked state rejects the sync with `reconciled_bank_transaction_changed`; undo that match, sync and review again. Original planned APIs/feeds remain planning projections; consumers must use these banking endpoints to display actual spending and reconciliation rather than summing both feeds together.

This implementation uses explicit on-demand sync. Apps or an authenticated scheduler may call the sync endpoint with an overlapping recent window; bank-specific consent and rate limits still apply. Split/aggregate matches and automatic reconciliation are not implemented: matching is deliberately one actual entry per account occurrence. Future projections must become materialized occurrences before they can be matched. No mobile UI changes are included.

## Verification

After deploying V46, run **Refresh connection**, then **List bank accounts** to recover accounts previously collapsed by shared alternative hashes. Refresh fetches each authorized account's details for active sessions. Existing rows are adopted by current session UID or bank-scoped IBAN, preserving local IDs, planning links and history. No new consent is required. Refreshing an older consent does not move accounts away from their renewed connection.

If transactions were imported while one row represented different pockets, review their attribution before reconciliation. Historical misattribution cannot be inferred safely or automatically reassigned.

```bash
cd apps/server
./gradlew :banking:test :app:compileKotlin

# Disposable PostgreSQL only; each test creates and removes its own random schema.
export BANKING_TEST_R2DBC_URL='r2dbc:postgresql://127.0.0.1:55446/postgres'
export BANKING_TEST_DB_USER='banking_test'
export BANKING_TEST_DB_PASSWORD=''
./gradlew :banking:test -Dtest.profile=integration
```

The integration suite runs the complete Flyway migration chain and real R2DBC transactions with a fake provider. Live-bank consent requires your registered application and browser interaction; it is not performed by automated tests.

Provider contract: [Enable Banking API reference](https://enablebanking.com/docs/api/reference/) and [quick start](https://enablebanking.com/docs/api/quick-start/), checked October 2026.
