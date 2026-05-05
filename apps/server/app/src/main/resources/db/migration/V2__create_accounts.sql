CREATE TABLE accounts (
    id VARCHAR(64) PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    institution VARCHAR(255) NOT NULL,
    currency_code VARCHAR(16) NOT NULL,
    weekend_handling VARCHAR(64) NOT NULL,
    is_archived BOOLEAN NOT NULL DEFAULT FALSE,
    created_at BIGINT NOT NULL,
    CONSTRAINT fk_accounts_currency FOREIGN KEY (currency_code) REFERENCES currencies(code)
);
