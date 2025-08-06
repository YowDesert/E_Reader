package E_Reader.core;

import E_Reader.core.TextExtractor;
import javafx.scene.image.Image;

import java.util.List;

/**
 * 狀態管理器 - 統一管理應用程式的狀態
 */
public class StateManager {
    
    // 文件狀態
    private boolean isPdfMode = false;
    private boolean isEpubMode = false;
    private boolean isTextMode = false;
    private String currentFilePath = "";
    
    // 顯示狀態
    private boolean isFullScreen = false;
    private boolean isControlsVisible = true;
    private boolean isAutoScrolling = false;
    
    // 內容數據
    private List<Image> currentImages;
    private List<TextExtractor.PageText> currentTextPages;
    
    // 閱讀狀態
    private long readingStartTime;
    private long totalReadingTime = 0;
    
    // 頁面狀態 - 用於記住當前頁面
    private int currentImagePageIndex = 0;
    private int currentTextPageIndex = 0;
    
    public StateManager() {
        this.readingStartTime = System.currentTimeMillis();
    }
    
    // 文件狀態管理
    public void setFileLoaded(String filePath, boolean isPdf, boolean isEpub, 
                             List<Image> images, List<TextExtractor.PageText> textPages) {
        this.currentFilePath = filePath;
        this.isPdfMode = isPdf;
        this.isEpubMode = isEpub;
        this.isTextMode = false; // 初始為圖片模式
        this.currentImages = images;
        this.currentTextPages = textPages;
        // 重置頁面索引
        this.currentImagePageIndex = 0;
        this.currentTextPageIndex = 0;
    }
    
    public void clearFileState() {
        this.currentFilePath = "";
        this.isPdfMode = false;
        this.isEpubMode = false;
        this.isTextMode = false;
        this.currentImages = null;
        this.currentTextPages = null;
        // 重置頁面索引
        this.currentImagePageIndex = 0;
        this.currentTextPageIndex = 0;
    }
    
    public void clearCurrentFile() {
        clearFileState();
        // 重置其他相關狀態
        this.isAutoScrolling = false;
        // 保留閱讀時間累計，但重置當前會話
        long currentSessionTime = System.currentTimeMillis() - readingStartTime;
        this.totalReadingTime += currentSessionTime;
        this.readingStartTime = System.currentTimeMillis();
    }
    
    // Getter 和 Setter 方法
    public boolean isPdfMode() {
        return isPdfMode;
    }
    
    public void setPdfMode(boolean pdfMode) {
        isPdfMode = pdfMode;
    }
    
    public boolean isEpubMode() {
        return isEpubMode;
    }
    
    public void setEpubMode(boolean epubMode) {
        isEpubMode = epubMode;
    }
    
    public boolean isTextMode() {
        return isTextMode;
    }
    
    public void setTextMode(boolean textMode) {
        isTextMode = textMode;
    }
    
    public String getCurrentFilePath() {
        return currentFilePath != null ? currentFilePath : "";
    }
    
    public void setCurrentFilePath(String currentFilePath) {
        this.currentFilePath = currentFilePath;
    }
    
    public boolean isFullScreen() {
        return isFullScreen;
    }
    
    public void setFullScreen(boolean fullScreen) {
        isFullScreen = fullScreen;
    }
    
    public boolean isControlsVisible() {
        return isControlsVisible;
    }
    
    public void setControlsVisible(boolean isControlsVisible) {
        this.isControlsVisible = isControlsVisible;
    }
    
    public boolean isAutoScrolling() {
        return isAutoScrolling;
    }
    
    public void setAutoScrolling(boolean autoScrolling) {
        isAutoScrolling = autoScrolling;
    }
    
    public List<Image> getCurrentImages() {
        return currentImages;
    }
    
    public void setCurrentImages(List<Image> currentImages) {
        this.currentImages = currentImages;
    }
    
    public List<TextExtractor.PageText> getCurrentTextPages() {
        return currentTextPages;
    }
    
    public void setCurrentTextPages(List<TextExtractor.PageText> currentTextPages) {
        this.currentTextPages = currentTextPages;
    }
    
    public long getReadingStartTime() {
        return readingStartTime;
    }
    
    public void setReadingStartTime(long readingStartTime) {
        this.readingStartTime = readingStartTime;
    }
    
    public long getTotalReadingTime() {
        return totalReadingTime;
    }
    
    public void setTotalReadingTime(long totalReadingTime) {
        this.totalReadingTime = totalReadingTime;
    }
    
    // 計算總閱讀時間
    public long calculateTotalReadingTime() {
        long currentTime = System.currentTimeMillis();
        long sessionTime = currentTime - readingStartTime;
        return totalReadingTime + sessionTime;
    }
    
    // 重設閱讀計時
    public void resetReadingTimer() {
        this.readingStartTime = System.currentTimeMillis();
    }
    
    // 檢查是否有已載入的內容
    public boolean hasLoadedContent() {
        return !currentFilePath.isEmpty() && 
               ((currentImages != null && !currentImages.isEmpty()) || 
                (currentTextPages != null && !currentTextPages.isEmpty()));
    }
    
    // 檢查是否可以進行文字模式切換
    public boolean canSwitchToTextMode() {
        return hasLoadedContent();
    }
    
    // 頁面索引管理
    public int getCurrentImagePageIndex() {
        return currentImagePageIndex;
    }
    
    public void setCurrentImagePageIndex(int currentImagePageIndex) {
        this.currentImagePageIndex = currentImagePageIndex;
    }
    
    public int getCurrentTextPageIndex() {
        return currentTextPageIndex;
    }
    
    public void setCurrentTextPageIndex(int currentTextPageIndex) {
        this.currentTextPageIndex = currentTextPageIndex;
    }

    /**
     * 獲取當前模式下的頁面索引
     */
    public int getCurrentPageIndex() {
        return isTextMode ? currentTextPageIndex : currentImagePageIndex;
    }
    
    /**
     * 設置當前模式下的頁面索引
     */
    public void setCurrentPageIndex(int pageIndex) {
        if (isTextMode) {
            currentTextPageIndex = pageIndex;
        } else {
            currentImagePageIndex = pageIndex;
        }
    }

    public boolean isFileLoaded() {
        return !currentFilePath.isEmpty() && hasLoadedContent();
    }
}
