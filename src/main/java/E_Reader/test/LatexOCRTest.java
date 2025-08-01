package E_Reader.test;

import E_Reader.core.EnhancedTextExtractor;
import E_Reader.core.LatexOCRIntegrator;
import E_Reader.settings.SettingsManager;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * LaTeX-OCR 整合測試類
 */
public class LatexOCRTest extends Application {

    public static void main(String[] args) {
        System.out.println("=== LaTeX-OCR 整合測試開始 ===");
        
        // 啟動 JavaFX 應用程式（需要為 Image 轉換）
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        // 在 JavaFX 執行緒中運行測試
        Platform.runLater(() -> {
            try {
                runTests();
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                Platform.exit();
            }
        });
    }

    private void runTests() {
        System.out.println("\n1. 測試 LatexOCRIntegrator 初始化...");
        testLatexOCRIntegrator();
        
        System.out.println("\n2. 測試 EnhancedTextExtractor 初始化...");
        testEnhancedTextExtractor();
        
        System.out.println("\n3. 測試創建測試圖片和識別...");
        testImageRecognition();
        
        System.out.println("\n=== LaTeX-OCR 整合測試完成 ===");
    }

    private void testLatexOCRIntegrator() {
        try {
            LatexOCRIntegrator integrator = new LatexOCRIntegrator();
            
            System.out.println("LatexOCRIntegrator 已創建");
            System.out.println("可用性: " + integrator.isAvailable());
            
            if (!integrator.isAvailable()) {
                System.out.println("最後錯誤: " + integrator.getLastError());
            } else {
                System.out.println("版本資訊:");
                System.out.println(integrator.getVersionInfo());
            }
            
        } catch (Exception e) {
            System.err.println("LatexOCRIntegrator 測試失敗: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void testEnhancedTextExtractor() {
        try {
            SettingsManager settingsManager = new SettingsManager();
            settingsManager.loadSettings();
            
            EnhancedTextExtractor extractor = new EnhancedTextExtractor(settingsManager);
            
            System.out.println("EnhancedTextExtractor 已創建");
            System.out.println("OCR 狀態:");
            System.out.println(extractor.getOcrStatus());
            System.out.println("LaTeX-OCR 可用: " + extractor.isLatexOcrAvailable());
            System.out.println("LaTeX 檢測模式: " + extractor.getLatexDetectionMode().getDisplayName());
            
            // 測試LaTeX-OCR功能
            if (extractor.isLatexOcrAvailable()) {
                System.out.println("執行LaTeX-OCR內建測試...");
                boolean testResult = extractor.testLatexOCR();
                System.out.println("內建測試結果: " + (testResult ? "成功" : "失敗"));
            }
            
        } catch (Exception e) {
            System.err.println("EnhancedTextExtractor 測試失敗: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void testImageRecognition() {
        try {
            // 創建包含數學公式的測試圖片
            BufferedImage testImage = createMathFormulaImage();
            
            // 儲存測試圖片（用於除錯）
            File testImageFile = new File("latex_test_image.png");
            ImageIO.write(testImage, "PNG", testImageFile);
            System.out.println("測試圖片已儲存: " + testImageFile.getAbsolutePath());
            
            // 測試 LatexOCRIntegrator
            System.out.println("\n測試 LatexOCRIntegrator 識別...");
            LatexOCRIntegrator integrator = new LatexOCRIntegrator();
            
            if (integrator.isAvailable()) {
                LatexOCRIntegrator.LatexOCRResult result = integrator.recognizeLatexFromFile(testImageFile);
                System.out.println("識別結果: " + result);
                
                if (result.isSuccess()) {
                    System.out.println("LaTeX 代碼: " + result.getLatexCode());
                } else {
                    System.out.println("識別失敗: " + result.getErrorMessage());
                }
            } else {
                System.out.println("LatexOCRIntegrator 不可用: " + integrator.getLastError());
            }
            
            // 測試 EnhancedTextExtractor
            System.out.println("\n測試 EnhancedTextExtractor 識別...");
            EnhancedTextExtractor extractor = new EnhancedTextExtractor();
            
            if (extractor.isLatexOcrAvailable()) {
                String latexResult = extractor.extractLatexFromBufferedImage(testImage);
                System.out.println("LaTeX 識別結果: " + (latexResult.isEmpty() ? "無結果" : latexResult));
            } else {
                System.out.println("EnhancedTextExtractor LaTeX-OCR 不可用");
            }
            
            // 清理測試檔案
            if (testImageFile.exists()) {
                testImageFile.delete();
                System.out.println("測試圖片已清理");
            }
            
        } catch (Exception e) {
            System.err.println("圖片識別測試失敗: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * 創建包含數學公式的測試圖片
     */
    private BufferedImage createMathFormulaImage() {
        int width = 400;
        int height = 200;
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        
        Graphics2D g2d = image.createGraphics();
        
        // 設定渲染品質
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        
        // 白色背景
        g2d.setColor(Color.WHITE);
        g2d.fillRect(0, 0, width, height);
        
        // 黑色文字
        g2d.setColor(Color.BLACK);
        g2d.setFont(new Font("Times New Roman", Font.PLAIN, 24));
        
        // 繪製數學公式
        String[] formulas = {
            "E = mc²",
            "x = (-b ± √(b² - 4ac)) / 2a",
            "∫ₐᵇ f(x)dx = F(b) - F(a)",
            "lim_{x→∞} (1 + 1/x)ˣ = e"
        };
        
        int y = 40;
        for (String formula : formulas) {
            g2d.drawString(formula, 20, y);
            y += 40;
        }
        
        g2d.dispose();
        return image;
    }
}
