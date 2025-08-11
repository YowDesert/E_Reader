package E_Reader.ui.components;

import javafx.animation.*;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.effect.DropShadow;
import javafx.scene.effect.GaussianBlur;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.scene.shape.Rectangle;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;

/**
 * 現代化的讀取進度條組件
 * 支援不同的顯示模式和動畫效果
 */
public class LoadingProgressBar {
    
    public enum LoadingType {
        STARTUP("啟動中", "🚀", Color.web("#3498db")),
        FILE_OPENING("開啟檔案", "📂", Color.web("#2ecc71")),
        TEXT_EXTRACTING("提取文字", "📝", Color.web("#f39c12")),
        IMAGE_LOADING("載入圖片", "🖼️", Color.web("#9b59b6")),
        PDF_PROCESSING("處理PDF", "📄", Color.web("#e74c3c")),
        EPUB_PROCESSING("處理EPUB", "📚", Color.web("#1abc9c")),
        SAVING("儲存中", "💾", Color.web("#34495e")),
        CUSTOM("處理中", "⚙️", Color.web("#7f8c8d"));
        
        private final String defaultMessage;
        private final String icon;
        private final Color themeColor;
        
        LoadingType(String defaultMessage, String icon, Color themeColor) {
            this.defaultMessage = defaultMessage;
            this.icon = icon;
            this.themeColor = themeColor;
        }
        
        public String getDefaultMessage() { return defaultMessage; }
        public String getIcon() { return icon; }
        public Color getThemeColor() { return themeColor; }
    }
    
    private Stage loadingStage;
    private ProgressBar progressBar;
    private ProgressIndicator progressIndicator;
    private Label messageLabel;
    private Label percentageLabel;
    private VBox containerBox;
    private Timeline pulseAnimation;
    private Rectangle backgroundOverlay;
    
    private LoadingType currentType = LoadingType.CUSTOM;
    private boolean isIndeterminate = true;
    private double currentProgress = 0.0;
    
    // 動畫相關
    private Timeline progressAnimation;
    private ScaleTransition scaleAnimation;
    private RotateTransition rotateAnimation;
    
    /**
     * 建構函數
     */
    public LoadingProgressBar() {
        initializeComponents();
    }
    
    /**
     * 建構函數 - 指定類型
     */
    public LoadingProgressBar(LoadingType type) {
        this.currentType = type;
        initializeComponents();
    }
    
    /**
     * 初始化組件
     */
    private void initializeComponents() {
        createLoadingStage();
        setupUI();
        setupAnimations();
    }
    
    /**
     * 創建載入視窗
     */
    private void createLoadingStage() {
        loadingStage = new Stage();
        loadingStage.initStyle(StageStyle.TRANSPARENT);
        loadingStage.initModality(Modality.NONE);
        loadingStage.setResizable(false);
        loadingStage.setAlwaysOnTop(true);
        
        // 設定視窗標題
        loadingStage.setTitle("E-Reader 載入中...");
    }
    
    /**
     * 設定UI界面
     */
    private void setupUI() {
        // 主容器
        StackPane root = new StackPane();
        root.setPrefSize(400, 220);
        
        // 背景毛玻璃效果
        backgroundOverlay = new Rectangle(400, 220);
        backgroundOverlay.setFill(Color.web("#000000", 0.4));
        backgroundOverlay.setEffect(new GaussianBlur(10));
        
        // 主要內容容器
        containerBox = new VBox(20);
        containerBox.setAlignment(Pos.CENTER);
        containerBox.setPadding(new Insets(30));
        containerBox.setMaxWidth(350);
        
        // iOS風格背景
        containerBox.setStyle(
            "-fx-background-color: linear-gradient(to bottom, " +
            "rgba(255,255,255,0.95) 0%, " +
            "rgba(248,248,248,0.95) 100%); " +
            "-fx-background-radius: 20; " +
            "-fx-border-color: rgba(255,255,255,0.8); " +
            "-fx-border-width: 1; " +
            "-fx-border-radius: 20; " +
            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 25, 0, 0, 10);"
        );
        
        // 圖標標籤
        Label iconLabel = new Label(currentType.getIcon());
        iconLabel.setStyle(
            "-fx-font-size: 36px; " +
            "-fx-text-fill: " + toHexString(currentType.getThemeColor()) + ";"
        );
        
