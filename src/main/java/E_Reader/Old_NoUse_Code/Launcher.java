//package E_Reader.Old_NoUse_Code;
//
//import E_Reader.Main;
//
///**
// * 啟動器類 - 用於解決模組路徑問題
// * 這個類不繼承 Application，可以避免一些 JavaFX 模組載入問題
// */
//public class Launcher {
//
//    public static void main(String[] args) {
//        try {
//            // 設定系統屬性
//            System.setProperty("javafx.preloader", "");
//            System.setProperty("file.encoding", "UTF-8");
//
//            // 檢查 JavaFX 是否可用
//            checkJavaFXAvailability();
//
//            // 啟動主應用程式
//            Main.main(args);
//
//        } catch (Exception e) {
//            System.err.println("應用程式啟動失敗: " + e.getMessage());
//            e.printStackTrace();
//
//            // 顯示錯誤對話框（如果可能）
//            showErrorDialog(e);
//
//            System.exit(1);
//        }
//    }
//
//    private static void checkJavaFXAvailability() {
//        try {
//            Class.forName("javafx.application.Application");
//            System.out.println("JavaFX 運行時環境檢查通過");
//        } catch (ClassNotFoundException e) {
//            throw new RuntimeException("JavaFX 運行時環境未找到。請確保已安裝 JavaFX。", e);
//        }
//    }
//
//    private static void showErrorDialog(Exception e) {
//        // 嘗試使用 Swing 顯示錯誤對話框
//        try {
//            javax.swing.SwingUtilities.invokeLater(() -> {
//                javax.swing.JOptionPane.showMessageDialog(
//                        null,
//                        "應用程式啟動失敗:\n" + e.getMessage() +
//                                "\n\n請檢查以下項目:" +
//                                "\n1. 是否已安裝 Java 17 或更高版本" +
//                                "\n2. 是否已安裝 JavaFX 運行時" +
//                                "\n3. 是否有足夠的系統記憶體" +
//                                "\n4. 檢查控制台輸出以獲取更多詳細信息",
//                        "E-Reader 啟動錯誤",
//                        javax.swing.JOptionPane.ERROR_MESSAGE
//                );
//            });
//        } catch (Exception swingError) {
//            // 如果連 Swing 都無法使用，就直接輸出到控制台
//            System.err.println("無法顯示錯誤對話框，原始錯誤:");
//            e.printStackTrace();
//        }
//    }
//}