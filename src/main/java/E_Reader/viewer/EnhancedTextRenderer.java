package E_Reader.viewer;

import E_Reader.core.TextExtractor;
import E_Reader.core.FormatPreservingTextProcessor;
import E_Reader.settings.SettingsManager;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.*;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.scene.paint.Color;
import javafx.scene.control.Label;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.TextAlignment;
import javafx.application.Platform;

import java.util.List;
import java.util.ArrayList;

/**
 * 增強版文字渲染器 - 支援格式保持的文字顯示
 */
public class EnhancedTextRenderer extends TextRenderer {

    private FormatPreservingTextProcessor formatProcessor;
    private boolean preserveOriginalFormat = true;
    private boolean showFormatIndicators = false;

    // 格式保持相關設定
    private double indentSize = 24.0; // 縮排大小（像素）
    private boolean useMonospaceForAlignment = true;
    private boolean highlightIndentation = false;

    public EnhancedTextRenderer() {
        super();
        initializeFormatProcessor();
    }

    /**
     * 初始化格式處理器
     */
    private void initializeFormatProcessor() {
        formatProcessor = new FormatPreservingTextProcessor(super.getFontSize());
    }

    /**
     * 設定頁面內容並保持格式
     */
    @Override
    public void setPages(List<TextExtractor.PageText> pages) {
        super.setPages(pages); // 調用父類方法設定基本資料

        if (preserveOriginalFormat && pages != null && !pages.isEmpty()) {
            // 使用格式保持模式重新渲染
            renderPagesWithFormat(pages);
        }
    }

    /**
     * 使用格式保持模式渲染頁面
     */
    private void renderPagesWithFormat(List<TextExtractor.PageText> pages) {
        List<EnhancedTextPage> formattedPages = new ArrayList<>();

        for (TextExtractor.PageText originalPage : pages) {
            // 使用格式處理器處理每一頁
            List<FormatPreservingTextProcessor.FormattedLine> formattedLines =
                    formatProcessor.processPageText(originalPage);

            EnhancedTextPage enhancedPage = new EnhancedTextPage();
            enhancedPage.pageNumber = originalPage.getPageNumber();
            enhancedPage.originalPageText = originalPage;
            enhancedPage.formattedLines = formattedLines;
            enhancedPage.isEmpty = formattedLines.isEmpty() ||
                    formattedLines.stream().allMatch(line -> line.lineType == FormatPreservingTextProcessor.LineType.EMPTY);

            formattedPages.add(enhancedPage);
        }

        // 更新顯示
        Platform.runLater(() -> renderEnhancedPages(formattedPages));
    }

    /**
     * 渲染增強版頁面
     */
    private void renderEnhancedPages(List<EnhancedTextPage> enhancedPages) {
        if (enhancedPages == null || enhancedPages.isEmpty()) {
            showNoContentMessage();
            return;
        }

        VBox pageContainer = getPageContainer();
        pageContainer.getChildren().clear();

        // 根據顯示模式渲染
        switch (getDisplayMode()) {
            case PAGE_BY_PAGE:
                renderEnhancedSinglePage(enhancedPages);
                break;
            case TWO_PAGE:
                renderEnhancedTwoPages(enhancedPages);
                break;
            case CONTINUOUS:
                renderEnhancedContinuousPages(enhancedPages);
                break;
        }
    }

    /**
     * 渲染單頁（格式保持模式）
     */
    private void renderEnhancedSinglePage(List<EnhancedTextPage> pages) {
        int currentIndex = getCurrentPageIndex();
        if (currentIndex < 0 || currentIndex >= pages.size()) {
            return;
        }

        EnhancedTextPage page = pages.get(currentIndex);
        VBox pageView = createEnhancedPageView(page);

        VBox pageContainer = getPageContainer();
        pageContainer.getChildren().clear();
        pageContainer.getChildren().add(pageView);
        pageContainer.setAlignment(Pos.CENTER);

        Platform.runLater(() -> getScrollPane().setVvalue(0));
    }

