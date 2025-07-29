package E_Reader.ui;

import javafx.animation.FadeTransition;
import javafx.animation.Timeline;
import javafx.animation.KeyFrame;
import javafx.scene.Node;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;

/**
 * 導覽列控制器 - 負責管理導覽列的顯示和隱藏功能
 * 包含自動隱藏、滑鼠觸發顯示、滾輪觸發顯示等功能
 */
public class NavigationBarController {
    
    // 導覽列狀態
    private boolean isNavigationBarVisible = true;
    private boolean isNavigationBarPinned = true; // 是否常駐顯示
    private boolean isMouseInTopArea = false;
    
    // UI 組件引用
    private VBox controlsContainer;
    private Stage primaryStage;
    private Node centerPane;
    
    // 動畫和計時器
    private FadeTransition showAnimation;
    private FadeTransition hideAnimation;
    private Timeline autoHideTimer;
    
    // 配置參數
    private static final double TOP_TRIGGER_HEIGHT = 50; // 頂部觸發區域高度（像素）
    private static final Duration ANIMATION_DURATION = Duration.millis(300); // 動畫持續時間
    private static final Duration AUTO_HIDE_DELAY = Duration.seconds(3); // 自動隱藏延遲時間
    private static final double HIDDEN_OPACITY = 0.0; // 隱藏時的透明度
    private static final double VISIBLE_OPACITY = 1.0; // 顯示時的透明度
    
    /**
     * 建構函數
     * @param controlsContainer 控制面板容器
     * @param primaryStage 主視窗
     * @param centerPane 中央面板（用於監聽滑鼠事件）
     */
    public NavigationBarController(VBox controlsContainer, Stage primaryStage, Node centerPane) {
        this.controlsContainer = controlsContainer;
        this.primaryStage = primaryStage;
        this.centerPane = centerPane;
        
        initializeAnimations();
        setupEventHandlers();
    }
    
    /**
     * 初始化動畫效果
     */
    private void initializeAnimations() {
        // 顯示動畫
        showAnimation = new FadeTransition(ANIMATION_DURATION, controlsContainer);
        showAnimation.setFromValue(HIDDEN_OPACITY);
        showAnimation.setToValue(VISIBLE_OPACITY);
        showAnimation.setOnFinished(e -> {
            controlsContainer.setVisible(true);
            controlsContainer.setManaged(true);
        });
        
        // 隱藏動畫
        hideAnimation = new FadeTransition(ANIMATION_DURATION, controlsContainer);
        hideAnimation.setFromValue(VISIBLE_OPACITY);
        hideAnimation.setToValue(HIDDEN_OPACITY);
        hideAnimation.setOnFinished(e -> {
            if (!isNavigationBarPinned) {
                controlsContainer.setVisible(false);
                controlsContainer.setManaged(false);
            }
        });
        
        // 自動隱藏計時器
        autoHideTimer = new Timeline(new KeyFrame(AUTO_HIDE_DELAY, e -> {
            if (!isNavigationBarPinned && !isMouseInTopArea) {
                hideNavigationBar();
            }
        }));
    }
    
    /**
     * 設置事件處理器
     */
    private void setupEventHandlers() {
        // 監聽中央面板的滑鼠移動事件
        centerPane.setOnMouseMoved(this::handleMouseMove);
        
        // 監聽滾輪事件
        centerPane.setOnScroll(this::handleScroll);
        
        // 監聽控制面板區域的滑鼠進入/離開事件
        controlsContainer.setOnMouseEntered(e -> {
            isMouseInTopArea = true;
            if (autoHideTimer != null) {
                autoHideTimer.stop();
            }
        });
        
        controlsContainer.setOnMouseExited(e -> {
            isMouseInTopArea = false;
            if (!isNavigationBarPinned) {
                startAutoHideTimer();
            }
        });
        
        // 監聽視窗大小變化
        primaryStage.widthProperty().addListener((obs, oldVal, newVal) -> updateTriggerArea());
        primaryStage.heightProperty().addListener((obs, oldVal, newVal) -> updateTriggerArea());
    }
    
    /**
     * 處理滑鼠移動事件
     */
    private void handleMouseMove(MouseEvent event) {
        double mouseY = event.getSceneY();
        
        // 檢查滑鼠是否在頂部觸發區域
        if (mouseY <= TOP_TRIGGER_HEIGHT) {
            if (!isMouseInTopArea) {
                isMouseInTopArea = true;
                if (!isNavigationBarVisible && !isNavigationBarPinned) {
                    showNavigationBarTemporarily();
                }
            }
        } else {
            if (isMouseInTopArea) {
                isMouseInTopArea = false;
                if (!isNavigationBarPinned && isNavigationBarVisible) {
                    startAutoHideTimer();
                }
            }
        }
    }
    
