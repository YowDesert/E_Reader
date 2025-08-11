package E_Reader.test;

import E_Reader.core.FormatPreservingTextProcessor;
import E_Reader.core.TextExtractor;
import E_Reader.viewer.EnhancedTextRenderer;

import java.util.Arrays;
import java.util.List;

/**
 * 格式保持功能測試類
 * 演示如何使用格式保持的文字處理器來保持原文件的排版
 */
public class FormatPreservingTest {

    public static void main(String[] args) {
        testFormatPreservation();
        demonstrateUsage();
        testThemeFormats();
    }

    /**
     * 測試格式保持功能
     */
    public static void testFormatPreservation() {
        System.out.println("=== 格式保持功能測試 ===");
        
        // 創建格式處理器
        FormatPreservingTextProcessor processor = new FormatPreservingTextProcessor();
        
        // 模擬原始文件內容（包含各種格式）
        List<String> testLines = Arrays.asList(
            "第一章  概述",  // 標題
            "",               // 空行
            "    這是一個有縮排的段落。這裡展示了四個空格的縮排。",
            "        這是更深層的縮排，有八個空格。",
            "",
            "1. 這是編號列表的第一項",
            "2. 這是編號列表的第二項",
            "   - 這是嵌套的項目符號",
            "   - 另一個嵌套項目",
            "",
            "普通段落文字，沒有特殊格式。",
            "",
            "名稱        年齡    職業",      // 表格對齊文字
            "張三        25      工程師",
            "李四        30      設計師", 
            "王五        28      產品經理",
            "",
            "　　中文全形空格縮排的段落。",
            "　　　　更深層的中文縮排。",
            "",
            "• 項目符號列表",
            "• 另一個項目",
            "  ○ 子項目",
            "",
            "這是包含    多個    空格    的文字，用於測試對齊功能。"
        );
        
        System.out.println("原始文字:");
        for (String line : testLines) {
            System.out.println("'" + line + "'");
        }
        
        System.out.println("\n=== 格式分析結果 ===");
        
        // 處理每一行
        for (String line : testLines) {
            FormatPreservingTextProcessor.FormattedLine formattedLine = processor.processLine(line);
            
            System.out.printf("行類型: %-12s | 縮排級別: %d | 內容: '%s'%n", 
                formattedLine.lineType.getDisplayName(), 
                formattedLine.indentLevel, 
                formattedLine.content);
            
            // 顯示文字段落詳情
            if (!formattedLine.segments.isEmpty()) {
                System.out.print("  文字段落: ");
                for (FormatPreservingTextProcessor.TextSegment segment : formattedLine.segments) {
                    System.out.printf("[%s:'%s'] ", segment.type.getDisplayName(), segment.text);
                }
                System.out.println();
            }
            
            System.out.println("  ────────────────────────");
        }
        
        testSpecificFormatCases();
    }
    
