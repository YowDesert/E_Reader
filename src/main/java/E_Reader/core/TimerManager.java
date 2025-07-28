package E_Reader.core;

import javafx.application.Platform;

import java.util.Timer;
import java.util.TimerTask;

/**
 * 計時器管理器 - 管理各種計時功能
 */
public class TimerManager {

    // 計時器實例
    private Timer readingTimerInstance;
    private Timer eyeCareReminderInstance;
    private Timer autoScrollInstance;

    // 計時任務
    private TimerTask readingTimerTask;
    private TimerTask eyeCareReminderTask;
    private TimerTask autoScrollTask;

    // 狀態標記
    private boolean isReadingTimerRunning = false;
    private boolean isEyeCareReminderRunning = false;
    private boolean isAutoScrollRunning = false;

    // 預設間隔時間（毫秒）
    private static final long READING_TIMER_INTERVAL = 1000;      // 1秒
    private static final long EYE_CARE_REMINDER_INTERVAL = 30 * 60 * 1000; // 30分鐘
    private static final long AUTO_SCROLL_INTERVAL = 3000;        // 3秒

    public TimerManager() {
        // 初始化計時器
    }

    /**
     * 啟動閱讀時間計時器
     * 
     * @param callback 回調函數，每秒執行一次
     */
    public void startReadingTimer(Runnable callback) {
        if (isReadingTimerRunning) {
            stopReadingTimer();
        }

        readingTimerInstance = new Timer("ReadingTimer", true);
        readingTimerTask = new TimerTask() {
            @Override
            public void run() {
                Platform.runLater(callback);
            }
        };

        readingTimerInstance.scheduleAtFixedRate(readingTimerTask, 
                READING_TIMER_INTERVAL, READING_TIMER_INTERVAL);
        isReadingTimerRunning = true;
    }

    /**
     * 停止閱讀時間計時器
     */
    public void stopReadingTimer() {
        if (readingTimerTask != null) {
            readingTimerTask.cancel();
            readingTimerTask = null;
        }
        if (readingTimerInstance != null) {
            readingTimerInstance.cancel();
            readingTimerInstance.purge();
            readingTimerInstance = null;
        }
        isReadingTimerRunning = false;
    }

    /**
     * 啟動護眼提醒計時器
     * 
     * @param callback 回調函數，每30分鐘執行一次
     */
    public void startEyeCareReminder(Runnable callback) {
        if (isEyeCareReminderRunning) {
            stopEyeCareReminder();
        }

        eyeCareReminderInstance = new Timer("EyeCareReminder", true);
        eyeCareReminderTask = new TimerTask() {
            @Override
            public void run() {
                Platform.runLater(callback);
            }
        };

        eyeCareReminderInstance.scheduleAtFixedRate(eyeCareReminderTask, 
                EYE_CARE_REMINDER_INTERVAL, EYE_CARE_REMINDER_INTERVAL);
        isEyeCareReminderRunning = true;
    }

    /**
     * 停止護眼提醒計時器
     */
    public void stopEyeCareReminder() {
        if (eyeCareReminderTask != null) {
            eyeCareReminderTask.cancel();
            eyeCareReminderTask = null;
        }
        if (eyeCareReminderInstance != null) {
            eyeCareReminderInstance.cancel();
            eyeCareReminderInstance.purge();
            eyeCareReminderInstance = null;
        }
        isEyeCareReminderRunning = false;
    }

    /**
     * 啟動自動翻頁計時器（預設間隔）
     * 
     * @param callback 回調函數，每3秒執行一次
     */
    public void startAutoScroll(Runnable callback) {
        startAutoScrollWithInterval(callback, AUTO_SCROLL_INTERVAL);
    }

    /**
     * 啟動自動翻頁計時器（自訂間隔）
     * 
     * @param callback 回調函數
     * @param intervalMs 間隔時間（毫秒）
     */
    public void startAutoScrollWithInterval(Runnable callback, long intervalMs) {
        if (isAutoScrollRunning) {
            stopAutoScroll();
        }

        autoScrollInstance = new Timer("AutoScroll", true);
        autoScrollTask = new TimerTask() {
            @Override
            public void run() {
                Platform.runLater(callback);
            }
        };

        autoScrollInstance.scheduleAtFixedRate(autoScrollTask, intervalMs, intervalMs);
        isAutoScrollRunning = true;
    }

    /**
     * 停止自動翻頁計時器
     */
    public void stopAutoScroll() {
        if (autoScrollTask != null) {
            autoScrollTask.cancel();
            autoScrollTask = null;
        }
        if (autoScrollInstance != null) {
            autoScrollInstance.cancel();
            autoScrollInstance.purge();
            autoScrollInstance = null;
        }
        isAutoScrollRunning = false;
    }

    /**
     * 停止所有計時器
     */
    public void stopAllTimers() {
        stopReadingTimer();
        stopEyeCareReminder();
        stopAutoScroll();
    }

