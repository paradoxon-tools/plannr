UPDATE financial_profiles
SET is_fallback = TRUE,
    is_archived = FALSE
WHERE LOWER(BTRIM(name)) = 'unassigned'
  AND NOT EXISTS (
      SELECT 1
      FROM financial_profiles
      WHERE is_fallback = TRUE
  );

INSERT INTO financial_profiles (
    name,
    description,
    is_default,
    is_archived,
    created_at,
    is_fallback
)
SELECT
    'Unassigned',
    'Fallback for records whose financial profile was deleted',
    NOT EXISTS (
        SELECT 1
        FROM financial_profiles
        WHERE is_default = TRUE
    ),
    FALSE,
    CAST(EXTRACT(EPOCH FROM CURRENT_TIMESTAMP) * 1000 AS BIGINT),
    TRUE
WHERE NOT EXISTS (
    SELECT 1
    FROM financial_profiles
    WHERE is_fallback = TRUE
);
