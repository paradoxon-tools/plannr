CREATE FUNCTION prevent_fallback_financial_profile_deletion()
RETURNS TRIGGER
LANGUAGE plpgsql
AS $$
BEGIN
    RAISE EXCEPTION 'Fallback financial profile cannot be deleted'
        USING ERRCODE = '23514',
              DETAIL = FORMAT('Financial profile id: %s', OLD.id);
END;
$$;

CREATE TRIGGER prevent_fallback_financial_profile_deletion
    BEFORE DELETE ON financial_profiles
    FOR EACH ROW
    WHEN (OLD.is_fallback)
    EXECUTE FUNCTION prevent_fallback_financial_profile_deletion();
