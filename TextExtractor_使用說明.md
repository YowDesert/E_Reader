# TextExtractor 增強版使用說明

## 功能改進

這個增強版的 TextExtractor 包含以下新功能：

### 1. 雙模型 OCR 支援
- **快速模型 (Fast Model)**: 優先使用，速度較快
- **最佳模型 (Best Model)**: 當快速模型結果不佳時自動切換，精確度更高

### 2. 智能文字偵測
- 自動判斷是否需要使用 OCR
- 驗證文字品質，過濾無意義內容
- 支援中文和英文混合文字識別

### 3. 偵測失敗處理
- 自動識別無法偵測文字的頁面
- 顯示失敗頁面通知
- 跳過失敗頁面的轉換

### 4. 圖片預處理增強
- 灰階轉換
- 對比度增強
- 銳化處理
- 去噪處理

### 5. OCR 品質檢測
- 最小文字長度檢測
- 有意義字符比例檢測
- 無意義字符過濾

## 安裝模型檔案

### 步驟 1: 準備 tessdata 資料夾
已經為您建立了 `tessdata` 資料夾，位於：
```
C:\E_Reader\E_Reader\tessdata\
```

### 步驟 2: 放置模型檔案
將您的 fast 和 best 模型檔案放置到 tessdata 資料夾中：

1. **快速模型**: 將檔案重新命名為 `chi_tra_fast.traineddata`
2. **最佳模型**: 將檔案重新命名為 `chi_tra_best.traineddata`

### 步驟 3: 標準模型（必需）
確保您有基本的繁體中文模型：
- `chi_tra.traineddata` - 繁體中文模型
- `eng.traineddata` - 英文模型

您可以從官方下載：https://github.com/tesseract-ocr/tessdata

## 使用方式

### 基本使用
```java
TextExtractor extractor = new TextExtractor();

// 檢查 OCR 狀態
System.out.println(extractor.getOcrStatus());
```

### PDF 文字提取
```java
File pdfFile = new File("document.pdf");
List<PageText> pages = extractor.extractTextFromPdf(pdfFile);

for (PageText page : pages) {
    System.out.println("第 " + (page.getPageNumber() + 1) + " 頁:");
    System.out.println("文字來源: " + page.getTextSource().getDisplayName());
    System.out.println("內容: " + page.getBestText());
}
```

### 圖片文字提取
```java
List<Image> images = loadImages(); // 您的圖片載入方法
List<PageText> pages = extractor.extractTextFromImages(images);
```

### 設定選項
```java
// 關閉偵測失敗通知
extractor.setShowDetectionFailures(false);
```

## 工作流程

### 1. 初始化階段
- 檢查並載入可用的模型檔案
- 初始化快速和最佳模型
- 顯示模型狀態

### 2. 文字提取階段
- **PDF**: 先嘗試原生文字提取
- **判斷**: 檢查原生文字品質
- **OCR**: 如需要，使用雙模型OCR
- **驗證**: 檢查OCR結果品質

### 3. 品質控制
- 文字長度檢查（最少10字符）
- 有意義字符比例檢查（至少30%）
- 過濾純符號內容

### 4. 失敗處理
- 記錄無法偵測的頁面
- 顯示失敗通知
- 跳過失敗頁面的後續處理

## 模型切換邏輯

```
開始 -> 有快速模型? -> 使用快速模型 -> 結果好?
                                    |
                                    否 -> 有最佳模型? -> 使用最佳模型
                                    |
                                    是 -> 返回結果
```

## 偵測失敗通知格式

```
文字偵測失敗通知：
以下頁面無法偵測到文字，將跳過轉換：
第3頁, 第7頁, 第12頁, 第15頁

共 4 頁偵測失敗
```

## 效能優化建議

### 1. 模型選擇
- 如果速度優先：只使用快速模型
- 如果精確度優先：只使用最佳模型
- 平衡模式：使用雙模型（推薦）

### 2. 圖片品質
- 使用高解析度圖片（300 DPI以上）
- 確保文字對比度足夠
- 避免過度模糊的圖片

### 3. 批次處理
- 對大量頁面進行批次處理
- 設定合理的執行緒數量

## 疑難排解

### 1. OCR 初始化失敗
- 檢查 Tesseract 是否正確安裝
- 確認 tessdata 路徑正確
- 檢查模型檔案是否存在

### 2. 偵測品質不佳
- 檢查圖片品質
- 調整預處理參數
- 嘗試不同的模型

### 3. 效能問題
- 降低圖片解析度
- 關閉不必要的預處理
- 使用單一模型

## 注意事項

1. **模型檔案**: 確保模型檔案完整且未損壞
2. **記憶體使用**: OCR 處理會消耗較多記憶體
3. **處理時間**: 最佳模型比快速模型慢
4. **相容性**: 確保 Tesseract 版本相容

## 進階設定

如需要更進一步的自訂，可以修改以下參數：

```java
// 在 initializeOCR() 方法中
tesseract.setVariable("tessedit_pageseg_mode", "1");
tesseract.setVariable("preserve_interword_spaces", "1");
```

常用的 Tesseract 參數：
- `tessedit_pageseg_mode`: 頁面分割模式
- `tessedit_ocr_engine_mode`: OCR 引擎模式
- `tessedit_char_whitelist`: 字符白名單
- `preserve_interword_spaces`: 保留詞間空格