        // 主要訊息標籤
        messageLabel = new Label(currentType.getDefaultMessage());
        messageLabel.setStyle(
            "-fx-text-fill: #2c3e50; " +
            "-fx-font-size: 18px; " +
            "-fx-font-weight: bold; " +
            "-fx-text-alignment: center;"
        );
        
        // 進度條容器
        VBox progressContainer = new VBox(15);
        progressContainer.setAlignment(Pos.CENTER);
        
        // 進度條
        progressBar = new ProgressBar();
        progressBar.setPrefWidth(280);
        progressBar.setPrefHeight(8);
        progressBar.setProgress(ProgressBar.INDETERMINATE_PROGRESS);
        
        // 動態進度條樣式
        String progressBarStyle = String.format(
            "-fx-accent: linear-gradient(to right, %s, %s); " +
            "-fx-background-color: rgba(220,220,220,0.8); " +
            "-fx-background-radius: 4; " +
            "-fx-background-insets: 0;",
            toHexString(currentType.getThemeColor()),
            toHexString(currentType.getThemeColor().deriveColor(0, 1.0, 1.2, 1.0))
        );
        progressBar.setStyle(progressBarStyle);
        
        // 百分比標籤
        percentageLabel = new Label("載入中...");
        percentageLabel.setStyle(
            "-fx-text-fill: #7f8c8d; " +
            "-fx-font-size: 14px; " +
            "-fx-font-weight: 500;"
        );
        
        // 旋轉載入指示器（用於不確定進度）
        progressIndicator = new ProgressIndicator();
        progressIndicator.setPrefSize(40, 40);
        progressIndicator.setVisible(true);
        progressIndicator.setStyle(
            "-fx-accent: " + toHexString(currentType.getThemeColor()) + ";"
        );
        
        // 組裝進度容器
        progressContainer.getChildren().addAll(progressBar, percentageLabel);
        
        // 組裝主容器
        containerBox.getChildren().addAll(
            iconLabel, messageLabel, progressIndicator, progressContainer
        );
        
        // 組裝根容器
        root.getChildren().addAll(backgroundOverlay, containerBox);
        
        // 創建場景
        Scene scene = new Scene(root);
        scene.setFill(Color.TRANSPARENT);
        loadingStage.setScene(scene);
        
