package E_Reader.core;

import javafx.scene.image.Image;
import javafx.embed.swing.SwingFXUtils;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Base64;
import java.util.concurrent.TimeUnit;
import java.util.List;
import java.util.ArrayList;

/**
 * LaTeX-OCR 整合器
 * 提供 Java 與 Python LaTeX-OCR 模組的橋接功能
 */
public class LatexOCRIntegrator {
    
    private static final String PYTHON_SCRIPT_PATH = "src/main/resources/run_latexocr.py";
    private static final int TIMEOUT_SECONDS = 30;
    private static final ObjectMapper objectMapper = new ObjectMapper();
    
    private boolean initialized = false;
    private String pythonCommand = "python";
    private String lastError = "";
    
    public LatexOCRIntegrator() {
        detectPythonCommand();
    }
    
    /**
     * 偵測系統中可用的 Python 命令
     */
    private void detectPythonCommand() {
        String[] possibleCommands = {"python3", "python", "py"};
        
        for (String cmd : possibleCommands) {
            try {
                ProcessBuilder pb = new ProcessBuilder(cmd, "--version");
                Process process = pb.start();
                
                if (process.waitFor(5, TimeUnit.SECONDS) && process.exitValue() == 0) {
                    this.pythonCommand = cmd;
                    System.out.println("LaTeX-OCR: 偵測到 Python 命令 - " + cmd);
                    return;
                }
            } catch (Exception e) {
                // 繼續嘗試下一個命令
            }
        }
        
        System.err.println("LaTeX-OCR: 警告 - 未偵測到可用的 Python 命令");
    }
    
