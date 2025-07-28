package E_Reader.utils;

import java.io.*;
import java.util.Properties;

/**
 * 檔案管理器配置管理器
 * 負責載入和儲存檔案管理器的各種設定
 */
public class FileManagerConfig {
    
    private static final String CONFIG_FILE = "file_manager_config.properties";
    private static final String DEFAULT_CONFIG_FILE = "file_manager_config_default.properties";
    
    private Properties config;
    private static FileManagerConfig instance;
    
    // 默認設定值
    private static final String DEFAULT_LIBRARY_PATH = "";
    private static final boolean DEFAULT_SHOW_WELCOME = true;
    private static final boolean DEFAULT_REMEMBER_LAST_FOLDER = true;
    private static final String DEFAULT_WINDOW_SIZE = "1000x700";
    private static final int DEFAULT_GRID_COLUMNS = 5;
    private static final boolean DEFAULT_THUMBNAIL_ENABLED = true;
    private static final String DEFAULT_THUMBNAIL_SIZE = "80x60";
    private static final boolean DEFAULT_USE_EMOJI_ICONS = true;
    private static final boolean DEFAULT_SEARCH_CASE_SENSITIVE = false;
    private static final boolean DEFAULT_SEARCH_FUZZY_ENABLED = true;
    private static final String DEFAULT_SORT_METHOD = "name-asc";
    private static final boolean DEFAULT_AUTO_GENERATE_THUMBNAIL = true;
    private static final boolean DEFAULT_SHOW_PROGRESS_FOR_LARGE_FILES = true;
    private static final int DEFAULT_LARGE_FILE_THRESHOLD = 10;
    private static final int DEFAULT_THUMBNAIL_CACHE_SIZE = 100;
    private static final boolean DEFAULT_DELETE_REQUIRE_CONFIRMATION = true;
    private static final boolean DEFAULT_OPERATION_LOGGING = true;
    private static final int DEFAULT_LOG_FILE_MAX_SIZE = 5;
    private static final boolean DEFAULT_LAZY_LOADING = true;
    private static final int DEFAULT_REFRESH_INTERVAL = 1000;
    private static final int DEFAULT_MEMORY_LIMIT = 512;
    private static final boolean DEFAULT_MULTI_THREADING = true;
    private static final boolean DEFAULT_DEBUG_MODE = false;
    private static final boolean DEFAULT_SHOW_DETAILED_ERRORS = true;
    private static final boolean DEFAULT_PERFORMANCE_METRICS = false;
    
    private FileManagerConfig() {
        loadConfig();
    }
    
    public static synchronized FileManagerConfig getInstance() {
        if (instance == null) {
            instance = new FileManagerConfig();
        }
        return instance;
    }
    
    /**
     * 載入配置檔
     */
    private void loadConfig() {
        config = new Properties();
        
        // 先載入默認配置
        loadDefaultConfig();
        
        // 然後載入用戶配置（如果存在）
        File configFile = new File(CONFIG_FILE);
        if (configFile.exists()) {
            try (FileInputStream fis = new FileInputStream(configFile)) {
                Properties userConfig = new Properties();
                userConfig.load(fis);
                
                // 合併用戶配置到默認配置
                for (String key : userConfig.stringPropertyNames()) {
                    config.setProperty(key, userConfig.getProperty(key));
                }
                
                System.out.println("✅ 已載入檔案管理器配置: " + configFile.getAbsolutePath());
            } catch (IOException e) {
                System.err.println("⚠️ 無法讀取配置檔，使用默認設定: " + e.getMessage());
            }
        } else {
            System.out.println("ℹ️ 配置檔不存在，使用默認設定");
            // 創建默認配置檔
            saveDefaultConfig();
        }
    }
    
