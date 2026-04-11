ALTER TABLE transactions
    ADD COLUMN pocket_id VARCHAR(64) NULL;

UPDATE transactions
SET pocket_id = CASE
    WHEN type = 'EXPENSE' THEN source_pocket_id
    WHEN type = 'INCOME' THEN destination_pocket_id
    ELSE NULL
END;

UPDATE transactions
SET source_pocket_id = NULL,
    destination_pocket_id = NULL
WHERE type IN ('EXPENSE', 'INCOME');

ALTER TABLE transactions
    ADD CONSTRAINT fk_transactions_pocket FOREIGN KEY (pocket_id) REFERENCES pockets(id) ON DELETE SET NULL;

DROP INDEX IF EXISTS idx_transactions_account_timeline;

CREATE INDEX idx_transactions_pocket_timeline
    ON transactions (pocket_id, transaction_date, created_at, id);

ALTER TABLE transactions
    DROP CONSTRAINT fk_transactions_account;

ALTER TABLE transactions
    DROP COLUMN account_id;
