param([int]$Port = 8080)
$ErrorActionPreference = 'Stop'
if ($Port -lt 1 -or $Port -gt 65535) { throw 'Port must be between 1 and 65535.' }
$projectRoot = Split-Path -Parent $PSScriptRoot
$jdk21 = 'C:/Program Files/Java/jdk-21.0.10'
if (Test-Path -LiteralPath "$jdk21/bin/java.exe") { $env:JAVA_HOME = $jdk21 }
if (-not $env:JAVA_HOME -or -not (Test-Path -LiteralPath "$env:JAVA_HOME/bin/java.exe")) {
    throw 'Set JAVA_HOME to your JDK 21 installation before running this script.'
}
$oldErrorPreference = $ErrorActionPreference
$ErrorActionPreference = 'Continue'
try { $javaVersion = & "$env:JAVA_HOME/bin/java.exe" -version 2>&1 | Out-String }
finally { $ErrorActionPreference = $oldErrorPreference }
if ($javaVersion -notmatch 'version "21\.') { throw 'LUNEA requires JDK 21. Set JAVA_HOME to JDK 21.' }
Push-Location -LiteralPath $projectRoot
try {
    & .\mvnw.cmd spring-boot:run '-Dspring-boot.run.profiles=demo' "-Dspring-boot.run.arguments=--server.port=$Port"
    if ($LASTEXITCODE -ne 0) { throw "Application exited with code $LASTEXITCODE." }
} finally { Pop-Location }
