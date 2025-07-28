package E_Reader.test;

import E_Reader.filemanager.FileManagerController;
import javafx.application.Application;
import javafx.stage.Stage;

import java.io.File;

/**
 * 檔案管理器測試程式
 */
public class FileManagerTest extends Application {

    @Override
    public void start(Stage primaryStage) {
        try {
            // 創建檔案管理器控制器
            FileManagerController fileManager = new FileManagerController(primaryStage);
            
            // 初始化並設定檔案開啟回調
            fileManager.initialize(this::handleFileOpen);
            
            // 顯示檔案管理器
            fileManager.show();
            
        } catch (Exception e) {
            System.err.println("檔案管理器測試失敗: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * 處理檔案開啟事件
     */
    private void handleFileOpen(File file) {
        System.out.println("檔案已選擇: " + file.getAbsolutePath());
        // 這裡可以添加實際的檔案開啟邏輯
    }

    public static void main(String[] args) {
        System.out.println("啟動檔案管理器測試...");
        launch(args);
    }
}
