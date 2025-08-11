package E_Reader.ui;

import E_Reader.settings.SettingsManager;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Stage;

/**
 * 增強版設定對話框
 */
public class EnhancedSettingsDialog {
    
    private Stage dialogStage;
    private SettingsManager settingsManager;
    private Runnable uiUpdateCallback;
    
    // UI 元件
    private ComboBox<SettingsManager.ThemeMode> themeComboBox;
    private CheckBox showPageNumbersCheckBox;
    private CheckBox rememberLastFileCheckBox;
    private CheckBox enableTouchNavigationCheckBox;
    private Slider brightnessSlider;
    private Label brightnessLabel;

    public EnhancedSettingsDialog(SettingsManager settingsManager, Stage parentStage) {
        this.settingsManager = settingsManager;
        createDialog(parentStage);
    }

    public void setUIUpdateCallback(Runnable callback) {
        this.uiUpdateCallback = callback;
    }

    private void createDialog(Stage parentStage) {
        dialogStage = new Stage();
        dialogStage.setTitle("設定");
        dialogStage.initModality(Modality.WINDOW_MODAL);
        dialogStage.initOwner(parentStage);
        
        VBox mainContainer = new VBox(20);
        mainContainer.setPadding(new Insets(25));
        mainContainer.setStyle("-fx-background-color: #2A2A2A;");
        
        // 標題
        Label titleLabel = new Label("⚙️ 應用程式設定");
        titleLabel.setStyle(
            "-fx-text-fill: white; " +
            "-fx-font-size: 18px; " +
            "-fx-font-weight: bold; " +
            "-fx-padding: 0 0 10 0;"
        );
        
        // 主題設定區塊
        VBox themeSection = createThemeSection();
        
        // 顯示設定區塊
        VBox displaySection = createDisplaySection();
        
        // 行為設定區塊
        VBox behaviorSection = createBehaviorSection();
        
        // 分隔線
        Separator separator1 = new Separator();
        separator1.setStyle("-fx-background-color: rgba(255,255,255,0.3);");
        
        Separator separator2 = new Separator();
        separator2.setStyle("-fx-background-color: rgba(255,255,255,0.3);");
        
        // 按鈕區域
        HBox buttonBox = createButtonBox();
        
        mainContainer.getChildren().addAll(
            titleLabel,
            themeSection,
            separator1,
            displaySection,
            separator2,
            behaviorSection,
            buttonBox
        );
        
        // 滾動面板
        ScrollPane scrollPane = new ScrollPane(mainContainer);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background-color: #2A2A2A; -fx-border-color: transparent;");
        
        Scene scene = new Scene(scrollPane, 500, 600);
        
        // 嘗試載入樣式文件
        try {
            scene.getStylesheets().add(getClass().getResource("/style.css").toExternalForm());
        } catch (Exception e) {
            // 如果樣式文件不存在，使用預設樣式
            System.out.println("未找到樣式文件，使用預設樣式");
        }
        
        dialogStage.setScene(scene);
        
        // 載入目前設定值
        loadCurrentSettings();
    }

