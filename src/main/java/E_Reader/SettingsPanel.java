package E_Reader;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.*;
import java.util.Properties;

public class SettingsPanel {

    private Properties settings;
    private final String SETTINGS_FILE = "e_reader_settings.properties";

    // 設定項目
    private ImageViewer.FitMode fitMode = ImageViewer.FitMode.FIT_WIDTH;
    private String backgroundColor = "#1e1e1e";
    private boolean showPageNumbers = true;
    private boolean enableTouchNavigation = true;
    private int autoSaveInterval = 30; // 秒
    private double defaultZoomLevel = 1.0;
    private boolean rememberLastFile = true;

    public SettingsPanel() {
        settings = new Properties();
        loadDefaultSettings();
    }

    private void loadDefaultSettings() {
        settings.setProperty("fitMode", "FIT_WIDTH");
        settings.setProperty("backgroundColor", "#1e1e1e");
        settings.setProperty("showPageNumbers", "true");
        settings.setProperty("enableTouchNavigation", "true");
        settings.setProperty("autoSaveInterval", "30");
        settings.setProperty("defaultZoomLevel", "1.0");
        settings.setProperty("rememberLastFile", "true");
    }

    public void showSettingsDialog(Stage parentStage, Runnable onSettingsChanged) {
        Stage settingsStage = new Stage();
        settingsStage.initModality(Modality.APPLICATION_MODAL);
        settingsStage.initOwner(parentStage);
        settingsStage.setTitle("設定");
        settingsStage.setResizable(false);

        VBox root = new VBox(20);
        root.setPadding(new Insets(20));
        root.setStyle("-fx-background-color: #2b2b2b;");

        // 建立設定表單
        GridPane formGrid = createSettingsForm();

        // 按鈕區域
        HBox buttonBox = new HBox(10);
        buttonBox.setAlignment(Pos.CENTER_RIGHT);

        Button saveBtn = new Button("儲存");
        Button cancelBtn = new Button("取消");
        Button resetBtn = new Button("重設為預設值");

        String buttonStyle = "-fx-background-color: #404040; -fx-text-fill: white; " +
                "-fx-border-radius: 5; -fx-background-radius: 5; " +
                "-fx-padding: 8 15 8 15;";

        saveBtn.setStyle(buttonStyle);
        cancelBtn.setStyle(buttonStyle);
        resetBtn.setStyle(buttonStyle);

        buttonBox.getChildren().addAll(resetBtn, cancelBtn, saveBtn);

        root.getChildren().addAll(
                new Label("閱讀器設定") {{
                    setStyle("-fx-text-fill: white; -fx-font-size: 18px; -fx-font-weight: bold;");
                }},
                new Separator(),
                formGrid,
                buttonBox
        );

        // 按鈕事件
        saveBtn.setOnAction(e -> {
            saveSettingsFromForm(formGrid);
            saveSettings();
            onSettingsChanged.run();
            settingsStage.close();
        });

        cancelBtn.setOnAction(e -> settingsStage.close());

        resetBtn.setOnAction(e -> {
            loadDefaultSettings();
            updateFormFromSettings(formGrid);
        });

        Scene scene = new Scene(root, 400, 500);
        settingsStage.setScene(scene);
        settingsStage.showAndWait();
    }

