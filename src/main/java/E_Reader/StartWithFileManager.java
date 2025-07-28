package E_Reader;

import E_Reader.filemanager.FileManagerController;
import E_Reader.ui.MainController;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.stage.Stage;

/**
 * 以檔案管理器模式啟動的替代啟動器
 * 讓用戶像 GoodNotes 一樣先選擇檔案再開始閱讀
 */
public class StartWithFileManager extends Application {

    private MainController mainController;
    private Stage readerStage;
    private Stage fileManagerStage;

    @Override
    public void start(Stage primaryStage) {
        try {
            // 創建閱讀器主視窗（但先隱藏）
            readerStage = primaryStage;
            readerStage.setTitle("E_Reader 閱讀器");
            
            // 創建檔案管理器視窗
            fileManagerStage = new Stage();
            fileManagerStage.setTitle("E_Reader - 選擇檔案");
            
            // 初始化主控制器
            mainController = new MainController(readerStage);
            
            // 獲取檔案管理器控制器並設置為新的視窗
            FileManagerController fileManagerController = new FileManagerController(fileManagerStage);
            
            // 設定檔案選擇回調
            fileManagerController.initialize(file -> {
                try {
                    // 初始化閱讀器
                    mainController.initialize();
                    
                    // 隱藏檔案管理器
                    fileManagerStage.hide();
                    
                    // 顯示閱讀器
                    readerStage.show();
                    
                    // 開啟選中的檔案
                    openSelectedFileInReader(file);
                    
                    // 在閱讀器中添加返回檔案管理器的功能
                    setupBackToFileManager();
                    
                } catch (Exception ex) {
                    showError("開啟檔案失敗", "無法開啟選中的檔案: " + ex.getMessage());
                    ex.printStackTrace();
                }
            });
            
            // 設定檔案管理器關閉事件
            fileManagerStage.setOnCloseRequest(e -> {
                if (!readerStage.isShowing()) {
                    Platform.exit();
                }
            });
            
            // 設定閱讀器關閉事件 - 返回檔案選擇器
            readerStage.setOnCloseRequest(e -> {
                e.consume(); // 阻止默認關閉行為
                showFileManagerAgain();
            });
            
            // 顯示檔案管理器
            fileManagerController.show();
            
            // 顯示歡迎訊息
            showWelcomeMessage();
            
        } catch (Exception e) {
            System.err.println("應用程式啟動失敗: " + e.getMessage());
            e.printStackTrace();
            showError("啟動錯誤", "E-Reader 無法正常啟動: " + e.getMessage());
            Platform.exit();
        }
    }

    /**
     * 在閱讀器中開啟選中的檔案
     */
    private void openSelectedFileInReader(java.io.File file) {
        if (file == null || !file.exists()) {
            showError("錯誤", "檔案不存在或已被移動");
            return;
        }
        
        String fileName = file.getName().toLowerCase();
        
        try {
            if (fileName.endsWith(".pdf")) {
                // 開啟PDF檔案
                var images = mainController.getPdfLoader().loadImagesFromPdf(file);
                if (!images.isEmpty()) {
                    mainController.getStateManager().setFileLoaded(file.getAbsolutePath(), true, false, images, null);
                    mainController.getImageViewer().setImages(images);
                    readerStage.setTitle("E_Reader - " + file.getName());
                    
                    showSuccess("已開啟 PDF 檔案: " + file.getName());
                } else {
                    showError("PDF 載入失敗", "PDF 檔案中沒有找到可顯示的內容");
                }
            } else if (isImageFile(fileName)) {
                // 開啟圖片檔案 - 載入整個資料夾
                java.io.File parentFolder = file.getParentFile();
                if (parentFolder != null) {
                    var images = mainController.getImageLoader().loadImagesFromFolder(parentFolder);
                    if (!images.isEmpty()) {
                        mainController.getStateManager().setFileLoaded(parentFolder.getAbsolutePath(), false, false, images, null);
                        mainController.getImageViewer().setImages(images);
                        
                        // TODO: 跳轉到選中的圖片
                        
                        readerStage.setTitle("E_Reader - " + parentFolder.getName());
                        
                        showSuccess("已載入圖片資料夾: " + parentFolder.getName() + " (" + images.size() + " 張圖片)");
                    } else {
                        showError("載入失敗", "資料夾中沒有找到支援的圖片格式");
                    }
                }
            } else {
                showError("不支援的檔案格式", 
                    "只支援 PDF 檔案和圖片檔案 (JPG, PNG, GIF, BMP)\n\n選中的檔案: " + file.getName());
            }
        } catch (Exception ex) {
            showError("載入檔案時發生錯誤", ex.getMessage());
            ex.printStackTrace();
        }
    }
    
