@echo off
setlocal enabledelayedexpansion
set "PROJECT_DIR=%~dp0"

if exist "%PROJECT_DIR%..\jdk-21\bin\java.exe" (
    set "JAVA_CMD=%PROJECT_DIR%..\jdk-21\bin\java.exe"
) else (
    set "JAVA_CMD=java"
)

if not exist "%PROJECT_DIR%bin\com\retail\inventory\gui\GuiLauncher.class" (
    echo Binaries not found. Building project first...
    call "%PROJECT_DIR%build.bat"
    if %ERRORLEVEL% NEQ 0 exit /b %ERRORLEVEL%
)

cd /d "%PROJECT_DIR%"
echo ========================================================
echo   Starting Smart Retail Modern Desktop Application
echo ========================================================
"%JAVA_CMD%" --module-path "%PROJECT_DIR%lib\javafx" --add-modules javafx.controls,javafx.fxml -cp "bin;lib\javafx\*" com.retail.inventory.gui.GuiLauncher %*
