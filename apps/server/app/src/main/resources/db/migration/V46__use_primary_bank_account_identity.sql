-- Alternative identification hashes are fuzzy matches, not unique account identities.
-- Existing rows are adopted by their current provider UID or bank-scoped IBAN on refresh.
ALTER TABLE bank_accounts ADD COLUMN primary_identification_hash TEXT UNIQUE;
DROP TABLE bank_account_identifiers;
