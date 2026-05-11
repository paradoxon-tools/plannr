TRUNCATE TABLE
    recurring_transactions
CASCADE;

ALTER TABLE recurring_transactions
    DROP CONSTRAINT IF EXISTS fk_recurring_transactions_previous_version;

ALTER TABLE recurring_transactions
    ALTER COLUMN id DROP DEFAULT;

CREATE SEQUENCE IF NOT EXISTS recurring_transactions_id_seq AS BIGINT;

ALTER TABLE recurring_transactions
    ALTER COLUMN id TYPE BIGINT USING nextval('recurring_transactions_id_seq'),
    ALTER COLUMN id SET DEFAULT nextval('recurring_transactions_id_seq'),
    ALTER COLUMN previous_version_id TYPE BIGINT USING NULL::BIGINT;

ALTER SEQUENCE recurring_transactions_id_seq OWNED BY recurring_transactions.id;

ALTER TABLE recurring_transactions
    ADD CONSTRAINT fk_recurring_transactions_previous_version
        FOREIGN KEY (previous_version_id) REFERENCES recurring_transactions(id) ON DELETE SET NULL;
