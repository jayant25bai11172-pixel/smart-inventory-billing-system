@echo off
setlocal enabledelayedexpansion
echo ========================================================
echo   Compiling Smart Inventory ^& Billing Manager
echo ========================================================

set "PROJECT_DIR=%~dp0"
if exist "%PROJECT_DIR%..\jdk-21\bin\javac.exe" (
    set "JAVAC_CMD=%PROJECT_DIR%..\jdk-21\bin\javac.exe"
) else (
    set "JAVAC_CMD=javac"
)

if not exist "%PROJECT_DIR%bin" mkdir "%PROJECT_DIR%bin"

echo Finding Java source files...
dir /s /b "%PROJECT_DIR%src\*.java" > "%PROJECT_DIR%sources.txt"

echo Compiling Java source files...
"%JAVAC_CMD%" -d "%PROJECT_DIR%bin" -encoding UTF-8 @"%PROJECT_DIR%sources.txt"

set COMPILE_STATUS=%ERRORLEVEL%
if exist "%PROJECT_DIR%sources.txt" del "%PROJECT_DIR%sources.txt"

if %COMPILE_STATUS% EQU 0 (
    echo [SUCCESS] Compilation successful! Classes compiled to bin\
) else (
    echo [ERROR] Compilation failed with error code %COMPILE_STATUS%.
    exit /b %COMPILE_STATUS%
)
