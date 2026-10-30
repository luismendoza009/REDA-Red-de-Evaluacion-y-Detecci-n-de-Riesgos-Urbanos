<#
.SYNOPSIS
    Construye la base de datos de REDA ejecutando las migraciones en orden.

.DESCRIPTION
    - Ejecuta migraciones\NNN_*.sql en orden numerico, cada una en su propia transaccion
      (--single-transaction): si una falla, no queda a medias.
    - Registra cada migracion aplicada en la tabla control_migraciones, asi al volver a
      ejecutar solo aplica las nuevas.
    - Con -Recrear borra y crea la base desde cero (base VACIA), que es como se prueba
      la entrega y como se prepara la demo.
    - Con -ConSemilla carga despues los datos de sql\semilla\*.sql en orden.

    La contrasena NUNCA va en este archivo: se toma de $env:PGPASSWORD o psql la pide.

.EXAMPLE
    .\scripts\migrar.ps1 -Recrear -ConSemilla
.EXAMPLE
    $env:PGPASSWORD = 'tu_clave'; .\scripts\migrar.ps1 -Puerto 5432 -BaseDatos reda
#>
param(
    [string]$Servidor  = "localhost",
    [int]   $Puerto    = 5432,
    [string]$Usuario   = "postgres",
    [string]$BaseDatos = "reda",
    [switch]$Recrear,
    [switch]$ConSemilla
)

$ErrorActionPreference = "Stop"
$raiz = Split-Path -Parent $PSScriptRoot

# psql: primero el PATH, si no, la instalacion estandar de PostgreSQL en Windows
$psql = (Get-Command psql -ErrorAction SilentlyContinue).Source
if (-not $psql) {
    $psql = Get-ChildItem "C:\Program Files\PostgreSQL\*\bin\psql.exe" -ErrorAction SilentlyContinue |
            Sort-Object { [int]$_.Directory.Parent.Name } -Descending | Select-Object -First 1 -ExpandProperty FullName
}
if (-not $psql) { throw "No se encontro psql. Instala PostgreSQL 16+ o agrega su carpeta bin al PATH." }

# Los .sql estan en UTF-8; sin esto psql en Windows asume WIN1252 y danaria las tildes
$env:PGCLIENTENCODING = "UTF8"
$conexion = @("-h", $Servidor, "-p", $Puerto, "-U", $Usuario, "-v", "ON_ERROR_STOP=1", "-q")

function Invoke-Psql([string]$db, [string[]]$extra) {
    & $psql @conexion -d $db @extra
    if ($LASTEXITCODE -ne 0) { throw "psql fallo (codigo $LASTEXITCODE)" }
}

if ($Recrear) {
    Write-Host "Recreando la base '$BaseDatos' desde cero..." -ForegroundColor Yellow
    Invoke-Psql "postgres" @("-c", "DROP DATABASE IF EXISTS $BaseDatos WITH (FORCE);")
    Invoke-Psql "postgres" @("-c", "CREATE DATABASE $BaseDatos ENCODING 'UTF8' TEMPLATE template0;")
} else {
    $existe = & $psql @conexion -d postgres -At -c "SELECT 1 FROM pg_database WHERE datname = '$BaseDatos';"
    if ($existe -ne "1") { Invoke-Psql "postgres" @("-c", "CREATE DATABASE $BaseDatos ENCODING 'UTF8' TEMPLATE template0;") }
}

Invoke-Psql $BaseDatos @("-c", @"
CREATE TABLE IF NOT EXISTS control_migraciones (
    version     VARCHAR(10) PRIMARY KEY,
    archivo     VARCHAR(200) NOT NULL,
    aplicada_en TIMESTAMPTZ NOT NULL DEFAULT now()
);
"@)

$aplicadas = & $psql @conexion -d $BaseDatos -At -c "SELECT version FROM control_migraciones;"
$migraciones = Get-ChildItem (Join-Path $raiz "migraciones") -Filter "*.sql" |
               Where-Object { $_.Name -match '^\d{3}_' } | Sort-Object Name
$duplicadas = $migraciones | Group-Object { $_.Name.Substring(0, 3) } | Where-Object Count -gt 1
if ($duplicadas) {
    $detalle = ($duplicadas | ForEach-Object { "$($_.Name): $($_.Group.Name -join ', ')" }) -join '; '
    throw "Hay versiones de migración duplicadas. Cada prefijo de tres dígitos debe ser único: $detalle"
}

foreach ($m in $migraciones) {
    $version = $m.Name.Substring(0, 3)
    if ($aplicadas -contains $version) { Write-Host "  = $($m.Name) (ya aplicada)"; continue }
    Write-Host "  + $($m.Name)" -ForegroundColor Green
    # la migracion y su registro en control_migraciones van en la MISMA transaccion
    $tmp = New-TemporaryFile
    $cuerpo = (Get-Content $m.FullName -Raw -Encoding UTF8) +
              "`nINSERT INTO control_migraciones (version, archivo) VALUES ('$version', '$($m.Name)');`n"
    [System.IO.File]::WriteAllText($tmp.FullName, $cuerpo, (New-Object System.Text.UTF8Encoding $false))
    try { Invoke-Psql $BaseDatos @("--single-transaction", "-f", $tmp.FullName) }
    finally { Remove-Item $tmp.FullName }
}

if ($ConSemilla) {
    Write-Host "Cargando datos semilla..." -ForegroundColor Yellow
    $semillas = Get-ChildItem (Join-Path $raiz "sql\semilla") -Filter "*.sql" | Sort-Object Name
    foreach ($s in $semillas) {
        Write-Host "  + $($s.Name)" -ForegroundColor Green
        Invoke-Psql $BaseDatos @("--single-transaction", "-f", $s.FullName)
    }
}

Write-Host "Listo. Base '$BaseDatos' en ${Servidor}:$Puerto" -ForegroundColor Cyan
