package E_Reader.test;

import E_Reader.core.TextExtractor;
import E_Reader.core.TextExtractor.PageText;
import javafx.scene.image.Image;

import java.io.File;
import java.util.List;
import java.util.Scanner;

/**
 * TextExtractor 增強版測試程式
 */
public class TextExtractorTest {
    
    private static TextExtractor extractor;
    private static Scanner scanner;

    public static void main(String[] args) {
        System.out.println("=== TextExtractor 增強版測試程式 ===\n");
        
        // 初始化
        extractor = new TextExtractor();
        scanner = new Scanner(System.in);
        
        // 顯示 OCR 狀態
        System.out.println(extractor.getOcrStatus());
        System.out.println("OCR 可用性: " + (extractor.isOcrAvailable() ? "可用" : "不可用"));
        System.out.println();
        
        // 主選單
        while (true) {
            showMenu();
            int choice = getChoice();
            
            switch (choice) {
                case 1:
                    testPdfExtraction();
                    break;
                case 2:
                    testImageExtraction();
                    break;
                case 3:
                    testOcrStatus();
                    break;
                case 4:
                    toggleDetectionFailureNotification();
                    break;
                case 5:
                    showModelInfo();
                    break;
                case 0:
                    System.out.println("程式結束");
                    return;
                default:
                    System.out.println("無效選擇，請重新輸入");
            }
            
            System.out.println("\n按 Enter 繼續...");
            scanner.nextLine();
        }
    }
    
    private static void showMenu() {
        System.out.println("=== 主選單 ===");
        System.out.println("1. 測試 PDF 文字提取");
        System.out.println("2. 測試圖片文字提取");
        System.out.println("3. 檢查 OCR 狀態");
        System.out.println("4. 切換偵測失敗通知");
        System.out.println("5. 顯示模型資訊");
        System.out.println("0. 結束程式");
        System.out.print("\n請選擇功能 (0-5): ");
    }
    
    private static int getChoice() {
        try {
            String input = scanner.nextLine().trim();
            return Integer.parseInt(input);
        } catch (NumberFormatException e) {
            return -1;
        }
    }
    
