<#
.SYNOPSIS
    Arranca REDA en http://localhost:8080 con un solo comando.

.DESCRIPTION
    1. Si no existe application-local.properties (esta en .gitignore), pide la clave de
       PostgreSQL una sola vez y lo crea con un secreto JWT aleatorio.
    2. Aplica las migraciones pendientes (base-de-datos\scripts\migrar.ps1, incremental:
       no borra datos). Con -Recrear reconstruye la base desde cero con la semilla de demo.
    3. Ejecuta mvn spring-boot:run.

    La clave NUNCA queda en este archivo ni en el repositorio.

.EXAMPLE
    .\iniciar.ps1
.EXAMPLE
    .\iniciar.ps1 -Recrear
#>
param([switch]$Recrear)

$ErrorActionPreference = "Stop"
Set-Location $PSScriptRoot
$local = Join-Path $PSScriptRoot "application-local.properties"

# --- 1. Configuracion local --------------------------------------------------
if (-not (Test-Path $local)) {
    $segura = Read-Host "Clave del usuario postgres" -AsSecureString
    $clave  = [Runtime.InteropServices.Marshal]::PtrToStringAuto(
                  [Runtime.InteropServices.Marshal]::SecureStringToBSTR($segura))
    $secreto = -join ((48..57) + (65..90) + (97..122) | Get-Random -Count 48 | ForEach-Object { [char]$_ })
    $lineas = @(
        "# Archivo LOCAL (en .gitignore). No subir al repositorio."
        "spring.datasource.url=jdbc:postgresql://localhost:5432/reda"
        "spring.datasource.username=postgres"
        "spring.datasource.password=$clave"
        "reda.jwt.secreto=$secreto"
    )
    # UTF-8 sin BOM: con BOM, Spring leeria basura al inicio del archivo
    [IO.File]::WriteAllLines($local, $lineas, (New-Object Text.UTF8Encoding($false)))
    Write-Host "Creado application-local.properties" -ForegroundColor Green
}

# Lee la clave del archivo local para que psql (migraciones) use la misma
$linea = Get-Content $local | Where-Object { $_ -match '^\s*spring\.datasource\.password\s*=' } | Select-Object -First 1
$env:PGPASSWORD = ($linea -split '=', 2)[1].Trim()

# --- 2. Migraciones ----------------------------------------------------------
$migrar = Join-Path $PSScriptRoot "..\..\base-de-datos\scripts\migrar.ps1"
if ($Recrear) { & $migrar -Recrear -ConSemilla } else { & $migrar }

# --- 3. Servidor -------------------------------------------------------------
$ocupado = Get-NetTCPConnection -LocalPort 8080 -State Listen -ErrorAction SilentlyContinue
if ($ocupado) { throw "El puerto 8080 ya esta en uso (proceso $($ocupado[0].OwningProcess)). Cierralo y vuelve a intentar." }

Write-Host "Arrancando REDA... cuando veas 'Started' abre http://localhost:8080" -ForegroundColor Cyan
mvn spring-boot:run
