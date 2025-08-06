package E_Reader.ui;

import E_Reader.settings.SettingsManager;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.animation.*;
import javafx.util.Duration;

import java.util.Timer;
import java.util.TimerTask;

/**
 * 增強版設定對話框 - 現代化iOS風格設計 + 即時預覽功能
 */
public class EnhancedSettingsDialog {

    private final SettingsManager settingsManager;
    private final Stage parentStage;
    private Stage dialogStage;
    private Timer brightnessUpdateTimer;

    // 添加主界面更新回調接口
    private Runnable uiUpdateCallback;

    // UI 組件
    private ToggleGroup themeGroup;
    private ToggleGroup ocrGroup;
    private Slider brightnessSlider;
    private CheckBox rememberFileCheckBox;
    private CheckBox showPageNumbersCheckBox;
    private CheckBox enableTouchNavCheckBox;
    private Slider autoSaveIntervalSlider;
    private Timer autoSaveUpdateTimer;

    // 預覽組件
    private VBox themePreviewBox;
    private Label ocrDetailLabel;
    private ProgressBar brightnessPreview;

    // 臨時設定（用於預覽）- 移除臨時變數，直接使用settingsManager
    // private SettingsManager.ThemeMode tempThemeMode;
    // private int tempBrightness;

    public EnhancedSettingsDialog(SettingsManager settingsManager, Stage parentStage) {
        this.settingsManager = settingsManager;
        this.parentStage = parentStage;
        createDialog();
    }

    // 添加設定UI更新回調的方法
    public void setUIUpdateCallback(Runnable callback) {
        this.uiUpdateCallback = callback;
    }

    private void createDialog() {
        dialogStage = new Stage();
        dialogStage.initStyle(StageStyle.UNDECORATED);
        dialogStage.initOwner(parentStage);
        dialogStage.setTitle("設定");

        // 創建主要內容
        VBox mainContent = createMainContent();

        // 創建場景
        Scene scene = new Scene(mainContent, 700, 750);
        scene.setFill(null);

        dialogStage.setScene(scene);
        dialogStage.centerOnScreen();
    }

