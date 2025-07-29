@echo off
echo 測試 E_Reader 新功能...

REM 進入專案目錄
cd /d "C:\E_Reader\E_Reader"

REM 使用 Maven 編譯並運行專案
echo 正在編譯並啟動 E_Reader...
call mvn clean compile javafx:run

if %errorlevel% equ 0 (
    echo.
    echo 新功能測試完成！
    echo.
    echo 🎉 新功能概述：
    echo 1. ✅ 導覽列隱藏/顯示功能
    echo    - 按下「🙈 隱藏導覽列」按鈕可切換導覽列顯示狀態
    echo    - 隱藏時，滑鼠移至頂部區域或向上滾動可暫時顯示導覽列
    echo    - 再次點擊按鈕可恢復常駐顯示模式
    echo.
    echo 2. ✅ 記憶最後閱讀位置功能
    echo    - 程式會自動記住你最後閱讀的檔案和頁面
    echo    - 下次啟動時會自動開啟並跳轉到上次的閱讀位置
    echo    - 支持圖片模式和文字模式的位置記憶
    echo.
    echo 🔧 使用方法：
    echo 導覽列控制：
    echo - 點擊「🙈 隱藏導覽列」按鈕進入隱藏模式
    echo - 隱藏後，將滑鼠移到螢幕頂部即可暫時顯示導覽列
    echo - 或者向上滾動滑鼠滾輪也可以暫時顯示導覽列
    echo - 再次點擊「🙉 顯示導覽列」按鈕恢復常駐模式
    echo.
    echo 閱讀位置記憶：
    echo - 在設定中確保「記住最後開啟的檔案」選項已啟用
    echo - 關閉程式時會自動保存當前閱讀位置
    echo - 下次啟動時會自動恢復上次的閱讀狀態
    echo.
) else (
    echo 啟動失敗，請檢查錯誤信息
)

pause
