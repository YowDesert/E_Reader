package E_Reader.ui;

import E_Reader.core.*;
import E_Reader.filemanager.FileManagerController;
import E_Reader.settings.SettingsManager;
import E_Reader.utils.AlertHelper;
import E_Reader.viewer.*;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.scene.image.Image;

import java.io.File;
import java.io.FileOutputStream;
import java.util.List;
import java.util.Properties;
import java.util.Timer;
import java.util.TimerTask;

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
    private final TextExtractor textExtractor;
    private final BookmarkManager bookmarkManager;
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
    private Button toggleNavBarBtn; // 新增：導覽列切換按鈕
    private ProgressBar readingProgressBar;
    private Label readingTimeLabel;
    
    // 導覽列控制器
    private NavigationBarController navBarController;

    public MainController(Stage primaryStage) {
        this.primaryStage = primaryStage;

        // 初始化核心組件
        this.imageViewer = new ImageViewer();
        this.textRenderer = new TextRenderer();
        this.imageLoader = new ImageLoader();
        this.pdfLoader = new PdfLoader();
        this.epubLoader = new EpubLoader();
        this.textExtractor = new TextExtractor();
        this.bookmarkManager = new BookmarkManager();
        this.settingsManager = new SettingsManager();
        // 修正：使用單一 Stage 實例來避免重複創建
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
        applySettings();

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
        mainLayout.setStyle("-fx-background-color: #2b2b2b;");

        centerPane = createCenterPane();
        mainLayout.setCenter(centerPane);

        createControlPanels();
        controlsContainer = new VBox();
        controlsContainer.getChildren().addAll(topControls, bottomControls);
        mainLayout.setTop(controlsContainer);
        
        // 初始化導覽列控制器
        navBarController = new NavigationBarController(controlsContainer, primaryStage, centerPane);

        Scene scene = new Scene(mainLayout, 1200, 800);
        scene.getRoot().setStyle("-fx-font-family: 'Microsoft JhengHei', sans-serif;");

        primaryStage.setScene(scene);
        primaryStage.setTitle("E_Reader 漫畫＆PDF閱讀器 v3.0 Enhanced");
    }

    private StackPane createCenterPane() {
        StackPane centerPane = new StackPane();
        centerPane.getChildren().add(imageViewer.getScrollPane());

        // 閱讀進度條
        readingProgressBar = new ProgressBar(0);
        readingProgressBar.setPrefWidth(300);
        readingProgressBar.setStyle("-fx-accent: #0078d4;");
        StackPane.setAlignment(readingProgressBar, Pos.BOTTOM_CENTER);
        StackPane.setMargin(readingProgressBar, new Insets(0, 0, 20, 0));
        centerPane.getChildren().add(readingProgressBar);

        // 閱讀時間顯示
        readingTimeLabel = new Label("閱讀時間: 00:00:00");
        readingTimeLabel.setStyle("-fx-text-fill: white; -fx-background-color: rgba(0,0,0,0.7); " +
                "-fx-padding: 5 10 5 10; -fx-background-radius: 15;");
        StackPane.setAlignment(readingTimeLabel, Pos.TOP_RIGHT);
        StackPane.setMargin(readingTimeLabel, new Insets(20, 20, 0, 0));
        centerPane.getChildren().add(readingTimeLabel);
        
        // 頁碼標籤 - 放在右下角
        pageLabel = new Label("頁面: 0 / 0");
        pageLabel.setStyle("-fx-text-fill: white; -fx-background-color: rgba(0,0,0,0.7); " +
                "-fx-padding: 5 10 5 10; -fx-background-radius: 15; -fx-font-size: 14px;");
        StackPane.setAlignment(pageLabel, Pos.BOTTOM_RIGHT);
        StackPane.setMargin(pageLabel, new Insets(0, 20, 20, 0));
        centerPane.getChildren().add(pageLabel);

        centerPane.setStyle("-fx-background-color: #1e1e1e;");
        return centerPane;
    }

    private void createControlPanels() {
        // 使用工廠模式創建控制面板
        topControls = controlsFactory.createTopControls(this);
        bottomControls = controlsFactory.createBottomControls(this);

        // 獲取按鈕引用
        textModeBtn = controlsFactory.getTextModeButton();
        autoScrollBtn = controlsFactory.getAutoScrollButton();
        nightModeBtn = controlsFactory.getNightModeButton();
        eyeCareBtn = controlsFactory.getEyeCareButton();
        toggleNavBarBtn = controlsFactory.getToggleNavBarButton(); // 新增
        pageField = controlsFactory.getPageField();
        
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
                case PAGE_UP:
                    goToPreviousPage();
                    break;
                case RIGHT:
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

    /**
     * 初始化檔案管理器
     */
    private void initializeFileManager() {
        fileManagerController.initialize(this::openFileFromManager);
    }
    
    /**
     * 開啟檔案管理器
     */
    public void showFileManager() {
        // 如果有開啟的檔案，先關閉它
        if (!stateManager.getCurrentFilePath().isEmpty()) {
            closeCurrentFile();
        }
        
        // 修正：直接顯示檔案管理器，不重複創建
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
            // 開啟PDF檔案
            try {
                List<Image> images = pdfLoader.loadImagesFromPdf(file);
                if (!images.isEmpty()) {
                    stateManager.setFileLoaded(file.getAbsolutePath(), true, false, images, null);
                    switchToImageMode();
                    imageViewer.setImages(images);
                    primaryStage.setTitle("E_Reader - " + file.getName());
                    updateUI();
                    resetTextModeButton();
                    
                    // 顯示成功訊息
                    showNotification("檔案開啟", "成功開啟 PDF檔案: " + file.getName());
                }
            } catch (Exception ex) {
                AlertHelper.showError("無法載入 PDF 檔案", ex.getMessage());
            }
        } else if (fileName.endsWith(".epub")) {
            // 開啟EPUB檔案
            try {
                List<Image> images = epubLoader.loadImagesFromEpub(file);
                if (!images.isEmpty()) {
                    stateManager.setFileLoaded(file.getAbsolutePath(), false, true, images, null);
                    switchToImageMode();
                    imageViewer.setImages(images);
                    primaryStage.setTitle("E_Reader - " + file.getName());
                    updateUI();
                    resetTextModeButton();
                    
                    // 顯示成功訊息
                    showNotification("檔案開啟", "成功開啟 EPUB檔案: " + file.getName());
                } else {
                    AlertHelper.showError("載入失敗", "EPUB檔案中沒有可讀取的內容");
                }
            } catch (Exception ex) {
                AlertHelper.showError("無法載入 EPUB 檔案", ex.getMessage());
            }
        } else if (fileName.endsWith(".jpg") || fileName.endsWith(".jpeg") || 
                   fileName.endsWith(".png") || fileName.endsWith(".gif") || 
                   fileName.endsWith(".bmp")) {
            // 開啟圖片檔案 - 載入整個資料夾
            File parentFolder = file.getParentFile();
            if (parentFolder != null) {
                List<Image> images = imageLoader.loadImagesFromFolder(parentFolder);
                if (!images.isEmpty()) {
                    stateManager.setFileLoaded(parentFolder.getAbsolutePath(), false, false, images, null);
                    switchToImageMode();
                    imageViewer.setImages(images);
                    
                    // 找到當前檔案的索引並跳轉到該頁
                    String targetFileName = file.getName();
                    for (int i = 0; i < images.size(); i++) {
                        // 這裡需要修改以適配你的圖片載入器的檔案名稱索引
                        // 暂時跳到第一張圖片
                        break;
                    }
                    
                    primaryStage.setTitle("E_Reader - " + parentFolder.getName());
                    updateUI();
                    resetTextModeButton();
                    
                    // 顯示成功訊息
                    showNotification("檔案開啟", "成功載入圖片資料夾: " + parentFolder.getName());
                } else {
                    AlertHelper.showError("載入失敗", "資料夾中沒有找到支援的圖片格式");
                }
            }
        } else {
            AlertHelper.showError("不支援的檔案格式", 
                "支援的格式：PDF檔案、EPUB檔案和圖片檔案 (JPG, PNG, GIF, BMP)");
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
                switchToImageMode();
                imageViewer.setImages(images);
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
            try {
                List<Image> images = pdfLoader.loadImagesFromPdf(pdfFile);
                if (!images.isEmpty()) {
                    stateManager.setFileLoaded(pdfFile.getAbsolutePath(), true, false, images, null);
                    switchToImageMode();
                    imageViewer.setImages(images);
                    primaryStage.setTitle("E_Reader - " + pdfFile.getName());
                    updateUI();
                    resetTextModeButton();
                }
            } catch (Exception ex) {
                AlertHelper.showError("無法載入 PDF 檔案", ex.getMessage());
            }
        }
    }

    // 模式切換方法
    public void toggleTextMode() {
        if (stateManager.getCurrentFilePath().isEmpty()) {
            AlertHelper.showError("提示", "請先開啟檔案");
            return;
        }

        boolean isTextMode = !stateManager.isTextMode();
        stateManager.setTextMode(isTextMode);

        if (isTextMode) {
            textModeBtn.setText("🖼️ 圖片模式");
            textModeBtn.setStyle(textModeBtn.getStyle() + "; -fx-background-color: #28a745;");
            switchToTextMode();
        } else {
            textModeBtn.setText("📖 文字模式");
            textModeBtn.setStyle(textModeBtn.getStyle().replace("; -fx-background-color: #28a745", ""));
            switchToImageMode();
        }
    }

    private void switchToTextMode() {
        showLoadingIndicator("正在提取文字內容...");

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
                    hideLoadingIndicator();

                    if (textPages != null && !textPages.isEmpty()) {
                        stateManager.setCurrentTextPages(textPages);
                        centerPane.getChildren().clear();
                        centerPane.getChildren().addAll(
                                textRenderer.getMainContainer(),
                                readingProgressBar,
                                readingTimeLabel,
                                pageLabel
                        );

                        textRenderer.setPages(textPages);
                        textRenderer.setThemeColors(settingsManager.getCurrentTheme());
                        showNotification("文字模式", "已成功提取 " + textPages.size() + " 頁文字內容");
                    } else {
                        AlertHelper.showError("文字提取失敗", "無法從檔案中提取文字內容");
                        stateManager.setTextMode(false);
                        resetTextModeButton();
                    }

                    updateUI();
                });

            } catch (Exception e) {
                Platform.runLater(() -> {
                    hideLoadingIndicator();
                    AlertHelper.showError("文字提取錯誤", e.getMessage());
                    stateManager.setTextMode(false);
                    resetTextModeButton();
                });
            }
        });

        extractThread.setDaemon(true);
        extractThread.start();
    }

    private void switchToImageMode() {
        centerPane.getChildren().clear();
        centerPane.getChildren().addAll(
                imageViewer.getScrollPane(),
                readingProgressBar,
                readingTimeLabel,
                pageLabel
        );
        updateUI();
    }

    // 導航方法
    public void goToFirstPage() {
        if (stateManager.isTextMode()) {
            textRenderer.goToPage(0);
        } else {
            imageViewer.goToFirstPage();
        }
        updateUI();
    }

    public void goToPreviousPage() {
        if (stateManager.isTextMode()) {
            int currentIndex = textRenderer.getCurrentPageIndex();
            if (currentIndex > 0) {
                textRenderer.goToPage(currentIndex - 1);
            }
        } else {
            imageViewer.prevPage();
        }
        updateUI();
    }

    public void goToNextPage() {
        if (stateManager.isTextMode()) {
            int currentIndex = textRenderer.getCurrentPageIndex();
            if (currentIndex < textRenderer.getTotalPages() - 1) {
                textRenderer.goToPage(currentIndex + 1);
            }
        } else {
            imageViewer.nextPage();
        }
        updateUI();
    }

    public void goToLastPage() {
        if (stateManager.isTextMode()) {
            textRenderer.goToPage(textRenderer.getTotalPages() - 1);
        } else {
            imageViewer.goToLastPage();
        }
        updateUI();
    }

    public void goToPage(int pageIndex) {
        if (stateManager.getCurrentFilePath().isEmpty()) return;

        if (stateManager.isTextMode()) {
            textRenderer.goToPage(pageIndex);
        } else {
            imageViewer.goToPage(pageIndex);
        }
        updateUI();
    }

    // UI 更新方法
    private void updateUI() {
        updateReadingProgress();
        updateControlsForMode();
    }

    private void updateReadingProgress() {
        double progress = 0;

        if (stateManager.isTextMode() && stateManager.getCurrentTextPages() != null) {
            progress = (double) (textRenderer.getCurrentPageIndex() + 1) / stateManager.getCurrentTextPages().size();
        } else if (!stateManager.isTextMode() && imageViewer.hasImages()) {
            progress = (double) (imageViewer.getCurrentIndex() + 1) / imageViewer.getTotalPages();
        }

        readingProgressBar.setProgress(progress);
    }

    private void updateControlsForMode() {
        // 更新 UI 控制按鈕的顯示
        controlsFactory.updateControlsForMode(stateManager.isTextMode());
        
        // 更新頁碼顯示
        if (stateManager.isTextMode() && stateManager.getCurrentTextPages() != null) {
            pageLabel.setText("文字: " + (textRenderer.getCurrentPageIndex() + 1) + " / " + stateManager.getCurrentTextPages().size());
        } else if (!stateManager.isTextMode() && imageViewer.hasImages()) {
            pageLabel.setText("頁面: " + (imageViewer.getCurrentIndex() + 1) + " / " + imageViewer.getTotalPages());
        } else {
            pageLabel.setText("頁面: 0 / 0");
        }
    }

    private void resetTextModeButton() {
        textModeBtn.setText("📖 文字模式");
        textModeBtn.setStyle(textModeBtn.getStyle().replace("; -fx-background-color: #28a745", ""));
    }

    // 載入指示器
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
        // 停止所有計時器
        if (stateManager.isAutoScrolling()) {
            toggleAutoScroll();
        }
        
        // 如果設定要求記住最後檔案，儲存閱讀位置
        if (settingsManager.isRememberLastFile() && !stateManager.getCurrentFilePath().isEmpty()) {
            saveLastReadingPosition();
        }
        
        // 清空狀態
        stateManager.clearCurrentFile();
        
        // 清空檢視器
        imageViewer.clearImages();
        textRenderer.clearPages();
        
        // 切換回圖片模式
        if (stateManager.isTextMode()) {
            stateManager.setTextMode(false);
            switchToImageMode();
            resetTextModeButton();
        }
        
        // 重置UI
        primaryStage.setTitle("E_Reader 漫畫＆PDF閱讀器 v3.0 Enhanced");
        updateUI();
        
        // 顯示關閉通知
        showNotification("檔案已關閉", "檔案已關閉，可以開啟新檔案");
    }
    
    // 工具方法
    private void handleImageClick(MouseEvent event) {
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

        // 更新按鈕狀態 - 基於 nightMode 標記
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

        // 更新按鈕狀態
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
        boolean focusMode = !controlsContainer.isVisible();
        controlsContainer.setVisible(!focusMode);
        controlsContainer.setManaged(!focusMode);
        readingTimeLabel.setVisible(!focusMode);
        readingProgressBar.setVisible(!focusMode);

        if (focusMode) {
            showNotification("專注模式", "按 F 鍵或點擊中央退出專注模式");
        }
    }
    
    /**
     * 切換導覽列顯示/隱藏狀態
     */
    public void toggleNavigationBar() {
        if (navBarController != null) {
            navBarController.toggleNavigationBar();
            
            // 更新按鈕顯示
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
                toggleAutoScroll(); // 停止自動翻頁
            }
        });
    }

    // 通知顯示
    public void showNotification(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.show();

        // 3秒後自動關閉
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                Platform.runLater(() -> alert.close());
            }
        }, 3000);
    }

    // 設定套用
    public void applySettings() {
        SettingsManager.ThemeMode currentTheme = settingsManager.getCurrentTheme();

        String backgroundColor = currentTheme.getBackgroundColor();
        String textColor = currentTheme.getTextColor();

        // 更新背景顏色
        if (imageViewer.getScrollPane() != null) {
            imageViewer.getScrollPane().setStyle("-fx-background: " + backgroundColor + "; -fx-background-color: " + backgroundColor + ";");
        }
        centerPane.setStyle("-fx-background-color: " + backgroundColor + ";");

        // 如果在文字模式，也更新文字渲染器的主題
        if (stateManager.isTextMode()) {
            textRenderer.setThemeColors(currentTheme);
        }

        // 套用其他設定
        imageViewer.setFitMode(settingsManager.getFitMode());

        // 更新按鈕狀態
        updateButtonStates();

        // 更新護眼提醒
        if (settingsManager.isEyeCareMode() && !timerManager.isEyeCareReminderRunning()) {
            timerManager.startEyeCareReminder(() ->
                    showNotification("護眼提醒", "您已經閱讀30分鐘了，建議休息5-10分鐘！"));
        } else if (!settingsManager.isEyeCareMode() && timerManager.isEyeCareReminderRunning()) {
            timerManager.stopEyeCareReminder();
        }
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

    // 設定對話框
    public void showSettingsDialog() {
        // 簡單的設定對話框實作
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("設定");
        dialog.setHeaderText("應用程式設定");

        // 創建設定選項
        VBox content = new VBox(10);
        content.setPadding(new Insets(20));

        // 主題選擇
        Label themeLabel = new Label("主題模式:");
        ComboBox<SettingsManager.ThemeMode> themeCombo = new ComboBox<>();
        themeCombo.getItems().addAll(SettingsManager.ThemeMode.values());
        themeCombo.setValue(settingsManager.getThemeMode());

        // 記住最後檔案
        CheckBox rememberFileCheckBox = new CheckBox("記住最後開啟的檔案");
        rememberFileCheckBox.setSelected(settingsManager.isRememberLastFile());

        // 顯示頁碼
        CheckBox showPageNumbersCheckBox = new CheckBox("顯示頁碼");
        showPageNumbersCheckBox.setSelected(settingsManager.isShowPageNumbers());

        content.getChildren().addAll(
                themeLabel, themeCombo,
                rememberFileCheckBox,
                showPageNumbersCheckBox
        );

        dialog.getDialogPane().setContent(content);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        dialog.showAndWait().ifPresent(result -> {
            if (result == ButtonType.OK) {
                settingsManager.setThemeMode(themeCombo.getValue());
                settingsManager.setRememberLastFile(rememberFileCheckBox.isSelected());
                settingsManager.setShowPageNumbers(showPageNumbersCheckBox.isSelected());
                settingsManager.saveSettings();
                applySettings();
            }
        });
    }
}
