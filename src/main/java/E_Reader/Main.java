package E_Reader;

import E_Reader.ui.MainController;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.stage.Stage;

/**
 * 主應用程式類 - 簡化版本，使用MainController處理所有邏輯
 */
public class Main extends Application {

    private MainController mainController;

    @Override
    public void start(Stage primaryStage) {
        try {
            // 創建檔案管理器視窗
            Stage fileManagerStage = new Stage();
            fileManagerStage.setTitle("E_Reader - 檔案管理器");
            
            // 初始化主控制器（但不顯示主視窗）
            mainController = new MainController(primaryStage);
            
            // 獲取檔案管理器控制器
            var fileManagerController = mainController.getFileManagerController();
            
            // 設定檔案開啟回調 - 當用戶選擇檔案時啟動閱讀器
            fileManagerController.initialize(file -> {
                try {
                    // 初始化主控制器
                    mainController.initialize();
                    
                    // 關閉檔案管理器
                    fileManagerStage.hide();
                    
                    // 顯示主閱讀器視窗
                    primaryStage.show();
                    
                    // 開啟選中的檔案
                    openSelectedFile(file);
                    
                } catch (Exception ex) {
                    System.err.println("開啟檔案失敗: " + ex.getMessage());
                    ex.printStackTrace();
                    showFileOpenError(primaryStage, ex, file.getName());
                }
            });
            
            // 設定檔案管理器視窗
            fileManagerStage.setOnCloseRequest(e -> {
                // 如果用戶關閉檔案管理器而沒有選擇檔案，結束應用程式
                if (!primaryStage.isShowing()) {
                    Platform.exit();
                }
            });
            
            // 顯示檔案管理器
            fileManagerController.show();
            
        } catch (Exception e) {
            System.err.println("應用程式啟動失敗: " + e.getMessage());
            e.printStackTrace();
            
            // 顯示錯誤訊息
            showStartupError(primaryStage, e);
        }
    }

    @Override
    public void stop() {
        // 清理資源（如果主控制器已初始化）
        if (mainController != null) {
            // MainController 會在視窗關閉時自動處理清理
        }
    }

    /**
     * 處理選中的檔案
     */
    private void openSelectedFile(java.io.File file) {
        if (file == null || !file.exists()) {
            showError("錯誤", "檔案不存在或已被移動");
            return;
        }
        
        String fileName = file.getName().toLowerCase();
        
        if (fileName.endsWith(".pdf")) {
            // 開啟PDF檔案
            try {
                var images = mainController.getPdfLoader().loadImagesFromPdf(file);
                if (!images.isEmpty()) {
                    mainController.getStateManager().setFileLoaded(file.getAbsolutePath(), true, false, images, null);
                    mainController.getImageViewer().setImages(images);
                    mainController.getPrimaryStage().setTitle("E_Reader - " + file.getName());
                    
                    showSuccess("檔案開啟", "成功開啟 PDF檔案: " + file.getName());
                }
            } catch (Exception ex) {
                showError("無法載入 PDF 檔案", ex.getMessage());
            }
        } else if (fileName.endsWith(".jpg") || fileName.endsWith(".jpeg") || 
                   fileName.endsWith(".png") || fileName.endsWith(".gif") || 
                   fileName.endsWith(".bmp")) {
            // 開啟圖片檔案 - 載入整個資料夾
            java.io.File parentFolder = file.getParentFile();
            if (parentFolder != null) {
                var images = mainController.getImageLoader().loadImagesFromFolder(parentFolder);
                if (!images.isEmpty()) {
                    mainController.getStateManager().setFileLoaded(parentFolder.getAbsolutePath(), false, false, images, null);
                    mainController.getImageViewer().setImages(images);
                    
                    // 找到當前檔案的索引並跳轉到該頁
                    // 暫時跳到第一張圖片
                    
                    mainController.getPrimaryStage().setTitle("E_Reader - " + parentFolder.getName());
                    
                    showSuccess("檔案開啟", "成功載入圖片資料夾: " + parentFolder.getName());
                } else {
                    showError("載入失敗", "資料夾中沒有找到支援的圖片格式");
                }
            }
        } else {
            showError("不支援的檔案格式", 
                "只支援 PDF 檔案和圖片檔案 (JPG, PNG, GIF, BMP)");
        }
    }
    