    /**
     * 渲染雙頁（格式保持模式）
     */
    private void renderEnhancedTwoPages(List<EnhancedTextPage> pages) {
        VBox pageContainer = getPageContainer();
        pageContainer.getChildren().clear();

        HBox twoPageContainer = new HBox();
        twoPageContainer.setAlignment(Pos.CENTER);
        twoPageContainer.setSpacing(40);

        int currentIndex = getCurrentPageIndex();
        Region mainContainer = getMainContainerFromParent();
        double containerWidth = mainContainer != null ? mainContainer.getWidth() : 800;

        if (currentIndex < pages.size()) {
            EnhancedTextPage leftPage = pages.get(currentIndex);
            VBox leftPageView = createEnhancedPageView(leftPage);
            leftPageView.setPrefWidth(containerWidth / 2 - 100);
            twoPageContainer.getChildren().add(leftPageView);
        }

        if (currentIndex + 1 < pages.size()) {
            EnhancedTextPage rightPage = pages.get(currentIndex + 1);
            VBox rightPageView = createEnhancedPageView(rightPage);
            rightPageView.setPrefWidth(containerWidth / 2 - 100);
            twoPageContainer.getChildren().add(rightPageView);
        }

        pageContainer.getChildren().add(twoPageContainer);
        pageContainer.setAlignment(Pos.CENTER);
        ScrollPane scrollPane = getScrollPane();
        if (scrollPane != null) {
            Platform.runLater(() -> scrollPane.setVvalue(0));
        }
    }

    /**
     * 渲染連續頁面（格式保持模式）
     */
    private void renderEnhancedContinuousPages(List<EnhancedTextPage> pages) {
        VBox pageContainer = getPageContainer();
        pageContainer.getChildren().clear();

        for (EnhancedTextPage page : pages) {
            VBox pageView = createEnhancedPageView(page);
            pageView.setStyle(pageView.getStyle() + "; -fx-border-color: #444444; -fx-border-width: 0 0 2 0; -fx-border-style: dashed;");
            pageContainer.getChildren().add(pageView);

            if (pages.indexOf(page) < pages.size() - 1) {
                Label spacer = new Label();
                spacer.setPrefHeight(40);
                pageContainer.getChildren().add(spacer);
            }
        }

        pageContainer.setAlignment(Pos.CENTER);
    }

    /**
     * 創建增強版頁面視圖
     */
    private VBox createEnhancedPageView(EnhancedTextPage page) {
        VBox pageView = new VBox();
        pageView.setAlignment(Pos.TOP_CENTER);
        pageView.setPadding(new Insets(40, 0, 40, 0));
        pageView.setPrefWidth(Region.USE_COMPUTED_SIZE);
        pageView.setMaxWidth(Region.USE_COMPUTED_SIZE);

        // 設定背景
        Color backgroundColor = getBackgroundColor();
        BackgroundFill backgroundFill = new BackgroundFill(backgroundColor, new CornerRadii(10), Insets.EMPTY);
        pageView.setBackground(new Background(backgroundFill));

        // 頁面標題
        Label pageHeader = createPageHeader(page);
        pageView.getChildren().add(pageHeader);

        if (page.isEmpty || page.formattedLines.isEmpty()) {
            Label noTextLabel = new Label("本頁無文字內容");
            noTextLabel.setTextFill(Color.web("#888888"));
            noTextLabel.setFont(Font.font(getFontSize() * 0.875));
            noTextLabel.setAlignment(Pos.CENTER);
            pageView.getChildren().add(noTextLabel);
            return pageView;
        }

        // 格式統計信息（如果啟用）
        if (showFormatIndicators) {
            Label formatInfo = createFormatInfoLabel(page);
            pageView.getChildren().add(formatInfo);
        }

        // 渲染格式化內容
        VBox contentContainer = createFormattedContent(page.formattedLines);
        pageView.getChildren().add(contentContainer);

        return pageView;
    }

    /**
     * 創建格式化內容容器
     */
    private VBox createFormattedContent(List<FormatPreservingTextProcessor.FormattedLine> formattedLines) {
        VBox contentContainer = new VBox();
        contentContainer.setAlignment(Pos.CENTER_LEFT);
        contentContainer.setSpacing(2);
        contentContainer.setPadding(new Insets(0, 60, 0, 60));

        double textAreaWidth = calculateOptimalTextWidth();

        for (FormatPreservingTextProcessor.FormattedLine line : formattedLines) {
            if (line.lineType == FormatPreservingTextProcessor.LineType.EMPTY) {
                // 空行
                Label spacer = new Label();
                spacer.setPrefHeight(getFontSize() * getLineSpacing() * 0.5);
                contentContainer.getChildren().add(spacer);
            } else {
                // 創建格式化的行
                HBox lineContainer = createFormattedLineContainer(line);
                lineContainer.setPrefWidth(textAreaWidth);
                lineContainer.setMaxWidth(textAreaWidth);
                contentContainer.getChildren().add(lineContainer);
            }
        }

        return contentContainer;
    }

