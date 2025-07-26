package E_Reader;

import javafx.scene.control.ScrollPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.scene.paint.Color;
import javafx.scene.control.Label;
import javafx.geometry.Insets;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import java.util.List;

public class TextRenderer {

    private ScrollPane scrollPane;
    private VBox contentContainer;
    private List<TextExtractor.PageText> pages;
    private int currentPageIndex = 0;

    // 樣式設定
    private Color backgroundColor = Color.web("#1e1e1e");
    private Color textColor = Color.web("#e0e0e0");
    private Color headerColor = Color.web("#ffffff");
    private Color highlightColor = Color.web("#ffff00");
    private Color highlightTextColor = Color.web("#000000");

    private double baseFontSize = 16.0;
    private Font textFont = Font.font("Microsoft JhengHei", baseFontSize);
    private Font headerFont = Font.font("Microsoft JhengHei", FontWeight.BOLD, baseFontSize * 1.25);
    private double lineSpacing = 1.5;
    private double paragraphSpacing = 20;
    private double pageMargin = 40;

    // 搜尋相關
    private String currentSearchTerm = "";

    public TextRenderer() {
        initializeComponents();
    }

    private void initializeComponents() {
        contentContainer = new VBox();
        contentContainer.setPadding(new Insets(pageMargin));
        contentContainer.setSpacing(paragraphSpacing);

        scrollPane = new ScrollPane(contentContainer);
        scrollPane.setFitToWidth(true);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);

