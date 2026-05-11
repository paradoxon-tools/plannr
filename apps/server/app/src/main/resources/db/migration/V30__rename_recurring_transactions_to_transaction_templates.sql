ALTER TABLE recurring_transactions
    DROP CONSTRAINT IF EXISTS fk_recurring_transactions_previous_version;

ALTER SEQUENCE recurring_transactions_id_seq RENAME TO transaction_templates_id_seq;

ALTER TABLE recurring_transactions RENAME TO transaction_templates;

ALTER TABLE transaction_templates
    RENAME CONSTRAINT fk_recurring_transactions_source_pocket TO fk_transaction_templates_source_pocket;
ALTER TABLE transaction_templates
    RENAME CONSTRAINT fk_recurring_transactions_destination_pocket TO fk_transaction_templates_destination_pocket;
ALTER TABLE transaction_templates
    RENAME CONSTRAINT fk_recurring_transactions_partner TO fk_transaction_templates_partner;

ALTER TABLE transaction_templates
    ADD CONSTRAINT fk_transaction_templates_previous_version
        FOREIGN KEY (previous_version_id) REFERENCES transaction_templates(id) ON DELETE SET NULL;
