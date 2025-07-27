# E_Reader
電子閱讀器

GPT : 
我想製作一個應用程式
類似讓PDF 或是 圖片資料夾匯入
之後做成類似電子閱讀器的程式
最後如果可以的話就轉成平板能用程式

Ctrl+B - 書籤管理
Ctrl+N - 夜間模式切換
Ctrl+E - 護眼模式切換
Ctrl+F - 專注模式
Ctrl+A - 自動翻頁
Ctrl+R - 旋轉圖片

# E-Reader 漫畫＆PDF閱讀器 v3.0

一個功能豐富的JavaFX閱讀器應用程式，支援圖片、PDF檔案閱讀，以及OCR文字提取功能。

## 主要功能

### 📖 閱讀功能
- **多格式支援**: 支援 JPG、PNG、PDF 格式
- **雙模式顯示**: 圖片模式和文字模式無縫切換
- **智能翻頁**: 鍵盤、滑鼠、觸控多種翻頁方式
- **縮放功能**: 支援多種適應模式（適合寬度、高度、頁面、原始尺寸）

### 🎨 視覺體驗
- **多主題支援**: 淺色、深色、純黑、護眼、復古五種主題
- **響應式設計**: 自動適應不同螢幕尺寸
- **全螢幕模式**: 沉浸式閱讀體驗
- **專注模式**: 隱藏所有控制元件

### 📚 進階功能
- **書籤管理**: 支援添加、編輯、刪除書籤，可添加備註
- **OCR文字提取**: 從圖片和PDF中提取文字（需要Tesseract）
- **文字搜尋**: 在提取的文字中搜尋關鍵字並高亮顯示
- **閱讀統計**: 記錄閱讀時間和進度
- **自動翻頁**: 可自訂翻頁間隔的自動閱讀功能

### ⚙️ 個人化設定
- **護眼模式**: 降低藍光並定時提醒休息
- **夜間模式**: 自動根據時間切換主題
- **字體調整**: 文字模式下可調整字體大小和行距
- **記憶功能**: 記住上次閱讀位置和偏好設定

## 系統需求

- **Java**: JDK 17 或更高版本
- **JavaFX**: 17.0.2 或更高版本
- **記憶體**: 至少 512MB RAM
- **硬碟**: 至少 100MB 可用空間

### 可選依賴
- **Tesseract OCR**: 用於圖片文字識別功能
  - Windows: 下載安裝 Tesseract 並將 `tessdata` 資料夾放在應用程式目錄
  - 支援繁體中文和英文識別

## 安裝和運行

### 方法一：使用 Gradle 運行
```bash
# 克隆專案
git clone <repository-url>
cd E-Reader

# 運行應用程式
./gradlew run
```

### 方法二：構建 JAR 檔案
```bash
# 構建可執行 JAR
./gradlew fatJar

# 運行 JAR 檔案
java -jar build/libs/E-Reader-3.0.0-all.jar
```

### 方法三：直接運行（開發環境）
```bash
# 編譯專案
javac -cp "lib/*" --module-path /path/to/javafx/lib --add-modules javafx.controls,javafx.fxml,javafx.swing src/E_Reader/*.java

# 運行應用程式
java -cp "lib/*:src" --module-path /path/to/javafx/lib --add-modules javafx.controls,javafx.fxml,javafx.swing E_Reader.Launcher
```

## 使用說明

### 基本操作

1. **開啟檔案**
   - 點擊「📂 圖片資料夾」開啟圖片資料夾
   - 點擊「📄 PDF檔案」開啟PDF文件

2. **閱讀控制**
   - 使用方向鍵或滑鼠滾輪翻頁
   - 點擊圖片左側/右側區域翻頁
   - 使用下方控制列的翻頁按鈕

3. **模式切換**
   - 點擊「📖 文字模式」切換到文字閱讀模式
   - 在文字模式下可以搜尋和調整字體

### 快捷鍵

| 快捷鍵 | 功能 |
|--------|------|
| `←` / `Page Up` | 上一頁 |
| `→` / `Page Down` / `Space` | 下一頁 |
| `Home` | 第一頁 |
| `End` | 最後一頁 |
| `F11` | 全螢幕切換 |
| `Esc` | 退出全螢幕 |
| `Ctrl` + `+` | 放大 |
| `Ctrl` + `-` | 縮小 |
| `Ctrl` + `0` | 重設縮放 |
| `H` | 隱藏/顯示控制列 |
| `Ctrl` + `B` | 書籤管理 |
| `Ctrl` + `T` | 切換文字模式 |
| `Ctrl` + `/` | 搜尋文字 |
| `Ctrl` + `N` | 夜間模式 |
| `Ctrl` + `E` | 護眼模式 |
| `Ctrl` + `F` | 專注模式 |
| `Ctrl` + `A` | 自動翻頁 |
| `Ctrl` + `R` | 旋轉圖片 |

