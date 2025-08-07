package E_Reader.settings;

import java.io.*;
import java.util.Properties;

/**
 * 設定管理器 - 統一管理應用程式設定
 */
public class SettingsManager {

    private Properties settings;
    private final String SETTINGS_FILE = "e_reader_settings.properties";

    // 定義 FitMode 枚舉
    public enum FitMode {
        FIT_WIDTH, FIT_HEIGHT, FIT_PAGE, ORIGINAL_SIZE
    }

    // 設定項目
    private FitMode fitMode = FitMode.FIT_WIDTH;
    private String backgroundColor = "#1e1e1e";
    private boolean showPageNumbers = true;
    private boolean enableTouchNavigation = true;
    private int autoSaveInterval = 30; // 秒
    private double defaultZoomLevel = 1.0;
    private boolean rememberLastFile = true;

    // 主題和護眼模式設定
    private ThemeMode themeMode = ThemeMode.DARK;
    private boolean eyeCareMode = false;
    private int eyeCareBrightness = 80; // 0-100
    private boolean nightMode = false;
    private int nightModeStartHour = 20; // 晚上8點
    private int nightModeEndHour = 7;   // 早上7點
    
    // OCR模型設定
    private OcrModel ocrModel = OcrModel.FAST;
    
    public enum OcrModel {
        FAST("快速模型", "tessdata_fast-4.1.0", "快速識別，適合一般用途"),
        BEST("最佳模型", "tessdata_best-4.1.0", "高精度識別，適合重要文件");
        
        private final String displayName;
        private final String dataPath;
        private final String description;
        
        OcrModel(String displayName, String dataPath, String description) {
            this.displayName = displayName;
            this.dataPath = dataPath;
            this.description = description;
        }
        
        public String getDisplayName() { return displayName; }
        public String getDataPath() { return dataPath; }
        public String getDescription() { return description; }
    }

    public enum ThemeMode {
        LIGHT("淺色模式", "#ffffff", "#000000", "#f5f5f5"),
        DARK("深色模式", "#1e1e1e", "#ffffff", "#2b2b2b"),
        BLACK("純黑模式", "#000000", "#e0e0e0", "#121212"),
        EYE_CARE("護眼模式", "#1a1a0f", "#d4d4aa", "#2a2a1f"),
        SEPIA("復古模式", "#f4ecd8", "#5d4e37", "#f0e6d2");

        private final String displayName;
        private final String backgroundColor;
        private final String textColor;
        private final String controlColor;

        ThemeMode(String displayName, String backgroundColor, String textColor, String controlColor) {
            this.displayName = displayName;
            this.backgroundColor = backgroundColor;
            this.textColor = textColor;
            this.controlColor = controlColor;
        }

        public String getDisplayName() { return displayName; }
        public String getBackgroundColor() { return backgroundColor; }
        public String getTextColor() { return textColor; }
        public String getControlColor() { return controlColor; }
    }

    public SettingsManager() {
        settings = new Properties();
        loadDefaultSettings();
    }

    private void loadDefaultSettings() {
        settings.setProperty("fitMode", "FIT_WIDTH");
        settings.setProperty("backgroundColor", "#1e1e1e");
        settings.setProperty("showPageNumbers", "true");
        settings.setProperty("enableTouchNavigation", "true");
        settings.setProperty("autoSaveInterval", "30");
        settings.setProperty("defaultZoomLevel", "1.0");
        settings.setProperty("rememberLastFile", "true");
        settings.setProperty("themeMode", "DARK");
        settings.setProperty("eyeCareMode", "false");
        settings.setProperty("eyeCareBrightness", "80");
        settings.setProperty("nightMode", "false");
        settings.setProperty("nightModeStartHour", "20");
        settings.setProperty("nightModeEndHour", "7");
        settings.setProperty("ocrModel", "FAST");
    }

