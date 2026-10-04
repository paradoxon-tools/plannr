CREATE TABLE bank_connections (
    id UUID PRIMARY KEY,
    bank_name TEXT NOT NULL,
    country VARCHAR(2) NOT NULL,
    status TEXT NOT NULL CHECK (status IN ('AUTHORIZING', 'ACTIVE', 'FAILED', 'EXPIRED', 'DISCONNECTED')),
    state_hash TEXT UNIQUE,
    state_expires_at BIGINT,
    session_id TEXT,
    valid_until BIGINT,
    created_at BIGINT NOT NULL,
    last_error TEXT
);

CREATE TABLE bank_accounts (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    connection_id UUID NOT NULL REFERENCES bank_connections(id),
    provider_uid TEXT NOT NULL,
    name TEXT NOT NULL,
    iban TEXT,
    currency_code VARCHAR(16) NOT NULL,
    account_id BIGINT UNIQUE REFERENCES accounts(id) ON DELETE RESTRICT,
    balances JSONB NOT NULL DEFAULT '[]',
    last_synced_at BIGINT,
    last_error TEXT
);
CREATE INDEX bank_accounts_connection ON bank_accounts(connection_id);
CREATE TABLE bank_account_identifiers (
    identification_hash TEXT PRIMARY KEY,
    bank_account_id BIGINT NOT NULL REFERENCES bank_accounts(id)
);

CREATE TABLE bank_transactions (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    bank_account_id BIGINT NOT NULL REFERENCES bank_accounts(id),
    import_key TEXT NOT NULL,
    entry_reference TEXT,
    identity_quality TEXT NOT NULL CHECK (identity_quality IN ('STABLE', 'FINGERPRINT')),
    booking_status TEXT NOT NULL,
    amount NUMERIC NOT NULL,
    currency_code VARCHAR(16) NOT NULL,
    booking_date DATE,
    value_date DATE,
    transaction_date DATE,
    counterparty TEXT,
    description TEXT NOT NULL,
    raw_data JSONB NOT NULL,
    ignored BOOLEAN NOT NULL DEFAULT FALSE,
    is_current BOOLEAN NOT NULL DEFAULT TRUE,
    first_seen_at BIGINT NOT NULL,
    last_seen_at BIGINT NOT NULL,
    UNIQUE (bank_account_id, import_key)
);
CREATE INDEX bank_transactions_account_date ON bank_transactions(bank_account_id, booking_date, id);

-- Occurrence identity and a snapshot survive materialization rebuilds and template deletion.
-- A transfer has one reconcilable leg per account. No cascade can erase bank history.
CREATE TABLE bank_reconciliations (
    bank_transaction_id BIGINT PRIMARY KEY REFERENCES bank_transactions(id),
    account_id BIGINT NOT NULL REFERENCES accounts(id) ON DELETE RESTRICT,
    template_version_id BIGINT NOT NULL,
    occurrence_date VARCHAR(32) NOT NULL,
    planned_amount NUMERIC NOT NULL,
    planned_snapshot JSONB NOT NULL,
    note TEXT,
    created_at BIGINT NOT NULL,
    UNIQUE (account_id, template_version_id, occurrence_date)
);
