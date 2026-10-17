# Supabase bootstrap for LUNEA

Status (2026-10-08): applied successfully to the configured Supabase project through Shared pooler session mode. PostgreSQL 17.11 has 38 tables in `lunea`, all 38 with RLS enabled. Supabase API roles `anon`, `authenticated` and `service_role` have neither schema USAGE nor SELECT on `lunea.accounts`. Before bootstrap, `public` had zero base tables and `lunea` did not exist. The app passed `ddl-auto=validate`, served HTTP 200, and customer registration/login/session succeeded against Supabase; the temporary smoke account was removed. A one-time bootstrap created the first ADMIN, ten roles, 25 permissions and one clearly labeled virtual test store. ADMIN login/session and role listing returned HTTP 200. No pre-existing LUNEA data was migrated.

LUNEA uses Supabase **only as PostgreSQL**. Spring Boot handles authentication and sends recovery mail over its own SMTP settings. Do not use Supabase `anon`/`service_role` API keys as `DB_PASSWORD`.

For the existing prepared schema, use `psql -X -v ON_ERROR_STOP=1 -v inventory_schema=lunea -f docs/database/01_inventory.sql`. This is a read-only inventory, distinct from bootstrap/migration. Supabase's database name is `postgres`; never run the destructive `test-postgres` profile against it.

The fresh LUNEA schema is `lunea`, separate from Supabase's API-exposed `public` schema. `scripts/prepare-supabase-schema.ps1` derives `.local/supabase-init.sql` from the PostgreSQL/JPA test baseline. The script checks that the baseline contains no DML/drop statements, wraps all DDL in one transaction, refuses a nonempty `public` or existing `lunea` schema, creates the 38 LUNEA tables, revokes API-role access and enables RLS. It does **not** execute SQL. The generated file is ignored by Git and can be inspected before execution.

For a **new** database only:

1. Confirm the database/version and inventory objects read-only. Save the result privately. Do not run `V001__identity_security.sql` against an empty database: the generated baseline already includes those changes.
2. Generate and review the SQL: `powershell -ExecutionPolicy Bypass -File scripts/prepare-supabase-schema.ps1`.
3. Run the generated SQL through `psql -v ON_ERROR_STOP=1 -f .local/supabase-init.sql` using the actual Supabase host/user/password. It is atomic; if any statement fails, verify whether `lunea` exists before retrying.
4. Add `&currentSchema=lunea` to the JDBC `DB_URL`. Start the app without the `demo` profile; Hibernate's `ddl-auto=validate` checks mapping but does not create/alter tables.
5. Verify the 38 tables and that `anon`/`authenticated` cannot access the schema. The first ADMIN was provisioned through `InitialAdminBootstrap` with the one-time `bootstrap-admin` profile; it refuses a second run when staff/roles/permissions/stores already exist. Demo identities are never seeded here. Do not rerun bootstrap on this project.

Do not use the schema baseline for an existing LUNEA database. For existing data, follow [the inventory and migration runbook](README.md), take a backup, and reconcile legacy identities before applying `V001__identity_security.sql`.
