-- Read-only preflight. No credential values are selected.
BEGIN TRANSACTION READ ONLY;
SELECT version(), current_database(), current_schema();
SELECT table_name FROM information_schema.tables WHERE table_schema='public' ORDER BY table_name;
SELECT table_name, column_name, data_type, is_nullable, character_maximum_length
FROM information_schema.columns WHERE table_schema='public' ORDER BY table_name, ordinal_position;
SELECT conrelid::regclass AS table_name, conname, pg_get_constraintdef(oid)
FROM pg_constraint WHERE connamespace='public'::regnamespace ORDER BY conrelid::regclass::text, conname;
SELECT 'accounts' AS table_name, count(*) AS rows FROM accounts
UNION ALL SELECT 'customers', count(*) FROM customers
UNION ALL SELECT 'employees', count(*) FROM employees
UNION ALL SELECT 'roles', count(*) FROM roles
UNION ALL SELECT 'permissions', count(*) FROM permissions
UNION ALL SELECT 'employee_roles', count(*) FROM employee_roles
UNION ALL SELECT 'employee_stores', count(*) FROM employee_stores;
SELECT count(*) AS duplicate_email_groups FROM (
 SELECT lower(trim(email)) FROM accounts GROUP BY lower(trim(email)) HAVING count(*)>1
) duplicates;
SELECT count(*) AS duplicate_username_groups FROM (
 SELECT lower(trim(username)) FROM accounts GROUP BY lower(trim(username)) HAVING count(*)>1
) duplicates;
SELECT count(*) AS duplicate_phone_groups FROM (
 SELECT nullif(regexp_replace(trim(phone), '[[:space:]().-]', '', 'g'),'') AS phone
 FROM accounts GROUP BY 1 HAVING count(*)>1 AND nullif(regexp_replace(trim(phone), '[[:space:]().-]', '', 'g'),'') IS NOT NULL
) duplicates;
SELECT count(*) AS missing_identity FROM accounts
WHERE email IS NULL OR trim(email)='' OR username IS NULL OR trim(username)='' OR password_hash IS NULL OR account_type IS NULL;
SELECT count(*) AS legacy_passwords_requiring_recovery FROM accounts
WHERE password_hash !~ '^\$2[aby]\$[0-9]{2}\$[./A-Za-z0-9]{53}$';
SELECT count(*) AS invalid_employees FROM employees e LEFT JOIN accounts a ON a.id=e.account_id
WHERE a.id IS NULL OR a.account_type<>'EMPLOYEE' OR e.full_name IS NULL OR e.internal_email IS NULL;
SELECT count(*) AS invalid_customers FROM customers c LEFT JOIN accounts a ON a.id=c.account_id
WHERE a.id IS NULL OR a.account_type<>'CUSTOMER';
COMMIT;
