ALTER TABLE partners
    RENAME COLUMN notes TO description;

ALTER TABLE contracts
    RENAME COLUMN notes TO description;
