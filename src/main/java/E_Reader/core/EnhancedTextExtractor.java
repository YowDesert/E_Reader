package E_Reader.core;

import javafx.scene.image.Image;
import javafx.embed.swing.SwingFXUtils;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.pdfbox.rendering.PDFRenderer;
import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;
import E_Reader.settings.SettingsManager;
import E_Reader.core.LatexOCRIntegrator;

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
 * 增強版文字提取器 - 支援多模型OCR、LaTeX-OCR和精準度檢測
 */
public class EnhancedTextExtractor extends TextExtractor {

    private LatexOCRIntegrator latexOCR;
    private boolean latexOcrEnabled = false;
    
    // LaTeX數學公式檢測模式
    public enum LatexDetectionMode {
        DISABLED("停用"),
        AUTO("自動檢測"),
        FORCE("強制使用");
        
        private final String displayName;
        
        LatexDetectionMode(String displayName) {
            this.displayName = displayName;
        }
        
        public String getDisplayName() {
            return displayName;
        }
    }
    
    private LatexDetectionMode latexDetectionMode = LatexDetectionMode.AUTO;
    
    // 數學公式檢測的正規表達式
    private static final Pattern MATH_FORMULA_INDICATORS = Pattern.compile(
        ".*[\\+\\-\\*\\/\\=\\(\\)\\[\\]\\{\\}\\^\\$∑∏∫√π∞≈≠≤≥±×÷∂∇∈∉⊂⊃∩∪∅].*"
    );
    
    public EnhancedTextExtractor() {
        super();
        initializeLatexOCR();
    }
    
    public EnhancedTextExtractor(SettingsManager settingsManager) {
        super(settingsManager);
        initializeLatexOCR();
    }
    
    /**
     * 初始化 LaTeX-OCR
     */
    private void initializeLatexOCR() {
        try {
            latexOCR = new LatexOCRIntegrator();
            latexOcrEnabled = latexOCR.initialize();
            
            if (latexOcrEnabled) {
                System.out.println("LaTeX-OCR 初始化成功");
                System.out.println("LaTeX-OCR 版本資訊:");
                System.out.println(latexOCR.getVersionInfo());
            } else {
                System.err.println("LaTeX-OCR 初始化失敗: " + latexOCR.getLastError());
            }
        } catch (Exception e) {
            System.err.println("LaTeX-OCR 初始化過程中發生錯誤: " + e.getMessage());
            latexOcrEnabled = false;
        }
    }
    
    /**
     * 從PDF檔案提取文字（包含LaTeX公式識別）
     */
    @Override
    public List<PageText> extractTextFromPdf(File pdfFile) throws IOException {
        List<PageText> pages = super.extractTextFromPdf(pdfFile);
        
        // 如果 LaTeX-OCR 啟用，對每頁進行數學公式識別
        if (latexOcrEnabled && latexDetectionMode != LatexDetectionMode.DISABLED) {
            enhancePagesWithLatex(pages, pdfFile);
        }
        
        return pages;
    }
    
    /**
     * 從圖片列表提取文字（包含LaTeX公式識別）
     */
    @Override
    public List<PageText> extractTextFromImages(List<Image> images) {
        List<PageText> pages = super.extractTextFromImages(images);
        
        // 如果 LaTeX-OCR 啟用，對每頁進行數學公式識別
        if (latexOcrEnabled && latexDetectionMode != LatexDetectionMode.DISABLED) {
            enhancePagesWithLatexFromImages(pages, images);
        }
        
        return pages;
    }
    
