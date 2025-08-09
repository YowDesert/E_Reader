package E_Reader.ui;

import E_Reader.core.*;
import E_Reader.ui.components.LoadingProgressBar;
import E_Reader.filemanager.FileManagerController;
import E_Reader.settings.SettingsManager;
import E_Reader.utils.AlertHelper;
import E_Reader.viewer.*;
import javafx.animation.*;
import javafx.scene.layout.*;
import javafx.stage.*;
import javafx.util.StringConverter;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.paint.LinearGradient;
import javafx.scene.image.Image;

import java.io.File;
import java.io.FileOutputStream;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

import javafx.geometry.Rectangle2D;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;

/**
 * 主控制器類 - 負責協調各個組件間的交互
 */
public class MainController {

    // 核心組件
    private final Stage primaryStage;
    private final ImageViewer imageViewer;
    private final TextRenderer textRenderer;
    private final ImageLoader imageLoader;
    private final PdfLoader pdfLoader;
    private final EpubLoader epubLoader;
    private final TextLoader textLoader;
    private final TextExtractor textExtractor;
    private final BookmarkManager bookmarkManager;
    private final NoteManager noteManager;
    private final SettingsManager settingsManager;
    private final FileManagerController fileManagerController;

    // UI 組件
    private final UIControlsFactory controlsFactory;
    private BorderPane mainLayout;
    private StackPane centerPane;
    private VBox controlsContainer;
    private HBox topControls;
    private HBox bottomControls;

    // 狀態管理
    private final StateManager stateManager;
    private final TimerManager timerManager;

    // UI 元素引用
    private Label pageLabel;
    private TextField pageField;
    private Button textModeBtn;
    private Button autoScrollBtn;
    private Button nightModeBtn;
    private Button eyeCareBtn;
    private Button toggleNavBarBtn;
    private Button focusModeBtn;
    private ProgressBar readingProgressBar;
    private Label readingTimeLabel;
    private LoadingProgressBar currentModernLoadingBar;

    // 專注模式狀態
    private boolean isFocusMode = false;

    // 導覽列控制器
    private NavigationBarController navBarController;

    private void showModernLoadingBar(LoadingProgressBar.LoadingType loadingType, String message) {
        Platform.runLater(() -> {
            // 如果已有載入條在顯示，先關閉它
            if (currentModernLoadingBar != null && currentModernLoadingBar.isShowing()) {
                currentModernLoadingBar.close();
            }

            currentModernLoadingBar = new LoadingProgressBar(loadingType);
            currentModernLoadingBar.setOwner(primaryStage);
            currentModernLoadingBar.updateMessage(message);
            currentModernLoadingBar.show();
        });
    }

    /**
     * 隱藏現代化載入條
     */
    private void hideModernLoadingBar() {
        Platform.runLater(() -> {
            if (currentModernLoadingBar != null && currentModernLoadingBar.isShowing()) {
                currentModernLoadingBar.hide();
                currentModernLoadingBar = null;
            }
        });
    }

    /**
     * 更新載入條進度
     */
    private void updateModernLoadingProgress(double progress) {
        Platform.runLater(() -> {
            if (currentModernLoadingBar != null && currentModernLoadingBar.isShowing()) {
                currentModernLoadingBar.updateProgress(progress);
            }
        });
    }

    /**
     * 更新載入條訊息
     */
    private void updateModernLoadingMessage(String message) {
        Platform.runLater(() -> {
            if (currentModernLoadingBar != null && currentModernLoadingBar.isShowing()) {
                currentModernLoadingBar.updateMessage(message);
            }
        });
    }
    public MainController(Stage primaryStage) {
        this.primaryStage = primaryStage;

        // 初始化核心組件
        this.settingsManager = new SettingsManager();
        this.imageViewer = new ImageViewer();
        this.textRenderer = new TextRenderer();
        this.imageLoader = new ImageLoader();
        this.pdfLoader = new PdfLoader();
        this.epubLoader = new EpubLoader();
        this.textLoader = new TextLoader(settingsManager);
        this.bookmarkManager = new BookmarkManager();
        this.noteManager = new NoteManager(settingsManager);
        this.textExtractor = new TextExtractor(settingsManager);
        this.fileManagerController = FileManagerController.getInstance();

        // 初始化管理器
        this.stateManager = new StateManager();
        this.timerManager = new TimerManager();
        this.controlsFactory = new UIControlsFactory();

        // 載入設定
        settingsManager.loadSettings();

        // 初始化檔案管理器
        initializeFileManager();
    }

    public void initialize() {
        setupMainLayout();
        setupEventHandlers();
        setupKeyboardShortcuts();

        // 確保設定正確載入和套用
        settingsManager.loadSettings();
        Platform.runLater(() -> {
            applyAllSettings();  // 使用完整的設定套用方法
        });

        // 啟動計時器
        timerManager.startReadingTimer(this::updateReadingTime);
        timerManager.startEyeCareReminder(() ->
                showNotification("護眼提醒", "您已經閱讀30分鐘了，建議休息5-10分鐘！"));

        setupWindowCloseHandler();

        // 如果設定要求記住最後檔案，嘗試載入
        if (settingsManager.isRememberLastFile()) {
            loadLastReadingPosition();
        }
    }

    private void setupMainLayout() {
        mainLayout = new BorderPane();

        // iOS風格的漸層背景 - 深色毛玻璃效果
        mainLayout.setStyle(
                "-fx-background-color: linear-gradient(to bottom, " +
                        "rgba(16,16,16,0.98) 0%, " +
                        "rgba(28,28,28,0.95) 30%, " +
                        "rgba(20,20,20,0.98) 70%, " +
                        "rgba(12,12,12,0.99) 100%); " +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.8), 25, 0, 0, 0);"
        );

        centerPane = createCenterPane();
        mainLayout.setCenter(centerPane);

        createControlPanels();
        controlsContainer = new VBox();
        controlsContainer.getChildren().addAll(topControls, bottomControls);

        // iOS風格的陰影效果
        controlsContainer.setStyle(
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.7), 20, 0, 0, 5);"
        );

        mainLayout.setTop(controlsContainer);

        // 初始化導覽列控制器
        navBarController = new NavigationBarController(controlsContainer, primaryStage, centerPane);

        Scene scene = new Scene(mainLayout, 1200, 800);
        scene.getRoot().setStyle(
                "-fx-font-family: 'SF Pro Display', '.SF NS Text', 'Helvetica Neue', " +
                        "'PingFang SC', 'Microsoft JhengHei UI', 'Segoe UI', 'Noto Sans CJK TC', sans-serif; " +
                        "-fx-font-smoothing-type: lcd; " +
                        "-fx-font-weight: 400;"
        );

        primaryStage.setScene(scene);
        primaryStage.setTitle("📚 E-Reader Pro · 現代化閱讀體驗");

        try {
            primaryStage.getIcons().add(new javafx.scene.image.Image(
                    getClass().getResourceAsStream("/icons/app-icon.png")
            ));
        } catch (Exception e) {
            // 如果找不到圖示檔案，就忽略
        }
    }

    private StackPane createCenterPane() {
        StackPane centerPane = new StackPane();
        centerPane.getChildren().add(imageViewer.getScrollPane());

        // iOS風格閱讀進度條 - 更細緻的設計
        readingProgressBar = new ProgressBar(0);
        readingProgressBar.setPrefWidth(280);
        readingProgressBar.setPrefHeight(3);
        readingProgressBar.setStyle(
                "-fx-accent: linear-gradient(to right, #1abc9c, #3498db); " +
                        "-fx-background-color: rgba(255,255,255,0.1); " +
                        "-fx-background-radius: 1.5; " +
                        "-fx-background-insets: 0; " +
                        "-fx-effect: dropshadow(gaussian, rgba(26,188,156,0.4), 6, 0, 0, 1);"
        );
        StackPane.setAlignment(readingProgressBar, Pos.BOTTOM_CENTER);
        StackPane.setMargin(readingProgressBar, new Insets(0, 0, 25, 0));
        centerPane.getChildren().add(readingProgressBar);

        // iOS風格閱讀時間標籤 - 毛玻璃效果
        readingTimeLabel = new Label("閱讀時間: 00:00:00");
        readingTimeLabel.setStyle(
                "-fx-text-fill: rgba(255,255,255,0.95); " +
                        "-fx-background-color: linear-gradient(to right, " +
                        "rgba(26,188,156,0.3), rgba(52,152,219,0.3)); " +
                        "-fx-border-color: rgba(255,255,255,0.2); " +
                        "-fx-border-width: 0.5; " +
                        "-fx-border-radius: 15; " +
                        "-fx-background-radius: 15; " +
                        "-fx-padding: 8 16 8 16; " +
                        "-fx-font-size: 11px; " +
                        "-fx-font-weight: 500; " +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 8, 0, 0, 2);"
        );
        StackPane.setAlignment(readingTimeLabel, Pos.TOP_RIGHT);
        StackPane.setMargin(readingTimeLabel, new Insets(20, 20, 0, 0));
        centerPane.getChildren().add(readingTimeLabel);

        // iOS風格頁碼標籤 - 右下角圓角設計
        pageLabel = new Label("頁面: 0 / 0");
        pageLabel.setStyle(
                "-fx-text-fill: rgba(255,255,255,0.95); " +
                        "-fx-background-color: linear-gradient(to right, " +
                        "rgba(52,152,219,0.3), rgba(155,89,182,0.3)); " +
                        "-fx-border-color: rgba(255,255,255,0.2); " +
                        "-fx-border-width: 0.5; " +
                        "-fx-border-radius: 15; " +
                        "-fx-background-radius: 15; " +
                        "-fx-padding: 8 16 8 16; " +
                        "-fx-font-size: 11px; " +
                        "-fx-font-weight: 500; " +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 8, 0, 0, 2);"
        );
        StackPane.setAlignment(pageLabel, Pos.BOTTOM_RIGHT);
        StackPane.setMargin(pageLabel, new Insets(0, 20, 25, 0));
        centerPane.getChildren().add(pageLabel);

        // iOS風格專注模式提示標籤 - 更精緻的設計
        Label focusModeLabel = new Label("🎯 專注模式 · 按 F 或點擊中央退出");
        focusModeLabel.setStyle(
                "-fx-text-fill: rgba(255,255,255,0.95);" +
                        "-fx-background-color: linear-gradient(to right, " +
                        "rgba(155,89,182,0.4), rgba(142,68,173,0.4));" +
                        "-fx-border-color: rgba(255,255,255,0.3);" +
                        "-fx-border-width: 0.5;" +
                        "-fx-border-radius: 20;" +
                        "-fx-background-radius: 20;" +
                        "-fx-padding: 12 24 12 24;" +
                        "-fx-font-size: 13px;" +
                        "-fx-font-weight: 500;" +
                        "-fx-effect: dropshadow(gaussian, rgba(155,89,182,0.5), 15, 0, 0, 3);"
        );
        focusModeLabel.setVisible(false);
        focusModeLabel.setId("focusModeLabel");
        StackPane.setAlignment(focusModeLabel, Pos.TOP_CENTER);
        StackPane.setMargin(focusModeLabel, new Insets(30, 0, 0, 0));
        centerPane.getChildren().add(focusModeLabel);

        // iOS風格背景
        centerPane.setStyle(
                "-fx-background-color: linear-gradient(to bottom, " +
                        "rgba(18,18,18,0.98) 0%, " +
                        "rgba(25,25,25,0.95) 100%);"
        );

        return centerPane;
    }

    /**
     * 初始化檔案管理器
     */
    private void initializeFileManager() {
        fileManagerController.setSettingsManager(settingsManager);
        fileManagerController.setTextExtractor(textExtractor);
        fileManagerController.initialize(this::openFileFromManager);
    }

    /**
     * 開啟檔案管理器
     */
    public void showFileManager() {
        if (!stateManager.getCurrentFilePath().isEmpty()) {
            closeCurrentFile();
        }

        if (isFocusMode) {
            isFocusMode = false;
            exitFocusMode();
        }

        fileManagerController.show();
    }

    /**
     * 從檔案管理器開啟檔案
     */
    private void openFileFromManager(File file) {
        if (file == null || !file.exists()) {
            AlertHelper.showError("錯誤", "檔案不存在或已被移動");
            return;
        }

        String fileName = file.getName().toLowerCase();

        if (fileName.endsWith(".pdf")) {
            openPdfFromFile(file);
        } else if (fileName.endsWith(".epub")) {
            openEpubFromFile(file);
        } else if (isImageFile(fileName)) {
            openImageFromFile(file);
        } else {
            AlertHelper.showError("不支援的檔案格式",
                    "支援的格式：PDF檔案、EPUB檔案和圖片檔案 (JPG, PNG, GIF, BMP)");
        }
    }

