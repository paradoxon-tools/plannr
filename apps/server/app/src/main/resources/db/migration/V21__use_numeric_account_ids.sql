-- This migration intentionally clears existing account-scoped data before
-- changing account ids from prefixed UUID strings to generated numeric ids.
TRUNCATE TABLE
    recurring_transactions,
    contracts,
    pockets,
    accounts
CASCADE;

ALTER TABLE contracts
    DROP CONSTRAINT IF EXISTS fk_contracts_account;
ALTER TABLE pockets
    DROP CONSTRAINT IF EXISTS fk_pockets_account;

ALTER TABLE accounts
    ALTER COLUMN id DROP DEFAULT;

CREATE SEQUENCE IF NOT EXISTS accounts_id_seq AS BIGINT;

ALTER TABLE accounts
    ALTER COLUMN id TYPE BIGINT USING nextval('accounts_id_seq');
ALTER TABLE accounts
    ALTER COLUMN id SET DEFAULT nextval('accounts_id_seq');

ALTER SEQUENCE accounts_id_seq OWNED BY accounts.id;

ALTER TABLE pockets
    ALTER COLUMN account_id TYPE BIGINT USING NULL::BIGINT;
ALTER TABLE contracts
    ALTER COLUMN account_id TYPE BIGINT USING NULL::BIGINT;

ALTER TABLE pockets
    ADD CONSTRAINT fk_pockets_account
        FOREIGN KEY (account_id) REFERENCES accounts(id) ON DELETE CASCADE;
ALTER TABLE contracts
    ADD CONSTRAINT fk_contracts_account
        FOREIGN KEY (account_id) REFERENCES accounts(id) ON DELETE CASCADE;
