package E_Reader.core;

import E_Reader.settings.SettingsManager;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.geometry.Insets;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.scene.control.Label;

import java.util.*;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

/**
 * 格式保持文字處理器
 * 專門用於保持原文件的格式，包括空格、縮排、行間距等
 */
public class FormatPreservingTextProcessor {

    // 格式檢測的正規表達式
    private static final Pattern LEADING_SPACES = Pattern.compile("^([ \\t]+)(.*)$");
    private static final Pattern MULTIPLE_SPACES = Pattern.compile("( {2,})");
    private static final Pattern TAB_SPACES = Pattern.compile("\\t");
    private static final Pattern CHINESE_INDENT = Pattern.compile("^(　+)(.*)$");
    private static final Pattern MIXED_INDENT = Pattern.compile("^([ \\t　]+)(.*)$");
    private static final Pattern LINE_WITH_NUMBERS = Pattern.compile("^(\\d+[.)、])\\s*(.*)$");
    private static final Pattern BULLET_POINTS = Pattern.compile("^([•·-‧○●])\\s*(.*)$");
    private static final Pattern PARAGRAPH_HEADER = Pattern.compile("^(第[一二三四五六七八九十0-9]+[章節段]|[A-Z0-9]+[.)、])(.*)$");

    // 樣式設定
    private Color textColor = Color.web("#e0e0e0");
    private Color indentColor = Color.web("#888888");
    private Color numberColor = Color.web("#99ccff");
    private Color headerColor = Color.web("#ffffff");
    private Font normalFont;
    private Font monoFont; // 等寬字體，用於保持空格對齊
    private double fontSize = 18.0;
    private double lineHeight = 1.8;

    public FormatPreservingTextProcessor() {
        initializeFonts();
    }

    public FormatPreservingTextProcessor(double fontSize) {
        this.fontSize = fontSize;
        initializeFonts();
    }

    private void initializeFonts() {
        try {
            normalFont = Font.font("Microsoft JhengHei", fontSize);
            // 使用等寬字體來精確控制空格顯示
            monoFont = Font.font("Consolas", fontSize);
            if (monoFont.getName().equals("System")) {
                // 如果 Consolas 不可用，使用其他等寬字體
                monoFont = Font.font("Courier New", fontSize);
            }
        } catch (Exception e) {
            normalFont = Font.font("System", fontSize);
            monoFont = Font.font("Monospaced", fontSize);
        }
    }

    /**
     * 處理單行文字，保持原有格式
     */
    public FormattedLine processLine(String originalLine) {
        if (originalLine == null) {
            return new FormattedLine("", "", 0, LineType.NORMAL);
        }

        // 分析縮排
        IndentInfo indentInfo = analyzeIndentation(originalLine);

        // 分析行類型
        LineType lineType = analyzeLineType(indentInfo.content);

        // 處理內容中的多重空格
        List<TextSegment> segments = processContentSpaces(indentInfo.content, lineType);

        return new FormattedLine(originalLine, indentInfo.content,
                indentInfo.indentLevel, lineType, indentInfo.indentText, segments);
    }

    public static List<String> splitIntoParagraphs(String rawText) {
        List<String> paragraphs = new ArrayList<>();
        StringBuilder currentParagraph = new StringBuilder();

        String[] lines = rawText.split("\\r?\\n");

        for (String line : lines) {
            if (line.trim().isEmpty()) continue;

                    // 偵測是否是新段落開頭：兩個以上的空格、tab 或全形空格
            if (line.matches("^[ \\t\u3000]{2,}.*")) {
                    if (currentParagraph.length() > 0) {
                            paragraphs.add(currentParagraph.toString().trim());
                            currentParagraph.setLength(0);
                    }
            }

                currentParagraph.append(line.trim()).append(" ");
        }

        if (currentParagraph.length() > 0) {
        paragraphs.add(currentParagraph.toString().trim());
        }

        return paragraphs;
    }
    /**
     * 處理文字段落列表，保持格式
     */
    public List<FormattedLine> processLines(List<String> lines) {
        List<FormattedLine> formattedLines = new ArrayList<>();

        if (lines == null || lines.isEmpty()) {
            return formattedLines;
        }

        for (String line : lines) {
            formattedLines.add(processLine(line));
        }

        return formattedLines;
    }