    private VBox createThemeSection() {
        VBox section = new VBox(10);
        
        Label sectionTitle = new Label("🎨 外觀主題");
        sectionTitle.setStyle(
            "-fx-text-fill: white; " +
            "-fx-font-size: 14px; " +
            "-fx-font-weight: 600;"
        );
        
        themeComboBox = new ComboBox<>();
        themeComboBox.getItems().addAll(SettingsManager.ThemeMode.values());
        themeComboBox.setStyle(
            "-fx-background-color: #3C3C3C; " +
            "-fx-text-fill: white; " +
            "-fx-border-color: #555555; " +
            "-fx-border-radius: 5px; " +
            "-fx-background-radius: 5px;"
        );
        
        // 亮度控制
        Label brightnessTitle = new Label("💡 亮度調整");
        brightnessTitle.setStyle("-fx-text-fill: white; -fx-font-size: 12px;");
        
        brightnessSlider = new Slider(10, 100, 80);
        brightnessSlider.setShowTickLabels(true);
        brightnessSlider.setShowTickMarks(true);
        brightnessSlider.setMajorTickUnit(20);
        brightnessSlider.setStyle(
            "-fx-background-color: #3C3C3C; " +
            "-fx-border-radius: 5px; " +
            "-fx-background-radius: 5px;"
        );
        
        brightnessLabel = new Label("80%");
        brightnessLabel.setStyle("-fx-text-fill: white; -fx-font-size: 12px; -fx-font-weight: bold;");
        
        // 監聽亮度變化
        brightnessSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            brightnessLabel.setText(String.format("%.0f%%", newVal.doubleValue()));
        });
        
        HBox brightnessBox = new HBox(10);
        brightnessBox.setAlignment(Pos.CENTER_LEFT);
        brightnessBox.getChildren().addAll(brightnessSlider, brightnessLabel);
        HBox.setHgrow(brightnessSlider, Priority.ALWAYS);
        
        section.getChildren().addAll(sectionTitle, themeComboBox, brightnessTitle, brightnessBox);
        return section;
    }

    private VBox createDisplaySection() {
        VBox section = new VBox(10);
        
        Label sectionTitle = new Label("🖥️ 顯示設定");
        sectionTitle.setStyle(
            "-fx-text-fill: white; " +
            "-fx-font-size: 14px; " +
            "-fx-font-weight: 600;"
        );
        
        showPageNumbersCheckBox = new CheckBox("顯示頁碼資訊");
        showPageNumbersCheckBox.setStyle("-fx-text-fill: white; -fx-font-size: 12px;");
        
        Label pageNumbersDesc = new Label("在介面上顯示目前頁數和總頁數");
        pageNumbersDesc.setStyle(
            "-fx-text-fill: rgba(255,255,255,0.7); " +
            "-fx-font-size: 10px; " +
            "-fx-padding: 0 0 0 25;"
        );
        
        section.getChildren().addAll(sectionTitle, showPageNumbersCheckBox, pageNumbersDesc);
        return section;
    }

    private VBox createBehaviorSection() {
        VBox section = new VBox(10);
        
        Label sectionTitle = new Label("⚙️ 行為設定");
        sectionTitle.setStyle(
            "-fx-text-fill: white; " +
            "-fx-font-size: 14px; " +
            "-fx-font-weight: 600;"
        );
        
        rememberLastFileCheckBox = new CheckBox("記住最後開啟的檔案");
        rememberLastFileCheckBox.setStyle("-fx-text-fill: white; -fx-font-size: 12px;");
        
        Label rememberDesc = new Label("下次啟動時自動載入上次閱讀的內容");
        rememberDesc.setStyle(
            "-fx-text-fill: rgba(255,255,255,0.7); " +
            "-fx-font-size: 10px; " +
            "-fx-padding: 0 0 0 25;"
        );
        
        enableTouchNavigationCheckBox = new CheckBox("啟用觸控導覽");
        enableTouchNavigationCheckBox.setStyle("-fx-text-fill: white; -fx-font-size: 12px;");
        
        Label touchDesc = new Label("支援觸控手勢進行頁面導航");
        touchDesc.setStyle(
            "-fx-text-fill: rgba(255,255,255,0.7); " +
            "-fx-font-size: 10px; " +
            "-fx-padding: 0 0 0 25;"
        );
        
        section.getChildren().addAll(
            sectionTitle, 
            rememberLastFileCheckBox, rememberDesc,
            enableTouchNavigationCheckBox, touchDesc
        );
        return section;
    }

    private HBox createButtonBox() {
        HBox buttonBox = new HBox(10);
        buttonBox.setAlignment(Pos.CENTER_RIGHT);
        buttonBox.setPadding(new Insets(20, 0, 0, 0));
        
        Button cancelButton = new Button("取消");
        cancelButton.setStyle(
            "-fx-background-color: #555555; " +
            "-fx-text-fill: white; " +
            "-fx-border-radius: 5px; " +
            "-fx-background-radius: 5px; " +
            "-fx-padding: 8px 16px; " +
            "-fx-cursor: hand;"
        );
        cancelButton.setOnAction(e -> dialogStage.close());
        
        Button okButton = new Button("確定");
        okButton.setStyle(
            "-fx-background-color: #0078D4; " +
            "-fx-text-fill: white; " +
            "-fx-border-radius: 5px; " +
            "-fx-background-radius: 5px; " +
            "-fx-padding: 8px 16px; " +
            "-fx-cursor: hand;"
        );
        okButton.setOnAction(e -> applySettings());
        
        buttonBox.getChildren().addAll(cancelButton, okButton);
        return buttonBox;
    }

    private void loadCurrentSettings() {
        // 載入目前的設定值
        themeComboBox.setValue(settingsManager.getCurrentTheme());
        showPageNumbersCheckBox.setSelected(settingsManager.isShowPageNumbers());
        rememberLastFileCheckBox.setSelected(settingsManager.isRememberLastFile());
        enableTouchNavigationCheckBox.setSelected(settingsManager.isEnableTouchNavigation());
        
        int currentBrightness = settingsManager.getEyeCareBrightness();
        brightnessSlider.setValue(currentBrightness);
        brightnessLabel.setText(currentBrightness + "%");
    }

    private void applySettings() {
        try {
            // 套用主題設定
            SettingsManager.ThemeMode selectedTheme = themeComboBox.getValue();
            if (selectedTheme != null) {
                settingsManager.setThemeMode(selectedTheme);
            }
            
            // 套用顯示設定
            settingsManager.setShowPageNumbers(showPageNumbersCheckBox.isSelected());
            
            // 套用行為設定
            settingsManager.setRememberLastFile(rememberLastFileCheckBox.isSelected());
            settingsManager.setEnableTouchNavigation(enableTouchNavigationCheckBox.isSelected());
            
            // 套用亮度設定
            int brightness = (int) brightnessSlider.getValue();
            settingsManager.setEyeCareBrightness(brightness);
            
            // 儲存設定
            settingsManager.saveSettings();
            
            // 觸發UI更新回調
            if (uiUpdateCallback != null) {
                Platform.runLater(() -> {
                    try {
                        uiUpdateCallback.run();
                    } catch (Exception e) {
                        System.err.println("UI更新回調執行失敗: " + e.getMessage());
                        e.printStackTrace();
                    }
                });
            }
            
            // 關閉對話框
            dialogStage.close();
            
        } catch (Exception e) {
            System.err.println("套用設定時發生錯誤: " + e.getMessage());
            e.printStackTrace();
            
            // 顯示錯誤對話框
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("設定錯誤");
            alert.setHeaderText("無法套用設定");
            alert.setContentText("套用設定時發生錯誤: " + e.getMessage());
            alert.showAndWait();
        }
    }

    /**
     * 顯示設定對話框
     */
    public void show() {
        try {
            dialogStage.show();
        } catch (Exception e) {
            System.err.println("顯示設定對話框失敗: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * 顯示設定對話框並等待
     */
    public void showAndWait() {
        try {
            dialogStage.showAndWait();
        } catch (Exception e) {
            System.err.println("顯示設定對話框失敗: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * 關閉設定對話框
     */
    public void close() {
        try {
            dialogStage.close();
        } catch (Exception e) {
            System.err.println("關閉設定對話框失敗: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
