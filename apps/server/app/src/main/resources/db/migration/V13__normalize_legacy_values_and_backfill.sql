UPDATE accounts
SET weekend_handling = CASE UPPER(weekend_handling)
    WHEN 'SAME_DAY' THEN 'NO_SHIFT'
    WHEN 'PREV_BUSINESS_DAY' THEN 'MOVE_BEFORE'
    WHEN 'NEXT_BUSINESS_DAY' THEN 'MOVE_AFTER'
    ELSE UPPER(weekend_handling)
END;

UPDATE account_query
SET weekend_handling = CASE UPPER(weekend_handling)
    WHEN 'SAME_DAY' THEN 'NO_SHIFT'
    WHEN 'PREV_BUSINESS_DAY' THEN 'MOVE_BEFORE'
    WHEN 'NEXT_BUSINESS_DAY' THEN 'MOVE_AFTER'
    ELSE UPPER(weekend_handling)
END;

UPDATE transactions
SET type = CASE UPPER(type)
    WHEN 'EXPENSE' THEN 'EXPENSE'
    WHEN 'INCOME' THEN 'INCOME'
    WHEN 'TRANSFER' THEN 'TRANSFER'
    ELSE UPPER(type)
END,
    status = CASE UPPER(status)
        WHEN 'BOOKED' THEN 'CLEARED'
        WHEN 'PENDING' THEN 'PENDING'
        WHEN 'RECONCILED' THEN 'RECONCILED'
        WHEN 'CLEARED' THEN 'CLEARED'
        ELSE UPPER(status)
    END;

UPDATE recurring_transactions
SET transaction_type = CASE UPPER(transaction_type)
    WHEN 'EXPENSE' THEN 'EXPENSE'
    WHEN 'INCOME' THEN 'INCOME'
    WHEN 'TRANSFER' THEN 'TRANSFER'
    ELSE UPPER(transaction_type)
END,
    recurrence_type = CASE UPPER(recurrence_type)
        WHEN 'NONE' THEN 'NONE'
        WHEN 'DAILY' THEN 'DAILY'
        WHEN 'WEEKLY' THEN 'WEEKLY'
        WHEN 'MONTHLY' THEN 'MONTHLY'
        WHEN 'YEARLY' THEN 'YEARLY'
        ELSE UPPER(recurrence_type)
    END,
    days_of_week = NULLIF((
        SELECT string_agg(normalized_value, ',' ORDER BY normalized_value)
        FROM (
            SELECT DISTINCT UPPER(trim(value)) AS normalized_value
            FROM unnest(string_to_array(COALESCE(days_of_week, ''), ',')) AS value
            WHERE trim(value) <> ''
        ) normalized_days
    ), ''),
    weeks_of_month = NULLIF((
        SELECT string_agg(normalized_value, ',' ORDER BY normalized_int)
        FROM (
            SELECT DISTINCT trim(value) AS normalized_value, (trim(value))::INTEGER AS normalized_int
            FROM unnest(string_to_array(COALESCE(weeks_of_month, ''), ',')) AS value
            WHERE trim(value) <> ''
        ) normalized_weeks
    ), ''),
    days_of_month = NULLIF((
        SELECT string_agg(normalized_value, ',' ORDER BY normalized_int)
        FROM (
            SELECT DISTINCT trim(value) AS normalized_value, (trim(value))::INTEGER AS normalized_int
            FROM unnest(string_to_array(COALESCE(days_of_month, ''), ',')) AS value
            WHERE trim(value) <> ''
        ) normalized_days
    ), ''),
    months_of_year = NULLIF((
        SELECT string_agg(normalized_value, ',' ORDER BY normalized_int)
        FROM (
            SELECT DISTINCT trim(value) AS normalized_value, (trim(value))::INTEGER AS normalized_int
            FROM unnest(string_to_array(COALESCE(months_of_year, ''), ',')) AS value
            WHERE trim(value) <> ''
        ) normalized_months
    ), '');

UPDATE account_transaction_feed
SET type = CASE UPPER(type)
    WHEN 'EXPENSE' THEN 'EXPENSE'
    WHEN 'INCOME' THEN 'INCOME'
    WHEN 'TRANSFER' THEN 'TRANSFER'
    ELSE UPPER(type)
END,
    status = CASE UPPER(status)
        WHEN 'BOOKED' THEN 'CLEARED'
        WHEN 'PENDING' THEN 'PENDING'
        WHEN 'RECONCILED' THEN 'RECONCILED'
        WHEN 'CLEARED' THEN 'CLEARED'
        ELSE UPPER(status)
    END;

UPDATE pocket_transaction_feed
SET type = CASE UPPER(type)
    WHEN 'EXPENSE' THEN 'EXPENSE'
    WHEN 'INCOME' THEN 'INCOME'
    WHEN 'TRANSFER' THEN 'TRANSFER'
    ELSE UPPER(type)
END,
    status = CASE UPPER(status)
        WHEN 'BOOKED' THEN 'CLEARED'
        WHEN 'PENDING' THEN 'PENDING'
        WHEN 'RECONCILED' THEN 'RECONCILED'
        WHEN 'CLEARED' THEN 'CLEARED'
        ELSE UPPER(status)
    END;

UPDATE pocket_transaction_feed ptf
SET contract_id = c.id
FROM contracts c
WHERE c.pocket_id = ptf.pocket_id;
