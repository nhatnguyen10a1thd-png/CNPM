param([int]$Port = 8081)
$ErrorActionPreference = 'Stop'

if ($Port -lt 1 -or $Port -gt 65535) { throw 'Port must be between 1 and 65535.' }
$projectRoot = Split-Path -Parent $PSScriptRoot
$configPath = Join-Path $projectRoot '.local/production.env'
$jarPath = Join-Path $projectRoot 'target/cosmetic-0.0.1-SNAPSHOT.jar'
if (-not (Test-Path -LiteralPath $configPath)) { throw 'Fill .local/production.env first.' }
if (-not (Test-Path -LiteralPath $jarPath)) { throw 'Build the app with .\mvnw.cmd package first.' }

$config = @{}
foreach ($line in [System.IO.File]::ReadAllLines($configPath)) {
    if ($line -match '^([A-Z][A-Z0-9_]*)=(.*)$') { $config[$matches[1]] = $matches[2] }
}
foreach ($key in @('DB_URL', 'DB_USERNAME', 'DB_PASSWORD')) {
    if ([string]::IsNullOrWhiteSpace($config[$key])) { throw "$key is missing in .local/production.env." }
}
if ($config['DB_URL'] -notmatch '^jdbc:postgresql://[^/]+/postgres\?[^\r\n]*currentSchema=lunea') {
    throw 'DB_URL must point to the prepared LUNEA schema on Supabase.'
}

$jdk21 = 'C:/Program Files/Java/jdk-21.0.10'
if (Test-Path -LiteralPath "$jdk21/bin/java.exe") { $javaExe = "$jdk21/bin/java.exe" }
elseif ($env:JAVA_HOME -and (Test-Path -LiteralPath "$env:JAVA_HOME/bin/java.exe")) { $javaExe = "$env:JAVA_HOME/bin/java.exe" }
else { throw 'JDK 21 is required; set JAVA_HOME to JDK 21.' }
$oldErrorPreference = $ErrorActionPreference
$ErrorActionPreference = 'Continue'
try { $javaVersion = & $javaExe -version 2>&1 | Out-String }
finally { $ErrorActionPreference = $oldErrorPreference }
if ($javaVersion -notmatch 'version "21\.') { throw 'LUNEA requires JDK 21.' }

$keys = @('DB_URL','DB_USERNAME','DB_PASSWORD','SMTP_HOST','SMTP_PORT','SMTP_USERNAME',
          'SMTP_PASSWORD','SMTP_AUTH','SMTP_STARTTLS','MAIL_FROM','APP_BASE_URL','PORT',
          'COOKIE_SECURE','SPRING_PROFILES_ACTIVE')
$previous = @{}
foreach ($key in $keys) { $previous[$key] = [Environment]::GetEnvironmentVariable($key, 'Process') }
try {
    foreach ($key in $keys) {
        if ($config.ContainsKey($key) -and $key -notin @('APP_BASE_URL','PORT','COOKIE_SECURE','SPRING_PROFILES_ACTIVE')) {
            [Environment]::SetEnvironmentVariable($key, $config[$key], 'Process')
        }
    }
    [Environment]::SetEnvironmentVariable('APP_BASE_URL', "http://localhost:$Port", 'Process')
    [Environment]::SetEnvironmentVariable('PORT', [string]$Port, 'Process')
    [Environment]::SetEnvironmentVariable('COOKIE_SECURE', 'false', 'Process')
    [Environment]::SetEnvironmentVariable('SPRING_PROFILES_ACTIVE', $null, 'Process')
    Write-Output "LUNEA on Supabase: http://localhost:$Port (Ctrl+C to stop)"
    & $javaExe -jar $jarPath '--server.address=127.0.0.1' '--logging.level.root=INFO'
    if ($LASTEXITCODE -ne 0) { throw "Application exited with code $LASTEXITCODE." }
} finally {
    foreach ($key in $keys) { [Environment]::SetEnvironmentVariable($key, $previous[$key], 'Process') }
}