    /**
     * 載入默認配置
     */
    private void loadDefaultConfig() {
        config.setProperty("library.root.path", DEFAULT_LIBRARY_PATH);
        config.setProperty("file.manager.show.welcome", String.valueOf(DEFAULT_SHOW_WELCOME));
        config.setProperty("file.manager.remember.last.folder", String.valueOf(DEFAULT_REMEMBER_LAST_FOLDER));
        config.setProperty("file.manager.window.size", DEFAULT_WINDOW_SIZE);
        config.setProperty("file.grid.columns.per.row", String.valueOf(DEFAULT_GRID_COLUMNS));
        config.setProperty("file.thumbnail.enabled", String.valueOf(DEFAULT_THUMBNAIL_ENABLED));
        config.setProperty("file.thumbnail.size", DEFAULT_THUMBNAIL_SIZE);
        config.setProperty("file.manager.use.emoji.icons", String.valueOf(DEFAULT_USE_EMOJI_ICONS));
        config.setProperty("search.case.sensitive", String.valueOf(DEFAULT_SEARCH_CASE_SENSITIVE));
        config.setProperty("search.fuzzy.enabled", String.valueOf(DEFAULT_SEARCH_FUZZY_ENABLED));
        config.setProperty("default.sort.method", DEFAULT_SORT_METHOD);
        config.setProperty("import.auto.generate.thumbnail", String.valueOf(DEFAULT_AUTO_GENERATE_THUMBNAIL));
        config.setProperty("import.show.progress.for.large.files", String.valueOf(DEFAULT_SHOW_PROGRESS_FOR_LARGE_FILES));
        config.setProperty("large.file.threshold", String.valueOf(DEFAULT_LARGE_FILE_THRESHOLD));
        config.setProperty("thumbnail.cache.max.size", String.valueOf(DEFAULT_THUMBNAIL_CACHE_SIZE));
        config.setProperty("delete.require.confirmation", String.valueOf(DEFAULT_DELETE_REQUIRE_CONFIRMATION));
        config.setProperty("operation.logging.enabled", String.valueOf(DEFAULT_OPERATION_LOGGING));
        config.setProperty("log.file.max.size", String.valueOf(DEFAULT_LOG_FILE_MAX_SIZE));
        config.setProperty("lazy.loading.enabled", String.valueOf(DEFAULT_LAZY_LOADING));
        config.setProperty("file.list.refresh.interval", String.valueOf(DEFAULT_REFRESH_INTERVAL));
        config.setProperty("memory.usage.limit", String.valueOf(DEFAULT_MEMORY_LIMIT));
        config.setProperty("multi.threading.enabled", String.valueOf(DEFAULT_MULTI_THREADING));
        config.setProperty("debug.mode.enabled", String.valueOf(DEFAULT_DEBUG_MODE));
        config.setProperty("show.detailed.error.messages", String.valueOf(DEFAULT_SHOW_DETAILED_ERRORS));
        config.setProperty("performance.metrics.enabled", String.valueOf(DEFAULT_PERFORMANCE_METRICS));
    }
    
    /**
     * 儲存默認配置檔
     */
    private void saveDefaultConfig() {
        try (FileOutputStream fos = new FileOutputStream(CONFIG_FILE)) {
            config.store(fos, "E_Reader File Manager Configuration - Auto Generated");
            System.out.println("✅ 已創建默認配置檔: " + CONFIG_FILE);
        } catch (IOException e) {
            System.err.println("⚠️ 無法創建配置檔: " + e.getMessage());
        }
    }
    
    /**
     * 儲存當前配置
     */
    public void saveConfig() {
        try (FileOutputStream fos = new FileOutputStream(CONFIG_FILE)) {
            config.store(fos, "E_Reader File Manager Configuration - Last Updated: " + 
                    java.time.LocalDateTime.now().toString());
            System.out.println("✅ 配置已儲存");
        } catch (IOException e) {
            System.err.println("❌ 儲存配置失敗: " + e.getMessage());
        }
    }
    
    // === 基本設定 getter 方法 ===
    
    public String getLibraryRootPath() {
        String path = config.getProperty("library.root.path", DEFAULT_LIBRARY_PATH);
        if (path.trim().isEmpty()) {
            return System.getProperty("user.home") + File.separator + "E_Reader_Library";
        }
        return path;
    }
    
    public boolean isShowWelcome() {
        return Boolean.parseBoolean(config.getProperty("file.manager.show.welcome", 
                String.valueOf(DEFAULT_SHOW_WELCOME)));
    }
    
    public boolean isRememberLastFolder() {
        return Boolean.parseBoolean(config.getProperty("file.manager.remember.last.folder", 
                String.valueOf(DEFAULT_REMEMBER_LAST_FOLDER)));
    }
    
