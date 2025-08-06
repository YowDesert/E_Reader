package E_Reader.ui;

import E_Reader.settings.SettingsManager;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

/**
 * 增強版設定對話框 - 現代化iOS風格設計
 */
public class EnhancedSettingsDialog {
    
    private final SettingsManager settingsManager;
    private final Stage parentStage;
    private Stage dialogStage;
    
    // UI 組件
    private ToggleGroup themeGroup;
    private ToggleGroup ocrGroup;
    private Slider brightnessSlider;
    private CheckBox rememberFileCheckBox;
    private CheckBox showPageNumbersCheckBox;
    private CheckBox enableTouchNavCheckBox;
    private Slider autoSaveIntervalSlider;
    
    public EnhancedSettingsDialog(SettingsManager settingsManager, Stage parentStage) {
        this.settingsManager = settingsManager;
        this.parentStage = parentStage;
        createDialog();
    }
    
    private void createDialog() {
        dialogStage = new Stage();
        dialogStage.initStyle(StageStyle.UNDECORATED);
        dialogStage.initOwner(parentStage);
        dialogStage.setTitle("設定");
        
        // 創建主要內容
        VBox mainContent = createMainContent();
        
        // 創建場景
        Scene scene = new Scene(mainContent, 600, 700);
        scene.setFill(null); // 透明背景
        
        dialogStage.setScene(scene);
        dialogStage.centerOnScreen();
    }

    private VBox createMainContent() {
        VBox mainContainer = new VBox();
        mainContainer.setAlignment(Pos.TOP_CENTER);
        mainContainer.setPadding(new Insets(20));
        // 修改：使用更明顯的深色背景，確保白色文字清楚可見
        mainContainer.setStyle(
                "-fx-background-color: linear-gradient(to bottom, " +
                        "rgba(40,40,40,0.98) 0%, " +           // 改為更淺的深灰色
                        "rgba(50,50,50,0.98) 100%); " +        // 改為更淺的深灰色
                        "-fx-border-color: rgba(255,255,255,0.3); " +  // 更明顯的白色邊框
                        "-fx-border-width: 1.5; " +            // 稍微加粗邊框
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
        // 修改：TabPane 背景改為深色
        tabPane.setStyle(
                "-fx-background-color: rgba(55,55,55,0.9); " +    // 深色背景
                        "-fx-border-radius: 12; " +
                        "-fx-background-radius: 12; " +
                        "-fx-tab-header-area-background-color: rgba(45,45,45,0.9);"  // Tab標題區域背景
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

        // 修改：設定Tab樣式為深色主題
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);

        return tabPane;
    }

    private Tab createThemeTab() {
        Tab tab = new Tab();
        tab.setClosable(false);

        VBox content = new VBox(20);
        content.setPadding(new Insets(20));
        // 修改：設定內容區域深色背景
        content.setStyle("-fx-background-color: rgba(50,50,50,0.95);");

        // 主題選擇
        VBox themeSection = createSection("🎨 外觀主題", "選擇你喜歡的閱讀風格");

        themeGroup = new ToggleGroup();
        VBox themeOptions = new VBox(10);

        for (SettingsManager.ThemeMode theme : SettingsManager.ThemeMode.values()) {
            RadioButton themeRadio = createThemeOption(theme);
            themeRadio.setToggleGroup(themeGroup);
            themeOptions.getChildren().add(themeRadio);

            if (theme == settingsManager.getCurrentTheme()) {
                themeRadio.setSelected(true);
            }
        }

        themeSection.getChildren().add(themeOptions);

        // 亮度設定
        VBox brightnessSection = createSection("🔆 顯示亮度", "調整閱讀舒適度");

        brightnessSlider = new Slider(10, 100, settingsManager.getEyeCareBrightness());
        brightnessSlider.setShowTickLabels(true);
        brightnessSlider.setShowTickMarks(true);
        brightnessSlider.setMajorTickUnit(20);
        brightnessSlider.setStyle(
                "-fx-background-color: rgba(70,70,70,0.8); " +    // 深色滑桿背景
                        "-fx-border-radius: 8; " +
                        "-fx-background-radius: 8;"
        );

        Label brightnessLabel = new Label(String.format("%.0f%%", brightnessSlider.getValue()));
        brightnessLabel.setStyle(
                "-fx-text-fill: white; " +                        // 確保是白色文字
                        "-fx-font-weight: bold; " +
                        "-fx-background-color: rgba(52,152,219,0.3); " +
                        "-fx-padding: 5 10; " +
                        "-fx-background-radius: 6;"
        );

        brightnessSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            brightnessLabel.setText(String.format("%.0f%%", newVal.doubleValue()));
        });