    @SuppressWarnings("unchecked")
    private GridPane createSettingsForm() {
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(15);
        grid.setAlignment(Pos.TOP_LEFT);

        int row = 0;

        // 顯示模式設定
        grid.add(new Label("預設顯示模式:") {{
            setStyle("-fx-text-fill: white;");
        }}, 0, row);

        ComboBox<String> fitModeCombo = new ComboBox<>();
        fitModeCombo.getItems().addAll("適合寬度", "適合高度", "適合頁面", "原始尺寸");
        fitModeCombo.setValue(getFitModeDisplayName(fitMode));
        fitModeCombo.setId("fitModeCombo");
        fitModeCombo.setStyle("-fx-background-color: #404040; -fx-text-fill: white;");
        grid.add(fitModeCombo, 1, row++);

        // 背景顏色設定
        grid.add(new Label("背景顏色:") {{
            setStyle("-fx-text-fill: white;");
        }}, 0, row);

        ComboBox<String> backgroundCombo = new ComboBox<>();
        backgroundCombo.getItems().addAll("深灰色", "黑色", "白色", "米色");
        backgroundCombo.setValue(getBackgroundDisplayName(backgroundColor));
        backgroundCombo.setId("backgroundCombo");
        backgroundCombo.setStyle("-fx-background-color: #404040; -fx-text-fill: white;");
        grid.add(backgroundCombo, 1, row++);

        // 顯示頁碼
        grid.add(new Label("顯示頁碼:") {{
            setStyle("-fx-text-fill: white;");
        }}, 0, row);

        CheckBox showPageNumCheck = new CheckBox();
        showPageNumCheck.setSelected(showPageNumbers);
        showPageNumCheck.setId("showPageNumCheck");
        grid.add(showPageNumCheck, 1, row++);

        // 觸控導航
        grid.add(new Label("啟用觸控導航:") {{
            setStyle("-fx-text-fill: white;");
        }}, 0, row);

        CheckBox touchNavCheck = new CheckBox();
        touchNavCheck.setSelected(enableTouchNavigation);
        touchNavCheck.setId("touchNavCheck");
        grid.add(touchNavCheck, 1, row++);

        // 預設縮放級別
        grid.add(new Label("預設縮放級別:") {{
            setStyle("-fx-text-fill: white;");
        }}, 0, row);

        Slider zoomSlider = new Slider(0.5, 3.0, defaultZoomLevel);
        zoomSlider.setShowTickLabels(true);
        zoomSlider.setShowTickMarks(true);
        zoomSlider.setMajorTickUnit(0.5);
        zoomSlider.setId("zoomSlider");
        zoomSlider.setStyle("-fx-control-inner-background: #404040;");

        Label zoomLabel = new Label(String.format("%.1fx", defaultZoomLevel));
        zoomLabel.setStyle("-fx-text-fill: white;");
        zoomLabel.setId("zoomLabel");

        zoomSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            zoomLabel.setText(String.format("%.1fx", newVal.doubleValue()));
        });

        VBox zoomBox = new VBox(5, zoomSlider, zoomLabel);
        grid.add(zoomBox, 1, row++);

        // 記住上次開啟的檔案
        grid.add(new Label("記住上次開啟的檔案:") {{
            setStyle("-fx-text-fill: white;");
        }}, 0, row);

        CheckBox rememberFileCheck = new CheckBox();
        rememberFileCheck.setSelected(rememberLastFile);
        rememberFileCheck.setId("rememberFileCheck");
        grid.add(rememberFileCheck, 1, row++);

        return grid;
    }

    private void saveSettingsFromForm(GridPane grid) {
        // 從表單控制項讀取設定值
        ComboBox<String> fitModeCombo = (ComboBox<String>) grid.lookup("#fitModeCombo");
        fitMode = getFitModeFromDisplayName(fitModeCombo.getValue());

        ComboBox<String> backgroundCombo = (ComboBox<String>) grid.lookup("#backgroundCombo");
        backgroundColor = getBackgroundColorFromDisplayName(backgroundCombo.getValue());

        CheckBox showPageNumCheck = (CheckBox) grid.lookup("#showPageNumCheck");
        showPageNumbers = showPageNumCheck.isSelected();

        CheckBox touchNavCheck = (CheckBox) grid.lookup("#touchNavCheck");
        enableTouchNavigation = touchNavCheck.isSelected();

        Slider zoomSlider = (Slider) grid.lookup("#zoomSlider");
        defaultZoomLevel = zoomSlider.getValue();

        CheckBox rememberFileCheck = (CheckBox) grid.lookup("#rememberFileCheck");
        rememberLastFile = rememberFileCheck.isSelected();

        // 更新 Properties
        settings.setProperty("fitMode", fitMode.toString());
        settings.setProperty("backgroundColor", backgroundColor);
        settings.setProperty("showPageNumbers", String.valueOf(showPageNumbers));
        settings.setProperty("enableTouchNavigation", String.valueOf(enableTouchNavigation));
        settings.setProperty("defaultZoomLevel", String.valueOf(defaultZoomLevel));
        settings.setProperty("rememberLastFile", String.valueOf(rememberLastFile));
    }

    private void updateFormFromSettings(GridPane grid) {
        ComboBox<String> fitModeCombo = (ComboBox<String>) grid.lookup("#fitModeCombo");
        fitModeCombo.setValue(getFitModeDisplayName(fitMode));

        ComboBox<String> backgroundCombo = (ComboBox<String>) grid.lookup("#backgroundCombo");
        backgroundCombo.setValue(getBackgroundDisplayName(backgroundColor));

        CheckBox showPageNumCheck = (CheckBox) grid.lookup("#showPageNumCheck");
        showPageNumCheck.setSelected(showPageNumbers);

        CheckBox touchNavCheck = (CheckBox) grid.lookup("#touchNavCheck");
        touchNavCheck.setSelected(enableTouchNavigation);

        Slider zoomSlider = (Slider) grid.lookup("#zoomSlider");
        zoomSlider.setValue(defaultZoomLevel);

        Label zoomLabel = (Label) grid.lookup("#zoomLabel");
        zoomLabel.setText(String.format("%.1fx", defaultZoomLevel));

        CheckBox rememberFileCheck = (CheckBox) grid.lookup("#rememberFileCheck");
        rememberFileCheck.setSelected(rememberLastFile);
    }

    private String getFitModeDisplayName(ImageViewer.FitMode mode) {
        switch (mode) {
            case FIT_WIDTH: return "適合寬度";
            case FIT_HEIGHT: return "適合高度";
            case FIT_PAGE: return "適合頁面";
            case ORIGINAL_SIZE: return "原始尺寸";
            default: return "適合寬度";
        }
    }

    private ImageViewer.FitMode getFitModeFromDisplayName(String displayName) {
        switch (displayName) {
            case "適合寬度": return ImageViewer.FitMode.FIT_WIDTH;
            case "適合高度": return ImageViewer.FitMode.FIT_HEIGHT;
            case "適合頁面": return ImageViewer.FitMode.FIT_PAGE;
            case "原始尺寸": return ImageViewer.FitMode.ORIGINAL_SIZE;
            default: return ImageViewer.FitMode.FIT_WIDTH;
        }
    }

    private String getBackgroundDisplayName(String color) {
        switch (color) {
            case "#1e1e1e": return "深灰色";
            case "#000000": return "黑色";
            case "#ffffff": return "白色";
            case "#f5f5dc": return "米色";
            default: return "深灰色";
        }
    }

    private String getBackgroundColorFromDisplayName(String displayName) {
        switch (displayName) {
            case "深灰色": return "#1e1e1e";
            case "黑色": return "#000000";
            case "白色": return "#ffffff";
            case "米色": return "#f5f5dc";
            default: return "#1e1e1e";
        }
    }

    public void loadSettings() {
        try (InputStream input = new FileInputStream(SETTINGS_FILE)) {
            settings.load(input);

            // 從 Properties 載入設定
            String fitModeStr = settings.getProperty("fitMode", "FIT_WIDTH");
            try {
                fitMode = ImageViewer.FitMode.valueOf(fitModeStr);
            } catch (IllegalArgumentException e) {
                fitMode = ImageViewer.FitMode.FIT_WIDTH;
            }

            backgroundColor = settings.getProperty("backgroundColor", "#1e1e1e");
            showPageNumbers = Boolean.parseBoolean(settings.getProperty("showPageNumbers", "true"));
            enableTouchNavigation = Boolean.parseBoolean(settings.getProperty("enableTouchNavigation", "true"));

            try {
                defaultZoomLevel = Double.parseDouble(settings.getProperty("defaultZoomLevel", "1.0"));
            } catch (NumberFormatException e) {
                defaultZoomLevel = 1.0;
            }

            rememberLastFile = Boolean.parseBoolean(settings.getProperty("rememberLastFile", "true"));

        } catch (IOException e) {
            // 設定檔不存在或讀取失敗，使用預設值
            loadDefaultSettings();
        }
    }

    public void saveSettings() {
        try (OutputStream output = new FileOutputStream(SETTINGS_FILE)) {
            settings.store(output, "E-Reader Settings");
        } catch (IOException e) {
            System.err.println("無法儲存設定檔: " + e.getMessage());
        }
    }

    // Getter 方法
    public ImageViewer.FitMode getFitMode() {
        return fitMode;
    }

    public String getBackgroundColor() {
        return backgroundColor;
    }

    public boolean isShowPageNumbers() {
        return showPageNumbers;
    }

    public boolean isEnableTouchNavigation() {
        return enableTouchNavigation;
    }

    public double getDefaultZoomLevel() {
        return defaultZoomLevel;
    }

    public boolean isRememberLastFile() {
        return rememberLastFile;
    }

    // Setter 方法
    public void setFitMode(ImageViewer.FitMode fitMode) {
        this.fitMode = fitMode;
        settings.setProperty("fitMode", fitMode.toString());
    }

    public void setBackgroundColor(String backgroundColor) {
        this.backgroundColor = backgroundColor;
        settings.setProperty("backgroundColor", backgroundColor);
    }

    public void setShowPageNumbers(boolean showPageNumbers) {
        this.showPageNumbers = showPageNumbers;
        settings.setProperty("showPageNumbers", String.valueOf(showPageNumbers));
    }

    public void setEnableTouchNavigation(boolean enableTouchNavigation) {
        this.enableTouchNavigation = enableTouchNavigation;
        settings.setProperty("enableTouchNavigation", String.valueOf(enableTouchNavigation));
    }

    public void setDefaultZoomLevel(double defaultZoomLevel) {
        this.defaultZoomLevel = defaultZoomLevel;
        settings.setProperty("defaultZoomLevel", String.valueOf(defaultZoomLevel));
    }

    public void setRememberLastFile(boolean rememberLastFile) {
        this.rememberLastFile = rememberLastFile;
        settings.setProperty("rememberLastFile", String.valueOf(rememberLastFile));
    }
}