<#
.SYNOPSIS
    Comprueba que la base de REDA se reconstruye desde cero (lo que se hace antes de cada tag).

.DESCRIPTION
    Trabaja sobre una base APARTE (por defecto reda_verificacion): la base de trabajo 'reda' no se toca.
    1. Crea la base vacia y aplica las migraciones y la semilla (migrar.ps1 -Recrear -ConSemilla).
    2. Ejecuta las pruebas negativas de restricciones, la coherencia de la semilla y la
       verificacion cruzada del banco de consultas.
    3. Muestra cuantas filas quedo en cada tabla.
    4. Borra la base de verificacion (salvo con -Conservar).

    La contrasena se toma de $env:PGPASSWORD; si no esta, de programacion\backend\application-local.properties
    (archivo local en .gitignore); si tampoco existe, psql la pide.

.EXAMPLE
    .\scripts\verificar_desde_cero.ps1
#>
param(
    [string]$Servidor  = "localhost",
    [int]   $Puerto    = 5432,
    [string]$Usuario   = "postgres",
    [string]$BaseDatos = "reda_verificacion",
    [switch]$Conservar
)

$ErrorActionPreference = "Stop"
$raiz = Split-Path -Parent $PSScriptRoot
if ($BaseDatos -eq "reda") { throw "Usa una base distinta de 'reda': esta verificacion la borra y la recrea." }

if (-not $env:PGPASSWORD) {
    $local = Join-Path $raiz "..\programacion\backend\application-local.properties"
    if (Test-Path $local) {
        $linea = Get-Content $local | Where-Object { $_ -match '^\s*spring\.datasource\.password\s*=' } | Select-Object -First 1
        if ($linea) { $env:PGPASSWORD = ($linea -split '=', 2)[1].Trim() }
    }
}

$psql = (Get-Command psql -ErrorAction SilentlyContinue).Source
if (-not $psql) {
    $psql = Get-ChildItem "C:\Program Files\PostgreSQL\*\bin\psql.exe" -ErrorAction SilentlyContinue |
            Sort-Object { [int]$_.Directory.Parent.Name } -Descending | Select-Object -First 1 -ExpandProperty FullName
}
$env:PGCLIENTENCODING = "UTF8"
# psql escribe en UTF-8; sin esto PowerShell lo lee como la pagina de la consola y las tildes salen mal
[Console]::OutputEncoding = [System.Text.Encoding]::UTF8
$conexion = @("-h", $Servidor, "-p", $Puerto, "-U", $Usuario)
$fallos = 0

function Ejecutar([string]$archivo) {
    $salida = & $psql @conexion -d $BaseDatos -v ON_ERROR_STOP=1 -q -f (Join-Path $raiz $archivo) 2>&1 | ForEach-Object { "$_" }
    if ($LASTEXITCODE -ne 0) { $script:fallos++ }
    return $salida
}

Write-Host "`n=== 1. Base vacia + migraciones + semilla ($BaseDatos)" -ForegroundColor Cyan
& (Join-Path $PSScriptRoot "migrar.ps1") -Servidor $Servidor -Puerto $Puerto -Usuario $Usuario -BaseDatos $BaseDatos -Recrear -ConSemilla

Write-Host "`n=== 2. Pruebas negativas de restricciones" -ForegroundColor Cyan
$r = Ejecutar "sql\pruebas\probar_restricciones.sql"
$ok = @($r | Where-Object { $_ -match '^\s*OK\s' }).Count
# -cmatch distingue mayusculas: el encabezado de la columna "debe_fallar" no es una falla
$r | Where-Object { $_ -cmatch 'FALLA:|ERROR' } | ForEach-Object { Write-Host $_ -ForegroundColor Red }
Write-Host "Rechazadas por la restriccion esperada: $ok"

Write-Host "`n=== 3. Coherencia de la semilla (cada comprobacion debe dar 0)" -ForegroundColor Cyan
$r = Ejecutar "sql\pruebas\verificar_semilla.sql"
$r | Where-Object { $_ -notmatch '^\s*(-+|\(\d+ (fila|row)s?\)|[a-z_]+)\s*$' -and $_.Trim() } | ForEach-Object { Write-Host $_ }

Write-Host "`n=== 4. Verificacion cruzada del banco de consultas (todas deben ser t)" -ForegroundColor Cyan
$r = Ejecutar "sql\consultas\verificacion_consultas.sql"
$filas = @($r | Where-Object { $_ -match '\|\s*[tf]\s*$' })
$malas = @($filas | Where-Object { $_ -match '\|\s*f\s*$' })
$malas | ForEach-Object { Write-Host $_ -ForegroundColor Red }
Write-Host "Verificaciones: $($filas.Count) - no coinciden: $($malas.Count)"
if ($malas.Count -gt 0) { $fallos++ }

Write-Host "`n=== 5. Filas por tabla" -ForegroundColor Cyan
& $psql @conexion -d $BaseDatos -At -P pager=off -c @"
SELECT rpad(c.relname, 24) || (xpath('//n/text()', query_to_xml('SELECT count(*) AS n FROM ' || quote_ident(c.relname), false, false, '')))[1]::text
FROM pg_class c JOIN pg_namespace s ON s.oid = c.relnamespace
WHERE s.nspname = 'public' AND c.relkind = 'r' AND c.relname <> 'control_migraciones' ORDER BY c.relname;
"@

if (-not $Conservar) {
    & $psql @conexion -d postgres -q -c "DROP DATABASE IF EXISTS $BaseDatos WITH (FORCE);"
    Write-Host "`nBase $BaseDatos eliminada." -ForegroundColor DarkGray
}
if ($fallos -gt 0) { Write-Host "`nRESULTADO: $fallos bloque(s) con fallas" -ForegroundColor Red; exit 1 }
Write-Host "`nRESULTADO: la base se reconstruye desde cero y pasa todas las verificaciones" -ForegroundColor Green
