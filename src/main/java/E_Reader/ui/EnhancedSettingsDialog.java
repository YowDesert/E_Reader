package E_Reader.ui;

import E_Reader.settings.SettingsManager;
import javafx.animation.*;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Separator;
import javafx.scene.control.Slider;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.layout.Region;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
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

        // 右側：預覽區域（包含主題+亮度）
        VBox previewArea = createThemePreviewArea();
        previewArea.setPrefWidth(280);

        // 主題選擇區塊
        VBox themeSection = createSection("🎨 外觀主題", "選擇你喜歡的閱讀風格");

        themeGroup = new ToggleGroup();
        VBox themeOptions = new VBox(10);

        for (SettingsManager.ThemeMode theme : SettingsManager.ThemeMode.values()) {
            RadioButton themeRadio = createThemeOption(theme);
            themeRadio.setToggleGroup(themeGroup);

            themeRadio.setOnAction(e -> {
                if (themeRadio.isSelected()) {
                    System.out.println("主題變更為: " + theme.getDisplayName());
                    settingsManager.setThemeMode(theme);
                    settingsManager.saveSettings();
                    updateThemePreview();

                    if (uiUpdateCallback != null) {
                        Platform.runLater(() -> {
                            uiUpdateCallback.run();
                            Timeline delayedApply = new Timeline();
                            delayedApply.getKeyFrames().addAll(
                                    new KeyFrame(Duration.millis(25), event -> {
                                        uiUpdateCallback.run();
                                        System.out.println("主題變更25ms後更新");
                                    }),
                                    new KeyFrame(Duration.millis(75), event -> {
                                        uiUpdateCallback.run();
                                        System.out.println("主題變更75ms後更新");
                                    })
                            );
                            delayedApply.play();
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

        // 亮度設定區塊
        VBox brightnessSection = createSection("🔆 顯示亮度", "調整閱讀舒適度（實時生效）");

        brightnessSlider = new Slider(10, 100, settingsManager.getEyeCareBrightness());
        brightnessSlider.setShowTickLabels(true);
        brightnessSlider.setShowTickMarks(true);
        brightnessSlider.setMajorTickUnit(20);
        brightnessSlider.setPrefWidth(250);
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

        HBox brightnessControl = new HBox(15);
        brightnessControl.setAlignment(Pos.CENTER_LEFT);
        brightnessControl.getChildren().addAll(brightnessSlider, brightnessLabel);

        brightnessSection.getChildren().add(brightnessControl);

        // 亮度變更事件處理
        brightnessSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            int brightness = newVal.intValue();
            brightnessLabel.setText(String.format("%.0f%%", newVal.doubleValue()));

            Platform.runLater(() -> {
                settingsManager.setEyeCareBrightness(brightness);
                settingsManager.saveSettings();
                updateBrightnessPreview();
                updateBrightnessPreviewVisual(brightness); // 整合後這個會更新預覽視覺區

                if (uiUpdateCallback != null) {
                    uiUpdateCallback.run();

                    Timeline delayUpdate = new Timeline();
                    delayUpdate.getKeyFrames().addAll(
                            new KeyFrame(Duration.millis(25), event -> {
                                uiUpdateCallback.run();
                                System.out.println("亮度設定25ms後更新: " + brightness + "%");
                            }),
                            new KeyFrame(Duration.millis(75), event -> {
                                uiUpdateCallback.run();
                                System.out.println("亮度設定75ms後更新: " + brightness + "%");
                            }),
                            new KeyFrame(Duration.millis(150), event -> {
                                uiUpdateCallback.run();
                                System.out.println("亮度設定150ms後更新: " + brightness + "%");
                            })
                    );
                    delayUpdate.play();
                }

                System.out.println("亮度設定已更新並立即套用: " + brightness + "%");
            });
        });

        // 將區塊加入左側設定區
        settingsArea.getChildren().addAll(themeSection, createSeparator(), brightnessSection);

        // 左右加入主畫面容器
        mainBox.getChildren().addAll(settingsArea, previewArea);

        ScrollPane scrollPane = new ScrollPane(mainBox);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background-color: rgba(50,50,50,0.95); -fx-background: transparent;");

        tab.setContent(scrollPane);
        return tab;
    }


    private void forceApplyTheme(SettingsManager.ThemeMode theme) {
        try {
            Stage primaryStage = (Stage) dialogStage.getOwner();
            if (primaryStage != null) {
                Scene scene = primaryStage.getScene();
                if (scene != null && scene.getRoot() != null) {
                    String backgroundColor = theme.getBackgroundColor();
                    String textColor = theme.getTextColor();

                    // 直接套用背景色到主場景
                    String backgroundStyle = "-fx-background-color: " + backgroundColor + ";";
                    scene.getRoot().setStyle(
                            scene.getRoot().getStyle() + backgroundStyle
                    );

                    // 更新文字顏色（如果有的話）
                    scene.getRoot().lookupAll(".label").forEach(node -> {
                        if (node instanceof javafx.scene.control.Label) {
                            javafx.scene.control.Label label = (javafx.scene.control.Label) node;
                            label.setStyle(label.getStyle() + "-fx-text-fill: " + textColor + ";");
                        }
                    });
                } else {
                    System.out.println("主場景尚未初始化，延遲套用主題");
                    // 延遲套用主題
                    Platform.runLater(() -> {
                        try {
                            Scene delayedScene = primaryStage.getScene();
                            if (delayedScene != null && delayedScene.getRoot() != null) {
                                String backgroundColor = theme.getBackgroundColor();
                                String textColor = theme.getTextColor();
                                
                                String backgroundStyle = "-fx-background-color: " + backgroundColor + ";";
                                delayedScene.getRoot().setStyle(
                                        delayedScene.getRoot().getStyle() + backgroundStyle
                                );
                                
                                delayedScene.getRoot().lookupAll(".label").forEach(node -> {
                                    if (node instanceof javafx.scene.control.Label) {
                                        javafx.scene.control.Label label = (javafx.scene.control.Label) node;
                                        label.setStyle(label.getStyle() + "-fx-text-fill: " + textColor + ";");
                                    }
                                });
                            }
                        } catch (Exception ex) {
                            System.err.println("延遲套用主題失敗: " + ex.getMessage());
                        }
                    });
                }
            }
        } catch (Exception e) {
            System.err.println("套用主題失敗: " + e.getMessage());
        }
    }
    private void applyBrightnessEffect(int brightness) {
        try {
            // 獲取主舞台引用
            Stage primaryStage = (Stage) dialogStage.getOwner();
            if (primaryStage != null && primaryStage.getScene() != null) {
                double normalizedBrightness = brightness / 100.0;
                String brightnessFilter = String.format("brightness(%.2f)", normalizedBrightness);
                primaryStage.getScene().getRoot().setStyle("-fx-effect: " + brightnessFilter + ";");
            }
        } catch (Exception e) {
            System.err.println("套用亮度效果失敗: " + e.getMessage());
        }
    }
    private VBox createBrightnessPreviewArea() {
        VBox previewContainer = new VBox(15); // 整塊區域
        previewContainer.setPadding(new Insets(20));
        previewContainer.setAlignment(Pos.CENTER);
        previewContainer.setStyle(
                "-fx-background-color: #000000;" +  // 純黑模式背景
                        "-fx-background-radius: 15;" +
                        "-fx-border-color: white;" +
                        "-fx-border-width: 1;" +
                        "-fx-border-radius: 15;"
        );

        // 模式標題
        Label titleLabel = new Label("純黑模式");
        titleLabel.setStyle("-fx-text-fill: white; -fx-font-size: 20px; -fx-font-weight: bold;");

        // 範例文字
        Label exampleText = new Label("這裡是文字範例");
        exampleText.setStyle("-fx-text-fill: white; -fx-font-size: 14px;");

        // 模擬文章區塊
        VBox articleBlock = new VBox(5);
        articleBlock.setPadding(new Insets(10));
        articleBlock.setAlignment(Pos.CENTER_LEFT);
        articleBlock.setStyle(
                "-fx-background-color: #444444;" +
                        "-fx-background-radius: 10;"
        );

        Label articleTitle = new Label("🗎 文章標題範例");
        articleTitle.setStyle("-fx-text-fill: white; -fx-font-size: 14px; -fx-font-weight: bold;");
        Label articleContent = new Label("這是一段不顯文字內容...");
        articleContent.setStyle("-fx-text-fill: #CCCCCC; -fx-font-size: 12px;");
        articleBlock.getChildren().addAll(articleTitle, articleContent);

        // 亮度百分比
        Label brightnessLabel = new Label("目前亮度：77%");
        brightnessLabel.setStyle("-fx-text-fill: white; -fx-font-size: 13px;");

        // 建議模式按鈕（模擬樣式）
        Button suggestionButton = new Button("🔆 明亮 - 適合白天使用");
        suggestionButton.setStyle(
                "-fx-background-color: transparent;" +
                        "-fx-border-color: #00BFFF;" +
                        "-fx-text-fill: #00BFFF;" +
                        "-fx-border-radius: 5;" +
                        "-fx-font-size: 12px;"
        );

        previewContainer.getChildren().addAll(
                titleLabel,
                exampleText,
                articleBlock,
                brightnessLabel,
                suggestionButton
        );

        return previewContainer;
    }


    // 更新亮度預覽視覺效果
    private void updateBrightnessPreviewVisual(int brightness) {
        try {
            // 查找預覽區域中的元素
            Node pagePreview = dialogStage.getScene().lookup("#brightnessPreviewPage");
            Node previewTitle = dialogStage.getScene().lookup("#previewTitle");
            Node previewContent = dialogStage.getScene().lookup("#previewContent");
            Node brightnessValueLabel = dialogStage.getScene().lookup("#brightnessValueLabel");

            if (pagePreview != null) {
                // 根據當前主題和亮度計算顏色
                SettingsManager.ThemeMode currentTheme = settingsManager.getCurrentTheme();
                String baseBackground = currentTheme.getBackgroundColor();
                String baseText = currentTheme.getTextColor();

                // 計算亮度調整後的顏色
                String adjustedBackground = adjustColorBrightness(baseBackground, brightness);
                String adjustedText = adjustColorBrightness(baseText, brightness);

                // 應用到預覽頁面背景
                pagePreview.setStyle(
                        "-fx-background-color: " + adjustedBackground + "; " +
                                "-fx-border-color: " + adjustedText + "; " +
                                "-fx-border-width: 0.5; " +
                                "-fx-border-radius: 8; " +
                                "-fx-background-radius: 8; " +
                                "-fx-effect: dropshadow(gaussian, rgba(0,0,0," + (brightness / 200.0) + "), 3, 0, 0, 1);"
                );

                // 應用到文字元素
                if (previewTitle != null) {
                    previewTitle.setStyle(
                            "-fx-text-fill: " + adjustedText + "; " +
                                    "-fx-font-size: 13px; " +
                                    "-fx-font-weight: bold;"
                    );
                }

                if (previewContent != null) {
                    previewContent.setStyle(
                            "-fx-text-fill: " + adjustedText + "; " +
                                    "-fx-font-size: 11px; " +
                                    "-fx-wrap-text: true; " +
                                    "-fx-text-alignment: left;"
                    );
                }
            }

            // 更新亮度數值顯示
            if (brightnessValueLabel != null) {
                ((Label) brightnessValueLabel).setText(String.format("目前亮度：%d%%", brightness));
            }

            // 更新舒適度指示器
            updateComfortIndicator(brightness);

            // 添加視覺反饋動畫
            if (pagePreview != null) {
                ScaleTransition scaleTransition = new ScaleTransition(Duration.millis(150), pagePreview);
                scaleTransition.setFromX(1.0);
                scaleTransition.setFromY(1.0);
                scaleTransition.setToX(1.02);
                scaleTransition.setToY(1.02);
                scaleTransition.setAutoReverse(true);
                scaleTransition.setCycleCount(2);
                scaleTransition.play();
            }

        } catch (Exception e) {
            System.err.println("更新亮度預覽視覺效果失敗: " + e.getMessage());
        }
    }

    // 調整顏色亮度的工具方法
    private String adjustColorBrightness(String hexColor, int brightnessPct) {
        try {
            // 移除 # 符號
            String cleanHex = hexColor.replace("#", "");

            // 解析RGB值
            int r = Integer.valueOf(cleanHex.substring(0, 2), 16);
            int g = Integer.valueOf(cleanHex.substring(2, 4), 16);
            int b = Integer.valueOf(cleanHex.substring(4, 6), 16);

            // 根據亮度百分比調整 (50%為基準，100%為最亮，10%為最暗)
            double factor = brightnessPct / 100.0;

            // 對於暗色主題，提高亮度時讓顏色更亮
            // 對於亮色主題，降低亮度時讓顏色更暗
            if (isDarkColor(r, g, b)) {
                // 暗色背景：亮度越高，顏色越亮
                r = Math.min(255, (int) (r + (255 - r) * (factor - 0.5)));
                g = Math.min(255, (int) (g + (255 - g) * (factor - 0.5)));
                b = Math.min(255, (int) (b + (255 - b) * (factor - 0.5)));
            } else {
                // 亮色背景：亮度越低，顏色越暗
                r = Math.max(0, (int) (r * factor));
                g = Math.max(0, (int) (g * factor));
                b = Math.max(0, (int) (b * factor));
            }

            return String.format("#%02x%02x%02x", r, g, b);
        } catch (Exception e) {
            return hexColor; // 如果轉換失敗，返回原色
        }
    }

    // 判斷是否為暗色
    private boolean isDarkColor(int r, int g, int b) {
        // 使用亮度公式判斷
        double brightness = (r * 0.299 + g * 0.587 + b * 0.114) / 255;
        return brightness < 0.5;
    }

    // 更新舒適度指示器
    private void updateComfortIndicator(int brightness) {
        try {
            Node indicator = dialogStage.getScene().lookup("#comfortIndicator");
            if (indicator instanceof Label) {
                Label comfortLabel = (Label) indicator;

                String comfortText;
                String comfortColor;

                if (brightness < 25) {
                    comfortText = "👁️ 太暗 - 可能造成眼睛疲勞";
                    comfortColor = "#e74c3c"; // 紅色警告
                } else if (brightness < 45) {
                    comfortText = "🌙 偏暗 - 適合夜間閱讀";
                    comfortColor = "#f39c12"; // 橙色提醒
                } else if (brightness < 75) {
                    comfortText = "✅ 舒適 - 理想的閱讀亮度";
                    comfortColor = "#27ae60"; // 綠色良好
                } else if (brightness < 90) {
                    comfortText = "☀️ 明亮 - 適合白天使用";
                    comfortColor = "#3498db"; // 藍色提示
                } else {
                    comfortText = "⚠️ 太亮 - 注意保護視力";
                    comfortColor = "#e67e22"; // 橙色警告
                }

                comfortLabel.setText(comfortText);
                comfortLabel.setStyle(
                        "-fx-text-fill: " + comfortColor + "; " +
                                "-fx-font-size: 11px; " +
                                "-fx-font-weight: 600; " +
                                "-fx-background-color: rgba(255,255,255,0.1); " +
                                "-fx-padding: 5 8; " +
                                "-fx-background-radius: 6;"
                );

                // 添加脈動動畫提示
                if (brightness < 25 || brightness > 90) {
                    Timeline pulseTimeline = new Timeline();
                    pulseTimeline.getKeyFrames().addAll(
                            new KeyFrame(Duration.ZERO, new KeyValue(comfortLabel.scaleXProperty(), 1.0)),
                            new KeyFrame(Duration.millis(500), new KeyValue(comfortLabel.scaleXProperty(), 1.05)),
                            new KeyFrame(Duration.millis(1000), new KeyValue(comfortLabel.scaleXProperty(), 1.0))
                    );
                    pulseTimeline.setCycleCount(2);
                    pulseTimeline.play();
                }
            }
        } catch (Exception e) {
            System.err.println("更新舒適度指示器失敗: " + e.getMessage());
        }
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

        // 亮度預覽整合到主題預覽中
        VBox brightnessPreviewSection = new VBox(8);
        brightnessPreviewSection.setAlignment(Pos.CENTER);

        Label brightnessPreviewTitle = new Label("💡 亮度效果");
        brightnessPreviewTitle.setStyle("-fx-text-fill: white; -fx-font-size: 12px; -fx-font-weight: 600;");

        brightnessPreview = new ProgressBar();
        brightnessPreview.setPrefWidth(180);
        brightnessPreview.setPrefHeight(6);

        // 亮度數值標籤
        Label brightnessValueLabel = new Label("100%");
        brightnessValueLabel.setId("brightnessValueLabel");
        brightnessValueLabel.setStyle("-fx-text-fill: white; -fx-font-size: 10px;");

        brightnessPreviewSection.getChildren().addAll(brightnessPreviewTitle, brightnessPreview, brightnessValueLabel);

        // 將亮度預覽整合到主題預覽框中
        themePreviewBox.getChildren().add(brightnessPreviewSection);

        previewArea.getChildren().addAll(previewTitle, themePreviewBox);

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

        VBox pagePreview = new VBox(8);
        pagePreview.setPrefHeight(120);
        pagePreview.setPadding(new Insets(12));
        pagePreview.setAlignment(Pos.TOP_CENTER);
        pagePreview.setId("brightnessPreviewPage");

// 模擬文章內容
        Label previewText1 = new Label("📖 文章標題範例");
        previewText1.setStyle(
                "-fx-font-size: 13px; " +
                        "-fx-font-weight: bold; " +
                        "-fx-text-fill: " + textColor + ";"
        );
        previewText1.setId("previewTitle");

        Label previewText2 = new Label("這是一段示範文字內容，\n用來展示不同亮度下的\n閱讀效果和視覺舒適度。");
        previewText2.setStyle(
                "-fx-font-size: 11px; " +
                        "-fx-text-fill: " + textColor + ";"
        );
        previewText2.setWrapText(true);
        previewText2.setId("previewContent");

        pagePreview.getChildren().addAll(previewText1, previewText2);

// 將 pagePreview 加進 themePreviewBox
        themePreviewBox.getChildren().add(pagePreview);

// 可以順便也把亮度數值標籤與舒適度指示器一起加進來
        Label brightnessValue = new Label(String.format("目前亮度：%d%%", settingsManager.getEyeCareBrightness()));
        brightnessValue.setStyle(
                "-fx-text-fill: rgba(255,255,255,0.8); " +
                        "-fx-font-size: 12px; " +
                        "-fx-font-weight: 600;"
        );
        brightnessValue.setId("brightnessValueLabel");

// 指示器（可根據亮度調整文字）
        Label comfortIndicator = new Label();
        comfortIndicator.setId("comfortIndicator");
        updateComfortIndicatorText(comfortIndicator, settingsManager.getEyeCareBrightness());

// 加到主容器中
        themePreviewBox.getChildren().addAll(brightnessValue, comfortIndicator);
        // 添加淡入動畫
        FadeTransition fadeIn = new FadeTransition(Duration.millis(300), themePreviewBox);
        fadeIn.setFromValue(0.0);
        fadeIn.setToValue(1.0);
        fadeIn.play();
    }

    private void updateComfortIndicatorText(Label comfortIndicator, int brightness) {
        if (brightness < 30) {
            comfortIndicator.setText("🔴 螢幕偏暗，建議調高亮度");
            comfortIndicator.setStyle("-fx-text-fill: #e74c3c; -fx-font-size: 11px;");
        } else if (brightness < 70) {
            comfortIndicator.setText("🟠 中等亮度，適合一般使用");
            comfortIndicator.setStyle("-fx-text-fill: #f39c12; -fx-font-size: 11px;");
        } else {
            comfortIndicator.setText("🟢 良好亮度，適合閱讀");
            comfortIndicator.setStyle("-fx-text-fill: #27ae60; -fx-font-size: 11px;");
        }
    }


    // 更新亮度預覽 - 修改為使用當前設定
    private void updateBrightnessPreview() {
        int currentBrightness = settingsManager.getEyeCareBrightness();
        double progress = currentBrightness / 100.0;
        brightnessPreview.setProgress(progress);

        Label comfortIndicator = (Label) themePreviewBox.lookup("#comfortIndicator");
        if (comfortIndicator != null) {
            updateComfortIndicatorText(comfortIndicator, currentBrightness);
        }

        // 更新亮度數值標籤
        Label brightnessValueLabel = (Label) themePreviewBox.lookup("#brightnessValueLabel");
        if (brightnessValueLabel != null) {
            brightnessValueLabel.setText(currentBrightness + "%");
        }

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

            // 立即保存設定
            settingsManager.setShowPageNumbers(isSelected);
            settingsManager.saveSettings();

            // **修正：立即更新UI，不等按確認**
            if (uiUpdateCallback != null) {
                Platform.runLater(() -> {
                    // 立即第一次更新
                    uiUpdateCallback.run();
                    System.out.println("頁碼顯示立即更新: " + isSelected);

                    // 多次延遲確保生效
                    Timeline pageNumbersUpdate = new Timeline();
                    
                    pageNumbersUpdate.getKeyFrames().add(
                        new KeyFrame(Duration.millis(25), event -> {
                            uiUpdateCallback.run();
                            System.out.println("頁碼顯示25ms後更新: " + isSelected);
                        })
                    );
                    
                    pageNumbersUpdate.getKeyFrames().add(
                        new KeyFrame(Duration.millis(75), event -> {
                            uiUpdateCallback.run();
                            System.out.println("頁碼顯示75ms後更新: " + isSelected);
                        })
                    );
                    
                    pageNumbersUpdate.getKeyFrames().add(
                        new KeyFrame(Duration.millis(150), event -> {
                            uiUpdateCallback.run();
                            System.out.println("頁碼顯示150ms後更新: " + isSelected);
                        })
                    );
                    
                    pageNumbersUpdate.getKeyFrames().add(
                        new KeyFrame(Duration.millis(250), event -> {
                            uiUpdateCallback.run();
                            System.out.println("頁碼顯示250ms後更新: " + isSelected);
                        })
                    );
                    
                    pageNumbersUpdate.play();
                });
            }

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

    private void forceUpdatePageNumbers(boolean show) {
        try {
            Stage primaryStage = (Stage) dialogStage.getOwner();
            if (primaryStage != null && primaryStage.getScene() != null) {
                // 尋找頁碼標籤並更新其可見性
                primaryStage.getScene().getRoot().lookupAll(".label").forEach(node -> {
                    if (node instanceof javafx.scene.control.Label) {
                        javafx.scene.control.Label label = (javafx.scene.control.Label) node;
                        String text = label.getText();
                        if (text != null && (text.contains("頁面:") || text.contains("文字:"))) {
                            label.setVisible(show);
                            label.setManaged(show);
                        }
                    }
                });
            }
        } catch (Exception e) {
            System.err.println("更新頁碼顯示失敗: " + e.getMessage());
        }
    }

    private RadioButton createThemeOption(SettingsManager.ThemeMode theme) {
        RadioButton radio = new RadioButton(theme.getDisplayName());
        radio.setStyle(
                "-fx-text-fill: white; " +
                        "-fx-font-size: 13px; " +
                        "-fx-font-weight: 500;"
        );

        // **修正：強化即時套用邏輯**
        radio.setOnAction(e -> {
            if (radio.isSelected()) {
                System.out.println("主題變更為: " + theme.getDisplayName());

                // 立即保存設定
                settingsManager.setThemeMode(theme);
                settingsManager.saveSettings();

                // 立即更新預覽
                updateThemePreview();

                // **修正：立即觸發UI更新，不等按確認**
                if (uiUpdateCallback != null) {
                    Platform.runLater(() -> {
                        // **新增：強制多次更新確保生效**
                        for (int i = 0; i < 3; i++) {
                            final int attempt = i;
                            Timeline delayedUpdate = new Timeline(
                                    new KeyFrame(Duration.millis(50 * (i + 1)), event -> {
                                        uiUpdateCallback.run();
                                        System.out.println("主題更新嘗試 #" + (attempt + 1));

                                        // **新增：最後一次嘗試時模擬頁面切換**
                                        if (attempt == 2) {
                                            forceCompleteUIRefresh();
                                        }
                                    })
                            );
                            delayedUpdate.play();
                        }
                    });
                }
            }
        });

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

        // **修正：按下確認鍵時的處理**
        okButton.setOnAction(e -> {
            System.out.println("確認按鈕被點擊，開始強制套用所有設定...");

            // **修正：立即強制保存所有設定**
            try {
                // 1. 強制保存所有設定到檔案
                settingsManager.saveSettings();
                System.out.println("設定已強制保存到檔案");

                // 2. **新增：強制重新套用當前主題設定**
                SettingsManager.ThemeMode currentTheme = settingsManager.getCurrentTheme();
                settingsManager.setThemeMode(currentTheme);
                System.out.println("強制重新套用主題: " + currentTheme.getDisplayName());

                // 3. **新增：強制更新主舞台的主題**
                try {
                    Stage primaryStage = (Stage) dialogStage.getOwner();
                    if (primaryStage != null && primaryStage.getScene() != null) {
                        String backgroundColor = currentTheme.getBackgroundColor();
                        String textColor = currentTheme.getTextColor();
                        
                        // 直接套用背景色到主場景
                        primaryStage.getScene().getRoot().setStyle(
                            "-fx-background-color: " + backgroundColor + ";"
                        );
                        
                        System.out.println("主舞台主題已強制更新");
                    }
                } catch (Exception ex) {
                    System.err.println("更新主舞台主題失敗: " + ex.getMessage());
                }

                // 4. **新增：多次強制更新UI，確保生效**
                if (uiUpdateCallback != null) {
                    // 立即第一次更新
                    uiUpdateCallback.run();
                    System.out.println("立即第一次UI更新完成");

                    // 使用多次Platform.runLater確保更新生效
                    Platform.runLater(() -> {
                        uiUpdateCallback.run();
                        System.out.println("第二次UI更新完成");
                        
                        Platform.runLater(() -> {
                            uiUpdateCallback.run();
                            System.out.println("第三次UI更新完成");
                            
                            Platform.runLater(() -> {
                                uiUpdateCallback.run();
                                forceCompleteUIRefresh();
                                System.out.println("第四次UI更新完成");
                                
                                Platform.runLater(() -> {
                                    // 強制重新套用主題設定
                                    settingsManager.setThemeMode(currentTheme);
                                    uiUpdateCallback.run();
                                    System.out.println("最終主題刷新完成");
                                });
                            });
                        });
                    });
                }

            } catch (Exception ex) {
                System.err.println("確認設定時發生錯誤: " + ex.getMessage());
                ex.printStackTrace();
            }

            // 延遲關閉對話框，確保設定生效
            Platform.runLater(() -> {
                close();
            });
        });

        buttonBar.getChildren().addAll(cancelButton, okButton);
        return buttonBar;
    }

    private void forceCompleteUIRefresh() {
        try {
            // **方法1：觸發主界面的完整刷新**
            if (uiUpdateCallback != null) {
                // 連續執行三次更新回調
                uiUpdateCallback.run();

                Platform.runLater(() -> {
                    uiUpdateCallback.run();

                    // **方法2：模擬按鍵事件來強制刷新**
                    Timeline simulateRefresh = new Timeline(
                            new KeyFrame(Duration.millis(50), event -> {
                                uiUpdateCallback.run();
                                // **方法3：嘗試觸發重新布局**
                                simulateLayoutRefresh();
                            })
                    );
                    simulateRefresh.play();
                });
            }
        } catch (Exception ex) {
            System.err.println("強制UI刷新時發生錯誤: " + ex.getMessage());
        }
    }

    // 6. **新增：模擬布局刷新的方法**
    private void simulateLayoutRefresh() {
        try {
            // 獲取主舞台的場景
            Stage mainStage = (Stage) dialogStage.getOwner();
            if (mainStage != null && mainStage.getScene() != null) {
                Platform.runLater(() -> {
                    // **方法1：強制重新計算CSS和布局**
                    mainStage.getScene().getRoot().applyCss();
                    mainStage.getScene().getRoot().autosize();
                    mainStage.getScene().getRoot().requestLayout();

                    // **方法2：觸發視窗大小微調來強制重繪**
                    double currentWidth = mainStage.getWidth();
                    double currentHeight = mainStage.getHeight();

                    // 微調1像素再調回來
                    mainStage.setWidth(currentWidth + 1);
                    mainStage.setHeight(currentHeight + 1);

                    Timeline resetSize = new Timeline(
                            new KeyFrame(Duration.millis(50), event -> {
                                mainStage.setWidth(currentWidth);
                                mainStage.setHeight(currentHeight);
                            })
                    );
                    resetSize.play();

                    System.out.println("模擬布局刷新完成");
                });
            }
        } catch (Exception ex) {
            System.err.println("模擬布局刷新時發生錯誤: " + ex.getMessage());
        }
    }

    private void simulatePageRefresh() {
        try {
            // 獲取主控制器的引用
            Stage mainStage = (Stage) dialogStage.getOwner();
            if (mainStage != null && mainStage.getScene() != null) {
                // 通過場景根節點查找主控制器相關的UI元素

                // **方法1：直接觸發場景的樣式重新計算**
                mainStage.getScene().getRoot().applyCss();
                mainStage.getScene().getRoot().autosize();
                mainStage.getScene().getRoot().requestLayout();

                // **方法2：模擬按鍵事件來觸發更新**
                javafx.scene.input.KeyEvent dummyKeyEvent = new javafx.scene.input.KeyEvent(
                        javafx.scene.input.KeyEvent.KEY_PRESSED,
                        "", "",
                        javafx.scene.input.KeyCode.F5,
                        false, false, false, false
                );

                // **方法3：直接調用主控制器的更新方法（如果可能的話）**
                // 這需要通過其他方式獲取主控制器引用
            }
        } catch (Exception ex) {
            System.err.println("模擬頁面刷新時發生錯誤: " + ex.getMessage());
        }
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

        if (autoSaveUpdateTimer != null) {
            autoSaveUpdateTimer.cancel();
            autoSaveUpdateTimer = null;
        }

        // 確保所有設定都已保存
        settingsManager.saveSettings();

        dialogStage.close();
    }
}