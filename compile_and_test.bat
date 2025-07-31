@echo off
echo Compiling E_Reader with new file manager features...

cd /d C:\E_Reader\E_Reader

echo Cleaning previous build...
if exist target rmdir /s /q target

echo Compiling with Maven...
call mvn clean compile

if %ERRORLEVEL% EQU 0 (
    echo Compilation successful!
    echo Running E_Reader...
    call mvn exec:java -Dexec.mainClass="E_Reader.Main"
) else (
    echo Compilation failed!
    pause
)
