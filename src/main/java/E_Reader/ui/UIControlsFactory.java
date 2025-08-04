package E_Reader.ui;

import E_Reader.utils.AlertHelper;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Priority;
import javafx.scene.effect.DropShadow;
import javafx.scene.effect.GaussianBlur;
import javafx.scene.paint.Color;


public class UIControlsFactory {

    // UI元件引用
    private Label pageLabel;
    private TextField pageField;
    private Button textModeBtn;
    private Button autoScrollBtn;
    private Button nightModeBtn;
    private Button eyeCareBtn;
    private Button toggleNavBarBtn;

    // 图片模式专用按钮
    private Button zoomInBtn;
    private Button zoomOutBtn;
    private Button fitWidthBtn;
    private Button fitHeightBtn;
    private Button rotateBtn;

    // 文字模式专用按钮
    private Button fontSizeIncBtn;
    private Button fontSizeDecBtn;
    private Button lineSpacingBtn;
    private Button searchBtn;
    private Button focusModeBtn;

    // 控制列容器
    private HBox topControls;
    private HBox bottomControls;

    // 亮度設置
    private double currentBrightness = 80.0;


    private static final String BUTTON_STYLE =
            "-fx-background-color: linear-gradient(to bottom, " +
                    "rgba(255,255,255,0.3) 0%, " +
                    "rgba(255,255,255,0.15) 50%, " +
                    "rgba(255,255,255,0.1) 100%); " +
                    "-fx-border-color: rgba(255,255,255,0.4); " +
                    "-fx-border-width: 1; " +
                    "-fx-border-radius: 8; " +
                    "-fx-background-radius: 8; " +
                    "-fx-text-fill: rgba(255,255,255,0.95); " +
                    "-fx-font-size: 11px; " +
                    "-fx-font-weight: 600; " +
                    "-fx-padding: 6 12 6 12; " +
                    "-fx-cursor: hand; " +
                    "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 4, 0, 0, 2);";

    // 重要按钮样式
    private static final String ACCENT_BUTTON_STYLE =
            "-fx-background-color: linear-gradient(to bottom, " +
                    "rgba(52,152,219,0.9) 0%, " +
                    "rgba(41,128,185,0.9) 100%); " +
                    "-fx-border-color: rgba(52,152,219,0.8); " +
                    "-fx-border-width: 1; " +
                    "-fx-border-radius: 8; " +
                    "-fx-background-radius: 8; " +
                    "-fx-text-fill: white; " +
                    "-fx-font-size: 11px; " +
                    "-fx-font-weight: 700; " +
                    "-fx-padding: 6 12 6 12; " +
                    "-fx-cursor: hand; " +
                    "-fx-effect: dropshadow(gaussian, rgba(52,152,219,0.4), 6, 0, 0, 2);";

    // 危险按钮样式
    private static final String DANGER_BUTTON_STYLE =
            "-fx-background-color: linear-gradient(to bottom, " +
                    "rgba(231,76,60,0.9) 0%, " +
                    "rgba(192,57,43,0.9) 100%); " +
                    "-fx-border-color: rgba(231,76,60,0.8); " +
                    "-fx-border-width: 1; " +
                    "-fx-border-radius: 8; " +
                    "-fx-background-radius: 8; " +
                    "-fx-text-fill: white; " +
                    "-fx-font-size: 11px; " +
                    "-fx-font-weight: 700; " +
                    "-fx-padding: 6 12 6 12; " +
                    "-fx-cursor: hand; " +
                    "-fx-effect: dropshadow(gaussian, rgba(231,76,60,0.4), 6, 0, 0, 2);";

    // 专注模式按钮样式
    private static final String FOCUS_BUTTON_STYLE =
            "-fx-background-color: linear-gradient(to bottom, " +
                    "rgba(155,89,182,0.9) 0%, " +
                    "rgba(142,68,173,0.9) 100%); " +
                    "-fx-border-color: rgba(155,89,182,0.8); " +
                    "-fx-border-width: 1; " +
                    "-fx-border-radius: 8; " +
                    "-fx-background-radius: 8; " +
                    "-fx-text-fill: white; " +
                    "-fx-font-size: 11px; " +
                    "-fx-font-weight: 700; " +
                    "-fx-padding: 6 12 6 12; " +
                    "-fx-cursor: hand; " +
                    "-fx-effect: dropshadow(gaussian, rgba(155,89,182,0.4), 6, 0, 0, 2);";

    // 文字输入框样式 - 修复版本（增强可见性）
    private static final String TEXT_FIELD_STYLE =
            "-fx-background-color: rgba(40,40,40,0.8); " +
                    "-fx-border-color: rgba(255,255,255,0.3); " +
                    "-fx-border-width: 1; " +
                    "-fx-border-radius: 6; " +
                    "-fx-background-radius: 6; " +
                    "-fx-text-fill: white; " +
                    "-fx-prompt-text-fill: rgba(255,255,255,0.7); " +
                    "-fx-font-size: 11px; " +
                    "-fx-font-weight: 500; " +
                    "-fx-padding: 6 10 6 10; " +
                    "-fx-effect: inset-dropshadow(gaussian, rgba(0,0,0,0.4), 2, 0, 0, 1);";

