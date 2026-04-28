CREATE EXTENSION IF NOT EXISTS pgcrypto;

ALTER TABLE partners
    ALTER COLUMN id SET DEFAULT CONCAT('par_', REPLACE(gen_random_uuid()::text, '-', ''));
