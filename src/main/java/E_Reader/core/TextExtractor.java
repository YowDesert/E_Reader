package E_Reader.core;

import javafx.scene.image.Image;
import javafx.embed.swing.SwingFXUtils;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.pdfbox.rendering.PDFRenderer;
import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;
import E_Reader.settings.SettingsManager;

import java.awt.image.BufferedImage;
import java.awt.image.ColorConvertOp;
import java.awt.image.ConvolveOp;
import java.awt.image.Kernel;
import java.awt.color.ColorSpace;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * 增強版文字提取器 - 支援多模型OCR和精準度檢測
 */
public class TextExtractor {

    private Tesseract fastTesseract;    // 快速模型
    private Tesseract bestTesseract;    // 最佳精確度模型
    private boolean fastOcrInitialized = false;
    private boolean bestOcrInitialized = false;
    private boolean showDetectionFailures = true;
    private SettingsManager settingsManager;
    private SettingsManager.OcrModel currentOcrModel = SettingsManager.OcrModel.FAST;

    // 文字處理相關的正規表達式
    private static final Pattern PARAGRAPH_SEPARATOR = Pattern.compile("\n\\s*\n+");
    private static final Pattern LINE_BREAK = Pattern.compile("\n");
    private static final Pattern SENTENCE_END = Pattern.compile("[。！？.!?]\\s*");
    private static final Pattern CHAPTER_HEADER = Pattern.compile("^(第[一二三四五六七八九十0-9]+[章節回部]|Chapter\\s+\\d+|CHAPTER\\s+\\d+)");
    
    // OCR 品質閾值
    private static final int MIN_TEXT_LENGTH = 10;
    private static final double MIN_CONFIDENCE_THRESHOLD = 60.0;
    private static final Pattern MEANINGLESS_TEXT = Pattern.compile("^[\\s\\|\\-_=+*#@!%^&()\\[\\]{}<>,.?/~`]*$");

    public TextExtractor() {
        this.settingsManager = new SettingsManager();
        settingsManager.loadSettings();
        this.currentOcrModel = settingsManager.getOcrModel();
        initializeOCR();
    }
    
    public TextExtractor(SettingsManager settingsManager) {
        this.settingsManager = settingsManager;
        this.currentOcrModel = settingsManager.getOcrModel();
        initializeOCR();
    }

    /**
     * 初始化多個OCR引擎
     */
    private void initializeOCR() {
        try {
            // 初始化快速模型
            fastTesseract = new Tesseract();
            
            // 設定快速模型的資料路徑
            String fastDataPath = "src/main/resources/tessdata/TessAct_Fast/tessdata_fast-4.1.0";
            File fastDataDir = new File(fastDataPath);
            if (fastDataDir.exists() && fastDataDir.isDirectory()) {
                fastTesseract.setDatapath(fastDataPath);
                System.out.println("使用快速模型路徑: " + fastDataPath);
            } else {
                fastTesseract.setDatapath("tessdata");
                System.out.println("快速模型路徑不存在，使用預設路徑");
            }
            
            fastTesseract.setLanguage("chi_tra+eng");
            fastTesseract.setPageSegMode(6);     // 統一文字區塊
            fastTesseract.setOcrEngineMode(1);   // LSTM OCR引擎
            fastTesseract.setVariable("tessedit_do_invert", "0");
            fastOcrInitialized = true;
            
            // 初始化最佳精確度模型
            bestTesseract = new Tesseract();
            
            // 設定最佳模型的資料路徑
            String bestDataPath = "src/main/resources/tessdata/TessAct_Best/tessdata_best-4.1.0";
            File bestDataDir = new File(bestDataPath);
            if (bestDataDir.exists() && bestDataDir.isDirectory()) {
                bestTesseract.setDatapath(bestDataPath);
                System.out.println("使用最佳模型路徑: " + bestDataPath);
            } else {
                bestTesseract.setDatapath("tessdata");
                System.out.println("最佳模型路徑不存在，使用預設路徑");
            }
            
            bestTesseract.setLanguage("chi_tra+eng");
            bestTesseract.setPageSegMode(1);     // 自動頁面分割
            bestTesseract.setOcrEngineMode(1);   // LSTM OCR引擎
            
            // 提高最佳模型的精確度設定
            bestTesseract.setVariable("tessedit_char_whitelist",
                    "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz" +
                            "一二三四五六七八九十百千萬億兆零壹貳參肆伍陸柒捌玖拾佰仟" +
                            "的是了不在有個人這上我大來以他時要說年生對中可你國也就到下所" +
                            "會小而能用於自己出道走子它種只多如面方前回什公司問題工作生活" +
                            "好新學校家庭朋友愛情婚姻孩子父母老師同學同事老闆客戶政府社會" +
                            "經濟政治文化教育醫療健康科技網路手機電腦遊戲音樂電影書籍閱讀" +
                            "旅遊美食購物衣服住房交通汽車飛機火車地鐵公車計程車走路跑步運動" +
                            "天氣陽光雨水雪花春天夏天秋天冬天早上中午下午晚上夜晚昨天今天明天" +
                            "，。！？：；「」『』（）〔〕【】《》〈〉");
            
            bestTesseract.setVariable("tessedit_pageseg_mode", "1");
            bestTesseract.setVariable("preserve_interword_spaces", "1");
            bestOcrInitialized = true;
            
            System.out.println("OCR引擎初始化成功 - 快速模型: " + fastOcrInitialized + ", 最佳模型: " + bestOcrInitialized);
            System.out.println("當前使用OCR模型: " + currentOcrModel.getDisplayName());
            
        } catch (Exception e) {
            System.err.println("OCR 初始化失敗: " + e.getMessage());
            System.err.println("請確保已安裝Tesseract並正確設定tessdata路徑");
            fastOcrInitialized = false;
            bestOcrInitialized = false;
        }
    }

