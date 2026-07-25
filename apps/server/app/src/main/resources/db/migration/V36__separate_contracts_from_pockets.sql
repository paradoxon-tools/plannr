ALTER TABLE contract_transaction_feed
    DROP CONSTRAINT IF EXISTS fk_contract_transaction_feed_contract;

ALTER TABLE contracts
    DROP CONSTRAINT IF EXISTS fk_contracts_pocket,
    DROP CONSTRAINT IF EXISTS contracts_pkey;

CREATE SEQUENCE contracts_id_seq AS BIGINT;

ALTER TABLE contracts
    ADD COLUMN id BIGINT DEFAULT nextval('contracts_id_seq'),
    ADD COLUMN name VARCHAR(255),
    ADD COLUMN description TEXT,
    ADD COLUMN color INTEGER,
    ADD COLUMN type VARCHAR(32) NOT NULL DEFAULT 'ACCUMULATING',
    ADD COLUMN is_archived BOOLEAN NOT NULL DEFAULT FALSE,
    ADD COLUMN created_at BIGINT;

UPDATE contracts contract
SET name = pocket.name,
    description = pocket.description,
    color = pocket.color,
    is_archived = pocket.is_archived,
    created_at = pocket.created_at
FROM pockets pocket
WHERE pocket.id = contract.pocket_id;

-- Contracts that used an account's shared default pocket cannot retain that pocket
-- as a dedicated reserve. Preserve their attribution and migrate them as cash-flow contracts.
UPDATE contracts contract
SET type = 'NON_ACCUMULATING'
FROM pockets pocket
WHERE pocket.id = contract.pocket_id
  AND pocket.is_default = TRUE;

ALTER TABLE contracts
    ALTER COLUMN id SET NOT NULL,
    ALTER COLUMN name SET NOT NULL,
    ALTER COLUMN color SET NOT NULL,
    ALTER COLUMN created_at SET NOT NULL,
    ADD PRIMARY KEY (id),
    ADD CONSTRAINT ck_contracts_type CHECK (type IN ('ACCUMULATING', 'NON_ACCUMULATING')),
    ADD CONSTRAINT ck_contracts_name_not_blank CHECK (BTRIM(name) <> '');

ALTER SEQUENCE contracts_id_seq OWNED BY contracts.id;

ALTER TABLE pockets
    ADD COLUMN contract_id BIGINT;

UPDATE pockets pocket
SET contract_id = contract.id
FROM contracts contract
WHERE contract.pocket_id = pocket.id
  AND pocket.is_default = FALSE;

ALTER TABLE transaction_templates
    ADD COLUMN contract_id BIGINT;

UPDATE transaction_templates template
SET contract_id = (
    SELECT contract.id
    FROM contracts contract
    WHERE contract.pocket_id = template.source_pocket_id
       OR contract.pocket_id = template.destination_pocket_id
    ORDER BY CASE WHEN contract.pocket_id = template.source_pocket_id THEN 0 ELSE 1 END
    LIMIT 1
);

ALTER TABLE transaction_materializations
    ADD COLUMN contract_id BIGINT;

UPDATE transaction_materializations materialization
SET contract_id = template.contract_id
FROM transaction_templates template
WHERE template.id = materialization.transaction_template_id;

DELETE FROM contract_transaction_feed;

ALTER TABLE contract_transaction_feed
    ADD CONSTRAINT fk_contract_transaction_feed_contract
        FOREIGN KEY (contract_id) REFERENCES contracts(id) ON DELETE CASCADE;

ALTER TABLE pockets
    DROP COLUMN is_contract_pocket,
    ALTER COLUMN name DROP NOT NULL,
    ALTER COLUMN color DROP NOT NULL;

UPDATE pockets
SET name = NULL,
    description = NULL,
    color = NULL
WHERE contract_id IS NOT NULL;

ALTER TABLE pockets
    ADD CONSTRAINT fk_pockets_contract
        FOREIGN KEY (contract_id) REFERENCES contracts(id) ON DELETE RESTRICT,
    ADD CONSTRAINT ck_pockets_presentation_ownership CHECK (
        (
            contract_id IS NULL
            AND name IS NOT NULL
            AND color IS NOT NULL
        )
        OR
        (
            contract_id IS NOT NULL
            AND name IS NULL
            AND description IS NULL
            AND color IS NULL
            AND is_default = FALSE
        )
    );

CREATE UNIQUE INDEX uq_pockets_contract_account
    ON pockets(contract_id, account_id)
    WHERE contract_id IS NOT NULL;

CREATE INDEX idx_pockets_contract
    ON pockets(contract_id);

ALTER TABLE transaction_templates
    ADD CONSTRAINT fk_transaction_templates_contract
        FOREIGN KEY (contract_id) REFERENCES contracts(id) ON DELETE SET NULL;

CREATE INDEX idx_transaction_templates_contract
    ON transaction_templates(contract_id);

ALTER TABLE transaction_materializations
    ADD CONSTRAINT fk_transaction_materializations_contract
        FOREIGN KEY (contract_id) REFERENCES contracts(id) ON DELETE SET NULL;

CREATE INDEX idx_transaction_materializations_contract
    ON transaction_materializations(contract_id);

ALTER TABLE contracts
    DROP COLUMN pocket_id;
