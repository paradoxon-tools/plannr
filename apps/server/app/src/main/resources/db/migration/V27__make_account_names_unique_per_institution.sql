DROP INDEX IF EXISTS ux_accounts_name;

CREATE UNIQUE INDEX IF NOT EXISTS ux_accounts_institution_name
    ON accounts (institution, name);
