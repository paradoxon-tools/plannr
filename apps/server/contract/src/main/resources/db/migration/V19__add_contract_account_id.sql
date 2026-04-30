ALTER TABLE contracts
    ADD COLUMN account_id VARCHAR(64);

UPDATE contracts c
SET account_id = p.account_id
FROM pockets p
WHERE p.id = c.pocket_id;

ALTER TABLE contracts
    ALTER COLUMN account_id SET NOT NULL;

ALTER TABLE contracts
    ADD CONSTRAINT fk_contracts_account
        FOREIGN KEY (account_id) REFERENCES accounts(id) ON DELETE CASCADE;

CREATE INDEX idx_contracts_account_archived_created_id
    ON contracts(account_id, is_archived, created_at, id);
