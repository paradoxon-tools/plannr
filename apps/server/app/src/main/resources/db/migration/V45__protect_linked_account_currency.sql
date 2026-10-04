-- Existing account commands must not invalidate the banking currency invariant.
CREATE FUNCTION protect_linked_account_currency() RETURNS trigger LANGUAGE plpgsql AS $$
BEGIN
    IF NEW.currency_code IS DISTINCT FROM OLD.currency_code
       AND EXISTS (SELECT 1 FROM bank_accounts WHERE account_id=OLD.id) THEN
        RAISE EXCEPTION 'Unlink the bank account before changing currency' USING ERRCODE='23514';
    END IF;
    RETURN NEW;
END;
$$;
CREATE TRIGGER accounts_protect_linked_currency BEFORE UPDATE OF currency_code ON accounts
    FOR EACH ROW EXECUTE FUNCTION protect_linked_account_currency();