        // 設定DropShadow效果
        DropShadow dropShadow = new DropShadow();
        dropShadow.setColor(Color.web("#000000", 0.3));
        dropShadow.setRadius(25);
        dropShadow.setOffsetY(10);
        containerBox.setEffect(dropShadow);
    }
    
    /**
     * 設定動畫
     */
    private void setupAnimations() {
        // 脈衝動畫（用於不確定進度）
        pulseAnimation = new Timeline(
            new KeyFrame(Duration.ZERO, new KeyValue(progressIndicator.scaleXProperty(), 1.0)),
            new KeyFrame(Duration.ZERO, new KeyValue(progressIndicator.scaleYProperty(), 1.0)),
            new KeyFrame(Duration.millis(500), new KeyValue(progressIndicator.scaleXProperty(), 1.1)),
            new KeyFrame(Duration.millis(500), new KeyValue(progressIndicator.scaleYProperty(), 1.1)),
            new KeyFrame(Duration.millis(1000), new KeyValue(progressIndicator.scaleXProperty(), 1.0)),
            new KeyFrame(Duration.millis(1000), new KeyValue(progressIndicator.scaleYProperty(), 1.0))
        );
        pulseAnimation.setCycleCount(Timeline.INDEFINITE);
        
        // 容器縮放動畫
        scaleAnimation = new ScaleTransition(Duration.millis(300), containerBox);
        scaleAnimation.setFromX(0.8);
        scaleAnimation.setFromY(0.8);
        scaleAnimation.setToX(1.0);
        scaleAnimation.setToY(1.0);
        scaleAnimation.setInterpolator(Interpolator.EASE_OUT);
        
        // 旋轉動畫（用於圖標）
        rotateAnimation = new RotateTransition(Duration.millis(2000), progressIndicator);
        rotateAnimation.setByAngle(360);
        rotateAnimation.setCycleCount(Timeline.INDEFINITE);
        rotateAnimation.setInterpolator(Interpolator.LINEAR);
    }
    
    /**
     * 顯示載入視窗
     */
    public void show() {
        Platform.runLater(() -> {
            if (loadingStage != null && !loadingStage.isShowing()) {
                // 設定視窗位置（居中顯示）
                centerOnScreen();
                
                // 顯示視窗
                loadingStage.show();
                
                // 啟動進入動畫
                playEnterAnimation();
                
                // 如果是不確定進度，啟動動畫
                if (isIndeterminate) {
                    pulseAnimation.play();
                    rotateAnimation.play();
                }
            }
        });
    }
    
    /**
     * 隱藏載入視窗
     */
    public void hide() {
        Platform.runLater(() -> {
            if (loadingStage != null && loadingStage.isShowing()) {
                playExitAnimation(() -> {
                    stopAllAnimations();
                    loadingStage.hide();
                });
            }
        });
    }
    
    /**
     * 關閉載入視窗
     */
    public void close() {
        Platform.runLater(() -> {
            if (loadingStage != null) {
                stopAllAnimations();
                loadingStage.close();
                loadingStage = null;
            }
        });
    }
    
    /**
     * 更新進度
     */
    public void updateProgress(double progress) {
        Platform.runLater(() -> {
            if (progress < 0 || progress > 1.0) {
                // 切換到不確定進度模式
                setIndeterminate(true);
                return;
            }

            currentProgress = progress;

            if (isIndeterminate && progress >= 0) {
                // 從不確定進度切換到確定進度
                setIndeterminate(false);
            }

            if (!isIndeterminate) {
                // 平滑動畫更新進度
                if (progressAnimation != null) {
                    progressAnimation.stop();
                }

                progressAnimation = new Timeline(
                        new KeyFrame(
                                Duration.millis(300),
                                new KeyValue(progressBar.progressProperty(), progress, Interpolator.EASE_OUT)
                        )
                );
                progressAnimation.play();

                // 更新百分比文字
                int percentage = (int) (progress * 100);
                percentageLabel.setText(percentage + "%");
            }
        });
    }


    /**
     * 更新載入訊息
     */
    public void updateMessage(String message) {
        Platform.runLater(() -> {
            if (messageLabel != null && message != null) {
                messageLabel.setText(message);
                
                // 添加淡入淡出效果
                FadeTransition fade = new FadeTransition(Duration.millis(200), messageLabel);
                fade.setFromValue(0.7);
                fade.setToValue(1.0);
                fade.play();
            }
        });
    }
    
    /**
     * 設定載入類型
     */
    public void setLoadingType(LoadingType type) {
        this.currentType = type;
        Platform.runLater(() -> {
            if (loadingStage != null && loadingStage.getScene() != null) {
                // 重新設定UI以反映新類型
                setupUI();
            }
        });
    }
    
    /**
     * 設定是否為不確定進度
     */
    public void setIndeterminate(boolean indeterminate) {
        this.isIndeterminate = indeterminate;
        
        Platform.runLater(() -> {
            if (indeterminate) {
                progressBar.setProgress(ProgressBar.INDETERMINATE_PROGRESS);
                progressIndicator.setVisible(true);
                percentageLabel.setText("載入中...");
                
                // 啟動動畫
                pulseAnimation.play();
                rotateAnimation.play();
            } else {
                progressBar.setProgress(currentProgress);
                progressIndicator.setVisible(false);
                
                // 停止動畫
                pulseAnimation.stop();
                rotateAnimation.stop();
            }
        });
    }
    
    /**
     * 設定父視窗（用於模態顯示）
     */
    public void setOwner(Stage owner) {
        if (loadingStage != null && owner != null) {
            loadingStage.initOwner(owner);
            loadingStage.initModality(Modality.WINDOW_MODAL);
        }
    }
    
    /**
     * 居中顯示
     */
    private void centerOnScreen() {
        if (loadingStage != null) {
            javafx.geometry.Rectangle2D screenBounds = 
                javafx.stage.Screen.getPrimary().getVisualBounds();
            
            double centerX = screenBounds.getMinX() + 
                (screenBounds.getWidth() - 400) / 2;
            double centerY = screenBounds.getMinY() + 
                (screenBounds.getHeight() - 220) / 2;
            
            loadingStage.setX(centerX);
            loadingStage.setY(centerY);
        }
    }
    
    /**
     * 播放進入動畫
     */
    private void playEnterAnimation() {
        // 設定初始狀態
        containerBox.setScaleX(0.8);
        containerBox.setScaleY(0.8);
        containerBox.setOpacity(0.0);
        backgroundOverlay.setOpacity(0.0);
        
        // 背景淡入
        FadeTransition bgFade = new FadeTransition(Duration.millis(200), backgroundOverlay);
        bgFade.setFromValue(0.0);
        bgFade.setToValue(0.4);
        
        // 容器縮放和淡入
        ScaleTransition scale = new ScaleTransition(Duration.millis(300), containerBox);
        scale.setFromX(0.8);
        scale.setFromY(0.8);
        scale.setToX(1.0);
        scale.setToY(1.0);
        scale.setInterpolator(Interpolator.EASE_OUT);
        
        FadeTransition fade = new FadeTransition(Duration.millis(300), containerBox);
        fade.setFromValue(0.0);
        fade.setToValue(1.0);
        
        // 組合動畫
        ParallelTransition enterAnimation = new ParallelTransition(bgFade, scale, fade);
        enterAnimation.play();
    }
    
    /**
     * 播放退出動畫
     */
    private void playExitAnimation(Runnable onFinished) {
        // 容器縮放和淡出
        ScaleTransition scale = new ScaleTransition(Duration.millis(250), containerBox);
        scale.setFromX(1.0);
        scale.setFromY(1.0);
        scale.setToX(0.8);
        scale.setToY(0.8);
        scale.setInterpolator(Interpolator.EASE_IN);
        
        FadeTransition fade = new FadeTransition(Duration.millis(250), containerBox);
        fade.setFromValue(1.0);
        fade.setToValue(0.0);
        
        // 背景淡出
        FadeTransition bgFade = new FadeTransition(Duration.millis(300), backgroundOverlay);
        bgFade.setFromValue(0.4);
        bgFade.setToValue(0.0);
        
        // 組合動畫
        ParallelTransition exitAnimation = new ParallelTransition(scale, fade, bgFade);
        exitAnimation.setOnFinished(e -> {
            if (onFinished != null) {
                onFinished.run();
            }
        });
        exitAnimation.play();
    }
    
    /**
     * 停止所有動畫
     */
    private void stopAllAnimations() {
        if (pulseAnimation != null) pulseAnimation.stop();
        if (scaleAnimation != null) scaleAnimation.stop();
        if (rotateAnimation != null) rotateAnimation.stop();
        if (progressAnimation != null) progressAnimation.stop();
    }
    
    /**
     * 將Color轉換為十六進位字串
     */
    private String toHexString(Color color) {
        return String.format("#%02x%02x%02x",
            (int) (color.getRed() * 255),
            (int) (color.getGreen() * 255),
            (int) (color.getBlue() * 255)
        );
    }
    
    /**
     * 檢查是否正在顯示
     */
    public boolean isShowing() {
        return loadingStage != null && loadingStage.isShowing();
    }
    
    // 靜態工廠方法
    
    /**
     * 創建啟動載入條
     */
    public static LoadingProgressBar createStartupLoader() {
        return new LoadingProgressBar(LoadingType.STARTUP);
    }
    
    /**
     * 創建檔案開啟載入條
     */
    public static LoadingProgressBar createFileLoader() {
        return new LoadingProgressBar(LoadingType.FILE_OPENING);
    }
    
    /**
     * 創建文字提取載入條
     */
    public static LoadingProgressBar createTextExtractor() {
        return new LoadingProgressBar(LoadingType.TEXT_EXTRACTING);
    }
    
    /**
     * 創建PDF處理載入條
     */
    public static LoadingProgressBar createPdfProcessor() {
        return new LoadingProgressBar(LoadingType.PDF_PROCESSING);
    }
    
    /**
     * 創建EPUB處理載入條
     */
    public static LoadingProgressBar createEpubProcessor() {
        return new LoadingProgressBar(LoadingType.EPUB_PROCESSING);
    }
    
    /**
     * 創建圖片載入條
     */
    public static LoadingProgressBar createImageLoader() {
        return new LoadingProgressBar(LoadingType.IMAGE_LOADING);
    }
    
    /**
     * 創建儲存載入條
     */
    public static LoadingProgressBar createSaveLoader() {
        return new LoadingProgressBar(LoadingType.SAVING);
    }
}