ALTER TABLE recurring_transactions DROP CONSTRAINT IF EXISTS fk_recurring_transactions_contract;
ALTER TABLE recurring_transactions DROP CONSTRAINT IF EXISTS fk_recurring_transactions_account;

ALTER TABLE recurring_transactions DROP COLUMN IF EXISTS contract_id CASCADE;
ALTER TABLE recurring_transactions DROP COLUMN IF EXISTS account_id CASCADE;
