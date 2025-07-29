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
    echo 1. ✅ 根據模式動態顯示功能按鈕
    echo 2. ✅ 圖片模式移除文字相關功能（文字搜尋、字體調整、行距調整）
    echo 3. ✅ 文字模式移除圖片相關功能（圖片縮放、旋轉、適配）
    echo 4. ✅ 新增返回檔案管理器按鈕
    echo 5. ✅ 頁碼標籤移至右下角顯示
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
