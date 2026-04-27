CREATE EXTENSION IF NOT EXISTS pgcrypto;

ALTER TABLE contracts
    ALTER COLUMN id SET DEFAULT CONCAT('con_', REPLACE(gen_random_uuid()::text, '-', ''));

ALTER TABLE transactions
    ALTER COLUMN id SET DEFAULT CONCAT('txn_', REPLACE(gen_random_uuid()::text, '-', ''));

ALTER TABLE recurring_transactions
    ALTER COLUMN id SET DEFAULT CONCAT('rtx_', REPLACE(gen_random_uuid()::text, '-', ''));
