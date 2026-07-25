ALTER TABLE account_transaction_feed
    DROP COLUMN financial_profile_kind;

ALTER TABLE pocket_transaction_feed
    DROP COLUMN financial_profile_kind;

ALTER TABLE contract_transaction_feed
    DROP COLUMN financial_profile_kind;

ALTER TABLE financial_profiles
    DROP COLUMN kind;