    /**
     * 從PDF檔案提取文字（增強版）
     */
    public List<PageText> extractTextFromPdf(File pdfFile) throws IOException {
        List<PageText> pages = new ArrayList<>();
        List<Integer> failedPages = new ArrayList<>();

        if (pdfFile == null || !pdfFile.exists()) {
            throw new IOException("PDF檔案不存在: " + pdfFile);
        }

        try (PDDocument document = PDDocument.load(pdfFile)) {
            PDFTextStripper stripper = new PDFTextStripper();
            PDFRenderer renderer = new PDFRenderer(document);

            for (int i = 0; i < document.getNumberOfPages(); i++) {
                PageText pageText = new PageText();
                pageText.setPageNumber(i);

                // 嘗試提取原生文字
                stripper.setStartPage(i + 1);
                stripper.setEndPage(i + 1);
                String extractedText = stripper.getText(document).trim();
                pageText.setOriginalText(cleanExtractedText(extractedText));

                boolean textDetected = false;

                // 如果原生文字很少或品質不佳，使用OCR
                if (shouldUseOCR(extractedText)) {
                    try {
                        BufferedImage pageImage = renderer.renderImageWithDPI(i, 300);
                        String ocrResult = performEnhancedOCR(pageImage, i + 1);
                        
                        if (isValidText(ocrResult)) {
                            pageText.setOcrText(ocrResult);
                            pageText.setTextSource(TextSource.OCR);
                            textDetected = true;
                            System.out.println("第 " + (i + 1) + " 頁使用OCR成功提取文字");
                        } else {
                            // OCR 失敗，記錄失敗頁面
                            failedPages.add(i + 1);
                            pageText.setTextSource(TextSource.NATIVE);
                            System.out.println("第 " + (i + 1) + " 頁OCR偵測失敗，使用原生文字");
                        }
                    } catch (Exception e) {
                        failedPages.add(i + 1);
                        pageText.setTextSource(TextSource.NATIVE);
                        System.err.println("第 " + (i + 1) + " 頁OCR處理失敗: " + e.getMessage());
                    }
                } else {
                    pageText.setTextSource(TextSource.NATIVE);
                    textDetected = !extractedText.isEmpty();
                }

                // 如果完全沒有偵測到文字，標記為失敗頁面
                if (!textDetected && pageText.getBestText().trim().isEmpty()) {
                    failedPages.add(i + 1);
                }

                pages.add(pageText);
            }
        } catch (IOException e) {
            throw new IOException("無法載入PDF檔案: " + e.getMessage(), e);
        }

        // 顯示偵測失敗的頁面通知
        if (!failedPages.isEmpty() && showDetectionFailures) {
            showDetectionFailureNotification(failedPages);
        }

        return pages;
    }