    public int[] getWindowSize() {
        String sizeStr = config.getProperty("file.manager.window.size", DEFAULT_WINDOW_SIZE);
        try {
            String[] parts = sizeStr.split("x");
            return new int[]{Integer.parseInt(parts[0]), Integer.parseInt(parts[1])};
        } catch (Exception e) {
            System.err.println("⚠️ 無效的視窗大小設定，使用默認值: " + e.getMessage());
            return new int[]{1000, 700};
        }
    }
    
    // === 界面設定 getter 方法 ===
    
    public int getGridColumnsPerRow() {
        try {
            return Integer.parseInt(config.getProperty("file.grid.columns.per.row", 
                    String.valueOf(DEFAULT_GRID_COLUMNS)));
        } catch (NumberFormatException e) {
            return DEFAULT_GRID_COLUMNS;
        }
    }
    
    public boolean isThumbnailEnabled() {
        return Boolean.parseBoolean(config.getProperty("file.thumbnail.enabled", 
                String.valueOf(DEFAULT_THUMBNAIL_ENABLED)));
    }
    
    public int[] getThumbnailSize() {
        String sizeStr = config.getProperty("file.thumbnail.size", DEFAULT_THUMBNAIL_SIZE);
        try {
            String[] parts = sizeStr.split("x");
            return new int[]{Integer.parseInt(parts[0]), Integer.parseInt(parts[1])};
        } catch (Exception e) {
            return new int[]{80, 60};
        }
    }
    
    public boolean isUseEmojiIcons() {
        return Boolean.parseBoolean(config.getProperty("file.manager.use.emoji.icons", 
                String.valueOf(DEFAULT_USE_EMOJI_ICONS)));
    }
    
    // === 搜尋設定 getter 方法 ===
    
    public boolean isSearchCaseSensitive() {
        return Boolean.parseBoolean(config.getProperty("search.case.sensitive", 
                String.valueOf(DEFAULT_SEARCH_CASE_SENSITIVE)));
    }
    
    public boolean isSearchFuzzyEnabled() {
        return Boolean.parseBoolean(config.getProperty("search.fuzzy.enabled", 
                String.valueOf(DEFAULT_SEARCH_FUZZY_ENABLED)));
    }
    
    public String getDefaultSortMethod() {
        return config.getProperty("default.sort.method", DEFAULT_SORT_METHOD);
    }
    
    // === 檔案處理設定 getter 方法 ===
    
    public boolean isAutoGenerateThumbnail() {
        return Boolean.parseBoolean(config.getProperty("import.auto.generate.thumbnail", 
                String.valueOf(DEFAULT_AUTO_GENERATE_THUMBNAIL)));
    }
    
    public boolean isShowProgressForLargeFiles() {
        return Boolean.parseBoolean(config.getProperty("import.show.progress.for.large.files", 
                String.valueOf(DEFAULT_SHOW_PROGRESS_FOR_LARGE_FILES)));
    }
    
    public int getLargeFileThreshold() {
        try {
            return Integer.parseInt(config.getProperty("large.file.threshold", 
                    String.valueOf(DEFAULT_LARGE_FILE_THRESHOLD)));
        } catch (NumberFormatException e) {
            return DEFAULT_LARGE_FILE_THRESHOLD;
        }
    }
    
    public int getThumbnailCacheMaxSize() {
        try {
            return Integer.parseInt(config.getProperty("thumbnail.cache.max.size", 
                    String.valueOf(DEFAULT_THUMBNAIL_CACHE_SIZE)));
        } catch (NumberFormatException e) {
            return DEFAULT_THUMBNAIL_CACHE_SIZE;
        }
    }
    
    // === 安全設定 getter 方法 ===
    
    public boolean isDeleteRequireConfirmation() {
        return Boolean.parseBoolean(config.getProperty("delete.require.confirmation", 
                String.valueOf(DEFAULT_DELETE_REQUIRE_CONFIRMATION)));
    }
    
    public boolean isOperationLoggingEnabled() {
        return Boolean.parseBoolean(config.getProperty("operation.logging.enabled", 
                String.valueOf(DEFAULT_OPERATION_LOGGING)));
    }
    
