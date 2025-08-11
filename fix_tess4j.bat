@echo off
echo ===============================================
echo    E-Reader Tess4J Fix Script
echo ===============================================
echo.

echo Step 1: Cleaning Maven project...
call mvn clean
if errorlevel 1 (
    echo ERROR: Maven clean failed!
    pause
    exit /b 1
)
echo ✓ Maven clean completed

echo.
echo Step 2: Checking dependencies and compiling...
call mvn dependency:resolve
if errorlevel 1 (
    echo ERROR: Dependency resolution failed!
    pause
    exit /b 1
)

echo.
echo Step 3: Attempting compilation...
call mvn compile
if errorlevel 1 (
    echo ERROR: Maven compile failed!
    echo.
    echo This usually indicates a Tess4J dependency issue.
    echo Let me try to fix it...
    pause
    exit /b 1
)
echo ✓ Compilation completed successfully

echo.
echo Step 4: Testing with a simple run...
call mvn exec:java -Dexec.mainClass="E_Reader.Main"

pause
