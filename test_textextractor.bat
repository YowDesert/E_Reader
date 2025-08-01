@echo off
chcp 65001 > nul
echo ====================================
echo   TextExtractor 增強版 編譯測試
echo ====================================
echo.

echo [1/3] 清理舊的編譯檔案...
if exist "target\classes" rmdir /s /q "target\classes"
mkdir "target\classes" 2>nul

echo [2/3] 編譯程式碼...
javac -cp "lib\*" -d "target\classes" -encoding UTF-8 ^
  "src\main\java\E_Reader\core\TextExtractor.java" ^
  "src\main\java\E_Reader\test\TextExtractorTest.java"

if %errorlevel% neq 0 (
    echo.
    echo 編譯失敗！請檢查程式碼是否有錯誤。
    pause
    exit /b 1
)

echo [3/3] 編譯成功！

echo.
echo ====================================
echo   可用的執行選項：
echo ====================================
echo 1. 執行測試程式
echo 2. 檢查模型檔案狀態
echo 3. 只編譯，不執行
echo.

set /p choice=請選擇 (1-3): 

if "%choice%"=="1" (
    echo.
    echo 啟動測試程式...
    echo.
    java -cp "target\classes;lib\*" E_Reader.test.TextExtractorTest
) else if "%choice%"=="2" (
    echo.
    echo ====================================
    echo   模型檔案狀態檢查
    echo ====================================
    echo.
    if exist "tessdata" (
        echo tessdata 資料夾: 存在
        echo.
        echo 檢查模型檔案:
        if exist "tessdata\chi_tra.traineddata" (
            echo [✓] chi_tra.traineddata - 繁體中文標準模型
        ) else (
            echo [✗] chi_tra.traineddata - 繁體中文標準模型 (缺少)
        )
        
        if exist "tessdata\chi_tra_fast.traineddata" (
            echo [✓] chi_tra_fast.traineddata - 快速模型
        ) else (
            echo [✗] chi_tra_fast.traineddata - 快速模型 (缺少)
        )
        
        if exist "tessdata\chi_tra_best.traineddata" (
            echo [✓] chi_tra_best.traineddata - 最佳模型
        ) else (
            echo [✗] chi_tra_best.traineddata - 最佳模型 (缺少)
        )
        
        if exist "tessdata\eng.traineddata" (
            echo [✓] eng.traineddata - 英文模型
        ) else (
            echo [✗] eng.traineddata - 英文模型 (缺少)
        )
        
        echo.
        echo 注意：請將您的 fast 和 best 模型檔案
        echo 重新命名後放置到 tessdata 資料夾中：
        echo - 快速模型 → chi_tra_fast.traineddata
        echo - 最佳模型 → chi_tra_best.traineddata
        
    ) else (
        echo [✗] tessdata 資料夾不存在
        echo 請先建立 tessdata 資料夾並放置模型檔案
    )
) else if "%choice%"=="3" (
    echo.
    echo 編譯完成，程式檔案位於 target\classes 目錄
) else (
    echo 無效選擇
)

echo.
pause