    /**
     * 創建格式化行容器
     */
    private HBox createFormattedLineContainer(FormatPreservingTextProcessor.FormattedLine line) {
        HBox lineContainer = new HBox();
        lineContainer.setAlignment(Pos.CENTER_LEFT);
        lineContainer.setSpacing(0);

        // 添加縮排指示器（如果啟用）
        if (highlightIndentation && line.indentLevel > 0) {
            VBox indentIndicator = createIndentIndicator(line.indentLevel);
            lineContainer.getChildren().add(indentIndicator);
        }

        // 創建文字內容
        TextFlow textFlow = formatProcessor.createFormattedTextFlow(line);

        // 根據行類型調整樣式
        adjustTextFlowStyle(textFlow, line.lineType);

        // 應用搜尋高亮（如果有）
        if (!getCurrentSearchTerm().isEmpty()) {
            applySearchHighlight(textFlow, line.content, getCurrentSearchTerm());
        }

        lineContainer.getChildren().add(textFlow);

        return lineContainer;
    }

    /**
     * 創建縮排指示器
     */
    private VBox createIndentIndicator(int indentLevel) {
        VBox indicator = new VBox();
        indicator.setAlignment(Pos.TOP_LEFT);
        indicator.setPrefWidth(indentSize);
        indicator.setMaxWidth(indentSize);

        // 為每個縮排層級創建視覺指示
        for (int i = 0; i < indentLevel; i++) {
            Label indentLine = new Label("│");
            indentLine.setTextFill(Color.web("#555555"));
            indentLine.setFont(Font.font("Consolas", getFontSize() * 0.7));
            indentLine.setPadding(new Insets(0, 0, 0, i * 8));
            indicator.getChildren().add(indentLine);
        }

        return indicator;
    }

    /**
     * 調整 TextFlow 樣式
     */
    private void adjustTextFlowStyle(TextFlow textFlow, FormatPreservingTextProcessor.LineType lineType) {
        switch (lineType) {
            case HEADER:
                textFlow.setStyle(textFlow.getStyle() + "; -fx-background-color: rgba(255,255,255,0.05);");
                textFlow.setPadding(new Insets(8, 12, 8, 12));
                break;
            case NUMBERED_LIST:
            case BULLET_LIST:
                textFlow.setPadding(new Insets(4, 8, 4, 16));
                break;
            case ALIGNED_TEXT:
                // 對齊文字使用特殊背景
                if (showFormatIndicators) {
                    textFlow.setStyle(textFlow.getStyle() + "; -fx-background-color: rgba(100,150,200,0.1);");
                }
                textFlow.setPadding(new Insets(2, 8, 2, 8));
                break;
            default:
                textFlow.setPadding(new Insets(2, 8, 2, 8));
                break;
        }
    }

    /**
     * 創建格式資訊標籤
     */
    private Label createFormatInfoLabel(EnhancedTextPage page) {
        // 統計各種行類型
        long headerCount = page.formattedLines.stream()
                .filter(line -> line.lineType == FormatPreservingTextProcessor.LineType.HEADER).count();
        long listCount = page.formattedLines.stream()
                .filter(line -> line.lineType == FormatPreservingTextProcessor.LineType.NUMBERED_LIST ||
                        line.lineType == FormatPreservingTextProcessor.LineType.BULLET_LIST).count();
        long alignedCount = page.formattedLines.stream()
                .filter(line -> line.lineType == FormatPreservingTextProcessor.LineType.ALIGNED_TEXT).count();
        long indentedCount = page.formattedLines.stream()
                .filter(line -> line.indentLevel > 0).count();

        String infoText = String.format("格式: 標題%d 列表%d 對齊%d 縮排%d",
                headerCount, listCount, alignedCount, indentedCount);

        Label infoLabel = new Label(infoText);
        infoLabel.setTextFill(Color.web("#888888"));
        infoLabel.setFont(Font.font(getFontSize() * 0.7));
        infoLabel.setAlignment(Pos.CENTER);
        infoLabel.setPadding(new Insets(0, 0, 10, 0));

        return infoLabel;
    }

