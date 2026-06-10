# Ejecutar como Administrador una sola vez.
# Registra la tarea que genera el reporte semanal HTML cada jueves a las 12:00.

$projectDir = "C:\aplic\ayudas_gv"
$nodePath   = (Get-Command node.exe -ErrorAction SilentlyContinue).Source

if (-not $nodePath) {
    Write-Error "node.exe no encontrado en el PATH. Instala Node.js o ajusta la ruta."
    exit 1
}

$action   = New-ScheduledTaskAction `
    -Execute        $nodePath `
    -Argument       "scripts\generar-reporte-semanal.js" `
    -WorkingDirectory $projectDir

$trigger  = New-ScheduledTaskTrigger -Weekly -DaysOfWeek Thursday -At "12:00"

$settings = New-ScheduledTaskSettingsSet `
    -ExecutionTimeLimit (New-TimeSpan -Minutes 2) `
    -StartWhenAvailable `
    -MultipleInstances IgnoreNew

Register-ScheduledTask `
    -TaskName    "AyudasGV - Reporte Semanal" `
    -Action      $action `
    -Trigger     $trigger `
    -Settings    $settings `
    -Description "Genera docs/reportes/semanas/semana-YYYY-MM-DD.html cada jueves a las 12:00" `
    -RunLevel    Highest `
    -Force

Write-Host ""
Write-Host "✅  Tarea registrada. Próxima ejecución: jueves a las 12:00."
Write-Host "    Para probarla ahora: Start-ScheduledTask 'AyudasGV - Reporte Semanal'"
