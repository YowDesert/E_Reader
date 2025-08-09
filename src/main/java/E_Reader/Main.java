package E_Reader;

import E_Reader.core.EpubLoader;
import E_Reader.ui.MainController;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

/**
 * 主應用程式類 - 簡化版本，使用MainController處理所有邏輯
 */
public class Main extends Application {

    private MainController mainController;
    private Stage loadingStage; // 載入進度視窗

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

                    // 開啟選中的檔案（使用新的非阻塞方法）
                    openSelectedFileAsync(file);

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
        // 關閉載入視窗
        if (loadingStage != null) {
            loadingStage.close();
        }
    }

    /**
     * 非阻塞方式處理選中的檔案
     */
    private void openSelectedFileAsync(java.io.File file) {
        if (file == null || !file.exists()) {
            showError("錯誤", "檔案不存在或已被移動");
            return;
        }

        String fileName = file.getName().toLowerCase();

        if (fileName.endsWith(".pdf")) {
            // 開啟PDF檔案 - 使用非阻塞載入
            openPdfFileAsync(file);
        } else if (fileName.endsWith(".epub")) {
            // 開啟EPUB檔案
            openEpubFile(file);
        } else if (fileName.endsWith(".txt")) {
            // 開啟TXT檔案
            openTextFile(file);
        } else if (isImageFile(fileName)) {
            // 開啟圖片檔案
            openImageFile(file);
        } else {
            // 不支援的檔案格式，嘗試使用系統預設程式開啟
            openWithSystemDefault(file);
        }
    }

    /**
     * 非阻塞方式開啟PDF檔案
     */
    private void openPdfFileAsync(java.io.File file) {
        // 顯示載入進度視窗
        showLoadingProgress("正在載入PDF檔案", "請稍候，正在處理 " + file.getName() + "...");

        // 創建後台任務
        Task<java.util.List<javafx.scene.image.Image>> loadTask = new Task<java.util.List<javafx.scene.image.Image>>() {
            @Override
            protected java.util.List<javafx.scene.image.Image> call() throws Exception {
                updateMessage("正在開啟PDF檔案...");
                updateProgress(-1, 1); // 不確定進度

                try {
                    // 載入PDF圖片
                    var images = mainController.getPdfLoader().loadImagesFromPdf(file);
                    updateMessage("載入完成，共 " + images.size() + " 頁");
                    updateProgress(1, 1); // 完成
                    return images;
                } catch (Exception e) {
                    updateMessage("載入失敗: " + e.getMessage());
                    throw e;
                }
            }

            @Override
            protected void succeeded() {
                Platform.runLater(() -> {
                    hideLoadingProgress();
                    var images = getValue();
                    if (images != null && !images.isEmpty()) {
                        mainController.getStateManager().setFileLoaded(file.getAbsolutePath(), true, false, images, null);
                        mainController.getImageViewer().setImages(images);
                        mainController.getPrimaryStage().setTitle("E_Reader - " + file.getName());

                        showSuccess("檔案開啟", "成功開啟 PDF檔案: " + file.getName() + "，共 " + images.size() + " 頁");
                    } else {
                        showError("載入失敗", "PDF檔案中沒有可讀取的內容");
                    }
                });
            }

            @Override
            protected void failed() {
                Platform.runLater(() -> {
                    hideLoadingProgress();
                    Throwable exception = getException();
                    showError("無法載入 PDF 檔案", exception.getMessage());
                });
            }

            @Override
            protected void cancelled() {
                Platform.runLater(() -> {
                    hideLoadingProgress();
                    showError("載入取消", "PDF檔案載入已取消");
                });
            }
        };

        // 綁定進度和訊息更新
        updateLoadingProgress(loadTask);

        // 在後台執行任務
        Thread loadThread = new Thread(loadTask);
        loadThread.setDaemon(true);
        loadThread.start();
    }

    /**
     * 開啟EPUB檔案
     */
    private void openEpubFile(java.io.File file) {
        try {
            var images = mainController.getEpubLoader().loadImagesFromEpub(file);
            if (!images.isEmpty()) {
                mainController.getStateManager().setFileLoaded(file.getAbsolutePath(), false, true, images, null);
                mainController.getImageViewer().setImages(images);
                mainController.getPrimaryStage().setTitle("E_Reader - " + file.getName());

                showSuccess("檔案開啟", "成功開啟 EPUB檔案: " + file.getName());
            } else {
                showError("載入失敗", "EPUB檔案中沒有可讀取的內容");
            }
        } catch (Exception ex) {
            showError("無法載入 EPUB 檔案", ex.getMessage());
        }
    }

    /**
     * 開啟TXT檔案
     */
    private void openTextFile(java.io.File file) {
        try {
            var images = mainController.getTextLoader().loadImagesFromText(file);
            if (!images.isEmpty()) {
                mainController.getStateManager().setFileLoaded(file.getAbsolutePath(), false, false, images, null);
                mainController.getImageViewer().setImages(images);
                mainController.getPrimaryStage().setTitle("E_Reader - " + file.getName());

                showSuccess("檔案開啟", "成功開啟 TXT檔案: " + file.getName());
            } else {
                showError("載入失敗", "TXT檔案中沒有可讀取的內容");
            }
        } catch (Exception ex) {
            showError("無法載入 TXT 檔案", ex.getMessage());
        }
    }

    /**
     * 顯示載入進度視窗
     */
    private void showLoadingProgress(String title, String message) {
        if (loadingStage != null) {
            loadingStage.close();
        }

        loadingStage = new Stage();
        loadingStage.initStyle(StageStyle.UNDECORATED);
        loadingStage.initModality(Modality.APPLICATION_MODAL);
        loadingStage.initOwner(mainController.getPrimaryStage());

        VBox content = new VBox(20);
        content.setAlignment(Pos.CENTER);
        content.setStyle("-fx-background-color: white; -fx-padding: 30; -fx-background-radius: 10; " +
                "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.3), 10, 0, 0, 5);");

        Label titleLabel = new Label(title);
        titleLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #333;");

        Label messageLabel = new Label(message);
        messageLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #666;");

        ProgressIndicator progressIndicator = new ProgressIndicator();
        progressIndicator.setPrefSize(50, 50);

        Button cancelButton = new Button("取消");
        cancelButton.setStyle("-fx-background-color: #6c757d; -fx-text-fill: white; -fx-padding: 8 20;");
        cancelButton.setOnAction(e -> {
            loadingStage.close();
            // 這裡可以加入取消載入的邏輯
        });

        content.getChildren().addAll(titleLabel, messageLabel, progressIndicator, cancelButton);

        StackPane root = new StackPane(content);
        root.setStyle("-fx-background-color: rgba(0,0,0,0.3);");

        Scene scene = new Scene(root, 400, 250);
        loadingStage.setScene(scene);
        loadingStage.show();
    }

    /**
     * 更新載入進度
     */
    private void updateLoadingProgress(Task<?> task) {
        if (loadingStage == null) return;

        VBox content = (VBox) ((StackPane) loadingStage.getScene().getRoot()).getChildren().get(0);
        Label messageLabel = (Label) content.getChildren().get(1);
        ProgressIndicator progressIndicator = (ProgressIndicator) content.getChildren().get(2);

        // 綁定進度和訊息
        messageLabel.textProperty().bind(task.messageProperty());
        progressIndicator.progressProperty().bind(task.progressProperty());
    }

    /**
     * 隱藏載入進度視窗
     */
    private void hideLoadingProgress() {
        if (loadingStage != null) {
            loadingStage.close();
            loadingStage = null;
        }
    }

    /**
     * 檢查是否為圖片檔案
     */
    private boolean isImageFile(String fileName) {
        return fileName.endsWith(".jpg") || fileName.endsWith(".jpeg") ||
                fileName.endsWith(".png") || fileName.endsWith(".gif") ||
                fileName.endsWith(".bmp") || fileName.endsWith(".tiff") ||
                fileName.endsWith(".webp");
    }

    /**
     * 開啟圖片檔案 - 改善的處理方式
     */
    private void openImageFile(java.io.File file) {
        java.io.File parentFolder = file.getParentFile();
        if (parentFolder == null) {
            showError("載入失敗", "無法存取檔案的父資料夾");
            return;
        }

        try {
            // 載入整個資料夾的圖片
            var images = mainController.getImageLoader().loadImagesFromFolder(parentFolder);
            if (images.isEmpty()) {
                showError("載入失敗", "資料夾中沒有找到支援的圖片格式");
                return;
            }

            mainController.getStateManager().setFileLoaded(parentFolder.getAbsolutePath(), false, false, images, null);
            mainController.getImageViewer().setImages(images);

            // 找到當前檔案在圖片列表中的索引位置
            int targetIndex = findImageIndex(parentFolder, file, images);
            if (targetIndex >= 0) {
                mainController.getImageViewer().goToPage(targetIndex);
                showSuccess("檔案開啟",
                        String.format("成功載入圖片資料夾: %s (共 %d 張圖片，目前第 %d 張)",
                                parentFolder.getName(), images.size(), targetIndex + 1));
            } else {
                mainController.getImageViewer().goToFirstPage();
                showSuccess("檔案開啟",
                        String.format("成功載入圖片資料夾: %s (共 %d 張圖片)",
                                parentFolder.getName(), images.size()));
            }

            mainController.getPrimaryStage().setTitle("E_Reader - " + parentFolder.getName());

        } catch (Exception ex) {
            showError("無法載入圖片資料夾", ex.getMessage());
        }
    }

    /**
     * 找到目標圖片檔案在圖片列表中的索引
     */
    private int findImageIndex(java.io.File parentFolder, java.io.File targetFile, java.util.List<javafx.scene.image.Image> images) {
        try {
            // 獲取資料夾中所有支援的圖片檔案
            java.io.File[] imageFiles = parentFolder.listFiles(this::isSupportedImageFile);
            if (imageFiles == null) return -1;

            // 按檔名排序（與ImageLoader中的排序方式一致）
            java.util.Arrays.sort(imageFiles, java.util.Comparator.comparing(java.io.File::getName));

            // 找到目標檔案的索引
            for (int i = 0; i < imageFiles.length; i++) {
                if (imageFiles[i].equals(targetFile)) {
                    return i;
                }
            }
        } catch (Exception e) {
            System.err.println("尋找圖片索引時發生錯誤: " + e.getMessage());
        }
        return -1;
    }

    /**
     * 檢查檔案是否為支援的圖片格式
     */
    private boolean isSupportedImageFile(java.io.File file) {
        if (file == null || !file.isFile()) {
            return false;
        }
        String fileName = file.getName().toLowerCase();
        return fileName.endsWith(".jpg") || fileName.endsWith(".jpeg") ||
                fileName.endsWith(".png") || fileName.endsWith(".gif") ||
                fileName.endsWith(".bmp") || fileName.endsWith(".tiff") ||
                fileName.endsWith(".webp");
    }

    /**
     * 使用系統預設程式開啟檔案
     */
    private void openWithSystemDefault(java.io.File file) {
        try {
            if (java.awt.Desktop.isDesktopSupported()) {
                java.awt.Desktop desktop = java.awt.Desktop.getDesktop();
                if (desktop.isSupported(java.awt.Desktop.Action.OPEN)) {
                    desktop.open(file);
                    showSuccess("檔案開啟", "已使用系統預設程式開啟: " + file.getName());
                } else {
                    showUnsupportedFileDialog(file);
                }
            } else {
                showUnsupportedFileDialog(file);
            }
        } catch (Exception e) {
            System.err.println("使用系統預設程式開啟檔案失敗: " + e.getMessage());
            showError("開啟失敗",
                    "無法開啟檔案: " + file.getName() + "\n\n" +
                            "錯誤原因: " + e.getMessage() + "\n\n" +
                            "建議：請手動使用適當的程式開啟此檔案。");
        }
    }

    /**
     * 顯示不支援的檔案類型對話框
     */
    private void showUnsupportedFileDialog(java.io.File file) {
        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(
                javafx.scene.control.Alert.AlertType.INFORMATION);
        alert.setTitle("檔案類型資訊");
        alert.setHeaderText("檔案: " + file.getName());

        StringBuilder content = new StringBuilder();
        content.append("此檔案類型不支援在E_Reader中直接開啟。\n\n");
        content.append("支援的檔案格式：\n");
        content.append("• PDF檔案 (.pdf)\n");
        content.append("• EPUB電子書 (.epub)\n");
        content.append("• 圖片檔案 (.jpg, .png, .gif, .bmp, .tiff, .webp)\n\n");
        content.append("你可以：\n");
        content.append("1. 點選「使用系統程式開啟」來用預設程式開啟檔案\n");
        content.append("2. 或者手動使用適當的程式開啟此檔案");

        alert.setContentText(content.toString());

        // 新增「使用系統程式開啟」按鈕
        javafx.scene.control.ButtonType openWithSystemButton =
                new javafx.scene.control.ButtonType("使用系統程式開啟");
        javafx.scene.control.ButtonType cancelButton =
                new javafx.scene.control.ButtonType("取消", javafx.scene.control.ButtonBar.ButtonData.CANCEL_CLOSE);

        alert.getButtonTypes().setAll(openWithSystemButton, cancelButton);

        java.util.Optional<javafx.scene.control.ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == openWithSystemButton) {
            try {
                if (java.awt.Desktop.isDesktopSupported()) {
                    java.awt.Desktop.getDesktop().open(file);
                }
            } catch (Exception e) {
                showError("開啟失敗", "無法使用系統預設程式開啟檔案: " + e.getMessage());
            }
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