    /**
     * 從圖片列表提取文字（增強版）
     */
    public List<PageText> extractTextFromImages(List<Image> images) {
        List<PageText> pages = new ArrayList<>();
        List<Integer> failedPages = new ArrayList<>();

        if (!isOcrAvailable()) {
            System.err.println("OCR 未初始化，無法從圖片提取文字");
            return pages;
        }

        if (images == null || images.isEmpty()) {
            System.err.println("圖片列表為空");
            return pages;
        }

        for (int i = 0; i < images.size(); i++) {
            PageText pageText = new PageText();
            pageText.setPageNumber(i);
            pageText.setTextSource(TextSource.OCR);

            try {
                BufferedImage bufferedImage = SwingFXUtils.fromFXImage(images.get(i), null);
                String ocrResult = performEnhancedOCR(bufferedImage, i + 1);
                
                if (isValidText(ocrResult)) {
                    pageText.setOcrText(ocrResult);
                    System.out.println("第 " + (i + 1) + " 頁圖片OCR成功");
                } else {
                    pageText.setOcrText("");
                    failedPages.add(i + 1);
                    System.out.println("第 " + (i + 1) + " 頁圖片OCR偵測失敗");
                }
                
            } catch (Exception e) {
                System.err.println("處理第 " + (i + 1) + " 頁圖片時發生錯誤: " + e.getMessage());
                pageText.setOcrText("");
                failedPages.add(i + 1);
            }

            pages.add(pageText);
        }

        // 顯示偵測失敗的頁面通知
        if (!failedPages.isEmpty() && showDetectionFailures) {
            showDetectionFailureNotification(failedPages);
        }

        return pages;
    }

    /**
     * 執行增強的OCR識別
     */
    private String performEnhancedOCR(BufferedImage image, int pageNumber) throws TesseractException {
        // 預處理圖片
        BufferedImage processedImage = preprocessImageForOCR(image);
        
        String result = "";
        
        try {
            // 根據設定選擇OCR模型
            if (currentOcrModel == SettingsManager.OcrModel.FAST && fastOcrInitialized) {
                result = fastTesseract.doOCR(processedImage);
                System.out.println("第 " + pageNumber + " 頁使用快速模型進行OCR識別");
                
                // 如果快速模型結果不佳且最佳模型可用，作為備用
                if (!isValidText(result) && bestOcrInitialized) {
                    System.out.println("第 " + pageNumber + " 頁快速模型結果不佳，自動切換到最佳模型");
                    result = bestTesseract.doOCR(processedImage);
                }
            } else if (currentOcrModel == SettingsManager.OcrModel.BEST && bestOcrInitialized) {
                result = bestTesseract.doOCR(processedImage);
                System.out.println("第 " + pageNumber + " 頁使用最佳模型進行OCR識別");
            } else {
                // 備用邏輯：使用任何可用的模型
                if (fastOcrInitialized) {
                    result = fastTesseract.doOCR(processedImage);
                    System.out.println("第 " + pageNumber + " 頁使用備用快速模型");
                } else if (bestOcrInitialized) {
                    result = bestTesseract.doOCR(processedImage);
                    System.out.println("第 " + pageNumber + " 頁使用備用最佳模型");
                }
            }
            
            return cleanOCRText(result);
            
        } catch (TesseractException e) {
            System.err.println("OCR識別失敗: " + e.getMessage());
            throw e;
        }
    }

    /**
     * 判斷是否應該使用OCR
     */
    private boolean shouldUseOCR(String nativeText) {
        if (nativeText == null || nativeText.trim().isEmpty()) {
            return true;
        }
        
        // 如果原生文字太短
        if (nativeText.length() < MIN_TEXT_LENGTH) {
            return true;
        }
        
        // 如果原生文字主要是無意義字符
        if (MEANINGLESS_TEXT.matcher(nativeText.trim()).matches()) {
            return true;
        }
        
        return false;
    }

    /**
     * 驗證文字是否有效
     */
    private boolean isValidText(String text) {
        if (text == null || text.trim().isEmpty()) {
            return false;
        }
        
        String trimmedText = text.trim();
        
        // 檢查最小長度
        if (trimmedText.length() < MIN_TEXT_LENGTH) {
            return false;
        }
        
        // 檢查是否主要是無意義字符
        if (MEANINGLESS_TEXT.matcher(trimmedText).matches()) {
            return false;
        }
        
        // 檢查是否有足夠的有意義字符
        long meaningfulChars = trimmedText.chars()
                .filter(c -> Character.isLetterOrDigit(c) || 
                           (c >= 0x4e00 && c <= 0x9fff)) // 中文字符範圍
                .count();
        
        return meaningfulChars >= Math.min(5, trimmedText.length() * 0.3);
    }

