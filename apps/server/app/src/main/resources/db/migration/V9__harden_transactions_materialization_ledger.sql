ALTER TABLE transactions
    ADD COLUMN transaction_origin VARCHAR(64) NOT NULL DEFAULT 'MANUAL';

CREATE INDEX idx_transactions_recurring_transaction_date
    ON transactions (recurring_transaction_id, transaction_date);

CREATE INDEX idx_transactions_parent_transaction_id
    ON transactions (parent_transaction_id);

CREATE INDEX idx_transactions_modified_by_id
    ON transactions (modified_by_id);

CREATE UNIQUE INDEX uq_transactions_root_recurring_occurrence
    ON transactions (recurring_transaction_id, transaction_date)
    WHERE recurring_transaction_id IS NOT NULL
      AND parent_transaction_id IS NULL;
