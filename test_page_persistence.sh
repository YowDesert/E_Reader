#!/bin/bash

# E_Reader 文字模式頁面保持功能測試腳本

echo "=========================================="
echo "E_Reader 文字模式頁面保持功能測試"
echo "=========================================="

# 檢查 Java 環境
echo "檢查 Java 環境..."
if command -v java &> /dev/null; then
    java -version
    echo "✓ Java 環境正常"
else
    echo "✗ 未找到 Java 環境，請安裝 Java"
    exit 1
fi

# 檢查 Maven 環境（可選）
echo ""
echo "檢查 Maven 環境..."
if command -v mvn &> /dev/null; then
    mvn -version | head -1
    echo "✓ Maven 環境正常"
else
    echo "⚠ 未找到 Maven，將使用 IDE 運行"
fi

echo ""
echo "=========================================="
echo "修改文件檢查"
echo "=========================================="

# 檢查修改的文件是否存在
files_to_check=(
    "src/main/java/E_Reader/core/StateManager.java"
    "src/main/java/E_Reader/ui/MainController.java"
)

for file in "${files_to_check[@]}"
do
    if [ -f "$file" ]; then
        echo "✓ $file 存在"
    else
        echo "✗ $file 不存在"
    fi
done

echo ""
echo "=========================================="
echo "功能測試步驟"
echo "=========================================="

cat << EOF
手動測試步驟：

1. 編譯並運行程式：
   - 使用 Maven: mvn clean compile && mvn javafx:run
   - 或使用 IDE 直接運行 Main.java

2. 開啟測試文件：
   - 選擇一個多頁 PDF 文件
   - 或選擇包含多張圖片的資料夾

3. 基本功能測試：
   - 瀏覽到第 5 頁（或任意非第一頁）
   - 點擊"文字模式"按鈕
   - 等待文字提取完成
   - 檢查是否保持在第 5 頁
   - 點擊"圖片模式"按鈕
   - 檢查是否回到第 5 頁的圖片

4. 邊界測試：
   - 在最後一頁切換模式
   - 在第一頁切換模式
   - 測試頁數不匹配的情況

5. 預期結果：
   ✓ 切換到文字模式時保持在相同頁面
   ✓ 切換回圖片模式時保持在相同頁面
   ✓ 顯示切換成功的通知訊息
   ✓ 頁面邊界處理正確

EOF

echo ""
echo "=========================================="
echo "測試完成"
echo "=========================================="
echo "如果發現任何問題，請檢查控制台輸出或日誌文件"
echo "修改摘要請參考 MODIFICATION_SUMMARY.md"