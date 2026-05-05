CREATE EXTENSION IF NOT EXISTS pgcrypto;

ALTER TABLE pockets
    ALTER COLUMN id SET DEFAULT CONCAT('poc_', REPLACE(gen_random_uuid()::text, '-', ''));