    public int getLogFileMaxSize() {
        try {
            return Integer.parseInt(config.getProperty("log.file.max.size", 
                    String.valueOf(DEFAULT_LOG_FILE_MAX_SIZE)));
        } catch (NumberFormatException e) {
            return DEFAULT_LOG_FILE_MAX_SIZE;
        }
    }
    
    // === 效能設定 getter 方法 ===
    
    public boolean isLazyLoadingEnabled() {
        return Boolean.parseBoolean(config.getProperty("lazy.loading.enabled", 
                String.valueOf(DEFAULT_LAZY_LOADING)));
    }
    
    public int getFileListRefreshInterval() {
        try {
            return Integer.parseInt(config.getProperty("file.list.refresh.interval", 
                    String.valueOf(DEFAULT_REFRESH_INTERVAL)));
        } catch (NumberFormatException e) {
            return DEFAULT_REFRESH_INTERVAL;
        }
    }
    
    public int getMemoryUsageLimit() {
        try {
            return Integer.parseInt(config.getProperty("memory.usage.limit", 
                    String.valueOf(DEFAULT_MEMORY_LIMIT)));
        } catch (NumberFormatException e) {
            return DEFAULT_MEMORY_LIMIT;
        }
    }
    
    public boolean isMultiThreadingEnabled() {
        return Boolean.parseBoolean(config.getProperty("multi.threading.enabled", 
                String.valueOf(DEFAULT_MULTI_THREADING)));
    }
    
    // === 開發者選項 getter 方法 ===
    
    public boolean isDebugModeEnabled() {
        return Boolean.parseBoolean(config.getProperty("debug.mode.enabled", 
                String.valueOf(DEFAULT_DEBUG_MODE)));
    }
    
    public boolean isShowDetailedErrorMessages() {
        return Boolean.parseBoolean(config.getProperty("show.detailed.error.messages", 
                String.valueOf(DEFAULT_SHOW_DETAILED_ERRORS)));
    }
    
    public boolean isPerformanceMetricsEnabled() {
        return Boolean.parseBoolean(config.getProperty("performance.metrics.enabled", 
                String.valueOf(DEFAULT_PERFORMANCE_METRICS)));
    }
    
    // === setter 方法 ===
    
    public void setLibraryRootPath(String path) {
        config.setProperty("library.root.path", path != null ? path : "");
    }
    
    public void setShowWelcome(boolean show) {
        config.setProperty("file.manager.show.welcome", String.valueOf(show));
    }
    
    public void setRememberLastFolder(boolean remember) {
        config.setProperty("file.manager.remember.last.folder", String.valueOf(remember));
    }
    
    public void setWindowSize(int width, int height) {
        config.setProperty("file.manager.window.size", width + "x" + height);
    }
    
    public void setGridColumnsPerRow(int columns) {
        config.setProperty("file.grid.columns.per.row", String.valueOf(columns));
    }
    
    public void setThumbnailEnabled(boolean enabled) {
        config.setProperty("file.thumbnail.enabled", String.valueOf(enabled));
    }
    
    public void setDefaultSortMethod(String method) {
        config.setProperty("default.sort.method", method);
    }
    
    // === 工具方法 ===
    
    /**
     * 重置所有設定為默認值
     */
    public void resetToDefaults() {
        config.clear();
        loadDefaultConfig();
        saveConfig();
        System.out.println("✅ 所有設定已重置為默認值");
    }
    
    /**
     * 檢查配置檔案是否存在
     */
    public boolean configFileExists() {
        return new File(CONFIG_FILE).exists();
    }
    
    /**
     * 獲取配置檔案路徑
     */
    public String getConfigFilePath() {
        return new File(CONFIG_FILE).getAbsolutePath();
    }
    
    /**
     * 列印所有配置項目（調試用）
     */
    public void printAllConfig() {
        if (isDebugModeEnabled()) {
            System.out.println("=== E_Reader 檔案管理器配置 ===");
            for (String key : config.stringPropertyNames()) {
                System.out.println(key + " = " + config.getProperty(key));
            }
            System.out.println("==============================");
        }
    }
    
    /**
     * 獲取原始配置屬性對象（謹慎使用）
     */
    protected Properties getRawConfig() {
        return config;
    }
}
