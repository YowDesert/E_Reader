# E_Reader 程式碼重構說明

## 新的程式碼結構

程式碼已經重新組織，採用更清晰的模組化架構，讓代碼更易於維護和擴展。

### 📁 包結構說明

```
E_Reader/
├── Main.java                    # 主程式入口（簡化版）
├── Main_New.java               # 新版主程式（推薦使用）
├── Launcher.java               # 啟動器（保持不變）
│
├── 📁 ui/                      # 用戶界面層
│   ├── MainController.java    # 主控制器（協調各組件）
│   └── UIControlsFactory.java # UI控制元件工廠
│
├── 📁 core/                    # 核心業務邏輯層
│   ├── StateManager.java      # 狀態管理器
│   ├── TimerManager.java      # 計時器管理器
│   ├── ImageLoader.java       # 圖片載入器
│   ├── PdfLoader.java          # PDF載入器
│   ├── TextExtractor.java     # 文字提取器
│   └── BookmarkManager.java   # 書籤管理器
│
├── 📁 viewer/                  # 檢視器層
│   ├── ImageViewer.java       # 圖片檢視器
│   └── TextRenderer.java      # 文字渲染器
│
├── 📁 settings/                # 設定管理層
│   └── SettingsManager.java   # 設定管理器
│
└── 📁 utils/                   # 工具類層
    └── AlertHelper.java       # 提醒對話框工具
```

## 🔄 重構改進

### 1. **模組化設計**
- 將龐大的 Main.java 分解為多個專門的類別
- 每個類別都有明確的職責分工
- 遵循單一職責原則 (Single Responsibility Principle)

### 2. **更好的依賴管理**
- 使用依賴注入模式，減少類別間的耦合
- 通過介面定義明確的合約
- 便於單元測試和維護

### 3. **增強的錯誤處理**
- 統一的錯誤處理方式
- 更友善的錯誤訊息顯示
- 完善的異常處理機制

### 4. **優化的效能**
- 更有效率的計時器管理
- 優化的記憶體使用
- 改進的圖片和文字處理

## 🚀 使用新結構

### 啟動應用程式
推薦使用新的啟動方式：
```java
// 使用 Main_New.java 或直接使用 MainController
MainController controller = new MainController(primaryStage);
controller.initialize();
```

### 擴展功能
要添加新功能，請遵循以下原則：

1. **UI 相關** → 放在 `ui` 包
2. **核心邏輯** → 放在 `core` 包
3. **檢視功能** → 放在 `viewer` 包
4. **設定相關** → 放在 `settings` 包
5. **工具方法** → 放在 `utils` 包

## 📋 主要改進功能

### MainController
- 統一協調各個組件
- 管理應用程式生命週期
- 處理用戶交互事件

### StateManager
- 集中管理應用程式狀態
- 提供狀態變更通知
- 確保狀態一致性

### TimerManager
- 統一管理所有計時器
- 避免計時器洩漏
- 提供更精確的時間控制

### SettingsManager
- 完整的設定管理功能
- 支援主題切換
- 自動儲存和載入設定

### AlertHelper
- 統一的對話框風格
- 支援深色主題
- 更友善的錯誤提示

## 🔧 遷移指南

### 從舊版本遷移
1. 將 `Main.java` 重命名為 `Main_Old.java`（備份）
2. 將 `Main_New.java` 重命名為 `Main.java`
3. 更新 import 語句以使用新的包結構
4. 測試所有功能是否正常運作

### 開發建議
- 優先使用 MainController 來協調組件交互
- 通過 StateManager 管理應用程式狀態
- 使用 AlertHelper 顯示用戶訊息
- 遵循包結構的設計原則

## 🗂️ 檔案對應關係

### 舊檔案 → 新位置
```
Main.java → ui/MainController.java (主要邏輯)
ImageViewer.java → viewer/ImageViewer.java
TextRenderer.java → viewer/TextRenderer.java
SettingsPanel.java → settings/SettingsManager.java
BookmarkManager.java → core/BookmarkManager.java
ImageLoader.java → core/ImageLoader.java
PdfLoader.java → core/PdfLoader.java
TextExtractor.java → core/TextExtractor.java
AlertHelper.java → utils/AlertHelper.java
```

## 🔍 程式碼品質改進

### 1. **可讀性提升**
- 更清晰的類別命名
- 完整的方法註釋
- 邏輯分組更明確

### 2. **可維護性增強**
- 減少代碼重複
- 統一的設計模式
- 更好的錯誤處理

### 3. **可擴展性改善**
- 模組化設計便於添加新功能
- 介面導向的設計
- 鬆耦合的組件關係

## 📝 TODO 清單

### 即將完成的功能
- [ ] 完善 UIControlsFactory 中的待實作方法
- [ ] 添加更多的主題選項
- [ ] 實作設定面板的對話框
- [ ] 增加搜尋和文字處理功能
- [ ] 優化記憶體使用和效能

### 長期規劃
- [ ] 添加插件系統
- [ ] 支援更多檔案格式
- [ ] 實作雲端同步功能
- [ ] 添加筆記和標註功能
- [ ] 支援多語言介面

## 🛠️ 開發工具建議

### IDE 設定
- 使用 IntelliJ IDEA 或 Eclipse
- 配置 JavaFX Scene Builder
- 設定代碼格式化規則

### 建置工具
- Maven 或 Gradle
- 自動化測試框架
- 代碼品質檢查工具

## 📞 支援和貢獻

如果您在使用新結構時遇到問題，或有改進建議，請：

1. 檢查這份文檔的說明
2. 查看代碼中的註釋
3. 測試功能是否正常
4. 提供反饋和建議

---

**重構完成日期**: 2025年1月
**版本**: v3.1 Enhanced
**主要改進**: 模組化架構、效能優化、可維護性提升