    private VBox createMainContent() {
        VBox mainContainer = new VBox();
        mainContainer.setAlignment(Pos.TOP_CENTER);
        mainContainer.setPadding(new Insets(20));
        mainContainer.setStyle(
                "-fx-background-color: linear-gradient(to bottom, " +
                        "rgba(40,40,40,0.98) 0%, " +
                        "rgba(50,50,50,0.98) 100%); " +
                        "-fx-border-color: rgba(255,255,255,0.3); " +
                        "-fx-border-width: 1.5; " +
                        "-fx-border-radius: 16; " +
                        "-fx-background-radius: 16; " +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.8), 20, 0, 0, 10);"
        );

        // 標題欄
        HBox titleBar = createTitleBar();

        // 內容區域
        TabPane contentTabs = createContentTabs();

        // 按鈕欄
        HBox buttonBar = createButtonBar();

        mainContainer.getChildren().addAll(titleBar, contentTabs, buttonBar);
        VBox.setVgrow(contentTabs, Priority.ALWAYS);

        return mainContainer;
    }

    private HBox createTitleBar() {
        HBox titleBar = new HBox();
        titleBar.setAlignment(Pos.CENTER);
        titleBar.setPadding(new Insets(0, 0, 20, 0));

        Label titleLabel = new Label("⚙️ 應用程式設定");
        titleLabel.setStyle(
                "-fx-text-fill: white; " +
                        "-fx-font-size: 20px; " +
                        "-fx-font-weight: 700;"
        );

        Button closeButton = new Button("✕");
        closeButton.setStyle(
                "-fx-background-color: rgba(231,76,60,0.8); " +
                        "-fx-text-fill: white; " +
                        "-fx-border-radius: 50%; " +
                        "-fx-background-radius: 50%; " +
                        "-fx-min-width: 30; " +
                        "-fx-min-height: 30; " +
                        "-fx-max-width: 30; " +
                        "-fx-max-height: 30; " +
                        "-fx-cursor: hand;"
        );
        closeButton.setOnAction(e -> close());

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        titleBar.getChildren().addAll(titleLabel, spacer, closeButton);
        return titleBar;
    }

    private TabPane createContentTabs() {
        TabPane tabPane = new TabPane();
        tabPane.setStyle(
                "-fx-background-color: rgba(55,55,55,0.9); " +
                        "-fx-border-radius: 12; " +
                        "-fx-background-radius: 12; " +
                        "-fx-tab-header-area-background-color: rgba(45,45,45,0.9);"
        );

        // 外觀主題標籤
        Tab themeTab = createThemeTab();
        themeTab.setText("🎨 外觀");

        // OCR設定標籤
        Tab ocrTab = createOcrTab();
        ocrTab.setText("🔧 OCR");

        // 功能選項標籤
        Tab functionsTab = createFunctionsTab();
        functionsTab.setText("⚙️ 功能");

        tabPane.getTabs().addAll(themeTab, ocrTab, functionsTab);
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);

        return tabPane;
    }

    private Tab createThemeTab() {
        Tab tab = new Tab();
        tab.setClosable(false);

        // 使用HBox來並排顯示設定和預覽
        HBox mainBox = new HBox(20);
        mainBox.setPadding(new Insets(20));

        // 左側：設定區域
        VBox settingsArea = new VBox(20);
        settingsArea.setPrefWidth(350);

        // 右側：預覽區域
        VBox previewArea = createThemePreviewArea();
        previewArea.setPrefWidth(280);

        // 主題選擇
        VBox themeSection = createSection("🎨 外觀主題", "選擇你喜歡的閱讀風格");

        themeGroup = new ToggleGroup();
        VBox themeOptions = new VBox(10);

        for (SettingsManager.ThemeMode theme : SettingsManager.ThemeMode.values()) {
            RadioButton themeRadio = createThemeOption(theme);
            themeRadio.setToggleGroup(themeGroup);

            // 修改：直接應用設定並更新預覽，不使用臨時變數
            themeRadio.setOnAction(e -> {
                if (themeRadio.isSelected()) {
                    System.out.println("主題變更為: " + theme.getDisplayName());

                    settingsManager.setThemeMode(theme);
                    settingsManager.saveSettings();
                    updateThemePreview();

                    // 即時更新UI
                    if (uiUpdateCallback != null) {
                        Platform.runLater(() -> {
                            uiUpdateCallback.run();
                        });
                    }
                }
            });

            themeOptions.getChildren().add(themeRadio);

            if (theme == settingsManager.getCurrentTheme()) {
                themeRadio.setSelected(true);
            }
        }

        themeSection.getChildren().add(themeOptions);

        // 亮度設定
        VBox brightnessSection = createSection("🔆 顯示亮度", "調整閱讀舒適度（實時生效）");

        brightnessSlider = new Slider(10, 100, settingsManager.getEyeCareBrightness());
        brightnessSlider.setShowTickLabels(true);
        brightnessSlider.setShowTickMarks(true);
        brightnessSlider.setMajorTickUnit(20);
        brightnessSlider.setStyle(
                "-fx-background-color: rgba(70,70,70,0.8); " +
                        "-fx-border-radius: 8; " +
                        "-fx-background-radius: 8;"
        );

        Label brightnessLabel = new Label(String.format("%.0f%%", brightnessSlider.getValue()));
        brightnessLabel.setStyle(
                "-fx-text-fill: white; " +
                        "-fx-font-weight: bold; " +
                        "-fx-background-color: rgba(52,152,219,0.3); " +
                        "-fx-padding: 5 10; " +
                        "-fx-background-radius: 6;"
        );

        // 修改：亮度變更時即時應用
        brightnessSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            int brightness = newVal.intValue();
            brightnessLabel.setText(String.format("%.0f%%", newVal.doubleValue()));

            // 防止過於頻繁的更新，使用去抖動機制
            if (brightnessUpdateTimer != null) {
                brightnessUpdateTimer.cancel();
            }

            brightnessUpdateTimer = new Timer();
            brightnessUpdateTimer.schedule(new TimerTask() {
                @Override
                public void run() {
                    Platform.runLater(() -> {
                        // 即時應用亮度設定
                        settingsManager.setEyeCareBrightness(brightness);
                        settingsManager.saveSettings();
                        updateBrightnessPreview();

                        // 即時更新UI亮度
                        if (uiUpdateCallback != null) {
                            uiUpdateCallback.run();
                        }

                        System.out.println("亮度設定已更新: " + brightness + "%");
                    });
                }
            }, 200); // 200ms 延遲，避免過於頻繁的更新
        });

        HBox brightnessControl = new HBox(15);
        brightnessControl.setAlignment(Pos.CENTER_LEFT);
        brightnessControl.getChildren().addAll(brightnessSlider, brightnessLabel);
        HBox.setHgrow(brightnessSlider, Priority.ALWAYS);

        brightnessSection.getChildren().add(brightnessControl);

        settingsArea.getChildren().addAll(themeSection, createSeparator(), brightnessSection);

        // 添加到主容器
        mainBox.getChildren().addAll(settingsArea, previewArea);

        ScrollPane scrollPane = new ScrollPane(mainBox);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background-color: rgba(50,50,50,0.95); -fx-background: transparent;");

        tab.setContent(scrollPane);
        return tab;
    }

    // 創建主題預覽區域
    private VBox createThemePreviewArea() {
        VBox previewArea = new VBox(15);
        previewArea.setStyle(
                "-fx-background-color: rgba(60,60,60,0.8); " +
                        "-fx-border-color: rgba(255,255,255,0.2); " +
                        "-fx-border-width: 1; " +
                        "-fx-border-radius: 12; " +
                        "-fx-background-radius: 12; " +
                        "-fx-padding: 20;"
        );

        Label previewTitle = new Label("📱 即時預覽");
        previewTitle.setStyle(
                "-fx-text-fill: white; " +
                        "-fx-font-size: 16px; " +
                        "-fx-font-weight: bold;"
        );

        // 主題預覽框
        themePreviewBox = new VBox(10);
        themePreviewBox.setPrefHeight(150);
        themePreviewBox.setPadding(new Insets(15));
        themePreviewBox.setAlignment(Pos.CENTER);

        // 亮度預覽
        VBox brightnessPreviewSection = new VBox(8);

        Label brightnessPreviewTitle = new Label("💡 亮度效果");
        brightnessPreviewTitle.setStyle("-fx-text-fill: white; -fx-font-size: 14px; -fx-font-weight: 600;");

        brightnessPreview = new ProgressBar();
        brightnessPreview.setPrefWidth(200);
        brightnessPreview.setPrefHeight(8);

        brightnessPreviewSection.getChildren().addAll(brightnessPreviewTitle, brightnessPreview);

        previewArea.getChildren().addAll(previewTitle, themePreviewBox, brightnessPreviewSection);

        // 初始化預覽
        updateThemePreview();
        updateBrightnessPreview();

        return previewArea;
    }

    // 更新主題預覽 - 修改為使用當前設定而非臨時變數
    private void updateThemePreview() {
        themePreviewBox.getChildren().clear();

        SettingsManager.ThemeMode currentTheme = settingsManager.getCurrentTheme();
        String bgColor = currentTheme.getBackgroundColor();
        String textColor = currentTheme.getTextColor();

        themePreviewBox.setStyle(
                "-fx-background-color: " + bgColor + "; " +
                        "-fx-border-color: " + textColor + "; " +
                        "-fx-border-width: 1; " +
                        "-fx-border-radius: 8; " +
                        "-fx-background-radius: 8;"
        );

        Label themeNameLabel = new Label(currentTheme.getDisplayName());
        themeNameLabel.setStyle(
                "-fx-text-fill: " + textColor + "; " +
                        "-fx-font-size: 18px; " +
                        "-fx-font-weight: bold;"
        );

        Label sampleText = new Label("這裡是文字範例\n背景顏色：" + bgColor + "\n文字顏色：" + textColor);
        sampleText.setStyle(
                "-fx-text-fill: " + textColor + "; " +
                        "-fx-font-size: 12px; " +
                        "-fx-text-alignment: center;"
        );

        // 添加一些裝飾元素
        HBox decorationBox = new HBox(10);
        decorationBox.setAlignment(Pos.CENTER);

        for (int i = 0; i < 3; i++) {
            Region colorDot = new Region();
            colorDot.setPrefSize(12, 12);
            colorDot.setStyle(
                    "-fx-background-color: " + textColor + "; " +
                            "-fx-background-radius: 50%;"
            );
            decorationBox.getChildren().add(colorDot);
        }

        themePreviewBox.getChildren().addAll(themeNameLabel, sampleText, decorationBox);

        // 添加淡入動畫
        FadeTransition fadeIn = new FadeTransition(Duration.millis(300), themePreviewBox);
        fadeIn.setFromValue(0.0);
        fadeIn.setToValue(1.0);
        fadeIn.play();
    }

    // 更新亮度預覽 - 修改為使用當前設定
    private void updateBrightnessPreview() {
        int currentBrightness = settingsManager.getEyeCareBrightness();
        double progress = currentBrightness / 100.0;
        brightnessPreview.setProgress(progress);

        // 根據亮度調整顏色
        String brightnessColor;
        if (currentBrightness < 30) {
            brightnessColor = "#e74c3c"; // 低亮度 - 紅色
        } else if (currentBrightness < 70) {
            brightnessColor = "#f39c12"; // 中亮度 - 橙色
        } else {
            brightnessColor = "#27ae60"; // 高亮度 - 綠色
        }

        brightnessPreview.setStyle(
                "-fx-accent: " + brightnessColor + "; " +
                        "-fx-background-color: rgba(255,255,255,0.1); " +
                        "-fx-background-radius: 4; " +
                        "-fx-background-insets: 0;"
        );
    }

    private Tab createOcrTab() {
        Tab tab = new Tab();
        tab.setClosable(false);

        HBox mainBox = new HBox(20);
        mainBox.setPadding(new Insets(20));

        // 左側：設定區域
        VBox settingsArea = new VBox(20);
        settingsArea.setPrefWidth(350);

        // 右側：詳細說明區域
        VBox detailArea = createOcrDetailArea();
        detailArea.setPrefWidth(280);

        // OCR狀態顯示
        VBox statusSection = createSection("📊 OCR狀態", "當前文字辨識設定");

        Label statusLabel = new Label("OCR 引擎已就緒");
        statusLabel.setStyle(
                "-fx-text-fill: white; " +
                        "-fx-font-weight: bold; " +
                        "-fx-background-color: rgba(46,204,113,0.3); " +
                        "-fx-padding: 10 15; " +
                        "-fx-background-radius: 8;"
        );
        statusSection.getChildren().add(statusLabel);

        // OCR模型選擇
        VBox modelSection = createSection("🔧 OCR模型", "選擇文字識別精準度");

        ocrGroup = new ToggleGroup();
        VBox modelOptions = new VBox(10);

        for (SettingsManager.OcrModel model : SettingsManager.OcrModel.values()) {
            RadioButton modelRadio = createOcrOption(model);
            modelRadio.setToggleGroup(ocrGroup);

            // 修改：直接應用OCR設定
            modelRadio.setOnAction(e -> {
                settingsManager.setOcrModel(model);
                updateOcrDetails(model);
                // 保存OCR設定
                settingsManager.saveSettings();
            });

            modelOptions.getChildren().add(modelRadio);

            if (model == settingsManager.getOcrModel()) {
                modelRadio.setSelected(true);
                updateOcrDetails(model);
            }
        }

        modelSection.getChildren().add(modelOptions);
        settingsArea.getChildren().addAll(statusSection, createSeparator(), modelSection);

        mainBox.getChildren().addAll(settingsArea, detailArea);

        ScrollPane scrollPane = new ScrollPane(mainBox);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background-color: rgba(50,50,50,0.95); -fx-background: transparent;");

        tab.setContent(scrollPane);
        return tab;
    }

    // 創建OCR詳細說明區域
    private VBox createOcrDetailArea() {
        VBox detailArea = new VBox(15);
        detailArea.setStyle(
                "-fx-background-color: rgba(60,60,60,0.8); " +
                        "-fx-border-color: rgba(255,255,255,0.2); " +
                        "-fx-border-width: 1; " +
                        "-fx-border-radius: 12; " +
                        "-fx-background-radius: 12; " +
                        "-fx-padding: 20;"
        );

        Label detailTitle = new Label("🔍 模型詳情");
        detailTitle.setStyle(
                "-fx-text-fill: white; " +
                        "-fx-font-size: 16px; " +
                        "-fx-font-weight: bold;"
        );

        ocrDetailLabel = new Label();
        ocrDetailLabel.setStyle(
                "-fx-text-fill: rgba(255,255,255,0.9); " +
                        "-fx-font-size: 12px; " +
                        "-fx-wrap-text: true;"
        );
        ocrDetailLabel.setWrapText(true);

        detailArea.getChildren().addAll(detailTitle, ocrDetailLabel);
        return detailArea;
    }

    // 更新OCR模型詳細說明
    private void updateOcrDetails(SettingsManager.OcrModel model) {
        String detailText = "";

        switch (model) {
            case FAST:
                detailText = "📈 快速模型特點：\n\n" +
                        "• 識別速度：⚡ 極快\n" +
                        "• 準確率：★★★☆☆ (約85%)\n" +
                        "• 適用場景：日常閱讀、快速掃描\n" +
                        "• 記憶體占用：💾 較低 (~200MB)\n" +
                        "• 語言支援：✅ 中英文基礎識別\n\n" +
                        "🔧 技術細節：\n" +
                        "• 使用 LSTM 神經網路\n" +
                        "• 基於 tessdata_fast 訓練資料\n" +
                        "• 適合移動設備和低配電腦\n\n" +
                        "💡 建議使用情況：\n" +
                        "• 漫畫、小說等文字較為簡單的內容\n" +
                        "• 需要快速瀏覽大量頁面時\n" +
                        "• 電腦性能較低時的最佳選擇";
                break;

            case BEST:
                detailText = "🎯 最佳模型特點：\n\n" +
                        "• 識別速度：⏳ 較慢但精確\n" +
                        "• 準確率：★★★★★ (約95%)\n" +
                        "• 適用場景：學術論文、重要文件\n" +
                        "• 記憶體占用：💾 較高 (~500MB)\n" +
                        "• 語言支援：✅ 多語言高精度識別\n\n" +
                        "🔧 技術細節：\n" +
                        "• 使用先進的 Transformer 架構\n" +
                        "• 基於 tessdata_best 高品質訓練資料\n" +
                        "• 包含字典和語言模型優化\n\n" +
                        "💡 建議使用情況：\n" +
                        "• PDF學術論文和技術文件\n" +
                        "• 需要高準確率的重要內容\n" +
                        "• 複雜排版和特殊字體的文件\n" +
                        "• 多語言混合的文檔";
                break;
        }

        ocrDetailLabel.setText(detailText);

        // 添加動畫效果
        FadeTransition fadeIn = new FadeTransition(Duration.millis(300), ocrDetailLabel);
        fadeIn.setFromValue(0.0);
        fadeIn.setToValue(1.0);
        fadeIn.play();
    }

    private Tab createFunctionsTab() {
        Tab tab = new Tab();
        tab.setClosable(false);

        VBox content = new VBox(20);
        content.setPadding(new Insets(20));
        content.setStyle("-fx-background-color: rgba(50,50,50,0.95);");

        // 文件管理
        VBox fileSection = createSection("📁 文件管理", "自訂文件處理行為");

        rememberFileCheckBox = new CheckBox("記住最後開啟的文件");
        rememberFileCheckBox.setSelected(settingsManager.isRememberLastFile());
        rememberFileCheckBox.setStyle("-fx-text-fill: white; -fx-font-size: 13px;");

        // 修改：即時應用設定變更
        rememberFileCheckBox.setOnAction(e -> {
            boolean isSelected = rememberFileCheckBox.isSelected();

            System.out.println("記住檔案設定變更: " + isSelected);

            settingsManager.setRememberLastFile(isSelected);
            settingsManager.saveSettings();

            String message = isSelected ? "將會記住最後開啟的檔案" : "不會記住最後開啟的檔案";
            System.out.println(message);
        });


        Label fileHelpLabel = new Label("💡 啟用後會在下次開啟應用程式時自動載入上次閱讀的檔案和頁碼");
        fileHelpLabel.setStyle("-fx-text-fill: rgba(255,255,255,0.7); -fx-font-size: 11px; -fx-wrap-text: true;");
        fileHelpLabel.setWrapText(true);

        fileSection.getChildren().addAll(rememberFileCheckBox, fileHelpLabel);

        // 介面顯示
        VBox interfaceSection = createSection("🖥️ 介面顯示", "自訂使用者介面（即時生效）");

        showPageNumbersCheckBox = new CheckBox("顯示頁碼資訊");
        showPageNumbersCheckBox.setSelected(settingsManager.isShowPageNumbers());
        showPageNumbersCheckBox.setStyle("-fx-text-fill: white; -fx-font-size: 13px;");

        // 修改：即時應用頁碼顯示設定
        showPageNumbersCheckBox.setOnAction(e -> {
            boolean isSelected = showPageNumbersCheckBox.isSelected();

            System.out.println("頁碼顯示設定變更: " + isSelected);

            settingsManager.setShowPageNumbers(isSelected);
            settingsManager.saveSettings();

            // 即時更新UI
            if (uiUpdateCallback != null) {
                Platform.runLater(() -> {
                    uiUpdateCallback.run();
                });
            }

            // 顯示變更確認
            String message = isSelected ? "頁碼顯示已啟用" : "頁碼顯示已關閉";
            System.out.println(message);
        });

        Label interfaceHelpLabel = new Label("📄 控制右下角頁碼顯示，關閉可獲得更清爽的閱讀體驗");
        interfaceHelpLabel.setStyle("-fx-text-fill: rgba(255,255,255,0.7); -fx-font-size: 11px; -fx-wrap-text: true;");
        interfaceHelpLabel.setWrapText(true);

        interfaceSection.getChildren().addAll(showPageNumbersCheckBox, interfaceHelpLabel);

        // 閱讀體驗
        VBox readingSection = createSection("📖 閱讀體驗", "優化閱讀舒適度");

        enableTouchNavCheckBox = new CheckBox("觸控導覽");
        enableTouchNavCheckBox.setSelected(settingsManager.isEnableTouchNavigation());
        enableTouchNavCheckBox.setStyle("-fx-text-fill: white; -fx-font-size: 13px;");

        enableTouchNavCheckBox.setOnAction(e -> {
            boolean isSelected = enableTouchNavCheckBox.isSelected();

            System.out.println("觸控導覽設定變更: " + isSelected);

            settingsManager.setEnableTouchNavigation(isSelected);
            settingsManager.saveSettings();
        });


        Label touchHelpLabel = new Label("👆 啟用觸控螢幕支援，可用手勢進行頁面導覽");
        touchHelpLabel.setStyle("-fx-text-fill: rgba(255,255,255,0.7); -fx-font-size: 11px; -fx-wrap-text: true;");
        touchHelpLabel.setWrapText(true);

        readingSection.getChildren().addAll(enableTouchNavCheckBox, touchHelpLabel);

        // 自動保存設定
        VBox autoSaveSection = createSection("💾 自動保存", "設定自動保存間隔");

        autoSaveIntervalSlider = new Slider(10, 300, settingsManager.getAutoSaveInterval());
        autoSaveIntervalSlider.setShowTickLabels(true);
        autoSaveIntervalSlider.setShowTickMarks(true);
        autoSaveIntervalSlider.setMajorTickUnit(60);
        autoSaveIntervalSlider.setStyle(
                "-fx-background-color: rgba(70,70,70,0.8); " +
                        "-fx-border-radius: 8; " +
                        "-fx-background-radius: 8;"
        );

        Label intervalLabel = new Label(String.format("%.0f 秒", autoSaveIntervalSlider.getValue()));
        intervalLabel.setStyle(
                "-fx-text-fill: white; " +
                        "-fx-font-weight: bold; " +
                        "-fx-background-color: rgba(52,152,219,0.3); " +
                        "-fx-padding: 5 10; " +
                        "-fx-background-radius: 6;"
        );

        // 修改：即時應用自動保存間隔設定
        autoSaveIntervalSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            int interval = newVal.intValue();
            intervalLabel.setText(String.format("%.0f 秒", newVal.doubleValue()));

            // 使用去抖動機制避免過於頻繁的更新
            if (autoSaveUpdateTimer != null) {
                autoSaveUpdateTimer.cancel();
            }

            autoSaveUpdateTimer = new Timer();
            autoSaveUpdateTimer.schedule(new TimerTask() {
                @Override
                public void run() {
                    Platform.runLater(() -> {
                        settingsManager.setAutoSaveInterval(interval);
                        settingsManager.saveSettings();
                        System.out.println("自動保存間隔已更新: " + interval + " 秒");
                    });
                }
            }, 300);
        });
        HBox intervalControl = new HBox(15);
        intervalControl.setAlignment(Pos.CENTER_LEFT);
        intervalControl.getChildren().addAll(autoSaveIntervalSlider, intervalLabel);
        HBox.setHgrow(autoSaveIntervalSlider, Priority.ALWAYS);

        Label autoSaveHelpLabel = new Label("⚡ 自動保存閱讀進度和書籤，防止意外關閉造成資料遺失");
        autoSaveHelpLabel.setStyle("-fx-text-fill: rgba(255,255,255,0.7); -fx-font-size: 11px; -fx-wrap-text: true;");
        autoSaveHelpLabel.setWrapText(true);

        autoSaveSection.getChildren().addAll(intervalControl, autoSaveHelpLabel);

        content.getChildren().addAll(
                fileSection,
                createSeparator(),
                interfaceSection,
                createSeparator(),
                readingSection,
                createSeparator(),
                autoSaveSection
        );

        ScrollPane scrollPane = new ScrollPane(content);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background-color: transparent; -fx-background: transparent;");

        tab.setContent(scrollPane);
        return tab;
    }

    private VBox createSection(String title, String description) {
        VBox section = new VBox(12);

        Label titleLabel = new Label(title);
        titleLabel.setStyle(
                "-fx-text-fill: white; " +
                        "-fx-font-size: 16px; " +
                        "-fx-font-weight: 700;"
        );

        Label descLabel = new Label(description);
        descLabel.setStyle(
                "-fx-text-fill: rgba(255,255,255,0.8); " +
                        "-fx-font-size: 12px; " +
                        "-fx-wrap-text: true;"
        );

        section.getChildren().addAll(titleLabel, descLabel);
        return section;
    }

    private RadioButton createThemeOption(SettingsManager.ThemeMode theme) {
        RadioButton radio = new RadioButton(theme.getDisplayName());
        radio.setStyle(
                "-fx-text-fill: white; " +
                        "-fx-font-size: 13px; " +
                        "-fx-font-weight: 500;"
        );
        return radio;
    }

    private RadioButton createOcrOption(SettingsManager.OcrModel model) {
        RadioButton radio = new RadioButton(model.getDisplayName());
        radio.setStyle(
                "-fx-text-fill: white; " +
                        "-fx-font-size: 13px; " +
                        "-fx-font-weight: 500;"
        );
        return radio;
    }

    private Separator createSeparator() {
        Separator separator = new Separator();
        separator.setStyle("-fx-background-color: rgba(255,255,255,0.3);");
        return separator;
    }

    private HBox createButtonBar() {
        HBox buttonBar = new HBox(15);
        buttonBar.setAlignment(Pos.CENTER_RIGHT);
        buttonBar.setPadding(new Insets(20, 0, 0, 0));

        Button cancelButton = new Button("取消");
        cancelButton.setStyle(
                "-fx-background-color: rgba(70,70,70,0.9); " +
                        "-fx-border-color: rgba(255,255,255,0.4); " +
                        "-fx-border-width: 1; " +
                        "-fx-border-radius: 8; " +
                        "-fx-background-radius: 8; " +
                        "-fx-text-fill: white; " +
                        "-fx-font-size: 14px; " +
                        "-fx-font-weight: 600; " +
                        "-fx-padding: 10 20; " +
                        "-fx-cursor: hand;"
        );
        cancelButton.setOnAction(e -> close());

        // 移除預覽按鈕，因為現在是即時預覽
        // Button previewButton = new Button("🔍 預覽變更");

        Button okButton = new Button("✅ 完成設定");
        okButton.setStyle(
                "-fx-background-color: linear-gradient(to bottom, " +
                        "rgba(52,152,219,0.9), rgba(41,128,185,0.9)); " +
                        "-fx-border-color: rgba(52,152,219,0.8); " +
                        "-fx-border-width: 1; " +
                        "-fx-border-radius: 8; " +
                        "-fx-background-radius: 8; " +
                        "-fx-text-fill: white; " +
                        "-fx-font-size: 14px; " +
                        "-fx-font-weight: 700; " +
                        "-fx-padding: 10 20; " +
                        "-fx-cursor: hand; " +
                        "-fx-effect: dropshadow(gaussian, rgba(52,152,219,0.5), 8, 0, 0, 3);"
        );
        okButton.setOnAction(e -> {
            // 最終保存所有設定
            saveAllSettings();
            close();
        });

        buttonBar.getChildren().addAll(cancelButton, okButton);
        return buttonBar;
    }

    // 移除原本的預覽功能，改為即時應用
    // private void previewSettings() { ... }

    // 修改：最終保存所有設定
    private void saveAllSettings() {
        // 確保所有設定都已保存
        settingsManager.saveSettings();
        showNotification("設定已保存", "所有變更已成功套用並保存");
    }

    // 保持原有的保存方法作為備用
    private void saveSettings() {
        // 保存主題設定
        if (themeGroup.getSelectedToggle() != null) {
            RadioButton selectedTheme = (RadioButton) themeGroup.getSelectedToggle();
            for (SettingsManager.ThemeMode theme : SettingsManager.ThemeMode.values()) {
                if (theme.getDisplayName().equals(selectedTheme.getText())) {
                    settingsManager.setThemeMode(theme);
                    break;
                }
            }
        }

        // 保存OCR設定
        if (ocrGroup.getSelectedToggle() != null) {
            RadioButton selectedOcr = (RadioButton) ocrGroup.getSelectedToggle();
            for (SettingsManager.OcrModel model : SettingsManager.OcrModel.values()) {
                if (model.getDisplayName().equals(selectedOcr.getText())) {
                    settingsManager.setOcrModel(model);
                    break;
                }
            }
        }

        // 保存亮度設定
        settingsManager.setEyeCareBrightness((int) brightnessSlider.getValue());

        // 保存功能設定
        settingsManager.setRememberLastFile(rememberFileCheckBox.isSelected());
        settingsManager.setShowPageNumbers(showPageNumbersCheckBox.isSelected());
        settingsManager.setEnableTouchNavigation(enableTouchNavCheckBox.isSelected());
        settingsManager.setAutoSaveInterval((int) autoSaveIntervalSlider.getValue());

        // 保存到文件
        settingsManager.saveSettings();

        showNotification("設定已保存", "所有變更已成功套用");
    }

    private void showNotification(String title, String message) {
        // 簡單的通知實現
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public void show() {
        dialogStage.show();
    }


    // 4. 修正關閉對話框時清理資源
    public void close() {
        // 清理計時器
        if (brightnessUpdateTimer != null) {
            brightnessUpdateTimer.cancel();
            brightnessUpdateTimer = null;
        }

        // 確保所有設定都已保存
        settingsManager.saveSettings();

        dialogStage.close();
    }
//    @Override
//    public void close() {
//        // 清理所有計時器
//        if (brightnessUpdateTimer != null) {
//            brightnessUpdateTimer.cancel();
//            brightnessUpdateTimer = null;
//        }
//
//        if (autoSaveUpdateTimer != null) {
//            autoSaveUpdateTimer.cancel();
//            autoSaveUpdateTimer = null;
//        }
//
//        // 確保所有設定都已保存
//        settingsManager.saveSettings();
//
//        dialogStage.close();
//    }
}