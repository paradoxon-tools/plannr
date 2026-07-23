DELETE FROM transaction_materializations
WHERE transaction_date::date > CURRENT_DATE;
