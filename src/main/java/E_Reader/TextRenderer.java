package E_Reader;

import javafx.scene.control.ScrollPane;
import javafx.scene.layout.VBox;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.scene.paint.Color;
import javafx.scene.control.Label;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.application.Platform;

import java.util.List;
import java.util.ArrayList;

public class TextRenderer {

    private StackPane mainContainer;
    private ScrollPane scrollPane;
    private VBox pageContainer;
    private List<TextExtractor.PageText> originalPages;
    private List<TextPage> formattedPages;
    private int currentPageIndex = 0;

    // 樣式設定
    private Color backgroundColor = Color.web("#1e1e1e");
    private Color textColor = Color.web("#e0e0e0");
    private Color headerColor = Color.web("#ffffff");
    private Color highlightColor = Color.web("#ffff00");
    private Color highlightTextColor = Color.web("#000000");

    private double baseFontSize = 18.0;
    private Font textFont;
    private Font headerFont;
    private double lineSpacing = 1.8;
    private double paragraphSpacing = 24;
    private double pageMarginHorizontal = 60;
    private double pageMarginVertical = 40;

    // 搜尋相關
    private String currentSearchTerm = "";

    // 版面設定
    private DisplayMode displayMode = DisplayMode.PAGE_BY_PAGE;
    private DeviceOrientation orientation = DeviceOrientation.PORTRAIT;
    private DeviceType deviceType = DeviceType.DESKTOP;

    public enum DisplayMode {
        PAGE_BY_PAGE,    // 一頁一頁顯示
        CONTINUOUS,      // 連續滾動
        TWO_PAGE         // 雙頁顯示（橫式時）
    }

    public enum DeviceOrientation {
        PORTRAIT,        // 直式
        LANDSCAPE        // 橫式
    }

    public enum DeviceType {
        DESKTOP,         // 桌面電腦
        TABLET           // 平板
    }

    public TextRenderer() {
        initializeFonts();
        initializeComponents();
        setupResponsiveLayout();
    }

    private void initializeFonts() {
        try {
            textFont = Font.font("Microsoft JhengHei", baseFontSize);
            headerFont = Font.font("Microsoft JhengHei", FontWeight.BOLD, baseFontSize * 1.2);
        } catch (Exception e) {
            // 如果指定字體不可用，使用系統預設字體
            textFont = Font.font("System", baseFontSize);
            headerFont = Font.font("System", FontWeight.BOLD, baseFontSize * 1.2);
        }
    }

    private void initializeComponents() {
        // 主容器
        mainContainer = new StackPane();
        mainContainer.setAlignment(Pos.CENTER);

        // 頁面容器
        pageContainer = new VBox();
        pageContainer.setAlignment(Pos.CENTER);
        pageContainer.setSpacing(0);

        // 滾動面板
        scrollPane = new ScrollPane();
        scrollPane.setContent(pageContainer);
        scrollPane.setFitToWidth(true);
        scrollPane.setFitToHeight(false);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);

        // 添加到主容器
        mainContainer.getChildren().add(scrollPane);

