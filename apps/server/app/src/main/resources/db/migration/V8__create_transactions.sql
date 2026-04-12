CREATE TABLE transactions (
    id VARCHAR(64) PRIMARY KEY,
    account_id VARCHAR(64) NOT NULL,
    type VARCHAR(32) NOT NULL,
    status VARCHAR(32) NOT NULL,
    transaction_date VARCHAR(32) NOT NULL,
    amount BIGINT NOT NULL,
    currency_code VARCHAR(16) NOT NULL,
    exchange_rate VARCHAR(64) NULL,
    destination_amount BIGINT NULL,
    description TEXT NOT NULL,
    partner_id VARCHAR(64) NULL,
    source_pocket_id VARCHAR(64) NULL,
    destination_pocket_id VARCHAR(64) NULL,
    parent_transaction_id VARCHAR(64) NULL,
    recurring_transaction_id VARCHAR(64) NULL,
    modified_by_id VARCHAR(64) NULL,
    is_archived BOOLEAN NOT NULL DEFAULT FALSE,
    created_at BIGINT NOT NULL,
    CONSTRAINT fk_transactions_account FOREIGN KEY (account_id) REFERENCES accounts(id) ON DELETE CASCADE,
    CONSTRAINT fk_transactions_currency FOREIGN KEY (currency_code) REFERENCES currencies(code),
    CONSTRAINT fk_transactions_partner FOREIGN KEY (partner_id) REFERENCES partners(id) ON DELETE SET NULL,
    CONSTRAINT fk_transactions_source_pocket FOREIGN KEY (source_pocket_id) REFERENCES pockets(id) ON DELETE SET NULL,
    CONSTRAINT fk_transactions_destination_pocket FOREIGN KEY (destination_pocket_id) REFERENCES pockets(id) ON DELETE SET NULL,
    CONSTRAINT fk_transactions_parent FOREIGN KEY (parent_transaction_id) REFERENCES transactions(id) ON DELETE CASCADE,
    CONSTRAINT fk_transactions_recurring FOREIGN KEY (recurring_transaction_id) REFERENCES recurring_transactions(id) ON DELETE SET NULL,
    CONSTRAINT fk_transactions_modified_by FOREIGN KEY (modified_by_id) REFERENCES transactions(id) ON DELETE SET NULL
);

CREATE INDEX idx_transactions_account_timeline
    ON transactions (account_id, transaction_date, created_at, id);

CREATE INDEX idx_transactions_source_pocket_timeline
    ON transactions (source_pocket_id, transaction_date, created_at, id);

CREATE INDEX idx_transactions_destination_pocket_timeline
    ON transactions (destination_pocket_id, transaction_date, created_at, id);