        applyTheme();
    }

    /**
     * 設定要渲染的頁面文字
     */
    public void setPages(List<TextExtractor.PageText> pages) {
        this.pages = pages;
        currentPageIndex = 0;
        renderAllPages();
    }

    /**
     * 渲染所有頁面
     */
    private void renderAllPages() {
        if (pages == null || pages.isEmpty()) {
            showNoContentMessage();
            return;
        }

        contentContainer.getChildren().clear();

        for (int i = 0; i < pages.size(); i++) {
            TextExtractor.PageText page = pages.get(i);
            renderPage(page);

            // 在頁面之間添加分隔符
            if (i < pages.size() - 1) {
                addPageSeparator(i + 1);
            }
        }
    }

    /**
     * 渲染單個頁面
     */
    private void renderPage(TextExtractor.PageText page) {
        // 頁面標題
        Label pageHeader = createPageHeader(page);
        contentContainer.getChildren().add(pageHeader);

        // 獲取格式化的段落
        List<String> paragraphs = page.getFormattedParagraphs();

        if (paragraphs.isEmpty()) {
            Label noTextLabel = new Label("本頁無文字內容或文字提取失敗");
            noTextLabel.setTextFill(Color.web("#888888"));
            noTextLabel.setFont(Font.font("Microsoft JhengHei", baseFontSize * 0.875));
            contentContainer.getChildren().add(noTextLabel);
            return;
        }

        // 渲染段落
        for (String paragraph : paragraphs) {
            if (!paragraph.trim().isEmpty()) {
                TextFlow textFlow = createParagraphTextFlow(paragraph);
                contentContainer.getChildren().add(textFlow);
            }
        }
    }

    /**
     * 創建頁面標題
     */
    private Label createPageHeader(TextExtractor.PageText page) {
        String headerText = String.format("第 %d 頁 (%s)",
                page.getPageNumber() + 1,
                page.getTextSource().getDisplayName());

        Label header = new Label(headerText);
        header.setTextFill(headerColor);
        header.setFont(headerFont);
        header.setPadding(new Insets(20, 0, 10, 0));

        return header;
    }

    /**
     * 創建段落文字流
     */
    private TextFlow createParagraphTextFlow(String paragraph) {
        TextFlow textFlow = new TextFlow();
        textFlow.setLineSpacing(lineSpacing);
        textFlow.setPadding(new Insets(0, 0, paragraphSpacing, 0));

        if (currentSearchTerm.isEmpty()) {
            // 沒有搜尋詞，直接顯示文字
            Text text = new Text(paragraph);
            text.setFill(textColor);
            text.setFont(textFont);
            textFlow.getChildren().add(text);
        } else {
            // 有搜尋詞，需要高亮顯示
            addHighlightedText(textFlow, paragraph, currentSearchTerm);
        }

        return textFlow;
    }

    /**
     * 添加頁面分隔符
     */
    private void addPageSeparator(int nextPageNumber) {
        VBox separator = new VBox();
        separator.setPrefHeight(30);
        separator.setStyle("-fx-border-color: #444444; -fx-border-width: 0 0 1 0; -fx-border-style: dashed;");

        Label separatorLabel = new Label("--- 第 " + nextPageNumber + " 頁 ---");
        separatorLabel.setTextFill(Color.web("#666666"));
        separatorLabel.setFont(Font.font("Microsoft JhengHei", baseFontSize * 0.75));
        separatorLabel.setPadding(new Insets(10));

        separator.getChildren().add(separatorLabel);
        contentContainer.getChildren().add(separator);
    }

    /**
     * 顯示無內容訊息
     */
    private void showNoContentMessage() {
        contentContainer.getChildren().clear();

        Label messageLabel = new Label("尚未載入任何文件\n請選擇PDF檔案或圖片資料夾，然後切換到文字模式");
        messageLabel.setTextFill(Color.web("#888888"));
        messageLabel.setFont(Font.font("Microsoft JhengHei", baseFontSize * 1.125));
        messageLabel.setStyle("-fx-text-alignment: center;");

        contentContainer.getChildren().add(messageLabel);
    }

    /**
     * 跳轉到指定頁面
     */
    public void goToPage(int pageIndex) {
        if (pages == null || pageIndex < 0 || pageIndex >= pages.size()) {
            return;
        }

        currentPageIndex = pageIndex;

        // 計算頁面在容器中的位置
        double pagePosition = calculatePagePosition(pageIndex);

        // 滾動到指定位置
        scrollPane.setVvalue(pagePosition);
    }

    /**
     * 計算頁面在滾動容器中的相對位置
     */
    private double calculatePagePosition(int pageIndex) {
        if (pages == null || pages.isEmpty()) {
            return 0;
        }

        // 簡單計算：假設每頁占用相等空間
        return (double) pageIndex / Math.max(1, pages.size() - 1);
    }

    /**
     * 應用主題樣式
     */
    public void applyTheme() {
        // 設定背景色
        BackgroundFill backgroundFill = new BackgroundFill(backgroundColor, CornerRadii.EMPTY, Insets.EMPTY);
        Background background = new Background(backgroundFill);

        contentContainer.setBackground(background);
        scrollPane.setStyle(String.format("-fx-background: %s; -fx-background-color: %s;",
                toHexString(backgroundColor), toHexString(backgroundColor)));
    }

    /**
     * 設定主題色彩
     */
    public void setThemeColors(SettingsPanel.ThemeMode theme) {
        switch (theme) {
            case LIGHT:
                backgroundColor = Color.web("#ffffff");
                textColor = Color.web("#000000");
                headerColor = Color.web("#333333");
                highlightColor = Color.web("#ffff00");
                highlightTextColor = Color.web("#000000");
                break;
            case DARK:
                backgroundColor = Color.web("#1e1e1e");
                textColor = Color.web("#e0e0e0");
                headerColor = Color.web("#ffffff");
                highlightColor = Color.web("#ffff00");
                highlightTextColor = Color.web("#000000");
                break;
            case BLACK:
                backgroundColor = Color.web("#000000");
                textColor = Color.web("#e0e0e0");
                headerColor = Color.web("#ffffff");
                highlightColor = Color.web("#ffff00");
                highlightTextColor = Color.web("#000000");
                break;
            case EYE_CARE:
                backgroundColor = Color.web("#1a1a0f");
                textColor = Color.web("#d4d4aa");
                headerColor = Color.web("#f0f0cc");
                highlightColor = Color.web("#ffff99");
                highlightTextColor = Color.web("#333300");
                break;
            case SEPIA:
                backgroundColor = Color.web("#f4ecd8");
                textColor = Color.web("#5d4e37");
                headerColor = Color.web("#3d2e17");
                highlightColor = Color.web("#ff6600");
                highlightTextColor = Color.web("#ffffff");
                break;
        }

        applyTheme();

        // 重新渲染以應用新顏色
        if (pages != null && !pages.isEmpty()) {
            renderAllPages();
        }
    }

    /**
     * 設定字體大小
     */
    public void setFontSize(double size) {
        if (size < 8) size = 8;   // 最小字體大小
        if (size > 72) size = 72; // 最大字體大小

        baseFontSize = size;
        textFont = Font.font(textFont.getFamily(), size);
        headerFont = Font.font(headerFont.getFamily(), FontWeight.BOLD, size * 1.25);

        // 重新渲染
        if (pages != null && !pages.isEmpty()) {
            renderAllPages();
        }
    }

    /**
     * 調整字體大小
     */
    public void adjustFontSize(double delta) {
        setFontSize(baseFontSize + delta);
    }

    /**
     * 設定行距
     */
    public void setLineSpacing(double spacing) {
        if (spacing < 0.5) spacing = 0.5;   // 最小行距
        if (spacing > 5.0) spacing = 5.0;   // 最大行距

        this.lineSpacing = spacing;

        // 重新渲染
        if (pages != null && !pages.isEmpty()) {
            renderAllPages();
        }
    }

    /**
     * 設定段落間距
     */
    public void setParagraphSpacing(double spacing) {
        if (spacing < 5) spacing = 5;       // 最小段落間距
        if (spacing > 50) spacing = 50;     // 最大段落間距

        this.paragraphSpacing = spacing;
        contentContainer.setSpacing(spacing);

        // 重新渲染
        if (pages != null && !pages.isEmpty()) {
            renderAllPages();
        }
    }

    /**
     * 設定頁面邊距
     */
    public void setPageMargin(double margin) {
        if (margin < 10) margin = 10;       // 最小邊距
        if (margin > 100) margin = 100;     // 最大邊距

        this.pageMargin = margin;
        contentContainer.setPadding(new Insets(margin));
    }

    /**
     * 搜尋文字
     */
    public void searchText(String searchTerm) {
        if (pages == null || searchTerm == null) {
            return;
        }

        currentSearchTerm = searchTerm.trim();

        // 重新渲染以顯示搜尋結果
        renderAllPages();

        // 如果有搜尋結果，跳轉到第一個匹配項
        if (!currentSearchTerm.isEmpty()) {
            scrollToFirstMatch();
        }
    }

    /**
     * 清除搜尋高亮
     */
    public void clearSearch() {
        currentSearchTerm = "";
        renderAllPages();
    }

    /**
     * 滾動到第一個搜尋匹配項
     */
    private void scrollToFirstMatch() {
        if (currentSearchTerm.isEmpty() || pages == null) {
            return;
        }

        String lowerSearchTerm = currentSearchTerm.toLowerCase();

        for (int i = 0; i < pages.size(); i++) {
            String pageText = pages.get(i).getBestText().toLowerCase();
            if (pageText.contains(lowerSearchTerm)) {
                goToPage(i);
                currentPageIndex = i;
                break;
            }
        }
    }

    /**
     * 添加高亮文字到TextFlow
     */
    private void addHighlightedText(TextFlow textFlow, String paragraph, String searchTerm) {
        String lowerParagraph = paragraph.toLowerCase();
        String lowerSearchTerm = searchTerm.toLowerCase();

        int lastIndex = 0;
        int index = lowerParagraph.indexOf(lowerSearchTerm, lastIndex);

        while (index != -1) {
            // 添加搜尋詞之前的文字
            if (index > lastIndex) {
                Text beforeText = new Text(paragraph.substring(lastIndex, index));
                beforeText.setFill(textColor);
                beforeText.setFont(textFont);
                textFlow.getChildren().add(beforeText);
            }

            // 添加高亮的搜尋詞
            Text highlightText = new Text(paragraph.substring(index, index + searchTerm.length()));
            highlightText.setFill(highlightTextColor);
            highlightText.setFont(textFont);
            highlightText.setStyle(String.format("-fx-background-color: %s;", toHexString(highlightColor)));
            textFlow.getChildren().add(highlightText);

            lastIndex = index + searchTerm.length();
            index = lowerParagraph.indexOf(lowerSearchTerm, lastIndex);
        }

        // 添加剩餘的文字
        if (lastIndex < paragraph.length()) {
            Text remainingText = new Text(paragraph.substring(lastIndex));
            remainingText.setFill(textColor);
            remainingText.setFont(textFont);
            textFlow.getChildren().add(remainingText);
        }
    }

    /**
     * 獲取下一個搜尋結果
     */
    public boolean findNext() {
        if (currentSearchTerm.isEmpty() || pages == null) {
            return false;
        }

        String lowerSearchTerm = currentSearchTerm.toLowerCase();

        // 從當前頁面的下一頁開始搜尋
        for (int i = currentPageIndex + 1; i < pages.size(); i++) {
            String pageText = pages.get(i).getBestText().toLowerCase();
            if (pageText.contains(lowerSearchTerm)) {
                goToPage(i);
                currentPageIndex = i;
                return true;
            }
        }

        // 如果沒找到，從頭開始搜尋到當前頁面
        for (int i = 0; i <= currentPageIndex; i++) {
            String pageText = pages.get(i).getBestText().toLowerCase();
            if (pageText.contains(lowerSearchTerm)) {
                goToPage(i);
                currentPageIndex = i;
                return true;
            }
        }

        return false;
    }

    /**
     * 獲取上一個搜尋結果
     */
    public boolean findPrevious() {
        if (currentSearchTerm.isEmpty() || pages == null) {
            return false;
        }

        String lowerSearchTerm = currentSearchTerm.toLowerCase();

        // 從當前頁面的上一頁開始向前搜尋
        for (int i = currentPageIndex - 1; i >= 0; i--) {
            String pageText = pages.get(i).getBestText().toLowerCase();
            if (pageText.contains(lowerSearchTerm)) {
                goToPage(i);
                currentPageIndex = i;
                return true;
            }
        }

        // 如果沒找到，從最後一頁開始搜尋到當前頁面
        for (int i = pages.size() - 1; i >= currentPageIndex; i--) {
            String pageText = pages.get(i).getBestText().toLowerCase();
            if (pageText.contains(lowerSearchTerm)) {
                goToPage(i);
                currentPageIndex = i;
                return true;
            }
        }

        return false;
    }

    /**
     * 導出文字內容
     */
    public String exportText() {
        if (pages == null || pages.isEmpty()) {
            return "";
        }

        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < pages.size(); i++) {
            TextExtractor.PageText page = pages.get(i);
            sb.append("=== 第 ").append(i + 1).append(" 頁 ===\n");

            List<String> paragraphs = page.getFormattedParagraphs();
            for (String paragraph : paragraphs) {
                if (!paragraph.trim().isEmpty()) {
                    sb.append(paragraph).append("\n\n");
                }
            }

            if (i < pages.size() - 1) {
                sb.append("\n");
            }
        }

        return sb.toString();
    }

    // 輔助方法
    private String toHexString(Color color) {
        return String.format("#%02X%02X%02X",
                (int) (color.getRed() * 255),
                (int) (color.getGreen() * 255),
                (int) (color.getBlue() * 255));
    }

    // Getter 方法
    public ScrollPane getScrollPane() {
        return scrollPane;
    }

    public int getCurrentPageIndex() {
        return currentPageIndex;
    }

    public int getTotalPages() {
        return pages != null ? pages.size() : 0;
    }

    public boolean hasContent() {
        return pages != null && !pages.isEmpty();
    }

    public double getFontSize() {
        return baseFontSize;
    }

    public double getLineSpacing() {
        return lineSpacing;
    }

    public double getParagraphSpacing() {
        return paragraphSpacing;
    }

    public double getPageMargin() {
        return pageMargin;
    }

    public String getCurrentSearchTerm() {
        return currentSearchTerm;
    }

    public List<TextExtractor.PageText> getPages() {
        return pages;
    }

    // Setter 方法
    public void setCurrentPageIndex(int pageIndex) {
        if (pageIndex >= 0 && pageIndex < getTotalPages()) {
            this.currentPageIndex = pageIndex;
        }
    }
}