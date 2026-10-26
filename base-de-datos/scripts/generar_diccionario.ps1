<#
.SYNOPSIS
    Genera documentacion\diccionario_datos.md a partir del esquema real de la base.
.DESCRIPTION
    Lee information_schema y los COMMENT ON de las migraciones con
    sql\herramientas\diccionario_markdown.sql. Ejecutalo despues de migrar.
.EXAMPLE
    .\scripts\generar_diccionario.ps1 -Puerto 5432 -BaseDatos reda
#>
param(
    [string]$Servidor  = "localhost",
    [int]   $Puerto    = 5432,
    [string]$Usuario   = "postgres",
    [string]$BaseDatos = "reda"
)
$ErrorActionPreference = "Stop"
$raiz = Split-Path -Parent $PSScriptRoot
$psql = (Get-Command psql -ErrorAction SilentlyContinue).Source
if (-not $psql) {
    $psql = Get-ChildItem "C:\Program Files\PostgreSQL\*\bin\psql.exe" | Sort-Object { [int]$_.Directory.Parent.Name } -Descending |
            Select-Object -First 1 -ExpandProperty FullName
}
$env:PGCLIENTENCODING = "UTF8"
$tmp = New-TemporaryFile
& $psql -h $Servidor -p $Puerto -U $Usuario -d $BaseDatos -v ON_ERROR_STOP=1 -At `
        -f (Join-Path $raiz "sql\herramientas\diccionario_markdown.sql") -o $tmp.FullName
if ($LASTEXITCODE -ne 0) { throw "psql fallo" }
$cuerpo = [System.IO.File]::ReadAllText($tmp.FullName, [System.Text.Encoding]::UTF8)
Remove-Item $tmp.FullName
$migraciones = (Get-ChildItem (Join-Path $raiz "migraciones") -Filter "*.sql" | Sort-Object Name | ForEach-Object { $_.Name }) -join ", "
$enc = @"
# Diccionario de datos - REDA

> Generado automaticamente desde el esquema real (``scripts/generar_diccionario.ps1``). No editar a mano:
> los significados vienen de los ``COMMENT ON`` de las migraciones.
>
> Migraciones aplicadas: $migraciones
>
> Etiquetas: **[PERSONAL]** dato personal (Ley 1581 de 2012) - **[SENSIBLE]** dato sensible de salud -
> **[SIMULADO]** valor simulado para el proyecto de aula. La medida de proteccion se indica en cada columna.
> Claves: PK primaria, FK foranea, UQ unica.


"@
[System.IO.File]::WriteAllText((Join-Path $raiz "documentacion\diccionario_datos.md"), $enc + $cuerpo, (New-Object System.Text.UTF8Encoding $false))
Write-Host "Diccionario generado en documentacion\diccionario_datos.md"