### 書籤功能

1. **新增書籤**
   - 開啟書籤管理對話框
   - 輸入書籤標題和備註
   - 點擊「新增書籤」

2. **管理書籤**
   - 在書籤列表中選擇書籤
   - 可以編輯、刪除或跳轉到書籤位置

### 文字模式

1. **啟用文字模式**
   - 開啟PDF或圖片檔案
   - 點擊「📖 文字模式」按鈕
   - 等待文字提取完成

2. **文字功能**
   - 使用「🔍 搜尋文字」搜尋關鍵字
   - 使用「A+」/「A-」調整字體大小
   - 使用「📏 行距」調整行間距

### 設定選項

進入「⚙️ 設定」可以調整：

- **一般設定**: 顯示模式、觸控導航、縮放級別等
- **外觀設定**: 主題模式、色彩配置等
- **護眼設定**: 護眼模式、夜間模式時間等

## 檔案結構

```
E-Reader/
├── src/
│   └── E_Reader/
│       ├── Main.java              # 主應用程式類
│       ├── Launcher.java          # 啟動器類
│       ├── ImageViewer.java       # 圖片檢視器
│       ├── TextRenderer.java      # 文字渲染器
│       ├── ImageLoader.java       # 圖片載入器
│       ├── PdfLoader.java         # PDF載入器
│       ├── TextExtractor.java     # 文字提取器
│       ├── BookmarkManager.java   # 書籤管理器
│       ├── SettingsPanel.java     # 設定面板
│       └── AlertHelper.java       # 警告對話框輔助
├── src/main/resources/
│   └── style.css                  # CSS樣式文件
├── tessdata/                      # Tesseract OCR 語言資料（可選）
├── build.gradle                   # Gradle 構建檔案
├── module-info.java              # Java 模組配置
└── README.md                     # 說明文件
```

## 疑難排解

### 常見問題

**Q: 應用程式無法啟動**
A: 請檢查：
- Java 版本是否為 17 或更高
- JavaFX 是否正確安裝
- 是否有足夠的系統記憶體

**Q: PDF 無法開啟**
A: 請確保：
- PDF 檔案沒有損壞
- PDF 檔案沒有密碼保護
- 有足夠的記憶體載入大型 PDF

**Q: OCR 功能無法使用**
A: 請檢查：
- Tesseract 是否正確安裝
- `tessdata` 資料夾是否存在且包含語言檔案
- 圖片品質是否足夠清晰

**Q: 中文字體顯示異常**
A: 請確保：
- 系統已安裝 Microsoft JhengHei 字體
- 系統語言設定正確
- Java 字體配置正常

### 效能優化

- **大型檔案**: 對於大型 PDF 或高解析度圖片，建議增加 JVM 記憶體
- **OCR 處理**: OCR 功能需要較多 CPU 資源，處理大量圖片時請耐心等待
- **快取機制**: 應用程式會自動快取已載入的內容以提升效能

## 開發說明

### 架構設計

應用程式採用模組化設計：

- **核心模組**: Main.java 負責整體協調
- **檢視模組**: ImageViewer 和 TextRenderer 負責內容顯示
- **資料模組**: 各種 Loader 類負責檔案載入
- **功能模組**: BookmarkManager、SettingsPanel 等提供專門功能
- **工具模組**: AlertHelper、TextExtractor 等提供輔助功能

### 擴展功能

要添加新功能，可以：

1. 在 Main.java 中添加新的按鈕和事件處理
2. 創建新的功能類別實現具體邏輯
3. 在 SettingsPanel 中添加相關設定選項
4. 更新 CSS 樣式以保持一致的外觀

### 貢獻指南

歡迎提交 Pull Request 或 Issue：

1. Fork 專案
2. 創建功能分支
3. 提交更改
4. 創建 Pull Request

## 授權條款

本專案採用 MIT 授權條款，詳見 LICENSE 檔案。

## 更新日誌

### v3.0.0 (目前版本)
- 新增文字模式和 OCR 功能
- 改進書籤管理系統
- 新增多種主題和護眼模式
- 優化響應式設計
- 改進使用者介面和操作體驗

### v2.x
- 基本的圖片和 PDF 閱讀功能
- 簡單的書籤系統
- 基本的設定選項

## 聯絡資訊

- 專案主頁: [GitHub Repository]
- 問題回報: [GitHub Issues]
- 功能建議: [GitHub Discussions]

---

感謝使用 E-Reader，希望這個應用程式能為您的閱讀體驗帶來便利！
