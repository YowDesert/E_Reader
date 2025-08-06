package E_Reader.ui;

import E_Reader.settings.SettingsManager;
import javafx.application.Application;
import javafx.stage.Stage;

/**
 * 測試增強版設定對話框
 */
public class SettingsDialogTest extends Application {

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("設定對話框測試");
        primaryStage.setWidth(400);
        primaryStage.setHeight(300);
        primaryStage.show();

        // 創建設定管理器
        SettingsManager settingsManager = new SettingsManager();
        settingsManager.loadSettings();

        // 顯示設定對話框
        EnhancedSettingsDialog dialog = new EnhancedSettingsDialog(settingsManager, primaryStage);
        dialog.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
