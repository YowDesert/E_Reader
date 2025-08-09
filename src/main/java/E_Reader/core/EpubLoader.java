package E_Reader.core;

import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.imageio.ImageIO;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * EPUB載入器 - 負責讀取和解析EPUB電子書檔案
 */
public class EpubLoader {

    // EPUB檔案結構常數
    private static final String MIMETYPE_FILE = "mimetype";
    private static final String CONTAINER_XML = "META-INF/container.xml";
    private static final String EPUB_MIMETYPE = "application/epub+zip";

    // 文字渲染設定
    private static final int DEFAULT_CHAPTER_WIDTH = 800;
    private static final int DEFAULT_CHAPTER_HEIGHT = 1000;
    private static final Font DEFAULT_FONT = new Font("Microsoft JhengHei", Font.PLAIN, 18);
    private static final int LINE_HEIGHT = 28;
    private static final int MARGIN = 40;

    // HTML標籤清理的正規表達式
    private static final Pattern HTML_TAG_PATTERN = Pattern.compile("<[^>]+>");
    private static final Pattern WHITESPACE_PATTERN = Pattern.compile("\\s+");
    private static final Pattern PARAGRAPH_PATTERN = Pattern.compile("<p[^>]*>(.*?)</p>", Pattern.DOTALL | Pattern.CASE_INSENSITIVE);
    private static final Pattern CHAPTER_TITLE_PATTERN = Pattern.compile("<h[1-6][^>]*>(.*?)</h[1-6]>", Pattern.DOTALL | Pattern.CASE_INSENSITIVE);

    /**
     * 從EPUB檔案載入所有章節為圖片
     *
     * @param epubFile EPUB檔案
     * @return 圖片列表（每個圖片代表一個章節或頁面）
     * @throws IOException 如果無法讀取EPUB檔案
     */
    public List<Image> loadImagesFromEpub(File epubFile) throws IOException {
        List<Image> images = new ArrayList<>();

        if (epubFile == null || !epubFile.exists() || !epubFile.isFile()) {
            throw new IOException("EPUB檔案不存在或無法讀取: " + epubFile);
        }

        // 驗證EPUB檔案
        if (!isValidEpubFile(epubFile)) {
            throw new IOException("不是有效的EPUB檔案: " + epubFile.getName());
        }

        try (ZipFile zipFile = new ZipFile(epubFile)) {
            // 解析EPUB結構
            EpubStructure structure = parseEpubStructure(zipFile);

            // 提取章節內容
            List<Chapter> chapters = extractChapters(zipFile, structure);

            // 將每個章節轉換為圖片
            for (Chapter chapter : chapters) {
                try {
                    BufferedImage chapterImage = renderChapterToImage(chapter);
                    if (chapterImage != null) {
                        Image fxImage = SwingFXUtils.toFXImage(chapterImage, null);
                        if (!fxImage.isError()) {
                            images.add(fxImage);
                        }
                    }
                } catch (Exception e) {
                    System.err.println("無法渲染章節: " + chapter.getTitle() + " - " + e.getMessage());
                    // 創建錯誤頁面
                    BufferedImage errorImage = createErrorImage("無法載入章節: " + chapter.getTitle());
                    images.add(SwingFXUtils.toFXImage(errorImage, null));
                }
            }

        } catch (Exception e) {
            throw new IOException("解析EPUB檔案時發生錯誤: " + e.getMessage(), e);
        }

        if (images.isEmpty()) {
            throw new IOException("EPUB檔案中沒有可讀取的內容");
        }

        return images;
    }

    /**
     * 從EPUB檔案提取文字內容
     *
     * @param epubFile EPUB檔案
     * @return 章節文字列表
     * @throws IOException 如果無法讀取EPUB檔案
     */
    public List<TextExtractor.PageText> extractTextFromEpub(File epubFile) throws IOException {
        List<TextExtractor.PageText> pages = new ArrayList<>();

        if (epubFile == null || !epubFile.exists() || !epubFile.isFile()) {
            throw new IOException("EPUB檔案不存在或無法讀取: " + epubFile);
        }

        try (ZipFile zipFile = new ZipFile(epubFile)) {
            // 解析EPUB結構
            EpubStructure structure = parseEpubStructure(zipFile);

            // 提取章節內容
            List<Chapter> chapters = extractChapters(zipFile, structure);

            // 將每個章節轉換為PageText
            for (int i = 0; i < chapters.size(); i++) {
                Chapter chapter = chapters.get(i);
                TextExtractor.PageText pageText = new TextExtractor.PageText();
                pageText.setPageNumber(i);
                pageText.setOriginalText(chapter.getCleanText());
                pageText.setTextSource(TextExtractor.TextSource.NATIVE);
                pages.add(pageText);
            }

        } catch (Exception e) {
            throw new IOException("解析EPUB檔案時發生錯誤: " + e.getMessage(), e);
        }

        return pages;
    }