        applyTheme();
    }

    private void setupResponsiveLayout() {
        // 監聽容器尺寸變化
        mainContainer.widthProperty().addListener((obs, oldWidth, newWidth) -> {
            if (newWidth.doubleValue() > 0) {
                Platform.runLater(() -> updateLayoutForSize());
            }
        });

        mainContainer.heightProperty().addListener((obs, oldHeight, newHeight) -> {
            if (newHeight.doubleValue() > 0) {
                Platform.runLater(() -> updateLayoutForSize());
            }
        });
    }

    private void updateLayoutForSize() {
        double width = mainContainer.getWidth();
        double height = mainContainer.getHeight();

        if (width <= 0 || height <= 0) return;

        // 判斷設備類型和方向
        if (width < 800) {
            deviceType = DeviceType.TABLET;
        } else {
            deviceType = DeviceType.DESKTOP;
        }

        if (width > height) {
            orientation = DeviceOrientation.LANDSCAPE;
        } else {
            orientation = DeviceOrientation.PORTRAIT;
        }

        // 調整版面設定
        adjustLayoutSettings();

        // 重新格式化頁面
        if (originalPages != null && !originalPages.isEmpty()) {
            formatPages();
            renderCurrentPage();
        }
    }

    private void adjustLayoutSettings() {
        switch (deviceType) {
            case TABLET:
                if (orientation == DeviceOrientation.PORTRAIT) {
                    baseFontSize = 20.0;
                    pageMarginHorizontal = 40;
                    pageMarginVertical = 30;
                    lineSpacing = 2.0;
                    paragraphSpacing = 28;
                } else {
                    baseFontSize = 18.0;
                    pageMarginHorizontal = 80;
                    pageMarginVertical = 40;
                    lineSpacing = 1.8;
                    paragraphSpacing = 24;
                    displayMode = DisplayMode.TWO_PAGE;
                }
                break;

            case DESKTOP:
                if (orientation == DeviceOrientation.PORTRAIT) {
                    baseFontSize = 18.0;
                    pageMarginHorizontal = 80;
                    pageMarginVertical = 50;
                    lineSpacing = 1.8;
                    paragraphSpacing = 26;
                } else {
                    baseFontSize = 16.0;
                    pageMarginHorizontal = 120;
                    pageMarginVertical = 60;
                    lineSpacing = 1.6;
                    paragraphSpacing = 22;
                }
                break;
        }

        updateFonts();
    }

    private void updateFonts() {
        try {
            textFont = Font.font("Microsoft JhengHei", baseFontSize);
            headerFont = Font.font("Microsoft JhengHei", FontWeight.BOLD, baseFontSize * 1.2);
        } catch (Exception e) {
            textFont = Font.font("System", baseFontSize);
            headerFont = Font.font("System", FontWeight.BOLD, baseFontSize * 1.2);
        }
    }

    public void setPages(List<TextExtractor.PageText> pages) {
        this.originalPages = pages;
        if (pages != null && !pages.isEmpty()) {
            formatPages();
            currentPageIndex = 0;
            renderCurrentPage();
        } else {
            showNoContentMessage();
        }
    }

    private void formatPages() {
        if (originalPages == null || originalPages.isEmpty()) {
            return;
        }

        formattedPages = new ArrayList<>();

        for (TextExtractor.PageText originalPage : originalPages) {
            List<String> paragraphs = originalPage.getFormattedParagraphs();

            if (paragraphs.isEmpty()) {
                TextPage emptyPage = new TextPage();
                emptyPage.pageNumber = originalPage.getPageNumber();
                emptyPage.paragraphs = new ArrayList<>();
                emptyPage.isEmpty = true;
                formattedPages.add(emptyPage);
                continue;
            }

            TextPage currentTextPage = new TextPage();
            currentTextPage.pageNumber = originalPage.getPageNumber();
            currentTextPage.originalPageText = originalPage;
            currentTextPage.paragraphs = new ArrayList<>();

            for (String paragraph : paragraphs) {
                if (!paragraph.trim().isEmpty()) {
                    currentTextPage.paragraphs.add(paragraph.trim());
                }
            }

            formattedPages.add(currentTextPage);
        }
    }

    private void renderCurrentPage() {
        if (formattedPages == null || formattedPages.isEmpty()) {
            showNoContentMessage();
            return;
        }

        pageContainer.getChildren().clear();

        if (displayMode == DisplayMode.PAGE_BY_PAGE) {
            renderSinglePage();
        } else if (displayMode == DisplayMode.TWO_PAGE && orientation == DeviceOrientation.LANDSCAPE) {
            renderTwoPages();
        } else {
            renderContinuousPages();
        }
    }

    private void renderSinglePage() {
        if (currentPageIndex < 0 || currentPageIndex >= formattedPages.size()) {
            return;
        }

        TextPage page = formattedPages.get(currentPageIndex);
        VBox pageView = createPageView(page);

        pageContainer.getChildren().clear();
        pageContainer.getChildren().add(pageView);
        pageContainer.setAlignment(Pos.CENTER);

        Platform.runLater(() -> scrollPane.setVvalue(0));
    }

    private void renderTwoPages() {
        pageContainer.getChildren().clear();

        javafx.scene.layout.HBox twoPageContainer = new javafx.scene.layout.HBox();
        twoPageContainer.setAlignment(Pos.CENTER);
        twoPageContainer.setSpacing(40);
        twoPageContainer.setPadding(new Insets(pageMarginVertical, pageMarginHorizontal, pageMarginVertical, pageMarginHorizontal));

        if (currentPageIndex < formattedPages.size()) {
            TextPage leftPage = formattedPages.get(currentPageIndex);
            VBox leftPageView = createPageView(leftPage);
            leftPageView.setPrefWidth(mainContainer.getWidth() / 2 - 100);
            twoPageContainer.getChildren().add(leftPageView);
        }

        if (currentPageIndex + 1 < formattedPages.size()) {
            TextPage rightPage = formattedPages.get(currentPageIndex + 1);
            VBox rightPageView = createPageView(rightPage);
            rightPageView.setPrefWidth(mainContainer.getWidth() / 2 - 100);
            twoPageContainer.getChildren().add(rightPageView);
        }

        pageContainer.getChildren().add(twoPageContainer);
        Platform.runLater(() -> scrollPane.setVvalue(0));
    }

    private void renderContinuousPages() {
        pageContainer.getChildren().clear();

        for (TextPage page : formattedPages) {
            VBox pageView = createPageView(page);
            pageView.setStyle(pageView.getStyle() + "; -fx-border-color: #444444; -fx-border-width: 0 0 2 0; -fx-border-style: dashed;");
            pageContainer.getChildren().add(pageView);

            if (formattedPages.indexOf(page) < formattedPages.size() - 1) {
                Label spacer = new Label();
                spacer.setPrefHeight(40);
                pageContainer.getChildren().add(spacer);
            }
        }
    }

    private VBox createPageView(TextPage page) {
        VBox pageView = new VBox();
        pageView.setAlignment(Pos.TOP_CENTER);
        pageView.setPadding(new Insets(pageMarginVertical, pageMarginHorizontal, pageMarginVertical, pageMarginHorizontal));
        pageView.setMaxWidth(calculateOptimalPageWidth());

        BackgroundFill backgroundFill = new BackgroundFill(backgroundColor, new CornerRadii(10), Insets.EMPTY);
        pageView.setBackground(new Background(backgroundFill));

        Label pageHeader = createPageHeader(page);
        pageView.getChildren().add(pageHeader);

        if (page.isEmpty || page.paragraphs.isEmpty()) {
            Label noTextLabel = new Label("本頁無文字內容");
            noTextLabel.setTextFill(Color.web("#888888"));
            noTextLabel.setFont(Font.font(textFont.getFamily(), baseFontSize * 0.875));
            noTextLabel.setAlignment(Pos.CENTER);
            pageView.getChildren().add(noTextLabel);
            return pageView;
        }

        // 修改：为每一行创建单独的文本元素
        for (String line : page.paragraphs) {
            if (line.isEmpty()) {
                // 空行作为段落分隔
                Label spacer = new Label();
                spacer.setPrefHeight(paragraphSpacing * 0.5);
                pageView.getChildren().add(spacer);
            } else if (isChapterHeader(line)) {
                // 章节标题
                Label chapterLabel = createChapterLabel(line);
                pageView.getChildren().add(chapterLabel);
            } else {
                // 普通文本行
                TextFlow lineFlow = createLineTextFlow(line);
                pageView.getChildren().add(lineFlow);
            }
        }

        return pageView;
    }

    private Label createChapterLabel(String chapterText) {
        Label chapterLabel = new Label(chapterText);
        chapterLabel.setTextFill(headerColor);
        chapterLabel.setFont(Font.font(headerFont.getFamily(), FontWeight.BOLD, baseFontSize * 1.3));
        chapterLabel.setAlignment(Pos.CENTER);
        chapterLabel.setPadding(new Insets(paragraphSpacing, 0, paragraphSpacing, 0));
        chapterLabel.setStyle("-fx-text-alignment: center;");
        chapterLabel.setWrapText(true);
        return chapterLabel;
    }

    private double calculateOptimalPageWidth() {
        double containerWidth = mainContainer.getWidth();

        if (containerWidth <= 0) {
            return deviceType == DeviceType.TABLET ? 600 : 800;
        }

        switch (deviceType) {
            case TABLET:
                if (orientation == DeviceOrientation.PORTRAIT) {
                    return Math.min(containerWidth - 80, 700);
                } else {
                    return Math.min(containerWidth - 160, 900);
                }
            case DESKTOP:
                if (orientation == DeviceOrientation.PORTRAIT) {
                    return Math.min(containerWidth - 160, 900);
                } else {
                    return Math.min(containerWidth - 240, 1200);
                }
            default:
                return 800;
        }
    }

    private Label createPageHeader(TextPage page) {
        String headerText;
        if (page.originalPageText != null) {
            headerText = String.format("第 %d 頁", page.pageNumber + 1);
        } else {
            headerText = String.format("頁面 %d", page.pageNumber + 1);
        }

        Label header = new Label(headerText);
        header.setTextFill(headerColor);
        header.setFont(headerFont);
        header.setAlignment(Pos.CENTER);
        header.setPadding(new Insets(0, 0, paragraphSpacing, 0));

        return header;
    }

    private TextFlow createLineTextFlow(String line) {
        TextFlow textFlow = new TextFlow();
        textFlow.setLineSpacing(lineSpacing);
        textFlow.setPadding(new Insets(lineSpacing * 2, 0, 0, 0)); // 行间距
        textFlow.setMaxWidth(Double.MAX_VALUE);

        if (currentSearchTerm.isEmpty()) {
            Text text = new Text(line);
            text.setFill(textColor);
            text.setFont(textFont);
            textFlow.getChildren().add(text);
        } else {
            addHighlightedText(textFlow, line, currentSearchTerm);
        }

        return textFlow;
    }

    private boolean isChapterHeader(String text) {
        if (text == null || text.trim().isEmpty()) return false;
        if (text.length() > 100) return false;

        String trimmedText = text.trim();

        // 检测中文章节标题
        if (trimmedText.matches("^第[一二三四五六七八九十0-9]+[章節回部].*")) {
            return true;
        }

        // 检测英文章节标题
        if (trimmedText.matches("^(Chapter|CHAPTER)\\s+\\d+.*")) {
            return true;
        }

        // 检测其他可能的标题格式
        if (trimmedText.matches("^[0-9]+[、．].*") && trimmedText.length() < 50) {
            return true;
        }

        return false;
    }

    private void showNoContentMessage() {
        pageContainer.getChildren().clear();

        VBox messageContainer = new VBox();
        messageContainer.setAlignment(Pos.CENTER);
        messageContainer.setPadding(new Insets(100));

        Label messageLabel = new Label("尚未載入任何文件\n請選擇PDF檔案或圖片資料夾，然後切換到文字模式");
        messageLabel.setTextFill(Color.web("#888888"));
        messageLabel.setFont(Font.font(textFont.getFamily(), baseFontSize * 1.125));
        messageLabel.setStyle("-fx-text-alignment: center;");
        messageLabel.setWrapText(true);

        messageContainer.getChildren().add(messageLabel);
        pageContainer.getChildren().add(messageContainer);
    }

    public void goToPage(int pageIndex) {
        if (formattedPages == null || pageIndex < 0 || pageIndex >= formattedPages.size()) {
            return;
        }

        currentPageIndex = pageIndex;
        renderCurrentPage();
    }

    public void nextPage() {
        if (displayMode == DisplayMode.TWO_PAGE && orientation == DeviceOrientation.LANDSCAPE) {
            if (currentPageIndex + 2 < formattedPages.size()) {
                currentPageIndex += 2;
                renderCurrentPage();
            }
        } else {
            if (currentPageIndex + 1 < formattedPages.size()) {
                currentPageIndex++;
                renderCurrentPage();
            }
        }
    }

    public void previousPage() {
        if (displayMode == DisplayMode.TWO_PAGE && orientation == DeviceOrientation.LANDSCAPE) {
            if (currentPageIndex - 2 >= 0) {
                currentPageIndex -= 2;
                renderCurrentPage();
            }
        } else {
            if (currentPageIndex > 0) {
                currentPageIndex--;
                renderCurrentPage();
            }
        }
    }

    public void applyTheme() {
        BackgroundFill backgroundFill = new BackgroundFill(backgroundColor, CornerRadii.EMPTY, Insets.EMPTY);
        Background background = new Background(backgroundFill);
        mainContainer.setBackground(background);

        scrollPane.setStyle(String.format("-fx-background: %s; -fx-background-color: %s;",
                toHexString(backgroundColor), toHexString(backgroundColor)));
    }

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

        if (formattedPages != null && !formattedPages.isEmpty()) {
            renderCurrentPage();
        }
    }

    public void searchText(String searchTerm) {
        if (originalPages == null || searchTerm == null) {
            return;
        }

        currentSearchTerm = searchTerm.trim();
        renderCurrentPage();

        if (!currentSearchTerm.isEmpty()) {
            scrollToFirstMatch();
        }
    }

    private void scrollToFirstMatch() {
        if (currentSearchTerm.isEmpty() || formattedPages == null) {
            return;
        }

        String lowerSearchTerm = currentSearchTerm.toLowerCase();

        for (int i = 0; i < formattedPages.size(); i++) {
            TextPage page = formattedPages.get(i);
            for (String paragraph : page.paragraphs) {
                if (paragraph.toLowerCase().contains(lowerSearchTerm)) {
                    goToPage(i);
                    return;
                }
            }
        }
    }

    private void addHighlightedText(TextFlow textFlow, String paragraph, String searchTerm) {
        String lowerParagraph = paragraph.toLowerCase();
        String lowerSearchTerm = searchTerm.toLowerCase();

        int lastIndex = 0;
        int index = lowerParagraph.indexOf(lowerSearchTerm, lastIndex);

        while (index != -1) {
            if (index > lastIndex) {
                Text beforeText = new Text(paragraph.substring(lastIndex, index));
                beforeText.setFill(textColor);
                beforeText.setFont(textFont);
                textFlow.getChildren().add(beforeText);
            }

            Text highlightText = new Text(paragraph.substring(index, index + searchTerm.length()));
            highlightText.setFill(highlightTextColor);
            highlightText.setFont(textFont);
            highlightText.setStyle(String.format("-fx-background-color: %s;", toHexString(highlightColor)));
            textFlow.getChildren().add(highlightText);

            lastIndex = index + searchTerm.length();
            index = lowerParagraph.indexOf(lowerSearchTerm, lastIndex);
        }

        if (lastIndex < paragraph.length()) {
            Text remainingText = new Text(paragraph.substring(lastIndex));
            remainingText.setFill(textColor);
            remainingText.setFont(textFont);
            textFlow.getChildren().add(remainingText);
        }
    }

    private String toHexString(Color color) {
        return String.format("#%02X%02X%02X",
                (int) (color.getRed() * 255),
                (int) (color.getGreen() * 255),
                (int) (color.getBlue() * 255));
    }

    // Getter 方法
    public StackPane getMainContainer() {
        return mainContainer;
    }

    public ScrollPane getScrollPane() {
        return scrollPane;
    }

    public int getCurrentPageIndex() {
        return currentPageIndex;
    }

    public int getTotalPages() {
        return formattedPages != null ? formattedPages.size() : 0;
    }

    public boolean hasContent() {
        return formattedPages != null && !formattedPages.isEmpty();
    }

    public double getFontSize() {
        return baseFontSize;
    }

    public DisplayMode getDisplayMode() {
        return displayMode;
    }

    public void setDisplayMode(DisplayMode displayMode) {
        this.displayMode = displayMode;
        if (formattedPages != null && !formattedPages.isEmpty()) {
            renderCurrentPage();
        }
    }

    public DeviceOrientation getOrientation() {
        return orientation;
    }

    public DeviceType getDeviceType() {
        return deviceType;
    }

    public void setLineSpacing(double spacing) {
        if (spacing < 0.5) spacing = 0.5;
        if (spacing > 5.0) spacing = 5.0;

        this.lineSpacing = spacing;
        if (formattedPages != null && !formattedPages.isEmpty()) {
            renderCurrentPage();
        }
    }

    public void setFontSize(double size) {
        if (size < 8) size = 8;
        if (size > 72) size = 72;

        baseFontSize = size;
        updateFonts();

        if (formattedPages != null && !formattedPages.isEmpty()) {
            renderCurrentPage();
        }
    }

    private static class TextPage {
        int pageNumber;
        List<String> paragraphs;
        TextExtractor.PageText originalPageText;
        boolean isEmpty = false;

        TextPage() {
            paragraphs = new ArrayList<>();
        }
    }
}