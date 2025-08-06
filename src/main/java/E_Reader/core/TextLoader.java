package E_Reader.core;

import E_Reader.settings.SettingsManager;
import javafx.scene.image.Image;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * 文字檔案載入器 - 將TXT檔案轉換為圖片供閱讀器顯示
 */
public class TextLoader {
    
    private static final int DEFAULT_PAGE_WIDTH = 800;
    private static final int DEFAULT_PAGE_HEIGHT = 1000;
    private static final int DEFAULT_MARGIN = 50;
    private static final int DEFAULT_LINE_SPACING = 20;
    private static final int DEFAULT_FONT_SIZE = 16;
    
    private final SettingsManager settingsManager;
    
    public TextLoader(SettingsManager settingsManager) {
        this.settingsManager = settingsManager;
    }
    
    /**
     * 從TXT檔案載入圖片
     */
    public List<Image> loadImagesFromText(File file) throws IOException {
        List<Image> images = new ArrayList<>();
        
        // 讀取TXT檔案內容
        String content = readTextFile(file);
        
        // 分割內容為頁面
        List<String> pages = splitIntoPages(content);
        
        // 為每頁創建圖片
        for (String pageContent : pages) {
            Image pageImage = createPageImage(pageContent);
            images.add(pageImage);
        }
        
        return images;
    }
    
    /**
     * 讀取TXT檔案內容
     */
    private String readTextFile(File file) throws IOException {
        StringBuilder content = new StringBuilder();
        
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                content.append(line).append("\n");
            }
        }
        
        return content.toString();
    }
    
    /**
     * 將內容分割為頁面
     */
    private List<String> splitIntoPages(String content) {
        List<String> pages = new ArrayList<>();
        
        // 計算每頁可容納的字元數
        int charsPerPage = calculateCharsPerPage();
        
        // 分割內容
        int startIndex = 0;
        while (startIndex < content.length()) {
            int endIndex = Math.min(startIndex + charsPerPage, content.length());
            
            // 嘗試在句號或換行處分割
            if (endIndex < content.length()) {
                int lastPeriod = content.lastIndexOf('。', endIndex);
                int lastNewline = content.lastIndexOf('\n', endIndex);
                int splitPoint = Math.max(lastPeriod, lastNewline);
                
                if (splitPoint > startIndex) {
                    endIndex = splitPoint + 1;
                }
            }
            
            String pageContent = content.substring(startIndex, endIndex).trim();
            if (!pageContent.isEmpty()) {
                pages.add(pageContent);
            }
            
            startIndex = endIndex;
        }
        
        // 如果沒有內容，創建一個空頁面
        if (pages.isEmpty()) {
            pages.add("檔案為空或無法讀取");
        }
        
        return pages;
    }
    
    /**
     * 計算每頁可容納的字元數
     */
    private int calculateCharsPerPage() {
        int availableWidth = DEFAULT_PAGE_WIDTH - (DEFAULT_MARGIN * 2);
        int availableHeight = DEFAULT_PAGE_HEIGHT - (DEFAULT_MARGIN * 2);
        
        // 估算每行字元數（假設平均每個字元寬度為字體大小的一半）
        int charsPerLine = availableWidth / (DEFAULT_FONT_SIZE / 2);
        
        // 估算行數
        int linesPerPage = availableHeight / (DEFAULT_FONT_SIZE + DEFAULT_LINE_SPACING);
        
        return charsPerLine * linesPerPage;
    }
    
    /**
     * 創建頁面圖片
     */
    private Image createPageImage(String pageContent) {
        Canvas canvas = new Canvas(DEFAULT_PAGE_WIDTH, DEFAULT_PAGE_HEIGHT);
        GraphicsContext gc = canvas.getGraphicsContext2D();
        
        // 設定背景色
        Color backgroundColor = getBackgroundColor();
        gc.setFill(backgroundColor);
        gc.fillRect(0, 0, DEFAULT_PAGE_WIDTH, DEFAULT_PAGE_HEIGHT);
        
        // 設定文字顏色
        Color textColor = getTextColor();
        gc.setFill(textColor);
        
        // 設定字體
        Font font = Font.font("Microsoft YaHei", DEFAULT_FONT_SIZE);
        gc.setFont(font);
        
        // 設定文字對齊
        gc.setTextAlign(TextAlignment.LEFT);
        
        // 繪製文字
        drawText(gc, pageContent);
        
        // 轉換為圖片
        return canvas.snapshot(null, null);
    }
    
    /**
     * 繪製文字到畫布
     */
    private void drawText(GraphicsContext gc, String text) {
        String[] lines = text.split("\n");
        int y = DEFAULT_MARGIN + DEFAULT_FONT_SIZE;
        
        for (String line : lines) {
            if (y + DEFAULT_FONT_SIZE > DEFAULT_PAGE_HEIGHT - DEFAULT_MARGIN) {
                break; // 超出頁面範圍
            }
            
            // 處理長行自動換行
            List<String> wrappedLines = wrapText(line, DEFAULT_PAGE_WIDTH - (DEFAULT_MARGIN * 2));
            
            for (String wrappedLine : wrappedLines) {
                if (y + DEFAULT_FONT_SIZE > DEFAULT_PAGE_HEIGHT - DEFAULT_MARGIN) {
                    break;
                }
                
                gc.fillText(wrappedLine, DEFAULT_MARGIN, y);
                y += DEFAULT_FONT_SIZE + DEFAULT_LINE_SPACING;
            }
        }
    }
    
    /**
     * 文字自動換行
     */
    private List<String> wrapText(String text, int maxWidth) {
        List<String> lines = new ArrayList<>();
        
        if (text.isEmpty()) {
            lines.add("");
            return lines;
        }
        
        String[] words = text.split(" ");
        StringBuilder currentLine = new StringBuilder();
        
        for (String word : words) {
            String testLine = currentLine.toString() + (currentLine.length() > 0 ? " " : "") + word;
            
            // 估算文字寬度（簡化計算）
            int estimatedWidth = testLine.length() * (DEFAULT_FONT_SIZE / 2);
            
            if (estimatedWidth > maxWidth && currentLine.length() > 0) {
                lines.add(currentLine.toString());
                currentLine = new StringBuilder(word);
            } else {
                if (currentLine.length() > 0) {
                    currentLine.append(" ");
                }
                currentLine.append(word);
            }
        }
        
        if (currentLine.length() > 0) {
            lines.add(currentLine.toString());
        }
        
        return lines;
    }
    
    /**
     * 獲取背景色
     */
    private Color getBackgroundColor() {
        SettingsManager.ThemeMode theme = settingsManager.getThemeMode();
        String colorHex = theme.getBackgroundColor();
        return Color.web(colorHex);
    }
    
    /**
     * 獲取文字色
     */
    private Color getTextColor() {
        SettingsManager.ThemeMode theme = settingsManager.getThemeMode();
        String colorHex = theme.getTextColor();
        return Color.web(colorHex);
    }
} 