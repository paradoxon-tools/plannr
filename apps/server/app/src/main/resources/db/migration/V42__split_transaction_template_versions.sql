CREATE TABLE transaction_template_versions (
    id BIGSERIAL PRIMARY KEY,
    transaction_template_id BIGINT NOT NULL,
    amount BIGINT NOT NULL,
    first_occurrence_date VARCHAR(32) NOT NULL,
    final_occurrence_date VARCHAR(32) NULL,
    recurrence_type VARCHAR(32) NOT NULL,
    skip_count INTEGER NOT NULL DEFAULT 0,
    days_of_week TEXT NULL,
    weeks_of_month TEXT NULL,
    days_of_month TEXT NULL,
    months_of_year TEXT NULL,
    valid_from VARCHAR(32) NOT NULL,
    valid_until VARCHAR(32) NULL,
    created_at BIGINT NOT NULL,
    CONSTRAINT fk_transaction_template_versions_template
        FOREIGN KEY (transaction_template_id) REFERENCES transaction_templates(id) ON DELETE CASCADE,
    CONSTRAINT uq_transaction_template_versions_start UNIQUE (transaction_template_id, valid_from)
);

INSERT INTO transaction_template_versions (
    id, transaction_template_id, amount, first_occurrence_date, final_occurrence_date,
    recurrence_type, skip_count, days_of_week, weeks_of_month, days_of_month,
    months_of_year, valid_from, valid_until, created_at
)
SELECT
    id, id, amount, first_occurrence_date, final_occurrence_date,
    recurrence_type, skip_count, days_of_week, weeks_of_month, days_of_month,
    months_of_year, first_occurrence_date, NULL, created_at
FROM transaction_templates;

SELECT setval(
    pg_get_serial_sequence('transaction_template_versions', 'id'),
    COALESCE((SELECT MAX(id) FROM transaction_template_versions), 1),
    EXISTS (SELECT 1 FROM transaction_template_versions)
);

ALTER TABLE transaction_materializations
    ADD COLUMN transaction_template_version_id BIGINT NULL;

UPDATE transaction_materializations
SET transaction_template_version_id = transaction_template_id;

ALTER TABLE transaction_materializations
    ALTER COLUMN transaction_template_version_id SET NOT NULL,
    ADD CONSTRAINT fk_transaction_materializations_template_version
        FOREIGN KEY (transaction_template_version_id) REFERENCES transaction_template_versions(id) ON DELETE CASCADE;

ALTER TABLE transaction_materializations
    DROP CONSTRAINT IF EXISTS uq_transaction_materializations_template_date,
    ADD CONSTRAINT uq_transaction_materializations_version_date
        UNIQUE (transaction_template_version_id, transaction_date);

CREATE INDEX idx_transaction_template_versions_template_dates
    ON transaction_template_versions (transaction_template_id, valid_from, valid_until);

ALTER TABLE transaction_templates
    DROP COLUMN amount,
    DROP COLUMN first_occurrence_date,
    DROP COLUMN final_occurrence_date,
    DROP COLUMN recurrence_type,
    DROP COLUMN skip_count,
    DROP COLUMN days_of_week,
    DROP COLUMN weeks_of_month,
    DROP COLUMN days_of_month,
    DROP COLUMN months_of_year,
    DROP COLUMN previous_version_id CASCADE;