    /**
     * 處理頁面文字，保持原有格式
     */
    public List<FormattedLine> processPageText(TextExtractor.PageText pageText) {
        if (pageText == null) {
            return new ArrayList<>();
        }

        String text = pageText.getBestText();
        if (text == null || text.isEmpty()) {
            return new ArrayList<>();
        }

        // 按行分割，保持原有的換行結構
        String[] lines = text.split("\n", -1); // -1 保持空行
        List<String> lineList = Arrays.asList(lines);

        return processLines(lineList);
    }

    /**
     * 分析縮排信息
     */
    private IndentInfo analyzeIndentation(String line) {
        if (line.isEmpty()) {
            return new IndentInfo("", line, 0);
        }

        // 檢測中文全形空格縮排
        Matcher chineseMatcher = CHINESE_INDENT.matcher(line);
        if (chineseMatcher.matches()) {
            String indentText = chineseMatcher.group(1);
            String content = chineseMatcher.group(2);
            int indentLevel = indentText.length(); // 每個全形空格算一級
            return new IndentInfo(indentText, content, indentLevel);
        }

        // 檢測英文空格和Tab縮排
        Matcher spaceMatcher = LEADING_SPACES.matcher(line);
        if (spaceMatcher.matches()) {
            String indentText = spaceMatcher.group(1);
            String content = spaceMatcher.group(2);

            // 計算縮排等級（4個空格或1個Tab為一級）
            int indentLevel = 0;
            for (char c : indentText.toCharArray()) {
                if (c == '\t') {
                    indentLevel += 1;
                } else if (c == ' ') {
                    indentLevel += 0.25; // 4個空格為一級
                }
            }

            return new IndentInfo(indentText, content, (int) Math.round(indentLevel));
        }

        // 沒有縮排
        return new IndentInfo("", line, 0);
    }

    /**
     * 分析行類型
     */
    private LineType analyzeLineType(String content) {
        if (content.trim().isEmpty()) {
            return LineType.EMPTY;
        }

        // 檢測段落標題
        if (PARAGRAPH_HEADER.matcher(content).matches()) {
            return LineType.HEADER;
        }

        // 檢測編號列表
        if (LINE_WITH_NUMBERS.matcher(content).matches()) {
            return LineType.NUMBERED_LIST;
        }

        // 檢測項目符號
        if (BULLET_POINTS.matcher(content).matches()) {
            return LineType.BULLET_LIST;
        }

        // 檢測是否包含大量空格（可能是表格或對齊文字）
        if (content.contains("    ") || content.contains("\t")) {
            return LineType.ALIGNED_TEXT;
        }

        return LineType.NORMAL;
    }

    /**
     * 處理內容中的空格
     */
    private List<TextSegment> processContentSpaces(String content, LineType lineType) {
        List<TextSegment> segments = new ArrayList<>();

        if (content.isEmpty()) {
            return segments;
        }

        // 對於對齊文字，保持所有空格
        if (lineType == LineType.ALIGNED_TEXT) {
            return processAlignedText(content);
        }

        // 對於普通文字，處理多重空格
        Matcher matcher = MULTIPLE_SPACES.matcher(content);
        int lastEnd = 0;

        while (matcher.find()) {
            // 添加空格前的文字
            if (matcher.start() > lastEnd) {
                String beforeText = content.substring(lastEnd, matcher.start());
                segments.add(new TextSegment(beforeText, SegmentType.NORMAL_TEXT));
            }

            // 添加多重空格段
            String spaces = matcher.group(1);
            segments.add(new TextSegment(spaces, SegmentType.MULTIPLE_SPACES));

            lastEnd = matcher.end();
        }

        // 添加剩餘文字
        if (lastEnd < content.length()) {
            String remainingText = content.substring(lastEnd);
            segments.add(new TextSegment(remainingText, SegmentType.NORMAL_TEXT));
        }

        // 如果沒有多重空格，整個內容作為普通文字
        if (segments.isEmpty()) {
            segments.add(new TextSegment(content, SegmentType.NORMAL_TEXT));
        }

        return segments;
    }

