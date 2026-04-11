ALTER TABLE pocket_transaction_feed
    ADD COLUMN contract_id VARCHAR(64) NULL;

CREATE INDEX idx_pocket_transaction_feed_contract_position
    ON pocket_transaction_feed (contract_id, history_position DESC);

CREATE TABLE account_future_transaction_feed (
    account_id VARCHAR(64) NOT NULL,
    transaction_id VARCHAR(64) NOT NULL,
    future_position BIGINT NOT NULL,
    transaction_date VARCHAR(32) NOT NULL,
    type VARCHAR(32) NOT NULL,
    status VARCHAR(32) NOT NULL,
    description TEXT NOT NULL,
    transaction_amount BIGINT NOT NULL,
    signed_amount BIGINT NOT NULL,
    projected_balance_after BIGINT NOT NULL,
    partner_id VARCHAR(64) NULL,
    partner_name VARCHAR(255) NULL,
    source_pocket_id VARCHAR(64) NULL,
    source_pocket_name VARCHAR(255) NULL,
    source_pocket_color INTEGER NULL,
    destination_pocket_id VARCHAR(64) NULL,
    destination_pocket_name VARCHAR(255) NULL,
    destination_pocket_color INTEGER NULL,
    PRIMARY KEY (account_id, transaction_id),
    CONSTRAINT uq_account_future_transaction_feed_position UNIQUE (account_id, future_position)
);

CREATE INDEX idx_account_future_transaction_feed_account_position
    ON account_future_transaction_feed (account_id, future_position ASC);

CREATE TABLE pocket_future_transaction_feed (
    pocket_id VARCHAR(64) NOT NULL,
    account_id VARCHAR(64) NOT NULL,
    contract_id VARCHAR(64) NULL,
    transaction_id VARCHAR(64) NOT NULL,
    future_position BIGINT NOT NULL,
    transaction_date VARCHAR(32) NOT NULL,
    type VARCHAR(32) NOT NULL,
    status VARCHAR(32) NOT NULL,
    description TEXT NOT NULL,
    transaction_amount BIGINT NOT NULL,
    signed_amount BIGINT NOT NULL,
    projected_balance_after BIGINT NOT NULL,
    partner_id VARCHAR(64) NULL,
    partner_name VARCHAR(255) NULL,
    transfer_pocket_id VARCHAR(64) NULL,
    transfer_pocket_name VARCHAR(255) NULL,
    transfer_pocket_color INTEGER NULL,
    PRIMARY KEY (pocket_id, transaction_id),
    CONSTRAINT uq_pocket_future_transaction_feed_position UNIQUE (pocket_id, future_position)
);

CREATE INDEX idx_pocket_future_transaction_feed_pocket_position
    ON pocket_future_transaction_feed (pocket_id, future_position ASC);

CREATE INDEX idx_pocket_future_transaction_feed_contract_position
    ON pocket_future_transaction_feed (contract_id, future_position ASC);
