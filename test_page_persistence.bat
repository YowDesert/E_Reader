@echo off
echo ==========================================
echo E_Reader 文字模式頁面保持功能編譯測試
echo ==========================================

REM 檢查 Java 環境
echo 檢查 Java 環境...
java -version 2>nul
if %errorlevel% neq 0 (
    echo [錯誤] 未找到 Java 環境，請安裝 Java
    pause
    exit /b 1
)
echo [成功] Java 環境正常

REM 檢查 Maven 環境
echo.
echo 檢查 Maven 環境...
mvn -version 2>nul
if %errorlevel% neq 0 (
    echo [警告] 未找到 Maven，請使用 IDE 運行程式
    goto :manual_compile
) else (
    echo [成功] Maven 環境正常
    goto :maven_compile
)

:maven_compile
echo.
echo ==========================================
echo 使用 Maven 編譯...
echo ==========================================
mvn clean compile
if %errorlevel% neq 0 (
    echo [錯誤] 編譯失敗
    pause
    exit /b 1
)
echo [成功] 編譯完成

echo.
echo ==========================================
echo 運行程式...
echo ==========================================
echo 即將啟動 E_Reader，請進行以下測試：
echo.
echo 1. 開啟一個 PDF 文件或包含多張圖片的資料夾
echo 2. 瀏覽到第 5 頁（或任意非第一頁）
echo 3. 點擊 "文字模式" 按鈕
echo 4. 等待文字提取完成，檢查是否保持在相同頁面
echo 5. 點擊 "圖片模式" 按鈕，檢查是否回到相同頁面
echo.
pause

mvn javafx:run
goto :end

:manual_compile
echo.
echo ==========================================
echo 手動編譯指示
echo ==========================================
echo 請使用以下方式之一來編譯和運行程式：
echo.
echo 方式一：使用 IntelliJ IDEA
echo 1. 用 IntelliJ IDEA 開啟專案
echo 2. 等待依賴項載入完成
echo 3. 找到 src/main/java/E_Reader/Main.java
echo 4. 右鍵選擇 "Run Main.main()"
echo.
echo 方式二：使用 Eclipse
echo 1. 用 Eclipse 開啟專案（Import as Maven Project）
echo 2. 等待依賴項載入完成
echo 3. 找到 src/main/java/E_Reader/Main.java
echo 4. 右鍵選擇 "Run As" -> "Java Application"
echo.
echo 方式三：使用命令列（需要手動設定 classpath）
echo 1. javac -cp "path/to/javafx/lib/*" src/main/java/E_Reader/*.java
echo 2. java -cp "path/to/javafx/lib/*;." --module-path "path/to/javafx/lib" --add-modules javafx.controls,javafx.fxml E_Reader.Main
echo.

:end
echo.
echo ==========================================
echo 測試功能檢查表
echo ==========================================
echo [ ] 程式正常啟動
echo [ ] 能夠開啟 PDF 或圖片資料夾
echo [ ] 能夠瀏覽到任意頁面
echo [ ] 在非第一頁點擊"文字模式"按鈕
echo [ ] 文字模式下保持在相同頁面
echo [ ] 切換回"圖片模式"時保持在相同頁面
echo [ ] 顯示正確的頁面通知訊息
echo [ ] 邊界情況處理正確（第一頁、最後一頁）
echo.
echo 如果所有測試項目都通過，則修改成功！
echo 詳細修改說明請參考 MODIFICATION_SUMMARY.md
echo.
pause