    public void loadSettings() {
        try {
            File settingsFile = new File(SETTINGS_FILE);

            if (!settingsFile.exists()) {
                System.out.println("設定檔不存在，使用預設設定");
                loadDefaultSettings();
                saveSettings(); // 創建預設設定檔
                return;
            }

            try (InputStream input = new FileInputStream(settingsFile)) {
                settings.load(input);
                System.out.println("設定檔案已載入: " + SETTINGS_FILE);
            }

            // **修正：立即同步到實例變數**
            syncPropertiesToSettings();

            // **修正：驗證載入的設定**
            validateLoadedSettings();

            System.out.println("設定載入完成 - 主題: " + themeMode + ", 亮度: " + eyeCareBrightness + ", 顯示頁碼: " + showPageNumbers);

        } catch (IOException e) {
            System.err.println("載入設定檔時發生錯誤: " + e.getMessage());

            // **修正：嘗試載入備用檔案**
            if (!tryLoadBackup()) {
                System.out.println("使用預設設定");
                loadDefaultSettings();
            }
        }
    }

    private void syncPropertiesToSettings() {
        // 主題設定
        String themeModeStr = settings.getProperty("themeMode", "DARK");
        try {
            themeMode = ThemeMode.valueOf(themeModeStr);
        } catch (IllegalArgumentException e) {
            themeMode = ThemeMode.DARK;
            System.err.println("無效的主題設定，使用預設值: DARK");
        }

        // 縮放模式設定
        String fitModeStr = settings.getProperty("fitMode", "FIT_WIDTH");
        try {
            fitMode = FitMode.valueOf(fitModeStr);
        } catch (IllegalArgumentException e) {
            fitMode = FitMode.FIT_WIDTH;
            System.err.println("無效的縮放模式設定，使用預設值: FIT_WIDTH");
        }

        // OCR模型設定
        String ocrModelStr = settings.getProperty("ocrModel", "FAST");
        try {
            ocrModel = OcrModel.valueOf(ocrModelStr);
        } catch (IllegalArgumentException e) {
            ocrModel = OcrModel.FAST;
            System.err.println("無效的OCR模型設定，使用預設值: FAST");
        }

        // 基本設定
        backgroundColor = settings.getProperty("backgroundColor", themeMode.getBackgroundColor());
        showPageNumbers = Boolean.parseBoolean(settings.getProperty("showPageNumbers", "true"));
        enableTouchNavigation = Boolean.parseBoolean(settings.getProperty("enableTouchNavigation", "true"));
        eyeCareMode = Boolean.parseBoolean(settings.getProperty("eyeCareMode", "false"));
        nightMode = Boolean.parseBoolean(settings.getProperty("nightMode", "false"));
        rememberLastFile = Boolean.parseBoolean(settings.getProperty("rememberLastFile", "true"));

        // 數值設定
        try {
            defaultZoomLevel = Double.parseDouble(settings.getProperty("defaultZoomLevel", "1.0"));
            eyeCareBrightness = Integer.parseInt(settings.getProperty("eyeCareBrightness", "80"));
            autoSaveInterval = Integer.parseInt(settings.getProperty("autoSaveInterval", "30"));
            nightModeStartHour = Integer.parseInt(settings.getProperty("nightModeStartHour", "20"));
            nightModeEndHour = Integer.parseInt(settings.getProperty("nightModeEndHour", "7"));
        } catch (NumberFormatException e) {
            System.err.println("數值設定格式錯誤，使用預設值");
            defaultZoomLevel = 1.0;
            eyeCareBrightness = 80;
            autoSaveInterval = 30;
            nightModeStartHour = 20;
            nightModeEndHour = 7;
        }
    }

