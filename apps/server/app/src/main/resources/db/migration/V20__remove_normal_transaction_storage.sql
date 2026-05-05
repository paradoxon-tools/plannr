DROP TABLE IF EXISTS projection_dirty_scope CASCADE;

DROP TABLE IF EXISTS account_future_transaction_feed CASCADE;
DROP TABLE IF EXISTS pocket_future_transaction_feed CASCADE;

DROP TABLE IF EXISTS account_transaction_feed CASCADE;
DROP TABLE IF EXISTS pocket_transaction_feed CASCADE;

DROP TABLE IF EXISTS account_query CASCADE;
DROP TABLE IF EXISTS pocket_query CASCADE;

DROP TABLE IF EXISTS transactions CASCADE;

ALTER TABLE recurring_transactions
    DROP COLUMN IF EXISTS last_materialized_date CASCADE;