    /**
     * 創建頁面標題
     */
    private Label createPageHeader(EnhancedTextPage page) {
        String headerText = String.format("第 %d 頁%s", page.pageNumber + 1,
                preserveOriginalFormat ? " (格式保持)" : "");

        Label header = new Label(headerText);
        header.setTextFill(getHeaderColor());
        header.setFont(Font.font("Microsoft JhengHei", FontWeight.BOLD, getFontSize() * 1.1));
        header.setAlignment(Pos.CENTER);
        header.setPadding(new Insets(0, 0, 20, 0));

        return header;
    }

    /**
     * 應用搜尋高亮
     */
    private void applySearchHighlight(TextFlow textFlow, String content, String searchTerm) {
        // 這裡需要重新實現搜尋高亮，因為 TextFlow 的內容已經被格式化
        // 暫時保持現有功能
    }

    /**
     * 計算最佳文字寬度
     */
    private double calculateOptimalTextWidth() {
        Region mainContainer = getMainContainerFromParent();
        double containerWidth = mainContainer != null ? mainContainer.getWidth() : 800;

        if (containerWidth <= 0) {
            return 700;
        }

        double availableWidth = containerWidth - 120; // 預留邊距

        if (preserveOriginalFormat && useMonospaceForAlignment) {
            // 格式保持模式下，寬度稍微寬一些以容納對齊內容
            return Math.min(availableWidth, 1000);
        } else {
            return Math.min(availableWidth, 800);
        }
    }

    // 設定方法

    /**
     * 設定是否保持原始格式
     */
    public void setPreserveOriginalFormat(boolean preserve) {
        if (this.preserveOriginalFormat != preserve) {
            this.preserveOriginalFormat = preserve;
            // 重新渲染當前頁面
            refreshCurrentPage();
        }
    }

    /**
     * 設定是否顯示格式指示器
     */
    public void setShowFormatIndicators(boolean show) {
        if (this.showFormatIndicators != show) {
            this.showFormatIndicators = show;
            refreshCurrentPage();
        }
    }

    /**
     * 設定是否高亮縮排
     */
    public void setHighlightIndentation(boolean highlight) {
        if (this.highlightIndentation != highlight) {
            this.highlightIndentation = highlight;
            refreshCurrentPage();
        }
    }

    /**
     * 設定縮排大小
     */
    public void setIndentSize(double size) {
        if (size > 0 && this.indentSize != size) {
            this.indentSize = size;
            refreshCurrentPage();
        }
    }

    /**
     * 設定是否對對齊文字使用等寬字體
     */
    public void setUseMonospaceForAlignment(boolean use) {
        if (this.useMonospaceForAlignment != use) {
            this.useMonospaceForAlignment = use;
            refreshCurrentPage();
        }
    }

    /**
     * 應用主題到格式處理器
     */
    @Override
    public void setThemeColors(SettingsManager.ThemeMode theme) {
        super.setThemeColors(theme);
        if (formatProcessor != null) {
            formatProcessor.applyTheme(theme);
        }
    }

    /**
     * 更新字體大小
     */
    @Override
    public void setFontSize(double size) {
        super.setFontSize(size);
        if (formatProcessor != null) {
            formatProcessor.setFontSize(size);
        }
    }

    /**
     * 更新行距
     */
    @Override
    public void setLineSpacing(double spacing) {
        super.setLineSpacing(spacing);
        if (formatProcessor != null) {
            formatProcessor.setLineHeight(spacing);
        }
    }

    // Getter 方法

    public boolean isPreserveOriginalFormat() {
        return preserveOriginalFormat;
    }

    public boolean isShowFormatIndicators() {
        return showFormatIndicators;
    }

    public boolean isHighlightIndentation() {
        return highlightIndentation;
    }

    public double getIndentSize() {
        return indentSize;
    }

    public boolean isUseMonospaceForAlignment() {
        return useMonospaceForAlignment;
    }

    // 輔助方法 - 從父類獲取必要的元素
    
