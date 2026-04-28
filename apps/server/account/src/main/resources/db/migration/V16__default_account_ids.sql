CREATE EXTENSION IF NOT EXISTS pgcrypto;

ALTER TABLE accounts
    ALTER COLUMN id SET DEFAULT CONCAT('acc_', REPLACE(gen_random_uuid()::text, '-', ''));