    /**
     * **新增：驗證載入的設定**
     */
    private void validateLoadedSettings() {
        boolean needsSave = false;

        // 驗證亮度範圍
        if (eyeCareBrightness < 10 || eyeCareBrightness > 100) {
            System.err.println("亮度設定超出範圍，重設為80%");
            eyeCareBrightness = 80;
            needsSave = true;
        }

        // 驗證自動保存間隔
        if (autoSaveInterval < 10 || autoSaveInterval > 300) {
            System.err.println("自動保存間隔超出範圍，重設為30秒");
            autoSaveInterval = 30;
            needsSave = true;
        }

        // 驗證時間設定
        if (nightModeStartHour < 0 || nightModeStartHour > 23) {
            System.err.println("夜間模式開始時間無效，重設為20點");
            nightModeStartHour = 20;
            needsSave = true;
        }

        if (nightModeEndHour < 0 || nightModeEndHour > 23) {
            System.err.println("夜間模式結束時間無效，重設為7點");
            nightModeEndHour = 7;
            needsSave = true;
        }

        if (needsSave) {
            saveSettings();
        }
    }

    /**
     * **新增：嘗試載入備用設定檔**
     */
    private boolean tryLoadBackup() {
        try {
            String backupFileName = SETTINGS_FILE + ".backup";
            File backupFile = new File(backupFileName);

            if (backupFile.exists()) {
                try (InputStream input = new FileInputStream(backupFile)) {
                    settings.load(input);
                    syncPropertiesToSettings();
                    System.out.println("已從備用檔案載入設定: " + backupFileName);

                    // 重新保存到主檔案
                    saveSettings();
                    return true;
                }
            }
        } catch (Exception e) {
            System.err.println("載入備用設定檔失敗: " + e.getMessage());
        }

        return false;
    }

    /**
     * **新增：立即套用設定並保存**
     * 用於設定對話框中的即時套用
     */
    public void applySettingsImmediately() {
        try {
            System.out.println("開始強化版立即套用設定...");

            // 1. 立即同步所有設定到Properties
            syncAllSettingsToProperties();

            // 2. 強制寫入檔案
            File settingsFile = new File(SETTINGS_FILE);

            // 確保檔案存在
            if (!settingsFile.exists()) {
                settingsFile.createNewFile();
            }

            // 強制保存
            try (OutputStream output = new FileOutputStream(settingsFile)) {
                settings.store(output, "E-Reader Settings (Immediate Apply) - " + java.time.LocalDateTime.now());
                output.flush();

                // 強制同步到磁碟
                if (output instanceof FileOutputStream) {
                    ((FileOutputStream) output).getFD().sync();
                }
            }

            System.out.println("強化版設定立即套用並保存完成");

            // 3. 驗證保存是否成功
            verifySettingsSaved();

        } catch (Exception e) {
            System.err.println("強化版立即套用設定時發生錯誤: " + e.getMessage());
            e.printStackTrace();
        }
    }



    public void saveSettings() {
        try {
            // **修正1：確保所有Properties都是最新的**
            syncAllSettingsToProperties();

            // **修正2：強制寫入檔案並同步**
            File settingsFile = new File(SETTINGS_FILE);

            // 如果檔案不存在，創建它
            if (!settingsFile.exists()) {
                settingsFile.createNewFile();
            }

            try (OutputStream output = new FileOutputStream(settingsFile)) {
                settings.store(output, "E-Reader Settings - " + java.time.LocalDateTime.now());
                output.flush(); // 強制寫入磁碟

                // **修正3：確保檔案系統同步**
                if (output instanceof FileOutputStream) {
                    ((FileOutputStream) output).getFD().sync();
                }
            }

            System.out.println("設定已成功保存到: " + settingsFile.getAbsolutePath());

            // **修正4：驗證設定是否正確保存**
            verifySettingsSaved();

        } catch (IOException e) {
            System.err.println("無法儲存設定檔: " + e.getMessage());
            e.printStackTrace();

            // **修正5：嘗試備用保存位置**
            tryBackupSave();
        }
    }

