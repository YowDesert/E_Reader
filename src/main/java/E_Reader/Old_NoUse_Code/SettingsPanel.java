//package E_Reader;
//
//import javafx.geometry.Insets;
//import javafx.geometry.Pos;
//import javafx.scene.Scene;
//import javafx.scene.control.*;
//import javafx.scene.layout.GridPane;
//import javafx.scene.layout.HBox;
//import javafx.scene.layout.VBox;
//import javafx.stage.Modality;
//import javafx.stage.Stage;
//
//import java.io.*;
//import java.util.Properties;
//
//public class SettingsPanel {
//
//    private Properties settings;
//    private final String SETTINGS_FILE = "e_reader_settings.properties";
//
//    // 設定項目
//    private ImageViewer.FitMode fitMode = ImageViewer.FitMode.FIT_WIDTH;
//    private String backgroundColor = "#1e1e1e";
//    private boolean showPageNumbers = true;
//    private boolean enableTouchNavigation = true;
//    private int autoSaveInterval = 30; // 秒
//    private double defaultZoomLevel = 1.0;
//    private boolean rememberLastFile = true;
//
//    // 新增的主題和護眼模式設定
//    private ThemeMode themeMode = ThemeMode.DARK;
//    private boolean eyeCareMode = false;
//    private int eyeCareBrightness = 80; // 0-100
//    private boolean nightMode = false;
//    private int nightModeStartHour = 20; // 晚上8點
//    private int nightModeEndHour = 7;   // 早上7點
//
//    public enum ThemeMode {
//        LIGHT("淺色模式", "#ffffff", "#000000", "#f5f5f5"),
//        DARK("深色模式", "#1e1e1e", "#ffffff", "#2b2b2b"),
//        BLACK("純黑模式", "#000000", "#e0e0e0", "#121212"),
//        EYE_CARE("護眼模式", "#1a1a0f", "#d4d4aa", "#2a2a1f"),
//        SEPIA("復古模式", "#f4ecd8", "#5d4e37", "#f0e6d2");
//
//        private final String displayName;
//        private final String backgroundColor;
//        private final String textColor;
//        private final String controlColor;
//
//        ThemeMode(String displayName, String backgroundColor, String textColor, String controlColor) {
//            this.displayName = displayName;
//            this.backgroundColor = backgroundColor;
//            this.textColor = textColor;
//            this.controlColor = controlColor;
//        }
//
//        public String getDisplayName() { return displayName; }
//        public String getBackgroundColor() { return backgroundColor; }
//        public String getTextColor() { return textColor; }
//        public String getControlColor() { return controlColor; }
//    }
//
//    public SettingsPanel() {
//        settings = new Properties();
//        loadDefaultSettings();
//    }
//
//    private void loadDefaultSettings() {
//        settings.setProperty("fitMode", "FIT_WIDTH");
//        settings.setProperty("backgroundColor", "#1e1e1e");
//        settings.setProperty("showPageNumbers", "true");
//        settings.setProperty("enableTouchNavigation", "true");
//        settings.setProperty("autoSaveInterval", "30");
//        settings.setProperty("defaultZoomLevel", "1.0");
//        settings.setProperty("rememberLastFile", "true");
//        settings.setProperty("themeMode", "DARK");
//        settings.setProperty("eyeCareMode", "false");
//        settings.setProperty("eyeCareBrightness", "80");
//        settings.setProperty("nightMode", "false");
//        settings.setProperty("nightModeStartHour", "20");
//        settings.setProperty("nightModeEndHour", "7");
//    }
//
//    public void showSettingsDialog(Stage parentStage, Runnable onSettingsChanged) {
//        Stage settingsStage = new Stage();
//        settingsStage.initModality(Modality.APPLICATION_MODAL);
//        settingsStage.initOwner(parentStage);
//        settingsStage.setTitle("設定");
//        settingsStage.setResizable(true);
//
//        VBox root = new VBox(20);
//        root.setPadding(new Insets(20));
//        applyThemeToContainer(root);
//
//        // 建立標籤頁
//        TabPane tabPane = new TabPane();
//        applyThemeToControl(tabPane);
//
//        // 一般設定標籤
//        Tab generalTab = new Tab("一般設定");
//        generalTab.setClosable(false);
//        generalTab.setContent(createGeneralSettingsForm());
//
//        // 外觀設定標籤
//        Tab themeTab = new Tab("外觀設定");
//        themeTab.setClosable(false);
//        themeTab.setContent(createThemeSettingsForm());
//
//        // 護眼設定標籤
//        Tab eyeCareTab = new Tab("護眼設定");
//        eyeCareTab.setClosable(false);
//        eyeCareTab.setContent(createEyeCareSettingsForm());
//
//        tabPane.getTabs().addAll(generalTab, themeTab, eyeCareTab);
//
//        // 按鈕區域
//        HBox buttonBox = new HBox(10);
//        buttonBox.setAlignment(Pos.CENTER_RIGHT);
//
//        Button saveBtn = new Button("儲存");
//        Button cancelBtn = new Button("取消");
//        Button resetBtn = new Button("重設為預設值");
//        Button previewBtn = new Button("預覽效果");
//
//        applyThemeToButton(saveBtn);
//        applyThemeToButton(cancelBtn);
//        applyThemeToButton(resetBtn);
//        applyThemeToButton(previewBtn);
//
//        buttonBox.getChildren().addAll(resetBtn, previewBtn, cancelBtn, saveBtn);
//
//        root.getChildren().addAll(
//                new Label("閱讀器設定") {{
//                    setStyle(getThemedTextStyle() + " -fx-font-size: 18px; -fx-font-weight: bold;");
//                }},
//                new Separator(),
//                tabPane,
//                buttonBox
//        );
//
//        // 按鈕事件
//        saveBtn.setOnAction(e -> {
//            saveSettingsFromAllForms(tabPane);
//            saveSettings();
//            onSettingsChanged.run();
//            settingsStage.close();
//        });
//
//        cancelBtn.setOnAction(e -> settingsStage.close());
//
//        resetBtn.setOnAction(e -> {
//            loadDefaultSettings();
//            updateAllFormsFromSettings(tabPane);
//            applyThemeToContainer(root);
//        });
//
//        previewBtn.setOnAction(e -> {
//            // 暫時套用設定進行預覽
//            saveSettingsFromAllForms(tabPane);
//            applyThemeToContainer(root);
//            onSettingsChanged.run();
//        });
//
//        Scene scene = new Scene(root, 500, 600);
//        settingsStage.setScene(scene);
//        settingsStage.showAndWait();
//    }
//
//    private VBox createGeneralSettingsForm() {
//        VBox container = new VBox(15);
//        container.setPadding(new Insets(20));
//        applyThemeToContainer(container);
//
//        GridPane grid = new GridPane();
//        grid.setHgap(10);
//        grid.setVgap(15);
//        grid.setAlignment(Pos.TOP_LEFT);
//
//        int row = 0;
//
//        // 顯示模式設定
//        grid.add(new Label("預設顯示模式:") {{
//            setStyle(getThemedTextStyle());
//        }}, 0, row);
//
//        ComboBox<String> fitModeCombo = new ComboBox<>();
//        fitModeCombo.getItems().addAll("適合寬度", "適合高度", "適合頁面", "原始尺寸");
//        fitModeCombo.setValue(getFitModeDisplayName(fitMode));
//        fitModeCombo.setId("fitModeCombo");
//        applyThemeToControl(fitModeCombo);
//        grid.add(fitModeCombo, 1, row++);
//
//        // 顯示頁碼
//        grid.add(new Label("顯示頁碼:") {{
//            setStyle(getThemedTextStyle());
//        }}, 0, row);
//
//        CheckBox showPageNumCheck = new CheckBox();
//        showPageNumCheck.setSelected(showPageNumbers);
//        showPageNumCheck.setId("showPageNumCheck");
//        applyThemeToControl(showPageNumCheck);
//        grid.add(showPageNumCheck, 1, row++);
//
//        // 觸控導航
//        grid.add(new Label("啟用觸控導航:") {{
//            setStyle(getThemedTextStyle());
//        }}, 0, row);
//
//        CheckBox touchNavCheck = new CheckBox();
//        touchNavCheck.setSelected(enableTouchNavigation);
//        touchNavCheck.setId("touchNavCheck");
//        applyThemeToControl(touchNavCheck);
//        grid.add(touchNavCheck, 1, row++);
//
//        // 預設縮放級別
//        grid.add(new Label("預設縮放級別:") {{
//            setStyle(getThemedTextStyle());
//        }}, 0, row);
//
//        Slider zoomSlider = new Slider(0.5, 3.0, defaultZoomLevel);
//        zoomSlider.setShowTickLabels(true);
//        zoomSlider.setShowTickMarks(true);
//        zoomSlider.setMajorTickUnit(0.5);
//        zoomSlider.setId("zoomSlider");
//        applyThemeToControl(zoomSlider);
//
//        Label zoomLabel = new Label(String.format("%.1fx", defaultZoomLevel));
//        zoomLabel.setStyle(getThemedTextStyle());
//        zoomLabel.setId("zoomLabel");
//
//        zoomSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
//            zoomLabel.setText(String.format("%.1fx", newVal.doubleValue()));
//        });
//
//        VBox zoomBox = new VBox(5, zoomSlider, zoomLabel);
//        grid.add(zoomBox, 1, row++);
//
//        // 記住上次開啟的檔案
//        grid.add(new Label("記住上次開啟的檔案:") {{
//            setStyle(getThemedTextStyle());
//        }}, 0, row);
//
//        CheckBox rememberFileCheck = new CheckBox();
//        rememberFileCheck.setSelected(rememberLastFile);
//        rememberFileCheck.setId("rememberFileCheck");
//        applyThemeToControl(rememberFileCheck);
//        grid.add(rememberFileCheck, 1, row++);
//
//        container.getChildren().add(grid);
//        return container;
//    }
//
//    private VBox createThemeSettingsForm() {
//        VBox container = new VBox(15);
//        container.setPadding(new Insets(20));
//        applyThemeToContainer(container);
//
//        GridPane grid = new GridPane();
//        grid.setHgap(10);
//        grid.setVgap(15);
//        grid.setAlignment(Pos.TOP_LEFT);
//
//        int row = 0;
//
//        // 主題模式選擇
//        grid.add(new Label("主題模式:") {{
//            setStyle(getThemedTextStyle());
//        }}, 0, row);
//
//        ComboBox<ThemeMode> themeCombo = new ComboBox<>();
//        themeCombo.getItems().addAll(ThemeMode.values());
//        themeCombo.setValue(themeMode);
//        themeCombo.setId("themeCombo");
//        applyThemeToControl(themeCombo);
//
//        // 自訂選項顯示
//        themeCombo.setCellFactory(lv -> new ListCell<ThemeMode>() {
//            @Override
//            protected void updateItem(ThemeMode item, boolean empty) {
//                super.updateItem(item, empty);
//                setText(empty ? null : item.getDisplayName());
//            }
//        });
//        themeCombo.setButtonCell(new ListCell<ThemeMode>() {
//            @Override
//            protected void updateItem(ThemeMode item, boolean empty) {
//                super.updateItem(item, empty);
//                setText(empty ? null : item.getDisplayName());
//            }
//        });
//
//        grid.add(themeCombo, 1, row++);
//
//        // 主題預覽
//        VBox previewBox = new VBox(10);
//        previewBox.setPadding(new Insets(15));
//        previewBox.setStyle(String.format("-fx-background-color: %s; -fx-border-color: %s; -fx-border-width: 1px;",
//                themeMode.getBackgroundColor(), themeMode.getControlColor()));
//
//        Label previewTitle = new Label("預覽效果");
//        previewTitle.setStyle(String.format("-fx-text-fill: %s; -fx-font-weight: bold;", themeMode.getTextColor()));
//
//        Label previewText = new Label("這是文字顯示效果的預覽");
//        previewText.setStyle(String.format("-fx-text-fill: %s;", themeMode.getTextColor()));
//
//        Button previewButton = new Button("按鈕樣式");
//        previewButton.setStyle(String.format("-fx-background-color: %s; -fx-text-fill: %s;",
//                themeMode.getControlColor(), themeMode.getTextColor()));
//
//        previewBox.getChildren().addAll(previewTitle, previewText, previewButton);
//        previewBox.setId("themePreview");
//
//        grid.add(new Label("預覽:") {{
//            setStyle(getThemedTextStyle());
//        }}, 0, row);
//        grid.add(previewBox, 1, row++);
//
//        // 主題變更監聽
//        themeCombo.valueProperty().addListener((obs, oldVal, newVal) -> {
//            if (newVal != null) {
//                themeMode = newVal;
//                updateThemePreview(previewBox);
//            }
//        });
//
//        container.getChildren().add(grid);
//        return container;
//    }
//
//    private VBox createEyeCareSettingsForm() {
//        VBox container = new VBox(15);
//        container.setPadding(new Insets(20));
//        applyThemeToContainer(container);
//
//        GridPane grid = new GridPane();
//        grid.setHgap(10);
//        grid.setVgap(15);
//        grid.setAlignment(Pos.TOP_LEFT);
//
//        int row = 0;
//
//        // 護眼模式開關
//        grid.add(new Label("啟用護眼模式:") {{
//            setStyle(getThemedTextStyle());
//        }}, 0, row);
//
//        CheckBox eyeCareModeCheck = new CheckBox();
//        eyeCareModeCheck.setSelected(eyeCareMode);
//        eyeCareModeCheck.setId("eyeCareModeCheck");
//        applyThemeToControl(eyeCareModeCheck);
//        grid.add(eyeCareModeCheck, 1, row++);
//
//        // 護眼亮度調整
//        grid.add(new Label("護眼亮度:") {{
//            setStyle(getThemedTextStyle());
//        }}, 0, row);
//
//        Slider brightnessSlider = new Slider(20, 100, eyeCareBrightness);
//        brightnessSlider.setShowTickLabels(true);
//        brightnessSlider.setShowTickMarks(true);
//        brightnessSlider.setMajorTickUnit(20);
//        brightnessSlider.setId("brightnessSlider");
//        applyThemeToControl(brightnessSlider);
//
//        Label brightnessLabel = new Label(eyeCareBrightness + "%");
//        brightnessLabel.setStyle(getThemedTextStyle());
//        brightnessLabel.setId("brightnessLabel");
//
//        brightnessSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
//            brightnessLabel.setText(newVal.intValue() + "%");
//        });
//
//        VBox brightnessBox = new VBox(5, brightnessSlider, brightnessLabel);
//        grid.add(brightnessBox, 1, row++);
//
//        // 夜間模式
//        grid.add(new Label("自動夜間模式:") {{
//            setStyle(getThemedTextStyle());
//        }}, 0, row);
//
//        CheckBox nightModeCheck = new CheckBox();
//        nightModeCheck.setSelected(nightMode);
//        nightModeCheck.setId("nightModeCheck");
//        applyThemeToControl(nightModeCheck);
//        grid.add(nightModeCheck, 1, row++);
//
//        // 夜間模式時間設定
//        grid.add(new Label("夜間模式時間:") {{
//            setStyle(getThemedTextStyle());
//        }}, 0, row);
//
//        HBox timeBox = new HBox(10);
//        timeBox.setAlignment(Pos.CENTER_LEFT);
//
//        Spinner<Integer> startHourSpinner = new Spinner<>(0, 23, nightModeStartHour);
//        startHourSpinner.setId("startHourSpinner");
//        startHourSpinner.setPrefWidth(80);
//        applyThemeToControl(startHourSpinner);
//
//        Label toLabel = new Label("到");
//        toLabel.setStyle(getThemedTextStyle());
//
//        Spinner<Integer> endHourSpinner = new Spinner<>(0, 23, nightModeEndHour);
//        endHourSpinner.setId("endHourSpinner");
//        endHourSpinner.setPrefWidth(80);
//        applyThemeToControl(endHourSpinner);
//
//        Label hourLabel = new Label("時");
//        hourLabel.setStyle(getThemedTextStyle());
//
//        timeBox.getChildren().addAll(startHourSpinner, toLabel, endHourSpinner, hourLabel);
//        grid.add(timeBox, 1, row++);
//
//        // 護眼建議
//        VBox tipsBox = new VBox(10);
//        tipsBox.setPadding(new Insets(15));
//        tipsBox.setStyle("-fx-background-color: rgba(76, 175, 80, 0.1); -fx-border-color: #4CAF50; -fx-border-width: 1px; -fx-border-radius: 5px;");
//
//        Label tipsTitle = new Label("💡 護眼小貼士");
//        tipsTitle.setStyle("-fx-text-fill: #4CAF50; -fx-font-weight: bold;");
//
//        Label tips = new Label("• 建議每閱讀30分鐘休息5-10分鐘\n• 保持適當的閱讀距離（約50-70cm）\n• 確保環境光線充足\n• 護眼模式可減少藍光刺激");
//        tips.setStyle(getThemedTextStyle() + " -fx-font-size: 12px;");
//        tips.setWrapText(true);
//
//        tipsBox.getChildren().addAll(tipsTitle, tips);
//
//        container.getChildren().addAll(grid, tipsBox);
//        return container;
//    }
//
//    private void updateThemePreview(VBox previewBox) {
//        previewBox.setStyle(String.format("-fx-background-color: %s; -fx-border-color: %s; -fx-border-width: 1px;",
//                themeMode.getBackgroundColor(), themeMode.getControlColor()));
//
//        previewBox.getChildren().forEach(node -> {
//            if (node instanceof Label) {
//                Label label = (Label) node;
//                if (label.getText().equals("預覽效果")) {
//                    label.setStyle(String.format("-fx-text-fill: %s; -fx-font-weight: bold;", themeMode.getTextColor()));
//                } else {
//                    label.setStyle(String.format("-fx-text-fill: %s;", themeMode.getTextColor()));
//                }
//            } else if (node instanceof Button) {
//                Button button = (Button) node;
//                button.setStyle(String.format("-fx-background-color: %s; -fx-text-fill: %s;",
//                        themeMode.getControlColor(), themeMode.getTextColor()));
//            }
//        });
//    }
//
//    private void saveSettingsFromAllForms(TabPane tabPane) {
//        // 儲存一般設定
//        Tab generalTab = tabPane.getTabs().get(0);
//        VBox generalContainer = (VBox) generalTab.getContent();
//        GridPane generalGrid = (GridPane) generalContainer.getChildren().get(0);
//        saveGeneralSettings(generalGrid);
//
//        // 儲存主題設定
//        Tab themeTab = tabPane.getTabs().get(1);
//        VBox themeContainer = (VBox) themeTab.getContent();
//        GridPane themeGrid = (GridPane) themeContainer.getChildren().get(0);
//        saveThemeSettings(themeGrid);
//
//        // 儲存護眼設定
//        Tab eyeCareTab = tabPane.getTabs().get(2);
//        VBox eyeCareContainer = (VBox) eyeCareTab.getContent();
//        GridPane eyeCareGrid = (GridPane) eyeCareContainer.getChildren().get(0);
//        saveEyeCareSettings(eyeCareGrid);
//    }
//
//    private void saveGeneralSettings(GridPane grid) {
//        ComboBox<String> fitModeCombo = (ComboBox<String>) grid.lookup("#fitModeCombo");
//        fitMode = getFitModeFromDisplayName(fitModeCombo.getValue());
//
//        CheckBox showPageNumCheck = (CheckBox) grid.lookup("#showPageNumCheck");
//        showPageNumbers = showPageNumCheck.isSelected();
//
//        CheckBox touchNavCheck = (CheckBox) grid.lookup("#touchNavCheck");
//        enableTouchNavigation = touchNavCheck.isSelected();
//
//        Slider zoomSlider = (Slider) grid.lookup("#zoomSlider");
//        defaultZoomLevel = zoomSlider.getValue();
//
//        CheckBox rememberFileCheck = (CheckBox) grid.lookup("#rememberFileCheck");
//        rememberLastFile = rememberFileCheck.isSelected();
//
//        // 更新 Properties
//        settings.setProperty("fitMode", fitMode.toString());
//        settings.setProperty("showPageNumbers", String.valueOf(showPageNumbers));
//        settings.setProperty("enableTouchNavigation", String.valueOf(enableTouchNavigation));
//        settings.setProperty("defaultZoomLevel", String.valueOf(defaultZoomLevel));
//        settings.setProperty("rememberLastFile", String.valueOf(rememberLastFile));
//    }
//
//    private void saveThemeSettings(GridPane grid) {
//        ComboBox<ThemeMode> themeCombo = (ComboBox<ThemeMode>) grid.lookup("#themeCombo");
//        themeMode = themeCombo.getValue();
//        backgroundColor = themeMode.getBackgroundColor();
//
//        settings.setProperty("themeMode", themeMode.toString());
//        settings.setProperty("backgroundColor", backgroundColor);
//    }
//
//    private void saveEyeCareSettings(GridPane grid) {
//        CheckBox eyeCareModeCheck = (CheckBox) grid.lookup("#eyeCareModeCheck");
//        eyeCareMode = eyeCareModeCheck.isSelected();
//
//        Slider brightnessSlider = (Slider) grid.lookup("#brightnessSlider");
//        eyeCareBrightness = (int) brightnessSlider.getValue();
//
//        CheckBox nightModeCheck = (CheckBox) grid.lookup("#nightModeCheck");
//        nightMode = nightModeCheck.isSelected();
//
//        Spinner<Integer> startHourSpinner = (Spinner<Integer>) grid.lookup("#startHourSpinner");
//        nightModeStartHour = startHourSpinner.getValue();
//
//        Spinner<Integer> endHourSpinner = (Spinner<Integer>) grid.lookup("#endHourSpinner");
//        nightModeEndHour = endHourSpinner.getValue();
//
//        settings.setProperty("eyeCareMode", String.valueOf(eyeCareMode));
//        settings.setProperty("eyeCareBrightness", String.valueOf(eyeCareBrightness));
//        settings.setProperty("nightMode", String.valueOf(nightMode));
//        settings.setProperty("nightModeStartHour", String.valueOf(nightModeStartHour));
//        settings.setProperty("nightModeEndHour", String.valueOf(nightModeEndHour));
//    }
//
//    private void updateAllFormsFromSettings(TabPane tabPane) {
//        // 更新所有表單控制項
//        // 實作省略，類似於之前的 updateFormFromSettings 方法
//    }
//
//    // 主題相關的輔助方法
//    private void applyThemeToContainer(VBox container) {
//        container.setStyle(String.format("-fx-background-color: %s;", themeMode.getBackgroundColor()));
//    }
//
//    private void applyThemeToControl(Control control) {
//        String baseStyle = String.format(
//                "-fx-background-color: %s; -fx-text-fill: %s; -fx-border-color: %s;",
//                themeMode.getControlColor(), themeMode.getTextColor(), "#666666"
//        );
//        control.setStyle(baseStyle);
//    }
//
//    private void applyThemeToButton(Button button) {
//        String buttonStyle = String.format(
//                "-fx-background-color: %s; -fx-text-fill: %s; " +
//                        "-fx-border-radius: 5; -fx-background-radius: 5; " +
//                        "-fx-padding: 8 15 8 15;",
//                themeMode.getControlColor(), themeMode.getTextColor()
//        );
//        button.setStyle(buttonStyle);
//    }
//
//    private String getThemedTextStyle() {
//        return String.format("-fx-text-fill: %s;", themeMode.getTextColor());
//    }
//
//    // 現有的輔助方法保持不變
//    private String getFitModeDisplayName(ImageViewer.FitMode mode) {
//        switch (mode) {
//            case FIT_WIDTH: return "適合寬度";
//            case FIT_HEIGHT: return "適合高度";
//            case FIT_PAGE: return "適合頁面";
//            case ORIGINAL_SIZE: return "原始尺寸";
//            default: return "適合寬度";
//        }
//    }
//
//    private ImageViewer.FitMode getFitModeFromDisplayName(String displayName) {
//        switch (displayName) {
//            case "適合寬度": return ImageViewer.FitMode.FIT_WIDTH;
//            case "適合高度": return ImageViewer.FitMode.FIT_HEIGHT;
//            case "適合頁面": return ImageViewer.FitMode.FIT_PAGE;
//            case "原始尺寸": return ImageViewer.FitMode.ORIGINAL_SIZE;
//            default: return ImageViewer.FitMode.FIT_WIDTH;
//        }
//    }
//
//    public void loadSettings() {
//        try (InputStream input = new FileInputStream(SETTINGS_FILE)) {
//            settings.load(input);
//
//            // 從 Properties 載入設定
//            String fitModeStr = settings.getProperty("fitMode", "FIT_WIDTH");
//            try {
//                fitMode = ImageViewer.FitMode.valueOf(fitModeStr);
//            } catch (IllegalArgumentException e) {
//                fitMode = ImageViewer.FitMode.FIT_WIDTH;
//            }
//
//            String themeModeStr = settings.getProperty("themeMode", "DARK");
//            try {
//                themeMode = ThemeMode.valueOf(themeModeStr);
//            } catch (IllegalArgumentException e) {
//                themeMode = ThemeMode.DARK;
//            }
//
//            backgroundColor = settings.getProperty("backgroundColor", themeMode.getBackgroundColor());
//            showPageNumbers = Boolean.parseBoolean(settings.getProperty("showPageNumbers", "true"));
//            enableTouchNavigation = Boolean.parseBoolean(settings.getProperty("enableTouchNavigation", "true"));
//            eyeCareMode = Boolean.parseBoolean(settings.getProperty("eyeCareMode", "false"));
//            nightMode = Boolean.parseBoolean(settings.getProperty("nightMode", "false"));
//
//            try {
//                defaultZoomLevel = Double.parseDouble(settings.getProperty("defaultZoomLevel", "1.0"));
//                eyeCareBrightness = Integer.parseInt(settings.getProperty("eyeCareBrightness", "80"));
//                nightModeStartHour = Integer.parseInt(settings.getProperty("nightModeStartHour", "20"));
//                nightModeEndHour = Integer.parseInt(settings.getProperty("nightModeEndHour", "7"));
//            } catch (NumberFormatException e) {
//                defaultZoomLevel = 1.0;
//                eyeCareBrightness = 80;
//                nightModeStartHour = 20;
//                nightModeEndHour = 7;
//            }
//
//            rememberLastFile = Boolean.parseBoolean(settings.getProperty("rememberLastFile", "true"));
//
//        } catch (IOException e) {
//            loadDefaultSettings();
//        }
//    }
//
//    public void saveSettings() {
//        try (OutputStream output = new FileOutputStream(SETTINGS_FILE)) {
//            settings.store(output, "E-Reader Settings");
//        } catch (IOException e) {
//            System.err.println("無法儲存設定檔: " + e.getMessage());
//        }
//    }
//
//    // Getter 方法
//    public ImageViewer.FitMode getFitMode() { return fitMode; }
//    public String getBackgroundColor() { return backgroundColor; }
//    public boolean isShowPageNumbers() { return showPageNumbers; }
//    public boolean isEnableTouchNavigation() { return enableTouchNavigation; }
//    public double getDefaultZoomLevel() { return defaultZoomLevel; }
//    public boolean isRememberLastFile() { return rememberLastFile; }
//    public ThemeMode getThemeMode() { return themeMode; }
//    public boolean isEyeCareMode() { return eyeCareMode; }
//    public int getEyeCareBrightness() { return eyeCareBrightness; }
//    public boolean isNightMode() { return nightMode; }
//    public int getNightModeStartHour() { return nightModeStartHour; }
//    public int getNightModeEndHour() { return nightModeEndHour; }
//
//    // Setter 方法
//    public void setThemeMode(ThemeMode themeMode) {
//        this.themeMode = themeMode;
//        this.backgroundColor = themeMode.getBackgroundColor();
//        settings.setProperty("themeMode", themeMode.toString());
//        settings.setProperty("backgroundColor", backgroundColor);
//    }
//
//    public void setEyeCareMode(boolean eyeCareMode) {
//        this.eyeCareMode = eyeCareMode;
//        settings.setProperty("eyeCareMode", String.valueOf(eyeCareMode));
//    }
//
//    // 檢查是否應該啟用夜間模式
//    public boolean shouldEnableNightMode() {
//        if (!nightMode) return false;
//
//        java.time.LocalTime now = java.time.LocalTime.now();
//        int currentHour = now.getHour();
//
//        if (nightModeStartHour <= nightModeEndHour) {
//            return currentHour >= nightModeStartHour && currentHour < nightModeEndHour;
//        } else {
//            // 跨夜情況，例如 22:00 到 6:00
//            return currentHour >= nightModeStartHour || currentHour < nightModeEndHour;
//        }
//    }
//
//    // 獲取當前應該使用的主題（考慮夜間模式和護眼模式）
//    public ThemeMode getCurrentTheme() {
//        if (shouldEnableNightMode()) {
//            return ThemeMode.BLACK; // 夜間自動切換到純黑模式
//        }
//        if (eyeCareMode) {
//            return ThemeMode.EYE_CARE; // 護眼模式
//        }
//        return themeMode; // 使用者選擇的主題
//    }
//}