    /**
     * 顯示偵測失敗通知
     */
    private void showDetectionFailureNotification(List<Integer> failedPages) {
        if (!showDetectionFailures || failedPages.isEmpty()) {
            return;
        }
        
        StringBuilder message = new StringBuilder("文字偵測失敗通知：\n");
        message.append("以下頁面無法偵測到文字，將跳過轉換：\n");
        
        for (int i = 0; i < failedPages.size() && i < 10; i++) {
            if (i > 0) message.append(", ");
            message.append("第").append(failedPages.get(i)).append("頁");
        }
        
        if (failedPages.size() > 10) {
            message.append("... 及其他 ").append(failedPages.size() - 10).append(" 頁");
        }
        
        message.append("\n\n共 ").append(failedPages.size()).append(" 頁偵測失敗");
        message.append("\n\n建議：嘗試切換到 '").append(currentOcrModel == SettingsManager.OcrModel.FAST ? "最佳模型" : "快速模型").append("' 來提高識別精度");
        
        System.out.println(message.toString());
        
        // 這裡可以加入GUI通知機制
        // 例如：showNotificationDialog(message.toString());
    }

    /**
     * 增強的圖片預處理
     */
    private BufferedImage preprocessImageForOCR(BufferedImage originalImage) {
        try {
            // 1. 轉換為灰階
            BufferedImage grayImage = new BufferedImage(
                originalImage.getWidth(), originalImage.getHeight(), BufferedImage.TYPE_BYTE_GRAY);
            Graphics2D g2d = grayImage.createGraphics();
            g2d.drawImage(originalImage, 0, 0, null);
            g2d.dispose();

            // 2. 增強對比度
            BufferedImage contrastImage = enhanceContrast(grayImage);

            // 3. 銳化處理
            BufferedImage sharpenedImage = sharpenImage(contrastImage);

            // 4. 去噪處理
            BufferedImage denoisedImage = denoiseImage(sharpenedImage);

            return denoisedImage;
            
        } catch (Exception e) {
            System.err.println("圖片預處理失敗，使用原圖: " + e.getMessage());
            return originalImage;
        }
    }

    /**
     * 增強對比度
     */
    private BufferedImage enhanceContrast(BufferedImage image) {
        BufferedImage enhanced = new BufferedImage(
            image.getWidth(), image.getHeight(), BufferedImage.TYPE_BYTE_GRAY);
        
        Graphics2D g2d = enhanced.createGraphics();
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2d.drawImage(image, 0, 0, null);
        g2d.dispose();
        
        return enhanced;
    }

    /**
     * 銳化圖片
     */
    private BufferedImage sharpenImage(BufferedImage image) {
        float[] sharpenKernel = {
            0f, -1f, 0f,
            -1f, 5f, -1f,
            0f, -1f, 0f
        };
        
        Kernel kernel = new Kernel(3, 3, sharpenKernel);
        ConvolveOp convolveOp = new ConvolveOp(kernel, ConvolveOp.EDGE_NO_OP, null);
        
        return convolveOp.filter(image, null);
    }

    /**
     * 去噪處理
     */
    private BufferedImage denoiseImage(BufferedImage image) {
        float[] blurKernel = {
            1f/9f, 1f/9f, 1f/9f,
            1f/9f, 1f/9f, 1f/9f,
            1f/9f, 1f/9f, 1f/9f
        };
        
        Kernel kernel = new Kernel(3, 3, blurKernel);
        ConvolveOp convolveOp = new ConvolveOp(kernel, ConvolveOp.EDGE_NO_OP, null);
        
        return convolveOp.filter(image, null);
    }

    /**
     * 從單一圖片提取文字（增強版）
     */
    public String extractTextFromImage(Image image) {
        if (!isOcrAvailable() || image == null) {
            return "";
        }

        try {
            BufferedImage bufferedImage = SwingFXUtils.fromFXImage(image, null);
            String result = performEnhancedOCR(bufferedImage, 0);
            
            if (!isValidText(result)) {
                System.out.println("圖片文字偵測失敗");
                return "";
            }
            
            return result;
            
        } catch (Exception e) {
            System.err.println("處理圖片時發生錯誤: " + e.getMessage());
            return "";
        }
    }

