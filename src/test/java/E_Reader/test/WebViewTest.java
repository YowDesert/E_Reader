package E_Reader.test;

import javafx.scene.web.WebView;
import javafx.scene.web.WebEngine;

/**
 * 簡單的 WebView 測試類別
 * 用於驗證 javafx.scene.web 包是否可以正確導入
 */
public class WebViewTest {
    
    public static void testWebViewImport() {
        try {
            System.out.println("測試 WebView 導入...");
            
            // 嘗試創建 WebView 實例
            WebView webView = new WebView();
            WebEngine webEngine = webView.getEngine();
            
            System.out.println("✅ WebView 導入成功！");
            System.out.println("WebView 類型: " + webView.getClass().getName());
            System.out.println("WebEngine 類型: " + webEngine.getClass().getName());
            
        } catch (Exception e) {
            System.out.println("❌ WebView 導入失敗: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    public static void main(String[] args) {
        testWebViewImport();
    }
}
