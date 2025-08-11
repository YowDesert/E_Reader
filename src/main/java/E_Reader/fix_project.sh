#!/bin/bash

# E-Reader 專案修正腳本
# 此腳本會自動修正專案中的常見問題

echo "開始修正 E-Reader 專案..."

# 1. 檢查並創建缺失的目錄
echo "1. 檢查目錄結構..."

# 確保所有必要的目錄存在
mkdir -p "src/main/java/E_Reader"
mkdir -p "src/main/java/E_Reader/core"
mkdir -p "src/main/java/E_Reader/ui"
mkdir -p "src/main/java/E_Reader/ui/components"
mkdir -p "src/main/java/E_Reader/filemanager"
mkdir -p "src/main/java/E_Reader/settings"
mkdir -p "src/main/java/E_Reader/viewer"
mkdir -p "src/main/java/E_Reader/utils"
mkdir -p "src/main/java/E_Reader/test"
mkdir -p "src/main/resources"
mkdir -p "lib"

# 2. 檢查 Java 和 JavaFX 版本
echo "2. 檢查 Java 版本..."
java -version
javac -version

# 3. 編譯專案
echo "3. 嘗試編譯專案..."

# 設置類路徑（根據實際的庫路徑調整）
CLASSPATH=".:lib/*"

# 編譯所有 Java 文件
find src/main/java -name "*.java" > sources.txt
javac -cp "$CLASSPATH" @sources.txt 2> compile_errors.txt

if [ $? -eq 0 ]; then
    echo "✓ 編譯成功！"
    rm sources.txt compile_errors.txt
else
    echo "✗ 編譯失敗，錯誤信息："
    cat compile_errors.txt
    echo ""
    echo "常見解決方案："
    echo "1. 檢查 JavaFX 依賴庫是否正確配置"
    echo "2. 確認所有必要的 jar 文件在 lib/ 目錄中"
    echo "3. 檢查代碼中的 import 語句是否正確"
    rm sources.txt
    exit 1
fi

# 4. 運行測試
echo "4. 嘗試運行應用程式..."
java -cp "$CLASSPATH:src/main/java" \
     --module-path="lib" \
     --add-modules=javafx.controls,javafx.fxml,javafx.swing \
     E_Reader.Main 2> runtime_errors.txt &

APP_PID=$!
sleep 5

if kill -0 $APP_PID 2>/dev/null; then
    echo "✓ 應用程式啟動成功！"
    kill $APP_PID
    rm runtime_errors.txt
else
    echo "✗ 應用程式啟動失敗，錯誤信息："
    cat runtime_errors.txt
    rm runtime_errors.txt
fi

echo ""
echo "修正完成！"
echo ""
echo "如果仍有問題，請檢查："
echo "1. JavaFX 運行時是否正確安裝"
echo "2. 所有依賴庫是否在 lib/ 目錄中"
echo "3. Tesseract OCR 是否正確安裝"
echo "4. 資源文件（CSS、圖標）是否存在"