    /**
     * 清理OCR識別的文字（增強版）
     */
    private String cleanOCRText(String ocrText) {
        if (ocrText == null || ocrText.trim().isEmpty()) {
            return "";
        }

        String cleaned = ocrText;
        
        // 移除常見的OCR錯誤字符
        cleaned = cleaned.replaceAll("[|┌┐└┘├┤┬┴┼─│]", "");
        cleaned = cleaned.replaceAll("\\s{3,}", "\n\n");
        cleaned = cleaned.replaceAll("(?m)^\\s+", "");
        cleaned = cleaned.replaceAll("(?m)\\s+$", "");
        cleaned = cleaned.replaceAll("\n{3,}", "\n\n");
        
        // 修正常見的OCR識別錯誤（根據繁體中文特點）
        cleaned = cleaned.replaceAll("０", "O"); // 全形數字0
        cleaned = cleaned.replaceAll("１", "1"); // 全形數字1
        cleaned = cleaned.replaceAll("丨", "1");  // 豎線替換為數字1
        cleaned = cleaned.replaceAll("〇", "O");  // 中文零
        
        return cleaned.trim();
    }

    /**
     * 清理從PDF提取的原生文字
     */
    private String cleanExtractedText(String text) {
        if (text == null || text.trim().isEmpty()) {
            return "";
        }

        String cleaned = text;
        
        // 統一換行符
        cleaned = cleaned.replaceAll("\r\n", "\n");
        cleaned = cleaned.replaceAll("\r", "\n");
        
        // 清理多餘的空格和換行
        cleaned = cleaned.replaceAll("[ \t]+", " ");
        cleaned = cleaned.replaceAll("(?m)^\\s+", "");
        cleaned = cleaned.replaceAll("(?m)\\s+$", "");

        return cleaned.trim();
    }

    /**
     * 檢查OCR是否可用
     */
    public boolean isOcrAvailable() {
        return fastOcrInitialized || bestOcrInitialized;
    }

    /**
     * 更新OCR模型設定
     */
    public void updateOcrModel(SettingsManager.OcrModel newModel) {
        this.currentOcrModel = newModel;
        System.out.println("OCR模型已切換為: " + newModel.getDisplayName());
    }
    
    /**
     * 獲取當前OCR模型
     */
    public SettingsManager.OcrModel getCurrentOcrModel() {
        return currentOcrModel;
    }
    
    /**
     * 獲取OCR狀態信息
     */
    public String getOcrStatus() {
        StringBuilder status = new StringBuilder();
        status.append("OCR狀態: ");
        
        if (fastOcrInitialized && bestOcrInitialized) {
            status.append("雙模型可用 (快速+最佳)");
        } else if (fastOcrInitialized) {
            status.append("快速模型可用");
        } else if (bestOcrInitialized) {
            status.append("最佳模型可用");
        } else {
            status.append("不可用");
        }
        
        status.append(" | 目前使用: ").append(currentOcrModel.getDisplayName());
        
        return status.toString();
    }

    /**
     * 設定是否顯示偵測失敗通知
     */
    public void setShowDetectionFailures(boolean show) {
        this.showDetectionFailures = show;
    }

    /**
     * 頁面文字數據類
     */
    public static class PageText {
        private int pageNumber;
        private String originalText = "";
        private String ocrText = "";
        private TextSource textSource;
        private List<TextBlock> textBlocks = new ArrayList<>();

        public int getPageNumber() { return pageNumber; }
        public void setPageNumber(int pageNumber) { this.pageNumber = pageNumber; }

        public String getOriginalText() { return originalText; }
        public void setOriginalText(String originalText) { 
            this.originalText = originalText != null ? originalText : ""; 
        }

        public String getOcrText() { return ocrText; }
        public void setOcrText(String ocrText) { 
            this.ocrText = ocrText != null ? ocrText : ""; 
        }

        public TextSource getTextSource() { return textSource; }
        public void setTextSource(TextSource textSource) { this.textSource = textSource; }