    /**
     * **新增：同步所有設定到Properties**
     */
    private void syncAllSettingsToProperties() {
        settings.setProperty("fitMode", fitMode.toString());
        settings.setProperty("backgroundColor", backgroundColor);
        settings.setProperty("showPageNumbers", String.valueOf(showPageNumbers));
        settings.setProperty("enableTouchNavigation", String.valueOf(enableTouchNavigation));
        settings.setProperty("autoSaveInterval", String.valueOf(autoSaveInterval));
        settings.setProperty("defaultZoomLevel", String.valueOf(defaultZoomLevel));
        settings.setProperty("rememberLastFile", String.valueOf(rememberLastFile));
        settings.setProperty("themeMode", themeMode.toString());
        settings.setProperty("eyeCareMode", String.valueOf(eyeCareMode));
        settings.setProperty("eyeCareBrightness", String.valueOf(eyeCareBrightness));
        settings.setProperty("nightMode", String.valueOf(nightMode));
        settings.setProperty("nightModeStartHour", String.valueOf(nightModeStartHour));
        settings.setProperty("nightModeEndHour", String.valueOf(nightModeEndHour));
        settings.setProperty("ocrModel", ocrModel.toString());

        System.out.println("所有設定已同步到Properties");
    }

    /**
     * **新增：驗證設定是否正確保存**
     */
    private void verifySettingsSaved() {
        try {
            Properties testProps = new Properties();
            try (InputStream input = new FileInputStream(SETTINGS_FILE)) {
                testProps.load(input);
            }

            // 驗證關鍵設定
            String savedTheme = testProps.getProperty("themeMode", "");
            String savedBrightness = testProps.getProperty("eyeCareBrightness", "");
            String savedPageNumbers = testProps.getProperty("showPageNumbers", "");

            boolean isValid = !savedTheme.isEmpty() &&
                    !savedBrightness.isEmpty() &&
                    !savedPageNumbers.isEmpty();

            if (isValid) {
                System.out.println("設定保存驗證成功:");
                System.out.println("- 主題: " + savedTheme);
                System.out.println("- 亮度: " + savedBrightness);
                System.out.println("- 顯示頁碼: " + savedPageNumbers);
            } else {
                System.err.println("設定保存驗證失敗，檔案可能損壞");
            }

        } catch (Exception e) {
            System.err.println("驗證設定保存時發生錯誤: " + e.getMessage());
        }
    }

    /**
     * **新增：備用保存方法**
     */
    private void tryBackupSave() {
        try {
            String backupFileName = SETTINGS_FILE + ".backup";
            File backupFile = new File(backupFileName);

            try (OutputStream output = new FileOutputStream(backupFile)) {
                settings.store(output, "E-Reader Settings Backup - " + java.time.LocalDateTime.now());
                output.flush();
                System.out.println("設定已保存到備用檔案: " + backupFileName);
            }

        } catch (Exception e) {
            System.err.println("備用保存也失敗: " + e.getMessage());
        }
    }

    // 主題相關方法
    public void toggleNightMode() {
        // 如果護眼模式開啟，先關閉護眼模式
        if (eyeCareMode) {
            eyeCareMode = false;
        }
        
        // 切換夜間模式標記
        nightMode = !nightMode;
        
        if (nightMode) {
            // 啟用夜間模式時，自動切換到純黑主題
            setThemeMode(ThemeMode.BLACK);
        } else {
            // 關閉夜間模式時，恢復到深色模式
            setThemeMode(ThemeMode.DARK);
        }
        
        saveSettings();
    }
    
    public void toggleEyeCareMode() {
        // 如果夜間模式開啟，先關閉夜間模式
        if (nightMode) {
            nightMode = false;
        }
        
        eyeCareMode = !eyeCareMode;
        if (eyeCareMode) {
            // 啟用護眼模式時自動切換到護眼主題
            setThemeMode(ThemeMode.EYE_CARE);
        } else {
            // 關閉護眼模式時恢復到深色主題
            setThemeMode(ThemeMode.DARK);
        }
        saveSettings();
    }

    // 檢查是否應該啟用夜間模式
    public boolean shouldEnableNightMode() {
        if (!nightMode) return false;

        java.time.LocalTime now = java.time.LocalTime.now();
        int currentHour = now.getHour();

        if (nightModeStartHour <= nightModeEndHour) {
            return currentHour >= nightModeStartHour && currentHour < nightModeEndHour;
        } else {
            // 跨夜情況，例如 22:00 到 6:00
            return currentHour >= nightModeStartHour || currentHour < nightModeEndHour;
        }
    }

