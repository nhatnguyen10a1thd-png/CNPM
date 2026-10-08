-- Read-only preflight. No credential values are selected.
-- psql -v ON_ERROR_STOP=1 -v inventory_schema=lunea -f docs/database/01_inventory.sql
-- Defaults to public for the older local database; never falls back to another schema.
\if :{?inventory_schema}
\else
\set inventory_schema public
\endif
BEGIN TRANSACTION READ ONLY;
SELECT :'inventory_schema'::regnamespace AS inventory_namespace;
SELECT set_config('search_path', format('%I', :'inventory_schema'), true);
SELECT version(), current_database(), current_schema();
SELECT table_name FROM information_schema.tables WHERE table_schema=:'inventory_schema' ORDER BY table_name;
SELECT table_name, column_name, data_type, is_nullable, character_maximum_length
FROM information_schema.columns WHERE table_schema=:'inventory_schema' ORDER BY table_name, ordinal_position;
SELECT conrelid::regclass AS table_name, conname, pg_get_constraintdef(oid)
FROM pg_constraint WHERE connamespace=:'inventory_schema'::regnamespace ORDER BY conrelid::regclass::text, conname;
SELECT tablename, indexname, indexdef FROM pg_indexes
WHERE schemaname=:'inventory_schema' ORDER BY tablename, indexname;
SELECT t.typname, e.enumlabel, e.enumsortorder FROM pg_type t
JOIN pg_namespace n ON n.oid=t.typnamespace JOIN pg_enum e ON e.enumtypid=t.oid
WHERE n.nspname=:'inventory_schema' ORDER BY t.typname, e.enumsortorder;
-- Count every base table, including empty business tables. No row payloads are returned.
SELECT format('SELECT %L AS table_name, count(*) AS rows FROM %I.%I;', table_name, table_schema, table_name)
FROM information_schema.tables WHERE table_schema=:'inventory_schema' AND table_type='BASE TABLE' ORDER BY table_name
\gexec
-- STRING/ORDINAL representations remain unchanged; report actual stored status values.
SELECT format('SELECT %L AS table_name, %L AS column_name, %I::text AS stored_value, count(*) AS rows FROM %I.%I GROUP BY %I ORDER BY %I;',
 table_name, column_name, column_name, table_schema, table_name, column_name, column_name)
FROM information_schema.columns WHERE table_schema=:'inventory_schema' AND column_name IN ('status','account_type')
ORDER BY table_name, column_name
\gexec
-- Check all existing FK pairs (including composite links); null optional references are excluded.
SELECT format('SELECT %L AS constraint_name, count(*) AS orphan_rows FROM %s source WHERE %s AND NOT EXISTS (SELECT 1 FROM %s target WHERE %s);',
 c.conname, c.conrelid::regclass, pairs.nonnull, c.confrelid::regclass, pairs.equal)
FROM pg_constraint c CROSS JOIN LATERAL (
 SELECT string_agg(format('source.%I IS NOT NULL', a.attname), ' AND ' ORDER BY k.i) AS nonnull,
        string_agg(format('source.%I = target.%I', a.attname, b.attname), ' AND ' ORDER BY k.i) AS equal
 FROM generate_subscripts(c.conkey, 1) k(i)
 JOIN pg_attribute a ON a.attrelid=c.conrelid AND a.attnum=c.conkey[k.i]
 JOIN pg_attribute b ON b.attrelid=c.confrelid AND b.attnum=c.confkey[k.i]
) pairs
WHERE c.contype='f' AND c.connamespace=:'inventory_schema'::regnamespace ORDER BY c.conname
\gexec
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
SELECT count(*) AS oversized_identity FROM accounts
WHERE length(trim(email))>254 OR length(trim(username))>254 OR length(password_hash)>100;
SELECT count(*) AS invalid_phone_format FROM accounts
WHERE nullif(trim(phone),'') IS NOT NULL AND regexp_replace(trim(phone),'[[:space:]().-]','','g') !~ '^\+?[0-9]{9,15}$';
SELECT count(*) AS duplicate_internal_email_groups FROM (
 SELECT lower(trim(internal_email)) FROM employees GROUP BY 1 HAVING count(*)>1
) duplicates;
SELECT count(*) AS duplicate_employee_account_groups FROM (
 SELECT account_id FROM employees GROUP BY account_id HAVING count(*)>1
) duplicates;
COMMIT;
