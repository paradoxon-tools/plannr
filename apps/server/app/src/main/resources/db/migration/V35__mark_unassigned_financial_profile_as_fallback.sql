ALTER TABLE financial_profiles
    ADD COLUMN is_fallback BOOLEAN NOT NULL DEFAULT FALSE;

INSERT INTO financial_profiles (
    name,
    description,
    kind,
    is_default,
    is_archived,
    created_at,
    is_fallback
)
SELECT
    'Unassigned',
    'Fallback for records whose financial profile was deleted',
    'GROUP',
    FALSE,
    FALSE,
    CAST(EXTRACT(EPOCH FROM CURRENT_TIMESTAMP) * 1000 AS BIGINT),
    TRUE
WHERE NOT EXISTS (
    SELECT 1
    FROM financial_profiles
    WHERE LOWER(BTRIM(name)) = 'unassigned'
);

UPDATE financial_profiles
SET kind = 'GROUP',
    is_archived = FALSE,
    is_fallback = TRUE
WHERE LOWER(BTRIM(name)) = 'unassigned';

CREATE UNIQUE INDEX uq_financial_profiles_single_fallback
    ON financial_profiles (is_fallback)
    WHERE is_fallback = TRUE;
