package E_Reader;

import java.io.File;

import javafx.application.Application;
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
    private boolean isPdfMode = false;
    private Stage primaryStage;
    private boolean isFullScreen = false;
    private VBox controlsContainer;
    private HBox topControls;
    private HBox bottomControls;

    // 新增設定面板相關
    private SettingsPanel settingsPanel = new SettingsPanel();
    private boolean isControlsVisible = true;

    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;
        primaryStage.setTitle("E_Reader 漫畫＆PDF閱讀器 v2.0");

        // 建立主版面
        BorderPane root = new BorderPane();

        // 設定背景顏色
        root.setStyle("-fx-background-color: #2b2b2b;");

        // 中央圖片顯示區域
        StackPane centerPane = new StackPane();
        centerPane.getChildren().add(viewer.getImageView());
        centerPane.setStyle("-fx-background-color: #1e1e1e;");
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
    }

    private void createControlPanels() {
        // 上方控制列
        Button openFolderBtn = new Button("📂 圖片資料夾");
        Button openPdfBtn = new Button("📄 PDF檔案");
        Button settingsBtn = new Button("⚙️ 設定");
        Button fullscreenBtn = new Button("🔲 全螢幕");
        Button exitBtn = new Button("❌ 離開");

        // 設定按鈕樣式
        String buttonStyle = "-fx-background-color: #404040; -fx-text-fill: white; " +
                "-fx-border-radius: 5; -fx-background-radius: 5; " +
                "-fx-padding: 8 12 8 12; -fx-font-size: 12px;";

        openFolderBtn.setStyle(buttonStyle);
        openPdfBtn.setStyle(buttonStyle);
        settingsBtn.setStyle(buttonStyle);
        fullscreenBtn.setStyle(buttonStyle);
        exitBtn.setStyle(buttonStyle);

        topControls = new HBox(10);
        topControls.setAlignment(Pos.CENTER);
        topControls.setPadding(new Insets(10));
        topControls.setStyle("-fx-background-color: #333333;");
        topControls.getChildren().addAll(openFolderBtn, openPdfBtn, settingsBtn,
                fullscreenBtn, exitBtn);

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

        Label pageLabel = viewer.getPageLabel();
        pageLabel.setStyle("-fx-text-fill: white; -fx-font-size: 14px;");

        // 設定下方按鈕樣式
        Button[] bottomButtons = {firstPageBtn, prevBtn, nextBtn, lastPageBtn,
                goToPageBtn, zoomInBtn, zoomOutBtn, fitWidthBtn, fitHeightBtn};
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
                fitWidthBtn, fitHeightBtn,
                new Separator(), pageLabel);

        // 設定事件處理器
        setupButtonHandlers(openFolderBtn, openPdfBtn, settingsBtn, fullscreenBtn, exitBtn,
                firstPageBtn, prevBtn, nextBtn, lastPageBtn, goToPageBtn,
                zoomInBtn, zoomOutBtn, fitWidthBtn, fitHeightBtn, pageField);
    }

    private void setupButtonHandlers(Button openFolderBtn, Button openPdfBtn, Button settingsBtn,
                                     Button fullscreenBtn, Button exitBtn, Button firstPageBtn,
                                     Button prevBtn, Button nextBtn, Button lastPageBtn,
                                     Button goToPageBtn, Button zoomInBtn, Button zoomOutBtn,
                                     Button fitWidthBtn, Button fitHeightBtn, TextField pageField) {

        openFolderBtn.setOnAction(e -> openImageFolder());
        openPdfBtn.setOnAction(e -> openPdfFile());
        settingsBtn.setOnAction(e -> showSettingsDialog());
        fullscreenBtn.setOnAction(e -> toggleFullscreen());
        exitBtn.setOnAction(e -> primaryStage.close());

        firstPageBtn.setOnAction(e -> viewer.goToFirstPage());
        prevBtn.setOnAction(e -> viewer.prevPage());
        nextBtn.setOnAction(e -> viewer.nextPage());
        lastPageBtn.setOnAction(e -> viewer.goToLastPage());

        goToPageBtn.setOnAction(e -> {
            try {
                int pageNum = Integer.parseInt(pageField.getText());
                viewer.goToPage(pageNum - 1); // 轉換為0基索引
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

        zoomInBtn.setOnAction(e -> viewer.zoomIn());
        zoomOutBtn.setOnAction(e -> viewer.zoomOut());
        fitWidthBtn.setOnAction(e -> viewer.fitToWidth());
        fitHeightBtn.setOnAction(e -> viewer.fitToHeight());
    }

    private void setupEventHandlers(BorderPane root) {
        // 滑鼠點擊翻頁（平板友善）
        viewer.getImageView().setOnMouseClicked(this::handleImageClick);

        // 滑鼠滾輪縮放
        viewer.getImageView().setOnScroll(e -> {
            if (e.isControlDown()) {
                if (e.getDeltaY() > 0) {
                    viewer.zoomIn();
                } else {
                    viewer.zoomOut();
                }
                e.consume();
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
        } else if (x < imageWidth * 0.3) {
            viewer.prevPage();
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
                    break;
                case RIGHT:
                case PAGE_DOWN:
                case SPACE:
                    viewer.nextPage();
                    break;
                case HOME:
                    viewer.goToFirstPage();
                    break;
                case END:
                    viewer.goToLastPage();
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
            }
        });

        root.setFocusTraversable(true);
        root.requestFocus();
    }

    private void openImageFolder() {
        DirectoryChooser dc = new DirectoryChooser();
        dc.setTitle("選擇圖片資料夾");
        File folder = dc.showDialog(primaryStage);
        if (folder != null) {
            var images = imageLoader.loadImagesFromFolder(folder);
            if (!images.isEmpty()) {
                isPdfMode = false;
                viewer.setImages(images);
                primaryStage.setTitle("E_Reader - " + folder.getName());
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
                    viewer.setImages(images);
                    primaryStage.setTitle("E_Reader - " + pdfFile.getName());
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
        viewer.setFitMode(settingsPanel.getFitMode());
        // 可以加入更多設定項目
    }

    public static void main(String[] args) {
        launch(args);
    }
}