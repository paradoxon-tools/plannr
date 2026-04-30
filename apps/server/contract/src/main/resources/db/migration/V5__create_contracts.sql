CREATE TABLE contracts (
    id VARCHAR(64) PRIMARY KEY,
    pocket_id VARCHAR(64) NOT NULL UNIQUE,
    partner_id VARCHAR(64) NULL,
    name VARCHAR(255) NOT NULL,
    start_date VARCHAR(32) NOT NULL,
    end_date VARCHAR(32) NULL,
    notes TEXT NULL,
    is_archived BOOLEAN NOT NULL DEFAULT FALSE,
    created_at BIGINT NOT NULL,
    CONSTRAINT fk_contracts_pocket FOREIGN KEY (pocket_id) REFERENCES pockets(id) ON DELETE CASCADE,
    CONSTRAINT fk_contracts_partner FOREIGN KEY (partner_id) REFERENCES partners(id) ON DELETE SET NULL
);