    /**
     * 處理對齊文字（如表格）
     */
    private List<TextSegment> processAlignedText(String content) {
        List<TextSegment> segments = new ArrayList<>();

        // 將 Tab 轉換為空格進行處理
        String processedContent = content.replaceAll("\t", "    ");

        StringBuilder currentText = new StringBuilder();
        StringBuilder currentSpaces = new StringBuilder();

        for (int i = 0; i < processedContent.length(); i++) {
            char c = processedContent.charAt(i);

            if (c == ' ') {
                // 如果前面有非空格文字，先添加它
                if (currentText.length() > 0) {
                    segments.add(new TextSegment(currentText.toString(), SegmentType.NORMAL_TEXT));
                    currentText.setLength(0);
                }
                currentSpaces.append(c);
            } else {
                // 如果前面有空格，先添加它們
                if (currentSpaces.length() > 0) {
                    if (currentSpaces.length() == 1) {
                        segments.add(new TextSegment(currentSpaces.toString(), SegmentType.SINGLE_SPACE));
                    } else {
                        segments.add(new TextSegment(currentSpaces.toString(), SegmentType.MULTIPLE_SPACES));
                    }
                    currentSpaces.setLength(0);
                }
                currentText.append(c);
            }
        }

        // 處理最後剩餘的內容
        if (currentText.length() > 0) {
            segments.add(new TextSegment(currentText.toString(), SegmentType.NORMAL_TEXT));
        }
        if (currentSpaces.length() > 0) {
            if (currentSpaces.length() == 1) {
                segments.add(new TextSegment(currentSpaces.toString(), SegmentType.SINGLE_SPACE));
            } else {
                segments.add(new TextSegment(currentSpaces.toString(), SegmentType.MULTIPLE_SPACES));
            }
        }

        return segments;
    }

    /**
     * 創建格式化的 TextFlow
     */
    public TextFlow createFormattedTextFlow(FormattedLine formattedLine) {
        TextFlow textFlow = new TextFlow();
        textFlow.setLineSpacing(lineHeight);

        // 添加縮排
        if (formattedLine.indentLevel > 0) {
            Text indentText = createIndentText(formattedLine.indentText, formattedLine.indentLevel);
            textFlow.getChildren().add(indentText);
        }

        // 添加內容
        for (TextSegment segment : formattedLine.segments) {
            Text segmentText = createSegmentText(segment, formattedLine.lineType);
            textFlow.getChildren().add(segmentText);
        }

        return textFlow;
    }

    /**
     * 創建縮排文字節點
     */
    private Text createIndentText(String indentText, int indentLevel) {
        Text text = new Text();

        // 將Tab和中文全形空格轉換為可見的縮排
        StringBuilder displayIndent = new StringBuilder();
        for (char c : indentText.toCharArray()) {
            if (c == '\t') {
                displayIndent.append("    "); // Tab 轉 4 空格
            } else if (c == '　') {
                displayIndent.append("  "); // 中文全形空格轉 2 空格
            } else {
                displayIndent.append(c);
            }
        }

        text.setText(displayIndent.toString());
        text.setFill(indentColor);
        text.setFont(monoFont); // 使用等寬字體

        return text;
    }

    /**
     * 創建文字段落節點
     */
    private Text createSegmentText(TextSegment segment, LineType lineType) {
        Text text = new Text(segment.text);

        // 根據段落類型設定樣式
        switch (segment.type) {
            case NORMAL_TEXT:
                text.setFill(getTextColorForLineType(lineType));
                text.setFont(normalFont);
                break;

            case SINGLE_SPACE:
                text.setFill(indentColor);
                text.setFont(monoFont);
                break;

            case MULTIPLE_SPACES:
                // 為多重空格使用特殊顯示（保持對齊）
                text.setFill(indentColor);
                text.setFont(monoFont);
                break;
        }

        // 為標題添加粗體
        if (lineType == LineType.HEADER) {
            Font boldFont = Font.font(normalFont.getFamily(), FontWeight.BOLD, normalFont.getSize());
            text.setFont(boldFont);
        }

        return text;
    }

    /**
     * 根據行類型獲取文字顏色
     */
    private Color getTextColorForLineType(LineType lineType) {
        switch (lineType) {
            case HEADER:
                return headerColor;
            case NUMBERED_LIST:
            case BULLET_LIST:
                return numberColor;
            case ALIGNED_TEXT:
                return textColor;
            case EMPTY:
                return Color.TRANSPARENT;
            default:
                return textColor;
        }
    }

