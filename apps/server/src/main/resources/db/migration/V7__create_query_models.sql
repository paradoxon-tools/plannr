CREATE TABLE account_query (
    account_id VARCHAR(64) PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    institution VARCHAR(255) NOT NULL,
    currency_code VARCHAR(16) NOT NULL,
    weekend_handling VARCHAR(64) NOT NULL,
    is_archived BOOLEAN NOT NULL DEFAULT FALSE,
    created_at BIGINT NOT NULL,
    current_balance BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT fk_account_query_account FOREIGN KEY (account_id) REFERENCES accounts(id) ON DELETE CASCADE
);

CREATE TABLE pocket_query (
    pocket_id VARCHAR(64) PRIMARY KEY,
    account_id VARCHAR(64) NOT NULL,
    name VARCHAR(255) NOT NULL,
    description TEXT NULL,
    color INTEGER NOT NULL,
    is_default BOOLEAN NOT NULL DEFAULT FALSE,
    is_archived BOOLEAN NOT NULL DEFAULT FALSE,
    created_at BIGINT NOT NULL,
    current_balance BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT fk_pocket_query_pocket FOREIGN KEY (pocket_id) REFERENCES pockets(id) ON DELETE CASCADE,
    CONSTRAINT fk_pocket_query_account FOREIGN KEY (account_id) REFERENCES accounts(id) ON DELETE CASCADE
);

CREATE TABLE account_transaction_feed (
    account_id VARCHAR(64) NOT NULL,
    transaction_id VARCHAR(64) NOT NULL,
    history_position BIGINT NOT NULL,
    transaction_date VARCHAR(32) NOT NULL,
    type VARCHAR(32) NOT NULL,
    status VARCHAR(32) NOT NULL,
    description TEXT NOT NULL,
    transaction_amount BIGINT NOT NULL,
    signed_amount BIGINT NOT NULL,
    balance_after BIGINT NOT NULL,
    partner_id VARCHAR(64) NULL,
    partner_name VARCHAR(255) NULL,
    source_pocket_id VARCHAR(64) NULL,
    source_pocket_name VARCHAR(255) NULL,
    source_pocket_color INTEGER NULL,
    destination_pocket_id VARCHAR(64) NULL,
    destination_pocket_name VARCHAR(255) NULL,
    destination_pocket_color INTEGER NULL,
    is_archived BOOLEAN NOT NULL DEFAULT FALSE,
    PRIMARY KEY (account_id, transaction_id),
    CONSTRAINT uq_account_transaction_feed_position UNIQUE (account_id, history_position),
    CONSTRAINT fk_account_transaction_feed_account FOREIGN KEY (account_id) REFERENCES accounts(id) ON DELETE CASCADE,
    CONSTRAINT fk_account_transaction_feed_partner FOREIGN KEY (partner_id) REFERENCES partners(id) ON DELETE SET NULL,
    CONSTRAINT fk_account_transaction_feed_source_pocket FOREIGN KEY (source_pocket_id) REFERENCES pockets(id) ON DELETE SET NULL,
    CONSTRAINT fk_account_transaction_feed_destination_pocket FOREIGN KEY (destination_pocket_id) REFERENCES pockets(id) ON DELETE SET NULL
);

CREATE INDEX idx_account_transaction_feed_account_position
    ON account_transaction_feed (account_id, history_position DESC);

CREATE TABLE pocket_transaction_feed (
    pocket_id VARCHAR(64) NOT NULL,
    account_id VARCHAR(64) NOT NULL,
    transaction_id VARCHAR(64) NOT NULL,
    history_position BIGINT NOT NULL,
    transaction_date VARCHAR(32) NOT NULL,
    type VARCHAR(32) NOT NULL,
    status VARCHAR(32) NOT NULL,
    description TEXT NOT NULL,
    transaction_amount BIGINT NOT NULL,
    signed_amount BIGINT NOT NULL,
    balance_after BIGINT NOT NULL,
    partner_id VARCHAR(64) NULL,
    partner_name VARCHAR(255) NULL,
    transfer_pocket_id VARCHAR(64) NULL,
    transfer_pocket_name VARCHAR(255) NULL,
    transfer_pocket_color INTEGER NULL,
    is_archived BOOLEAN NOT NULL DEFAULT FALSE,
    PRIMARY KEY (pocket_id, transaction_id),
    CONSTRAINT uq_pocket_transaction_feed_position UNIQUE (pocket_id, history_position),
    CONSTRAINT fk_pocket_transaction_feed_pocket FOREIGN KEY (pocket_id) REFERENCES pockets(id) ON DELETE CASCADE,
    CONSTRAINT fk_pocket_transaction_feed_account FOREIGN KEY (account_id) REFERENCES accounts(id) ON DELETE CASCADE,
    CONSTRAINT fk_pocket_transaction_feed_partner FOREIGN KEY (partner_id) REFERENCES partners(id) ON DELETE SET NULL,
    CONSTRAINT fk_pocket_transaction_feed_transfer_pocket FOREIGN KEY (transfer_pocket_id) REFERENCES pockets(id) ON DELETE SET NULL
);

CREATE INDEX idx_pocket_transaction_feed_pocket_position
    ON pocket_transaction_feed (pocket_id, history_position DESC);

INSERT INTO account_query (
    account_id,
    name,
    institution,
    currency_code,
    weekend_handling,
    is_archived,
    created_at,
    current_balance
)
SELECT
    id,
    name,
    institution,
    currency_code,
    weekend_handling,
    is_archived,
    created_at,
    0
FROM accounts;

INSERT INTO pocket_query (
    pocket_id,
    account_id,
    name,
    description,
    color,
    is_default,
    is_archived,
    created_at,
    current_balance
)
SELECT
    id,
    account_id,
    name,
    description,
    color,
    is_default,
    is_archived,
    created_at,
    0
FROM pockets;
