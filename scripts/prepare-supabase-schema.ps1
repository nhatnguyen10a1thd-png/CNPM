# Generate a reviewable, atomic schema bootstrap for a fresh Supabase database.
# The source is the PostgreSQL schema already validated against the JPA entities.
$ErrorActionPreference = 'Stop'

$projectRoot = Split-Path -Parent $PSScriptRoot
$sourcePath = Join-Path $projectRoot 'docs/database/POSTGRES16_TEST_SCHEMA.sql'
$outputPath = Join-Path $projectRoot '.local/supabase-init.sql'
$source = [System.IO.File]::ReadAllText($sourcePath)

if ($source -notmatch 'CREATE TABLE public\.accounts') {
    throw 'Expected LUNEA baseline schema was not found.'
}
if ($source -match '(?im)^\s*(DROP|TRUNCATE|DELETE|INSERT|UPDATE|COPY)\b') {
    throw 'Baseline contains data-changing or destructive statements.'
}

$tableCount = [regex]::Matches($source, '(?m)^CREATE TABLE public\.').Count
$body = $source.Replace('public.', 'lunea.')
$body = [regex]::Replace($body, '(?m)^\\(?:un)?restrict[^\r\n]*\r?\n', '')

$prefix = @'
-- LUNEA fresh Supabase bootstrap. Run only after read-only inventory confirms
-- that schema lunea does not exist. All statements are one transaction.
BEGIN;
DO $$
BEGIN
  IF EXISTS (SELECT 1 FROM pg_namespace WHERE nspname = 'lunea') THEN
    RAISE EXCEPTION 'Schema lunea already exists; bootstrap is for an empty target only';
  END IF;
  IF EXISTS (
    SELECT 1 FROM pg_class c JOIN pg_namespace n ON n.oid = c.relnamespace
    WHERE n.nspname = 'public' AND c.relkind IN ('r', 'p')
  ) THEN
    RAISE EXCEPTION 'Public schema has tables; inventory or migrate existing data first';
  END IF;
END $$;
CREATE SCHEMA lunea;
REVOKE ALL ON SCHEMA lunea FROM PUBLIC, anon, authenticated, service_role;

'@

$suffix = @'

-- Defense in depth: Supabase Data API roles get no access even if this
-- private schema is later added to the list of exposed schemas.
REVOKE ALL ON ALL TABLES IN SCHEMA lunea FROM PUBLIC, anon, authenticated, service_role;
REVOKE ALL ON ALL SEQUENCES IN SCHEMA lunea FROM PUBLIC, anon, authenticated, service_role;
DO $$
DECLARE item record;
BEGIN
  FOR item IN
    SELECT c.relname
    FROM pg_class c JOIN pg_namespace n ON n.oid = c.relnamespace
    WHERE n.nspname = 'lunea' AND c.relkind IN ('r', 'p')
  LOOP
    EXECUTE format('ALTER TABLE lunea.%I ENABLE ROW LEVEL SECURITY', item.relname);
  END LOOP;
  IF (SELECT count(*) FROM pg_class c JOIN pg_namespace n ON n.oid = c.relnamespace
      WHERE n.nspname = 'lunea' AND c.relkind IN ('r', 'p')) <> __EXPECTED_TABLES__ THEN
    RAISE EXCEPTION 'Unexpected LUNEA table count after bootstrap';
  END IF;
END $$;
COMMIT;
'@

$sql = $prefix + [Environment]::NewLine + $body + [Environment]::NewLine + $suffix.Replace('__EXPECTED_TABLES__', [string]$tableCount)
[System.IO.File]::WriteAllText($outputPath, $sql, (New-Object System.Text.UTF8Encoding($false)))
Write-Output ("Prepared private LUNEA schema with {0} tables at {1}" -f $tableCount, $outputPath)