    /**
     * 初始化 LaTeX-OCR 模組
     * 
     * @return 初始化是否成功
     */
    public boolean initialize() {
        if (initialized) {
            return true;
        }
        
        try {
            // 檢查 Python 腳本是否存在
            File scriptFile = new File(PYTHON_SCRIPT_PATH);
            if (!scriptFile.exists()) {
                lastError = "LaTeX-OCR Python 腳本不存在: " + PYTHON_SCRIPT_PATH;
                System.err.println(lastError);
                return false;
            }
            
            // 測試 LaTeX-OCR 模組
            LatexOCRResult testResult = runPythonScript("--action", "test");
            
            if (testResult.isSuccess()) {
                initialized = true;
                System.out.println("LaTeX-OCR 初始化成功");
                
                // 獲取版本資訊
                LatexOCRResult versionResult = runPythonScript("--action", "version");
                if (versionResult.isSuccess() && versionResult.getRawOutput() != null) {
                    System.out.println("LaTeX-OCR 版本資訊:\n" + versionResult.getRawOutput());
                }
                
                return true;
            } else {
                lastError = "LaTeX-OCR 模組測試失敗: " + testResult.getErrorMessage();
                System.err.println(lastError);
                return false;
            }
            
        } catch (Exception e) {
            lastError = "LaTeX-OCR 初始化過程中發生錯誤: " + e.getMessage();
            System.err.println(lastError);
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * 從圖片檔案識別 LaTeX 公式
     * 
     * @param imageFile 圖片檔案
     * @return LaTeX-OCR 結果
     */
    public LatexOCRResult recognizeLatexFromFile(File imageFile) {
        if (!initialized && !initialize()) {
            return new LatexOCRResult(false, null, lastError);
        }
        
        if (imageFile == null || !imageFile.exists()) {
            return new LatexOCRResult(false, null, "圖片檔案不存在");
        }
        
        try {
            return runPythonScript("--action", "process_file", 
                                 "--image_path", imageFile.getAbsolutePath());
        } catch (Exception e) {
            String error = "處理圖片檔案時發生錯誤: " + e.getMessage();
            System.err.println(error);
            return new LatexOCRResult(false, null, error);
        }
    }
    
    /**
     * 從 JavaFX Image 識別 LaTeX 公式
     * 
     * @param image JavaFX Image 對象
     * @return LaTeX-OCR 結果
     */
    public LatexOCRResult recognizeLatexFromImage(Image image) {
        if (!initialized && !initialize()) {
            return new LatexOCRResult(false, null, lastError);
        }
        
        if (image == null) {
            return new LatexOCRResult(false, null, "圖片對象為空");
        }
        
        try {
            // 將 JavaFX Image 轉換為 BufferedImage
            BufferedImage bufferedImage = SwingFXUtils.fromFXImage(image, null);
            
            // 將 BufferedImage 轉換為 Base64
            String base64Data = bufferedImageToBase64(bufferedImage);
            
            return runPythonScript("--action", "process_base64", 
                                 "--base64_data", base64Data);
                                 
        } catch (Exception e) {
            String error = "處理 JavaFX Image 時發生錯誤: " + e.getMessage();
            System.err.println(error);
            return new LatexOCRResult(false, null, error);
        }
    }
    
    /**
     * 從 BufferedImage 識別 LaTeX 公式
     * 
     * @param bufferedImage BufferedImage 對象
     * @return LaTeX-OCR 結果
     */
    public LatexOCRResult recognizeLatexFromBufferedImage(BufferedImage bufferedImage) {
        if (!initialized && !initialize()) {
            return new LatexOCRResult(false, null, lastError);
        }
        
        if (bufferedImage == null) {
            return new LatexOCRResult(false, null, "BufferedImage 對象為空");
        }
        
        try {
            // 將 BufferedImage 轉換為 Base64
            String base64Data = bufferedImageToBase64(bufferedImage);
            
            return runPythonScript("--action", "process_base64", 
                                 "--base64_data", base64Data);
                                 
        } catch (Exception e) {
            String error = "處理 BufferedImage 時發生錯誤: " + e.getMessage();
            System.err.println(error);
            return new LatexOCRResult(false, null, error);
        }
    }
    
    /**
     * 批量處理圖片列表中的 LaTeX 公式
     * 
     * @param images 圖片列表
     * @return LaTeX-OCR 結果列表
     */
    public List<LatexOCRResult> recognizeLatexFromImages(List<Image> images) {
        List<LatexOCRResult> results = new ArrayList<>();
        
        if (images == null || images.isEmpty()) {
            return results;
        }
        
        System.out.println("LaTeX-OCR: 開始批量處理 " + images.size() + " 張圖片");
        
        for (int i = 0; i < images.size(); i++) {
            Image image = images.get(i);
            System.out.println("LaTeX-OCR: 處理第 " + (i + 1) + " 張圖片");
            
            LatexOCRResult result = recognizeLatexFromImage(image);
            results.add(result);
            
            // 簡單的進度報告
            if ((i + 1) % 10 == 0 || (i + 1) == images.size()) {
                System.out.println("LaTeX-OCR: 已完成 " + (i + 1) + "/" + images.size() + " 張圖片");
            }
        }
        
        return results;
    }
    
    /**
     * 將 BufferedImage 轉換為 Base64 字符串
     * 
     * @param image BufferedImage 對象
     * @return Base64 編碼的圖片數據
     * @throws IOException IO 異常
     */
    private String bufferedImageToBase64(BufferedImage image) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(image, "PNG", baos);
        byte[] imageBytes = baos.toByteArray();
        return Base64.getEncoder().encodeToString(imageBytes);
    }
    
    /**
     * 執行 Python 腳本
     * 
     * @param args 命令行參數
     * @return LaTeX-OCR 結果
     */
    private LatexOCRResult runPythonScript(String... args) {
        try {
            // 構建命令
            List<String> command = new ArrayList<>();
            command.add(pythonCommand);
            command.add(PYTHON_SCRIPT_PATH);
            
            for (String arg : args) {
                command.add(arg);
            }
            
            // 執行命令
            ProcessBuilder pb = new ProcessBuilder(command);
            pb.redirectErrorStream(true);
            
            Process process = pb.start();
            
            // 讀取輸出
            StringBuilder output = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream(), "UTF-8"))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    output.append(line).append("\n");
                }
            }
            
            // 等待程序完成
            boolean finished = process.waitFor(TIMEOUT_SECONDS, TimeUnit.SECONDS);
            
            if (!finished) {
                process.destroyForcibly();
                return new LatexOCRResult(false, null, "Python 腳本執行超時");
            }
            
            int exitCode = process.exitValue();
            String rawOutput = output.toString().trim();
            
            if (exitCode != 0) {
                return new LatexOCRResult(false, null, "Python 腳本執行失敗，退出碼: " + exitCode + "\n輸出: " + rawOutput);
            }
            
            // 解析 JSON 輸出
            return parseJsonOutput(rawOutput);
            
        } catch (Exception e) {
            String error = "執行 Python 腳本時發生錯誤: " + e.getMessage();
            System.err.println(error);
            e.printStackTrace();
            return new LatexOCRResult(false, null, error);
        }
    }
    
    /**
     * 解析 JSON 輸出
     * 
     * @param jsonOutput JSON 輸出字符串
     * @return LaTeX-OCR 結果
     */
    private LatexOCRResult parseJsonOutput(String jsonOutput) {
        try {
            JsonNode rootNode = objectMapper.readTree(jsonOutput);
            
            boolean success = rootNode.path("success").asBoolean(false);
            String latexCode = rootNode.path("latex_code").asText(null);
            String error = rootNode.path("error").asText(null);
            
            return new LatexOCRResult(success, latexCode, error, jsonOutput);
            
        } catch (Exception e) {
            System.err.println("解析 JSON 輸出時發生錯誤: " + e.getMessage());
            System.err.println("原始輸出: " + jsonOutput);
            
            // 如果 JSON 解析失敗，嘗試將原始輸出作為 LaTeX 代碼
            if (jsonOutput != null && !jsonOutput.trim().isEmpty() && 
                !jsonOutput.contains("錯誤") && !jsonOutput.contains("error")) {
                return new LatexOCRResult(true, jsonOutput.trim(), null, jsonOutput);
            }
            
            return new LatexOCRResult(false, null, "JSON 解析失敗: " + e.getMessage(), jsonOutput);
        }
    }
    
    /**
     * 獲取版本資訊
     * 
     * @return 版本資訊字符串
     */
    public String getVersionInfo() {
        try {
            LatexOCRResult result = runPythonScript("--action", "version");
            if (result.isSuccess()) {
                return result.getRawOutput();
            } else {
                return "無法獲取版本資訊: " + result.getErrorMessage();
            }
        } catch (Exception e) {
            return "獲取版本資訊時發生錯誤: " + e.getMessage();
        }
    }
    
    /**
     * 檢查 LaTeX-OCR 是否可用
     * 
     * @return 是否可用
     */
    public boolean isAvailable() {
        return initialized || initialize();
    }
    
    /**
     * 獲取最後的錯誤訊息
     * 
     * @return 錯誤訊息
     */
    public String getLastError() {
        return lastError;
    }
    
    /**
     * 設定 Python 命令
     * 
     * @param pythonCommand Python 命令路徑
     */
    public void setPythonCommand(String pythonCommand) {
        this.pythonCommand = pythonCommand;
        this.initialized = false; // 重置初始化狀態
    }
    
    /**
     * LaTeX-OCR 結果類
     */
    public static class LatexOCRResult {
        private final boolean success;
        private final String latexCode;
        private final String errorMessage;
        private final String rawOutput;
        
        public LatexOCRResult(boolean success, String latexCode, String errorMessage) {
            this(success, latexCode, errorMessage, null);
        }
        
        public LatexOCRResult(boolean success, String latexCode, String errorMessage, String rawOutput) {
            this.success = success;
            this.latexCode = latexCode;
            this.errorMessage = errorMessage;
            this.rawOutput = rawOutput;
        }
        
        public boolean isSuccess() {
            return success;
        }
        
        public String getLatexCode() {
            return latexCode;
        }
        
        public String getErrorMessage() {
            return errorMessage;
        }
        
        public String getRawOutput() {
            return rawOutput;
        }
        
        @Override
        public String toString() {
            if (success) {
                return "LaTeX-OCR 成功: " + (latexCode != null ? latexCode : "無結果");
            } else {
                return "LaTeX-OCR 失敗: " + (errorMessage != null ? errorMessage : "未知錯誤");
            }
        }
    }
}