    /**
     * 批量處理並創建 VBox 容器
     */
    public VBox createFormattedContainer(List<FormattedLine> formattedLines) {
        VBox container = new VBox();
        container.setSpacing(2); // 行間距

        for (FormattedLine line : formattedLines) {
            if (line.lineType == LineType.EMPTY) {
                // 空行
                Label spacer = new Label();
                spacer.setPrefHeight(fontSize * 0.5);
                container.getChildren().add(spacer);
            } else {
                TextFlow textFlow = createFormattedTextFlow(line);
                container.getChildren().add(textFlow);
            }
        }

        return container;
    }

    // Getter 和 Setter 方法
    public void setFontSize(double fontSize) {
        this.fontSize = fontSize;
        initializeFonts();
    }

    public void setLineHeight(double lineHeight) {
        this.lineHeight = lineHeight;
    }

    public void setTextColor(Color color) {
        this.textColor = color;
    }

    public void setIndentColor(Color color) {
        this.indentColor = color;
    }

    public void setNumberColor(Color color) {
        this.numberColor = color;
    }

    public void setHeaderColor(Color color) {
        this.headerColor = color;
    }

    public void applyTheme(SettingsManager.ThemeMode theme) {
        switch (theme) {
            case LIGHT:
                textColor = Color.web("#000000");
                indentColor = Color.web("#666666");
                numberColor = Color.web("#0066cc");
                headerColor = Color.web("#333333");
                break;
            case DARK:
                textColor = Color.web("#e0e0e0");
                indentColor = Color.web("#888888");
                numberColor = Color.web("#99ccff");
                headerColor = Color.web("#ffffff");
                break;
            case BLACK:
                textColor = Color.web("#e0e0e0");
                indentColor = Color.web("#666666");
                numberColor = Color.web("#88bbff");
                headerColor = Color.web("#ffffff");
                break;
            case EYE_CARE:
                textColor = Color.web("#d4d4aa");
                indentColor = Color.web("#999977");
                numberColor = Color.web("#bbcc88");
                headerColor = Color.web("#f0f0cc");
                break;
            case SEPIA:
                textColor = Color.web("#5d4e37");
                indentColor = Color.web("#8b7355");
                numberColor = Color.web("#b8860b");
                headerColor = Color.web("#3d2e17");
                break;
        }
    }

    // 內部類別定義

    /**
     * 縮排信息類
     */
    public static class IndentInfo {
        public final String indentText;
        public final String content;
        public final int indentLevel;

        public IndentInfo(String indentText, String content, int indentLevel) {
            this.indentText = indentText;
            this.content = content;
            this.indentLevel = indentLevel;
        }
    }

    /**
     * 格式化行類
     */
    public static class FormattedLine {
        public final String originalText;
        public final String content;
        public final int indentLevel;
        public final LineType lineType;
        public final String indentText;
        public final List<TextSegment> segments;

        public FormattedLine(String originalText, String content, int indentLevel, LineType lineType) {
            this(originalText, content, indentLevel, lineType, "", new ArrayList<>());
        }

        public FormattedLine(String originalText, String content, int indentLevel,
                             LineType lineType, String indentText, List<TextSegment> segments) {
            this.originalText = originalText;
            this.content = content;
            this.indentLevel = indentLevel;
            this.lineType = lineType;
            this.indentText = indentText;
            this.segments = segments != null ? segments : new ArrayList<>();
        }

        @Override
        public String toString() {
            return String.format("FormattedLine[indent=%d, type=%s, content='%s']",
                    indentLevel, lineType, content);
        }
    }

    /**
     * 文字段落類
     */
    public static class TextSegment {
        public final String text;
        public final SegmentType type;

        public TextSegment(String text, SegmentType type) {
            this.text = text;
            this.type = type;
        }

        @Override
        public String toString() {
            return String.format("TextSegment[type=%s, text='%s']", type, text);
        }
    }

    /**
     * 行類型枚舉
     */
    public enum LineType {
        NORMAL("普通文字"),
        HEADER("標題"),
        NUMBERED_LIST("編號列表"),
        BULLET_LIST("項目列表"),
        ALIGNED_TEXT("對齊文字"),
        EMPTY("空行");

        private final String displayName;

        LineType(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    /**
     * 文字段落類型枚舉
     */
    public enum SegmentType {
        NORMAL_TEXT("普通文字"),
        SINGLE_SPACE("單一空格"),
        MULTIPLE_SPACES("多重空格");

        private final String displayName;

        SegmentType(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }
}