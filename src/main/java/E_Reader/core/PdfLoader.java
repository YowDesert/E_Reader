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
import java.util.function.Consumer;

/**
 * PDF載入器 - 負責將PDF檔案轉換為圖片序列
 * 改進版：支援進度回調和更好的錯誤處理
 */
public class PdfLoader {

    // 預設DPI設定
    private static final float DEFAULT_DPI = 150f;
    private static final float HIGH_QUALITY_DPI = 300f;

    // 進度回調介面
    public interface ProgressCallback {
        void updateProgress(int currentPage, int totalPages, String message);
        void onError(String error);
        boolean isCancelled();
    }

    /**
     * 從PDF檔案載入所有頁面為圖片
     *
     * @param pdfFile PDF檔案
     * @return 圖片列表
     * @throws IOException 如果無法讀取PDF檔案
     */
    public List<Image> loadImagesFromPdf(File pdfFile) throws IOException {
        return loadImagesFromPdf(pdfFile, DEFAULT_DPI, null);
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
        return loadImagesFromPdf(pdfFile, dpi, null);
    }

    /**
     * 從PDF檔案載入所有頁面為圖片（支援進度回調）
     *
     * @param pdfFile PDF檔案
     * @param dpi 圖片解析度
     * @param progressCallback 進度回調
     * @return 圖片列表
     * @throws IOException 如果無法讀取PDF檔案
     */
    public List<Image> loadImagesFromPdf(File pdfFile, float dpi, ProgressCallback progressCallback) throws IOException {
        List<Image> images = new ArrayList<>();

        if (pdfFile == null || !pdfFile.exists() || !pdfFile.isFile()) {
            throw new IOException("PDF檔案不存在或無法讀取: " + pdfFile);
        }

        System.out.println("開始載入PDF檔案: " + pdfFile.getName() + " (DPI: " + dpi + ")");

        try (PDDocument document = PDDocument.load(pdfFile)) {
            PDFRenderer renderer = new PDFRenderer(document);
            int pageCount = document.getNumberOfPages();

            System.out.println("PDF總頁數: " + pageCount);

            if (progressCallback != null) {
                progressCallback.updateProgress(0, pageCount, "開始處理PDF檔案...");
            }

            for (int i = 0; i < pageCount; i++) {
                // 檢查是否取消
                if (progressCallback != null && progressCallback.isCancelled()) {
                    System.out.println("PDF載入已取消");
                    break;
                }

                try {
                    System.out.println("正在處理第 " + (i + 1) + "/" + pageCount + " 頁");

                    if (progressCallback != null) {
                        progressCallback.updateProgress(i, pageCount, "正在處理第 " + (i + 1) + " 頁...");
                    }

                    BufferedImage bufferedImage = renderer.renderImageWithDPI(i, dpi, ImageType.RGB);
                    Image fxImage = SwingFXUtils.toFXImage(bufferedImage, null);

                    if (!fxImage.isError()) {
                        images.add(fxImage);
                        System.out.println("第 " + (i + 1) + " 頁載入成功");
                    } else {
                        String errorMsg = "第 " + (i + 1) + " 頁轉換失敗";
                        System.err.println(errorMsg);
                        if (progressCallback != null) {
                            progressCallback.onError(errorMsg);
                        }
                    }
                } catch (IOException e) {
                    String errorMsg = "第 " + (i + 1) + " 頁渲染失敗: " + e.getMessage();
                    System.err.println(errorMsg);
                    if (progressCallback != null) {
                        progressCallback.onError(errorMsg);
                    }
                    // 繼續處理其他頁面
                }
            }

            if (progressCallback != null) {
                progressCallback.updateProgress(pageCount, pageCount, "載入完成，共 " + images.size() + " 頁");
            }

        } catch (IOException e) {
            String errorMsg = "無法載入PDF檔案: " + e.getMessage();
            System.err.println(errorMsg);
            if (progressCallback != null) {
                progressCallback.onError(errorMsg);
            }
            throw new IOException(errorMsg, e);
        }

        if (images.isEmpty()) {
            throw new IOException("PDF檔案中沒有可讀取的頁面");
        }

        System.out.println("PDF載入完成，總共 " + images.size() + " 頁");
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
        return loadPagesFromPdf(pdfFile, startPage, endPage, DEFAULT_DPI, null);
    }