    /**
     * 檢查是否為支援的圖片檔案
     */
    private boolean isImageFile(String fileName) {
        return fileName.endsWith(".jpg") || fileName.endsWith(".jpeg") || 
               fileName.endsWith(".png") || fileName.endsWith(".gif") || 
               fileName.endsWith(".bmp");
    }
    
    /**
     * 設定返回檔案管理器的功能
     */
    private void setupBackToFileManager() {
        // 在這裡可以添加快捷鍵或按鈕來返回檔案管理器
        // 例如 Ctrl+O 或 F12
        readerStage.getScene().setOnKeyPressed(e -> {
            if (e.isControlDown() && e.getCode() == javafx.scene.input.KeyCode.O) {
                showFileManagerAgain();
            } else if (e.getCode() == javafx.scene.input.KeyCode.F12) {
                showFileManagerAgain();
            }
        });
    }
    
    /**
     * 再次顯示檔案管理器
     */
    private void showFileManagerAgain() {
        // 隱藏閱讀器
        readerStage.hide();
        
        // 顯示檔案管理器
        fileManagerStage.show();
        fileManagerStage.toFront();
    }
    
    /**
     * 顯示歡迎訊息
     */
    private void showWelcomeMessage() {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("歡迎使用 E_Reader");
            alert.setHeaderText("檔案選擇模式");
            alert.setContentText("🎉 歡迎使用 E_Reader！\n\n" +
                    "📁 請從檔案管理器中選擇您想要閱讀的檔案\n" +
                    "📄 支援 PDF 檔案\n" +
                    "🖼️ 支援圖片檔案 (JPG, PNG, GIF, BMP)\n\n" +
                    "💡 小提示：\n" +
                    "• 在閱讀器中按 Ctrl+O 可返回檔案選擇\n" +
                    "• 按 F12 也可以返回檔案選擇\n" +
                    "• 支援拖拽檔案到檔案管理器");
                    
            alert.show();
            
            // 5秒後自動關閉
            java.util.Timer timer = new java.util.Timer();
            timer.schedule(new java.util.TimerTask() {
                @Override
                public void run() {
                    Platform.runLater(() -> alert.close());
                }
            }, 5000);
        });
    }
    
    /**
     * 顯示成功訊息
     */
    private void showSuccess(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("成功");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.show();
        
        // 3秒後自動關閉
        java.util.Timer timer = new java.util.Timer();
        timer.schedule(new java.util.TimerTask() {
            @Override
            public void run() {
                Platform.runLater(() -> alert.close());
            }
        }, 3000);
    }
    
    /**
     * 顯示錯誤訊息
     */
    private void showError(String title, String message) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(message);
            alert.showAndWait();
        });
    }

    @Override
    public void stop() {
        // 清理資源
        if (mainController != null) {
            // MainController 會處理資源清理
        }
    }

    /**
     * 主程式入口點
     */
    public static void main(String[] args) {
        // 設定系統屬性
        System.setProperty("javafx.preloader", "");
        System.setProperty("file.encoding", "UTF-8");
        System.setProperty("prism.lcdtext", "false");
        System.setProperty("prism.text", "t2k");
        
        System.out.println("🚀 啟動 E_Reader 檔案選擇模式...");
        
        try {
            launch(args);
        } catch (Exception e) {
            System.err.println("應用程式啟動失敗: " + e.getMessage());
            e.printStackTrace();
            
            // 嘗試使用 Swing 顯示錯誤訊息
            try {
                javax.swing.SwingUtilities.invokeLater(() -> {
                    javax.swing.JOptionPane.showMessageDialog(
                            null,
                            "E-Reader 無法啟動\n\n" +
                            "錯誤訊息: " + e.getMessage() + "\n\n" +
                            "請檢查：\n" +
                            "1. Java 17 或更高版本\n" +
                            "2. JavaFX 運行時環境\n" +
                            "3. 系統記憶體\n" +
                            "4. 檔案權限",
                            "E-Reader 啟動錯誤",
                            javax.swing.JOptionPane.ERROR_MESSAGE
                    );
                });
            } catch (Exception swingError) {
                System.err.println("連圖形錯誤對話框都無法顯示，請檢查 Java 安裝");
            }
            
            System.exit(1);
        }
    }
}
