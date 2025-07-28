package E_Reader.core;

import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.apache.pdfbox.rendering.ImageType;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * PDF載入器 - 負責將PDF檔案轉換為圖片序列
 */
public class PdfLoader {

    // 預設DPI設定
    private static final float DEFAULT_DPI = 150f;
    private static final float HIGH_QUALITY_DPI = 300f;
    
    /**
     * 從PDF檔案載入所有頁面為圖片
     * 
     * @param pdfFile PDF檔案
     * @return 圖片列表
     * @throws IOException 如果無法讀取PDF檔案
     */
    public List<Image> loadImagesFromPdf(File pdfFile) throws IOException {
        return loadImagesFromPdf(pdfFile, DEFAULT_DPI);
    }
    
    /**
     * 從PDF檔案載入所有頁面為圖片（指定DPI）
     * 
     * @param pdfFile PDF檔案
     * @param dpi 圖片解析度
     * @return 圖片列表
     * @throws IOException 如果無法讀取PDF檔案
     */
    public List<Image> loadImagesFromPdf(File pdfFile, float dpi) throws IOException {
        List<Image> images = new ArrayList<>();
        
        if (pdfFile == null || !pdfFile.exists() || !pdfFile.isFile()) {
            throw new IOException("PDF檔案不存在或無法讀取: " + pdfFile);
        }
        
        try (PDDocument document = PDDocument.load(pdfFile)) {
            PDFRenderer renderer = new PDFRenderer(document);
            int pageCount = document.getNumberOfPages();
            
            for (int i = 0; i < pageCount; i++) {
                try {
                    BufferedImage bufferedImage = renderer.renderImageWithDPI(i, dpi, ImageType.RGB);
                    Image fxImage = SwingFXUtils.toFXImage(bufferedImage, null);
                    
                    if (!fxImage.isError()) {
                        images.add(fxImage);
                    } else {
                        System.err.println("第 " + (i + 1) + " 頁轉換失敗");
                    }
                } catch (IOException e) {
                    System.err.println("第 " + (i + 1) + " 頁渲染失敗: " + e.getMessage());
                    // 繼續處理其他頁面
                }
            }
        } catch (IOException e) {
            throw new IOException("無法載入PDF檔案: " + e.getMessage(), e);
        }
        
        if (images.isEmpty()) {
            throw new IOException("PDF檔案中沒有可讀取的頁面");
        }
        
        return images;
    }
    
    /**
     * 載入PDF檔案的指定頁面範圍
     * 
     * @param pdfFile PDF檔案
     * @param startPage 起始頁面（從0開始）
     * @param endPage 結束頁面（從0開始，不包含）
     * @return 圖片列表
     * @throws IOException 如果無法讀取PDF檔案
     */
    public List<Image> loadPagesFromPdf(File pdfFile, int startPage, int endPage) throws IOException {
        return loadPagesFromPdf(pdfFile, startPage, endPage, DEFAULT_DPI);
    }
    
    /**
     * 載入PDF檔案的指定頁面範圍（指定DPI）
     * 
     * @param pdfFile PDF檔案
     * @param startPage 起始頁面（從0開始）
     * @param endPage 結束頁面（從0開始，不包含）
     * @param dpi 圖片解析度
     * @return 圖片列表
     * @throws IOException 如果無法讀取PDF檔案
     */
    public List<Image> loadPagesFromPdf(File pdfFile, int startPage, int endPage, float dpi) throws IOException {
        List<Image> images = new ArrayList<>();
        
        if (pdfFile == null || !pdfFile.exists() || !pdfFile.isFile()) {
            throw new IOException("PDF檔案不存在或無法讀取: " + pdfFile);
        }
        
        try (PDDocument document = PDDocument.load(pdfFile)) {
            PDFRenderer renderer = new PDFRenderer(document);
            int pageCount = document.getNumberOfPages();
            
            // 驗證頁面範圍
            if (startPage < 0) startPage = 0;
            if (endPage > pageCount) endPage = pageCount;
            if (startPage >= endPage) {
                throw new IllegalArgumentException("無效的頁面範圍: " + startPage + " - " + endPage);
            }
            
            for (int i = startPage; i < endPage; i++) {
                try {
                    BufferedImage bufferedImage = renderer.renderImageWithDPI(i, dpi, ImageType.RGB);
                    Image fxImage = SwingFXUtils.toFXImage(bufferedImage, null);
                    
                    if (!fxImage.isError()) {
                        images.add(fxImage);
                    } else {
                        System.err.println("第 " + (i + 1) + " 頁轉換失敗");
                    }
                } catch (IOException e) {
                    System.err.println("第 " + (i + 1) + " 頁渲染失敗: " + e.getMessage());
                    // 繼續處理其他頁面
                }
            }
        } catch (IOException e) {
            throw new IOException("無法載入PDF檔案: " + e.getMessage(), e);
        }
        
        return images;
    }
    