    /**
     * 顯示檔案開啟錯誤訊息
     */
    private void showFileOpenError(Stage stage, Exception e, String fileName) {
        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(
                javafx.scene.control.Alert.AlertType.ERROR);
        alert.setTitle("檔案開啟錯誤");
        alert.setHeaderText("無法開啟檔案: " + fileName);
        alert.setContentText("錯誤詳情：\n" + e.getMessage() + 
                "\n\n可能的原因：" +
                "\n• 檔案格式不受支援" +
                "\n• 檔案已損壞或不完整" +
                "\n• 缺少必要的編解碼器" +
                "\n• 記憶體不足" +
                "\n• 檔案正被其他程式使用");
        
        if (stage != null) {
            alert.initOwner(stage);
        }
        
        alert.showAndWait();
    }
    
    /**
     * 顯示成功訊息
     */
    private void showSuccess(String title, String message) {
        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(
                javafx.scene.control.Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.show();
        
        // 3秒後自動關閉
        java.util.Timer timer = new java.util.Timer();
        timer.schedule(new java.util.TimerTask() {
            @Override
            public void run() {
                javafx.application.Platform.runLater(() -> alert.close());
            }
        }, 3000);
    }
    
    /**
     * 顯示錯誤訊息
     */
    private void showError(String title, String message) {
        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(
                javafx.scene.control.Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    /**
     * 顯示啟動錯誤訊息
     */
    private void showStartupError(Stage stage, Exception e) {
        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(
                javafx.scene.control.Alert.AlertType.ERROR);
        alert.setTitle("啟動錯誤");
        alert.setHeaderText("E-Reader 無法正常啟動");
        alert.setContentText("錯誤詳情：\n" + e.getMessage() + 
                "\n\n請檢查以下項目：" +
                "\n• Java 版本是否為 17 或更高" +
                "\n• JavaFX 運行時是否正確安裝" +
                "\n• 相關依賴庫是否完整" +
                "\n• 系統記憶體是否充足");
        
        if (stage != null) {
            alert.initOwner(stage);
        }
        
        alert.showAndWait();
    }

    /**
     * 主程式入口點
     * 
     * @param args 命令行參數
     */
    public static void main(String[] args) {
        // 設定系統屬性
        System.setProperty("javafx.preloader", "");
        System.setProperty("file.encoding", "UTF-8");
        
        // 設定 JavaFX 相關屬性（可選）
        System.setProperty("prism.lcdtext", "false");  // 改善文字渲染
        System.setProperty("prism.text", "t2k");       // 使用 T2K 文字引擎
        
        try {
            // 啟動 JavaFX 應用程式
            launch(args);
        } catch (Exception e) {
            System.err.println("應用程式啟動失敗: " + e.getMessage());
            e.printStackTrace();
            
            // 嘗試使用 Swing 顯示錯誤訊息
            showSwingError(e);
            
            System.exit(1);
        }
    }

    /**
     * 使用 Swing 顯示錯誤訊息（當 JavaFX 無法啟動時）
     */
    private static void showSwingError(Exception e) {
        try {
            javax.swing.SwingUtilities.invokeLater(() -> {
                javax.swing.JOptionPane.showMessageDialog(
                        null,
                        "E-Reader 無法啟動\n\n" +
                                "錯誤訊息: " + e.getMessage() + "\n\n" +
                                "可能的解決方案:\n" +
                                "1. 確認已安裝 Java 17 或更高版本\n" +
                                "2. 確認 JavaFX 運行時已正確安裝\n" +
                                "3. 檢查系統記憶體是否充足\n" +
                                "4. 嘗試以管理員權限執行\n" +
                                "5. 查看控制台輸出以獲取更多資訊",
                        "E-Reader 啟動錯誤",
                        javax.swing.JOptionPane.ERROR_MESSAGE
                );
            });
        } catch (Exception swingError) {
            // 如果連 Swing 都無法使用，就直接輸出到控制台
            System.err.println("無法顯示圖形錯誤對話框");
            System.err.println("原始錯誤: " + e.getMessage());
            System.err.println("Swing 錯誤: " + swingError.getMessage());
        }
    }
}
