@echo off
echo "Compiling and running E_Reader..."
echo.

REM Clean and compile
echo "Step 1: Cleaning previous build..."
call mvn clean

echo.
echo "Step 2: Compiling project..."
call mvn compile

echo.
echo "Step 3: Running application..."
call mvn javafx:run

echo.
echo "Done!"
pause