//    private boolean isImageFile(String fileName) {
//        return fileName.endsWith(".jpg") || fileName.endsWith(".jpeg") ||
//                fileName.endsWith(".png") || fileName.endsWith(".gif") ||
//                fileName.endsWith(".bmp");
//    }

    private void openPdfFromFile(File file) {
        // **改善：在檔案驗證階段就立即顯示載入畫面**
        Platform.runLater(() -> {
            showModernLoadingBar(LoadingProgressBar.LoadingType.PDF_PROCESSING, "準備開啟 PDF 檔案: " + file.getName());
        });
        
        // 在新執行緒中處理 PDF
        Thread pdfThread = new Thread(() -> {
            try {
                // **新增：檔案初步驗證階段**
                Platform.runLater(() -> {
                    updateModernLoadingMessage("正在驗證 PDF 檔案...");
                    updateModernLoadingProgress(0.1);
                });
                
                // 短暫延遲讓使用者看到載入畫面已出現
                Thread.sleep(100);
                
                // 檢查檔案是否有效
                if (!file.exists() || !file.canRead()) {
                    Platform.runLater(() -> {
                        hideModernLoadingBar();
                        AlertHelper.showError("檔案錯誤", "無法讀取檔案: " + file.getName());
                    });
                    return;
                }
                
                Platform.runLater(() -> {
                    updateModernLoadingMessage("正在載入 PDF 內容...");
                    updateModernLoadingProgress(0.3);
                });
                
                // 載入PDF圖片
                List<Image> images = pdfLoader.loadImagesFromPdf(file);
                
                Platform.runLater(() -> {
                    updateModernLoadingMessage("正在處理圖片內容...");
                    updateModernLoadingProgress(0.8);
                });
                
                // 短暫延遲讓進度更明顯
                Thread.sleep(200);
                
                Platform.runLater(() -> {
                    updateModernLoadingProgress(1.0);
                    updateModernLoadingMessage("載入完成！");
                    
                    // 再短暫延遲讓使用者看到完成狀態
                    Timeline completeDelay = new Timeline(new KeyFrame(Duration.millis(300), e -> {
                        hideModernLoadingBar();
                        
                        if (!images.isEmpty()) {
                            stateManager.setFileLoaded(file.getAbsolutePath(), true, false, images, null);
                            switchToImageMode(0);
                            imageViewer.setImages(images);
                            stateManager.setCurrentImagePageIndex(0);
                            primaryStage.setTitle("E_Reader - " + file.getName());
                            updateUI();
                            resetTextModeButton();

                            showNotification("檔案開啟", "成功開啟 PDF檔案: " + file.getName());
                        } else {
                            AlertHelper.showError("載入失敗", "PDF 檔案中沒有可讀取的內容");
                        }
                    }));
                    completeDelay.play();
                });
                
            } catch (Exception ex) {
                Platform.runLater(() -> {
                    hideModernLoadingBar();
                    AlertHelper.showError("無法載入 PDF 檔案", ex.getMessage());
                });
            }
        });
        
        pdfThread.setDaemon(true);
        pdfThread.start();
    }

    private void openEpubFromFile(File file) {
        // **改善：在檔案驗證階段就立即顯示載入畫面**
        Platform.runLater(() -> {
            showModernLoadingBar(LoadingProgressBar.LoadingType.EPUB_PROCESSING, "準備開啟 EPUB 檔案: " + file.getName());
        });

        Thread epubThread = new Thread(() -> {
            try {
                // **新增：檔案初步驗證階段**
                Platform.runLater(() -> {
                    updateModernLoadingMessage("正在驗證 EPUB 檔案...");
                    updateModernLoadingProgress(0.1);
                });
                
                // 短暫延遲讓使用者看到載入畫面已出現
                Thread.sleep(100);
                
                // 檢查檔案是否有效
                if (!file.exists() || !file.canRead()) {
                    Platform.runLater(() -> {
                        hideModernLoadingBar();
                        AlertHelper.showError("檔案錯誤", "無法讀取檔案: " + file.getName());
                    });
                    return;
                }
                
                Platform.runLater(() -> {
                    updateModernLoadingMessage("正在載入 EPUB 內容...");
                    updateModernLoadingProgress(0.3);
                });
                
                // 載入EPUB圖片
                List<Image> images = epubLoader.loadImagesFromEpub(file);
                
                Platform.runLater(() -> {
                    updateModernLoadingMessage("正在處理內容結構...");
                    updateModernLoadingProgress(0.8);
                });
                
                // 短暫延遲讓進度更明顯
                Thread.sleep(200);
                
                Platform.runLater(() -> {
                    updateModernLoadingProgress(1.0);
                    updateModernLoadingMessage("載入完成！");
                    
                    // 再短暫延遲讓使用者看到完成狀態
                    Timeline completeDelay = new Timeline(new KeyFrame(Duration.millis(300), e -> {
                        hideModernLoadingBar();
                        
                        if (!images.isEmpty()) {
                            stateManager.setFileLoaded(file.getAbsolutePath(), false, true, images, null);
                            switchToImageMode(0);
                            imageViewer.setImages(images);
                            stateManager.setCurrentImagePageIndex(0);
                            primaryStage.setTitle("E_Reader - " + file.getName());
                            updateUI();
                            resetTextModeButton();

                            showNotification("檔案開啟", "成功開啟 EPUB檔案: " + file.getName());
                        } else {
                            AlertHelper.showError("載入失敗", "EPUB檔案中沒有可讀取的內容");
                        }
                    }));
                    completeDelay.play();
                });
                
            } catch (Exception ex) {
                Platform.runLater(() -> {
                    hideModernLoadingBar();
                    AlertHelper.showError("無法載入 EPUB 檔案", ex.getMessage());
                });
            }
        });

        epubThread.setDaemon(true);
        epubThread.start();
    }

    private void showPdfOpeningWarning(File file) {
        Alert warningAlert = new Alert(Alert.AlertType.WARNING);
        warningAlert.setTitle("檔案開啟警告");
        warningAlert.setHeaderText("即將開啟檔案");
        warningAlert.setContentText(
                "檔案: " + file.getName() + "\n\n" +
                        "⚠️ 注意事項:\n" +
                        "• 檔案可能需要較長的載入時間\n" +
                        "• 大型檔案會占用較多記憶體\n" +
                        "• 建議確保有足夠的系統資源\n\n" +
                        "是否確定要開啟此檔案？"
        );

        // 設定按鈕
        warningAlert.getButtonTypes().clear();
        warningAlert.getButtonTypes().addAll(
                ButtonType.OK,
                ButtonType.CANCEL
        );

        // 設定對話框樣式
        warningAlert.initOwner(primaryStage);
        warningAlert.initModality(Modality.WINDOW_MODAL);

        // 顯示對話框並處理結果
        Optional<ButtonType> result = warningAlert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            // 用戶確認開啟，立即開始載入流程
            startFileLoading(file);
        }
        // 如果用戶取消，就什麼都不做
    }
    private void startFileLoading(File file) {
        // 立即顯示進度條
        LoadingProgressBar.LoadingType loadingType = determineLoadingType(file);
        showModernLoadingBar(loadingType, "準備開啟檔案...");

        // 在背景執行緒中載入檔案
        Thread loadingThread = new Thread(() -> {
            try {
                // 更新進度條訊息
                Platform.runLater(() -> updateModernLoadingMessage("正在分析檔案..."));
                Thread.sleep(300); // 短暫延遲讓用戶看到進度條

                // 更新進度到10%
                Platform.runLater(() -> updateModernLoadingProgress(0.1));

                // 根據檔案類型載入
                String fileName = file.getName().toLowerCase();

                if (fileName.endsWith(".pdf")) {
                    loadPdfFile(file);
                } else if (fileName.endsWith(".epub")) {
                    loadEpubFile(file);
                } else if (isImageFile(fileName)) {
                    loadImageFile(file);
                } else if (isTextFile(fileName)) {
                    loadTextFile(file);
                } else {
                    throw new RuntimeException("不支援的檔案格式: " + fileName);
                }

            } catch (Exception e) {
                Platform.runLater(() -> {
                    hideModernLoadingBar();
                    AlertHelper.showError("載入失敗", "無法開啟檔案: " + e.getMessage());
                });
            }
        });

        loadingThread.setDaemon(true);
        loadingThread.start();
    }

    /**
     * 決定載入類型
     */
    private LoadingProgressBar.LoadingType determineLoadingType(File file) {
        String fileName = file.getName().toLowerCase();

        if (fileName.endsWith(".pdf")) {
            return LoadingProgressBar.LoadingType.PDF_PROCESSING;
        } else if (fileName.endsWith(".epub")) {
            return LoadingProgressBar.LoadingType.EPUB_PROCESSING;
        } else if (isImageFile(fileName)) {
            return LoadingProgressBar.LoadingType.IMAGE_LOADING;
        } else {
            return LoadingProgressBar.LoadingType.FILE_OPENING;
        }
    }

    /**
     * 載入PDF檔案（修改版本，包含進度更新）
     */
    private void loadPdfFile(File file) throws Exception {
        Platform.runLater(() -> updateModernLoadingMessage("正在載入PDF檔案..."));
        Platform.runLater(() -> updateModernLoadingProgress(0.2));

        try {
            // 先檢查PDF檔案是否有效
            if (!pdfLoader.isPdfFile(file)) {
                throw new RuntimeException("檔案不是有效的PDF格式");
            }

            Platform.runLater(() -> updateModernLoadingProgress(0.3));
            Platform.runLater(() -> updateModernLoadingMessage("正在分析PDF頁面..."));

            // 獲取頁面數量
            int pageCount = pdfLoader.getPageCount(file);

            Platform.runLater(() -> updateModernLoadingProgress(0.4));
            Platform.runLater(() -> updateModernLoadingMessage("正在渲染PDF頁面 (1/" + pageCount + ")..."));

            // 載入所有頁面
            List<javafx.scene.image.Image> images = pdfLoader.loadImagesFromPdf(file);

            Platform.runLater(() -> updateModernLoadingProgress(0.8));
            Platform.runLater(() -> updateModernLoadingMessage("正在準備顯示..."));

            // 在UI執行緒中設定圖片檢視器
            Platform.runLater(() -> {
                try {
                    imageViewer.setImages(images);
                    centerPane.getChildren().clear();
                    centerPane.getChildren().add(imageViewer.getScrollPane());

                    // 更新狀態
                    stateManager.setCurrentFile(file);
                    stateManager.setCurrentPage(0);
                    stateManager.setTotalPages(images.size());

                    // 更新UI控制項
                    updatePageControls();

                    // 更新進度到100%並隱藏進度條
                    updateModernLoadingProgress(1.0);
                    updateModernLoadingMessage("載入完成！");

                    // 延遲隱藏進度條，讓用戶看到完成狀態
                    Timeline delayHide = new Timeline(
                            new KeyFrame(Duration.millis(500), e -> hideModernLoadingBar())
                    );
                    delayHide.play();

                    // 顯示載入完成的通知
                    showNotification("檔案載入成功", "PDF檔案已成功載入，共 " + images.size() + " 頁");

                } catch (Exception e) {
                    hideModernLoadingBar();
                    AlertHelper.showError("顯示失敗", "無法顯示PDF內容: " + e.getMessage());
                }
            });

        } catch (Exception e) {
            throw new RuntimeException("PDF載入失敗: " + e.getMessage(), e);
        }
    }

    /**
     * 載入EPUB檔案（也添加進度條支援）
     */
    private void loadEpubFile(File file) throws Exception {
        Platform.runLater(() -> updateModernLoadingMessage("正在載入EPUB檔案..."));
        Platform.runLater(() -> updateModernLoadingProgress(0.2));

        try {
            // 載入EPUB內容
            String content = epubLoader.loadEpubContent(file);

            Platform.runLater(() -> updateModernLoadingProgress(0.6));
            Platform.runLater(() -> updateModernLoadingMessage("正在處理文字內容..."));

            // 在UI執行緒中設定文字渲染器
            Platform.runLater(() -> {
                try {
                    textRenderer.setContent(content);
                    centerPane.getChildren().clear();
                    centerPane.getChildren().add(textRenderer.getScrollPane());

                    // 更新狀態
                    stateManager.setCurrentFile(file);
                    stateManager.setCurrentPage(0);
                    stateManager.setTotalPages(1);

                    // 更新UI控制項
                    updatePageControls();

                    // 完成載入
                    updateModernLoadingProgress(1.0);
                    updateModernLoadingMessage("載入完成！");

                    Timeline delayHide = new Timeline(
                            new KeyFrame(Duration.millis(500), e -> hideModernLoadingBar())
                    );
                    delayHide.play();

                    showNotification("檔案載入成功", "EPUB檔案已成功載入");

                } catch (Exception e) {
                    hideModernLoadingBar();
                    AlertHelper.showError("顯示失敗", "無法顯示EPUB內容: " + e.getMessage());
                }
            });

        } catch (Exception e) {
            throw new RuntimeException("EPUB載入失敗: " + e.getMessage(), e);
        }
    }

    /**
     * 載入圖片檔案（添加進度條支援）
     */
    private void loadImageFile(File file) throws Exception {
        Platform.runLater(() -> updateModernLoadingMessage("正在載入圖片..."));
        Platform.runLater(() -> updateModernLoadingProgress(0.3));

        try {
            List<javafx.scene.image.Image> images = imageLoader.loadImage(file);

            Platform.runLater(() -> updateModernLoadingProgress(0.8));
            Platform.runLater(() -> updateModernLoadingMessage("正在準備顯示..."));

            Platform.runLater(() -> {
                try {
                    imageViewer.setImages(images);
                    centerPane.getChildren().clear();
                    centerPane.getChildren().add(imageViewer.getScrollPane());

                    // 更新狀態
                    stateManager.setCurrentFile(file);
                    stateManager.setCurrentPage(0);
                    stateManager.setTotalPages(images.size());

                    updatePageControls();

                    updateModernLoadingProgress(1.0);
                    updateModernLoadingMessage("載入完成！");

                    Timeline delayHide = new Timeline(
                            new KeyFrame(Duration.millis(300), e -> hideModernLoadingBar())
                    );
                    delayHide.play();

                    showNotification("檔案載入成功", "圖片檔案已成功載入");

                } catch (Exception e) {
                    hideModernLoadingBar();
                    AlertHelper.showError("顯示失敗", "無法顯示圖片內容: " + e.getMessage());
                }
            });

        } catch (Exception e) {
            throw new RuntimeException("圖片載入失敗: " + e.getMessage(), e);
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
     * 檢查是否為文字檔案
     */
    private boolean isTextFile(String fileName) {
        return fileName.endsWith(".txt") || fileName.endsWith(".md") ||
                fileName.endsWith(".doc") || fileName.endsWith(".docx");
    }

    /**
     * 載入文字檔案
     */
    private void loadTextFile(File file) throws Exception {
        Platform.runLater(() -> updateModernLoadingMessage("正在載入文字檔案..."));
        Platform.runLater(() -> updateModernLoadingProgress(0.3));

        try {
            String content = textLoader.loadTextFile(file);

            Platform.runLater(() -> updateModernLoadingProgress(0.8));
            Platform.runLater(() -> updateModernLoadingMessage("正在準備顯示..."));

            Platform.runLater(() -> {
                try {
                    textRenderer.setContent(content);
                    centerPane.getChildren().clear();
                    centerPane.getChildren().add(textRenderer.getScrollPane());

                    stateManager.setCurrentFile(file);
                    stateManager.setCurrentPage(0);
                    stateManager.setTotalPages(1);

                    updatePageControls();

                    updateModernLoadingProgress(1.0);
                    updateModernLoadingMessage("載入完成！");

                    Timeline delayHide = new Timeline(
                            new KeyFrame(Duration.millis(300), e -> hideModernLoadingBar())
                    );
                    delayHide.play();

                    showNotification("檔案載入成功", "文字檔案已成功載入");

                } catch (Exception e) {
                    hideModernLoadingBar();
                    AlertHelper.showError("顯示失敗", "無法顯示文字內容: " + e.getMessage());
                }
            });

        } catch (Exception e) {
            throw new RuntimeException("文字檔案載入失敗: " + e.getMessage(), e);
        }
    }

    private void openImageFromFile(File file) {
        File parentFolder = file.getParentFile();
        if (parentFolder != null) {
            List<Image> images = imageLoader.loadImagesFromFolder(parentFolder);
            if (!images.isEmpty()) {
                stateManager.setFileLoaded(parentFolder.getAbsolutePath(), false, false, images, null);
                switchToImageMode(0);
                imageViewer.setImages(images);
                stateManager.setCurrentImagePageIndex(0);

                primaryStage.setTitle("E_Reader - " + parentFolder.getName());
                updateUI();
                resetTextModeButton();

                showNotification("檔案開啟", "成功載入圖片資料夾: " + parentFolder.getName());
            } else {
                AlertHelper.showError("載入失敗", "資料夾中沒有找到支援的圖片格式");
            }
        }
    }

    // 文件操作方法
    public void openImageFolder() {
        DirectoryChooser dc = new DirectoryChooser();
        dc.setTitle("選擇圖片資料夾");
        File folder = dc.showDialog(primaryStage);

        if (folder != null) {
            List<Image> images = imageLoader.loadImagesFromFolder(folder);
            if (!images.isEmpty()) {
                stateManager.setFileLoaded(folder.getAbsolutePath(), false, false, images, null);
                switchToImageMode(0);
                imageViewer.setImages(images);
                stateManager.setCurrentImagePageIndex(0);
                primaryStage.setTitle("E_Reader - " + folder.getName());
                updateUI();
                resetTextModeButton();
            } else {
                AlertHelper.showError("載入失敗", "資料夾中沒有找到支援的圖片格式");
            }
        }
    }

    public void openPdfFile() {
        FileChooser fc = new FileChooser();
        fc.setTitle("選擇 PDF 檔案");
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF Files", "*.pdf"));
        File pdfFile = fc.showOpenDialog(primaryStage);

        if (pdfFile != null) {
            // **改善：使用新的載入機制**
            openPdfFromFile(pdfFile);
        }
    }

    // 模式切換方法
    public void toggleTextMode() {
        if (stateManager.getCurrentFilePath().isEmpty()) {
            AlertHelper.showError("提示", "請先開啟檔案");
            return;
        }

        // 在切換模式前，先保存當前頁面索引
        int currentPageIndex;
        if (stateManager.isTextMode()) {
            // 從文字模式切換到圖片模式
            currentPageIndex = textRenderer.getCurrentPageIndex();
            stateManager.setCurrentTextPageIndex(currentPageIndex);
        } else {
            // 從圖片模式切換到文字模式
            currentPageIndex = imageViewer.getCurrentIndex();
            stateManager.setCurrentImagePageIndex(currentPageIndex);
        }

        boolean isTextMode = !stateManager.isTextMode();
        stateManager.setTextMode(isTextMode);

        if (isTextMode) {
            textModeBtn.setText("🖼️ 圖片模式");
            textModeBtn.setStyle(textModeBtn.getStyle() + "; -fx-background-color: #28a745;");
            switchToTextMode(currentPageIndex);
        } else {
            textModeBtn.setText("📖 文字模式");
            textModeBtn.setStyle(textModeBtn.getStyle().replace("; -fx-background-color: #28a745", ""));
            switchToImageMode(currentPageIndex);
        }
    }

    private void switchToTextMode(int targetPageIndex) {
        // 使用現代化載入條
        showModernLoadingBar(LoadingProgressBar.LoadingType.TEXT_EXTRACTING, "正在提取文字內容，請稍候...");

        Thread extractThread = new Thread(() -> {
            try {
                List<TextExtractor.PageText> textPages;
                if (stateManager.isPdfMode()) {
                    textPages = textExtractor.extractTextFromPdf(new File(stateManager.getCurrentFilePath()));
                } else if (stateManager.isEpubMode()) {
                    textPages = epubLoader.extractTextFromEpub(new File(stateManager.getCurrentFilePath()));
                } else {
                    textPages = textExtractor.extractTextFromImages(stateManager.getCurrentImages());
                }

                Platform.runLater(() -> {
                    hideModernLoadingBar();

                    if (textPages != null && !textPages.isEmpty()) {
                        stateManager.setCurrentTextPages(textPages);
                        centerPane.getChildren().clear();
                        centerPane.getChildren().addAll(
                                textRenderer.getMainContainer(),
                                readingProgressBar,
                                readingTimeLabel,
                                pageLabel
                        );

                        addFocusModeLabel();

                        textRenderer.setPages(textPages);
                        textRenderer.setThemeColors(settingsManager.getCurrentTheme());

                        int safePageIndex = Math.min(targetPageIndex, textPages.size() - 1);
                        safePageIndex = Math.max(0, safePageIndex);
                        textRenderer.goToPage(safePageIndex);
                        stateManager.setCurrentTextPageIndex(safePageIndex);

                        showNotification("文字模式", "已成功提取 " + textPages.size() + " 頁文字內容\n保持在第 " + (safePageIndex + 1) + " 頁");
                    } else {
                        AlertHelper.showError("文字提取失敗", "無法從檔案中提取文字內容");
                        stateManager.setTextMode(false);
                        resetTextModeButton();
                    }

                    updateUI();
                });

            } catch (Exception e) {
                Platform.runLater(() -> {
                    hideModernLoadingBar();
                    AlertHelper.showError("文字提取錯誤", e.getMessage());
                    stateManager.setTextMode(false);
                    resetTextModeButton();
                });
            }
        });

        extractThread.setDaemon(true);
        extractThread.start();
    }

    private void switchToImageMode(int targetPageIndex) {
        centerPane.getChildren().clear();
        centerPane.getChildren().addAll(
                imageViewer.getScrollPane(),
                readingProgressBar,
                readingTimeLabel,
                pageLabel
        );

        addFocusModeLabel();

        if (imageViewer.hasImages()) {
            int safePageIndex = Math.min(targetPageIndex, imageViewer.getTotalPages() - 1);
            safePageIndex = Math.max(0, safePageIndex);
            imageViewer.goToPage(safePageIndex);
            stateManager.setCurrentImagePageIndex(safePageIndex);
            showNotification("圖片模式", "已切換到圖片模式\n保持在第 " + (safePageIndex + 1) + " 頁");
        }

        updateUI();
    }

    // 導航方法
    public void goToFirstPage() {
        if (stateManager.isTextMode()) {
            textRenderer.goToPage(0);
            stateManager.setCurrentTextPageIndex(0);
        } else {
            imageViewer.goToFirstPage();
            stateManager.setCurrentImagePageIndex(0);
        }
        updateUI();
        Platform.runLater(() -> {
            ensurePageNumbersVisibilityCorrect();
        });
    }

    public void goToPreviousPage() {
        if (stateManager.isTextMode()) {
            int currentIndex = textRenderer.getCurrentPageIndex();
            if (currentIndex > 0) {
                int newIndex = currentIndex - 1;
                textRenderer.goToPage(newIndex);
                stateManager.setCurrentTextPageIndex(newIndex);
            }
        } else {
            imageViewer.prevPage();
            stateManager.setCurrentImagePageIndex(imageViewer.getCurrentIndex());
        }
        
        // **修正：與 goToNextPage 相同的處理**
        updateUI();
        Platform.runLater(() -> {
            ensurePageNumbersVisibilityCorrect();
        });
    }

    public void goToNextPage() {
        if (stateManager.isTextMode()) {
            int currentIndex = textRenderer.getCurrentPageIndex();
            if (currentIndex < textRenderer.getTotalPages() - 1) {
                int newIndex = currentIndex + 1;
                textRenderer.goToPage(newIndex);
                stateManager.setCurrentTextPageIndex(newIndex);
            }
        } else {
            imageViewer.nextPage();
            stateManager.setCurrentImagePageIndex(imageViewer.getCurrentIndex());
        }
        
        // **修正：確保UI更新後頁碼狀態正確**
        updateUI();
        
        // **修正：確保頁碼顯示狀態不被意外改變**
        Platform.runLater(() -> {
            ensurePageNumbersVisibilityCorrect();
        });
    }

    public void goToLastPage() {
        if (stateManager.isTextMode()) {
            int lastIndex = textRenderer.getTotalPages() - 1;
            textRenderer.goToPage(lastIndex);
            stateManager.setCurrentTextPageIndex(lastIndex);
        } else {
            imageViewer.goToLastPage();
            stateManager.setCurrentImagePageIndex(imageViewer.getTotalPages() - 1);
        }
        updateUI();
        Platform.runLater(() -> {
            ensurePageNumbersVisibilityCorrect();
        });
    }

    public void goToPage(int pageIndex) {
        if (stateManager.getCurrentFilePath().isEmpty()) return;

        if (stateManager.isTextMode()) {
            textRenderer.goToPage(pageIndex);
            stateManager.setCurrentTextPageIndex(pageIndex);
        } else {
            imageViewer.goToPage(pageIndex);
            stateManager.setCurrentImagePageIndex(pageIndex);
        }
        
        // **修正：與其他導航方法相同的處理**
        updateUI();
        Platform.runLater(() -> {
            ensurePageNumbersVisibilityCorrect();
        });
    }

    // UI 控制面板創建
    private void createControlPanels() {
        topControls = controlsFactory.createTopControls(this);
        bottomControls = controlsFactory.createBottomControls(this);

        // 獲取按鈕引用
        textModeBtn = controlsFactory.getTextModeButton();
        autoScrollBtn = controlsFactory.getAutoScrollButton();
        nightModeBtn = controlsFactory.getNightModeButton();
        eyeCareBtn = controlsFactory.getEyeCareButton();
        toggleNavBarBtn = controlsFactory.getToggleNavBarButton();
        focusModeBtn = controlsFactory.getFocusModeButton();
        pageField = controlsFactory.getPageField();

        System.out.println("專注模式按鈕已初始化: " + (focusModeBtn != null));

        // 初始化時更新按鈕顯示（預設為圖片模式）
        controlsFactory.updateControlsForMode(false);
    }

    private void setupEventHandlers() {
        // 滑鼠事件
        imageViewer.getImageView().setOnMouseClicked(this::handleImageClick);

        // 滾輪事件
        imageViewer.getScrollPane().setOnScroll(e -> {
            if (e.isControlDown()) {
                if (e.getDeltaY() > 0) {
                    imageViewer.zoomIn();
                } else {
                    imageViewer.zoomOut();
                }
                e.consume();
            } else {
                if (e.getDeltaY() < 0) {
                    goToNextPage();
                } else if (e.getDeltaY() > 0) {
                    goToPreviousPage();
                }
            }
        });

        // 雙擊全螢幕
        imageViewer.getImageView().setOnMouseClicked(e -> {
            if (e.getClickCount() == 2) {
                toggleFullscreen();
            }
        });
    }

    private void setupKeyboardShortcuts() {
        mainLayout.setOnKeyPressed(e -> {
            switch (e.getCode()) {
                case LEFT:
                    goToPreviousPage();
                    break;
                case PAGE_UP:
                    goToPreviousPage();
                    break;
                case RIGHT:
                    goToNextPage();
                    break;
                case PAGE_DOWN:
                case SPACE:
                    goToNextPage();
                    break;
                case HOME:
                    goToFirstPage();
                    break;
                case END:
                    goToLastPage();
                    break;
                case F11:
                    toggleFullscreen();
                    break;
                case ESCAPE:
                    if (stateManager.isFullScreen()) {
                        toggleFullscreen();
                    }
                    break;
                case H:
                    toggleControlsVisibility();
                    break;
                case B:
                    if (e.isControlDown()) {
                        showBookmarkDialog();
                    }
                    break;
                case N:
                    if (e.isControlDown()) {
                        toggleNightMode();
                    }
                    break;
                case T:
                    if (e.isControlDown()) {
                        toggleTextMode();
                    }
                    break;
                case F:
                    toggleFocusMode();
                    break;
            }
        });

        mainLayout.setFocusTraversable(true);
        mainLayout.requestFocus();
    }

    private void setupWindowCloseHandler() {
        primaryStage.setOnCloseRequest(e -> {
            timerManager.stopAllTimers();
            if (settingsManager.isRememberLastFile() && !stateManager.getCurrentFilePath().isEmpty()) {
                saveLastReadingPosition();
            }
        });
    }

    // UI 更新方法
    private void updateUI() {
        updateReadingProgress();
        updateControlsForMode();
        
        // **修正：不在這裡調用頁碼顯示更新，避免狀態被意外改變**
        // 只有在設定變更時才需要更新顯示狀態
        
        // 確保當前頁碼內容是最新的，但不改變顯示狀態
        if (pageLabel != null && pageLabel.isVisible()) {
            updatePageNumberContent();
        }
    }
    
    // **新增：確保頁碼顯示狀態正確的方法**
    private void ensurePageNumbersVisibilityCorrect() {
        if (pageLabel != null) {
            boolean shouldShow = settingsManager.isShowPageNumbers();
            if (pageLabel.isVisible() != shouldShow) {
                pageLabel.setVisible(shouldShow);
                pageLabel.setManaged(shouldShow);
                System.out.println("修正頁碼顯示狀態為: " + shouldShow);
            }
        }
    }
    
    // **新增：只更新頁碼內容，不改變顯示狀態**
    private void updatePageNumberContent() {
        if (pageLabel != null) {
            if (stateManager.isTextMode() && stateManager.getCurrentTextPages() != null) {
                pageLabel.setText("文字: " + (textRenderer.getCurrentPageIndex() + 1) + " / " + stateManager.getCurrentTextPages().size());
            } else if (!stateManager.isTextMode() && imageViewer.hasImages()) {
                pageLabel.setText("頁面: " + (imageViewer.getCurrentIndex() + 1) + " / " + imageViewer.getTotalPages());
            } else {
                pageLabel.setText("頁面: 0 / 0");
            }
        }
    }

    private void updateReadingProgress() {
        double progress = 0;

        if (stateManager.isTextMode() && stateManager.getCurrentTextPages() != null) {
            progress = (double) (textRenderer.getCurrentPageIndex() + 1) / stateManager.getCurrentTextPages().size();
        } else if (!stateManager.isTextMode() && imageViewer.hasImages()) {
            progress = (double) (imageViewer.getCurrentIndex() + 1) / imageViewer.getTotalPages();
        }

        if (readingProgressBar != null) {
            readingProgressBar.setProgress(progress);
        }
    }

    private void updateControlsForMode() {
        controlsFactory.updateControlsForMode(stateManager.isTextMode());
        
        // **修正：只更新頁碼內容，不改變顯示狀態**
        updatePageNumberContent();
    }

    private void resetTextModeButton() {
        textModeBtn.setText("📖 文字模式");
        textModeBtn.setStyle(textModeBtn.getStyle().replace("; -fx-background-color: #28a745", ""));
    }

    // 現代化載入條
    private LoadingProgressBar currentLoadingBar;
    
    // 舊版載入指示器（保留以備兼容）
    private ProgressIndicator loadingIndicator;
    private Label loadingLabel;
    private VBox loadingBox;

    private void showLoadingIndicator(String message) {
        if (loadingBox != null) return;

        loadingIndicator = new ProgressIndicator();
        loadingIndicator.setMaxSize(50, 50);

        loadingLabel = new Label(message);
        loadingLabel.setStyle("-fx-text-fill: white; -fx-font-size: 16px;");

        loadingBox = new VBox(20);
        loadingBox.setAlignment(Pos.CENTER);
        loadingBox.setStyle("-fx-background-color: rgba(0, 0, 0, 0.8); -fx-background-radius: 10px;");
        loadingBox.setPadding(new Insets(30));
        loadingBox.getChildren().addAll(loadingIndicator, loadingLabel);

        StackPane.setAlignment(loadingBox, Pos.CENTER);
        centerPane.getChildren().add(loadingBox);
    }

    private void hideLoadingIndicator() {
        if (loadingBox != null) {
            centerPane.getChildren().remove(loadingBox);
            loadingBox = null;
            loadingIndicator = null;
            loadingLabel = null;
        }
    }

    /**
     * 關閉當前開啟的檔案
     */
    private void closeCurrentFile() {
        if (stateManager.isAutoScrolling()) {
            toggleAutoScroll();
        }

        if (settingsManager.isRememberLastFile() && !stateManager.getCurrentFilePath().isEmpty()) {
            saveLastReadingPosition();
        }

        stateManager.clearCurrentFile();

        imageViewer.clearImages();
        textRenderer.clearPages();

        if (stateManager.isTextMode()) {
            stateManager.setTextMode(false);
            switchToImageMode(0);
            resetTextModeButton();
        }

        primaryStage.setTitle("E_Reader 漫畫＆PDF閱讀器 v3.0 Enhanced");
        updateUI();

        showNotification("檔案已關閉", "檔案已關閉，可以開啟新檔案");
    }

    // 工具方法
    private void handleImageClick(MouseEvent event) {
        if (isFocusMode) {
            double x = event.getX();
            double imageWidth = imageViewer.getImageView().getBoundsInLocal().getWidth();

            if (x > imageWidth * 0.3 && x < imageWidth * 0.7) {
                exitFocusMode();
                return;
            }
        }

        if (stateManager.isTextMode()) return;

        double x = event.getX();
        double imageWidth = imageViewer.getImageView().getBoundsInLocal().getWidth();

        if (x > imageWidth * 0.7) {
            goToNextPage();
        } else if (x < imageWidth * 0.3) {
            goToPreviousPage();
        } else {
            toggleControlsVisibility();
        }
    }

    // Getter 方法用於其他類別存取
    public Stage getPrimaryStage() { return primaryStage; }
    public StateManager getStateManager() { return stateManager; }
    public SettingsManager getSettingsManager() { return settingsManager; }
    public BookmarkManager getBookmarkManager() { return bookmarkManager; }
    public TextRenderer getTextRenderer() { return textRenderer; }
    public ImageViewer getImageViewer() { return imageViewer; }
    public TimerManager getTimerManager() { return timerManager; }
    public FileManagerController getFileManagerController() { return fileManagerController; }
    public ImageLoader getImageLoader() { return imageLoader; }
    public PdfLoader getPdfLoader() { return pdfLoader; }
    public EpubLoader getEpubLoader() { return epubLoader; }
    public TextExtractor getTextExtractor() { return textExtractor; }
    public TextLoader getTextLoader() { return textLoader; }
    public NoteManager getNoteManager() { return noteManager; }

    // 書籤管理
    public void showBookmarkDialog() {
        if (stateManager.getCurrentFilePath().isEmpty()) {
            AlertHelper.showError("提示", "請先開啟檔案");
            return;
        }

        int currentPageIndex;
        if (stateManager.isTextMode()) {
            currentPageIndex = textRenderer.getCurrentPageIndex();
        } else {
            currentPageIndex = imageViewer.getCurrentIndex();
        }

        bookmarkManager.showBookmarkDialog(primaryStage, stateManager.getCurrentFilePath(),
                currentPageIndex, bookmark -> {
                    goToPage(bookmark.getPageNumber());
                });
    }

    // 夜間模式切換
    public void toggleNightMode() {
        settingsManager.toggleNightMode();

        if (settingsManager.isNightMode()) {
            nightModeBtn.setStyle(nightModeBtn.getStyle() + "; -fx-background-color: #28a745;");
        } else {
            nightModeBtn.setStyle(nightModeBtn.getStyle().replace("; -fx-background-color: #28a745", ""));
        }

        applySettings();
    }

    // 護眼模式切換
    public void toggleEyeCareMode() {
        settingsManager.toggleEyeCareMode();

        if (settingsManager.isEyeCareMode()) {
            eyeCareBtn.setStyle(eyeCareBtn.getStyle() + "; -fx-background-color: #28a745;");
            showNotification("護眼模式已啟用", "建議每30分鐘休息5-10分鐘");
        } else {
            eyeCareBtn.setStyle(eyeCareBtn.getStyle().replace("; -fx-background-color: #28a745", ""));
        }

        applySettings();
    }

    // 全螢幕切換
    public void toggleFullscreen() {
        boolean isFullScreen = !stateManager.isFullScreen();
        stateManager.setFullScreen(isFullScreen);
        primaryStage.setFullScreen(isFullScreen);

        if (isFullScreen) {
            controlsContainer.setVisible(false);
            controlsContainer.setManaged(false);
        } else {
            controlsContainer.setVisible(stateManager.isControlsVisible());
            controlsContainer.setManaged(stateManager.isControlsVisible());
        }
    }

    // 控制面板顯示切換
    public void toggleControlsVisibility() {
        if (!stateManager.isFullScreen()) {
            boolean isVisible = !stateManager.isControlsVisible();
            stateManager.setControlsVisible(isVisible);
            controlsContainer.setVisible(isVisible);
            controlsContainer.setManaged(isVisible);
        }
    }

    // 專注模式
    public void toggleFocusMode() {
        System.out.println("專注模式切換被呼叫, 目前狀態: " + isFocusMode);

        isFocusMode = !isFocusMode;

        Platform.runLater(() -> {
            if (isFocusMode) {
                enterFocusMode();
            } else {
                exitFocusMode();
            }
        });
    }

    /**
     * 改進的專注模式進入動畫
     */
    private void enterFocusMode() {
        // iOS風格的淡出動畫
        javafx.animation.Timeline fadeOutTimeline = new javafx.animation.Timeline();

        // 控制面板淡出
        javafx.animation.KeyFrame controlsFadeOut = new javafx.animation.KeyFrame(
                javafx.util.Duration.millis(400),
                new javafx.animation.KeyValue(controlsContainer.opacityProperty(), 0.0),
                new javafx.animation.KeyValue(controlsContainer.scaleXProperty(), 0.95),
                new javafx.animation.KeyValue(controlsContainer.scaleYProperty(), 0.95)
        );

        // 進度條和標籤淡出
        javafx.animation.KeyFrame progressFadeOut = new javafx.animation.KeyFrame(
                javafx.util.Duration.millis(300),
                new javafx.animation.KeyValue(readingProgressBar.opacityProperty(), 0.0),
                new javafx.animation.KeyValue(readingTimeLabel.opacityProperty(), 0.0),
                new javafx.animation.KeyValue(pageLabel.opacityProperty(), 0.0)
        );

        fadeOutTimeline.getKeyFrames().addAll(controlsFadeOut, progressFadeOut);

        fadeOutTimeline.setOnFinished(e -> {
            controlsContainer.setVisible(false);
            controlsContainer.setManaged(false);
            readingProgressBar.setVisible(false);
            readingTimeLabel.setVisible(false);
            pageLabel.setVisible(false);
        });

        fadeOutTimeline.play();

        // 顯示專注模式提示
        Label focusModeLabel = (Label) centerPane.lookup("#focusModeLabel");
        if (focusModeLabel != null) {
            focusModeLabel.setVisible(true);
            focusModeLabel.setOpacity(0.0);
            focusModeLabel.setScaleX(0.8);
            focusModeLabel.setScaleY(0.8);

            // iOS風格彈性動畫
            javafx.animation.Timeline showLabelTimeline = new javafx.animation.Timeline();
            javafx.animation.KeyFrame labelFadeIn = new javafx.animation.KeyFrame(
                    javafx.util.Duration.millis(600),
                    new javafx.animation.KeyValue(focusModeLabel.opacityProperty(), 1.0),
                    new javafx.animation.KeyValue(focusModeLabel.scaleXProperty(), 1.0),
                    new javafx.animation.KeyValue(focusModeLabel.scaleYProperty(), 1.0)
            );
            showLabelTimeline.getKeyFrames().add(labelFadeIn);
            showLabelTimeline.play();

            // 3秒後自動隱藏提示
            javafx.animation.PauseTransition pause = new javafx.animation.PauseTransition(javafx.util.Duration.seconds(3));
            pause.setOnFinished(event -> {
                javafx.animation.Timeline hideLabelTimeline = new javafx.animation.Timeline();
                javafx.animation.KeyFrame labelFadeOut = new javafx.animation.KeyFrame(
                        javafx.util.Duration.millis(400),
                        new javafx.animation.KeyValue(focusModeLabel.opacityProperty(), 0.0),
                        new javafx.animation.KeyValue(focusModeLabel.scaleXProperty(), 0.8),
                        new javafx.animation.KeyValue(focusModeLabel.scaleYProperty(), 0.8)
                );
                hideLabelTimeline.getKeyFrames().add(labelFadeOut);
                hideLabelTimeline.setOnFinished(hideEvent -> focusModeLabel.setVisible(false));
                hideLabelTimeline.play();
            });
            pause.play();
        }

        // 更新按鈕狀態
        if (focusModeBtn != null) {
            focusModeBtn.setText("🛋️ 退出");
            focusModeBtn.setStyle(
                    "-fx-background-color: linear-gradient(to bottom, " +
                            "rgba(231,76,60,0.8), rgba(192,57,43,0.8)); " +
                            "-fx-border-color: rgba(231,76,60,0.6); " +
                            "-fx-border-width: 0.5; " +
                            "-fx-border-radius: 8; " +
                            "-fx-background-radius: 8; " +
                            "-fx-text-fill: white; " +
                            "-fx-font-size: 11px; " +
                            "-fx-font-weight: 600; " +
                            "-fx-padding: 6 12 6 12; " +
                            "-fx-cursor: hand; " +
                            "-fx-effect: dropshadow(gaussian, rgba(231,76,60,0.3), 6, 0, 0, 2);"
            );
        }

        showNotification("專注模式", "已進入沉浸式閱讀體驗");
    }



    /**
     * 切換導覽列顯示/隱藏狀態
     */
    public void toggleNavigationBar() {
        if (navBarController != null) {
            navBarController.toggleNavigationBar();

            if (navBarController.isNavigationBarPinned()) {
                toggleNavBarBtn.setText("🙈 隱藏導覽列");
                toggleNavBarBtn.setStyle(toggleNavBarBtn.getStyle().replace("; -fx-background-color: #dc3545", ""));
                showNotification("導覽列已顯示", "導覽列現在為常駐顯示模式");
            } else {
                toggleNavBarBtn.setText("🙉 顯示導覽列");
                toggleNavBarBtn.setStyle(toggleNavBarBtn.getStyle() + "; -fx-background-color: #dc3545");
                showNotification("導覽列已隱藏", "將滑鼠移至頂部或向上滾動可暫時顯示導覽列");
            }
        }
    }

    // 自動翻頁功能
    public void toggleAutoScroll() {
        boolean isAutoScrolling = !stateManager.isAutoScrolling();
        stateManager.setAutoScrolling(isAutoScrolling);

        if (isAutoScrolling) {
            autoScrollBtn.setText("⏸️ 停止翻頁");
            autoScrollBtn.setStyle(autoScrollBtn.getStyle() + "; -fx-background-color: #dc3545;");
            startAutoScroll();
        } else {
            autoScrollBtn.setText("⏯️ 自動翻頁");
            autoScrollBtn.setStyle(autoScrollBtn.getStyle().replace("; -fx-background-color: #dc3545", ""));
            timerManager.stopAutoScroll();
        }
    }

    private void startAutoScroll() {
        timerManager.startAutoScroll(() -> {
            boolean canGoNext;
            if (stateManager.isTextMode()) {
                canGoNext = textRenderer.getCurrentPageIndex() < textRenderer.getTotalPages() - 1;
            } else {
                canGoNext = imageViewer.canGoNext();
            }

            if (canGoNext) {
                goToNextPage();
            } else {
                toggleAutoScroll();
            }
        });
    }




    // 這是MainController.java中需要修改的部分

    /**
     * 改進的showNotification方法 - 現代化通知設計
     */
    public void showNotification(String title, String message) {
        // 創建非阻塞的通知彈窗
        Stage notificationStage = new Stage();
        notificationStage.initStyle(StageStyle.UNDECORATED);
        notificationStage.initOwner(primaryStage);
        notificationStage.setAlwaysOnTop(true);
        // 修復：設置為非模態窗口，不阻塞用戶操作
        notificationStage.initModality(Modality.NONE);

        // 創建通知內容
        VBox notificationBox = new VBox(12);
        notificationBox.setPadding(new Insets(20, 24, 20, 24));
        notificationBox.setAlignment(Pos.CENTER_LEFT);
        notificationBox.setMaxWidth(320);
        notificationBox.setMinWidth(280);

        // 修復：使用更深色的背景，文字設為偏灰色
        notificationBox.setStyle(
                "-fx-background-color: linear-gradient(to bottom right, #ffffff, #f4f4f4); " +
                        "-fx-border-color: #e0e0e0; " +
                        "-fx-border-width: 1; " +
                        "-fx-border-radius: 16; " +
                        "-fx-background-radius: 16; " +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.06), 12, 0, 0, 4); " +
                        "-fx-padding: 16;"
        );


        // 頂部指示條
        Rectangle topIndicator = new Rectangle(50, 3);
        topIndicator.setFill(LinearGradient.valueOf(
                "linear-gradient(to right, #4facfe, #00f2fe)"  // 漸層藍綠
        ));
        topIndicator.setArcWidth(3);
        topIndicator.setArcHeight(3);

        // 標題文字
        Label titleLabel = new Label(title);
        titleLabel.setStyle("-fx-text-fill: #111111; -fx-font-size: 16px; -fx-font-weight: bold;");

// 消息文字
        Label messageLabel = new Label(message);
        messageLabel.setStyle("-fx-text-fill: #333333; -fx-font-size: 13px;");
        messageLabel.setWrapText(true);
        messageLabel.setMaxWidth(280);

// 時間文字
        Label timeLabel = new Label(LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm")));
        timeLabel.setStyle("-fx-text-fill: #888888; -fx-font-size: 12px;");

// 圖標
        Label iconLabel = new Label(getNotificationIcon(title));
        iconLabel.setStyle(
                "-fx-font-size: 20px; " +
                        "-fx-background-color: #e6f0ff; " +  // 淺藍背景
                        "-fx-text-fill: #2980b9; " +
                        "-fx-padding: 6; " +
                        "-fx-background-radius: 50%; " +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 3, 0, 0, 1);"
        );

        // 頂部容器（圖標 + 標題 + 時間）
        HBox headerBox = new HBox(10);
        headerBox.setAlignment(Pos.CENTER_LEFT);

        VBox titleBox = new VBox(2);
        titleBox.getChildren().add(titleLabel);

        headerBox.getChildren().addAll(iconLabel, titleBox);

        // 右側時間
        HBox.setHgrow(titleBox, Priority.ALWAYS);
        headerBox.getChildren().add(timeLabel);

        // 分隔線
        Separator separator = new Separator();
        separator.setStyle("-fx-background-color: rgba(255,255,255,0.15);");

        // 組裝通知內容
        notificationBox.getChildren().addAll(
                topIndicator,
                headerBox,
                separator,
                messageLabel
        );

        // 修復：關閉按鈕設計為更小更不顯眼
        Button closeButton = new Button("×");
        closeButton.setStyle(
                "-fx-background-color: transparent; " +
                        "-fx-text-fill: rgba(160, 160, 160, 0.6); " +  // 灰色關閉按鈕
                        "-fx-font-size: 14px; " +
                        "-fx-font-weight: bold; " +
                        "-fx-cursor: hand; " +
                        "-fx-background-radius: 10; " +
                        "-fx-min-width: 20; " +
                        "-fx-min-height: 20; " +
                        "-fx-max-width: 20; " +
                        "-fx-max-height: 20;"
        );

        closeButton.setOnMouseEntered(e -> {
            closeButton.setStyle(closeButton.getStyle() +
                    "-fx-background-color: rgba(231,76,60,0.7); -fx-text-fill: white;");
        });

        closeButton.setOnMouseExited(e -> {
            closeButton.setStyle(
                    "-fx-background-color: transparent; " +
                            "-fx-text-fill: rgba(160, 160, 160, 0.6); " +  // 灰色關閉按鈕
                            "-fx-font-size: 14px; " +
                            "-fx-font-weight: bold; " +
                            "-fx-cursor: hand; " +
                            "-fx-background-radius: 10; " +
                            "-fx-min-width: 20; " +
                            "-fx-min-height: 20; " +
                            "-fx-max-width: 20; " +
                            "-fx-max-height: 20;"
            );
        });

        // 主容器
        StackPane mainContainer = new StackPane();
        mainContainer.getChildren().add(notificationBox);

        // 將關閉按鈕定位到右上角
        StackPane.setAlignment(closeButton, Pos.TOP_RIGHT);
        StackPane.setMargin(closeButton, new Insets(8, 8, 0, 0));
        mainContainer.getChildren().add(closeButton);

        Scene notificationScene = new Scene(mainContainer);
        notificationScene.setFill(Color.TRANSPARENT);
        notificationStage.setScene(notificationScene);

        // 修復：智能定位 - 自動選擇安全的顯示位置
        Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds();
        double notificationWidth = 320;
        double notificationHeight = 120;

        // 預設位置：右上角，但避開可能的UI元素
        // 通知顯示在主視窗的右上角，稍微往內縮一點點（例如 20px）
        // 通知固定顯示在螢幕右上角
        double targetX = screenBounds.getMaxX() - notificationWidth - 20;
        double targetY = screenBounds.getMinY() + 80; // 距離螢幕頂部 80px

        notificationStage.setX(targetX);
        notificationStage.setY(targetY);

// 通知從右側滑入（固定）
        double fromX = 400;

        TranslateTransition slideIn = new TranslateTransition(Duration.millis(350), mainContainer);
        slideIn.setFromX(fromX);
        slideIn.setToX(0);
        slideIn.setInterpolator(Interpolator.EASE_OUT);

        FadeTransition fadeIn = new FadeTransition(Duration.millis(350), mainContainer);
        fadeIn.setFromValue(0.0);
        fadeIn.setToValue(1.0);

        ScaleTransition scaleIn = new ScaleTransition(Duration.millis(350), mainContainer);
        scaleIn.setFromX(0.9);
        scaleIn.setFromY(0.9);
        scaleIn.setToX(1.0);
        scaleIn.setToY(1.0);
        scaleIn.setInterpolator(Interpolator.EASE_OUT);

        ParallelTransition enterAnimation = new ParallelTransition(slideIn, fadeIn, scaleIn);

// 通知關閉動畫（固定向右滑出）
        Runnable closeNotification = () -> {
            TranslateTransition slideOut = new TranslateTransition(Duration.millis(250), mainContainer);
            slideOut.setFromX(0);
            slideOut.setToX(350); // 向右滑出
            slideOut.setInterpolator(Interpolator.EASE_IN);

            FadeTransition fadeOut = new FadeTransition(Duration.millis(250), mainContainer);
            fadeOut.setFromValue(1.0);
            fadeOut.setToValue(0.0);

            ScaleTransition scaleOut = new ScaleTransition(Duration.millis(250), mainContainer);
            scaleOut.setFromX(1.0);
            scaleOut.setFromY(1.0);
            scaleOut.setToX(0.9);
            scaleOut.setToY(0.9);

            ParallelTransition exitAnimation = new ParallelTransition(slideOut, fadeOut, scaleOut);
            exitAnimation.setOnFinished(e -> notificationStage.close());
            exitAnimation.play();
        };



        // 事件處理
        closeButton.setOnAction(e -> closeNotification.run());

        // 修復：點擊通知本體不再自動關閉，避免誤操作
        // 只有點擊關閉按鈕才會關閉通知
        notificationBox.setOnMouseClicked(e -> {
            // 移除自動關閉功能，只提供視覺反饋
            if (e.getClickCount() == 2) {
                // 雙擊才關閉
                closeNotification.run();
            } else {
                // 單擊顯示閃爍效果表示收到點擊
                FadeTransition blink = new FadeTransition(Duration.millis(100), notificationBox);
                blink.setFromValue(1.0);
                blink.setToValue(0.8);
                blink.setCycleCount(2);
                blink.setAutoReverse(true);
                blink.play();
            }
        });
        Timeline autoCloseTimeline = new Timeline(
                new KeyFrame(Duration.seconds(4), e -> {
                    if (notificationStage.isShowing()) {
                        closeNotification.run();
                    }
                })
        );
        autoCloseTimeline.play();
        // 修復：鼠標懸停效果改進
        notificationBox.setOnMouseEntered(e -> {
            // 懸停時輕微放大並增加陰影
            ScaleTransition hoverScale = new ScaleTransition(Duration.millis(150), notificationBox);
            hoverScale.setFromX(1.0);
            hoverScale.setFromY(1.0);
            hoverScale.setToX(1.02);
            hoverScale.setToY(1.02);
            hoverScale.play();

            // 暫停自動關閉
            if (autoCloseTimeline != null) {
                autoCloseTimeline.pause();
            }
        });

        notificationBox.setOnMouseExited(e -> {
            // 恢復正常大小
            ScaleTransition normalScale = new ScaleTransition(Duration.millis(150), notificationBox);
            normalScale.setFromX(1.02);
            normalScale.setFromY(1.02);
            normalScale.setToX(1.0);
            normalScale.setToY(1.0);
            normalScale.play();

            // 恢復自動關閉
            if (autoCloseTimeline != null) {
                autoCloseTimeline.play();
            }
        });

        // 顯示通知
        notificationStage.show();
        enterAnimation.play();

        // 修復：使用Timeline代替PauseTransition，更好的控制


        // 修復：添加鍵盤快捷鍵支持（ESC關閉通知）
        notificationScene.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ESCAPE) {
                closeNotification.run();
            }
        });

        // 修復：防止通知堆積 - 如果已有通知在顯示，自動關閉舊的
        if (currentNotificationStage != null && currentNotificationStage.isShowing()) {
            currentNotificationStage.close();
        }
        currentNotificationStage = notificationStage;
    }

    private Stage currentNotificationStage = null;

    /**
     * 更新頁面控制元件 - 新增缺少的方法
     */
    public void updatePageControls() {
        updateUI();
    }



    private void updateButtonStates() {
        // 更新夜間模式按鈕狀態 - 基於 nightMode 標記而非當前主題
        if (settingsManager.isNightMode()) {
            if (!nightModeBtn.getStyle().contains("-fx-background-color: #28a745")) {
                nightModeBtn.setStyle(nightModeBtn.getStyle() + "; -fx-background-color: #28a745;");
            }
        } else {
            nightModeBtn.setStyle(nightModeBtn.getStyle().replace("; -fx-background-color: #28a745", ""));
        }

        // 更新護眼模式按鈕狀態
        if (settingsManager.isEyeCareMode()) {
            if (!eyeCareBtn.getStyle().contains("-fx-background-color: #28a745")) {
                eyeCareBtn.setStyle(eyeCareBtn.getStyle() + "; -fx-background-color: #28a745;");
            }
        } else {
            eyeCareBtn.setStyle(eyeCareBtn.getStyle().replace("; -fx-background-color: #28a745", ""));
        }
        
        // 確保互斥性：當一個模式開啟時，另一個模式的按鈕狀態會被關閉
        if (settingsManager.isNightMode() && eyeCareBtn.getStyle().contains("-fx-background-color: #28a745")) {
            eyeCareBtn.setStyle(eyeCareBtn.getStyle().replace("; -fx-background-color: #28a745", ""));
        }
        if (settingsManager.isEyeCareMode() && nightModeBtn.getStyle().contains("-fx-background-color: #28a745")) {
            nightModeBtn.setStyle(nightModeBtn.getStyle().replace("; -fx-background-color: #28a745", ""));
        }
    }

    // 更新閱讀時間
    public void updateReadingTime() {
        long totalTime = stateManager.calculateTotalReadingTime();

        long hours = totalTime / (1000 * 60 * 60);
        long minutes = (totalTime % (1000 * 60 * 60)) / (1000 * 60);
        long seconds = ((totalTime % (1000 * 60 * 60)) % (1000 * 60)) / 1000;

        readingTimeLabel.setText(String.format("閱讀時間: %02d:%02d:%02d", hours, minutes, seconds));
    }

    // 載入最後閱讀位置
    public void loadLastReadingPosition() {
        try {
            Properties props = new Properties();
            File propertiesFile = new File("last_reading.properties");
            
            if (!propertiesFile.exists()) {
                return; // 沒有儲存的閱讀位置
            }
            
            try (java.io.FileInputStream in = new java.io.FileInputStream(propertiesFile)) {
                props.load(in);
            }
            
            String lastFilePath = props.getProperty("lastFile");
            String lastPageStr = props.getProperty("lastPage", "0");
            String mode = props.getProperty("mode", "image");
            
            if (lastFilePath == null || lastFilePath.isEmpty()) {
                return;
            }
            
            File lastFile = new File(lastFilePath);
            if (!lastFile.exists()) {
                showNotification("提醒", "上次閱讀的檔案已不存在: " + lastFile.getName());
                return;
            }
            int lastPage = 0;
            try {
                lastPage = Integer.parseInt(lastPageStr);
            } catch (NumberFormatException e) {
                lastPage = 0;
            }
            final int targetPage = lastPage;

            // 自動開啟最後閱讀的檔案
            openFileFromManager(lastFile);
            
            // 等待檔案載入完成後跳轉到最後閱讀的頁面
            javafx.application.Platform.runLater(() -> {
                try {
                    Thread.sleep(500); // 等待檔案載入完成
                    
                    // 如果需要切換到文字模式
                    if ("text".equals(mode) && !stateManager.isTextMode()) {
                        toggleTextMode();
                        // 再次延遲，等待文字模式載入完成
                        javafx.application.Platform.runLater(() -> {
                            try {
                                Thread.sleep(1000);
                                goToPage(targetPage);
                                showNotification("歡迎回來", 
                                    String.format("已自動開啟上次閱讀的檔案\n檔案: %s\n上次閱讀到的頁數: %d\n模式: %s",
                                        lastFile.getName(), targetPage + 1, "text".equals(mode) ? "文字模式" : "圖片模式"));
                            } catch (InterruptedException ex) {
                                Thread.currentThread().interrupt();
                            }
                        });
                    } else {
                        goToPage(targetPage);
                        showNotification("歡迎回來", 
                            String.format("已自動開啟上次閱讀的檔案\n檔案: %s\n頁數: %d\n模式: %s", 
                                lastFile.getName(), targetPage + 1, "text".equals(mode) ? "文字模式" : "圖片模式"));
                    }
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                }
            });
            
        } catch (Exception e) {
            System.err.println("無法載入最後閱讀位置: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    // 儲存最後閱讀位置
    public void saveLastReadingPosition() {
        try {
            Properties props = new Properties();
            props.setProperty("lastFile", stateManager.getCurrentFilePath());
            if (stateManager.isTextMode()) {
                props.setProperty("lastPage", String.valueOf(textRenderer.getCurrentPageIndex()));
                props.setProperty("mode", "text");
            } else {
                props.setProperty("lastPage", String.valueOf(imageViewer.getCurrentIndex()));
                props.setProperty("mode", "image");
            }

            try (FileOutputStream out = new FileOutputStream("last_reading.properties")) {
                props.store(out, "Last Reading Position");
            }
        } catch (Exception e) {
            System.err.println("無法儲存閱讀位置: " + e.getMessage());
        }
    }

    /**
     * 添加專注模式標籤
     */
    private void addFocusModeLabel() {
        // 檢查是否已經存在
        Label existingLabel = (Label) centerPane.lookup("#focusModeLabel");
        if (existingLabel == null) {
            // 專注模式提示標籤
            Label focusModeLabel = new Label("🎯 專注模式 - 按 F 或點擊中央退出");
            focusModeLabel.setStyle(
                "-fx-text-fill: #40e0d0;" +
                "-fx-background-color: rgba(111,66,193,0.9);" +
                "-fx-background-radius: 25px;" +
                "-fx-padding: 12 25 12 25;" +
                "-fx-font-size: 16px;" +
                "-fx-font-weight: bold;" +
                "-fx-effect: dropshadow(gaussian, rgba(111,66,193,0.8), 20, 0, 0, 5);"
            );
            focusModeLabel.setVisible(false);
            focusModeLabel.setId("focusModeLabel");
            StackPane.setAlignment(focusModeLabel, Pos.TOP_CENTER);
            StackPane.setMargin(focusModeLabel, new Insets(30, 0, 0, 0));
            centerPane.getChildren().add(focusModeLabel);
        }
    }



    /**
     * 創建設定區塊的輔助方法
     */
    private VBox createSettingsSection(String title, String description) {
        VBox section = new VBox(8);

        Label titleLabel = new Label(title);
        titleLabel.setStyle(
                "-fx-text-fill: rgba(255,255,255,0.95); " +
                        "-fx-font-size: 14px; " +
                        "-fx-font-weight: 600;"
        );

        Label descLabel = new Label(description);
        descLabel.setStyle(
                "-fx-text-fill: rgba(255,255,255,0.7); " +
                        "-fx-font-size: 11px; " +
                        "-fx-wrap-text: true;"
        );

        section.getChildren().addAll(titleLabel, descLabel);
        return section;
    }

    /**
     * 創建iOS風格的CheckBox
     */
    private CheckBox createStyledCheckBox(String text, String description) {
        VBox container = new VBox(3);

        CheckBox checkBox = new CheckBox(text);
        checkBox.setStyle(
                "-fx-text-fill: rgba(255,255,255,0.9); " +
                        "-fx-font-size: 12px; " +
                        "-fx-font-weight: 500;"
        );

        Label descLabel = new Label(description);
        descLabel.setStyle(
                "-fx-text-fill: rgba(255,255,255,0.6); " +
                        "-fx-font-size: 10px; " +
                        "-fx-padding: 0 0 0 25;"
        );

        container.getChildren().addAll(checkBox, descLabel);

        // 將容器的CheckBox返回（需要特殊處理）
        return checkBox;
    }



    /**
     * 改進的專注模式退出動畫
     */
    private void exitFocusMode() {
        // 重新顯示UI元素
        controlsContainer.setVisible(true);
        controlsContainer.setManaged(true);
        readingProgressBar.setVisible(true);
        readingTimeLabel.setVisible(true);
        pageLabel.setVisible(true);

        // 設置初始狀態
        controlsContainer.setOpacity(0.0);
        controlsContainer.setScaleX(0.95);
        controlsContainer.setScaleY(0.95);
        readingProgressBar.setOpacity(0.0);
        readingTimeLabel.setOpacity(0.0);
        pageLabel.setOpacity(0.0);

        // iOS風格的彈入動畫
        javafx.animation.Timeline fadeInTimeline = new javafx.animation.Timeline();

        // 控制面板彈入
        javafx.animation.KeyFrame controlsFadeIn = new javafx.animation.KeyFrame(
                javafx.util.Duration.millis(500),
                new javafx.animation.KeyValue(controlsContainer.opacityProperty(), 1.0),
                new javafx.animation.KeyValue(controlsContainer.scaleXProperty(), 1.0),
                new javafx.animation.KeyValue(controlsContainer.scaleYProperty(), 1.0),
                new javafx.animation.KeyValue(controlsContainer.translateYProperty(), 0.0)
        );

        // 進度條和標籤淡入
        javafx.animation.KeyFrame elementsFadeIn = new javafx.animation.KeyFrame(
                javafx.util.Duration.millis(400),
                new javafx.animation.KeyValue(readingProgressBar.opacityProperty(), 1.0),
                new javafx.animation.KeyValue(readingTimeLabel.opacityProperty(), 1.0),
                new javafx.animation.KeyValue(pageLabel.opacityProperty(), 1.0)
        );

        fadeInTimeline.getKeyFrames().addAll(controlsFadeIn, elementsFadeIn);
        fadeInTimeline.play();

        // 隱藏專注模式提示
        Label focusModeLabel = (Label) centerPane.lookup("#focusModeLabel");
        if (focusModeLabel != null) {
            focusModeLabel.setVisible(false);
        }

        // 恢復按鈕狀態
        if (focusModeBtn != null) {
            focusModeBtn.setText("🎯 專注模式");
            focusModeBtn.setStyle(
                    "-fx-background-color: linear-gradient(to bottom, " +
                            "rgba(155,89,182,0.8), rgba(142,68,173,0.8)); " +
                            "-fx-border-color: rgba(155,89,182,0.6); " +
                            "-fx-border-width: 0.5; " +
                            "-fx-border-radius: 8; " +
                            "-fx-background-radius: 8; " +
                            "-fx-text-fill: white; " +
                            "-fx-font-size: 11px; " +
                            "-fx-font-weight: 600; " +
                            "-fx-padding: 6 12 6 12; " +
                            "-fx-cursor: hand; " +
                            "-fx-effect: dropshadow(gaussian, rgba(155,89,182,0.3), 6, 0, 0, 2);"
            );
        }

        showNotification("歡迎回來", "已退出專注模式");
    }

    /**
     * 改進的applySettings方法 - 更流暢的主題切換
     */
    public void applySettings() {
        SettingsManager.ThemeMode currentTheme = settingsManager.getCurrentTheme();

        String backgroundColor = currentTheme.getBackgroundColor();
        String textColor = currentTheme.getTextColor();

        // 使用動畫進行主題切換
        javafx.animation.Timeline themeTransition = new javafx.animation.Timeline();

        // 背景顏色過渡
        if (imageViewer.getScrollPane() != null) {
            String newStyle = "-fx-background: " + backgroundColor + "; -fx-background-color: " + backgroundColor + ";";

            // 創建漸變動畫效果
            javafx.animation.FadeTransition bgFade = new javafx.animation.FadeTransition(
                    javafx.util.Duration.millis(300),
                    imageViewer.getScrollPane()
            );
            bgFade.setFromValue(0.8);
            bgFade.setToValue(1.0);
            bgFade.setOnFinished(e -> imageViewer.getScrollPane().setStyle(newStyle));
            bgFade.play();
        }

        // 中央面板背景更新
        String centerStyle =
                "-fx-background-color: linear-gradient(to bottom, " +
                        "rgba(18,18,18,0.98) 0%, " +
                        "rgba(25,25,25,0.95) 100%);";
        centerPane.setStyle(centerStyle);

        // 如果在文字模式，也更新文字渲染器的主題
        if (stateManager.isTextMode()) {
            textRenderer.setThemeColors(currentTheme);
        }

        // 套用其他設定
        imageViewer.setFitMode(E_Reader.viewer.ImageViewer.FitMode.valueOf(settingsManager.getFitMode().toString()));

        // 更新按鈕狀態（帶動畫效果）
        updateButtonStatesWithAnimation();

        // 更新護眼提醒
        if (settingsManager.isEyeCareMode() && !timerManager.isEyeCareReminderRunning()) {
            timerManager.startEyeCareReminder(() ->
                    showNotification("護眼提醒 👁️", "已閱讀30分鐘，建議休息片刻"));
        } else if (!settingsManager.isEyeCareMode() && timerManager.isEyeCareReminderRunning()) {
            timerManager.stopEyeCareReminder();
        }
    }

    /**
     * 帶動畫效果的按鈕狀態更新
     */
    private void updateButtonStatesWithAnimation() {
        // 夜間模式按鈕動畫更新
        javafx.animation.FadeTransition nightBtnTransition = new javafx.animation.FadeTransition(
                javafx.util.Duration.millis(200), nightModeBtn
        );

        if (settingsManager.isNightMode()) {
            nightBtnTransition.setFromValue(0.7);
            nightBtnTransition.setToValue(1.0);
            nightBtnTransition.setOnFinished(e -> {
                if (!nightModeBtn.getStyle().contains("rgba(46,204,113,0.8)")) {
                    nightModeBtn.setStyle(nightModeBtn.getStyle() +
                            "; -fx-background-color: linear-gradient(to bottom, rgba(46,204,113,0.8), rgba(39,174,96,0.8));");
                }
            });
        } else {
            nightModeBtn.setStyle(nightModeBtn.getStyle().replaceAll(
                    "; -fx-background-color: linear-gradient\\(to bottom, rgba\\(46,204,113,0\\.8\\), rgba\\(39,174,96,0\\.8\\)\\)", ""));
        }
        nightBtnTransition.play();

        // 護眼模式按鈕動畫更新
        javafx.animation.FadeTransition eyeCareBtnTransition = new javafx.animation.FadeTransition(
                javafx.util.Duration.millis(200), eyeCareBtn
        );

        if (settingsManager.isEyeCareMode()) {
            eyeCareBtnTransition.setFromValue(0.7);
            eyeCareBtnTransition.setToValue(1.0);
            eyeCareBtnTransition.setOnFinished(e -> {
                if (!eyeCareBtn.getStyle().contains("rgba(46,204,113,0.8)")) {
                    eyeCareBtn.setStyle(eyeCareBtn.getStyle() +
                            "; -fx-background-color: linear-gradient(to bottom, rgba(46,204,113,0.8), rgba(39,174,96,0.8));");
                }
            });
        } else {
            eyeCareBtn.setStyle(eyeCareBtn.getStyle().replaceAll(
                    "; -fx-background-color: linear-gradient\\(to bottom, rgba\\(46,204,113,0\\.8\\), rgba\\(39,174,96,0\\.8\\)\\)", ""));
        }
        eyeCareBtnTransition.play();
    }

    /**
     * 顯示增強版設定對話框
     */
    public void showSettingsDialog() {
        EnhancedSettingsDialog settingsDialog = new EnhancedSettingsDialog(settingsManager, primaryStage);

        settingsDialog.setUIUpdateCallback(() -> {
            System.out.println("設定變更回調被觸發");

            Platform.runLater(() -> {
                try {
                    // **修正1：使用強化版立即套用方法**
                    applyAllSettingsImmediate();

                    // **修正2：額外的確保機制**
                    Timeline extraEnsure = new Timeline();

                    extraEnsure.getKeyFrames().add(
                            new KeyFrame(Duration.millis(25), e -> {
                                // 確保頁碼顯示正確
                                if (pageLabel != null) {
                                    boolean shouldShow = settingsManager.isShowPageNumbers();
                                    pageLabel.setVisible(shouldShow);
                                    pageLabel.setManaged(shouldShow);
                                    System.out.println("頁碼顯示狀態更新: " + shouldShow);

                                    if (shouldShow) {
                                        updateControlsForMode();
                                    }
                                }

                                // 確保亮度正確
                                int brightness = settingsManager.getEyeCareBrightness();
                                if (imageViewer.getImageView() != null) {
                                    imageViewer.getImageView().setOpacity(brightness / 100.0);
                                }

                                // 確保主題正確套用
                                SettingsManager.ThemeMode currentTheme = settingsManager.getCurrentTheme();
                                updateCenterPaneBackgroundImmediate(currentTheme);

                                System.out.println("25ms額外確保完成");
                            })
                    );

                    extraEnsure.getKeyFrames().add(
                            new KeyFrame(Duration.millis(75), e -> {
                                // 最終確保
                                updateUI();
                                forceUIRefresh();
                                
                                // 強制重新布局
                                if (centerPane != null) {
                                    centerPane.requestLayout();
                                    if (centerPane.getParent() != null) {
                                        centerPane.getParent().requestLayout();
                                    }
                                }
                                
                                System.out.println("75ms最終確保完成");
                            })
                    );

                    extraEnsure.play();

                    System.out.println("增強UI更新完成");
                } catch (Exception e) {
                    System.err.println("設定套用錯誤: " + e.getMessage());
                    e.printStackTrace();
                }
            });
        });

        settingsDialog.show();
    }
    public void applyAllSettings() {
        System.out.println("開始套用所有設定...");

        try {
            // 1. 套用主題設定
            SettingsManager.ThemeMode currentTheme = settingsManager.getCurrentTheme();
            String backgroundColor = currentTheme.getBackgroundColor();
            String textColor = currentTheme.getTextColor();

            // **修正：立即更新背景色**
            if (imageViewer.getScrollPane() != null) {
                String newStyle = "-fx-background: " + backgroundColor + "; -fx-background-color: " + backgroundColor + ";";
                imageViewer.getScrollPane().setStyle(newStyle);
            }

            // **修正：更新主場景背景**
            if (primaryStage.getScene() != null) {
                primaryStage.getScene().getRoot().setStyle(
                        "-fx-background-color: " + backgroundColor + ";"
                );
            }

            // 2. **修正：立即套用亮度設定**
            applyBrightnessSettingsImmediate();

            // 3. **修正：立即更新頁碼顯示**
            updatePageNumbersVisibilityImmediate();

            // 4. 套用其他設定
            imageViewer.setFitMode(E_Reader.viewer.ImageViewer.FitMode.valueOf(settingsManager.getFitMode().toString()));

            // 5. 更新中央面板背景
            updateCenterPaneBackground(currentTheme);

            // 6. 如果在文字模式，也更新文字渲染器的主題
            if (stateManager.isTextMode()) {
                textRenderer.setThemeColors(currentTheme);
            }

            // 7. **修正：強制UI更新**
            Platform.runLater(() -> {
                updateUI();
                centerPane.requestLayout();
                if (centerPane.getParent() != null) {
                    centerPane.getParent().requestLayout();
                }
            });

            System.out.println("所有設定套用完成");

        } catch (Exception e) {
            System.err.println("套用設定時發生錯誤: " + e.getMessage());
            e.printStackTrace();
        }
    }


    // 新增：立即更新頁碼顯示的方法
    private void updatePageNumbersVisibilityImmediate() {
        boolean showPageNumbers = settingsManager.isShowPageNumbers();

        System.out.println("立即更新頁碼顯示狀態: " + showPageNumbers);

        if (pageLabel != null) {
            if (showPageNumbers) {
                pageLabel.setVisible(true);
                pageLabel.setManaged(true);
                pageLabel.setOpacity(1.0);
                pageLabel.setDisable(false);
                System.out.println("頁碼標籤已立即顯示");
            } else {
                pageLabel.setVisible(false);
                pageLabel.setManaged(false);
                pageLabel.setOpacity(0.0);
                pageLabel.setDisable(true);
                System.out.println("頁碼標籤已立即隱藏");
            }

            // **立即強制重新布局**
            if (centerPane != null) {
                centerPane.autosize();
                centerPane.requestLayout();
                if (centerPane.getParent() != null) {
                    centerPane.getParent().requestLayout();
                }
            }

            // 強制更新UI
            Platform.runLater(() -> {
                updateUI();
                if (controlsContainer != null) {
                    controlsContainer.requestLayout();
                }
            });
        } else {
            System.err.println("錯誤: pageLabel 為 null");
        }
    }

    // 新增：更新亮度覆蓋層的方法
    private void updateBrightnessOverlay(int brightness) {
        if (centerPane != null) {
            javafx.scene.shape.Rectangle overlay = (javafx.scene.shape.Rectangle) centerPane.lookup("#brightnessOverlay");

            if (overlay == null) {
                // 創建新的覆蓋層
                overlay = new javafx.scene.shape.Rectangle();
                overlay.setId("brightnessOverlay");
                overlay.setMouseTransparent(true);
                overlay.widthProperty().bind(centerPane.widthProperty());
                overlay.heightProperty().bind(centerPane.heightProperty());
                centerPane.getChildren().add(overlay);
            }

            // 亮度越低，覆蓋層越深
            double overlayOpacity = Math.max(0, (100 - brightness) / 100.0 * 0.5);
            overlay.setFill(javafx.scene.paint.Color.BLACK);
            overlay.setOpacity(overlayOpacity);
        }
    }

    /**
     * 更新中央面板背景
     */
    private void updateCenterPaneBackground(SettingsManager.ThemeMode theme) {
        // 根據主題調整背景漸變
        String bgGradient = switch (theme) {
            case LIGHT -> "-fx-background-color: linear-gradient(to bottom, rgba(248,248,248,0.98) 0%, rgba(255,255,255,0.95) 100%);";
            case DARK -> "-fx-background-color: linear-gradient(to bottom, rgba(18,18,18,0.98) 0%, rgba(25,25,25,0.95) 100%);";
            case BLACK -> "-fx-background-color: linear-gradient(to bottom, rgba(0,0,0,0.98) 0%, rgba(8,8,8,0.95) 100%);";
            case EYE_CARE -> "-fx-background-color: linear-gradient(to bottom, rgba(26,26,15,0.98) 0%, rgba(32,32,20,0.95) 100%);";
            case SEPIA -> "-fx-background-color: linear-gradient(to bottom, rgba(244,236,216,0.98) 0%, rgba(240,230,210,0.95) 100%);";
            default -> "-fx-background-color: linear-gradient(to bottom, rgba(18,18,18,0.98) 0%, rgba(25,25,25,0.95) 100%);";
        };

        centerPane.setStyle(bgGradient);
    }

    /**
     * 套用亮度設定
     */
    private void applyBrightnessSettings() {
        int brightness = settingsManager.getEyeCareBrightness();
        double opacity = brightness / 100.0;

        System.out.println("套用亮度設定: " + brightness + "% (透明度: " + opacity + ")");

        // 套用到圖片檢視器 - 修正：使用 setOpacity 而不是 FadeTransition
        if (imageViewer.getImageView() != null) {
            imageViewer.getImageView().setOpacity(opacity);
        }

        // 如果在文字模式，也套用到文字渲染器
        if (stateManager.isTextMode() && textRenderer.getMainContainer() != null) {
            textRenderer.getMainContainer().setOpacity(opacity);
        }

        // 套用到整個中央面板以確保所有內容都受到影響
        if (centerPane != null) {
            // 使用CSS濾鏡來調整亮度
            String brightnessFilter = String.format("-fx-effect: dropshadow(gaussian, transparent, 0, 0, 0, 0); " +
                    "-fx-background-color: rgba(0, 0, 0, %f);", 1.0 - opacity);

            // 創建一個半透明覆蓋層來模擬亮度調整
            if (centerPane.lookup("#brightnessOverlay") == null) {
                javafx.scene.shape.Rectangle brightnessOverlay = new javafx.scene.shape.Rectangle();
                brightnessOverlay.setId("brightnessOverlay");
                brightnessOverlay.setMouseTransparent(true);
                brightnessOverlay.widthProperty().bind(centerPane.widthProperty());
                brightnessOverlay.heightProperty().bind(centerPane.heightProperty());
                centerPane.getChildren().add(brightnessOverlay);
            }

            javafx.scene.shape.Rectangle overlay = (javafx.scene.shape.Rectangle) centerPane.lookup("#brightnessOverlay");
            if (overlay != null) {
                // 亮度越低，覆蓋層越深
                double overlayOpacity = Math.max(0, (100 - brightness) / 100.0 * 0.5);
                overlay.setFill(javafx.scene.paint.Color.BLACK);
                overlay.setOpacity(overlayOpacity);
            }
        }
    }

    /**
     * 更新頁碼顯示狀態
     */
    private void updatePageNumbersVisibility() {
        boolean showPageNumbers = settingsManager.isShowPageNumbers();

        System.out.println("更新頁碼顯示狀態: " + showPageNumbers);

        if (pageLabel != null) {
            if (showPageNumbers) {
                pageLabel.setVisible(true);
                pageLabel.setManaged(true);
                pageLabel.setOpacity(1.0);
                System.out.println("頁碼標籤已顯示");
            } else {
                pageLabel.setVisible(false);
                pageLabel.setManaged(false);
                pageLabel.setOpacity(0.0);
                System.out.println("頁碼標籤已隱藏");
            }

            // **新增：強制重新布局**
            Platform.runLater(() -> {
                centerPane.autosize();
                centerPane.requestLayout();
                if (centerPane.getParent() != null) {
                    centerPane.getParent().requestLayout();
                }
            });
        } else {
            System.err.println("錯誤: pageLabel 為 null");
        }
    }

    /**
     * 更新控制元素顯示
     */
    private void updateControlsVisibility() {
        boolean enableTouchNav = settingsManager.isEnableTouchNavigation();

        // 這裡可以根據觸控導覽設定來調整UI元素
        // 例如：顯示或隱藏特定的觸控提示等

        // 更新自動保存間隔
        int autoSaveInterval = settingsManager.getAutoSaveInterval();
        if (timerManager != null) {
            timerManager.updateAutoSaveInterval(autoSaveInterval);
        }
    }

    /**
     * 更新護眼提醒
     */
    private void updateEyeCareReminder() {
        if (settingsManager.isEyeCareMode() && !timerManager.isEyeCareReminderRunning()) {
            timerManager.startEyeCareReminder(() ->
                    showNotification("護眼提醒 👁️", "已閱讀30分鐘，建議休息片刻"));
        } else if (!settingsManager.isEyeCareMode() && timerManager.isEyeCareReminderRunning()) {
            timerManager.stopEyeCareReminder();
        }
    }


    /**
     * 根據通知標題獲取對應圖標
     */
    private String getNotificationIcon(String title) {
        if (title.contains("成功") || title.contains("完成") || title.contains("已")) {
            return "✅";
        } else if (title.contains("錯誤") || title.contains("失敗")) {
            return "❌";
        } else if (title.contains("警告") || title.contains("注意")) {
            return "⚠️";
        } else if (title.contains("設定") || title.contains("配置")) {
            return "⚙️";
        } else if (title.contains("檔案") || title.contains("文件")) {
            return "📄";
        } else if (title.contains("專注") || title.contains("模式")) {
            return "🎯";
        } else if (title.contains("閱讀") || title.contains("時間")) {
            return "📚";
        } else if (title.contains("護眼") || title.contains("夜間")) {
            return "👁️";
        } else if (title.contains("歡迎")) {
            return "👋";
        } else {
            return "ℹ️";
        }
    }

    private void applyAllSettingsImmediate() {
        System.out.println("開始強化版立即套用所有設定...");

        try {
            // **步驟1：重新載入設定檔確保最新**
            settingsManager.loadSettings();

            // **步驟2：立即套用主題設定**
            SettingsManager.ThemeMode currentTheme = settingsManager.getCurrentTheme();
            String backgroundColor = currentTheme.getBackgroundColor();
            String textColor = currentTheme.getTextColor();

            // 立即更新背景色
            if (imageViewer.getScrollPane() != null) {
                String newStyle = "-fx-background: " + backgroundColor + "; -fx-background-color: " + backgroundColor + ";";
                imageViewer.getScrollPane().setStyle(newStyle);
            }

            // 立即更新主場景背景
            if (primaryStage.getScene() != null) {
                primaryStage.getScene().getRoot().setStyle(
                        "-fx-background-color: " + backgroundColor + ";"
                );
            }

            // **步驟3：立即套用亮度設定**
            int brightness = settingsManager.getEyeCareBrightness();
            double opacity = brightness / 100.0;

            if (imageViewer.getImageView() != null) {
                imageViewer.getImageView().setOpacity(opacity);
            }

            if (stateManager.isTextMode() && textRenderer.getMainContainer() != null) {
                textRenderer.getMainContainer().setOpacity(opacity);
            }

            // **步驟4：立即更新頁碼顯示**
            boolean showPageNumbers = settingsManager.isShowPageNumbers();
            System.out.println("立即套用頁碼顯示設定: " + showPageNumbers);

            if (pageLabel != null) {
                pageLabel.setVisible(showPageNumbers);
                pageLabel.setManaged(showPageNumbers);
                pageLabel.setOpacity(showPageNumbers ? 1.0 : 0.0);

                // 如果顯示頁碼，確保內容是最新的
                if (showPageNumbers) {
                    updateControlsForMode();
                }
            }

            // **步驟5：強制UI重繪**
            Platform.runLater(() -> {
                if (centerPane != null) {
                    centerPane.applyCss();
                    centerPane.autosize();
                    centerPane.requestLayout();
                }

                if (primaryStage.getScene() != null) {
                    primaryStage.getScene().getRoot().applyCss();
                    primaryStage.getScene().getRoot().requestLayout();
                }

                // 更新UI狀態
                updateUI();
            });

            System.out.println("強化版設定套用完成");

        } catch (Exception e) {
            System.err.println("強化版設定套用失敗: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * **新增：立即套用主題設定**
     */
    private void applyThemeSettingsImmediate() {
        SettingsManager.ThemeMode currentTheme = settingsManager.getCurrentTheme();
        String backgroundColor = currentTheme.getBackgroundColor();
        String textColor = currentTheme.getTextColor();

        System.out.println("立即套用主題: " + currentTheme.getDisplayName());

        // **立即更新背景色**
        if (imageViewer.getScrollPane() != null) {
            String newStyle = "-fx-background: " + backgroundColor + "; -fx-background-color: " + backgroundColor + ";";
            imageViewer.getScrollPane().setStyle(newStyle);
        }

        // **立即更新主場景背景**
        if (primaryStage.getScene() != null) {
            primaryStage.getScene().getRoot().setStyle(
                    "-fx-background-color: " + backgroundColor + ";"
            );
        }

        // **立即更新中央面板背景**
        updateCenterPaneBackgroundImmediate(currentTheme);

        // **如果在文字模式，立即更新文字渲染器的主題**
        if (stateManager.isTextMode()) {
            textRenderer.setThemeColors(currentTheme);
        }
    }

    /**
     * **新增：立即更新中央面板背景**
     */
    private void updateCenterPaneBackgroundImmediate(SettingsManager.ThemeMode theme) {
        String bgGradient = switch (theme) {
            case LIGHT -> "-fx-background-color: linear-gradient(to bottom, rgba(248,248,248,0.98) 0%, rgba(255,255,255,0.95) 100%);";
            case DARK -> "-fx-background-color: linear-gradient(to bottom, rgba(18,18,18,0.98) 0%, rgba(25,25,25,0.95) 100%);";
            case BLACK -> "-fx-background-color: linear-gradient(to bottom, rgba(0,0,0,0.98) 0%, rgba(8,8,8,0.95) 100%);";
            case EYE_CARE -> "-fx-background-color: linear-gradient(to bottom, rgba(26,26,15,0.98) 0%, rgba(32,32,20,0.95) 100%);";
            case SEPIA -> "-fx-background-color: linear-gradient(to bottom, rgba(244,236,216,0.98) 0%, rgba(240,230,210,0.95) 100%);";
            default -> "-fx-background-color: linear-gradient(to bottom, rgba(18,18,18,0.98) 0%, rgba(25,25,25,0.95) 100%);";
        };

        if (centerPane != null) {
            centerPane.setStyle(bgGradient);
        }
    }

    /**
     * **修正：立即套用亮度設定**
     */
    private void applyBrightnessSettingsImmediate() {
        int brightness = settingsManager.getEyeCareBrightness();
        double opacity = brightness / 100.0;

        System.out.println("立即套用亮度設定: " + brightness + "% (透明度: " + opacity + ")");

        // **立即套用到圖片檢視器**
        if (imageViewer.getImageView() != null) {
            imageViewer.getImageView().setOpacity(opacity);
        }

        // **如果在文字模式，立即套用到文字渲染器**
        if (stateManager.isTextMode() && textRenderer.getMainContainer() != null) {
            textRenderer.getMainContainer().setOpacity(opacity);
        }

        // **立即更新亮度覆蓋層**
        updateBrightnessOverlayImmediate(brightness);
    }

    /**
     * **新增：立即更新亮度覆蓋層**
     */
    private void updateBrightnessOverlayImmediate(int brightness) {
        if (centerPane != null) {
            javafx.scene.shape.Rectangle overlay = (javafx.scene.shape.Rectangle) centerPane.lookup("#brightnessOverlay");

            if (overlay == null) {
                // 立即創建新的覆蓋層
                overlay = new javafx.scene.shape.Rectangle();
                overlay.setId("brightnessOverlay");
                overlay.setMouseTransparent(true);
                overlay.widthProperty().bind(centerPane.widthProperty());
                overlay.heightProperty().bind(centerPane.heightProperty());
                centerPane.getChildren().add(overlay);
            }

            // 立即調整覆蓋層透明度
            double overlayOpacity = Math.max(0, (100 - brightness) / 100.0 * 0.5);
            overlay.setFill(javafx.scene.paint.Color.BLACK);
            overlay.setOpacity(overlayOpacity);
        }
    }

    private void updateUIImmediate() {
        // **立即更新閱讀進度**
        updateReadingProgress();

        // **立即更新控制項目狀態**
        updateControlsForMode();

        // **立即更新按鈕狀態**
        updateButtonStatesImmediate();
    }

    /**
     * **新增：立即更新按鈕狀態**
     */
    private void updateButtonStatesImmediate() {
        // **立即更新夜間模式按鈕狀態**
        if (settingsManager.isNightMode()) {
            if (!nightModeBtn.getStyle().contains("rgba(46,204,113,0.8)")) {
                nightModeBtn.setStyle(nightModeBtn.getStyle() +
                        "; -fx-background-color: linear-gradient(to bottom, rgba(46,204,113,0.8), rgba(39,174,96,0.8));");
            }
        } else {
            nightModeBtn.setStyle(nightModeBtn.getStyle().replaceAll(
                    "; -fx-background-color: linear-gradient\\(to bottom, rgba\\(46,204,113,0\\.8\\), rgba\\(39,174,96,0\\.8\\)\\)", ""));
        }

        // **立即更新護眼模式按鈕狀態**
        if (settingsManager.isEyeCareMode()) {
            if (!eyeCareBtn.getStyle().contains("rgba(46,204,113,0.8)")) {
                eyeCareBtn.setStyle(eyeCareBtn.getStyle() +
                        "; -fx-background-color: linear-gradient(to bottom, rgba(46,204,113,0.8), rgba(39,174,96,0.8));");
            }
        } else {
            eyeCareBtn.setStyle(eyeCareBtn.getStyle().replaceAll(
                    "; -fx-background-color: linear-gradient\\(to bottom, rgba\\(46,204,113,0\\.8\\), rgba\\(39,174,96,0\\.8\\)\\)", ""));
        }
    }

    /**
     * **新增：強制UI重新整理**
     */
    private void forceUIRefresh() {
        Platform.runLater(() -> {
            try {
                // **方法1：強制重新計算樣式**
                if (primaryStage.getScene() != null) {
                    primaryStage.getScene().getRoot().applyCss();
                    primaryStage.getScene().getRoot().autosize();
                    primaryStage.getScene().getRoot().requestLayout();
                }

                // **方法2：強制中央面板重繪**
                if (centerPane != null) {
                    centerPane.applyCss();
                    centerPane.autosize();
                    centerPane.requestLayout();
                }

                // **方法3：如果有圖片，強制重新渲染當前頁面**
                if (imageViewer.hasImages()) {
                    int currentIndex = imageViewer.getCurrentIndex();
                    // 觸發圖片重新渲染
                    imageViewer.refreshCurrentImage();
                }

                // **方法4：如果在文字模式，強制重新渲染文字**
                if (stateManager.isTextMode() && textRenderer.getMainContainer() != null) {
                    textRenderer.getMainContainer().applyCss();
                    textRenderer.getMainContainer().autosize();
                    textRenderer.getMainContainer().requestLayout();
                }

                System.out.println("UI強制重新整理完成");

            } catch (Exception e) {
                System.err.println("UI重新整理時發生錯誤: " + e.getMessage());
            }
        });
    }

    private void simulatePageChange() {
        try {
            System.out.println("模擬頁面變化，強制觸發UI更新...");

            // 方法1：如果有開啟的檔案，模擬微小的頁面移動
            if (!stateManager.getCurrentFilePath().isEmpty()) {
                Platform.runLater(() -> {
                    if (stateManager.isTextMode()) {
                        // 文字模式：重新渲染當前頁面
                        int currentPage = textRenderer.getCurrentPageIndex();
                        textRenderer.refreshCurrentPage(); // 需要在TextRenderer中實現
                    } else {
                        // 圖片模式：重新刷新當前圖片
                        imageViewer.refreshCurrentImage(); // 需要在ImageViewer中實現
                    }

                    // 強制更新UI元素
                    updateUI();

                    System.out.println("頁面變化模擬完成");
                });
            }

            // 方法2：強制重新計算頁碼顯示
            Platform.runLater(() -> {
                updatePageNumbersVisibilityImmediate();
                applyBrightnessSettingsImmediate();
            });

        } catch (Exception e) {
            System.err.println("模擬頁面變化時發生錯誤: " + e.getMessage());
        }
    }
}