    /**
     * 測試特定格式案例
     */
    private static void testSpecificFormatCases() {
        System.out.println("\n=== 特定格式測試 ===");
        
        FormatPreservingTextProcessor processor = new FormatPreservingTextProcessor();
        
        // 測試不同類型的縮排
        String[] indentTests = {
            "    四個空格縮排",
            "\t一個Tab縮排", 
            "        八個空格縮排",
            "\t\t兩個Tab縮排",
            "　中文全形空格縮排",
            "　　　三個中文全形空格",
            "    \t混合縮排（4空格+1Tab）"
        };
        
        System.out.println("縮排測試:");
        for (String test : indentTests) {
            FormatPreservingTextProcessor.FormattedLine result = processor.processLine(test);
            System.out.printf("縮排級別 %d: '%s' -> '%s'%n", 
                result.indentLevel, test, result.content);
        }
        
        // 測試多重空格保持
        String[] spaceTests = {
            "單詞    間隔    測試",
            "名字        年齡        城市",
            "A     B     C     D",
            "數字1      數字2      數字3",
            "這裡有  兩個  空格",
            "這裡有   三個   空格",
            "這裡有     五個     空格"
        };
        
        System.out.println("\n多重空格測試:");
        for (String test : spaceTests) {
            FormatPreservingTextProcessor.FormattedLine result = processor.processLine(test);
            System.out.printf("原文: '%s'%n", test);
            System.out.print("段落分析: ");
            for (FormatPreservingTextProcessor.TextSegment segment : result.segments) {
                if (segment.type == FormatPreservingTextProcessor.SegmentType.MULTIPLE_SPACES) {
                    System.out.printf("[%d個空格] ", segment.text.length());
                } else if (segment.type == FormatPreservingTextProcessor.SegmentType.SINGLE_SPACE) {
                    System.out.print("[1空格] ");
                } else {
                    System.out.printf("[文字:'%s'] ", segment.text);
                }
            }
            System.out.println();
            System.out.println("  ────────────────");
        }
        
        // 測試行類型識別
        String[] lineTypeTests = {
            "第一章  開始",
            "Chapter 1: Introduction",
            "1. 第一項",
            "2) 第二項",
            "3、第三項", 
            "• 項目符號",
            "- 破折號項目",
            "○ 圓圈項目",
            "普通文字段落",
            "表格    數據    對齊    測試",
            ""
        };
        
        System.out.println("\n行類型識別測試:");
        for (String test : lineTypeTests) {
            FormatPreservingTextProcessor.FormattedLine result = processor.processLine(test);
            System.out.printf("'%s' -> %s%n", test, result.lineType.getDisplayName());
        }
    }
    
