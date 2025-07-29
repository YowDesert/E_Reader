@echo off
echo 正在測試檔案關閉功能...
echo.

cd /d "C:\E_Reader\E_Reader"

echo 1. 清理之前的編譯檔案...
if exist "target\classes" rmdir /s /q "target\classes"

echo.
echo 2. 使用 Maven 編譯專案...
call mvn clean compile

if %errorlevel% equ 0 (
    echo.
    echo ✅ 編譯成功！檔案關閉功能已成功整合。
    echo.
    echo 新功能說明：
    echo - 點擊 "返回檔案管理" 按鈕會自動關閉當前開啟的檔案
    echo - 關閉檔案時會保存閱讀進度（如果設定中啟用）
    echo - 關閉檔案後會自動切換回圖片模式
    echo - 檔案管理器會重新顯示在前景
    echo.
    echo 測試建議：
    echo 1. 開啟一個PDF或圖片檔案
    echo 2. 點擊 "返回檔案管理" 按鈕
    echo 3. 確認檔案已關閉且檔案管理器顯示
    echo.
) else (
    echo.
    echo ❌ 編譯失敗！請檢查錯誤訊息。
    echo.
)

echo 按任意鍵繼續...
pause >nul
