package E_Reader;

import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import javafx.application.Application;
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

public class Main extends Application {

    private ImageViewer imageViewer = new ImageViewer();
    private TextRenderer textRenderer = new TextRenderer();
    private ImageLoader imageLoader = new ImageLoader();
    private PdfLoader pdfLoader = new PdfLoader();
    private TextExtractor textExtractor = new TextExtractor();
    private BookmarkManager bookmarkManager = new BookmarkManager();

    private boolean isPdfMode = false;
    private boolean isTextMode = false; // 新增：文字模式標記
    private String currentFilePath = "";
    private Stage primaryStage;
    private boolean isFullScreen = false;
    private VBox controlsContainer;
    private HBox topControls;
    private HBox bottomControls;

    // 設定面板相關
    private SettingsPanel settingsPanel = new SettingsPanel();
    private boolean isControlsVisible = true;

    // 功能相關
    private Timer readingTimer;
    private long readingStartTime;
    private long totalReadingTime = 0;
    private Label readingTimeLabel;
    private Timer eyeCareReminderTimer;
    private boolean isAutoScrolling = false;
    private Timer autoScrollTimer;
    private ProgressBar readingProgressBar;

    // 中央顯示區域
    private StackPane centerPane;
    private List<Image> currentImages;
    private List<TextExtractor.PageText> currentTextPages;

    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;
        primaryStage.setTitle("E_Reader 漫畫＆PDF閱讀器 v3.0 Enhanced (支援文字提取)");

        // 建立主版面
        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: #2b2b2b;");

        // 中央顯示區域
        centerPane = createCenterPane();
        root.setCenter(centerPane);

        // 建立控制面板
        createControlPanels();

        // 將控制面板加入主版面
        controlsContainer = new VBox();
        controlsContainer.getChildren().addAll(topControls, bottomControls);
        root.setTop(controlsContainer);

        // 設定事件處理
        setupEventHandlers(root);

        // 設定快捷鍵
        setupKeyboardShortcuts(root);

        Scene scene = new Scene(root, 1200, 800);
        scene.getStylesheets().add(getClass().getResource("/style.css").toExternalForm());

        primaryStage.setScene(scene);
        primaryStage.show();

        // 載入設定
        settingsPanel.loadSettings();
        applySettings();

        // 啟動計時器
        startReadingTimer();
        startEyeCareReminder();

