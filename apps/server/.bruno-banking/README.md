# Banking integration collection

Open this directory as a separate collection in Bruno. It contains only connection inspection, refresh, transaction sync and transaction listing. It does not create planning accounts, change links, reconcile transactions, run seeds, or change planned transactions.

Actual bank transactions are persisted by the server in `bank_transactions`, separate from planning materializations. This collection does not create a separate database. `Prod` uses the existing private server hostname; add a different environment if needed. Override `connectionId`, `bankAccountId`, `dateFrom`, `dateTo`, `limit` and `offset` in Bruno or CLI. Run individual requests; do not run the entire collection with unset IDs.

To discover accounts, import each in yearly windows and verify repeated syncs preserve transaction IDs:

```bash
python import_transactions.py --from 2020-01-01 --to 2026-10-06 --expected-accounts 5 --verify-repeat
```

Requires Python 3 and Node/npm. The runner uses Bruno CLI 4.2.1 via npx and imports newest windows first. Add `--connection <uuid>` to target one consent or `--env <name>` for another environment. Consent must already be active. A provider failure stops the run; completed sync windows remain committed. Retry the same command after resolving the failure. Bank availability limits historical coverage; an empty window does not prove that no historical transactions ever existed.

Bruno response reports use temporary directories. Only account IDs and counts are saved in the ignored `reports/import-summary.json`; transaction payloads are retained in the server database. The list request includes all review states and supports pagination.
