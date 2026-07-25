CREATE FUNCTION prevent_fallback_financial_profile_truncation()
RETURNS TRIGGER
LANGUAGE plpgsql
AS $$
BEGIN
    IF EXISTS (
        SELECT 1
        FROM financial_profiles
        WHERE is_fallback = TRUE
    ) THEN
        RAISE EXCEPTION 'Financial profiles cannot be truncated while the fallback profile exists'
            USING ERRCODE = '23514';
    END IF;

    RETURN NULL;
END;
$$;

CREATE TRIGGER prevent_fallback_financial_profile_truncation
    BEFORE TRUNCATE ON financial_profiles
    FOR EACH STATEMENT
    EXECUTE FUNCTION prevent_fallback_financial_profile_truncation();
