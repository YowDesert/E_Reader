package E_Reader.core;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import javafx.scene.image.Image;

/**
 * 圖片載入器 - 負責從資料夾載入圖片檔案
 */
public class ImageLoader {

    // 支援的圖片格式
    private static final String[] SUPPORTED_EXTENSIONS = {".jpg", ".jpeg", ".png", ".gif", ".bmp"};

    /**
     * 從資料夾載入所有支援的圖片檔案
     * 
     * @param folder 圖片資料夾
     * @return 圖片列表，按檔名排序
     */
    public List<Image> loadImagesFromFolder(File folder) {
        if (folder == null || !folder.exists() || !folder.isDirectory()) {
            return new ArrayList<>();
        }

        File[] files = folder.listFiles(this::isSupportedImageFile);
        if (files == null) {
            return new ArrayList<>();
        }

        // 按檔名排序
        Arrays.sort(files, Comparator.comparing(File::getName));
        
        List<Image> images = new ArrayList<>();
        for (File file : files) {
            try {
                Image image = new Image(file.toURI().toString());
                if (!image.isError()) {
                    images.add(image);
                } else {
                    System.err.println("無法載入圖片: " + file.getName());
                }
            } catch (Exception e) {
                System.err.println("載入圖片時發生錯誤: " + file.getName() + " - " + e.getMessage());
            }
        }
        
        return images;
    }

    /**
     * 檢查檔案是否為支援的圖片格式
     * 
     * @param file 檔案
     * @return 是否為支援的圖片格式
     */
    private boolean isSupportedImageFile(File file) {
        if (file == null || !file.isFile()) {
            return false;
        }

        String fileName = file.getName().toLowerCase();
        for (String extension : SUPPORTED_EXTENSIONS) {
            if (fileName.endsWith(extension)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 獲取支援的圖片格式列表
     * 
     * @return 支援的檔案副檔名陣列
     */
    public static String[] getSupportedExtensions() {
        return SUPPORTED_EXTENSIONS.clone();
    }

    /**
     * 驗證資料夾是否包含圖片檔案
     * 
     * @param folder 要檢查的資料夾
     * @return 是否包含支援的圖片檔案
     */
    public boolean hasImageFiles(File folder) {
        if (folder == null || !folder.exists() || !folder.isDirectory()) {
            return false;
        }

        File[] files = folder.listFiles(this::isSupportedImageFile);
        return files != null && files.length > 0;
    }

    /**
     * 獲取資料夾中圖片檔案的數量
     * 
     * @param folder 資料夾
     * @return 圖片檔案數量
     */
    public int getImageFileCount(File folder) {
        if (folder == null || !folder.exists() || !folder.isDirectory()) {
            return 0;
        }

        File[] files = folder.listFiles(this::isSupportedImageFile);
        return files != null ? files.length : 0;
    }

    /**
     * 載入單個圖片檔案
     * 
     * @param imageFile 圖片檔案
     * @return 包含該圖片的圖片列表
     */
    public List<Image> loadImage(File imageFile) {
        List<Image> images = new ArrayList<>();
        
        if (imageFile == null || !imageFile.exists() || !imageFile.isFile()) {
            return images;
        }
        
        if (!isSupportedImageFile(imageFile)) {
            return images;
        }
        
        try {
            Image image = new Image(imageFile.toURI().toString());
            if (!image.isError()) {
                images.add(image);
            } else {
                System.err.println("無法載入圖片: " + imageFile.getName());
            }
        } catch (Exception e) {
            System.err.println("載入圖片時發生錯誤: " + imageFile.getName() + " - " + e.getMessage());
        }
        
        return images;
    }
}
