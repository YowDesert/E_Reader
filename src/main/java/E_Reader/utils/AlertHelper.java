package E_Reader.utils;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.stage.Stage;

import java.util.Optional;

/**
 * 提醒對話框輔助類 - 統一管理各種提醒對話框
 */
public class AlertHelper {
    
    // 私有建構子，防止實例化
    private AlertHelper() {}
    
    /**
     * 顯示錯誤對話框
     * 
     * @param title 標題
     * @param content 內容
     */
    public static void showError(String title, String content) {
        showError(null, title, content);
    }
    
    /**
     * 顯示錯誤對話框（指定父視窗）
     * 
     * @param owner 父視窗
     * @param title 標題
     * @param content 內容
     */
    public static void showError(Stage owner, String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        if (owner != null) {
            alert.initOwner(owner);
        }
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        applyDarkTheme(alert);
        alert.showAndWait();
    }
    
    /**
     * 顯示資訊對話框
     * 
     * @param title 標題
     * @param content 內容
     */
    public static void showInfo(String title, String content) {
        showInfo(null, title, content);
    }
    
    /**
     * 顯示資訊對話框（指定父視窗）
     * 
     * @param owner 父視窗
     * @param title 標題
     * @param content 內容
     */
    public static void showInfo(Stage owner, String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        if (owner != null) {
            alert.initOwner(owner);
        }
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        applyDarkTheme(alert);
        alert.showAndWait();
    }
    
    /**
     * 顯示警告對話框
     * 
     * @param title 標題
     * @param content 內容
     */
    public static void showWarning(String title, String content) {
        showWarning(null, title, content);
    }
    
    /**
     * 顯示警告對話框（指定父視窗）
     * 
     * @param owner 父視窗
     * @param title 標題
     * @param content 內容
     */
    public static void showWarning(Stage owner, String title, String content) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        if (owner != null) {
            alert.initOwner(owner);
        }
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        applyDarkTheme(alert);
        alert.showAndWait();
    }
    
    /**
     * 顯示確認對話框
     * 
     * @param title 標題
     * @param content 內容
     * @return 使用者是否點擊確認
     */
    public static boolean showConfirmation(String title, String content) {
        return showConfirmation(null, title, content);
    }
    
    /**
     * 顯示確認對話框（指定父視窗）
     * 
     * @param owner 父視窗
     * @param title 標題
     * @param content 內容
     * @return 使用者是否點擊確認
     */
    public static boolean showConfirmation(Stage owner, String title, String content) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        if (owner != null) {
            alert.initOwner(owner);
        }
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        applyDarkTheme(alert);
        
        Optional<ButtonType> result = alert.showAndWait();
        return result.isPresent() && result.get() == ButtonType.OK;
    }
    
    /**
     * 顯示是/否/取消對話框
     * 
     * @param title 標題
     * @param content 內容
     * @return 使用者選擇的按鈕類型
     */
    public static Optional<ButtonType> showYesNoCancel(String title, String content) {
        return showYesNoCancel(null, title, content);
    }
    
    /**
     * 顯示是/否/取消對話框（指定父視窗）
     * 
     * @param owner 父視窗
     * @param title 標題
     * @param content 內容
     * @return 使用者選擇的按鈕類型
     */
    public static Optional<ButtonType> showYesNoCancel(Stage owner, String title, String content) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        if (owner != null) {
            alert.initOwner(owner);
        }
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        
        // 設定自訂按鈕
        ButtonType yesButton = new ButtonType("是");
        ButtonType noButton = new ButtonType("否");
        ButtonType cancelButton = new ButtonType("取消");
        
        alert.getButtonTypes().setAll(yesButton, noButton, cancelButton);
        applyDarkTheme(alert);
        
        return alert.showAndWait();
    }
    
    /**
     * 顯示載入錯誤對話框
     * 
     * @param fileName 檔案名稱
     * @param errorMessage 錯誤訊息
     */
    public static void showLoadError(String fileName, String errorMessage) {
        showLoadError(null, fileName, errorMessage);
    }
    
    /**
     * 顯示載入錯誤對話框（指定父視窗）
     * 
     * @param owner 父視窗
     * @param fileName 檔案名稱
     * @param errorMessage 錯誤訊息
     */
    public static void showLoadError(Stage owner, String fileName, String errorMessage) {
        String content = String.format("無法載入檔案: %s\n\n錯誤詳情:\n%s", fileName, errorMessage);
        showError(owner, "載入失敗", content);
    }
    
    /**
     * 顯示儲存錯誤對話框
     * 
     * @param fileName 檔案名稱
     * @param errorMessage 錯誤訊息
     */
    public static void showSaveError(String fileName, String errorMessage) {
        showSaveError(null, fileName, errorMessage);
    }
    
    /**
     * 顯示儲存錯誤對話框（指定父視窗）
     * 
     * @param owner 父視窗
     * @param fileName 檔案名稱
     * @param errorMessage 錯誤訊息
     */
    public static void showSaveError(Stage owner, String fileName, String errorMessage) {
        String content = String.format("無法儲存檔案: %s\n\n錯誤詳情:\n%s", fileName, errorMessage);
        showError(owner, "儲存失敗", content);
    }
    
    /**
     * 顯示功能不可用對話框
     * 
     * @param featureName 功能名稱
     * @param reason 原因
     */
    public static void showFeatureUnavailable(String featureName, String reason) {
        showFeatureUnavailable(null, featureName, reason);
    }
    
    /**
     * 顯示功能不可用對話框（指定父視窗）
     * 
     * @param owner 父視窗
     * @param featureName 功能名稱
     * @param reason 原因
     */
    public static void showFeatureUnavailable(Stage owner, String featureName, String reason) {
        String content = String.format("功能 '%s' 目前不可用。\n\n原因: %s", featureName, reason);
        showWarning(owner, "功能不可用", content);
    }
    
    /**
     * 為對話框套用深色主題
     * 
     * @param alert 對話框
     */
    private static void applyDarkTheme(Alert alert) {
        // 套用深色主題樣式
        alert.getDialogPane().setStyle(
            "-fx-background-color: #2b2b2b; " +
            "-fx-text-fill: white;"
        );
        
        // 套用按鈕樣式
        alert.getDialogPane().lookupAll(".button").forEach(node -> {
            node.setStyle(
                "-fx-background-color: #404040; " +
                "-fx-text-fill: white; " +
                "-fx-border-color: #666666; " +
                "-fx-border-radius: 3px; " +
                "-fx-background-radius: 3px;"
            );
        });
        
        // 套用標籤樣式
        alert.getDialogPane().lookupAll(".label").forEach(node -> {
            node.setStyle("-fx-text-fill: white;");
        });
    }
    
    /**
     * 顯示成功訊息（自動關閉）
     * 
     * @param title 標題
     * @param content 內容
     */
    public static void showSuccess(String title, String content) {
        showSuccess(null, title, content);
    }
    
    /**
     * 顯示成功訊息（自動關閉，指定父視窗）
     * 
     * @param owner 父視窗
     * @param title 標題
     * @param content 內容
     */
    public static void showSuccess(Stage owner, String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        if (owner != null) {
            alert.initOwner(owner);
        }
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        applyDarkTheme(alert);
        
        // 3秒後自動關閉
        java.util.Timer timer = new java.util.Timer();
        timer.schedule(new java.util.TimerTask() {
            @Override
            public void run() {
                javafx.application.Platform.runLater(() -> {
                    if (alert.isShowing()) {
                        alert.close();
                    }
                });
            }
        }, 3000);
        
        alert.show();
    }
}