    /**
     * 演示如何在實際應用中使用格式保持功能
     */
    public static void demonstrateUsage() {
        System.out.println("\n=== 實際使用演示 ===");
        
        // 模擬從 PDF 或文字檔案中提取的內容
        String extractedText = 
            "第三章  演算法實現\n" +
            "\n" +
            "    本章將詳細介紹演算法的具體實現方法。\n" +
            "\n" +
            "3.1 基本概念\n" +
            "    演算法是解決問題的步驟序列。主要特點包括：\n" +
            "    \n" +
            "    1. 有限性    - 演算法必須在有限步驟內結束\n" +
            "    2. 確定性    - 每一步都有確定的含義\n" +
            "    3. 可行性    - 每一步都能夠執行\n" +
            "    4. 輸入      - 零個或多個輸入\n" +
            "    5. 輸出      - 一個或多個輸出\n" +
            "\n" +
            "    演算法複雜度分析：\n" +
            "    \n" +
            "    時間複雜度        空間複雜度        說明\n" +
            "    O(1)              O(1)              常數時間\n" +
            "    O(log n)          O(1)              對數時間\n" +
            "    O(n)              O(n)              線性時間\n" +
            "    O(n log n)        O(n)              線性對數時間\n" +
            "    O(n²)             O(1)              平方時間\n" +
            "\n" +
            "    • 常見排序演算法比較\n" +
            "      ○ 冒泡排序：簡單但效率較低\n" +
            "      ○ 快速排序：平均情況下效率高\n" +
            "      ○ 合併排序：穩定的 O(n log n)\n" +
            "\n" +
            "　　在實際應用中，我們需要根據資料特性選擇合適的演算法。\n" +
            "　　　　例如：小數據集可以使用插入排序。\n" +
            "　　　　　　　　大數據集建議使用快速排序或合併排序。";
        
        System.out.println("原始提取的文字:");
        System.out.println("────────────────────────────────");
        System.out.println(extractedText);
        System.out.println("────────────────────────────────");
        
        // 使用格式處理器處理文字
        FormatPreservingTextProcessor processor = new FormatPreservingTextProcessor();
        String[] lines = extractedText.split("\n");
        List<String> lineList = Arrays.asList(lines);
        List<FormatPreservingTextProcessor.FormattedLine> formattedLines = processor.processLines(lineList);
        
        System.out.println("\n格式保持處理後的結果:");
        System.out.println("────────────────────────────────");
        
        for (FormatPreservingTextProcessor.FormattedLine line : formattedLines) {
            // 顯示縮排
            String indentDisplay = "";
            for (int i = 0; i < line.indentLevel; i++) {
                indentDisplay += "  "; // 每級縮排用兩個空格表示
            }
            
            // 根據行類型設定前綴
            String typePrefix = "";
            switch (line.lineType) {
                case HEADER:
                    typePrefix = "[標題] ";
                    break;
                case NUMBERED_LIST:
                    typePrefix = "[編號] ";
                    break;
                case BULLET_LIST:
                    typePrefix = "[項目] ";
                    break;
                case ALIGNED_TEXT:
                    typePrefix = "[對齊] ";
                    break;
                case EMPTY:
                    typePrefix = "[空行] ";
                    break;
                default:
                    typePrefix = "[文字] ";
                    break;
            }
            
            if (line.lineType == FormatPreservingTextProcessor.LineType.EMPTY) {
                System.out.println(typePrefix);
            } else {
                // 重建完整行（包含縮排和內容）
                StringBuilder rebuiltLine = new StringBuilder();
                rebuiltLine.append(indentDisplay);
                
                for (FormatPreservingTextProcessor.TextSegment segment : line.segments) {
                    rebuiltLine.append(segment.text);
                }
                
                System.out.printf("%s%s%s%n", typePrefix, indentDisplay, rebuiltLine.toString().trim());
            }
        }
        
        System.out.println("────────────────────────────────");
        System.out.println("\n格式統計:");
        
        // 統計各種格式類型
        long headerCount = formattedLines.stream().filter(l -> l.lineType == FormatPreservingTextProcessor.LineType.HEADER).count();
        long listCount = formattedLines.stream().filter(l -> 
            l.lineType == FormatPreservingTextProcessor.LineType.NUMBERED_LIST || 
            l.lineType == FormatPreservingTextProcessor.LineType.BULLET_LIST).count();
        long alignedCount = formattedLines.stream().filter(l -> l.lineType == FormatPreservingTextProcessor.LineType.ALIGNED_TEXT).count();
        long indentedCount = formattedLines.stream().filter(l -> l.indentLevel > 0).count();
        long emptyCount = formattedLines.stream().filter(l -> l.lineType == FormatPreservingTextProcessor.LineType.EMPTY).count();
        
        System.out.printf("- 標題行: %d%n", headerCount);
        System.out.printf("- 列表行: %d%n", listCount);
        System.out.printf("- 對齊文字行: %d%n", alignedCount);
        System.out.printf("- 有縮排的行: %d%n", indentedCount);
        System.out.printf("- 空行: %d%n", emptyCount);
        System.out.printf("- 總行數: %d%n", formattedLines.size());
    }
    
    /**
     * 測試不同主題下的格式顯示
     */
    public static void testThemeFormats() {
        System.out.println("\n=== 主題格式測試 ===");
        
        FormatPreservingTextProcessor processor = new FormatPreservingTextProcessor();
        
        // 測試文字
        String testText = "第一章 測試\n    縮排文字\n1. 編號項目\n• 符號項目\n名稱    年齡    職業";
        String[] lines = testText.split("\n");
        
        // 測試不同主題
        E_Reader.settings.SettingsManager.ThemeMode[] themes = {
            E_Reader.settings.SettingsManager.ThemeMode.LIGHT,
            E_Reader.settings.SettingsManager.ThemeMode.DARK,
            E_Reader.settings.SettingsManager.ThemeMode.BLACK,
            E_Reader.settings.SettingsManager.ThemeMode.EYE_CARE,
            E_Reader.settings.SettingsManager.ThemeMode.SEPIA
        };
        
        for (E_Reader.settings.SettingsManager.ThemeMode theme : themes) {
            System.out.printf("\n--- %s 主題 ---%n", theme.toString());
            processor.applyTheme(theme);
            
            for (String line : lines) {
                FormatPreservingTextProcessor.FormattedLine formatted = processor.processLine(line);
                System.out.printf("  %s: %s%n", formatted.lineType.getDisplayName(), formatted.content);
            }
        }
    }
}
