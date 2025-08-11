@echo off
REM E-Reader 專案修正腳本 (Windows版本)
REM 此腳本會自動修正專案中的常見問題

echo 開始修正 E-Reader 專案...

REM 1. 檢查並創建缺失的目錄
echo 1. 檢查目錄結構...

if not exist "src\main\java\E_Reader" mkdir "src\main\java\E_Reader"
if not exist "src\main\java\E_Reader\core" mkdir "src\main\java\E_Reader\core"
if not exist "src\main\java\E_Reader\ui" mkdir "src\main\java\E_Reader\ui"
if not exist "src\main\java\E_Reader\ui\components" mkdir "src\main\java\E_Reader\ui\components"
if not exist "src\main\java\E_Reader\filemanager" mkdir "src\main\java\E_Reader\filemanager"
if not exist "src\main\java\E_Reader\settings" mkdir "src\main\java\E_Reader\settings"
if not exist "src\main\java\E_Reader\viewer" mkdir "src\main\java\E_Reader\viewer"
if not exist "src\main\java\E_Reader\utils" mkdir "src\main\java\E_Reader\utils"
if not exist "src\main\java\E_Reader\test" mkdir "src\main\java\E_Reader\test"
if not exist "src\main\resources" mkdir "src\main\resources"
if not exist "lib" mkdir "lib"

REM 2. 檢查 Java 版本
echo 2. 檢查 Java 版本...
java -version
javac -version

REM 3. 列出所有 Java 文件
echo 3. 準備編譯...
dir /s /b src\main\java\*.java > sources.txt

REM 4. 編譯專案
echo 4. 編譯專案...
javac -cp ".;lib\*" @sources.txt 2> compile_errors.txt

if %ERRORLEVEL% EQU 0 (
    echo ✓ 編譯成功！
    del sources.txt
    del compile_errors.txt
) else (
    echo ✗ 編譯失敗，錯誤信息：
    type compile_errors.txt
    echo.
    echo 常見解決方案：
    echo 1. 檢查 JavaFX 依賴庫是否正確配置
    echo 2. 確認所有必要的 jar 文件在 lib\ 目錄中
    echo 3. 檢查代碼中的 import 語句是否正確
    echo 4. 確認 Java 版本為 17 或以上
    del sources.txt
    pause
    exit /b 1
)

REM 5. 嘗試運行應用程式（測試模式）
echo 5. 測試應用程式啟動...
start /b java -cp ".;lib\*;src\main\java" ^
     --module-path="lib" ^
     --add-modules=javafx.controls,javafx.fxml,javafx.swing ^
     E_Reader.Main

timeout /t 3

echo.
echo 修正完成！
echo.
echo 如果仍有問題，請檢查：
echo 1. JavaFX 運行時是否正確安裝
echo 2. 所有依賴庫是否在 lib\ 目錄中：
echo    - javafx-controls.jar
echo    - javafx-fxml.jar  
echo    - javafx-swing.jar
echo    - javafx-base.jar
echo    - pdfbox.jar
echo    - tesseract4j.jar
echo    - 其他必要的依賴庫
echo 3. Tesseract OCR 是否正確安裝
echo 4. 資源文件（CSS、圖標）是否存在於 src\main\resources 中

pause
