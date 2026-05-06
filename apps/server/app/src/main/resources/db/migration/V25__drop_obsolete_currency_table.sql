ALTER TABLE accounts
    DROP CONSTRAINT IF EXISTS fk_accounts_currency;

ALTER TABLE recurring_transactions
    DROP CONSTRAINT IF EXISTS fk_recurring_transactions_currency;

DROP TABLE IF EXISTS currencies;