        HBox brightnessControl = new HBox(15);
        brightnessControl.setAlignment(Pos.CENTER_LEFT);
        brightnessControl.getChildren().addAll(brightnessSlider, brightnessLabel);
        HBox.setHgrow(brightnessSlider, Priority.ALWAYS);

        brightnessSection.getChildren().add(brightnessControl);

        content.getChildren().addAll(themeSection, createSeparator(), brightnessSection);

        ScrollPane scrollPane = new ScrollPane(content);
        scrollPane.setFitToWidth(true);
        // 修改：ScrollPane背景透明，讓深色背景顯示
        scrollPane.setStyle("-fx-background-color: transparent; -fx-background: transparent;");

        tab.setContent(scrollPane);
        return tab;
    }

    private Tab createOcrTab() {
        Tab tab = new Tab();
        tab.setClosable(false);

        VBox content = new VBox(20);
        content.setPadding(new Insets(20));
        // 修改：設定內容區域深色背景
        content.setStyle("-fx-background-color: rgba(50,50,50,0.95);");

        // OCR狀態顯示
        VBox statusSection = createSection("📊 OCR狀態", "當前文字辨識設定");

        Label statusLabel = new Label("OCR 引擎已就緒");
        statusLabel.setStyle(
                "-fx-text-fill: white; " +                        // 確保是白色文字
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
            modelOptions.getChildren().add(modelRadio);

            if (model == settingsManager.getOcrModel()) {
                modelRadio.setSelected(true);
            }
        }

        modelSection.getChildren().add(modelOptions);

        content.getChildren().addAll(statusSection, createSeparator(), modelSection);

        ScrollPane scrollPane = new ScrollPane(content);
        scrollPane.setFitToWidth(true);
        // 修改：ScrollPane背景透明
        scrollPane.setStyle("-fx-background-color: transparent; -fx-background: transparent;");

        tab.setContent(scrollPane);
        return tab;
    }

    private Tab createFunctionsTab() {
        Tab tab = new Tab();
        tab.setClosable(false);

        VBox content = new VBox(20);
        content.setPadding(new Insets(20));
        // 修改：設定內容區域深色背景
        content.setStyle("-fx-background-color: rgba(50,50,50,0.95);");

        // 文件管理
        VBox fileSection = createSection("📁 文件管理", "自訂文件處理行為");

        rememberFileCheckBox = new CheckBox("記住最後開啟的文件");
        rememberFileCheckBox.setSelected(settingsManager.isRememberLastFile());
        // 修改：確保CheckBox文字是白色
        rememberFileCheckBox.setStyle("-fx-text-fill: white; -fx-font-size: 13px;");

        fileSection.getChildren().add(rememberFileCheckBox);

        // 介面顯示
        VBox interfaceSection = createSection("🖥️ 介面顯示", "自訂使用者介面");

        showPageNumbersCheckBox = new CheckBox("顯示頁碼資訊");
        showPageNumbersCheckBox.setSelected(settingsManager.isShowPageNumbers());
        // 修改：確保CheckBox文字是白色
        showPageNumbersCheckBox.setStyle("-fx-text-fill: white; -fx-font-size: 13px;");

        interfaceSection.getChildren().add(showPageNumbersCheckBox);

        // 閱讀體驗
        VBox readingSection = createSection("📖 閱讀體驗", "優化閱讀舒適度");

        enableTouchNavCheckBox = new CheckBox("觸控導覽");
        enableTouchNavCheckBox.setSelected(settingsManager.isEnableTouchNavigation());
        // 修改：確保CheckBox文字是白色
        enableTouchNavCheckBox.setStyle("-fx-text-fill: white; -fx-font-size: 13px;");

        readingSection.getChildren().add(enableTouchNavCheckBox);

        // 自動保存設定
        VBox autoSaveSection = createSection("💾 自動保存", "設定自動保存間隔");

        autoSaveIntervalSlider = new Slider(10, 300, settingsManager.getAutoSaveInterval());
        autoSaveIntervalSlider.setShowTickLabels(true);
        autoSaveIntervalSlider.setShowTickMarks(true);
        autoSaveIntervalSlider.setMajorTickUnit(60);
        autoSaveIntervalSlider.setStyle(
                "-fx-background-color: rgba(70,70,70,0.8); " +    // 深色滑桿背景
                        "-fx-border-radius: 8; " +
                        "-fx-background-radius: 8;"
        );

        Label intervalLabel = new Label(String.format("%.0f 秒", autoSaveIntervalSlider.getValue()));
        intervalLabel.setStyle(
                "-fx-text-fill: white; " +                        // 確保是白色文字
                        "-fx-font-weight: bold; " +
                        "-fx-background-color: rgba(52,152,219,0.3); " +
                        "-fx-padding: 5 10; " +
                        "-fx-background-radius: 6;"
        );

        autoSaveIntervalSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            intervalLabel.setText(String.format("%.0f 秒", newVal.doubleValue()));
        });

        HBox intervalControl = new HBox(15);
        intervalControl.setAlignment(Pos.CENTER_LEFT);
        intervalControl.getChildren().addAll(autoSaveIntervalSlider, intervalLabel);
        HBox.setHgrow(autoSaveIntervalSlider, Priority.ALWAYS);

        autoSaveSection.getChildren().add(intervalControl);

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
        // 修改：ScrollPane背景透明
        scrollPane.setStyle("-fx-background-color: transparent; -fx-background: transparent;");

        tab.setContent(scrollPane);
        return tab;
    }

    private VBox createSection(String title, String description) {
        VBox section = new VBox(12);

        Label titleLabel = new Label(title);
        titleLabel.setStyle(
                "-fx-text-fill: white; " +                        // 確保標題是白色
                        "-fx-font-size: 16px; " +
                        "-fx-font-weight: 700;"
        );

        Label descLabel = new Label(description);
        descLabel.setStyle(
                "-fx-text-fill: rgba(255,255,255,0.8); " +        // 描述文字稍微透明的白色
                        "-fx-font-size: 12px; " +
                        "-fx-wrap-text: true;"
        );

        section.getChildren().addAll(titleLabel, descLabel);
        return section;
    }

    private RadioButton createThemeOption(SettingsManager.ThemeMode theme) {
        RadioButton radio = new RadioButton(theme.getDisplayName());
        radio.setStyle(
                "-fx-text-fill: white; " +                        // 確保RadioButton文字是白色
                        "-fx-font-size: 13px; " +
                        "-fx-font-weight: 500;"
        );
        return radio;
    }

    private RadioButton createOcrOption(SettingsManager.OcrModel model) {
        RadioButton radio = new RadioButton(model.getDisplayName());
        radio.setStyle(
                "-fx-text-fill: white; " +                        // 確保RadioButton文字是白色
                        "-fx-font-size: 13px; " +
                        "-fx-font-weight: 500;"
        );
        return radio;
    }

    private Separator createSeparator() {
        Separator separator = new Separator();
        separator.setStyle("-fx-background-color: rgba(255,255,255,0.3);"); // 更明顯的分隔線
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
        
        Button okButton = new Button("確定");
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
            saveSettings();
            close();
        });
        
        buttonBar.getChildren().addAll(cancelButton, okButton);
        return buttonBar;
    }
    
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
    }
    
    public void show() {
        dialogStage.show();
    }
    
    public void close() {
        dialogStage.close();
    }
}
