package E_Reader;

import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
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

public class Main extends Application {

    private ImageViewer viewer = new ImageViewer();
    private ImageLoader imageLoader = new ImageLoader();
    private PdfLoader pdfLoader = new PdfLoader();
    private BookmarkManager bookmarkManager = new BookmarkManager();
    private boolean isPdfMode = false;
    private String currentFilePath = "";
    private Stage primaryStage;
    private boolean isFullScreen = false;
    private VBox controlsContainer;
    private HBox topControls;
    private HBox bottomControls;

    // 新增設定面板相關
    private SettingsPanel settingsPanel = new SettingsPanel();
    private boolean isControlsVisible = true;

    // 新功能相關
    private Timer readingTimer;
    private long readingStartTime;
    private long totalReadingTime = 0;
    private Label readingTimeLabel;
    private Timer eyeCareReminderTimer;
    private boolean isAutoScrolling = false;
    private Timer autoScrollTimer;
    private ProgressBar readingProgressBar;

    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;
        primaryStage.setTitle("E_Reader 漫畫＆PDF閱讀器 v3.0 Enhanced");

        // 建立主版面
        BorderPane root = new BorderPane();

        // 設定背景顏色
        root.setStyle("-fx-background-color: #2b2b2b;");

        // 中央圖片顯示區域
        StackPane centerPane = createCenterPane();
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
                // 保存最後閱讀位置
                saveLastReadingPosition();
            }
        });
    }

    private StackPane createCenterPane() {
        StackPane centerPane = new StackPane();

        // 圖片顯示區域
        centerPane.getChildren().add(viewer.getScrollPane());

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

        // 設定按鈕樣式
        String buttonStyle = "-fx-background-color: #404040; -fx-text-fill: white; " +
                "-fx-border-radius: 5; -fx-background-radius: 5; " +
                "-fx-padding: 8 12 8 12; -fx-font-size: 12px;";

        Button[] topButtons = {openFolderBtn, openPdfBtn, bookmarkBtn, settingsBtn,
                autoScrollBtn, nightModeBtn, eyeCareBtn, fullscreenBtn, exitBtn};
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

        Label pageLabel = viewer.getPageLabel();
        pageLabel.setStyle("-fx-text-fill: white; -fx-font-size: 14px;");

        // 設定下方按鈕樣式
        Button[] bottomButtons = {firstPageBtn, prevBtn, nextBtn, lastPageBtn,
                goToPageBtn, zoomInBtn, zoomOutBtn, fitWidthBtn, fitHeightBtn,
                rotateBtn, focusModeBtn, speedReadBtn};
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
                new Separator(), focusModeBtn, speedReadBtn,
                new Separator(), pageLabel);

        // 設定事件處理器
        setupButtonHandlers(openFolderBtn, openPdfBtn, bookmarkBtn, settingsBtn,
                fullscreenBtn, exitBtn, autoScrollBtn, nightModeBtn, eyeCareBtn,
                firstPageBtn, prevBtn, nextBtn, lastPageBtn, goToPageBtn,
                zoomInBtn, zoomOutBtn, fitWidthBtn, fitHeightBtn,
                rotateBtn, focusModeBtn, speedReadBtn, pageField);
    }

    private void setupButtonHandlers(Button openFolderBtn, Button openPdfBtn, Button bookmarkBtn,
                                     Button settingsBtn, Button fullscreenBtn, Button exitBtn,
                                     Button autoScrollBtn, Button nightModeBtn, Button eyeCareBtn,
                                     Button firstPageBtn, Button prevBtn, Button nextBtn,
                                     Button lastPageBtn, Button goToPageBtn, Button zoomInBtn,
                                     Button zoomOutBtn, Button fitWidthBtn, Button fitHeightBtn,
                                     Button rotateBtn, Button focusModeBtn, Button speedReadBtn,
                                     TextField pageField) {

        // 原有功能
        openFolderBtn.setOnAction(e -> openImageFolder());
        openPdfBtn.setOnAction(e -> openPdfFile());
        settingsBtn.setOnAction(e -> showSettingsDialog());
        fullscreenBtn.setOnAction(e -> toggleFullscreen());
        exitBtn.setOnAction(e -> primaryStage.close());

        // 書籤功能
        bookmarkBtn.setOnAction(e -> showBookmarkDialog());

        // 新功能
        autoScrollBtn.setOnAction(e -> toggleAutoScroll());
        nightModeBtn.setOnAction(e -> toggleNightMode());
        eyeCareBtn.setOnAction(e -> toggleEyeCareMode());
        focusModeBtn.setOnAction(e -> toggleFocusMode());
        speedReadBtn.setOnAction(e -> showSpeedReadingDialog());
        rotateBtn.setOnAction(e -> rotateImage());

        // 導航功能
        firstPageBtn.setOnAction(e -> {
            viewer.goToFirstPage();
            updateReadingProgress();
        });
        prevBtn.setOnAction(e -> {
            viewer.prevPage();
            updateReadingProgress();
        });
        nextBtn.setOnAction(e -> {
            viewer.nextPage();
            updateReadingProgress();
        });
        lastPageBtn.setOnAction(e -> {
            viewer.goToLastPage();
            updateReadingProgress();
        });

        goToPageBtn.setOnAction(e -> {
            try {
                int pageNum = Integer.parseInt(pageField.getText());
                viewer.goToPage(pageNum - 1);
                pageField.clear();
                updateReadingProgress();
            } catch (NumberFormatException ex) {
                AlertHelper.showError("錯誤", "請輸入有效的頁數");
            }
        });

        pageField.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ENTER) {
                goToPageBtn.fire();
            }
        });

        zoomInBtn.setOnAction(e -> viewer.zoomIn());
        zoomOutBtn.setOnAction(e -> viewer.zoomOut());
        fitWidthBtn.setOnAction(e -> viewer.fitToWidth());
        fitHeightBtn.setOnAction(e -> viewer.fitToHeight());
    }

    private void setupEventHandlers(BorderPane root) {
        // 滑鼠點擊翻頁（平板友善）
        viewer.getImageView().setOnMouseClicked(this::handleImageClick);

        // 滑鼠滾輪縮放
        viewer.getScrollPane().setOnScroll(e -> {
            if (e.isControlDown()) {
                if (e.getDeltaY() > 0) {
                    viewer.zoomIn();
                } else {
                    viewer.zoomOut();
                }
                e.consume();
            } else {
                // 滾輪翻頁
                if (e.getDeltaY() < 0) {
                    viewer.nextPage();
                    updateReadingProgress();
                } else if (e.getDeltaY() > 0) {
                    viewer.prevPage();
                    updateReadingProgress();
                }
            }
        });

        // 雙擊全螢幕
        viewer.getImageView().setOnMouseClicked(e -> {
            if (e.getClickCount() == 2) {
                toggleFullscreen();
            }
        });
    }

    private void handleImageClick(MouseEvent event) {
        double x = event.getX();
        double imageWidth = viewer.getImageView().getBoundsInLocal().getWidth();

        // 點擊右側翻下頁，左側翻上頁
        if (x > imageWidth * 0.7) {
            viewer.nextPage();
            updateReadingProgress();
        } else if (x < imageWidth * 0.3) {
            viewer.prevPage();
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
                    viewer.prevPage();
                    updateReadingProgress();
                    break;
                case RIGHT:
                case PAGE_DOWN:
                case SPACE:
                    viewer.nextPage();
                    updateReadingProgress();
                    break;
                case HOME:
                    viewer.goToFirstPage();
                    updateReadingProgress();
                    break;
                case END:
                    viewer.goToLastPage();
                    updateReadingProgress();
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
                        viewer.zoomIn();
                    }
                    break;
                case MINUS:
                    if (e.isControlDown()) {
                        viewer.zoomOut();
                    }
                    break;
                case DIGIT0:
                    if (e.isControlDown()) {
                        viewer.resetZoom();
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
                    if (e.isControlDown()) {
                        rotateImage();
                    }
                    break;
            }
        });

        root.setFocusTraversable(true);
        root.requestFocus();
    }

    // 新功能實作
    private void showBookmarkDialog() {
        if (currentFilePath.isEmpty()) {
            AlertHelper.showError("提示", "請先開啟檔案");
            return;
        }

        bookmarkManager.showBookmarkDialog(primaryStage, currentFilePath,
                viewer.getCurrentIndex(),
                bookmark -> {
                    // 跳轉到書籤
                    viewer.goToPage(bookmark.getPageNumber());
                    updateReadingProgress();
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
                    if (viewer.canGoNext()) {
                        viewer.nextPage();
                        updateReadingProgress();
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
        viewer.getImageView().setRotate(viewer.getImageView().getRotate() + 90);
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
                    if (viewer.canGoNext()) {
                        viewer.nextPage();
                        updateReadingProgress();
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
        if (viewer.hasImages()) {
            double progress = (double) (viewer.getCurrentIndex() + 1) / viewer.getTotalPages();
            readingProgressBar.setProgress(progress);
        }
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

    // 原有方法保持不變
    private void openImageFolder() {
        DirectoryChooser dc = new DirectoryChooser();
        dc.setTitle("選擇圖片資料夾");
        File folder = dc.showDialog(primaryStage);
        if (folder != null) {
            var images = imageLoader.loadImagesFromFolder(folder);
            if (!images.isEmpty()) {
                isPdfMode = false;
                currentFilePath = folder.getAbsolutePath();
                viewer.setImages(images);
                primaryStage.setTitle("E_Reader - " + folder.getName());
                updateReadingProgress();
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
                    currentFilePath = pdfFile.getAbsolutePath();
                    viewer.setImages(images);
                    primaryStage.setTitle("E_Reader - " + pdfFile.getName());
                    updateReadingProgress();
                }
            } catch (Exception ex) {
                AlertHelper.showError("無法載入 PDF 檔案", ex.getMessage());
            }
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
        viewer.getScrollPane().setStyle("-fx-background: " + backgroundColor + "; -fx-background-color: " + backgroundColor + ";");

        // 套用其他設定
        viewer.setFitMode(settingsPanel.getFitMode());

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