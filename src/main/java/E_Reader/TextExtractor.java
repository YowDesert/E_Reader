package E_Reader;

import javafx.scene.image.Image;
import javafx.embed.swing.SwingFXUtils;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.pdfbox.rendering.PDFRenderer;
import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class TextExtractor {

    private Tesseract tesseract;
    private boolean ocrInitialized = false;

    private static final Pattern PARAGRAPH_SEPARATOR = Pattern.compile("\n\\s*\n+");
    private static final Pattern LINE_BREAK = Pattern.compile("\n");
    private static final Pattern SENTENCE_END = Pattern.compile("[。！？.!?]\\s*");
    private static final Pattern CHAPTER_HEADER = Pattern.compile("^(第[一二三四五六七八九十0-9]+[章節回部]|Chapter\\s+\\d+|CHAPTER\\s+\\d+)");

    public TextExtractor() {
        initializeOCR();
    }

    private void initializeOCR() {
        try {
            tesseract = new Tesseract();
            tesseract.setDatapath("tessdata");
            tesseract.setLanguage("chi_tra+eng");
            tesseract.setPageSegMode(1);
            tesseract.setOcrEngineMode(1);
            tesseract.setVariable("tessedit_char_whitelist",
                    "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz" +
                            "一二三四五六七八九十百千萬億" +
                            "的是了不在有個人這上我大來以他時要說年生對中可你國也就到下所" +
                            "會小而能用於自己出道走子它種只多如面方前回什公司問題工作生活" +
                            "好了在是的有不了就是這個一是不的有了就這個一是不的有了");

            ocrInitialized = true;
        } catch (Exception e) {
            System.err.println("OCR 初始化失敗: " + e.getMessage());
            ocrInitialized = false;
        }
    }

    public List<PageText> extractTextFromPdf(File pdfFile) throws IOException {
        List<PageText> pages = new ArrayList<>();

        try (PDDocument document = PDDocument.load(pdfFile)) {
            PDFTextStripper stripper = new PDFTextStripper();
            PDFRenderer renderer = new PDFRenderer(document);

            for (int i = 0; i < document.getNumberOfPages(); i++) {
                stripper.setStartPage(i + 1);
                stripper.setEndPage(i + 1);
                String extractedText = stripper.getText(document).trim();

                PageText pageText = new PageText();
                pageText.setPageNumber(i);
                pageText.setOriginalText(cleanExtractedText(extractedText));

                if (extractedText.length() < 50 && ocrInitialized) {
                    try {
                        BufferedImage pageImage = renderer.renderImageWithDPI(i, 300);
                        String ocrText = tesseract.doOCR(pageImage);
                        pageText.setOcrText(cleanOCRText(ocrText));
                        pageText.setTextSource(TextSource.OCR);
                    } catch (TesseractException e) {
                        System.err.println("OCR 處理第 " + (i + 1) + " 頁失敗: " + e.getMessage());
                        pageText.setTextSource(TextSource.NATIVE);
                    }
                } else {
                    pageText.setTextSource(TextSource.NATIVE);
                }

                pages.add(pageText);
            }
        }

        return pages;
    }

    public List<PageText> extractTextFromImages(List<Image> images) {
        List<PageText> pages = new ArrayList<>();

        if (!ocrInitialized) {
            System.err.println("OCR 未初始化，無法從圖片提取文字");
            return pages;
        }

        for (int i = 0; i < images.size(); i++) {
            PageText pageText = new PageText();
            pageText.setPageNumber(i);
            pageText.setTextSource(TextSource.OCR);

            try {
                BufferedImage bufferedImage = SwingFXUtils.fromFXImage(images.get(i), null);
                BufferedImage processedImage = preprocessImageForOCR(bufferedImage);
                String ocrText = tesseract.doOCR(processedImage);
                pageText.setOcrText(cleanOCRText(ocrText));
            } catch (TesseractException e) {
                System.err.println("OCR 處理第 " + (i + 1) + " 頁圖片失敗: " + e.getMessage());
                pageText.setOcrText("");
            }

            pages.add(pageText);
        }

        return pages;
    }

    private BufferedImage preprocessImageForOCR(BufferedImage originalImage) {
        return originalImage;
    }

    private String cleanOCRText(String ocrText) {
        if (ocrText == null || ocrText.trim().isEmpty()) return "";

        String cleaned = ocrText;
        cleaned = cleaned.replaceAll("[|]", "");
        cleaned = cleaned.replaceAll("\\s{3,}", "\n\n");
        cleaned = cleaned.replaceAll("(?m)^\\s+", "");
        cleaned = cleaned.replaceAll("(?m)\\s+$", "");
        cleaned = cleaned.replaceAll("\n{3,}", "\n\n");
        cleaned = cleaned.replaceAll("0", "O");
        cleaned = cleaned.replaceAll("丨", "1");

        return cleaned.trim();
    }

    private String cleanExtractedText(String text) {
        if (text == null || text.trim().isEmpty()) return "";

        String cleaned = text;
        cleaned = cleaned.replaceAll("\r\n", "\n");
        cleaned = cleaned.replaceAll("\r", "\n");
        cleaned = cleaned.replaceAll("[ \t]+", " ");
        cleaned = cleaned.replaceAll("(?m)^\\s+", "");
        cleaned = cleaned.replaceAll("(?m)\\s+$", "");

        return cleaned.trim();
    }

    public String extractTextFromImage(Image image) {
        if (!ocrInitialized) return "";

        try {
            BufferedImage bufferedImage = SwingFXUtils.fromFXImage(image, null);
            BufferedImage processedImage = preprocessImageForOCR(bufferedImage);
            String ocrText = tesseract.doOCR(processedImage);
            return cleanOCRText(ocrText);
        } catch (TesseractException e) {
            System.err.println("OCR 處理圖片失敗: " + e.getMessage());
            return "";
        }
    }

    public boolean isOcrAvailable() {
        return ocrInitialized;
    }

    public static class PageText {
        private int pageNumber;
        private String originalText = "";
        private String ocrText = "";
        private TextSource textSource;
        private List<TextBlock> textBlocks = new ArrayList<>();

        public int getPageNumber() { return pageNumber; }
        public void setPageNumber(int pageNumber) { this.pageNumber = pageNumber; }

        public String getOriginalText() { return originalText; }
        public void setOriginalText(String originalText) { this.originalText = originalText; }

        public String getOcrText() { return ocrText; }
        public void setOcrText(String ocrText) { this.ocrText = ocrText; }

        public TextSource getTextSource() { return textSource; }
        public void setTextSource(TextSource textSource) { this.textSource = textSource; }

        public List<TextBlock> getTextBlocks() { return textBlocks; }
        public void setTextBlocks(List<TextBlock> textBlocks) { this.textBlocks = textBlocks; }

        public String getBestText() {
            switch (textSource) {
                case NATIVE:
                    return originalText.isEmpty() ? ocrText : originalText;
                case OCR:
                    return ocrText.isEmpty() ? originalText : ocrText;
                default:
                    return (ocrText != null && !ocrText.isEmpty()) ? ocrText : originalText;
            }
        }

        // 在TextExtractor.java的PageText类中修改getFormattedParagraphs方法

        public List<String> getFormattedParagraphs() {
            String text = getBestText();
            if (text.isEmpty()) return new ArrayList<>();
            // 修改：保持原有换行格式
            return formatTextWithOriginalLineBreaks(text);
        }

        private List<String> formatTextWithOriginalLineBreaks(String text) {
            List<String> lines = new ArrayList<>();

            // 将文本按行分割，保持原有的换行
            String[] originalLines = text.split("\n");

            for (String line : originalLines) {
                line = line.trim();

                // 跳过空行，但保留一个空行作为段落分隔
                if (line.isEmpty()) {
                    if (!lines.isEmpty() && !lines.get(lines.size() - 1).isEmpty()) {
                        lines.add(""); // 添加空行作为段落分隔
                    }
                    continue;
                }

                // 检查是否是章节标题
                if (isChapterHeader(line)) {
                    // 章节标题前后添加空行分隔
                    if (!lines.isEmpty() && !lines.get(lines.size() - 1).isEmpty()) {
                        lines.add("");
                    }
                    lines.add(line);
                    lines.add("");
                    continue;
                }

                // 清理行内容但保持换行结构
                String cleanedLine = cleanLineContent(line);
                if (!cleanedLine.isEmpty()) {
                    lines.add(cleanedLine);
                }
            }

            // 移除开头和结尾的空行
            while (!lines.isEmpty() && lines.get(0).isEmpty()) {
                lines.remove(0);
            }
            while (!lines.isEmpty() && lines.get(lines.size() - 1).isEmpty()) {
                lines.remove(lines.size() - 1);
            }

            return lines;
        }

        // 辅助方法：清理行内容
        private String cleanLineContent(String line) {
            if (line == null) return "";

            // 移除行首行尾的空白字符
            line = line.trim();

            // 清理多余的空格
            line = line.replaceAll("\\s+", " ");

            // 移除一些OCR可能产生的错误字符
            line = line.replaceAll("[|]", "");
            line = line.replaceAll("0", "O"); // 将数字0替换为字母O（如果需要）
            line = line.replaceAll("丨", "1"); // 将竖线替换为数字1

            return line;
        }

        private List<String> formatTextIntoParagraphs(String text) {
            List<String> paragraphs = new ArrayList<>();
            String[] majorSections = PARAGRAPH_SEPARATOR.split(text);

            for (String section : majorSections) {
                section = section.trim();
                if (section.isEmpty()) continue;
                if (isChapterHeader(section)) {
                    paragraphs.add(section);
                    continue;
                }

                List<String> subParagraphs = processSectionIntoParagraphs(section);
                paragraphs.addAll(subParagraphs);
            }

            if (paragraphs.isEmpty()) {
                paragraphs.addAll(fallbackParagraphSplit(text));
            }

            return cleanParagraphs(paragraphs);
        }

        private boolean isChapterHeader(String text) {
            if (text.length() > 100) return false;
            return CHAPTER_HEADER.matcher(text.trim()).find();
        }

        private List<String> processSectionIntoParagraphs(String section) {
            List<String> result = new ArrayList<>();
            String[] lines = LINE_BREAK.split(section);
            StringBuilder currentParagraph = new StringBuilder();

            for (String line : lines) {
                line = line.trim();
                if (line.isEmpty()) {
                    if (currentParagraph.length() > 0) {
                        result.add(currentParagraph.toString().trim());
                        currentParagraph = new StringBuilder();
                    }
                    continue;
                }

                if (shouldStartNewParagraph(line, currentParagraph.toString())) {
                    if (currentParagraph.length() > 0) {
                        result.add(currentParagraph.toString().trim());
                        currentParagraph = new StringBuilder();
                    }
                }

                if (currentParagraph.length() > 0 && needsSpaceBetweenLines(currentParagraph.toString(), line)) {
                    currentParagraph.append(" ");
                }

                currentParagraph.append(line);
            }

            if (currentParagraph.length() > 0) {
                result.add(currentParagraph.toString().trim());
            }

            return result;
        }

        private boolean shouldStartNewParagraph(String line, String currentParagraph) {
            if (currentParagraph.isEmpty()) return false;
            if (isChapterHeader(line)) return true;
            if (line.startsWith("「") || line.startsWith("『") || line.startsWith("“") || line.startsWith("\"")) return true;

            if (currentParagraph.endsWith("。") || currentParagraph.endsWith("！") || currentParagraph.endsWith("？") ||
                    currentParagraph.endsWith(".") || currentParagraph.endsWith("!") || currentParagraph.endsWith("?")) {
                if (!line.matches("^[而且但是因此所以然而不過].*")) {
                    return true;
                }
            }

            return false;
        }

        private boolean needsSpaceBetweenLines(String currentText, String newLine) {
            if (currentText.isEmpty() || newLine.isEmpty()) return false;
            char lastChar = currentText.charAt(currentText.length() - 1);
            char firstChar = newLine.charAt(0);

            if ("，。！？；：、".indexOf(lastChar) != -1) return false;
            if ("，。！？；：、」』“".indexOf(firstChar) != -1) return false;
            if (isChinese(lastChar) && isChinese(firstChar)) return false;

            return Character.isLetterOrDigit(lastChar) && Character.isLetterOrDigit(firstChar);
        }

        private boolean isChinese(char c) {
            return c >= 0x4e00 && c <= 0x9fff;
        }

        private List<String> fallbackParagraphSplit(String text) {
            List<String> paragraphs = new ArrayList<>();
            String[] sentences = SENTENCE_END.split(text);
            StringBuilder currentParagraph = new StringBuilder();

            for (String sentence : sentences) {
                sentence = sentence.trim();
                if (sentence.isEmpty()) continue;

                if (currentParagraph.length() > 0) {
                    currentParagraph.append("。");
                }

                currentParagraph.append(sentence);

                if (currentParagraph.length() > 100 && currentParagraph.length() < 300) {
                    paragraphs.add(currentParagraph.toString().trim());
                    currentParagraph = new StringBuilder();
                }
            }

            if (currentParagraph.length() > 0) {
                paragraphs.add(currentParagraph.toString().trim());
            }

            return paragraphs;
        }

        private List<String> cleanParagraphs(List<String> paragraphs) {
            List<String> cleaned = new ArrayList<>();

            for (String paragraph : paragraphs) {
                paragraph = paragraph.trim();
                if (paragraph.isEmpty()) continue;
                if (paragraph.length() < 5) continue;

                if (paragraph.length() < 30 && !cleaned.isEmpty() && !isChapterHeader(paragraph)) {
                    String last = cleaned.get(cleaned.size() - 1);
                    cleaned.set(cleaned.size() - 1, last + " " + paragraph);
                } else {
                    cleaned.add(paragraph);
                }
            }

            return cleaned;
        }
    }

    public static class TextBlock {
        private double x, y, width, height;
        private String text;
        private double confidence;

        public TextBlock(double x, double y, double width, double height, String text) {
            this(x, y, width, height, text, 0.0);
        }

        public TextBlock(double x, double y, double width, double height, String text, double confidence) {
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
            this.text = text;
            this.confidence = confidence;
        }

        public double getX() { return x; }
        public void setX(double x) { this.x = x; }

        public double getY() { return y; }
        public void setY(double y) { this.y = y; }

        public double getWidth() { return width; }
        public void setWidth(double width) { this.width = width; }

        public double getHeight() { return height; }
        public void setHeight(double height) { this.height = height; }

        public String getText() { return text; }
        public void setText(String text) { this.text = text; }

        public double getConfidence() { return confidence; }
        public void setConfidence(double confidence) { this.confidence = confidence; }
    }

    public enum TextSource {
        NATIVE("原生文字"),
        OCR("OCR識別"),
        MIXED("混合模式");

        private final String displayName;

        TextSource(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }
}
