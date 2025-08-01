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

/**
 * UI控制面板工廠 - 負責創建和管理UI控制元件
 * 根據模式動態顯示相應的功能按鈕
 */
public class UIControlsFactory {
    
    // UI元件引用
    private Label pageLabel;
    private TextField pageField;
    private Button textModeBtn;
    private Button autoScrollBtn;
    private Button nightModeBtn;
    private Button eyeCareBtn;
    private Button toggleNavBarBtn; // 新增：導覽列切換按鈕
    
    // 圖片模式專用按鈕
    private Button zoomInBtn;
    private Button zoomOutBtn;
    private Button fitWidthBtn;
    private Button fitHeightBtn;
    private Button rotateBtn;
    
    // 文字模式專用按鈕
    private Button fontSizeIncBtn;
    private Button fontSizeDecBtn;
    private Button lineSpacingBtn;
    private Button searchBtn;
    
    // 控制列容器
    private HBox topControls;
    private HBox bottomControls;
    
    // 按鈕樣式常量
    private static final String BUTTON_STYLE = 
        "-fx-background-color: #404040; -fx-text-fill: white; " +
        "-fx-border-radius: 5; -fx-background-radius: 5; " +
        "-fx-padding: 8 12 8 12; -fx-font-size: 12px;";
    
    private static final String TEXT_FIELD_STYLE = 
        "-fx-background-color: #404040; -fx-text-fill: white; " +
        "-fx-border-color: #666666; -fx-border-radius: 3;";
    
    /**
     * 創建上方控制列
     */
    public HBox createTopControls(MainController controller) {
        // 主要功能按鈕
        Button returnToManagerBtn = createButton("↩️ 返回檔案管理", () -> {
            // 返回檔案管理器，會自動關閉當前檔案
            controller.showFileManager();
        });
        
        // 導覽列控制按鈕
        toggleNavBarBtn = createButton("🙈 隱藏導覽列", () -> {
            controller.toggleNavigationBar();
        });
        
        // 已移除檔案管理器按鈕和離開按鈕
        Button bookmarkBtn = createButton("🔖 書籤管理", controller::showBookmarkDialog);
        Button settingsBtn = createButton("⚙️ 設定", controller::showSettingsDialog);
        Button fullscreenBtn = createButton("🔲 全螢幕", controller::toggleFullscreen);
        
        // 共用功能按鈕
        autoScrollBtn = createButton("⏯️ 自動翻頁", controller::toggleAutoScroll);
        nightModeBtn = createButton("🌙 夜間模式", controller::toggleNightMode);
        eyeCareBtn = createButton("👁️ 護眼模式", controller::toggleEyeCareMode);
        textModeBtn = createButton("📖 文字模式", controller::toggleTextMode);
        
        // 文字模式專用按鈕
        searchBtn = createButton("🔍 搜尋文字", () -> showSearchDialog(controller));
        
        // 創建控制列容器
        topControls = new HBox(10);
        topControls.setAlignment(Pos.CENTER);
        topControls.setPadding(new Insets(10));
        topControls.setStyle("-fx-background-color: #333333;");
        
        // 基本按鈕始終顯示
        topControls.getChildren().addAll(
            returnToManagerBtn, toggleNavBarBtn, bookmarkBtn, settingsBtn, textModeBtn, autoScrollBtn, 
            nightModeBtn, eyeCareBtn, fullscreenBtn
        );
        
        return topControls;
    }
    
