package E_Reader;

/**
 * 啟動器類 - 避免模組系統問題
 * 這個類可以直接執行，不需要特殊的 JVM 參數
 */
public class Launcher {
    
    /**
     * 主入口點 - 直接啟動 JavaFX 應用程式
     * @param args 命令行參數
     */
    public static void main(String[] args) {
        // 設定系統屬性
        System.setProperty("javafx.preloader", "");
        System.setProperty("file.encoding", "UTF-8");
        System.setProperty("prism.lcdtext", "false");
        System.setProperty("prism.text", "t2k");
        
        try {
            // 檢查 JavaFX 是否可用
            Class.forName("javafx.application.Application");
            
            // 啟動主應用程式
            Main.main(args);
            
        } catch (ClassNotFoundException e) {
            // JavaFX 不可用，顯示錯誤訊息
            showJavaFXError();
            System.exit(1);
            
        } catch (Exception e) {
            System.err.println("應用程式啟動失敗: " + e.getMessage());
            e.printStackTrace();
            
            // 嘗試使用 Swing 顯示錯誤
            showSwingError(e);
            System.exit(1);
        }
    }
    
    /**
     * 顯示 JavaFX 不可用的錯誤訊息
     */
    private static void showJavaFXError() {
        String errorMessage = 
            "JavaFX 運行時組件缺失\n\n" +
            "請選擇以下解決方案之一：\n\n" +
            "方案1 - 使用 Maven 執行：\n" +
            "  1. 在命令提示字元中執行：mvn javafx:run\n" +
            "  2. 或執行 run_with_maven.bat\n\n" +
            "方案2 - 下載 JavaFX SDK：\n" +
            "  1. 從 https://gluonhq.com/products/javafx/ 下載 JavaFX SDK 21\n" +
            "  2. 解壓到 C:\\javafx-sdk-21\\\n" +
            "  3. 執行 run_with_javafx.bat\n\n" +
            "方案3 - 使用 IDE：\n" +
            "  在 IDE 的 VM 選項中添加：\n" +
            "  --module-path \"C:\\javafx-sdk-21\\lib\" --add-modules javafx.controls,javafx.fxml,javafx.swing\n\n" +
            "推薦使用方案1（Maven 執行）";
        
        try {
            // 嘗試使用 Swing 顯示錯誤
            javax.swing.SwingUtilities.invokeLater(() -> {
                javax.swing.JOptionPane.showMessageDialog(
                    null,
                    errorMessage,
                    "JavaFX 運行時缺失",
                    javax.swing.JOptionPane.ERROR_MESSAGE
                );
            });
        } catch (Exception swingError) {
            // 如果 Swing 也不可用，輸出到控制台
            System.err.println("=".repeat(60));
            System.err.println("JavaFX 運行時組件缺失");
            System.err.println("=".repeat(60));
            System.err.println(errorMessage.replace("\n", System.lineSeparator()));
            System.err.println("=".repeat(60));
        }
    }
    
    /**
     * 顯示其他錯誤訊息
     */
    private static void showSwingError(Exception e) {
        try {
            javax.swing.SwingUtilities.invokeLater(() -> {
                javax.swing.JOptionPane.showMessageDialog(
                    null,
                    "E-Reader 啟動失敗\n\n" +
                    "錯誤訊息: " + e.getMessage() + "\n\n" +
                    "請檢查：\n" +
                    "1. Java 版本（需要 17 或更高）\n" +
                    "2. JavaFX 是否正確安裝\n" +
                    "3. 所有依賴是否完整\n" +
                    "4. 系統記憶體是否充足",
                    "啓動錯誤",
                    javax.swing.JOptionPane.ERROR_MESSAGE
                );
            });
        } catch (Exception ignored) {
            System.err.println("啟動失敗: " + e.getMessage());
        }
    }
}