    /**
     * 暫停所有計時器
     */
    public void pauseAllTimers() {
        // 暫時停止所有計時器，但保留狀態以便恢復
        if (readingTimerTask != null) {
            readingTimerTask.cancel();
        }
        if (eyeCareReminderTask != null) {
            eyeCareReminderTask.cancel();
        }
        if (autoScrollTask != null) {
            autoScrollTask.cancel();
        }
    }

    /**
     * 恢復所有之前運行的計時器
     */
    public void resumeAllTimers() {
        // 此方法需要配合具體的恢復邏輯
        // 目前簡化處理，實際使用時需要保存之前的callback
    }

    // 狀態查詢方法
    public boolean isReadingTimerRunning() {
        return isReadingTimerRunning;
    }

    public boolean isEyeCareReminderRunning() {
        return isEyeCareReminderRunning;
    }

    public boolean isAutoScrollRunning() {
        return isAutoScrollRunning;
    }

    /**
     * 獲取所有計時器狀態
     * 
     * @return 狀態描述字串
     */
    public String getTimerStatus() {
        StringBuilder status = new StringBuilder();
        status.append("計時器狀態:\n");
        status.append("閱讀計時器: ").append(isReadingTimerRunning ? "運行中" : "已停止").append("\n");
        status.append("護眼提醒: ").append(isEyeCareReminderRunning ? "運行中" : "已停止").append("\n");
        status.append("自動翻頁: ").append(isAutoScrollRunning ? "運行中" : "已停止").append("\n");
        return status.toString();
    }

    /**
     * 重置所有計時器
     */
    public void resetAllTimers() {
        stopAllTimers();
        
        // 清理資源
        System.gc(); // 建議垃圾回收
    }

    /**
     * 檢查是否有任何計時器在運行
     * 
     * @return 如果有任何計時器在運行則返回true
     */
    public boolean hasActiveTimer() {
        return isReadingTimerRunning || isEyeCareReminderRunning || isAutoScrollRunning;
    }

    /**
     * 安全地取消計時器任務
     * 
     * @param task 要取消的任務
     */
    private void safelyCancelTask(TimerTask task) {
        if (task != null) {
            try {
                task.cancel();
            } catch (Exception e) {
                System.err.println("取消計時器任務時發生錯誤: " + e.getMessage());
            }
        }
    }

    /**
     * 安全地取消並清理計時器
     * 
     * @param timer 要清理的計時器
     */
    private void safelyCleanupTimer(Timer timer) {
        if (timer != null) {
            try {
                timer.cancel();
                timer.purge();
            } catch (Exception e) {
                System.err.println("清理計時器時發生錯誤: " + e.getMessage());
            }
        }
    }

    /**
     * 創建延遲任務
     * 
     * @param callback 回調函數
     * @param delayMs 延遲時間（毫秒）
     * @return 計時器任務
     */
    public TimerTask createDelayedTask(Runnable callback, long delayMs) {
        Timer delayTimer = new Timer("DelayedTask", true);
        TimerTask delayTask = new TimerTask() {
            @Override
            public void run() {
                Platform.runLater(() -> {
                    callback.run();
                    delayTimer.cancel();
                });
            }
        };
        
        delayTimer.schedule(delayTask, delayMs);
        return delayTask;
    }

    /**
     * 創建重複任務
     * 
     * @param callback 回調函數
     * @param intervalMs 間隔時間（毫秒）
     * @param maxExecutions 最大執行次數，-1表示無限制
     * @return 計時器實例
     */
    public Timer createRepeatingTask(Runnable callback, long intervalMs, int maxExecutions) {
        Timer repeatingTimer = new Timer("RepeatingTask", true);
        
        TimerTask repeatingTask = new TimerTask() {
            private int executionCount = 0;
            
            @Override
            public void run() {
                if (maxExecutions > 0 && executionCount >= maxExecutions) {
                    repeatingTimer.cancel();
                    return;
                }
                
                Platform.runLater(callback);
                executionCount++;
            }
        };
        
        repeatingTimer.scheduleAtFixedRate(repeatingTask, intervalMs, intervalMs);
        return repeatingTimer;
    }

    /**
     * 設定護眼提醒間隔時間
     * 
     * @param callback 回調函數
     * @param intervalMinutes 間隔時間（分鐘）
     */
    public void setEyeCareReminderInterval(Runnable callback, int intervalMinutes) {
        if (isEyeCareReminderRunning) {
            stopEyeCareReminder();
        }
        
        long intervalMs = intervalMinutes * 60 * 1000L;
        
        eyeCareReminderInstance = new Timer("EyeCareReminder", true);
        eyeCareReminderTask = new TimerTask() {
            @Override
            public void run() {
                Platform.runLater(callback);
            }
        };
        
        eyeCareReminderInstance.scheduleAtFixedRate(eyeCareReminderTask, intervalMs, intervalMs);
        isEyeCareReminderRunning = true;
    }
}
