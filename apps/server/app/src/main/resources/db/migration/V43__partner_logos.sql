ALTER TABLE partners ADD COLUMN logo_version VARCHAR(64);
CREATE TABLE partner_logos (
    partner_id BIGINT PRIMARY KEY REFERENCES partners(id) ON DELETE CASCADE,
    image BYTEA NOT NULL
);
