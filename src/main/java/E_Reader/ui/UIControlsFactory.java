package E_Reader.ui;

import E_Reader.utils.AlertHelper;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

/**
 * UI控制面板工廠 - 負責創建和管理UI控制元件
 */
public class UIControlsFactory {
    
    // UI元件引用
    private Label pageLabel;
    private TextField pageField;
    private Button textModeBtn;
    private Button autoScrollBtn;
    private Button nightModeBtn;
    private Button eyeCareBtn;
    
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
        Button fileManagerBtn = createButton("🗄️ 檔案管理器", controller::showFileManager);
        Button openFolderBtn = createButton("📂 圖片資料夾", controller::openImageFolder);
        Button openPdfBtn = createButton("📄 PDF檔案", controller::openPdfFile);
        Button bookmarkBtn = createButton("🔖 書籤管理", controller::showBookmarkDialog);
        Button settingsBtn = createButton("⚙️ 設定", controller::showSettingsDialog);
        Button fullscreenBtn = createButton("🔲 全螢幕", controller::toggleFullscreen);
        Button exitBtn = createButton("❌ 離開", () -> controller.getPrimaryStage().close());
        
        // 新增功能按鈕
        autoScrollBtn = createButton("⏯️ 自動翻頁", controller::toggleAutoScroll);
        nightModeBtn = createButton("🌙 夜間模式", controller::toggleNightMode);
        eyeCareBtn = createButton("👁️ 護眼模式", controller::toggleEyeCareMode);
        textModeBtn = createButton("📖 文字模式", controller::toggleTextMode);
        Button searchBtn = createButton("🔍 搜尋文字", () -> showSearchDialog(controller));
        
        // 創建控制列容器
        HBox topControls = new HBox(10);
        topControls.setAlignment(Pos.CENTER);
        topControls.setPadding(new Insets(10));
        topControls.setStyle("-fx-background-color: #333333;");
        
        topControls.getChildren().addAll(
            fileManagerBtn, openFolderBtn, openPdfBtn, bookmarkBtn, settingsBtn,
            textModeBtn, searchBtn, autoScrollBtn, nightModeBtn, eyeCareBtn,
            fullscreenBtn, exitBtn
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
        
        // 縮放控制按鈕
        Button zoomInBtn = createButton("🔍+", () -> zoomIn(controller));
        Button zoomOutBtn = createButton("🔍-", () -> zoomOut(controller));
        Button fitWidthBtn = createButton("適合寬度", () -> fitWidth(controller));
        Button fitHeightBtn = createButton("適合高度", () -> fitHeight(controller));
        Button rotateBtn = createButton("🔄 旋轉", () -> rotateImage(controller));
        
        // 閱讀模式控制
        Button focusModeBtn = createButton("🎯 専注模式", controller::toggleFocusMode);
        Button speedReadBtn = createButton("⚡ 快速閱讀", () -> showSpeedReadingDialog(controller));
        
        // 文字模式專用控制
        Button fontSizeIncBtn = createButton("A+", () -> adjustFontSize(controller, 2));
        Button fontSizeDecBtn = createButton("A-", () -> adjustFontSize(controller, -2));
        Button lineSpacingBtn = createButton("📏 行距", () -> showLineSpacingDialog(controller));
        
        // 頁面標籤
        pageLabel = new Label("頁面: 0 / 0");
        pageLabel.setStyle("-fx-text-fill: white; -fx-font-size: 14px;");
        
        // 創建控制列容器
        HBox bottomControls = new HBox(10);
        bottomControls.setAlignment(Pos.CENTER);
        bottomControls.setPadding(new Insets(10));
        bottomControls.setStyle("-fx-background-color: #333333;");
        
        bottomControls.getChildren().addAll(
            firstPageBtn, prevBtn, nextBtn, lastPageBtn,
            new Separator(), pageField, goToPageBtn,
            new Separator(), zoomInBtn, zoomOutBtn,
            fitWidthBtn, fitHeightBtn, rotateBtn,
            new Separator(), fontSizeIncBtn, fontSizeDecBtn, lineSpacingBtn,
            new Separator(), focusModeBtn, speedReadBtn,
            new Separator(), pageLabel
        );
        
        return bottomControls;
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
    
    // 實作功能方法
    private void showSearchDialog(MainController controller) {
        if (!controller.getStateManager().isTextMode() || 
            controller.getStateManager().getCurrentTextPages() == null || 
            controller.getStateManager().getCurrentTextPages().isEmpty()) {
            AlertHelper.showError("提示", "請先切換到文字模式");
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
        if (!controller.getStateManager().isTextMode()) {
            controller.getImageViewer().zoomIn();
        }
    }
    
    private void zoomOut(MainController controller) {
        if (!controller.getStateManager().isTextMode()) {
            controller.getImageViewer().zoomOut();
        }
    }
    
    private void fitWidth(MainController controller) {
        if (!controller.getStateManager().isTextMode()) {
            controller.getImageViewer().fitToWidth();
        }
    }
    
    private void fitHeight(MainController controller) {
        if (!controller.getStateManager().isTextMode()) {
            controller.getImageViewer().fitToHeight();
        }
    }
    
    private void rotateImage(MainController controller) {
        if (!controller.getStateManager().isTextMode()) {
            controller.getImageViewer().getImageView().setRotate(
                controller.getImageViewer().getImageView().getRotate() + 90);
        }
    }
    
    private void adjustFontSize(MainController controller, double delta) {
        if (!controller.getStateManager().isTextMode()) {
            return;
        }

        double currentSize = controller.getTextRenderer().getFontSize();
        double newSize = currentSize + delta;
        controller.getTextRenderer().setFontSize(newSize);
        controller.showNotification("字體調整", delta > 0 ? "字體已放大" : "字體已縮小");
    }

    private void showLineSpacingDialog(MainController controller) {
        if (!controller.getStateManager().isTextMode()) {
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
}
