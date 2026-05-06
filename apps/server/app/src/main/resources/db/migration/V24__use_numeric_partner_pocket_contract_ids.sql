-- This migration intentionally clears data tied to the prefixed UUID command-side ids
-- before changing partners, pockets, and contracts to generated numeric ids.
TRUNCATE TABLE
    recurring_transactions,
    contracts,
    pockets,
    partners
CASCADE;

ALTER TABLE recurring_transactions
    DROP CONSTRAINT IF EXISTS fk_recurring_transactions_source_pocket,
    DROP CONSTRAINT IF EXISTS fk_recurring_transactions_destination_pocket,
    DROP CONSTRAINT IF EXISTS fk_recurring_transactions_partner;

ALTER TABLE contracts
    DROP CONSTRAINT IF EXISTS fk_contracts_pocket,
    DROP CONSTRAINT IF EXISTS fk_contracts_partner;

ALTER TABLE partners
    ALTER COLUMN id DROP DEFAULT;
ALTER TABLE pockets
    ALTER COLUMN id DROP DEFAULT;
ALTER TABLE contracts
    ALTER COLUMN id DROP DEFAULT;

CREATE SEQUENCE IF NOT EXISTS partners_id_seq AS BIGINT;
CREATE SEQUENCE IF NOT EXISTS pockets_id_seq AS BIGINT;
CREATE SEQUENCE IF NOT EXISTS contracts_id_seq AS BIGINT;

ALTER TABLE partners
    ALTER COLUMN id TYPE BIGINT USING nextval('partners_id_seq'),
    ALTER COLUMN id SET DEFAULT nextval('partners_id_seq');
ALTER SEQUENCE partners_id_seq OWNED BY partners.id;

ALTER TABLE pockets
    ALTER COLUMN id TYPE BIGINT USING nextval('pockets_id_seq'),
    ALTER COLUMN id SET DEFAULT nextval('pockets_id_seq');
ALTER SEQUENCE pockets_id_seq OWNED BY pockets.id;

ALTER TABLE contracts
    ALTER COLUMN id TYPE BIGINT USING nextval('contracts_id_seq'),
    ALTER COLUMN id SET DEFAULT nextval('contracts_id_seq'),
    ALTER COLUMN pocket_id TYPE BIGINT USING NULL::BIGINT,
    ALTER COLUMN partner_id TYPE BIGINT USING NULL::BIGINT;
ALTER SEQUENCE contracts_id_seq OWNED BY contracts.id;

ALTER TABLE recurring_transactions
    ALTER COLUMN source_pocket_id TYPE BIGINT USING NULL::BIGINT,
    ALTER COLUMN destination_pocket_id TYPE BIGINT USING NULL::BIGINT,
    ALTER COLUMN partner_id TYPE BIGINT USING NULL::BIGINT;

ALTER TABLE contracts
    ADD CONSTRAINT fk_contracts_pocket
        FOREIGN KEY (pocket_id) REFERENCES pockets(id) ON DELETE CASCADE,
    ADD CONSTRAINT fk_contracts_partner
        FOREIGN KEY (partner_id) REFERENCES partners(id) ON DELETE SET NULL;

ALTER TABLE recurring_transactions
    ADD CONSTRAINT fk_recurring_transactions_source_pocket
        FOREIGN KEY (source_pocket_id) REFERENCES pockets(id) ON DELETE SET NULL,
    ADD CONSTRAINT fk_recurring_transactions_destination_pocket
        FOREIGN KEY (destination_pocket_id) REFERENCES pockets(id) ON DELETE SET NULL,
    ADD CONSTRAINT fk_recurring_transactions_partner
        FOREIGN KEY (partner_id) REFERENCES partners(id) ON DELETE SET NULL;
