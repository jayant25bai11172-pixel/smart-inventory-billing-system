@echo off
setlocal enabledelayedexpansion
set "PROJECT_DIR=%~dp0"

if exist "%PROJECT_DIR%..\jdk-21\bin\java.exe" (
    set "JAVA_CMD=%PROJECT_DIR%..\jdk-21\bin\java.exe"
) else (
    set "JAVA_CMD=java"
)

if not exist "%PROJECT_DIR%bin\com\retail\inventory\SmartInventoryTestSuite.class" (
    echo Binaries not found. Building project first...
    call "%PROJECT_DIR%build.bat"
    if %ERRORLEVEL% NEQ 0 exit /b %ERRORLEVEL%
)

cd /d "%PROJECT_DIR%"
"%JAVA_CMD%" -cp bin com.retail.inventory.SmartInventoryTestSuite