    /**
     * 增強頁面內容，加入LaTeX公式識別（從PDF）
     */
    private void enhancePagesWithLatex(List<PageText> pages, File pdfFile) {
        if (pages == null || pages.isEmpty()) {
            return;
        }
        
        System.out.println("開始對PDF頁面進行LaTeX公式識別...");
        
        try (PDDocument document = PDDocument.load(pdfFile)) {
            PDFRenderer renderer = new PDFRenderer(document);
            
            for (int i = 0; i < pages.size() && i < document.getNumberOfPages(); i++) {
                PageText pageText = pages.get(i);
                
                // 檢查是否需要進行LaTeX識別
                if (shouldDetectLatex(pageText)) {
                    try {
                        BufferedImage pageImage = renderer.renderImageWithDPI(i, 300);
                        enhancePageWithLatex(pageText, pageImage, i + 1);
                    } catch (Exception e) {
                        System.err.println("第 " + (i + 1) + " 頁LaTeX識別失敗: " + e.getMessage());
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("LaTeX識別過程中無法讀取PDF: " + e.getMessage());
        }
        
        System.out.println("PDF頁面LaTeX公式識別完成");
    }
    
    /**
     * 增強頁面內容，加入LaTeX公式識別（從圖片）
     */
    private void enhancePagesWithLatexFromImages(List<PageText> pages, List<Image> images) {
        if (pages == null || pages.isEmpty() || images == null || images.isEmpty()) {
            return;
        }
        
        System.out.println("開始對圖片頁面進行LaTeX公式識別...");
        
        int maxPages = Math.min(pages.size(), images.size());
        
        for (int i = 0; i < maxPages; i++) {
            PageText pageText = pages.get(i);
            Image image = images.get(i);
            
            // 檢查是否需要進行LaTeX識別
            if (shouldDetectLatex(pageText)) {
                try {
                    BufferedImage bufferedImage = SwingFXUtils.fromFXImage(image, null);
                    enhancePageWithLatex(pageText, bufferedImage, i + 1);
                } catch (Exception e) {
                    System.err.println("第 " + (i + 1) + " 頁LaTeX識別失敗: " + e.getMessage());
                }
            }
        }
        
        System.out.println("圖片頁面LaTeX公式識別完成");
    }
    
    /**
     * 判斷是否應該進行LaTeX識別
     */
    private boolean shouldDetectLatex(PageText pageText) {
        switch (latexDetectionMode) {
            case DISABLED:
                return false;
            case FORCE:
                return true;
            case AUTO:
            default:
                // 自動檢測：檢查文字內容是否包含數學公式指示符
                String text = pageText.getBestText();
                return text != null && MATH_FORMULA_INDICATORS.matcher(text).matches();
        }
    }
    
    /**
     * 對單頁進行LaTeX公式識別增強
     */
    private void enhancePageWithLatex(PageText pageText, BufferedImage image, int pageNumber) {
        try {
            LatexOCRIntegrator.LatexOCRResult result = latexOCR.recognizeLatexFromBufferedImage(image);
            
            if (result.isSuccess() && result.getLatexCode() != null && !result.getLatexCode().trim().isEmpty()) {
                // 將LaTeX公式加入到頁面文字中
                String existingText = pageText.getBestText();
                String latexCode = result.getLatexCode().trim();
                
                // 創建增強的文字內容
                StringBuilder enhancedText = new StringBuilder();
                
                if (existingText != null && !existingText.trim().isEmpty()) {
                    enhancedText.append(existingText);
                    enhancedText.append("\n\n");
                }
                
                enhancedText.append("=== LaTeX 數學公式 ===\n");
                enhancedText.append(latexCode);
                enhancedText.append("\n=== LaTeX 公式結束 ===");
                
                // 更新頁面文字
                if (pageText.getTextSource() == TextSource.OCR) {
                    pageText.setOcrText(enhancedText.toString());
                } else {
                    pageText.setOcrText(enhancedText.toString());
                    pageText.setTextSource(TextSource.MIXED);
                }
                
                System.out.println("第 " + pageNumber + " 頁成功識別LaTeX公式: " + 
                                 (latexCode.length() > 50 ? latexCode.substring(0, 50) + "..." : latexCode));
            } else {
                System.out.println("第 " + pageNumber + " 頁LaTeX識別無結果");
                if (result.getErrorMessage() != null) {
                    System.out.println("錯誤: " + result.getErrorMessage());
                }
            }
        } catch (Exception e) {
            System.err.println("第 " + pageNumber + " 頁LaTeX識別過程中發生錯誤: " + e.getMessage());
        }
    }
    
    /**
     * 從單一圖片識別LaTeX公式
     */
    public String extractLatexFromImage(Image image) {
        if (!isLatexOcrAvailable() || image == null) {
            return "";
        }
        
        try {
            LatexOCRIntegrator.LatexOCRResult result = latexOCR.recognizeLatexFromImage(image);
            
            if (result.isSuccess()) {
                return result.getLatexCode() != null ? result.getLatexCode() : "";
            } else {
                System.err.println("LaTeX識別失敗: " + result.getErrorMessage());
                return "";
            }
        } catch (Exception e) {
            System.err.println("LaTeX識別過程中發生錯誤: " + e.getMessage());
            return "";
        }
    }
    
    /**
     * 從BufferedImage識別LaTeX公式
     */
    public String extractLatexFromBufferedImage(BufferedImage image) {
        if (!isLatexOcrAvailable() || image == null) {
            return "";
        }
        
        try {
            LatexOCRIntegrator.LatexOCRResult result = latexOCR.recognizeLatexFromBufferedImage(image);
            
            if (result.isSuccess()) {
                return result.getLatexCode() != null ? result.getLatexCode() : "";
            } else {
                System.err.println("LaTeX識別失敗: " + result.getErrorMessage());
                return "";
            }
        } catch (Exception e) {
            System.err.println("LaTeX識別過程中發生錯誤: " + e.getMessage());
            return "";
        }
    }
    
    /**
     * 批量從圖片列表識別LaTeX公式
     */
    public List<String> extractLatexFromImages(List<Image> images) {
        List<String> results = new ArrayList<>();
        
        if (!isLatexOcrAvailable() || images == null || images.isEmpty()) {
            return results;
        }
        
        System.out.println("開始批量LaTeX公式識別，共 " + images.size() + " 張圖片");
        
        List<LatexOCRIntegrator.LatexOCRResult> ocrResults = latexOCR.recognizeLatexFromImages(images);
        
        for (LatexOCRIntegrator.LatexOCRResult result : ocrResults) {
            if (result.isSuccess() && result.getLatexCode() != null) {
                results.add(result.getLatexCode());
            } else {
                results.add(""); // 失敗時加入空字符串
            }
        }
        
        System.out.println("批量LaTeX公式識別完成");
        return results;
    }
    
    /**
     * 檢查LaTeX-OCR是否可用
     */
    public boolean isLatexOcrAvailable() {
        return latexOcrEnabled && latexOCR != null && latexOCR.isAvailable();
    }
    
    /**
     * 獲取LaTeX檢測模式
     */
    public LatexDetectionMode getLatexDetectionMode() {
        return latexDetectionMode;
    }
    
    /**
     * 設定LaTeX檢測模式
     */
    public void setLatexDetectionMode(LatexDetectionMode mode) {
        this.latexDetectionMode = mode;
        System.out.println("LaTeX檢測模式已設定為: " + mode.getDisplayName());
    }
    
    /**
     * 重新初始化LaTeX-OCR
     */
    public boolean reinitializeLatexOCR() {
        System.out.println("重新初始化LaTeX-OCR...");
        initializeLatexOCR();
        return latexOcrEnabled;
    }
    
    /**
     * 獲取LaTeX-OCR狀態資訊
     */
    public String getLatexOcrStatus() {
        StringBuilder status = new StringBuilder();
        status.append("LaTeX-OCR狀態: ");
        
        if (latexOcrEnabled) {
            status.append("可用");
        } else {
            status.append("不可用");
            if (latexOCR != null) {
                status.append(" (").append(latexOCR.getLastError()).append(")");
            }
        }
        
        status.append(" | 檢測模式: ").append(latexDetectionMode.getDisplayName());
        
        return status.toString();
    }
    
    /**
     * 獲取完整的OCR狀態資訊（包含傳統OCR和LaTeX-OCR）
     */
    @Override
    public String getOcrStatus() {
        StringBuilder status = new StringBuilder();
        status.append(super.getOcrStatus());
        status.append("\n");
        status.append(getLatexOcrStatus());
        
        return status.toString();
    }
    
    /**
     * 測試LaTeX-OCR功能
     */
    public boolean testLatexOCR() {
        if (!isLatexOcrAvailable()) {
            System.err.println("LaTeX-OCR 不可用，無法執行測試");
            return false;
        }
        
        try {
            // 創建一個簡單的測試圖片（純白背景）
            BufferedImage testImage = new BufferedImage(100, 50, BufferedImage.TYPE_INT_RGB);
            Graphics2D g2d = testImage.createGraphics();
            g2d.setColor(Color.WHITE);
            g2d.fillRect(0, 0, 100, 50);
            g2d.setColor(Color.BLACK);
            g2d.drawString("x = y + 1", 10, 30);
            g2d.dispose();
            
            // 測試識別
            String result = extractLatexFromBufferedImage(testImage);
            
            System.out.println("LaTeX-OCR 測試完成");
            System.out.println("測試結果: " + (result.isEmpty() ? "無結果" : result));
            
            return true;
            
        } catch (Exception e) {
            System.err.println("LaTeX-OCR 測試失敗: " + e.getMessage());
            return false;
        }
    }
}
