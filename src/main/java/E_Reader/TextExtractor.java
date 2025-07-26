package E_Reader;

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

public class TextExtractor {

    private Tesseract tesseract;
    private boolean ocrInitialized = false;

    public TextExtractor() {
        initializeOCR();
    }

    private void initializeOCR() {
        try {
            tesseract = new Tesseract();
            // 設定 Tesseract 數據路徑（需要下載語言包）
            tesseract.setDatapath("tessdata"); // 語言包路徑
            tesseract.setLanguage("chi_tra+eng"); // 繁體中文 + 英文
            tesseract.setPageSegMode(1); // 自動頁面分割
            tesseract.setOcrEngineMode(1); // 使用 LSTM OCR 引擎
            ocrInitialized = true;
        } catch (Exception e) {
            System.err.println("OCR 初始化失敗: " + e.getMessage());
            System.err.println("請確保已安裝 Tesseract 並下載中文語言包");
            ocrInitialized = false;
        }
    }

    /**
     * 從PDF提取文字（原生文字）
     */
    public List<PageText> extractTextFromPdf(File pdfFile) throws IOException {
        List<PageText> pages = new ArrayList<>();

        try (PDDocument document = PDDocument.load(pdfFile)) {
            PDFTextStripper stripper = new PDFTextStripper();
            PDFRenderer renderer = new PDFRenderer(document);

            for (int i = 0; i < document.getNumberOfPages(); i++) {
                // 嘗試提取原生文字
                stripper.setStartPage(i + 1);
                stripper.setEndPage(i + 1);
                String extractedText = stripper.getText(document).trim();

                PageText pageText = new PageText();
                pageText.setPageNumber(i);
                pageText.setOriginalText(extractedText);

                // 如果原生文字提取失敗或文字很少，使用OCR
                if (extractedText.length() < 50 && ocrInitialized) {
                    try {
                        BufferedImage pageImage = renderer.renderImageWithDPI(i, 300);
                        String ocrText = tesseract.doOCR(pageImage);
                        pageText.setOcrText(ocrText);
                        pageText.setTextSource(TextSource.OCR);
                    } catch (TesseractException e) {
                        System.err.println("OCR 處理第 " + (i + 1) + " 頁失敗: " + e.getMessage());
                        pageText.setTextSource(TextSource.NATIVE);
                    }
                } else {
                    pageText.setTextSource(TextSource.NATIVE);
                }

                pages.add(pageText);
            }
        }

        return pages;
    }

    /**
     * 從圖片提取文字（OCR）
     */
    public List<PageText> extractTextFromImages(List<Image> images) {
        List<PageText> pages = new ArrayList<>();

        if (!ocrInitialized) {
            System.err.println("OCR 未初始化，無法從圖片提取文字");
            return pages;
        }

        for (int i = 0; i < images.size(); i++) {
            PageText pageText = new PageText();
            pageText.setPageNumber(i);
            pageText.setTextSource(TextSource.OCR);

            try {
                // 將JavaFX Image轉換為BufferedImage
                BufferedImage bufferedImage = SwingFXUtils.fromFXImage(images.get(i), null);

                // 使用OCR提取文字
                String ocrText = tesseract.doOCR(bufferedImage);
                pageText.setOcrText(ocrText);

            } catch (TesseractException e) {
                System.err.println("OCR 處理第 " + (i + 1) + " 頁圖片失敗: " + e.getMessage());
                pageText.setOcrText("");
            }

            pages.add(pageText);
        }

        return pages;
    }

    /**
     * 從單張圖片提取文字
     */
    public String extractTextFromImage(Image image) {
        if (!ocrInitialized) {
            return "";
        }

        try {
            BufferedImage bufferedImage = SwingFXUtils.fromFXImage(image, null);
            return tesseract.doOCR(bufferedImage);
        } catch (TesseractException e) {
            System.err.println("OCR 處理圖片失敗: " + e.getMessage());
            return "";
        }
    }

    public boolean isOcrAvailable() {
        return ocrInitialized;
    }

    /**
     * 頁面文字數據類
     */
    public static class PageText {
        private int pageNumber;
        private String originalText = ""; // PDF原生文字
        private String ocrText = "";      // OCR提取的文字
        private TextSource textSource;
        private List<TextBlock> textBlocks = new ArrayList<>();

        // Getters and Setters
        public int getPageNumber() { return pageNumber; }
        public void setPageNumber(int pageNumber) { this.pageNumber = pageNumber; }

        public String getOriginalText() { return originalText; }
        public void setOriginalText(String originalText) { this.originalText = originalText; }

        public String getOcrText() { return ocrText; }
        public void setOcrText(String ocrText) { this.ocrText = ocrText; }

        public TextSource getTextSource() { return textSource; }
        public void setTextSource(TextSource textSource) { this.textSource = textSource; }

        public List<TextBlock> getTextBlocks() { return textBlocks; }
        public void setTextBlocks(List<TextBlock> textBlocks) { this.textBlocks = textBlocks; }

        /**
         * 獲取最佳文字內容
         */
        public String getBestText() {
            switch (textSource) {
                case NATIVE:
                    return originalText.isEmpty() ? ocrText : originalText;
                case OCR:
                    return ocrText.isEmpty() ? originalText : ocrText;
                default:
                    return originalText.isEmpty() ? ocrText : originalText;
            }
        }

        /**
         * 格式化文字為段落
         */
        public List<String> getFormattedParagraphs() {
            String text = getBestText();
            if (text.isEmpty()) {
                return new ArrayList<>();
            }

            List<String> paragraphs = new ArrayList<>();
            String[] lines = text.split("\n");
            StringBuilder currentParagraph = new StringBuilder();

            for (String line : lines) {
                line = line.trim();
                if (line.isEmpty()) {
                    if (currentParagraph.length() > 0) {
                        paragraphs.add(currentParagraph.toString().trim());
                        currentParagraph = new StringBuilder();
                    }
                } else {
                    if (currentParagraph.length() > 0) {
                        currentParagraph.append(" ");
                    }
                    currentParagraph.append(line);
                }
            }

            if (currentParagraph.length() > 0) {
                paragraphs.add(currentParagraph.toString().trim());
            }

            return paragraphs;
        }
    }

    /**
     * 文字區塊類（用於版面分析）
     */
    public static class TextBlock {
        private double x, y, width, height;
        private String text;
        private double confidence;

        public TextBlock(double x, double y, double width, double height, String text) {
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
            this.text = text;
        }

        // Getters and Setters
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