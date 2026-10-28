@echo off
setlocal enabledelayedexpansion
echo ========================================================
echo   Compiling Smart Inventory and Billing Manager JavaFX
echo ========================================================

set "PROJECT_DIR=%~dp0"
if exist "%PROJECT_DIR%..\jdk-21\bin\javac.exe" (
    set "JAVAC_CMD=%PROJECT_DIR%..\jdk-21\bin\javac.exe"
) else (
    set "JAVAC_CMD=javac"
)

if not exist "%PROJECT_DIR%bin" mkdir "%PROJECT_DIR%bin"

REM Ensure JavaFX libraries are present
if not exist "%PROJECT_DIR%lib\javafx\javafx-controls-21.0.2-win.jar" (
    echo [INFO] JavaFX libraries not found. Downloading OpenJFX 21 from Maven Central...
    powershell -NoProfile -ExecutionPolicy Bypass -Command "$dest = '%PROJECT_DIR%lib\javafx'; if (!(Test-Path $dest)) { New-Item -ItemType Directory -Path $dest -Force | Out-Null }; $mods = @('javafx-base', 'javafx-graphics', 'javafx-controls', 'javafx-fxml'); foreach ($m in $mods) { foreach ($s in @('', '-win')) { $f = \"$m-21.0.2$s.jar\"; $t = Join-Path $dest $f; if (!(Test-Path $t)) { Write-Host \"Fetching $f...\"; Invoke-WebRequest -Uri \"https://repo1.maven.org/maven2/org/openjfx/$m/21.0.2/$f\" -OutFile $t -UseBasicParsing } } }"
)

echo Finding Java source files...
dir /s /b "%PROJECT_DIR%src\*.java" > "%PROJECT_DIR%sources.txt"

echo Compiling Java source files with JavaFX modules...
"%JAVAC_CMD%" --module-path "%PROJECT_DIR%lib\javafx" --add-modules javafx.controls,javafx.fxml -d "%PROJECT_DIR%bin" -encoding UTF-8 @"%PROJECT_DIR%sources.txt"

set COMPILE_STATUS=%ERRORLEVEL%
if exist "%PROJECT_DIR%sources.txt" del "%PROJECT_DIR%sources.txt"

if %COMPILE_STATUS% EQU 0 (
    echo [INFO] Copying UI resources...
    if exist "%PROJECT_DIR%src\main\resources" (
        xcopy /s /e /y /i "%PROJECT_DIR%src\main\resources\*" "%PROJECT_DIR%bin\" > nul
    )
    echo [SUCCESS] Compilation successful! Classes and resources placed in bin
) else (
    echo [ERROR] Compilation failed with error code %COMPILE_STATUS%.
    exit /b %COMPILE_STATUS%
)