    private static void testPdfExtraction() {
        System.out.println("\n=== PDF 文字提取測試 ===");
        System.out.print("請輸入 PDF 檔案路徑: ");
        String filePath = scanner.nextLine().trim();
        
        if (filePath.isEmpty()) {
            System.out.println("未輸入檔案路徑");
            return;
        }
        
        File pdfFile = new File(filePath);
        if (!pdfFile.exists()) {
            System.out.println("檔案不存在: " + filePath);
            return;
        }
        
        try {
            System.out.println("正在處理 PDF 檔案，請稍候...");
            long startTime = System.currentTimeMillis();
            
            List<PageText> pages = extractor.extractTextFromPdf(pdfFile);
            
            long endTime = System.currentTimeMillis();
            System.out.println("處理完成，耗時: " + (endTime - startTime) + " 毫秒");
            System.out.println("總頁數: " + pages.size());
            
            // 統計結果
            int nativeCount = 0, ocrCount = 0, failedCount = 0;
            
            for (PageText page : pages) {
                switch (page.getTextSource()) {
                    case NATIVE:
                        nativeCount++;
                        break;
                    case OCR:
                        ocrCount++;
                        break;
                }
                
                if (page.getBestText().trim().isEmpty()) {
                    failedCount++;
                }
            }
            
            System.out.println("\n=== 處理統計 ===");
            System.out.println("原生文字頁面: " + nativeCount);
            System.out.println("OCR 處理頁面: " + ocrCount);
            System.out.println("偵測失敗頁面: " + failedCount);
            
            // 顯示部分結果
            System.out.println("\n=== 前3頁內容預覽 ===");
            for (int i = 0; i < Math.min(3, pages.size()); i++) {
                PageText page = pages.get(i);
                String text = page.getBestText();
                
                System.out.println("第 " + (i + 1) + " 頁 (" + page.getTextSource().getDisplayName() + "):");
                if (!text.isEmpty()) {
                    String preview = text.length() > 200 ? text.substring(0, 200) + "..." : text;
                    System.out.println(preview);
                } else {
                    System.out.println("[無文字內容]");
                }
                System.out.println();
            }
            
        } catch (Exception e) {
            System.err.println("處理 PDF 時發生錯誤: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private static void testImageExtraction() {
        System.out.println("\n=== 圖片文字提取測試 ===");
        
        if (!extractor.isOcrAvailable()) {
            System.out.println("OCR 不可用，無法進行圖片文字提取測試");
            return;
        }
        
        System.out.print("請輸入圖片檔案路徑: ");
        String filePath = scanner.nextLine().trim();
        
        if (filePath.isEmpty()) {
            System.out.println("未輸入檔案路徑");
            return;
        }
        
        File imageFile = new File(filePath);
        if (!imageFile.exists()) {
            System.out.println("檔案不存在: " + filePath);
            return;
        }
        
        try {
            System.out.println("正在處理圖片，請稍候...");
            long startTime = System.currentTimeMillis();
            
            Image image = new Image(imageFile.toURI().toString());
            String text = extractor.extractTextFromImage(image);
            
            long endTime = System.currentTimeMillis();
            System.out.println("處理完成，耗時: " + (endTime - startTime) + " 毫秒");
            
            if (!text.isEmpty()) {
                System.out.println("\n=== 提取的文字內容 ===");
                System.out.println(text);
                System.out.println("\n文字長度: " + text.length() + " 字符");
            } else {
                System.out.println("未能從圖片中提取到文字");
            }
            
        } catch (Exception e) {
            System.err.println("處理圖片時發生錯誤: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private static void testOcrStatus() {
        System.out.println("\n=== OCR 狀態檢查 ===");
        System.out.println(extractor.getOcrStatus());
        System.out.println("OCR 可用性: " + (extractor.isOcrAvailable() ? "可用" : "不可用"));
        
        // 檢查模型檔案
        System.out.println("\n=== 模型檔案檢查 ===");
        File tessdataDir = new File("tessdata");
        if (tessdataDir.exists() && tessdataDir.isDirectory()) {
            System.out.println("tessdata 資料夾存在");
            
            String[] modelFiles = {
                "chi_tra.traineddata",
                "chi_tra_fast.traineddata", 
                "chi_tra_best.traineddata",
                "eng.traineddata"
            };
            
            for (String fileName : modelFiles) {
                File modelFile = new File(tessdataDir, fileName);
                String status = modelFile.exists() ? "存在" : "不存在";
                long size = modelFile.exists() ? modelFile.length() / 1024 / 1024 : 0;
                System.out.println(fileName + ": " + status + 
                    (modelFile.exists() ? " (" + size + " MB)" : ""));
            }
        } else {
            System.out.println("tessdata 資料夾不存在");
        }
    }
    
    private static void toggleDetectionFailureNotification() {
        System.out.println("\n=== 偵測失敗通知設定 ===");
        System.out.print("是否顯示偵測失敗通知? (y/n): ");
        String input = scanner.nextLine().trim().toLowerCase();
        
        boolean show = input.equals("y") || input.equals("yes");
        extractor.setShowDetectionFailures(show);
        
        System.out.println("偵測失敗通知已" + (show ? "開啟" : "關閉"));
    }
    
    private static void showModelInfo() {
        System.out.println("\n=== 模型資訊 ===");
        System.out.println("模型載入順序:");
        System.out.println("1. 優先使用快速模型 (chi_tra_fast)");
        System.out.println("2. 如果快速模型結果不佳，切換到最佳模型 (chi_tra_best)");
        System.out.println("3. 如果沒有專用模型，使用標準模型 (chi_tra)");
        
        System.out.println("\n支援的檔案格式:");
        System.out.println("- PDF 檔案 (.pdf)");
        System.out.println("- 圖片檔案 (.jpg, .png, .bmp, .tiff 等)");
        
        System.out.println("\n文字偵測策略:");
        System.out.println("- PDF: 先嘗試原生文字，如品質不佳則使用 OCR");
        System.out.println("- 圖片: 直接使用 OCR");
        System.out.println("- 自動過濾無意義內容");
        System.out.println("- 支援中英文混合識別");
    }
}
