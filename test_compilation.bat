@echo off
echo 編譯 E_Reader 專案...

REM 進入專案目錄
cd /d "C:\E_Reader\E_Reader"

REM 使用 Maven 編譯專案
echo 正在使用 Maven 編譯...
call mvn clean compile

if %errorlevel% equ 0 (
    echo.
    echo 編譯成功！
    echo.
    echo 主要修改內容：
    echo 1. ✅ 檔案管理器左側資料夾樹狀結構改為懶惰載入
    echo 2. ✅ 只有點擊展開時才顯示子資料夾
    echo 3. ✅ 新增網格檢視和清單檢視切換功能
    echo 4. ✅ 修正檢視模式切換問題
    echo.
    echo 你可以運行以下命令啟動應用程式：
    echo mvn javafx:run
    echo.
    echo 或者使用現有的批次檔案：
    echo run_with_maven.bat
) else (
    echo 編譯失敗，請檢查錯誤信息
)

pause
