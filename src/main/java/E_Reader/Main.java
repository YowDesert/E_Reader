package E_Reader;

import E_Reader.ui.MainController;
import javafx.application.Application;
import javafx.stage.Stage;

/**
 * 主應用程式類 - 簡化版本，使用MainController處理所有邏輯
 */
public class Main extends Application {

    private MainController mainController;

    @Override
    public void start(Stage primaryStage) {
        try {
            // 創建主控制器
            mainController = new MainController(primaryStage);
            
            // 初始化應用程式
            mainController.initialize();
            
            // 顯示主視窗
            primaryStage.show();
            
        } catch (Exception e) {
            System.err.println("應用程式啟動失敗: " + e.getMessage());
            e.printStackTrace();
            
            // 顯示錯誤訊息
            showStartupError(primaryStage, e);
        }
    }

    @Override
    public void stop() {
        // 清理資源（如果主控制器已初始化）
        if (mainController != null) {
            // MainController 會在視窗關閉時自動處理清理
        }
    }

    /**
     * 顯示啟動錯誤訊息
     */
    private void showStartupError(Stage stage, Exception e) {
        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(
                javafx.scene.control.Alert.AlertType.ERROR);
        alert.setTitle("啟動錯誤");
        alert.setHeaderText("E-Reader 無法正常啟動");
        alert.setContentText("錯誤詳情：\n" + e.getMessage() + 
                "\n\n請檢查以下項目：" +
                "\n• Java 版本是否為 17 或更高" +
                "\n• JavaFX 運行時是否正確安裝" +
                "\n• 相關依賴庫是否完整" +
                "\n• 系統記憶體是否充足");
        
        if (stage != null) {
            alert.initOwner(stage);
        }
        
        alert.showAndWait();
    }

    /**
     * 主程式入口點
     * 
     * @param args 命令行參數
     */
    public static void main(String[] args) {
        // 設定系統屬性
        System.setProperty("javafx.preloader", "");
        System.setProperty("file.encoding", "UTF-8");
        
        // 設定 JavaFX 相關屬性（可選）
        System.setProperty("prism.lcdtext", "false");  // 改善文字渲染
        System.setProperty("prism.text", "t2k");       // 使用 T2K 文字引擎
        
        try {
            // 啟動 JavaFX 應用程式
            launch(args);
        } catch (Exception e) {
            System.err.println("應用程式啟動失敗: " + e.getMessage());
            e.printStackTrace();
            
            // 嘗試使用 Swing 顯示錯誤訊息
            showSwingError(e);
            
            System.exit(1);
        }
    }

    /**
     * 使用 Swing 顯示錯誤訊息（當 JavaFX 無法啟動時）
     */
    private static void showSwingError(Exception e) {
        try {
            javax.swing.SwingUtilities.invokeLater(() -> {
                javax.swing.JOptionPane.showMessageDialog(
                        null,
                        "E-Reader 無法啟動\n\n" +
                                "錯誤訊息: " + e.getMessage() + "\n\n" +
                                "可能的解決方案:\n" +
                                "1. 確認已安裝 Java 17 或更高版本\n" +
                                "2. 確認 JavaFX 運行時已正確安裝\n" +
                                "3. 檢查系統記憶體是否充足\n" +
                                "4. 嘗試以管理員權限執行\n" +
                                "5. 查看控制台輸出以獲取更多資訊",
                        "E-Reader 啟動錯誤",
                        javax.swing.JOptionPane.ERROR_MESSAGE
                );
            });
        } catch (Exception swingError) {
            // 如果連 Swing 都無法使用，就直接輸出到控制台
            System.err.println("無法顯示圖形錯誤對話框");
            System.err.println("原始錯誤: " + e.getMessage());
            System.err.println("Swing 錯誤: " + swingError.getMessage());
        }
    }
}
