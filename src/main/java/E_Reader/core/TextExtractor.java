package E_Reader.core;

import javafx.scene.image.Image;
import javafx.embed.swing.SwingFXUtils;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.pdfbox.rendering.PDFRenderer;
import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * 文字提取器 - 負責從PDF和圖片中提取文字內容
 */
public class TextExtractor {

    private Tesseract tesseract;
    private boolean ocrInitialized = false;

    // 文字處理相關的正規表達式
    private static final Pattern PARAGRAPH_SEPARATOR = Pattern.compile("\n\\s*\n+");
    private static final Pattern LINE_BREAK = Pattern.compile("\n");
    private static final Pattern SENTENCE_END = Pattern.compile("[。！？.!?]\\s*");
    private static final Pattern CHAPTER_HEADER = Pattern.compile("^(第[一二三四五六七八九十0-9]+[章節回部]|Chapter\\s+\\d+|CHAPTER\\s+\\d+)");

    public TextExtractor() {
        initializeOCR();
    }

    /**
     * 初始化OCR引擎
     */
    private void initializeOCR() {
        try {
            tesseract = new Tesseract();
            tesseract.setDatapath("tessdata");
            tesseract.setLanguage("chi_tra+eng");  // 繁體中文 + 英文
            tesseract.setPageSegMode(1);  // 自動頁面分割
            tesseract.setOcrEngineMode(1);  // LSTM OCR引擎
            
            // 設定字符白名單以提高準確性
            tesseract.setVariable("tessedit_char_whitelist",
                    "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz" +
                            "一二三四五六七八九十百千萬億兆" +
                            "的是了不在有個人這上我大來以他時要說年生對中可你國也就到下所" +
                            "會小而能用於自己出道走子它種只多如面方前回什公司問題工作生活" +
                            "好了在是的有不了就是這個一是不的有了就這個一是不的有了");

            ocrInitialized = true;
            System.out.println("OCR引擎初始化成功");
        } catch (Exception e) {
            System.err.println("OCR 初始化失敗: " + e.getMessage());
            System.err.println("請確保已安裝Tesseract並正確設定tessdata路徑");
            ocrInitialized = false;
        }
    }

    /**
     * 從PDF檔案提取文字
     * 
     * @param pdfFile PDF檔案
     * @return 頁面文字列表
     * @throws IOException 如果無法讀取PDF檔案
     */
    public List<PageText> extractTextFromPdf(File pdfFile) throws IOException {
        List<PageText> pages = new ArrayList<>();

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

                // 如果原生文字很少，嘗試使用OCR
                if (extractedText.length() < 50 && ocrInitialized) {
                    try {
                        BufferedImage pageImage = renderer.renderImageWithDPI(i, 300);
                        BufferedImage processedImage = preprocessImageForOCR(pageImage);
                        String ocrText = tesseract.doOCR(processedImage);
                        pageText.setOcrText(cleanOCRText(ocrText));
                        pageText.setTextSource(TextSource.OCR);
                        System.out.println("第 " + (i + 1) + " 頁使用OCR提取文字");
                    } catch (TesseractException e) {
                        System.err.println("OCR 處理第 " + (i + 1) + " 頁失敗: " + e.getMessage());
                        pageText.setTextSource(TextSource.NATIVE);
                    }
                } else {
                    pageText.setTextSource(TextSource.NATIVE);
                }

                pages.add(pageText);
            }
        } catch (IOException e) {
            throw new IOException("無法載入PDF檔案: " + e.getMessage(), e);
        }

        return pages;
    }

    /**
     * 從圖片列表提取文字
     * 
     * @param images 圖片列表
     * @return 頁面文字列表
     */
    public List<PageText> extractTextFromImages(List<Image> images) {
        List<PageText> pages = new ArrayList<>();

        if (!ocrInitialized) {
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
                BufferedImage processedImage = preprocessImageForOCR(bufferedImage);
                String ocrText = tesseract.doOCR(processedImage);
                pageText.setOcrText(cleanOCRText(ocrText));
                
                System.out.println("第 " + (i + 1) + " 頁圖片OCR完成");
            } catch (TesseractException e) {
                System.err.println("OCR 處理第 " + (i + 1) + " 頁圖片失敗: " + e.getMessage());
                pageText.setOcrText("");
            } catch (Exception e) {
                System.err.println("處理第 " + (i + 1) + " 頁圖片時發生錯誤: " + e.getMessage());
                pageText.setOcrText("");
            }

            pages.add(pageText);
        }

        return pages;
    }

    /**
     * 從單一圖片提取文字
     * 
     * @param image 圖片
     * @return 提取的文字
     */
    public String extractTextFromImage(Image image) {
        if (!ocrInitialized || image == null) {
            return "";
        }

        try {
            BufferedImage bufferedImage = SwingFXUtils.fromFXImage(image, null);
            BufferedImage processedImage = preprocessImageForOCR(bufferedImage);
            String ocrText = tesseract.doOCR(processedImage);
            return cleanOCRText(ocrText);
        } catch (TesseractException e) {
            System.err.println("OCR 處理圖片失敗: " + e.getMessage());
            return "";
        } catch (Exception e) {
            System.err.println("處理圖片時發生錯誤: " + e.getMessage());
            return "";
        }
    }

    /**
     * 預處理圖片以提高OCR準確度
     * 
     * @param originalImage 原始圖片
     * @return 處理後的圖片
     */
    private BufferedImage preprocessImageForOCR(BufferedImage originalImage) {
        // 這裡可以加入圖片預處理邏輯
        // 例如：銳化、對比度調整、去噪等
        // 目前直接返回原圖
        return originalImage;
    }

    /**
     * 清理OCR識別的文字
     * 
     * @param ocrText OCR原始文字
     * @return 清理後的文字
     */
    private String cleanOCRText(String ocrText) {
        if (ocrText == null || ocrText.trim().isEmpty()) {
            return "";
        }

        String cleaned = ocrText;
        
        // 移除常見的OCR錯誤字符
        cleaned = cleaned.replaceAll("[|]", "");
        cleaned = cleaned.replaceAll("\\s{3,}", "\n\n");
        cleaned = cleaned.replaceAll("(?m)^\\s+", "");
        cleaned = cleaned.replaceAll("(?m)\\s+$", "");
        cleaned = cleaned.replaceAll("\n{3,}", "\n\n");
        
        // 修正常見的OCR識別錯誤
        cleaned = cleaned.replaceAll("0", "O");  // 數字0替換為字母O
        cleaned = cleaned.replaceAll("丨", "1");  // 豎線替換為數字1
        
        return cleaned.trim();
    }

    /**
     * 清理從PDF提取的原生文字
     * 
     * @param text 原生文字
     * @return 清理後的文字
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
     * 
     * @return OCR是否已初始化並可用
     */
    public boolean isOcrAvailable() {
        return ocrInitialized;
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