    /**
     * 載入PDF檔案的指定頁面範圍（指定DPI和進度回調）
     *
     * @param pdfFile PDF檔案
     * @param startPage 起始頁面（從0開始）
     * @param endPage 結束頁面（從0開始，不包含）
     * @param dpi 圖片解析度
     * @param progressCallback 進度回調
     * @return 圖片列表
     * @throws IOException 如果無法讀取PDF檔案
     */
    public List<Image> loadPagesFromPdf(File pdfFile, int startPage, int endPage, float dpi, ProgressCallback progressCallback) throws IOException {
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

            int totalPages = endPage - startPage;

            if (progressCallback != null) {
                progressCallback.updateProgress(0, totalPages, "開始處理指定頁面...");
            }

            for (int i = startPage; i < endPage; i++) {
                // 檢查是否取消
                if (progressCallback != null && progressCallback.isCancelled()) {
                    break;
                }

                try {
                    if (progressCallback != null) {
                        progressCallback.updateProgress(i - startPage, totalPages, "正在處理第 " + (i + 1) + " 頁...");
                    }

                    BufferedImage bufferedImage = renderer.renderImageWithDPI(i, dpi, ImageType.RGB);
                    Image fxImage = SwingFXUtils.toFXImage(bufferedImage, null);

                    if (!fxImage.isError()) {
                        images.add(fxImage);
                    } else {
                        String errorMsg = "第 " + (i + 1) + " 頁轉換失敗";
                        System.err.println(errorMsg);
                        if (progressCallback != null) {
                            progressCallback.onError(errorMsg);
                        }
                    }
                } catch (IOException e) {
                    String errorMsg = "第 " + (i + 1) + " 頁渲染失敗: " + e.getMessage();
                    System.err.println(errorMsg);
                    if (progressCallback != null) {
                        progressCallback.onError(errorMsg);
                    }
                    // 繼續處理其他頁面
                }
            }

            if (progressCallback != null) {
                progressCallback.updateProgress(totalPages, totalPages, "載入完成");
            }

        } catch (IOException e) {
            String errorMsg = "無法載入PDF檔案: " + e.getMessage();
            if (progressCallback != null) {
                progressCallback.onError(errorMsg);
            }
            throw new IOException(errorMsg, e);
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
     * 預載入PDF檔案信息（不載入圖片）
     *
     * @param pdfFile PDF檔案
     * @return PDF信息對象
     * @throws IOException 如果無法讀取PDF檔案
     */
    public PdfInfo getPdfInfo(File pdfFile) throws IOException {
        if (pdfFile == null || !pdfFile.exists() || !pdfFile.isFile()) {
            throw new IOException("PDF檔案不存在或無法讀取: " + pdfFile);
        }

        try (PDDocument document = PDDocument.load(pdfFile)) {
            PdfInfo info = new PdfInfo();
            info.fileName = pdfFile.getName();
            info.filePath = pdfFile.getAbsolutePath();
            info.fileSize = pdfFile.length();
            info.pageCount = document.getNumberOfPages();

            // 獲取文檔信息
            if (document.getDocumentInformation() != null) {
                var docInfo = document.getDocumentInformation();
                info.title = docInfo.getTitle();
                info.author = docInfo.getAuthor();
                info.subject = docInfo.getSubject();
                info.creator = docInfo.getCreator();
                info.producer = docInfo.getProducer();
                info.creationDate = docInfo.getCreationDate() != null ?
                        docInfo.getCreationDate() : null;
                info.modificationDate = docInfo.getModificationDate() != null ?
                        docInfo.getModificationDate() : null;
            }

            return info;
        } catch (IOException e) {
            throw new IOException("無法讀取PDF檔案信息: " + e.getMessage(), e);
        }
    }

    /**
     * PDF信息類別
     */
    public static class PdfInfo {
        public String fileName;
        public String filePath;
        public long fileSize;
        public int pageCount;
        public String title;
        public String author;
        public String subject;
        public String creator;
        public String producer;
        public java.util.Calendar creationDate;
        public java.util.Calendar modificationDate;

        @Override
        public String toString() {
            return String.format("PDF信息[檔案=%s, 頁數=%d, 大小=%d bytes]",
                    fileName, pageCount, fileSize);
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