package E_Reader.settings;

import E_Reader.viewer.ImageViewer;

import java.io.*;
import java.util.Properties;

/**
 * 設定管理器 - 統一管理應用程式設定
 */
public class SettingsManager {

    private Properties settings;
    private final String SETTINGS_FILE = "e_reader_settings.properties";

    // 設定項目
    private ImageViewer.FitMode fitMode = ImageViewer.FitMode.FIT_WIDTH;
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
    }

    public void loadSettings() {
        try (InputStream input = new FileInputStream(SETTINGS_FILE)) {
            settings.load(input);

            // 從 Properties 載入設定
            String fitModeStr = settings.getProperty("fitMode", "FIT_WIDTH");
            try {
                fitMode = ImageViewer.FitMode.valueOf(fitModeStr);
            } catch (IllegalArgumentException e) {
                fitMode = ImageViewer.FitMode.FIT_WIDTH;
            }

            String themeModeStr = settings.getProperty("themeMode", "DARK");
            try {
                themeMode = ThemeMode.valueOf(themeModeStr);
            } catch (IllegalArgumentException e) {
                themeMode = ThemeMode.DARK;
            }

            backgroundColor = settings.getProperty("backgroundColor", themeMode.getBackgroundColor());
            showPageNumbers = Boolean.parseBoolean(settings.getProperty("showPageNumbers", "true"));
            enableTouchNavigation = Boolean.parseBoolean(settings.getProperty("enableTouchNavigation", "true"));
            eyeCareMode = Boolean.parseBoolean(settings.getProperty("eyeCareMode", "false"));
            nightMode = Boolean.parseBoolean(settings.getProperty("nightMode", "false"));

            try {
                defaultZoomLevel = Double.parseDouble(settings.getProperty("defaultZoomLevel", "1.0"));
                eyeCareBrightness = Integer.parseInt(settings.getProperty("eyeCareBrightness", "80"));
                nightModeStartHour = Integer.parseInt(settings.getProperty("nightModeStartHour", "20"));
                nightModeEndHour = Integer.parseInt(settings.getProperty("nightModeEndHour", "7"));
            } catch (NumberFormatException e) {
                defaultZoomLevel = 1.0;
                eyeCareBrightness = 80;
                nightModeStartHour = 20;
                nightModeEndHour = 7;
            }

            rememberLastFile = Boolean.parseBoolean(settings.getProperty("rememberLastFile", "true"));

        } catch (IOException e) {
            loadDefaultSettings();
        }
    }

    public void saveSettings() {
        // 更新Properties中的所有設定
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

        try (OutputStream output = new FileOutputStream(SETTINGS_FILE)) {
            settings.store(output, "E-Reader Settings");
        } catch (IOException e) {
            System.err.println("無法儲存設定檔: " + e.getMessage());
        }
    }

    // 主題相關方法
    public void toggleNightMode() {
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
    public ImageViewer.FitMode getFitMode() { return fitMode; }
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

    // Setter 方法
    public void setFitMode(ImageViewer.FitMode fitMode) {
        this.fitMode = fitMode;
    }
    
    public void setBackgroundColor(String backgroundColor) {
        this.backgroundColor = backgroundColor;
    }
    
    public void setShowPageNumbers(boolean showPageNumbers) {
        this.showPageNumbers = showPageNumbers;
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

    public void setThemeMode(ThemeMode themeMode) {
        this.themeMode = themeMode;
        this.backgroundColor = themeMode.getBackgroundColor();
    }

    public void setEyeCareMode(boolean eyeCareMode) {
        this.eyeCareMode = eyeCareMode;
    }
    
    public void setEyeCareBrightness(int eyeCareBrightness) {
        this.eyeCareBrightness = eyeCareBrightness;
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
}
