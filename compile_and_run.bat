@echo off
echo 編譯並運行 E_Reader 專案...

REM 進入專案目錄
cd /d "C:\E_Reader\E_Reader"

REM 使用 Maven 編譯專案
echo 正在使用 Maven 編譯...
call mvn clean compile

if %errorlevel% equ 0 (
    echo.
    echo 編譯成功！現在運行程序...
    echo.
    call mvn javafx:run
) else (
    echo 編譯失敗，請檢查錯誤信息
)

pause