    /**
     * 创建上方控制列 - iOS风格玻璃质感
     */
    public HBox createTopControls(MainController controller) {
        // 主要功能按钮
        Button returnToManagerBtn = createStyledButton("↩ 返回", () -> {
            controller.showFileManager();
        }, DANGER_BUTTON_STYLE);

        // 导航列控制按钮
        toggleNavBarBtn = createButton("📱 導航", () -> {
            controller.toggleNavigationBar();
        });

        Button bookmarkBtn = createButton("🔖 書籤", controller::showBookmarkDialog);
        Button fullscreenBtn = createButton("⛶ 全螢幕", controller::toggleFullscreen);

        // 共用功能按钮
        autoScrollBtn = createButton("⏯ 自動閱讀", controller::toggleAutoScroll);
        nightModeBtn = createButton("🌙 夜間模式", controller::toggleNightMode);
        eyeCareBtn = createButton("👁 護眼模式", controller::toggleEyeCareMode);
        textModeBtn = createButton("📖 文字模式", controller::toggleTextMode);

        // 文字模式专用按钮
        searchBtn = createButton("🔍 搜索", () -> showSearchDialog(controller));

        // 专注模式按钮
        focusModeBtn = createStyledButton("🎯 專注", () -> {
            System.out.println("專注按鈕被點擊");
            controller.toggleFocusMode();
        }, FOCUS_BUTTON_STYLE);

        // 创建控制列容器 - 毛玻璃效果
        topControls = new HBox(8);
        topControls.setAlignment(Pos.CENTER);
        topControls.setPadding(new Insets(8, 12, 8, 12));

        // iOS风格毛玻璃背景 - 修复版本（增强可见性）
        topControls.setStyle(
                "-fx-background-color: linear-gradient(to bottom, " +
                        "rgba(25,25,25,0.9) 0%, " +
                        "rgba(35,35,35,0.95) 50%, " +
                        "rgba(25,25,25,0.9) 100%); " +
                        "-fx-border-color: linear-gradient(to right, " +
                        "rgba(26,188,156,0.5), rgba(52,152,219,0.5), rgba(155,89,182,0.5)); " +
                        "-fx-border-width: 0 0 1 0; " +
                        "-fx-background-radius: 0 0 12 12; " +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.5), 12, 0, 0, 3);"
        );

        // 基本按钮始终显示
        topControls.getChildren().addAll(
                returnToManagerBtn, toggleNavBarBtn,
                createSeparator(),
                bookmarkBtn, textModeBtn,
                createSeparator(),
                autoScrollBtn, nightModeBtn, eyeCareBtn, focusModeBtn,
                createSeparator(),
                fullscreenBtn
        );

        return topControls;
    }

    /**
     * 创建下方控制列 - iOS风格玻璃质感
     */
    public HBox createBottomControls(MainController controller) {
        // 导航控制按钮 - 更小的尺寸
        Button firstPageBtn = createButton("⏮", controller::goToFirstPage);
        Button prevBtn = createButton("◀", controller::goToPreviousPage);
        Button nextBtn = createButton("▶", controller::goToNextPage);
        Button lastPageBtn = createButton("⏭", controller::goToLastPage);

        // 页面跳转控制
        pageField = new TextField();
        pageField.setPrefWidth(60);
        pageField.setPromptText("頁數");
        pageField.setStyle(TEXT_FIELD_STYLE);

        Button goToPageBtn = createButton("跳轉", () -> handleGoToPage(controller));


        pageField.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ENTER) {
                handleGoToPage(controller);
            }
        });

        zoomInBtn = createButton("🔍+", () -> zoomIn(controller));
        zoomOutBtn = createButton("🔍-", () -> zoomOut(controller));
        fitWidthBtn = createButton("↔ 寬度", () -> fitWidth(controller));
        fitHeightBtn = createButton("↕ 高度", () -> fitHeight(controller));
        rotateBtn = createButton("🔄", () -> rotateImage(controller));

        // 文字模式专用按钮
        fontSizeIncBtn = createButton("A+", () -> adjustFontSize(controller, 2));
        fontSizeDecBtn = createButton("A-", () -> adjustFontSize(controller, -2));
        lineSpacingBtn = createButton("📏 行距", () -> showLineSpacingDialog(controller));

        // 阅读模式控制
        Button focusModeBtn2 = createStyledButton("🎯 專注模式",
                controller::toggleFocusMode, FOCUS_BUTTON_STYLE);
        Button speedReadBtn = createButton("⚡ 快速閱讀", () -> showSpeedReadingDialog(controller));

        // 设置按钮 - 移至左下角
        Button settingsBtn = createStyledButton("⚙️ 設置",
                () -> showEnhancedSettingsDialog(controller), ACCENT_BUTTON_STYLE);

        // 创建控制列容器
        bottomControls = new HBox(8);
        bottomControls.setAlignment(Pos.CENTER);
        bottomControls.setPadding(new Insets(8, 12, 8, 12));

        // iOS风格毛玻璃背景 - 修复版本（增强可见性）
        bottomControls.setStyle(
                "-fx-background-color: linear-gradient(to bottom, " +
                        "rgba(35,35,35,0.95) 0%, " +
                        "rgba(25,25,25,0.9) 100%); " +
                        "-fx-border-color: linear-gradient(to right, " +
                        "rgba(155,89,182,0.5), rgba(52,152,219,0.5), rgba(26,188,156,0.5)); " +
                        "-fx-border-width: 1 0 0 0; " +
                        "-fx-background-radius: 12 12 0 0; " +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.5), 12, 0, 0, -3);"
        );

        // 创建左侧控制区域
        HBox leftControls = new HBox(6);
        leftControls.setAlignment(Pos.CENTER_LEFT);
        leftControls.getChildren().add(settingsBtn);

        // 创建中央导航区域
        HBox centerControls = new HBox(6);
        centerControls.setAlignment(Pos.CENTER);
        centerControls.getChildren().addAll(
                firstPageBtn, prevBtn, nextBtn, lastPageBtn,
                createSeparator(),
                pageField, goToPageBtn
        );

        // 创建右侧控制区域
        HBox rightControls = new HBox(6);
        rightControls.setAlignment(Pos.CENTER_RIGHT);
        rightControls.getChildren().addAll(focusModeBtn2, speedReadBtn);

        // 使用BorderPane来排列左、中、右三个区域
        BorderPane bottomLayout = new BorderPane();
        bottomLayout.setLeft(leftControls);
        bottomLayout.setCenter(centerControls);
        bottomLayout.setRight(rightControls);

        // 将BorderPane包装在HBox中
        bottomControls.getChildren().add(bottomLayout);
        HBox.setHgrow(bottomLayout, Priority.ALWAYS);

        return bottomControls;
    }

    /**
     * 创建视觉分隔符 - 修复版本（增强可见性）
     */
    private Label createSeparator() {
        Label separator = new Label("│");
        separator.setStyle(
                "-fx-text-fill: rgba(255,255,255,0.3); " +
                        "-fx-font-size: 12px; " +
                        "-fx-font-weight: 300; " +
                        "-fx-padding: 0 4 0 4;"
        );
        return separator;
    }

    /**
     * 根据当前模式更新控制按钮的显示
     */
    public void updateControlsForMode(boolean isTextMode) {
        updateTopControlsForMode(isTextMode);
        updateBottomControlsForMode(isTextMode);
    }

    /**
     * 更新上方控制列的按钮显示
     */
    private void updateTopControlsForMode(boolean isTextMode) {
        if (topControls == null) return;

        // 移除模式专用按钮
        topControls.getChildren().remove(searchBtn);

        // 根据模式添加相应按钮
        if (isTextMode) {
            // 文字模式：添加搜索按钮
            if (!topControls.getChildren().contains(searchBtn)) {
                int textModeIndex = topControls.getChildren().indexOf(textModeBtn);
                if (textModeIndex >= 0) {
                    topControls.getChildren().add(textModeIndex + 1, searchBtn);
                }
            }
        }
    }

    /**
     * 更新下方控制列的按钮显示
     */
    private void updateBottomControlsForMode(boolean isTextMode) {
        if (bottomControls == null) return;

        if (bottomControls.getChildren().isEmpty()) return;

        BorderPane bottomLayout = (BorderPane) bottomControls.getChildren().get(0);
        HBox centerBottomControls = (HBox) bottomLayout.getCenter();

        // 先移除所有模式专用按钮
        centerBottomControls.getChildren().removeAll(
                zoomInBtn, zoomOutBtn, fitWidthBtn, fitHeightBtn, rotateBtn,
                fontSizeIncBtn, fontSizeDecBtn, lineSpacingBtn
        );

        // 移除多余的分隔符
        centerBottomControls.getChildren().removeIf(node ->
                node instanceof Label &&
                        centerBottomControls.getChildren().indexOf(node) > 1);

        // 根据模式添加相应按钮
        if (isTextMode) {
            // 文字模式：添加字体和行距控制
            centerBottomControls.getChildren().add(createSeparator());
            centerBottomControls.getChildren().add(fontSizeIncBtn);
            centerBottomControls.getChildren().add(fontSizeDecBtn);
            centerBottomControls.getChildren().add(lineSpacingBtn);
        } else {
            // 图片模式：添加缩放和旋转控制
            centerBottomControls.getChildren().add(createSeparator());
            centerBottomControls.getChildren().add(zoomInBtn);
            centerBottomControls.getChildren().add(zoomOutBtn);
            centerBottomControls.getChildren().add(fitWidthBtn);
            centerBottomControls.getChildren().add(fitHeightBtn);
            centerBottomControls.getChildren().add(rotateBtn);
        }
    }

    /**
     * 创建标准按钮 - iOS风格玻璃质感 - 修复版本
     */
    private Button createButton(String text, Runnable action) {
        Button button = new Button(text);
        button.setStyle(BUTTON_STYLE);

        // iOS风格交互效果 - 修复版本
        button.setOnMouseEntered(e -> {
            button.setStyle(BUTTON_STYLE.replace("rgba(255,255,255,0.3)", "rgba(255,255,255,0.4)")
                    .replace("rgba(255,255,255,0.15)", "rgba(255,255,255,0.25)")
                    .replace("rgba(255,255,255,0.1)", "rgba(255,255,255,0.2)") +
                    "; -fx-scale-x: 1.02; -fx-scale-y: 1.02; " +
                    "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.4), 6, 0, 0, 3);");
        });

        button.setOnMouseExited(e -> {
            button.setStyle(BUTTON_STYLE);
        });

        button.setOnMousePressed(e -> {
            button.setStyle(BUTTON_STYLE.replace("rgba(255,255,255,0.3)", "rgba(255,255,255,0.15)")
                    .replace("rgba(255,255,255,0.15)", "rgba(255,255,255,0.1)")
                    .replace("rgba(255,255,255,0.1)", "rgba(255,255,255,0.05)") +
                    "; -fx-scale-x: 0.98; -fx-scale-y: 0.98; " +
                    "-fx-effect: inset-dropshadow(gaussian, rgba(0,0,0,0.4), 3, 0, 0, 1);");
        });

        button.setOnMouseReleased(e -> {
            button.setStyle(BUTTON_STYLE);
        });

        button.setOnAction(e -> {
            System.out.println("按鈕被點擊 " + text);
            action.run();
        });

        return button;
    }

    /**
     * 创建特定样式的按钮
     */
    private Button createStyledButton(String text, Runnable action, String customStyle) {
        Button button = new Button(text);
        button.setStyle(customStyle);

        // 获取原始颜色用于悬停效果
        String hoverStyle = customStyle;
        if (customStyle.contains("rgba(52,152,219")) {
            hoverStyle = customStyle.replace("rgba(52,152,219,0.9)", "rgba(52,152,219,1.0)");
        } else if (customStyle.contains("rgba(231,76,60")) {
            hoverStyle = customStyle.replace("rgba(231,76,60,0.9)", "rgba(231,76,60,1.0)");
        } else if (customStyle.contains("rgba(155,89,182")) {
            hoverStyle = customStyle.replace("rgba(155,89,182,0.9)", "rgba(155,89,182,1.0)");
        }

        final String finalHoverStyle = hoverStyle;

        button.setOnMouseEntered(e -> {
            button.setStyle(finalHoverStyle + "; -fx-scale-x: 1.02; -fx-scale-y: 1.02;");
        });

        button.setOnMouseExited(e -> {
            button.setStyle(customStyle);
        });

        button.setOnMousePressed(e -> {
            button.setStyle(customStyle +
                    "; -fx-scale-x: 0.98; -fx-scale-y: 0.98; " +
                    "-fx-effect: inset-dropshadow(gaussian, rgba(0,0,0,0.5), 3, 0, 0, 1);");
        });

        button.setOnMouseReleased(e -> {
            button.setStyle(customStyle);
        });

        button.setOnAction(e -> {
            System.out.println("按鈕被點擊: " + text);
            action.run();
        });

        return button;
    }

    /**
     * 处理跳转到指定页面
     */
    private void handleGoToPage(MainController controller) {
        try {
            int pageNum = Integer.parseInt(pageField.getText());
            controller.goToPage(pageNum - 1);
            pageField.clear();
        } catch (NumberFormatException ex) {
            AlertHelper.showError("錯誤", "請輸入有效頁數");
        }
    }

    // Getter方法用于外部访问UI元件
    public Label getPageLabel() { return pageLabel; }
    public TextField getPageField() { return pageField; }
    public Button getTextModeButton() { return textModeBtn; }
    public Button getAutoScrollButton() { return autoScrollBtn; }
    public Button getNightModeButton() { return nightModeBtn; }
    public Button getEyeCareButton() { return eyeCareBtn; }
    public Button getToggleNavBarButton() { return toggleNavBarBtn; }
    public Button getFocusModeButton() { return focusModeBtn; }
    public HBox getTopControls() { return topControls; }
    public HBox getBottomControls() { return bottomControls; }

    // 实现功能方法（保持原有功能不变）
    private void showSearchDialog(MainController controller) {
        if (!controller.getStateManager().isTextMode() ||
                controller.getStateManager().getCurrentTextPages() == null ||
                controller.getStateManager().getCurrentTextPages().isEmpty()) {
            AlertHelper.showError("提示", "搜索功能僅在文字模式啟用");
            return;
        }

        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("搜索文字");
        dialog.setHeaderText("在文件中搜索文字");
        dialog.setContentText("請輸入搜索關鍵字:");

        // 自定义对话框样式 - 修复版本（增强可见性）
        dialog.getDialogPane().setStyle(
                "-fx-background-color: rgba(35,35,35,0.98); " +
                        "-fx-border-color: rgba(255,255,255,0.4); " +
                        "-fx-border-width: 1; " +
                        "-fx-border-radius: 12; " +
                        "-fx-background-radius: 12; " +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.6), 15, 0, 0, 5);"
        );

        // 修复对话框中的文字颜色
        dialog.getDialogPane().lookup(".header-panel").setStyle("-fx-text-fill: white;");
        dialog.getDialogPane().lookup(".content").setStyle("-fx-text-fill: white;");

        dialog.showAndWait().ifPresent(searchTerm -> {
            if (!searchTerm.trim().isEmpty()) {
                controller.getTextRenderer().searchText(searchTerm);
                controller.showNotification("搜索完成", "以高亮顯示搜索結果");
            }
        });
    }

    /**
     * 显示增强版设置对话框 - 修复版本
     */
    private void showEnhancedSettingsDialog(MainController controller) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("⚙️ 應用程式設置");
        dialog.setHeaderText("個人化你的閱讀體驗");

        // iOS风格对话框样式 - 修复版本（增强可见性和对比度）
        dialog.getDialogPane().setStyle(
                "-fx-background-color: linear-gradient(to bottom, " +
                        "rgba(35,35,35,0.98), rgba(28,28,28,0.98)); " +
                        "-fx-border-color: rgba(255,255,255,0.35); " +
                        "-fx-border-width: 1; " +
                        "-fx-border-radius: 16; " +
                        "-fx-background-radius: 16; " +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.7), 20, 0, 0, 5); " +
                        "-fx-font-family: 'SF Pro Display', '.SF NS Text', 'Helvetica Neue', sans-serif;"
        );

        // 修复对话框标题文字颜色
        dialog.getDialogPane().lookup(".header-panel").setStyle(
                "-fx-text-fill: white; " +
                        "-fx-background-color: rgba(45,45,45,0.9); " +
                        "-fx-border-color: rgba(255,255,255,0.2); " +
                        "-fx-border-width: 0 0 1 0;"
        );

        // 创建TabPane来组织不同设置页面
        TabPane tabPane = new TabPane();
        tabPane.setStyle(
                "-fx-background-color: transparent; " +
                        "-fx-tab-min-width: 120; " +
                        "-fx-tab-max-width: 150;"
        );

        // 修复Tab标签的文字颜色
        tabPane.lookupAll(".tab").forEach(node -> {
            node.setStyle("-fx-text-fill: white; -fx-background-color: rgba(45,45,45,0.8);");
        });

        // 1. 外观主题标签页
        Tab themeTab = createThemeTab(controller);
        themeTab.setText("🎨 外觀");

        // 2. OCR设置标签页
        Tab ocrTab = createOcrTab(controller);
        ocrTab.setText("🔧 OCR");

        // 3. 功能选项标签页
        Tab functionsTab = createFunctionsTab(controller);
        functionsTab.setText("⚙️ 功能");

        tabPane.getTabs().addAll(themeTab, ocrTab, functionsTab);

        dialog.getDialogPane().setContent(tabPane);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        // 自定义按钮样式 - 修复版本
        Button okButton = (Button) dialog.getDialogPane().lookupButton(ButtonType.OK);
        Button cancelButton = (Button) dialog.getDialogPane().lookupButton(ButtonType.CANCEL);

        okButton.setStyle(ACCENT_BUTTON_STYLE);
        cancelButton.setStyle(
                "-fx-background-color: rgba(60,60,60,0.8); " +
                        "-fx-border-color: rgba(255,255,255,0.3); " +
                        "-fx-border-width: 1; " +
                        "-fx-border-radius: 8; " +
                        "-fx-background-radius: 8; " +
                        "-fx-text-fill: white; " +
                        "-fx-font-size: 12px; " +
                        "-fx-font-weight: 600; " +
                        "-fx-padding: 8 16 8 16; " +
                        "-fx-cursor: hand;"
        );

        dialog.showAndWait().ifPresent(result -> {
            if (result == ButtonType.OK) {
                saveAllSettings(controller, themeTab, ocrTab, functionsTab);
                controller.showNotification("設置以保存", "你的偏好設置已成功更新");
            }
        });
    }

    /**
     * 创建外观主题标签页 - 修复版本（增强文字可见性）
     */
    private Tab createThemeTab(MainController controller) {
        Tab tab = new Tab();

        VBox content = new VBox(20);
        content.setPadding(new Insets(25));
        content.setStyle("-fx-background-color: rgba(40,40,40,0.7);"); // 添加背景色增强可见性

        // 主题选择区块
        VBox themeSection = createSettingsSection("🎨 外觀主題", "選擇你喜歡的風格");

        // 主题预览网格
        VBox themePreviewContainer = new VBox(15);

        // 当前主题显示 - 修复版本
        Label currentThemeLabel = new Label("當前主題: " + controller.getSettingsManager().getCurrentTheme().getDisplayName());
        currentThemeLabel.setStyle(
                "-fx-text-fill: white; " +
                        "-fx-font-weight: bold; " +
                        "-fx-font-size: 13px; " +
                        "-fx-background-color: rgba(52,152,219,0.3); " +
                        "-fx-padding: 10 15 10 15; " +
                        "-fx-background-radius: 8; " +
                        "-fx-border-color: rgba(52,152,219,0.5); " +
                        "-fx-border-width: 1; " +
                        "-fx-border-radius: 8;"
        );

        // 主题选项区域
        VBox themeOptions = new VBox(12);
        ToggleGroup themeGroup = new ToggleGroup();

        for (E_Reader.settings.SettingsManager.ThemeMode theme : E_Reader.settings.SettingsManager.ThemeMode.values()) {
            HBox themeOption = createThemePreviewOption(theme, themeGroup, controller);
            themeOptions.getChildren().add(themeOption);
        }

        // 设置当前选中的主题
        themeGroup.getToggles().forEach(toggle -> {
            RadioButton rb = (RadioButton) toggle;
            if (rb.getText().equals(controller.getSettingsManager().getCurrentTheme().getDisplayName())) {
                rb.setSelected(true);
            }
        });

        // 即时预览提示 - 修复版本
        Label previewTip = new Label("💡 提示：選擇主題後會立即預覽效果");
        previewTip.setStyle(
                "-fx-text-fill: rgba(255,255,255,0.9); " +
                        "-fx-font-size: 12px; " +
                        "-fx-font-weight: 500; " +
                        "-fx-background-color: rgba(255,255,255,0.08); " +
                        "-fx-padding: 10 15 10 15; " +
                        "-fx-background-radius: 8; " +
                        "-fx-border-color: rgba(255,255,255,0.2); " +
                        "-fx-border-width: 1; " +
                        "-fx-border-radius: 8;"
        );

        themePreviewContainer.getChildren().addAll(currentThemeLabel, themeOptions, previewTip);
        themeSection.getChildren().add(themePreviewContainer);

        // 亮度控制区块 - 修复版本（使其真正可用）
        VBox brightnessSection = createSettingsSection("🔆 顯示亮度", "調整閱讀舒適度");

        Slider brightnessSlider = new Slider(10, 100, currentBrightness);
        brightnessSlider.setShowTickLabels(true);
        brightnessSlider.setShowTickMarks(true);
        brightnessSlider.setMajorTickUnit(20);
        brightnessSlider.setMinorTickCount(1);
        brightnessSlider.setStyle(
                "-fx-background-color: rgba(60,60,60,0.8); " +
                        "-fx-border-radius: 8; " +
                        "-fx-background-radius: 8; " +
                        "-fx-border-color: rgba(255,255,255,0.2); " +
                        "-fx-border-width: 1; " +
                        "-fx-padding: 10;"
        );

        Label brightnessLabel = new Label(String.format("%.0f%%", currentBrightness));
        brightnessLabel.setStyle(
                "-fx-text-fill: white; " +
                        "-fx-font-weight: bold; " +
                        "-fx-font-size: 14px; " +
                        "-fx-background-color: rgba(52,152,219,0.3); " +
                        "-fx-padding: 5 10 5 10; " +
                        "-fx-background-radius: 6;"
        );

        // 实现真正的亮度调节功能
        brightnessSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            double brightness = newVal.doubleValue();
            currentBrightness = brightness;
            brightnessLabel.setText(String.format("%.0f%%", brightness));

            // 应用亮度到应用程序
            applyBrightnessToApp(controller, brightness);
        });

        HBox brightnessControl = new HBox(15);
        brightnessControl.setAlignment(Pos.CENTER_LEFT);
        brightnessControl.getChildren().addAll(brightnessSlider, brightnessLabel);
        HBox.setHgrow(brightnessSlider, Priority.ALWAYS);

        brightnessSection.getChildren().add(brightnessControl);

        // 分隔线 - 修复版本
        Separator separator = new Separator();
        separator.setStyle("-fx-background-color: rgba(255,255,255,0.25); -fx-border-color: rgba(255,255,255,0.25);");

        content.getChildren().addAll(themeSection, separator, brightnessSection);

        ScrollPane scrollPane = new ScrollPane(content);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle(
                "-fx-background-color: transparent; " +
                        "-fx-background: transparent; " +
                        "-fx-border-color: transparent;"
        );

        tab.setContent(scrollPane);
        return tab;
    }

    /**
     * 应用亮度到应用程序 - 新增功能
     */
    private void applyBrightnessToApp(MainController controller, double brightness) {
        // 将亮度值转换为不透明度 (10-100% -> 0.1-1.0)
        double opacity = brightness / 100.0;

        // 应用到主窗口
        if (controller.getPrimaryStage() != null && controller.getPrimaryStage().getScene() != null) {
            controller.getPrimaryStage().getScene().getRoot().setOpacity(opacity);
        }

        // 保存亮度设置到SettingsManager
        controller.getSettingsManager().setEyeCareBrightness((int) brightness);

        // 显示反馈
        controller.showNotification("亮度調整", String.format("亮度已調整至 %.0f%%", brightness));
    }

    /**
     * 创建主题预览选项 - 修复版本
     */
    private HBox createThemePreviewOption(E_Reader.settings.SettingsManager.ThemeMode theme,
                                          ToggleGroup group, MainController controller) {
        HBox option = new HBox(15);
        option.setAlignment(Pos.CENTER_LEFT);
        option.setPadding(new Insets(12));
        option.setStyle(
                "-fx-background-color: rgba(50,50,50,0.8); " +
                        "-fx-border-color: rgba(255,255,255,0.2); " +
                        "-fx-border-width: 1; " +
                        "-fx-border-radius: 10; " +
                        "-fx-background-radius: 10;"
        );

        // 主题预览色块 - 修复版本
        VBox colorPreview = new VBox(0);
        colorPreview.setPrefSize(70, 45);
        colorPreview.setAlignment(Pos.CENTER);
        colorPreview.setStyle(
                "-fx-background-color: " + theme.getBackgroundColor() + "; " +
                        "-fx-border-color: " + theme.getTextColor() + "; " +
                        "-fx-border-width: 2; " +
                        "-fx-border-radius: 6; " +
                        "-fx-background-radius: 6;"
        );

        // 示例文字 - 修复版本
        Label sampleText = new Label("Aa 文字");
        sampleText.setStyle(
                "-fx-text-fill: " + theme.getTextColor() + "; " +
                        "-fx-font-size: 12px; " +
                        "-fx-font-weight: bold;"
        );
        colorPreview.getChildren().add(sampleText);

        // 主题选项 - 修复版本
        RadioButton themeRadio = new RadioButton(theme.getDisplayName());
        themeRadio.setToggleGroup(group);
        themeRadio.setStyle(
                "-fx-text-fill: white; " +
                        "-fx-font-size: 13px; " +
                        "-fx-font-weight: 600;"
        );

        // 主题描述 - 修复版本
        String description = getThemeDescription(theme);
        Label descLabel = new Label(description);
        descLabel.setStyle(
                "-fx-text-fill: rgba(255,255,255,0.8); " +
                        "-fx-font-size: 11px; " +
                        "-fx-wrap-text: true; " +
                        "-fx-max-width: 200;"
        );

        VBox textInfo = new VBox(5);
        textInfo.getChildren().addAll(themeRadio, descLabel);

        // 即时预览功能
        themeRadio.setOnAction(e -> {
            if (themeRadio.isSelected()) {
                // 临时预览主题效果
                controller.getSettingsManager().setThemeMode(theme);
                controller.applySettings();
            }
        });

        option.getChildren().addAll(colorPreview, textInfo);
        return option;
    }

    /**
     * 获取主题描述
     */
    private String getThemeDescription(E_Reader.settings.SettingsManager.ThemeMode theme) {
        switch (theme) {
            case LIGHT: return "適合白天閱讀，清爽明亮";
            case DARK: return "適合夜晚閱讀，護眼舒適";
            case BLACK: return "純黑背景，OLED螢幕友好";
            case EYE_CARE: return "護眼模式，減少藍光傷害";
            case SEPIA: return "復古泛黃，模擬紙張質感";
            default: return "經典主題";
        }
    }


    /**
     * 创建OCR设置标签页 - 修复版本
     */
    private Tab createOcrTab(MainController controller) {
        Tab tab = new Tab();

        VBox content = new VBox(20);
        content.setPadding(new Insets(25));
        content.setStyle("-fx-background-color: rgba(40,40,40,0.7);"); // 添加背景色

        // OCR状态显示 - 修复版本
        VBox statusSection = createSettingsSection("📊 OCR狀態", "當前文字辨識設定");

        Label statusLabel = new Label(controller.getTextExtractor().getOcrStatus());
        statusLabel.setStyle(
                "-fx-font-weight: bold; " +
                        "-fx-text-fill: white; " +
                        "-fx-font-size: 13px; " +
                        "-fx-background-color: rgba(52,152,219,0.2); " +
                        "-fx-padding: 15 20 15 20; " +
                        "-fx-background-radius: 10; " +
                        "-fx-border-color: rgba(52,152,219,0.4); " +
                        "-fx-border-width: 1; " +
                        "-fx-border-radius: 10;"
        );
        statusSection.getChildren().add(statusLabel);

        // OCR模型选择 - 修复版本
        VBox modelSection = createSettingsSection("🔧 OCR模型", "選擇文字識別精準度");

        ToggleGroup ocrGroup = new ToggleGroup();
        VBox modelOptions = new VBox(15);

        for (E_Reader.settings.SettingsManager.OcrModel model : E_Reader.settings.SettingsManager.OcrModel.values()) {
            HBox modelOption = createOcrModelOption(model, ocrGroup);
            modelOptions.getChildren().add(modelOption);
        }

        // 设置当前选中的模型
        ocrGroup.getToggles().forEach(toggle -> {
            RadioButton rb = (RadioButton) toggle;
            if (rb.getText().equals(controller.getSettingsManager().getOcrModel().getDisplayName())) {
                rb.setSelected(true);
            }
        });

        modelSection.getChildren().add(modelOptions);

        // OCR功能选项 - 修复版本
        VBox optionsSection = createSettingsSection("⚙️ 識別選項", "自訂OCR行為");

        CheckBox showFailuresCheckBox = new CheckBox("顯示文字檢測失敗通知");
        showFailuresCheckBox.setSelected(true);
        showFailuresCheckBox.setStyle(
                "-fx-text-fill: white; " +
                        "-fx-font-size: 12px; " +
                        "-fx-font-weight: 500;"
        );

        CheckBox autoFallbackCheckBox = new CheckBox("快速模型失敗時自動切換到最佳模型");
        autoFallbackCheckBox.setSelected(true);
        autoFallbackCheckBox.setStyle(
                "-fx-text-fill: white; " +
                        "-fx-font-size: 12px; " +
                        "-fx-font-weight: 500;"
        );

        CheckBox enhancedPreprocessCheckBox = new CheckBox("啟用增強圖片預處理");
        enhancedPreprocessCheckBox.setSelected(true);
        enhancedPreprocessCheckBox.setStyle(
                "-fx-text-fill: white; " +
                        "-fx-font-size: 12px; " +
                        "-fx-font-weight: 500;"
        );

        VBox checkBoxContainer = new VBox(12);
        checkBoxContainer.getChildren().addAll(showFailuresCheckBox, autoFallbackCheckBox, enhancedPreprocessCheckBox);
        optionsSection.getChildren().add(checkBoxContainer);

// 性能調整 - 修復版本
        VBox performanceSection = createSettingsSection("⚡ 性能調整", "平衡速度與精度");

        Label performanceLabel = new Label("處理優先級:");
        performanceLabel.setStyle(
                "-fx-text-fill: white; " +
                        "-fx-font-size: 12px; " +
                        "-fx-font-weight: 500;"
        );

        ComboBox<String> performanceCombo = new ComboBox<>();
        performanceCombo.getItems().addAll("速度優先", "平衡模式", "精度優先");
        performanceCombo.setValue("平衡模式");
        performanceCombo.setStyle(
                "-fx-background-color: rgba(60,60,60,0.8); " +
                        "-fx-border-color: rgba(255,255,255,0.3); " +
                        "-fx-border-width: 1; " +
                        "-fx-border-radius: 8; " +
                        "-fx-background-radius: 8; " +
                        "-fx-text-fill: white; " +
                        "-fx-font-size: 12px; " +
                        "-fx-padding: 8 12 8 12;"
        );


        VBox performanceControls = new VBox(10);
        performanceControls.getChildren().addAll(performanceLabel, performanceCombo);
        performanceSection.getChildren().add(performanceControls);

        // 分隔线
        Separator separator1 = new Separator();
        separator1.setStyle("-fx-background-color: rgba(255,255,255,0.25);");

        Separator separator2 = new Separator();
        separator2.setStyle("-fx-background-color: rgba(255,255,255,0.25);");

        content.getChildren().addAll(
                statusSection,
                separator1,
                modelSection,
                separator2,
                optionsSection,
                performanceSection
        );

        ScrollPane scrollPane = new ScrollPane(content);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background-color: transparent; -fx-background: transparent;");

        tab.setContent(scrollPane);
        return tab;
    }

    /**
     * 创建OCR模型选项 - 修复版本
     */
    private HBox createOcrModelOption(E_Reader.settings.SettingsManager.OcrModel model, ToggleGroup group) {
        HBox option = new HBox(15);
        option.setAlignment(Pos.CENTER_LEFT);
        option.setPadding(new Insets(15));
        option.setStyle(
                "-fx-background-color: rgba(50,50,50,0.8); " +
                        "-fx-border-color: rgba(255,255,255,0.2); " +
                        "-fx-border-width: 1; " +
                        "-fx-border-radius: 10; " +
                        "-fx-background-radius: 10;"
        );

        // 模型图标 - 修复版本
        Label iconLabel = new Label(model == E_Reader.settings.SettingsManager.OcrModel.FAST ? "⚡" : "🎯");
        iconLabel.setStyle(
                "-fx-font-size: 26px; " +
                        "-fx-background-color: rgba(52,152,219,0.3); " +
                        "-fx-padding: 10; " +
                        "-fx-background-radius: 50%; " +
                        "-fx-border-color: rgba(52,152,219,0.5); " +
                        "-fx-border-width: 1; " +
                        "-fx-border-radius: 50%;"
        );

        // 模型信息 - 修复版本
        RadioButton modelRadio = new RadioButton(model.getDisplayName());
        modelRadio.setToggleGroup(group);
        modelRadio.setStyle(
                "-fx-text-fill: white; " +
                        "-fx-font-size: 14px; " +
                        "-fx-font-weight: 700;"
        );

        Label descLabel = new Label(model.getDescription());
        descLabel.setStyle(
                "-fx-text-fill: rgba(255,255,255,0.8); " +
                        "-fx-font-size: 11px; " +
                        "-fx-wrap-text: true; " +
                        "-fx-max-width: 250;"
        );

        VBox modelInfo = new VBox(6);
        modelInfo.getChildren().addAll(modelRadio, descLabel);

        option.getChildren().addAll(iconLabel, modelInfo);
        return option;
    }

    /**
     * 创建功能选项标签页 - 修复版本
     */
    private Tab createFunctionsTab(MainController controller) {
        Tab tab = new Tab();

        VBox content = new VBox(20);
        content.setPadding(new Insets(25));
        content.setStyle("-fx-background-color: rgba(40,40,40,0.7);"); // 添加背景色

        // 文件管理 - 修復版本
        VBox fileSection = createSettingsSection("📁 文件管理", "自訂文件處理行為");

        CheckBox rememberFileCheckBox = createStyledCheckBox("記住最後開啟的文件", "下次啟動時自動載入上次閱讀的內容");
        rememberFileCheckBox.setSelected(controller.getSettingsManager().isRememberLastFile());

        CheckBox autoBookmarkCheckBox = createStyledCheckBox("自動書籤", "自動記住每個文件的閱讀位置");
        autoBookmarkCheckBox.setSelected(true);

        fileSection.getChildren().addAll(rememberFileCheckBox, autoBookmarkCheckBox);

        // 介面顯示 - 修復版本
        VBox interfaceSection = createSettingsSection("🖥️ 介面顯示", "自訂使用者介面");

        CheckBox showPageNumbersCheckBox = createStyledCheckBox("顯示頁碼資訊", "在介面上顯示目前頁數和總頁數");
        showPageNumbersCheckBox.setSelected(controller.getSettingsManager().isShowPageNumbers());

        CheckBox showReadingTimeCheckBox = createStyledCheckBox("顯示閱讀時間", "追蹤並顯示累計閱讀時間");
        showReadingTimeCheckBox.setSelected(true);

        CheckBox compactModeCheckBox = createStyledCheckBox("緊湊模式", "減少介面元素大小，增加閱讀空間");
        compactModeCheckBox.setSelected(false);

        interfaceSection.getChildren().addAll(showPageNumbersCheckBox, showReadingTimeCheckBox, compactModeCheckBox);

        // 閱讀體驗 - 修復版本
        VBox readingSection = createSettingsSection("📖 閱讀體驗", "優化閱讀舒適度");

        CheckBox enableTouchNavCheckBox = createStyledCheckBox("觸控導覽", "支援觸控手勢翻頁");
        enableTouchNavCheckBox.setSelected(controller.getSettingsManager().isEnableTouchNavigation());

        CheckBox smoothScrollCheckBox = createStyledCheckBox("平滑滾動", "啟用平滑的頁面切換動畫");
        smoothScrollCheckBox.setSelected(true);

        CheckBox fullscreenReadingCheckBox = createStyledCheckBox("全螢幕閱讀提示", "進入全螢幕時顯示操作提示");
        fullscreenReadingCheckBox.setSelected(true);

        readingSection.getChildren().addAll(enableTouchNavCheckBox, smoothScrollCheckBox, fullscreenReadingCheckBox);

        // 自動保存設定 - 修復版本
        VBox autoSaveSection = createSettingsSection("💾 自動保存", "設定自動保存間隔");

        Label intervalLabel = new Label("自動保存間隔 (秒):");
        intervalLabel.setStyle(
                "-fx-text-fill: white; " +
                        "-fx-font-size: 12px; " +
                        "-fx-font-weight: 500;"
        );

        Slider intervalSlider = new Slider(10, 300, controller.getSettingsManager().getAutoSaveInterval());
        intervalSlider.setShowTickLabels(true);
        intervalSlider.setShowTickMarks(true);
        intervalSlider.setMajorTickUnit(60);
        intervalSlider.setStyle(
                "-fx-background-color: rgba(60,60,60,0.8); " +
                        "-fx-border-radius: 8; " +
                        "-fx-background-radius: 8; " +
                        "-fx-border-color: rgba(255,255,255,0.2); " +
                        "-fx-border-width: 1; " +
                        "-fx-padding: 10;"
        );

        Label intervalValueLabel = new Label(String.valueOf(controller.getSettingsManager().getAutoSaveInterval()) + " 秒");
        intervalValueLabel.setStyle(
                "-fx-text-fill: white; " +
                        "-fx-font-weight: bold; " +
                        "-fx-font-size: 12px; " +
                        "-fx-background-color: rgba(52,152,219,0.3); " +
                        "-fx-padding: 5 10 5 10; " +
                        "-fx-background-radius: 6;"
        );
        intervalSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            intervalValueLabel.setText(String.format("%.0f 秒", newVal.doubleValue()));
        });

        HBox intervalControl = new HBox(15);
        intervalControl.setAlignment(Pos.CENTER_LEFT);
        intervalControl.getChildren().addAll(intervalSlider, intervalValueLabel);
        HBox.setHgrow(intervalSlider, Priority.ALWAYS);

        autoSaveSection.getChildren().addAll(intervalLabel, intervalControl);

        // 分隔線
        Separator separator1 = new Separator();
        separator1.setStyle("-fx-background-color: rgba(255,255,255,0.25);");

        Separator separator2 = new Separator();
        separator2.setStyle("-fx-background-color: rgba(255,255,255,0.25);");

        Separator separator3 = new Separator();
        separator3.setStyle("-fx-background-color: rgba(255,255,255,0.25);");

        content.getChildren().addAll(
                fileSection,
                separator1,
                interfaceSection,
                separator2,
                readingSection,
                separator3,
                autoSaveSection
        );

        ScrollPane scrollPane = new ScrollPane(content);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background-color: transparent; -fx-background: transparent;");

        tab.setContent(scrollPane);
        return tab;
    }
    /**
     * 創建設定區塊的輔助方法 - 修復版本
     */
    private VBox createSettingsSection(String title, String description) {
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

    /**
     * 創建 iOS 風格的 CheckBox - 修復版本
     */
    private CheckBox createStyledCheckBox(String text, String description) {
        VBox container = new VBox(6);

        CheckBox checkBox = new CheckBox(text);
        checkBox.setStyle(
                "-fx-text-fill: white; " +
                        "-fx-font-size: 13px; " +
                        "-fx-font-weight: 500;"
        );

        Label descLabel = new Label(description);
        descLabel.setStyle(
                "-fx-text-fill: rgba(255,255,255,0.7); " +
                        "-fx-font-size: 11px; " +
                        "-fx-padding: 0 0 0 25; " +
                        "-fx-wrap-text: true;"
        );

        container.getChildren().addAll(checkBox, descLabel);

        // 返回 CheckBox 而不是容器
        return checkBox;
    }

    /**
     * 保存所有設定
     */
    private void saveAllSettings(MainController controller, Tab themeTab, Tab ocrTab, Tab functionsTab) {
        // 保存主題設定
        saveThemeSettings(controller, themeTab);

        // 保存 OCR 設定
        saveOcrSettings(controller, ocrTab);

        // 保存功能設定
        saveFunctionSettings(controller, functionsTab);

        // 應用設定
        controller.getSettingsManager().saveSettings();
        controller.applySettings();
    }

    private void saveThemeSettings(MainController controller, Tab themeTab) {
        // 主題已在選擇時即時應用，無需額外處理
        // 但需要保存亮度設定
        controller.getSettingsManager().setEyeCareBrightness((int) currentBrightness);
    }

    private void saveOcrSettings(MainController controller, Tab ocrTab) {
        ScrollPane scrollPane = (ScrollPane) ocrTab.getContent();
        VBox content = (VBox) scrollPane.getContent();

        // 查找 OCR 模型選擇
        content.getChildren().forEach(node -> {
            if (node instanceof VBox) {
                VBox section = (VBox) node;
                section.getChildren().forEach(child -> {
                    if (child instanceof VBox) {
                        VBox options = (VBox) child;
                        options.getChildren().forEach(option -> {
                            if (option instanceof HBox) {
                                HBox optionBox = (HBox) option;
                                optionBox.getChildren().forEach(item -> {
                                    if (item instanceof VBox) {
                                        VBox modelInfo = (VBox) item;
                                        modelInfo.getChildren().forEach(info -> {
                                            if (info instanceof RadioButton) {
                                                RadioButton rb = (RadioButton) info;
                                                if (rb.isSelected()) {
                                                    // 更新 OCR 模型設定
                                                    String selectedText = rb.getText();
                                                    for (E_Reader.settings.SettingsManager.OcrModel model :
                                                            E_Reader.settings.SettingsManager.OcrModel.values()) {
                                                        if (model.getDisplayName().equals(selectedText)) {
                                                            controller.getSettingsManager().setOcrModel(model);
                                                            controller.getTextExtractor().updateOcrModel(model);
                                                            break;
                                                        }
                                                    }
                                                }
                                            }
                                        });
                                    }
                                });
                            }
                        });
                    }
                });
            }
        });
    }

    private void saveFunctionSettings(MainController controller, Tab functionsTab) {
        ScrollPane scrollPane = (ScrollPane) functionsTab.getContent();
        VBox content = (VBox) scrollPane.getContent();

        // 遍歷功能設定並保存
        content.getChildren().forEach(node -> {
            if (node instanceof VBox) {
                VBox section = (VBox) node;
                section.getChildren().forEach(child -> {
                    if (child instanceof CheckBox) {
                        CheckBox checkBox = (CheckBox) child;
                        String text = checkBox.getText();

                        if (text.contains("記住最後打開的文件")) {
                            controller.getSettingsManager().setRememberLastFile(checkBox.isSelected());
                        } else if (text.contains("顯示頁碼資訊")) {
                            controller.getSettingsManager().setShowPageNumbers(checkBox.isSelected());
                        } else if (text.contains("觸控導覽")) {
                            controller.getSettingsManager().setEnableTouchNavigation(checkBox.isSelected());
                        }
                    }
                });
            }
        });
    }

    private void zoomIn(MainController controller) {
        if (controller.getStateManager().isTextMode()) {
            AlertHelper.showError("提示", "圖片縮放功能僅在圖片模式下可用");
            return;
        }
        controller.getImageViewer().zoomIn();
    }

    private void zoomOut(MainController controller) {
        if (controller.getStateManager().isTextMode()) {
            AlertHelper.showError("提示", "圖片縮放功能僅在圖片模式下可用");
            return;
        }
        controller.getImageViewer().zoomOut();
    }

    private void fitWidth(MainController controller) {
        if (controller.getStateManager().isTextMode()) {
            AlertHelper.showError("提示", "圖片適配功能僅在圖片模式下可用");
            return;
        }
        controller.getImageViewer().fitToWidth();
    }

    private void fitHeight(MainController controller) {
        if (controller.getStateManager().isTextMode()) {
            AlertHelper.showError("提示", "圖片適配功能僅在圖片模式下可用");
            return;
        }
        controller.getImageViewer().fitToHeight();
    }

    private void rotateImage(MainController controller) {
        if (controller.getStateManager().isTextMode()) {
            AlertHelper.showError("提示", "圖片旋轉功能僅在圖片模式下可用");
            return;
        }
        controller.getImageViewer().getImageView().setRotate(
                controller.getImageViewer().getImageView().getRotate() + 90);
    }

    private void adjustFontSize(MainController controller, double delta) {
        if (!controller.getStateManager().isTextMode()) {
            AlertHelper.showError("提示", "字體調整功能僅在文字模式下可用");
            return;
        }

        double currentSize = controller.getTextRenderer().getFontSize();
        double newSize = Math.max(8, Math.min(36, currentSize + delta)); // 限制字體大小範圍
        controller.getTextRenderer().setFontSize(newSize);
        controller.showNotification("字體調整",
                String.format("字體大小已調整至 %.0f", newSize));
    }

    private void showLineSpacingDialog(MainController controller) {
        if (!controller.getStateManager().isTextMode()) {
            AlertHelper.showError("提示", "行距調整功能僅在文字模式下可用");
            return;
        }

        Dialog<Double> dialog = new Dialog<>();
        dialog.setTitle("行距設定");
        dialog.setHeaderText("調整文字行距");

        // iOS 風格對話框樣式 - 修復版本
        dialog.getDialogPane().setStyle(
                "-fx-background-color: rgba(35,35,35,0.98); " +
                        "-fx-border-color: rgba(255,255,255,0.4); " +
                        "-fx-border-width: 1; " +
                        "-fx-border-radius: 12; " +
                        "-fx-background-radius: 12; " +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.6), 15, 0, 0, 5);"
        );

        // 修復對話框文字顏色
        dialog.getDialogPane().lookup(".header-panel").setStyle("-fx-text-fill: white;");

        ButtonType okButtonType = new ButtonType("確定", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(okButtonType, ButtonType.CANCEL);

        Slider spacingSlider = new Slider(1.0, 3.0, 1.5);
        spacingSlider.setShowTickLabels(true);
        spacingSlider.setShowTickMarks(true);
        spacingSlider.setMajorTickUnit(0.5);
        spacingSlider.setStyle(
                "-fx-background-color: rgba(60,60,60,0.8); " +
                        "-fx-border-radius: 8; " +
                        "-fx-background-radius: 8; " +
                        "-fx-border-color: rgba(255,255,255,0.2); " +
                        "-fx-border-width: 1; " +
                        "-fx-padding: 10;"
        );

        Label spacingLabel = new Label("1.5");
        spacingLabel.setStyle(
                "-fx-text-fill: white; " +
                        "-fx-font-weight: bold; " +
                        "-fx-background-color: rgba(52,152,219,0.3); " +
                        "-fx-padding: 5 10 5 10; " +
                        "-fx-background-radius: 6;"
        );
        spacingSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            spacingLabel.setText(String.format("%.1f", newVal.doubleValue()));
        });

        VBox content = new VBox(15);
        content.setPadding(new Insets(20));
        content.setStyle("-fx-background-color: rgba(40,40,40,0.7);");

        Label titleLabel = new Label("行距倍數:");
        titleLabel.setStyle("-fx-text-fill: white; -fx-font-size: 13px; -fx-font-weight: 500;");

        HBox sliderContainer = new HBox(15);
        sliderContainer.setAlignment(Pos.CENTER_LEFT);
        sliderContainer.getChildren().addAll(spacingSlider, spacingLabel);
        HBox.setHgrow(spacingSlider, Priority.ALWAYS);

        content.getChildren().addAll(titleLabel, sliderContainer);
        dialog.getDialogPane().setContent(content);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == okButtonType) {
                return spacingSlider.getValue();
            }
            return null;
        });

        dialog.showAndWait().ifPresent(spacing -> {
            controller.getTextRenderer().setLineSpacing(spacing);
            controller.showNotification("行距調整", "行距已設定為 " + String.format("%.1f", spacing));
        });
    }

    private void showSpeedReadingDialog(MainController controller) {
        Dialog<Integer> dialog = new Dialog<>();
        dialog.setTitle("快速閱讀設定");
        dialog.setHeaderText("設定自動翻頁間隔時間");

        // iOS 風格對話框樣式 - 修復版本
        dialog.getDialogPane().setStyle(
                "-fx-background-color: rgba(35,35,35,0.98); " +
                        "-fx-border-color: rgba(255,255,255,0.4); " +
                        "-fx-border-width: 1; " +
                        "-fx-border-radius: 12; " +
                        "-fx-background-radius: 12; " +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.6), 15, 0, 0, 5);"
        );

        // 修復對話框文字顏色
        dialog.getDialogPane().lookup(".header-panel").setStyle("-fx-text-fill: white;");

        ButtonType okButtonType = new ButtonType("確定", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(okButtonType, ButtonType.CANCEL);

        Slider speedSlider = new Slider(1, 10, 3);
        speedSlider.setShowTickLabels(true);
        speedSlider.setShowTickMarks(true);
        speedSlider.setMajorTickUnit(1);
        speedSlider.setStyle(
                "-fx-background-color: rgba(60,60,60,0.8); " +
                        "-fx-border-radius: 8; " +
                        "-fx-background-radius: 8; " +
                        "-fx-border-color: rgba(255,255,255,0.2); " +
                        "-fx-border-width: 1; " +
                        "-fx-padding: 10;"
        );

        Label speedLabel = new Label("3 秒");
        speedLabel.setStyle(
                "-fx-text-fill: white; " +
                        "-fx-font-weight: bold; " +
                        "-fx-background-color: rgba(52,152,219,0.3); " +
                        "-fx-padding: 5 10 5 10; " +
                        "-fx-background-radius: 6;"
        );
        speedSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            speedLabel.setText(newVal.intValue() + " 秒");
        });

        VBox content = new VBox(15);
        content.setPadding(new Insets(20));
        content.setStyle("-fx-background-color: rgba(40,40,40,0.7);");

        Label titleLabel = new Label("翻頁間隔:");
        titleLabel.setStyle("-fx-text-fill: white; -fx-font-size: 13px; -fx-font-weight: 500;");

        HBox sliderContainer = new HBox(15);
        sliderContainer.setAlignment(Pos.CENTER_LEFT);
        sliderContainer.getChildren().addAll(speedSlider, speedLabel);
        HBox.setHgrow(speedSlider, Priority.ALWAYS);

        content.getChildren().addAll(titleLabel, sliderContainer);
        dialog.getDialogPane().setContent(content);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == okButtonType) {
                return (int) speedSlider.getValue();
            }
            return null;
        });

        dialog.showAndWait().ifPresent(speed -> {
            // 如果自動翻頁正在運行，先停止
            if (controller.getStateManager().isAutoScrolling()) {
                controller.toggleAutoScroll();
            }

            // 開始自定速度的自動翻頁
            controller.getStateManager().setAutoScrolling(true);
            autoScrollBtn.setText("⏸ 停止");
            autoScrollBtn.setStyle(DANGER_BUTTON_STYLE);

            controller.getTimerManager().startAutoScrollWithInterval(() -> {
                boolean canGoNext;
                if (controller.getStateManager().isTextMode()) {
                    canGoNext = controller.getTextRenderer().getCurrentPageIndex() <
                            controller.getTextRenderer().getTotalPages() - 1;
                } else {
                    canGoNext = controller.getImageViewer().canGoNext();
                }

                if (canGoNext) {
                    controller.goToNextPage();
                } else {
                    controller.toggleAutoScroll();
                }
            }, speed * 1000L);

            controller.showNotification("快速閱讀", "自動翻頁已啟動，間隔 " + speed + " 秒");
        });
    }
}