    /**
     * 載入PDF檔案的單一頁面
     * 
     * @param pdfFile PDF檔案
     * @param pageIndex 頁面索引（從0開始）
     * @return 圖片，如果載入失敗則返回null
     * @throws IOException 如果無法讀取PDF檔案
     */
    public Image loadSinglePageFromPdf(File pdfFile, int pageIndex) throws IOException {
        return loadSinglePageFromPdf(pdfFile, pageIndex, DEFAULT_DPI);
    }
    
    /**
     * 載入PDF檔案的單一頁面（指定DPI）
     * 
     * @param pdfFile PDF檔案
     * @param pageIndex 頁面索引（從0開始）
     * @param dpi 圖片解析度
     * @return 圖片，如果載入失敗則返回null
     * @throws IOException 如果無法讀取PDF檔案
     */
    public Image loadSinglePageFromPdf(File pdfFile, int pageIndex, float dpi) throws IOException {
        if (pdfFile == null || !pdfFile.exists() || !pdfFile.isFile()) {
            throw new IOException("PDF檔案不存在或無法讀取: " + pdfFile);
        }
        
        try (PDDocument document = PDDocument.load(pdfFile)) {
            if (pageIndex < 0 || pageIndex >= document.getNumberOfPages()) {
                throw new IndexOutOfBoundsException("頁面索引超出範圍: " + pageIndex);
            }
            
            PDFRenderer renderer = new PDFRenderer(document);
            BufferedImage bufferedImage = renderer.renderImageWithDPI(pageIndex, dpi, ImageType.RGB);
            Image fxImage = SwingFXUtils.toFXImage(bufferedImage, null);
            
            if (fxImage.isError()) {
                throw new IOException("第 " + (pageIndex + 1) + " 頁轉換失敗");
            }
            
            return fxImage;
        } catch (IOException e) {
            throw new IOException("無法載入PDF頁面: " + e.getMessage(), e);
        }
    }
    
    /**
     * 獲取PDF檔案的頁面數量
     * 
     * @param pdfFile PDF檔案
     * @return 頁面數量
     * @throws IOException 如果無法讀取PDF檔案
     */
    public int getPageCount(File pdfFile) throws IOException {
        if (pdfFile == null || !pdfFile.exists() || !pdfFile.isFile()) {
            throw new IOException("PDF檔案不存在或無法讀取: " + pdfFile);
        }
        
        try (PDDocument document = PDDocument.load(pdfFile)) {
            return document.getNumberOfPages();
        } catch (IOException e) {
            throw new IOException("無法讀取PDF檔案頁面數: " + e.getMessage(), e);
        }
    }
    
    /**
     * 驗證檔案是否為有效的PDF檔案
     * 
     * @param file 要檢查的檔案
     * @return 是否為有效的PDF檔案
     */
    public boolean isPdfFile(File file) {
        if (file == null || !file.exists() || !file.isFile()) {
            return false;
        }
        
        String fileName = file.getName().toLowerCase();
        if (!fileName.endsWith(".pdf")) {
            return false;
        }
        
        try (PDDocument document = PDDocument.load(file)) {
            return document.getNumberOfPages() > 0;
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * 獲取高品質DPI設定
     * 
     * @return 高品質DPI值
     */
    public static float getHighQualityDpi() {
        return HIGH_QUALITY_DPI;
    }
    
    /**
     * 獲取預設DPI設定
     * 
     * @return 預設DPI值
     */
    public static float getDefaultDpi() {
        return DEFAULT_DPI;
    }
}