        public List<TextBlock> getTextBlocks() { return textBlocks; }
        public void setTextBlocks(List<TextBlock> textBlocks) { this.textBlocks = textBlocks; }

        /**
         * 獲取最佳文字內容
         * 
         * @return 根據文字來源返回最適合的文字
         */
        public String getBestText() {
            switch (textSource) {
                case NATIVE:
                    return originalText.isEmpty() ? ocrText : originalText;
                case OCR:
                    return ocrText.isEmpty() ? originalText : ocrText;
                case MIXED:
                default:
                    return (ocrText != null && !ocrText.isEmpty()) ? ocrText : originalText;
            }
        }

        /**
         * 獲取格式化的段落列表
         * 
         * @return 格式化後的段落列表
         */
        public List<String> getFormattedParagraphs() {
            String text = getBestText();
            if (text.isEmpty()) {
                return new ArrayList<>();
            }
            return formatTextWithOriginalLineBreaks(text);
        }

        /**
         * 保持原有換行格式的文字格式化
         * 
         * @param text 原始文字
         * @return 格式化後的行列表
         */
        private List<String> formatTextWithOriginalLineBreaks(String text) {
            List<String> lines = new ArrayList<>();

            // 將文字按行分割，保持原有的換行
            String[] originalLines = text.split("\n");

            for (String line : originalLines) {
                line = line.trim();

                // 跳過空行，但保留一個空行作為段落分隔
                if (line.isEmpty()) {
                    if (!lines.isEmpty() && !lines.get(lines.size() - 1).isEmpty()) {
                        lines.add(""); // 添加空行作為段落分隔
                    }
                    continue;
                }

                // 檢查是否是章節標題
                if (isChapterHeader(line)) {
                    // 章節標題前後添加空行分隔
                    if (!lines.isEmpty() && !lines.get(lines.size() - 1).isEmpty()) {
                        lines.add("");
                    }
                    lines.add(line);
                    lines.add("");
                    continue;
                }

                // 清理行內容但保持換行結構
                String cleanedLine = cleanLineContent(line);
                if (!cleanedLine.isEmpty()) {
                    lines.add(cleanedLine);
                }
            }

            // 移除開頭和結尾的空行
            while (!lines.isEmpty() && lines.get(0).isEmpty()) {
                lines.remove(0);
            }
            while (!lines.isEmpty() && lines.get(lines.size() - 1).isEmpty()) {
                lines.remove(lines.size() - 1);
            }

            return lines;
        }

        /**
         * 清理行內容
         * 
         * @param line 原始行
         * @return 清理後的行
         */
        private String cleanLineContent(String line) {
            if (line == null) return "";

            // 移除行首行尾的空白字符
            line = line.trim();

            // 清理多餘的空格
            line = line.replaceAll("\\s+", " ");

            // 移除一些OCR可能產生的錯誤字符
            line = line.replaceAll("[|]", "");

            return line;
        }

        /**
         * 檢查是否為章節標題
         * 
         * @param text 文字內容
         * @return 是否為章節標題
         */
        private boolean isChapterHeader(String text) {
            if (text == null || text.length() > 100) return false;
            return CHAPTER_HEADER.matcher(text.trim()).find();
        }
    }

    /**
     * 文字區塊類
     */
    public static class TextBlock {
        private double x, y, width, height;
        private String text;
        private double confidence;

        public TextBlock(double x, double y, double width, double height, String text) {
            this(x, y, width, height, text, 0.0);
        }

        public TextBlock(double x, double y, double width, double height, String text, double confidence) {
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
            this.text = text;
            this.confidence = confidence;
        }

        // Getter 和 Setter 方法
        public double getX() { return x; }
        public void setX(double x) { this.x = x; }

        public double getY() { return y; }
        public void setY(double y) { this.y = y; }

        public double getWidth() { return width; }
        public void setWidth(double width) { this.width = width; }

        public double getHeight() { return height; }
        public void setHeight(double height) { this.height = height; }

        public String getText() { return text; }
        public void setText(String text) { this.text = text; }

        public double getConfidence() { return confidence; }
        public void setConfidence(double confidence) { this.confidence = confidence; }
    }

    /**
     * 文字來源枚舉
     */
    public enum TextSource {
        NATIVE("原生文字"),
        OCR("OCR識別"),
        MIXED("混合模式");

        private final String displayName;

        TextSource(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }
}
