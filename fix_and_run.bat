@echo off
echo ===============================================
echo    E-Reader Project Fix Script
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
echo Step 2: Compiling project...
call mvn compile
if errorlevel 1 (
    echo ERROR: Maven compile failed!
    echo.
    echo Common fixes:
    echo - Make sure Java 17+ is installed
    echo - Check if all dependencies are available
    echo - Verify your internet connection
    pause
    exit /b 1
)
echo ✓ Compilation completed

echo.
echo Step 3: Running application via Maven...
echo.
call mvn javafx:run

echo.
echo ===============================================
echo If the application ran successfully, great!
echo.
echo If you still have issues in IntelliJ:
echo 1. Go to File → Project Structure → Project
echo 2. Set Project SDK to Java 17 or higher
echo 3. Go to Run → Edit Configurations
echo 4. Make sure Main class is: E_Reader.Main
echo 5. VM options: --module-path ^"path\to\javafx\lib^" --add-modules javafx.controls,javafx.fxml,javafx.swing,javafx.media,javafx.web
echo 6. Module: ereader
echo ===============================================
pause