<#
.SYNOPSIS
    Exporta un .docx a .pdf con Microsoft Word (mismo nombre, misma carpeta).
.EXAMPLE
    .\scripts\exportar_pdf.ps1 documentacion\entrega2\Entrega2_REDA.docx
#>
param([Parameter(Mandatory = $true)][string]$Docx)
$ErrorActionPreference = "Stop"
$entrada = (Resolve-Path $Docx).Path
$salida = [System.IO.Path]::ChangeExtension($entrada, ".pdf")
$word = New-Object -ComObject Word.Application
$word.Visible = $false
try {
    $doc = $word.Documents.Open($entrada, $false, $true)
    $doc.SaveAs([ref]$salida, [ref]17)   # 17 = wdFormatPDF
    $doc.Close($false)
} finally {
    $word.Quit()
}
Write-Host "PDF generado: $salida"