    /**
     * 處理滾輪事件
     */
    private void handleScroll(ScrollEvent event) {
        // 如果滾輪向上滾動且滾動到頂部區域，顯示導覽列
        if (event.getDeltaY() > 0) { // 向上滾動
            double mouseY = event.getSceneY();
            if (mouseY <= TOP_TRIGGER_HEIGHT * 2) { // 擴大觸發區域
                if (!isNavigationBarVisible && !isNavigationBarPinned) {
                    showNavigationBarTemporarily();
                }
            }
        }
    }
    
    /**
     * 切換導覽列的顯示狀態
     */
    public void toggleNavigationBar() {
        if (isNavigationBarPinned) {
            // 如果當前是常駐狀態，切換到隱藏狀態
            hideNavigationBar();
            isNavigationBarPinned = false;
        } else {
            // 如果當前是隱藏狀態，切換到常駐狀態
            showNavigationBarPermanently();
            isNavigationBarPinned = true;
        }
    }
    
    /**
     * 永久顯示導覽列（常駐模式）
     */
    public void showNavigationBarPermanently() {
        isNavigationBarPinned = true;
        isNavigationBarVisible = true;
        
        if (autoHideTimer != null) {
            autoHideTimer.stop();
        }
        
        controlsContainer.setVisible(true);
        controlsContainer.setManaged(true);
        
        if (hideAnimation.getStatus() == javafx.animation.Animation.Status.RUNNING) {
            hideAnimation.stop();
        }
        
        showAnimation.play();
    }
    
    /**
     * 隱藏導覽列
     */
    public void hideNavigationBar() {
        isNavigationBarVisible = false;
        
        if (autoHideTimer != null) {
            autoHideTimer.stop();
        }
        
        if (showAnimation.getStatus() == javafx.animation.Animation.Status.RUNNING) {
            showAnimation.stop();
        }
        
        hideAnimation.play();
    }
    
    /**
     * 臨時顯示導覽列（會自動隱藏）
     */
    public void showNavigationBarTemporarily() {
        if (isNavigationBarPinned) return; // 如果是常駐模式，不需要臨時顯示
        
        isNavigationBarVisible = true;
        
        controlsContainer.setVisible(true);
        controlsContainer.setManaged(true);
        
        if (hideAnimation.getStatus() == javafx.animation.Animation.Status.RUNNING) {
            hideAnimation.stop();
        }
        
        showAnimation.play();
        
        // 開始自動隱藏計時器
        if (!isMouseInTopArea) {
            startAutoHideTimer();
        }
    }
    
    /**
     * 開始自動隱藏計時器
     */
    private void startAutoHideTimer() {
        if (autoHideTimer != null) {
            autoHideTimer.stop();
            autoHideTimer.play();
        }
    }
    
    /**
     * 更新觸發區域（當視窗大小改變時）
     */
    private void updateTriggerArea() {
        // 可以根據視窗大小調整觸發區域
        // 目前使用固定值，可根據需要進行調整
    }
    
    /**
     * 獲取導覽列是否可見
     */
    public boolean isNavigationBarVisible() {
        return isNavigationBarVisible;
    }
    
    /**
     * 獲取導覽列是否為常駐模式
     */
    public boolean isNavigationBarPinned() {
        return isNavigationBarPinned;
    }
    
    /**
     * 設置常駐模式
     */
    public void setNavigationBarPinned(boolean pinned) {
        if (pinned) {
            showNavigationBarPermanently();
        } else {
            isNavigationBarPinned = false;
            if (!isMouseInTopArea) {
                hideNavigationBar();
            }
        }
    }
    
    /**
     * 清理資源
     */
    public void cleanup() {
        if (autoHideTimer != null) {
            autoHideTimer.stop();
        }
        if (showAnimation != null) {
            showAnimation.stop();
        }
        if (hideAnimation != null) {
            hideAnimation.stop();
        }
    }
    
    /**
     * 重置到初始狀態
     */
    public void reset() {
        isNavigationBarPinned = true;
        isNavigationBarVisible = true;
        isMouseInTopArea = false;
        
        if (autoHideTimer != null) {
            autoHideTimer.stop();
        }
        
        controlsContainer.setVisible(true);
        controlsContainer.setManaged(true);
        controlsContainer.setOpacity(VISIBLE_OPACITY);
    }
}