        // 關閉時清理資源
        primaryStage.setOnCloseRequest(e -> {
            stopAllTimers();
            if (settingsPanel.isRememberLastFile() && !currentFilePath.isEmpty()) {
                saveLastReadingPosition();
            }
        });
    }

    private StackPane createCenterPane() {
        StackPane centerPane = new StackPane();

        // 預設顯示圖片檢視器
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

        centerPane.setStyle("-fx-background-color: #1e1e1e;");
        return centerPane;
    }

    private void createControlPanels() {
        // 上方控制列
        Button openFolderBtn = new Button("📂 圖片資料夾");
        Button openPdfBtn = new Button("📄 PDF檔案");
        Button bookmarkBtn = new Button("🔖 書籤管理");
        Button settingsBtn = new Button("⚙️ 設定");
        Button fullscreenBtn = new Button("🔲 全螢幕");
        Button exitBtn = new Button("❌ 離開");

        // 新增功能按鈕
        Button autoScrollBtn = new Button("⏯️ 自動翻頁");
        Button nightModeBtn = new Button("🌙 夜間模式");
        Button eyeCareBtn = new Button("👁️ 護眼模式");
        Button textModeBtn = new Button("📖 文字模式"); // 新增：文字模式按鈕
        Button searchBtn = new Button("🔍 搜尋文字"); // 新增：搜尋功能按鈕

        // 設定按鈕樣式
        String buttonStyle = "-fx-background-color: #404040; -fx-text-fill: white; " +
                "-fx-border-radius: 5; -fx-background-radius: 5; " +
                "-fx-padding: 8 12 8 12; -fx-font-size: 12px;";

        Button[] topButtons = {openFolderBtn, openPdfBtn, bookmarkBtn, settingsBtn,
                textModeBtn, searchBtn, autoScrollBtn, nightModeBtn, eyeCareBtn, fullscreenBtn, exitBtn};
        for (Button btn : topButtons) {
            btn.setStyle(buttonStyle);
        }

        topControls = new HBox(10);
        topControls.setAlignment(Pos.CENTER);
        topControls.setPadding(new Insets(10));
        topControls.setStyle("-fx-background-color: #333333;");
        topControls.getChildren().addAll(topButtons);

        // 下方控制列
        Button firstPageBtn = new Button("⏮️ 首頁");
        Button prevBtn = new Button("◀️ 上頁");
        Button nextBtn = new Button("下頁 ▶️");
        Button lastPageBtn = new Button("末頁 ⏭️");

        // 頁面跳轉
        TextField pageField = new TextField();
        pageField.setPrefWidth(60);
        pageField.setPromptText("頁數");
        Button goToPageBtn = new Button("跳轉");

        // 縮放控制
        Button zoomInBtn = new Button("🔍+");
        Button zoomOutBtn = new Button("🔍-");
        Button fitWidthBtn = new Button("適合寬度");
        Button fitHeightBtn = new Button("適合高度");
        Button rotateBtn = new Button("🔄 旋轉");

        // 閱讀模式控制
        Button focusModeBtn = new Button("🎯 專注模式");
        Button speedReadBtn = new Button("⚡ 快速閱讀");

        // 文字模式專用控制
        Button fontSizeIncBtn = new Button("A+"); // 增大字體
        Button fontSizeDecBtn = new Button("A-"); // 縮小字體
        Button lineSpacingBtn = new Button("📏 行距"); // 調整行距

        Label pageLabel = isTextMode ? new Label("文字: 0 / 0") : imageViewer.getPageLabel();
        pageLabel.setStyle("-fx-text-fill: white; -fx-font-size: 14px;");

        // 設定下方按鈕樣式
        Button[] bottomButtons = {firstPageBtn, prevBtn, nextBtn, lastPageBtn,
                goToPageBtn, zoomInBtn, zoomOutBtn, fitWidthBtn, fitHeightBtn,
                rotateBtn, focusModeBtn, speedReadBtn, fontSizeIncBtn, fontSizeDecBtn, lineSpacingBtn};
        for (Button btn : bottomButtons) {
            btn.setStyle(buttonStyle);
        }

        pageField.setStyle("-fx-background-color: #404040; -fx-text-fill: white; " +
                "-fx-border-color: #666666; -fx-border-radius: 3;");

        bottomControls = new HBox(10);
        bottomControls.setAlignment(Pos.CENTER);
        bottomControls.setPadding(new Insets(10));
        bottomControls.setStyle("-fx-background-color: #333333;");
        bottomControls.getChildren().addAll(firstPageBtn, prevBtn, nextBtn, lastPageBtn,
                new Separator(), pageField, goToPageBtn,
                new Separator(), zoomInBtn, zoomOutBtn,
                fitWidthBtn, fitHeightBtn, rotateBtn,
                new Separator(), fontSizeIncBtn, fontSizeDecBtn, lineSpacingBtn,
                new Separator(), focusModeBtn, speedReadBtn,
                new Separator(), pageLabel);

        // 設定事件處理器
        setupButtonHandlers(openFolderBtn, openPdfBtn, bookmarkBtn, settingsBtn,
                fullscreenBtn, exitBtn, textModeBtn, searchBtn, autoScrollBtn, nightModeBtn, eyeCareBtn,
                firstPageBtn, prevBtn, nextBtn, lastPageBtn, goToPageBtn,
                zoomInBtn, zoomOutBtn, fitWidthBtn, fitHeightBtn,
                rotateBtn, focusModeBtn, speedReadBtn, pageField,
                fontSizeIncBtn, fontSizeDecBtn, lineSpacingBtn);
    }

    private void setupButtonHandlers(Button openFolderBtn, Button openPdfBtn, Button bookmarkBtn,
                                     Button settingsBtn, Button fullscreenBtn, Button exitBtn,
                                     Button textModeBtn, Button searchBtn, Button autoScrollBtn,
                                     Button nightModeBtn, Button eyeCareBtn,
                                     Button firstPageBtn, Button prevBtn, Button nextBtn,
                                     Button lastPageBtn, Button goToPageBtn, Button zoomInBtn,
                                     Button zoomOutBtn, Button fitWidthBtn, Button fitHeightBtn,
                                     Button rotateBtn, Button focusModeBtn, Button speedReadBtn,
                                     TextField pageField, Button fontSizeIncBtn, Button fontSizeDecBtn,
                                     Button lineSpacingBtn) {

        // 原有功能
        openFolderBtn.setOnAction(e -> openImageFolder());
        openPdfBtn.setOnAction(e -> openPdfFile());
        settingsBtn.setOnAction(e -> showSettingsDialog());
        fullscreenBtn.setOnAction(e -> toggleFullscreen());
        exitBtn.setOnAction(e -> primaryStage.close());

        // 書籤功能
        bookmarkBtn.setOnAction(e -> showBookmarkDialog());

        // 新功能
        textModeBtn.setOnAction(e -> toggleTextMode());
        searchBtn.setOnAction(e -> showSearchDialog());
        autoScrollBtn.setOnAction(e -> toggleAutoScroll());
        nightModeBtn.setOnAction(e -> toggleNightMode());
        eyeCareBtn.setOnAction(e -> toggleEyeCareMode());
        focusModeBtn.setOnAction(e -> toggleFocusMode());
        speedReadBtn.setOnAction(e -> showSpeedReadingDialog());
        rotateBtn.setOnAction(e -> rotateImage());

        // 文字模式專用功能
        fontSizeIncBtn.setOnAction(e -> adjustFontSize(2));
        fontSizeDecBtn.setOnAction(e -> adjustFontSize(-2));
        lineSpacingBtn.setOnAction(e -> showLineSpacingDialog());

        // 導航功能
        firstPageBtn.setOnAction(e -> goToFirstPage());
        prevBtn.setOnAction(e -> goToPreviousPage());
        nextBtn.setOnAction(e -> goToNextPage());
        lastPageBtn.setOnAction(e -> goToLastPage());

        goToPageBtn.setOnAction(e -> {
            try {
                int pageNum = Integer.parseInt(pageField.getText());
                goToPage(pageNum - 1);
                pageField.clear();
            } catch (NumberFormatException ex) {
                AlertHelper.showError("錯誤", "請輸入有效的頁數");
            }
        });

        pageField.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ENTER) {
                goToPageBtn.fire();
            }
        });

        // 縮放功能（只在圖片模式有效）
        zoomInBtn.setOnAction(e -> {
            if (!isTextMode) {
                imageViewer.zoomIn();
            }
        });
        zoomOutBtn.setOnAction(e -> {
            if (!isTextMode) {
                imageViewer.zoomOut();
            }
        });
        fitWidthBtn.setOnAction(e -> {
            if (!isTextMode) {
                imageViewer.fitToWidth();
            }
        });
        fitHeightBtn.setOnAction(e -> {
            if (!isTextMode) {
                imageViewer.fitToHeight();
            }
        });
    }

    // 新增：切換文字模式
    private void toggleTextMode() {
        if (currentFilePath.isEmpty()) {
            AlertHelper.showError("提示", "請先開啟檔案");
            return;
        }

        isTextMode = !isTextMode;

        if (isTextMode) {
            // 切換到文字模式
            switchToTextMode();
        } else {
            // 切換回圖片模式
            switchToImageMode();
        }

        updateControlsForMode();
    }

    private void switchToTextMode() {
        try {
            // 顯示載入指示器
            showLoadingIndicator("正在提取文字內容...");

            // 在背景執行緒執行文字提取
            Thread extractThread = new Thread(() -> {
                try {
                    if (isPdfMode) {
                        // 從PDF提取文字
                        File pdfFile = new File(currentFilePath);
                        currentTextPages = textExtractor.extractTextFromPdf(pdfFile);
                    } else {
                        // 從圖片提取文字
                        currentTextPages = textExtractor.extractTextFromImages(currentImages);
                    }

                    // 在UI執行緒更新介面
                    Platform.runLater(() -> {
                        hideLoadingIndicator();

                        if (currentTextPages != null && !currentTextPages.isEmpty()) {
                            // 切換到文字渲染器
                            centerPane.getChildren().clear();
                            centerPane.getChildren().addAll(
                                    textRenderer.getScrollPane(),
                                    readingProgressBar,
                                    readingTimeLabel
                            );

                            // 設定文字頁面
                            textRenderer.setPages(currentTextPages);
                            textRenderer.setThemeColors(settingsPanel.getCurrentTheme());

                            showNotification("文字模式", "已成功提取 " + currentTextPages.size() + " 頁文字內容");
                        } else {
                            AlertHelper.showError("文字提取失敗", "無法從檔案中提取文字內容");
                            isTextMode = false;
                        }

                        updateReadingProgress();
                    });

                } catch (Exception e) {
                    Platform.runLater(() -> {
                        hideLoadingIndicator();
                        AlertHelper.showError("文字提取錯誤", e.getMessage());
                        isTextMode = false;
                    });
                }
            });

            extractThread.setDaemon(true);
            extractThread.start();

        } catch (Exception e) {
            hideLoadingIndicator();
            AlertHelper.showError("文字提取錯誤", e.getMessage());
            isTextMode = false;
        }
    }

    private void switchToImageMode() {
        // 切換回圖片檢視器
        centerPane.getChildren().clear();
        centerPane.getChildren().addAll(
                imageViewer.getScrollPane(),
                readingProgressBar,
                readingTimeLabel
        );
        updateReadingProgress();
    }

    private void updateControlsForMode() {
        // 更新頁面標籤
        Label pageLabel = (Label) bottomControls.getChildren().get(bottomControls.getChildren().size() - 1);
        if (isTextMode && currentTextPages != null) {
            pageLabel.setText("文字: " + (textRenderer.getCurrentPageIndex() + 1) + " / " + currentTextPages.size());
        } else if (!isTextMode && imageViewer.hasImages()) {
            pageLabel.setText("Page: " + (imageViewer.getCurrentIndex() + 1) + " / " + imageViewer.getTotalPages());
        }
    }

    // 新增：搜尋對話框
    private void showSearchDialog() {
        if (!isTextMode || currentTextPages == null || currentTextPages.isEmpty()) {
            AlertHelper.showError("提示", "請先切換到文字模式");
            return;
        }

        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("搜尋文字");
        dialog.setHeaderText("在文件中搜尋文字");
        dialog.setContentText("請輸入搜尋關鍵字:");

        dialog.showAndWait().ifPresent(searchTerm -> {
            if (!searchTerm.trim().isEmpty()) {
                textRenderer.searchText(searchTerm);
                showNotification("搜尋完成", "已高亮顯示搜尋結果");
            }
        });
    }

    // 新增：調整字體大小
    private void adjustFontSize(double delta) {
        if (!isTextMode) {
            return;
        }

        // 這裡需要實作字體大小調整邏輯
        // 可以在TextRenderer中添加setFontSize方法
        showNotification("字體調整", delta > 0 ? "字體已放大" : "字體已縮小");
    }

    // 新增：行距調整對話框
    private void showLineSpacingDialog() {
        if (!isTextMode) {
            return;
        }

        Dialog<Double> dialog = new Dialog<>();
        dialog.setTitle("行距設定");
        dialog.setHeaderText("調整文字行距");

        ButtonType okButtonType = new ButtonType("確定", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(okButtonType, ButtonType.CANCEL);

        Slider spacingSlider = new Slider(1.0, 3.0, 1.5);
        spacingSlider.setShowTickLabels(true);
        spacingSlider.setShowTickMarks(true);
        spacingSlider.setMajorTickUnit(0.5);

        Label spacingLabel = new Label("1.5");
        spacingSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            spacingLabel.setText(String.format("%.1f", newVal.doubleValue()));
        });

        VBox content = new VBox(10);
        content.getChildren().addAll(
                new Label("行距倍數:"), spacingSlider, spacingLabel
        );
        dialog.getDialogPane().setContent(content);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == okButtonType) {
                return spacingSlider.getValue();
            }
            return null;
        });

        dialog.showAndWait().ifPresent(spacing -> {
            textRenderer.setLineSpacing(spacing);
            showNotification("行距調整", "行距已設定為 " + String.format("%.1f", spacing));
        });
    }

    // 修改導航方法以支援兩種模式
    private void goToFirstPage() {
        if (isTextMode) {
            textRenderer.goToPage(0);
        } else {
            imageViewer.goToFirstPage();
        }
        updateReadingProgress();
    }

    private void goToPreviousPage() {
        if (isTextMode) {
            int currentIndex = textRenderer.getCurrentPageIndex();
            if (currentIndex > 0) {
                textRenderer.goToPage(currentIndex - 1);
            }
        } else {
            imageViewer.prevPage();
        }
        updateReadingProgress();
    }

    private void goToNextPage() {
        if (isTextMode) {
            int currentIndex = textRenderer.getCurrentPageIndex();
            if (currentIndex < textRenderer.getTotalPages() - 1) {
                textRenderer.goToPage(currentIndex + 1);
            }
        } else {
            imageViewer.nextPage();
        }
        updateReadingProgress();
    }

    private void goToLastPage() {
        if (isTextMode) {
            textRenderer.goToPage(textRenderer.getTotalPages() - 1);
        } else {
            imageViewer.goToLastPage();
        }
        updateReadingProgress();
    }

    private void goToPage(int pageIndex) {
        if (isTextMode) {
            textRenderer.goToPage(pageIndex);
        } else {
            imageViewer.goToPage(pageIndex);
        }
        updateReadingProgress();
    }

    // 新增：載入指示器
    private ProgressIndicator loadingIndicator;
    private Label loadingLabel;
    private VBox loadingBox;

    private void showLoadingIndicator(String message) {
        if (loadingBox != null) {
            return; // 已經在顯示
        }

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

    private void setupEventHandlers(BorderPane root) {
        // 滑鼠點擊翻頁（只在圖片模式有效）
        imageViewer.getImageView().setOnMouseClicked(this::handleImageClick);

        // 滑鼠滾輪縮放/翻頁
        if (isTextMode) {
            textRenderer.getScrollPane().setOnScroll(e -> {
                if (e.getDeltaY() < 0) {
                    goToNextPage();
                } else if (e.getDeltaY() > 0) {
                    goToPreviousPage();
                }
            });
        } else {
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
                        imageViewer.nextPage();
                        updateReadingProgress();
                    } else if (e.getDeltaY() > 0) {
                        imageViewer.prevPage();
                        updateReadingProgress();
                    }
                }
            });
        }

        // 雙擊全螢幕
        imageViewer.getImageView().setOnMouseClicked(e -> {
            if (e.getClickCount() == 2) {
                toggleFullscreen();
            }
        });
    }

    private void handleImageClick(MouseEvent event) {
        if (isTextMode) return; // 文字模式不處理圖片點擊

        double x = event.getX();
        double imageWidth = imageViewer.getImageView().getBoundsInLocal().getWidth();

        // 點擊右側翻下頁，左側翻上頁
        if (x > imageWidth * 0.7) {
            imageViewer.nextPage();
            updateReadingProgress();
        } else if (x < imageWidth * 0.3) {
            imageViewer.prevPage();
            updateReadingProgress();
        } else {
            // 中間區域切換控制列顯示
            toggleControlsVisibility();
        }
    }

    private void setupKeyboardShortcuts(BorderPane root) {
        root.setOnKeyPressed(e -> {
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
                    if (isFullScreen) {
                        toggleFullscreen();
                    }
                    break;
                case PLUS:
                case EQUALS:
                    if (e.isControlDown()) {
                        if (isTextMode) {
                            adjustFontSize(2);
                        } else {
                            imageViewer.zoomIn();
                        }
                    }
                    break;
                case MINUS:
                    if (e.isControlDown()) {
                        if (isTextMode) {
                            adjustFontSize(-2);
                        } else {
                            imageViewer.zoomOut();
                        }
                    }
                    break;
                case DIGIT0:
                    if (e.isControlDown() && !isTextMode) {
                        imageViewer.resetZoom();
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
                case E:
                    if (e.isControlDown()) {
                        toggleEyeCareMode();
                    }
                    break;
                case F:
                    if (e.isControlDown()) {
                        toggleFocusMode();
                    }
                    break;
                case A:
                    if (e.isControlDown()) {
                        toggleAutoScroll();
                    }
                    break;
                case R:
                    if (e.isControlDown() && !isTextMode) {
                        rotateImage();
                    }
                    break;
                case T:
                    if (e.isControlDown()) {
                        toggleTextMode();
                    }
                    break;
                case SLASH:
                    if (e.isControlDown()) {
                        showSearchDialog();
                    }
                    break;
            }
        });

        root.setFocusTraversable(true);
        root.requestFocus();
    }

    // 原有方法保持不變，但需要修改以支援文字模式
    private void openImageFolder() {
        DirectoryChooser dc = new DirectoryChooser();
        dc.setTitle("選擇圖片資料夾");
        File folder = dc.showDialog(primaryStage);
        if (folder != null) {
            var images = imageLoader.loadImagesFromFolder(folder);
            if (!images.isEmpty()) {
                isPdfMode = false;
                isTextMode = false;
                currentFilePath = folder.getAbsolutePath();
                currentImages = images;
                currentTextPages = null;

                switchToImageMode();
                imageViewer.setImages(images);
                primaryStage.setTitle("E_Reader - " + folder.getName());
                updateReadingProgress();
                updateControlsForMode();
            } else {
                AlertHelper.showError("載入失敗", "資料夾中沒有找到支援的圖片格式");
            }
        }
    }

    private void openPdfFile() {
        FileChooser fc = new FileChooser();
        fc.setTitle("選擇 PDF 檔案");
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF Files", "*.pdf"));
        File pdfFile = fc.showOpenDialog(primaryStage);
        if (pdfFile != null) {
            try {
                var images = pdfLoader.loadImagesFromPdf(pdfFile);
                if (!images.isEmpty()) {
                    isPdfMode = true;
                    isTextMode = false;
                    currentFilePath = pdfFile.getAbsolutePath();
                    currentImages = images;
                    currentTextPages = null;

                    switchToImageMode();
                    imageViewer.setImages(images);
                    primaryStage.setTitle("E_Reader - " + pdfFile.getName());
                    updateReadingProgress();
                    updateControlsForMode();
                }
            } catch (Exception ex) {
                AlertHelper.showError("無法載入 PDF 檔案", ex.getMessage());
            }
        }
    }

    // 修改書籤對話框以支援文字模式
    private void showBookmarkDialog() {
        if (currentFilePath.isEmpty()) {
            AlertHelper.showError("提示", "請先開啟檔案");
            return;
        }

        int currentPageIndex;
        if (isTextMode) {
            currentPageIndex = textRenderer.getCurrentPageIndex();
        } else {
            currentPageIndex = imageViewer.getCurrentIndex();
        }

        bookmarkManager.showBookmarkDialog(primaryStage, currentFilePath,
                currentPageIndex,
                bookmark -> {
                    // 跳轉到書籤
                    goToPage(bookmark.getPageNumber());
                });
    }

    private void toggleAutoScroll() {
        isAutoScrolling = !isAutoScrolling;
        if (isAutoScrolling) {
            startAutoScroll();
        } else {
            stopAutoScroll();
        }
    }

    private void startAutoScroll() {
        if (autoScrollTimer != null) {
            autoScrollTimer.cancel();
        }

        autoScrollTimer = new Timer();
        autoScrollTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                Platform.runLater(() -> {
                    boolean canGoNext;
                    if (isTextMode) {
                        canGoNext = textRenderer.getCurrentPageIndex() < textRenderer.getTotalPages() - 1;
                    } else {
                        canGoNext = imageViewer.canGoNext();
                    }

                    if (canGoNext) {
                        goToNextPage();
                    } else {
                        stopAutoScroll();
                    }
                });
            }
        }, 3000, 3000); // 每3秒翻頁
    }

    private void stopAutoScroll() {
        isAutoScrolling = false;
        if (autoScrollTimer != null) {
            autoScrollTimer.cancel();
            autoScrollTimer = null;
        }
    }

    private void toggleNightMode() {
        settingsPanel.setThemeMode(settingsPanel.getThemeMode() == SettingsPanel.ThemeMode.BLACK ?
                SettingsPanel.ThemeMode.DARK : SettingsPanel.ThemeMode.BLACK);
        applySettings();
    }

    private void toggleEyeCareMode() {
        settingsPanel.setEyeCareMode(!settingsPanel.isEyeCareMode());
        applySettings();

        if (settingsPanel.isEyeCareMode()) {
            showNotification("護眼模式已啟用", "建議每30分鐘休息5-10分鐘");
        }
    }

    private void toggleFocusMode() {
        // 專注模式：隱藏所有控制元件，只顯示內容
        boolean focusMode = !controlsContainer.isVisible();
        controlsContainer.setVisible(!focusMode);
        controlsContainer.setManaged(!focusMode);
        readingTimeLabel.setVisible(!focusMode);
        readingProgressBar.setVisible(!focusMode);

        if (focusMode) {
            showNotification("專注模式", "按 F 鍵或點擊中央退出專注模式");
        }
    }

    private void rotateImage() {
        if (!isTextMode) {
            imageViewer.getImageView().setRotate(imageViewer.getImageView().getRotate() + 90);
        }
    }

    private void showSpeedReadingDialog() {
        Dialog<Integer> dialog = new Dialog<>();
        dialog.setTitle("快速閱讀設定");
        dialog.setHeaderText("設定自動翻頁間隔時間");

        ButtonType okButtonType = new ButtonType("確定", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(okButtonType, ButtonType.CANCEL);

        Slider speedSlider = new Slider(1, 10, 3);
        speedSlider.setShowTickLabels(true);
        speedSlider.setShowTickMarks(true);
        speedSlider.setMajorTickUnit(1);

        Label speedLabel = new Label("3 秒");
        speedSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            speedLabel.setText(newVal.intValue() + " 秒");
        });

        VBox content = new VBox(10);
        content.getChildren().addAll(
                new Label("翻頁間隔:"), speedSlider, speedLabel
        );
        dialog.getDialogPane().setContent(content);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == okButtonType) {
                return (int) speedSlider.getValue();
            }
            return null;
        });

        dialog.showAndWait().ifPresent(speed -> {
            // 設定自動翻頁速度
            if (isAutoScrolling) {
                stopAutoScroll();
            }
            // 使用新的速度重新開始
            startAutoScrollWithSpeed(speed * 1000);
        });
    }

    private void startAutoScrollWithSpeed(int milliseconds) {
        if (autoScrollTimer != null) {
            autoScrollTimer.cancel();
        }

        autoScrollTimer = new Timer();
        autoScrollTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                Platform.runLater(() -> {
                    boolean canGoNext;
                    if (isTextMode) {
                        canGoNext = textRenderer.getCurrentPageIndex() < textRenderer.getTotalPages() - 1;
                    } else {
                        canGoNext = imageViewer.canGoNext();
                    }

                    if (canGoNext) {
                        goToNextPage();
                    } else {
                        stopAutoScroll();
                    }
                });
            }
        }, milliseconds, milliseconds);

        isAutoScrolling = true;
    }

    // 計時器相關
    private void startReadingTimer() {
        readingStartTime = System.currentTimeMillis();
        readingTimer = new Timer();
        readingTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                Platform.runLater(() -> updateReadingTime());
            }
        }, 1000, 1000);
    }

    private void startEyeCareReminder() {
        if (!settingsPanel.isEyeCareMode()) return;

        eyeCareReminderTimer = new Timer();
        eyeCareReminderTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                Platform.runLater(() -> {
                    showNotification("護眼提醒", "您已經閱讀30分鐘了，建議休息5-10分鐘！");
                });
            }
        }, 30 * 60 * 1000, 30 * 60 * 1000); // 每30分鐘提醒
    }

    private void updateReadingTime() {
        long currentTime = System.currentTimeMillis();
        long sessionTime = currentTime - readingStartTime;
        long totalTime = totalReadingTime + sessionTime;

        long hours = totalTime / (1000 * 60 * 60);
        long minutes = (totalTime % (1000 * 60 * 60)) / (1000 * 60);
        long seconds = ((totalTime % (1000 * 60 * 60)) % (1000 * 60)) / 1000;

        readingTimeLabel.setText(String.format("閱讀時間: %02d:%02d:%02d", hours, minutes, seconds));
    }

    private void updateReadingProgress() {
        double progress = 0;

        if (isTextMode && currentTextPages != null && !currentTextPages.isEmpty()) {
            progress = (double) (textRenderer.getCurrentPageIndex() + 1) / currentTextPages.size();
        } else if (!isTextMode && imageViewer.hasImages()) {
            progress = (double) (imageViewer.getCurrentIndex() + 1) / imageViewer.getTotalPages();
        }

        readingProgressBar.setProgress(progress);
        updateControlsForMode();
    }

    private void showNotification(String title, String message) {
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

    private void saveLastReadingPosition() {
        // 儲存最後閱讀位置的邏輯
        // 可以使用 Properties 或 JSON 格式儲存
    }

    private void stopAllTimers() {
        if (readingTimer != null) {
            readingTimer.cancel();
        }
        if (eyeCareReminderTimer != null) {
            eyeCareReminderTimer.cancel();
        }
        if (autoScrollTimer != null) {
            autoScrollTimer.cancel();
        }
    }

    private void showSettingsDialog() {
        settingsPanel.showSettingsDialog(primaryStage, this::applySettings);
    }

    private void toggleFullscreen() {
        isFullScreen = !isFullScreen;
        primaryStage.setFullScreen(isFullScreen);

        if (isFullScreen) {
            controlsContainer.setVisible(false);
            controlsContainer.setManaged(false);
        } else {
            controlsContainer.setVisible(isControlsVisible);
            controlsContainer.setManaged(isControlsVisible);
        }
    }

    private void toggleControlsVisibility() {
        if (!isFullScreen) {
            isControlsVisible = !isControlsVisible;
            controlsContainer.setVisible(isControlsVisible);
            controlsContainer.setManaged(isControlsVisible);
        }
    }

    private void applySettings() {
        // 從設定面板套用設定到閱讀器
        SettingsPanel.ThemeMode currentTheme = settingsPanel.getCurrentTheme();

        // 套用主題
        String backgroundColor = currentTheme.getBackgroundColor();
        String textColor = currentTheme.getTextColor();

        // 更新UI元素的顏色
        imageViewer.getScrollPane().setStyle("-fx-background: " + backgroundColor + "; -fx-background-color: " + backgroundColor + ";");

        // 如果在文字模式，也更新文字渲染器的主題
        if (isTextMode) {
            textRenderer.setThemeColors(currentTheme);
        }

        // 套用其他設定
        imageViewer.setFitMode(settingsPanel.getFitMode());

        // 更新護眼提醒
        if (settingsPanel.isEyeCareMode() && eyeCareReminderTimer == null) {
            startEyeCareReminder();
        } else if (!settingsPanel.isEyeCareMode() && eyeCareReminderTimer != null) {
            eyeCareReminderTimer.cancel();
            eyeCareReminderTimer = null;
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}