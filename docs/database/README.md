# Database baseline and migration

PostgreSQL is primary; H2 is only the disposable demo/default test database. A fresh private schema has now been created on Supabase PostgreSQL 17.11; see [the Supabase runbook](SUPABASE.md). The older developer/deployed database on localhost port 5432 was unavailable during implementation. Its data/schema must be inventoried before migration. No existing volume or database was reset.

1. Set `PGHOST`, `PGPORT`, `PGDATABASE`, `PGUSER` and supply the password interactively or through an appropriate local credential store. Run `psql -v ON_ERROR_STOP=1 -f docs/database/01_inventory.sql`; save its output privately. It reports counts and schema, never credential values.
2. Back up with `pg_dump --format=custom --file=<private-backup>` and arrange a separate restore rehearsal before changing a database with real data. This release does not claim Phase31 backup verification.
3. Reconcile missing identities, normalized email/phone/username duplicates, employee/customer links and oversized legacy values. Never delete accounts to satisfy uniqueness. V001 fails atomically when its preflight finds these issues.
4. Review/apply `psql -v ON_ERROR_STOP=1 -f docs/database/V001__identity_security.sql`. Record filename/checksum/date/operator in your migration record. The migration is manual and versioned; application startup never applies SQL silently.
5. Start the default application with `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`; Hibernate uses `validate`, not `update`. Legacy plaintext credentials remain unchanged in storage and cannot authenticate: use verified recovery with configured SMTP to replace them. There is no automatic conversion/shared password.

UTC is used for security timestamps/expiry. Domain business dates from later phases retain existing behavior until their policies are settled. BusinessException is unchecked: Spring transactions roll back; later checked-exception domain workflows still need their owner-phase regression tests.

Tests: `mvnw.cmd test` is isolated H2. For real PostgreSQL use a disposable database named `lunea_test`, set `TEST_DB_URL`, `TEST_DB_USERNAME`, `TEST_DB_PASSWORD`, then `mvnw.cmd test -Dspring.profiles.active=test-postgres`. This profile creates/drops test schema and refuses database names without `_test`. Never point it at a real database.

The loopback-only demo uses its own in-memory H2, seeds explicit roles/permissions and three demo identities, and refuses a PostgreSQL datasource override. PostgreSQL schema creation for an empty test environment is distinct from migrating existing data. A schema dump from the disposable PostgreSQL verification is a mapping baseline, not evidence of a deployed schema.