    // 獲取當前應該使用的主題（考慮夜間模式和護眼模式）
    public ThemeMode getCurrentTheme() {
        // 優先檢查手動開啟的夜間模式
        if (nightMode) {
            return ThemeMode.BLACK; // 手動開啟的夜間模式
        }
        // 再檢查自動時間夜間模式（如果啟用了自動模式）
        if (shouldEnableNightMode()) {
            return ThemeMode.BLACK; // 自動時間夜間模式
        }
        if (eyeCareMode) {
            return ThemeMode.EYE_CARE; // 護眼模式
        }
        return themeMode; // 使用者選擇的主題
    }

    // Getter 方法
    public FitMode getFitMode() { return fitMode; }
    public String getBackgroundColor() { return backgroundColor; }
    public boolean isShowPageNumbers() { return showPageNumbers; }
    public boolean isEnableTouchNavigation() { return enableTouchNavigation; }
    public int getAutoSaveInterval() { return autoSaveInterval; }
    public double getDefaultZoomLevel() { return defaultZoomLevel; }
    public boolean isRememberLastFile() { return rememberLastFile; }
    public ThemeMode getThemeMode() { return themeMode; }
    public boolean isEyeCareMode() { return eyeCareMode; }
    public int getEyeCareBrightness() { return eyeCareBrightness; }
    public boolean isNightMode() { return nightMode; }
    public int getNightModeStartHour() { return nightModeStartHour; }
    public int getNightModeEndHour() { return nightModeEndHour; }
    public OcrModel getOcrModel() { return ocrModel; }

    // Setter 方法
    public void setFitMode(FitMode fitMode) {
        this.fitMode = fitMode;
    }
    
    public void setBackgroundColor(String backgroundColor) {
        this.backgroundColor = backgroundColor;
    }


//    @Override
    public void setShowPageNumbers(boolean showPageNumbers) {
        this.showPageNumbers = showPageNumbers;
        System.out.println("頁碼顯示設定變更為: " + showPageNumbers);

        // 立即保存
        applySettingsImmediately();
    }
    
    public void setEnableTouchNavigation(boolean enableTouchNavigation) {
        this.enableTouchNavigation = enableTouchNavigation;
    }
    
    public void setAutoSaveInterval(int autoSaveInterval) {
        this.autoSaveInterval = autoSaveInterval;
    }
    
    public void setDefaultZoomLevel(double defaultZoomLevel) {
        this.defaultZoomLevel = defaultZoomLevel;
    }
    
    public void setRememberLastFile(boolean rememberLastFile) {
        this.rememberLastFile = rememberLastFile;
    }

//    @Override
    public void setThemeMode(ThemeMode themeMode) {
        this.themeMode = themeMode;
        this.backgroundColor = themeMode.getBackgroundColor();
        System.out.println("主題模式設定變更為: " + themeMode.getDisplayName());

        // 立即保存
        applySettingsImmediately();
    }

    public void setEyeCareMode(boolean eyeCareMode) {
        this.eyeCareMode = eyeCareMode;
    }

//    @Override
    public void setEyeCareBrightness(int eyeCareBrightness) {
        // 驗證範圍
        this.eyeCareBrightness = Math.max(10, Math.min(100, eyeCareBrightness));
        System.out.println("護眼亮度設定變更為: " + this.eyeCareBrightness + "%");

        // 立即保存
        applySettingsImmediately();
    }
    
    public void setNightMode(boolean nightMode) {
        this.nightMode = nightMode;
    }
    
    public void setNightModeStartHour(int nightModeStartHour) {
        this.nightModeStartHour = nightModeStartHour;
    }
    
    public void setNightModeEndHour(int nightModeEndHour) {
        this.nightModeEndHour = nightModeEndHour;
    }
    
    public void setOcrModel(OcrModel ocrModel) {
        this.ocrModel = ocrModel;
    }
}
