@echo off
setlocal enabledelayedexpansion
set "PROJECT_DIR=%~dp0"

echo ========================================================
echo   SmartRetail - Retail Inventory & Billing Suite
echo ========================================================
echo.
echo   [1] Modern Desktop GUI (JavaFX 21 Pro Interface) [Default]
echo   [2] Classic Terminal Console Mode
echo.
echo Auto-launching Modern Desktop GUI in 3 seconds...
choice /c 12 /t 3 /d 1 /n /m "Press 1 for GUI or 2 for Console [Default: 1]: "

if errorlevel 2 goto run_console
goto run_gui

:run_gui
call "%PROJECT_DIR%run-gui.bat" %*
goto end

:run_console
call "%PROJECT_DIR%run-console.bat" %*
goto end

:end
