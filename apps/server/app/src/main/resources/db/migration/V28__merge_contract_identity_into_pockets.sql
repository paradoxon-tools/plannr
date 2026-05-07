ALTER TABLE contracts
    ADD COLUMN signing_date VARCHAR(32) NULL,
    ADD COLUMN expiration_date VARCHAR(32) NULL,
    ADD COLUMN last_cancellation_date VARCHAR(32) NULL;

UPDATE contracts
SET signing_date = start_date,
    expiration_date = end_date;

DROP INDEX IF EXISTS idx_contracts_account_archived_created_id;

ALTER TABLE contracts
    DROP CONSTRAINT IF EXISTS fk_contracts_account,
    DROP CONSTRAINT IF EXISTS contracts_pkey;

ALTER TABLE contracts
    DROP COLUMN IF EXISTS id,
    DROP COLUMN IF EXISTS account_id,
    DROP COLUMN IF EXISTS name,
    DROP COLUMN IF EXISTS start_date,
    DROP COLUMN IF EXISTS end_date,
    DROP COLUMN IF EXISTS description,
    DROP COLUMN IF EXISTS is_archived,
    DROP COLUMN IF EXISTS created_at;

ALTER TABLE contracts
    ADD PRIMARY KEY (pocket_id);

DROP SEQUENCE IF EXISTS contracts_id_seq;