    /**
     * 獲取頁面容器 - 通過反射從父類獲取
     */
    private VBox getPageContainer() {
        try {
            java.lang.reflect.Field field = TextRenderer.class.getDeclaredField("pageContainer");
            field.setAccessible(true);
            return (VBox) field.get(this);
        } catch (Exception e) {
            // 如果反射失敗，創建新的容器
            System.err.println("無法獲取父類的 pageContainer，創建新容器: " + e.getMessage());
            VBox container = new VBox();
            container.setAlignment(Pos.CENTER);
            container.setPadding(new Insets(20));
            return container;
        }
    }
    
    /**
     * 獲取主容器 - 通過反射從父類獲取
     */
    private Region getMainContainerFromParent() {
        try {
            java.lang.reflect.Field field = TextRenderer.class.getDeclaredField("mainContainer");
            field.setAccessible(true);
            return (Region) field.get(this);
        } catch (Exception e) {
            // 創建預設大小的容器
            VBox container = new VBox();
            container.setPrefWidth(800);
            return container;
        }
    }
    
    /**
     * 獲取滾動面板 - 通過反射從父類獲取
     */
    public ScrollPane getScrollPane() {
        try {
            java.lang.reflect.Field field = TextRenderer.class.getDeclaredField("scrollPane");
            field.setAccessible(true);
            return (ScrollPane) field.get(this);
        } catch (Exception e) {
            // 如果獲取失敗，返回 null
            return null;
        }
    }

    private Color getBackgroundColor() {
        // 根據當前主題返回背景顏色
        try {
            java.lang.reflect.Field field = TextRenderer.class.getDeclaredField("backgroundColor");
            field.setAccessible(true);
            return (Color) field.get(this);
        } catch (Exception e) {
            // 預設深色背景
            return Color.web("#1e1e1e");
        }
    }

    private Color getHeaderColor() {
        // 根據當前主題返回標題顏色
        try {
            java.lang.reflect.Field field = TextRenderer.class.getDeclaredField("headerColor");
            field.setAccessible(true);
            return (Color) field.get(this);
        } catch (Exception e) {
            // 預設白色標題
            return Color.web("#ffffff");
        }
    }

    private String getCurrentSearchTerm() {
        // 從父類獲取當前搜尋詞
        try {
            java.lang.reflect.Field field = TextRenderer.class.getDeclaredField("currentSearchTerm");
            field.setAccessible(true);
            String searchTerm = (String) field.get(this);
            return searchTerm != null ? searchTerm : "";
        } catch (Exception e) {
            return "";
        }
    }
    
    /**
     * 獲取當前頁面索引
     */
    public int getCurrentPageIndex() {
        return super.getCurrentPageIndex();
    }
    
    /**
     * 獲取顯示模式
     */
    public TextRenderer.DisplayMode getDisplayMode() {
        return super.getDisplayMode();
    }

    private void showNoContentMessage() {
        // 顯示無內容消息
        VBox pageContainer = getPageContainer();
        pageContainer.getChildren().clear();
        
        Label noContentLabel = new Label("此頁面無可顯示的文字內容");
        noContentLabel.setTextFill(Color.web("#888888"));
        noContentLabel.setFont(Font.font(getFontSize() * 1.2));
        noContentLabel.setAlignment(Pos.CENTER);
        
        pageContainer.getChildren().add(noContentLabel);
        pageContainer.setAlignment(Pos.CENTER);
    }

    /**
     * 增強版文字頁面類
     */
    private static class EnhancedTextPage {
        int pageNumber;
        TextExtractor.PageText originalPageText;
        List<FormatPreservingTextProcessor.FormattedLine> formattedLines;
        boolean isEmpty = false;
    }

    /**
     * 格式保持模式切換
     */
    public void toggleFormatPreservation() {
        setPreserveOriginalFormat(!preserveOriginalFormat);
    }

    /**
     * 格式指示器切換
     */
    public void toggleFormatIndicators() {
        setShowFormatIndicators(!showFormatIndicators);
    }

    /**
     * 獲取當前頁面的格式統計
     */
    public String getCurrentPageFormatStats() {
        // 返回當前頁面的格式統計信息
        return "格式統計功能需要實現";
    }

    /**
     * 匯出格式化文字
     */
    public String exportFormattedText(boolean includeFormatInfo) {
        // 匯出保持格式的文字
        return "匯出功能需要實現";
    }
}