    /**
     * 驗證檔案是否為有效的EPUB檔案
     *
     * @param file 要檢查的檔案
     * @return 是否為有效的EPUB檔案
     */
    public boolean isValidEpubFile(File file) {
        if (file == null || !file.exists() || !file.isFile()) {
            return false;
        }

        String fileName = file.getName().toLowerCase();
        if (!fileName.endsWith(".epub")) {
            return false;
        }

        try (ZipFile zipFile = new ZipFile(file)) {
            // 檢查mimetype檔案
            ZipEntry mimetypeEntry = zipFile.getEntry(MIMETYPE_FILE);
            if (mimetypeEntry != null) {
                try (InputStream is = zipFile.getInputStream(mimetypeEntry)) {
                    String mimetype = new String(is.readAllBytes(), StandardCharsets.UTF_8).trim();
                    if (!EPUB_MIMETYPE.equals(mimetype)) {
                        return false;
                    }
                }
            }

            // 檢查container.xml檔案
            ZipEntry containerEntry = zipFile.getEntry(CONTAINER_XML);
            if (containerEntry == null) {
                return false;
            }

            // 嘗試解析container.xml
            parseEpubStructure(zipFile);
            return true;

        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 解析EPUB檔案結構
     *
     * @param zipFile EPUB ZIP檔案
     * @return EPUB結構資訊
     * @throws Exception 解析錯誤
     */
    private EpubStructure parseEpubStructure(ZipFile zipFile) throws Exception {
        EpubStructure structure = new EpubStructure();

        // 解析container.xml獲取OPF檔案路徑
        ZipEntry containerEntry = zipFile.getEntry(CONTAINER_XML);
        if (containerEntry == null) {
            throw new IOException("找不到container.xml檔案");
        }

        try (InputStream is = zipFile.getInputStream(containerEntry)) {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(is);

            NodeList rootfiles = doc.getElementsByTagName("rootfile");
            if (rootfiles.getLength() == 0) {
                throw new IOException("container.xml中找不到rootfile");
            }

            String opfPath = rootfiles.item(0).getAttributes().getNamedItem("full-path").getNodeValue();
            structure.setOpfPath(opfPath);
        }

        // 解析OPF檔案
        ZipEntry opfEntry = zipFile.getEntry(structure.getOpfPath());
        if (opfEntry == null) {
            throw new IOException("找不到OPF檔案: " + structure.getOpfPath());
        }

        try (InputStream is = zipFile.getInputStream(opfEntry)) {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(is);

            // 獲取基礎路徑
            String basePath = "";
            int lastSlash = structure.getOpfPath().lastIndexOf('/');
            if (lastSlash > 0) {
                basePath = structure.getOpfPath().substring(0, lastSlash + 1);
            }
            structure.setBasePath(basePath);

            // 解析spine獲取章節順序
            NodeList spineItems = doc.getElementsByTagName("itemref");
            for (int i = 0; i < spineItems.getLength(); i++) {
                String idref = spineItems.item(i).getAttributes().getNamedItem("idref").getNodeValue();
                structure.addSpineItem(idref);
            }

            // 解析manifest獲取檔案資訊
            NodeList manifestItems = doc.getElementsByTagName("item");
            for (int i = 0; i < manifestItems.getLength(); i++) {
                String id = manifestItems.item(i).getAttributes().getNamedItem("id").getNodeValue();
                String href = manifestItems.item(i).getAttributes().getNamedItem("href").getNodeValue();
                String mediaType = manifestItems.item(i).getAttributes().getNamedItem("media-type").getNodeValue();

                structure.addManifestItem(id, basePath + href, mediaType);
            }
        }

        return structure;
    }

    /**
     * 提取章節內容
     *
     * @param zipFile EPUB ZIP檔案
     * @param structure EPUB結構
     * @return 章節列表
     * @throws Exception 提取錯誤
     */
    private List<Chapter> extractChapters(ZipFile zipFile, EpubStructure structure) throws Exception {
        List<Chapter> chapters = new ArrayList<>();

        for (String spineId : structure.getSpineItems()) {
            String filePath = structure.getManifestItems().get(spineId);
            if (filePath != null) {
                ZipEntry entry = zipFile.getEntry(filePath);
                if (entry != null) {
                    try (InputStream is = zipFile.getInputStream(entry)) {
                        String content = new String(is.readAllBytes(), StandardCharsets.UTF_8);
                        Chapter chapter = parseChapter(content, spineId);
                        chapters.add(chapter);
                    } catch (Exception e) {
                        System.err.println("無法讀取章節 " + spineId + ": " + e.getMessage());
                        // 創建空白章節
                        Chapter errorChapter = new Chapter();
                        errorChapter.setTitle("錯誤章節: " + spineId);
                        errorChapter.setContent("無法載入此章節內容");
                        chapters.add(errorChapter);
                    }
                }
            }
        }

        return chapters;
    }

    /**
     * 解析章節內容
     *
     * @param htmlContent HTML內容
     * @param chapterId 章節ID
     * @return 章節物件
     */
    private Chapter parseChapter(String htmlContent, String chapterId) {
        Chapter chapter = new Chapter();
        chapter.setId(chapterId);

        // 提取章節標題
        String title = extractChapterTitle(htmlContent);
        chapter.setTitle(title.isEmpty() ? "第 " + chapterId + " 章" : title);

        // 提取和清理文字內容
        String cleanText = extractAndCleanText(htmlContent);
        chapter.setContent(cleanText);

        return chapter;
    }

    /**
     * 提取章節標題
     *
     * @param htmlContent HTML內容
     * @return 章節標題
     */
    private String extractChapterTitle(String htmlContent) {
        Matcher matcher = CHAPTER_TITLE_PATTERN.matcher(htmlContent);
        if (matcher.find()) {
            String title = matcher.group(1);
            return cleanHtmlTags(title).trim();
        }

        // 如果沒有找到標題標籤，嘗試從title標籤提取
        Pattern titlePattern = Pattern.compile("<title[^>]*>(.*?)</title>", Pattern.DOTALL | Pattern.CASE_INSENSITIVE);
        matcher = titlePattern.matcher(htmlContent);
        if (matcher.find()) {
            String title = matcher.group(1);
            return cleanHtmlTags(title).trim();
        }

        return "";
    }

    /**
     * 提取和清理文字內容
     *
     * @param htmlContent HTML內容
     * @return 清理後的文字
     */
    private String extractAndCleanText(String htmlContent) {
        StringBuilder result = new StringBuilder();

        // 首先提取段落
        Matcher paragraphMatcher = PARAGRAPH_PATTERN.matcher(htmlContent);
        while (paragraphMatcher.find()) {
            String paragraph = paragraphMatcher.group(1);
            String cleanParagraph = cleanHtmlTags(paragraph);
            cleanParagraph = cleanWhitespace(cleanParagraph);
            if (!cleanParagraph.trim().isEmpty()) {
                result.append(cleanParagraph.trim()).append("\n\n");
            }
        }

        // 如果沒有找到段落，則提取body內的所有文字
        if (result.length() == 0) {
            Pattern bodyPattern = Pattern.compile("<body[^>]*>(.*?)</body>", Pattern.DOTALL | Pattern.CASE_INSENSITIVE);
            Matcher bodyMatcher = bodyPattern.matcher(htmlContent);
            if (bodyMatcher.find()) {
                String bodyContent = bodyMatcher.group(1);
                String cleanText = cleanHtmlTags(bodyContent);
                cleanText = cleanWhitespace(cleanText);
                result.append(cleanText.trim());
            } else {
                // 最後手段：清理整個HTML
                String cleanText = cleanHtmlTags(htmlContent);
                cleanText = cleanWhitespace(cleanText);
                result.append(cleanText.trim());
            }
        }

        return result.toString().trim();
    }

    /**
     * 清理HTML標籤
     *
     * @param html HTML文字
     * @return 清理後的純文字
     */
    private String cleanHtmlTags(String html) {
        if (html == null) return "";

        // 將某些標籤轉換為換行
        html = html.replaceAll("(?i)</(p|div|h[1-6]|br)>", "\n");
        html = html.replaceAll("(?i)<br[^>]*>", "\n");

        // 移除所有HTML標籤
        html = HTML_TAG_PATTERN.matcher(html).replaceAll("");

        // 解碼HTML實體
        html = decodeHtmlEntities(html);

        return html;
    }

    /**
     * 清理空白字符
     *
     * @param text 文字
     * @return 清理後的文字
     */
    private String cleanWhitespace(String text) {
        if (text == null) return "";

        // 將多個空白字符替換為單一空格
        text = WHITESPACE_PATTERN.matcher(text).replaceAll(" ");

        // 移除行首行尾空白
        text = text.replaceAll("(?m)^\\s+", "");
        text = text.replaceAll("(?m)\\s+$", "");

        // 將多個換行替換為最多兩個換行
        text = text.replaceAll("\n{3,}", "\n\n");

        return text;
    }

    /**
     * 解碼HTML實體
     *
     * @param text 包含HTML實體的文字
     * @return 解碼後的文字
     */
    private String decodeHtmlEntities(String text) {
        if (text == null) return "";

        text = text.replace("&lt;", "<");
        text = text.replace("&gt;", ">");
        text = text.replace("&amp;", "&");
        text = text.replace("&quot;", "\"");
        text = text.replace("&apos;", "'");
        text = text.replace("&nbsp;", " ");
        text = text.replace("&#8212;", "—");
        text = text.replace("&#8211;", "–");
        text = text.replace("&#8220;", "\"");
        text = text.replace("&#8221;", "\"");
        text = text.replace("&#8216;", "\'");
        text = text.replace("&#8217;", "\'");

        return text;
    }

    /**
     * 將章節渲染為圖片
     *
     * @param chapter 章節
     * @return 渲染後的圖片
     */
    private BufferedImage renderChapterToImage(Chapter chapter) {
        BufferedImage image = new BufferedImage(DEFAULT_CHAPTER_WIDTH, DEFAULT_CHAPTER_HEIGHT, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = image.createGraphics();

        try {
            // 設定渲染提示
            g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

            // 設定背景
            g2d.setColor(Color.WHITE);
            g2d.fillRect(0, 0, DEFAULT_CHAPTER_WIDTH, DEFAULT_CHAPTER_HEIGHT);

            // 設定字體和顏色
            g2d.setFont(DEFAULT_FONT);
            g2d.setColor(Color.BLACK);

            FontMetrics fm = g2d.getFontMetrics();
            int lineHeight = LINE_HEIGHT;
            int currentY = MARGIN + fm.getAscent();

            // 渲染標題
            if (!chapter.getTitle().isEmpty()) {
                Font titleFont = new Font(DEFAULT_FONT.getName(), Font.BOLD, 24);
                g2d.setFont(titleFont);
                FontMetrics titleFm = g2d.getFontMetrics();

                String title = chapter.getTitle();
                int titleWidth = titleFm.stringWidth(title);
                int titleX = (DEFAULT_CHAPTER_WIDTH - titleWidth) / 2;

                g2d.drawString(title, titleX, currentY);
                currentY += titleFm.getHeight() + 20;

                // 恢復正文字體
                g2d.setFont(DEFAULT_FONT);
                fm = g2d.getFontMetrics();
            }

            // 渲染內容
            String content = chapter.getContent();
            if (!content.isEmpty()) {
                String[] paragraphs = content.split("\n\n");

                for (String paragraph : paragraphs) {
                    if (paragraph.trim().isEmpty()) continue;

                    // 分行渲染段落
                    String[] words = paragraph.trim().split("\\s+");
                    StringBuilder currentLine = new StringBuilder();

                    for (String word : words) {
                        String testLine = currentLine.length() == 0 ? word : currentLine + " " + word;
                        int testWidth = fm.stringWidth(testLine);

                        if (testWidth > DEFAULT_CHAPTER_WIDTH - 2 * MARGIN) {
                            // 渲染當前行
                            if (currentLine.length() > 0) {
                                g2d.drawString(currentLine.toString(), MARGIN, currentY);
                                currentY += lineHeight;
                                currentLine = new StringBuilder(word);
                            } else {
                                // 單個詞太長，強制分行
                                g2d.drawString(word, MARGIN, currentY);
                                currentY += lineHeight;
                            }
                        } else {
                            currentLine = new StringBuilder(testLine);
                        }

                        // 檢查是否超出頁面高度
                        if (currentY > DEFAULT_CHAPTER_HEIGHT - MARGIN) {
                            break;
                        }
                    }

                    // 渲染最後一行
                    if (currentLine.length() > 0 && currentY <= DEFAULT_CHAPTER_HEIGHT - MARGIN) {
                        g2d.drawString(currentLine.toString(), MARGIN, currentY);
                        currentY += lineHeight;
                    }

                    // 段落間距
                    currentY += 10;

                    // 檢查是否超出頁面高度
                    if (currentY > DEFAULT_CHAPTER_HEIGHT - MARGIN) {
                        break;
                    }
                }
            }

        } finally {
            g2d.dispose();
        }

        return image;
    }

    /**
     * 創建錯誤圖片
     *
     * @param errorMessage 錯誤訊息
     * @return 錯誤圖片
     */
    private BufferedImage createErrorImage(String errorMessage) {
        BufferedImage image = new BufferedImage(DEFAULT_CHAPTER_WIDTH, DEFAULT_CHAPTER_HEIGHT, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = image.createGraphics();

        try {
            // 設定渲染提示
            g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

            // 設定背景
            g2d.setColor(Color.WHITE);
            g2d.fillRect(0, 0, DEFAULT_CHAPTER_WIDTH, DEFAULT_CHAPTER_HEIGHT);

            // 設定字體和顏色
            g2d.setFont(new Font(DEFAULT_FONT.getName(), Font.BOLD, 20));
            g2d.setColor(Color.RED);

            FontMetrics fm = g2d.getFontMetrics();
            int messageWidth = fm.stringWidth(errorMessage);
            int x = (DEFAULT_CHAPTER_WIDTH - messageWidth) / 2;
            int y = DEFAULT_CHAPTER_HEIGHT / 2;

            g2d.drawString(errorMessage, x, y);

        } finally {
            g2d.dispose();
        }

        return image;
    }

    /**
     * EPUB結構類
     */
    private static class EpubStructure {
        private String opfPath;
        private String basePath;
        private List<String> spineItems = new ArrayList<>();
        private Map<String, String> manifestItems = new HashMap<>();

        public String getOpfPath() { return opfPath; }
        public void setOpfPath(String opfPath) { this.opfPath = opfPath; }

        public String getBasePath() { return basePath; }
        public void setBasePath(String basePath) { this.basePath = basePath; }

        public List<String> getSpineItems() { return spineItems; }
        public void addSpineItem(String item) { this.spineItems.add(item); }

        public Map<String, String> getManifestItems() { return manifestItems; }
        public void addManifestItem(String id, String href, String mediaType) {
            this.manifestItems.put(id, href);
        }
    }

    /**
     * 載入EPUB內容 - 新增缺少的方法
     */
    public String loadEpubContent(File epubFile) throws IOException {
        List<TextExtractor.PageText> pages = extractTextFromEpub(epubFile);
        
        StringBuilder content = new StringBuilder();
        for (TextExtractor.PageText page : pages) {
            content.append(page.getOriginalText()).append("\n\n");
        }
        
        return content.toString();
    }

    /**
     * 章節類
     */
    public static class Chapter {
        private String id;
        private String title;
        private String content;

        public String getId() { return id; }
        public void setId(String id) { this.id = id; }

        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }

        public String getContent() { return content; }
        public void setContent(String content) { this.content = content; }

        /**
         * 獲取清理後的純文字內容
         *
         * @return 清理後的文字
         */
        public String getCleanText() {
            if (content == null) return "";

            StringBuilder result = new StringBuilder();
            if (title != null && !title.isEmpty()) {
                result.append(title).append("\n\n");
            }
            result.append(content);

            return result.toString();
        }
    }
}