    /**
     * 創建下方控制列
     */
    public HBox createBottomControls(MainController controller) {
        // 導航控制按鈕
        Button firstPageBtn = createButton("⏮️ 首頁", controller::goToFirstPage);
        Button prevBtn = createButton("◀️ 上頁", controller::goToPreviousPage);
        Button nextBtn = createButton("下頁 ▶️", controller::goToNextPage);
        Button lastPageBtn = createButton("末頁 ⏭️", controller::goToLastPage);
        
        // 頁面跳轉控制
        pageField = new TextField();
        pageField.setPrefWidth(60);
        pageField.setPromptText("頁數");
        pageField.setStyle(TEXT_FIELD_STYLE);
        
        Button goToPageBtn = createButton("跳轉", () -> handleGoToPage(controller));
        
        // 頁面跳轉事件處理
        pageField.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ENTER) {
                handleGoToPage(controller);
            }
        });
        
        // 圖片模式專用按鈕
        zoomInBtn = createButton("🔍+", () -> zoomIn(controller));
        zoomOutBtn = createButton("🔍-", () -> zoomOut(controller));
        fitWidthBtn = createButton("適合寬度", () -> fitWidth(controller));
        fitHeightBtn = createButton("適合高度", () -> fitHeight(controller));
        rotateBtn = createButton("🔄 旋轉", () -> rotateImage(controller));
        
        // 文字模式專用按鈕
        fontSizeIncBtn = createButton("A+", () -> adjustFontSize(controller, 2));
        fontSizeDecBtn = createButton("A-", () -> adjustFontSize(controller, -2));
        lineSpacingBtn = createButton("📏 行距", () -> showLineSpacingDialog(controller));
        
        // 閱讀模式控制
        Button focusModeBtn = createButton("🎯 専注模式", controller::toggleFocusMode);
        Button speedReadBtn = createButton("⚡ 快速閱讀", () -> showSpeedReadingDialog(controller));
        
        // OCR設定按鈕（位於左下角）
        Button ocrSettingsBtn = createButton("🔧 OCR設定", () -> showOcrSettingsDialog(controller));
        ocrSettingsBtn.setStyle(BUTTON_STYLE + "; -fx-background-color: #0078d4;");
        
        // 創建控制列容器
        bottomControls = new HBox(10);
        bottomControls.setAlignment(Pos.CENTER);
        bottomControls.setPadding(new Insets(10));
        bottomControls.setStyle("-fx-background-color: #333333;");
        
        // 創建左下角區域放置OCR設定按鈕
        HBox leftBottomControls = new HBox(10);
        leftBottomControls.setAlignment(Pos.CENTER_LEFT);
        leftBottomControls.getChildren().add(ocrSettingsBtn);
        
        // 創建右下角區域放置其他按鈕
        HBox rightBottomControls = new HBox(10);
        rightBottomControls.setAlignment(Pos.CENTER_RIGHT);
        rightBottomControls.getChildren().addAll(focusModeBtn, speedReadBtn);
        
        // 創建包含基本導航按鈕的中央區域
        HBox centerBottomControls = new HBox(10);
        centerBottomControls.setAlignment(Pos.CENTER);
        centerBottomControls.getChildren().addAll(
            firstPageBtn, prevBtn, nextBtn, lastPageBtn,
            new Separator(), pageField, goToPageBtn
        );
        
        // 使用BorderPane來排列左、中、右三個區域
        BorderPane bottomLayout = new BorderPane();
        bottomLayout.setLeft(leftBottomControls);
        bottomLayout.setCenter(centerBottomControls);
        bottomLayout.setRight(rightBottomControls);
        
        // 將BorderPane包裝在HBox中
        bottomControls.getChildren().add(bottomLayout);
        HBox.setHgrow(bottomLayout, Priority.ALWAYS);
        
        return bottomControls;
    }
    
    /**
     * 根據當前模式更新控制按鈕的顯示
     */
    public void updateControlsForMode(boolean isTextMode) {
        // 更新上方控制列
        updateTopControlsForMode(isTextMode);
        
        // 更新下方控制列的模式專用按鈕
        updateBottomControlsForMode(isTextMode);
    }
    
    /**
     * 更新上方控制列的按鈕顯示
     */
    private void updateTopControlsForMode(boolean isTextMode) {
        if (topControls == null) return;
        
        // 移除模式專用按鈕
        topControls.getChildren().remove(searchBtn);
        
        // 根據模式添加相應按鈕
        if (isTextMode) {
            // 文字模式：添加搜尋按鈕
            if (!topControls.getChildren().contains(searchBtn)) {
                // 在文字模式按鈕後添加搜尋按鈕
                int textModeIndex = topControls.getChildren().indexOf(textModeBtn);
                if (textModeIndex >= 0) {
                    topControls.getChildren().add(textModeIndex + 1, searchBtn);
                }
            }
        }
    }
    
    /**
     * 更新下方控制列的按鈕顯示
     */
    private void updateBottomControlsForMode(boolean isTextMode) {
        if (bottomControls == null) return;
        
        // 獲取BorderPane（假設它是bottomControls的第一個子元素）
        if (bottomControls.getChildren().isEmpty()) return;
        
        BorderPane bottomLayout = (BorderPane) bottomControls.getChildren().get(0);
        HBox centerBottomControls = (HBox) bottomLayout.getCenter();
        
        // 先移除所有模式專用按鈕
        centerBottomControls.getChildren().removeAll(
            zoomInBtn, zoomOutBtn, fitWidthBtn, fitHeightBtn, rotateBtn,
            fontSizeIncBtn, fontSizeDecBtn, lineSpacingBtn
        );
        
        // 移除多餘的分隔符（保留基本的分隔符）
        centerBottomControls.getChildren().removeIf(node -> 
            node instanceof Separator && 
            centerBottomControls.getChildren().indexOf(node) > 1); // 保留第一個分隔符
        
        // 根據模式添加相應按鈕
        if (isTextMode) {
            // 文字模式：添加字體和行距控制
            centerBottomControls.getChildren().add(new Separator());
            centerBottomControls.getChildren().add(fontSizeIncBtn);
            centerBottomControls.getChildren().add(fontSizeDecBtn);
            centerBottomControls.getChildren().add(lineSpacingBtn);
        } else {
            // 圖片模式：添加縮放和旋轉控制
            centerBottomControls.getChildren().add(new Separator());
            centerBottomControls.getChildren().add(zoomInBtn);
            centerBottomControls.getChildren().add(zoomOutBtn);
            centerBottomControls.getChildren().add(fitWidthBtn);
            centerBottomControls.getChildren().add(fitHeightBtn);
            centerBottomControls.getChildren().add(rotateBtn);
        }
    }
    
    /**
     * 創建按鈕的輔助方法
     */
    private Button createButton(String text, Runnable action) {
        Button button = new Button(text);
        button.setStyle(BUTTON_STYLE);
        button.setOnAction(e -> action.run());
        return button;
    }
    
    /**
     * 處理跳轉到指定頁面
     */
    private void handleGoToPage(MainController controller) {
        try {
            int pageNum = Integer.parseInt(pageField.getText());
            controller.goToPage(pageNum - 1);
            pageField.clear();
        } catch (NumberFormatException ex) {
            AlertHelper.showError("錯誤", "請輸入有效的頁數");
        }
    }
    
    // Getter方法用於外部存取UI元件
    public Label getPageLabel() { return pageLabel; }
    public TextField getPageField() { return pageField; }
    public Button getTextModeButton() { return textModeBtn; }
    public Button getAutoScrollButton() { return autoScrollBtn; }
    public Button getNightModeButton() { return nightModeBtn; }
    public Button getEyeCareButton() { return eyeCareBtn; }
    public Button getToggleNavBarButton() { return toggleNavBarBtn; } // 新增
    public HBox getTopControls() { return topControls; }
    public HBox getBottomControls() { return bottomControls; }
    
    // 實作功能方法
    private void showSearchDialog(MainController controller) {
        if (!controller.getStateManager().isTextMode() || 
            controller.getStateManager().getCurrentTextPages() == null || 
            controller.getStateManager().getCurrentTextPages().isEmpty()) {
            AlertHelper.showError("提示", "搜尋功能僅在文字模式下可用");
            return;
        }

        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("搜尋文字");
        dialog.setHeaderText("在文件中搜尋文字");
        dialog.setContentText("請輸入搜尋關鍵字:");

        dialog.showAndWait().ifPresent(searchTerm -> {
            if (!searchTerm.trim().isEmpty()) {
                controller.getTextRenderer().searchText(searchTerm);
                controller.showNotification("搜尋完成", "已高亮顯示搜尋結果");
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
        double newSize = currentSize + delta;
        controller.getTextRenderer().setFontSize(newSize);
        controller.showNotification("字體調整", delta > 0 ? "字體已放大" : "字體已縮小");
    }

    private void showLineSpacingDialog(MainController controller) {
        if (!controller.getStateManager().isTextMode()) {
            AlertHelper.showError("提示", "行距調整功能僅在文字模式下可用");
            return;
        }

        Dialog<Double> dialog = new Dialog<>();
        dialog.setTitle("行距設定");
        dialog.setHeaderText("調整文字行距");

        ButtonType okButtonType = new ButtonType("確定", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(okButtonType, ButtonType.CANCEL);

        Slider spacingSlider = new Slider(1.0, 3.0, 1.5);
        spacingSlider.setShowTickLabels(true);
        spacingSlider.setShowTickMarks(true);
        spacingSlider.setMajorTickUnit(0.5);

        Label spacingLabel = new Label("1.5");
        spacingSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            spacingLabel.setText(String.format("%.1f", newVal.doubleValue()));
        });

        VBox content = new VBox(10);
        content.getChildren().addAll(
                new Label("行距倍數:"), spacingSlider, spacingLabel
        );
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
            // 如果自動翻頁正在運行，先停止
            if (controller.getStateManager().isAutoScrolling()) {
                controller.toggleAutoScroll();
            }
            
            // 開始自訂速度的自動翻頁
            controller.getStateManager().setAutoScrolling(true);
            autoScrollBtn.setText("⏸️ 停止翻頁");
            autoScrollBtn.setStyle(autoScrollBtn.getStyle() + "; -fx-background-color: #dc3545;");
            
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
                    controller.toggleAutoScroll(); // 停止自動翻頁
                }
            }, speed * 1000L);
        });
    }
    
    /**
     * 顯示OCR設定對話框
     */
    private void showOcrSettingsDialog(MainController controller) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("OCR文字識別設定");
        dialog.setHeaderText("選擇OCR識別模型");
        
        ButtonType okButtonType = new ButtonType("確定", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(okButtonType, ButtonType.CANCEL);
        
        VBox content = new VBox(15);
        content.setPadding(new Insets(20));
        
        // 當前OCR狀態
        Label statusLabel = new Label(controller.getTextExtractor().getOcrStatus());
        statusLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #0078d4;");
        
        // OCR模型選擇
        Label modelLabel = new Label("選擇OCR模型:");
        ComboBox<E_Reader.settings.SettingsManager.OcrModel> modelCombo = new ComboBox<>();
        modelCombo.getItems().addAll(E_Reader.settings.SettingsManager.OcrModel.values());
        modelCombo.setValue(controller.getSettingsManager().getOcrModel());
        
        // 模型描述
        Label descLabel = new Label(controller.getSettingsManager().getOcrModel().getDescription());
        descLabel.setStyle("-fx-text-fill: #666666; -fx-wrap-text: true;");
        descLabel.setMaxWidth(300);
        
        // 更新描述當選擇改變時
        modelCombo.setOnAction(e -> {
            E_Reader.settings.SettingsManager.OcrModel selected = modelCombo.getValue();
            if (selected != null) {
                descLabel.setText(selected.getDescription());
            }
        });
        
        // 模型說明
        Label infoLabel = new Label("• 快速模型：識別速度快，適合一般閱讀\n• 最佳模型：識別精度高，適合重要文件");
        infoLabel.setStyle("-fx-text-fill: #888888; -fx-font-size: 11px;");
        
        // 偵測失敗提醒設定
        CheckBox showFailuresCheckBox = new CheckBox("顯示文字偵測失敗通知");
        showFailuresCheckBox.setSelected(true); // 預設開啟
        
        content.getChildren().addAll(
            statusLabel,
            new Separator(),
            modelLabel,
            modelCombo,
            descLabel,
            new Separator(),
            infoLabel,
            showFailuresCheckBox
        );
        
        dialog.getDialogPane().setContent(content);
        
        dialog.showAndWait().ifPresent(result -> {
            if (result == okButtonType) {
                E_Reader.settings.SettingsManager.OcrModel newModel = modelCombo.getValue();
                if (newModel != controller.getSettingsManager().getOcrModel()) {
                    controller.getSettingsManager().setOcrModel(newModel);
                    controller.getTextExtractor().updateOcrModel(newModel);
                    controller.getSettingsManager().saveSettings();
                    
                    controller.showNotification("OCR設定已更新", 
                        "OCR模型已切換為: " + newModel.getDisplayName() + "\n" +
                        "新設定將在下次文字識別時生效");
                }
                
                // 更新偵測失敗通知設定
                controller.getTextExtractor().setShowDetectionFailures(showFailuresCheckBox.isSelected());
            }
        });
    }
}
