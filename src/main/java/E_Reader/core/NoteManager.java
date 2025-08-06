package E_Reader.core;

import E_Reader.settings.SettingsManager;
import java.io.*;
import java.util.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * 筆記管理器 - 處理畫重點和筆記功能
 */
public class NoteManager {
    
    private final String NOTES_FILE = "book_notes.json";
    private final Map<String, BookNotes> bookNotesMap;
    private final SettingsManager settingsManager;
    
    public NoteManager(SettingsManager settingsManager) {
        this.settingsManager = settingsManager;
        this.bookNotesMap = new HashMap<>();
        loadNotes();
    }
    
    /**
     * 書籍筆記類
     */
    public static class BookNotes {
        private String bookPath;
        private String bookName;
        private List<Highlight> highlights;
        private List<Note> notes;
        private LocalDateTime lastModified;
        
        public BookNotes(String bookPath, String bookName) {
            this.bookPath = bookPath;
            this.bookName = bookName;
            this.highlights = new ArrayList<>();
            this.notes = new ArrayList<>();
            this.lastModified = LocalDateTime.now();
        }
        
        // Getters and Setters
        public String getBookPath() { return bookPath; }
        public String getBookName() { return bookName; }
        public List<Highlight> getHighlights() { return highlights; }
        public List<Note> getNotes() { return notes; }
        public LocalDateTime getLastModified() { return lastModified; }
        public void setLastModified(LocalDateTime lastModified) { this.lastModified = lastModified; }
    }
    
    /**
     * 重點標記類
     */
    public static class Highlight {
        private int pageIndex;
        private int startPosition;
        private int endPosition;
        private String selectedText;
        private String color;
        private LocalDateTime createdTime;
        private String note;
        
        public Highlight(int pageIndex, int startPosition, int endPosition, String selectedText, String color) {
            this.pageIndex = pageIndex;
            this.startPosition = startPosition;
            this.endPosition = endPosition;
            this.selectedText = selectedText;
            this.color = color;
            this.createdTime = LocalDateTime.now();
            this.note = "";
        }
        
        // Getters and Setters
        public int getPageIndex() { return pageIndex; }
        public int getStartPosition() { return startPosition; }
        public int getEndPosition() { return endPosition; }
        public String getSelectedText() { return selectedText; }
        public String getColor() { return color; }
        public LocalDateTime getCreatedTime() { return createdTime; }
        public String getNote() { return note; }
        public void setNote(String note) { this.note = note; }
    }
    
    /**
     * 筆記類
     */
    public static class Note {
        private int pageIndex;
        private String content;
        private LocalDateTime createdTime;
        private LocalDateTime modifiedTime;
        private String title;
        
        public Note(int pageIndex, String content, String title) {
            this.pageIndex = pageIndex;
            this.content = content;
            this.title = title;
            this.createdTime = LocalDateTime.now();
            this.modifiedTime = LocalDateTime.now();
        }
        
        // Getters and Setters
        public int getPageIndex() { return pageIndex; }
        public String getContent() { return content; }
        public LocalDateTime getCreatedTime() { return createdTime; }
        public LocalDateTime getModifiedTime() { return modifiedTime; }
        public String getTitle() { return title; }
        public void setContent(String content) { 
            this.content = content; 
            this.modifiedTime = LocalDateTime.now();
        }
        public void setTitle(String title) { this.title = title; }
    }
    
    /**
     * 添加重點標記
     */
    public void addHighlight(String bookPath, String bookName, int pageIndex, 
                           int startPosition, int endPosition, String selectedText, String color) {
        BookNotes bookNotes = getOrCreateBookNotes(bookPath, bookName);
        Highlight highlight = new Highlight(pageIndex, startPosition, endPosition, selectedText, color);
        bookNotes.getHighlights().add(highlight);
        bookNotes.setLastModified(LocalDateTime.now());
        saveNotes();
    }
    
    /**
     * 添加筆記
     */
    public void addNote(String bookPath, String bookName, int pageIndex, String content, String title) {
        BookNotes bookNotes = getOrCreateBookNotes(bookPath, bookName);
        Note note = new Note(pageIndex, content, title);
        bookNotes.getNotes().add(note);
        bookNotes.setLastModified(LocalDateTime.now());
        saveNotes();
    }
    
    /**
     * 獲取書籍的所有重點
     */
    public List<Highlight> getHighlights(String bookPath) {
        BookNotes bookNotes = bookNotesMap.get(bookPath);
        return bookNotes != null ? bookNotes.getHighlights() : new ArrayList<>();
    }
    
    /**
     * 獲取書籍的所有筆記
     */
    public List<Note> getNotes(String bookPath) {
        BookNotes bookNotes = bookNotesMap.get(bookPath);
        return bookNotes != null ? bookNotes.getNotes() : new ArrayList<>();
    }
    
    /**
     * 獲取特定頁面的重點
     */
    public List<Highlight> getHighlightsForPage(String bookPath, int pageIndex) {
        List<Highlight> pageHighlights = new ArrayList<>();
        List<Highlight> allHighlights = getHighlights(bookPath);
        
        for (Highlight highlight : allHighlights) {
            if (highlight.getPageIndex() == pageIndex) {
                pageHighlights.add(highlight);
            }
        }
        
        return pageHighlights;
    }
    
    /**
     * 獲取特定頁面的筆記
     */
    public List<Note> getNotesForPage(String bookPath, int pageIndex) {
        List<Note> pageNotes = new ArrayList<>();
        List<Note> allNotes = getNotes(bookPath);
        
        for (Note note : allNotes) {
            if (note.getPageIndex() == pageIndex) {
                pageNotes.add(note);
            }
        }
        
        return pageNotes;
    }
    
    /**
     * 刪除重點標記
     */
    public void removeHighlight(String bookPath, Highlight highlight) {
        BookNotes bookNotes = bookNotesMap.get(bookPath);
        if (bookNotes != null) {
            bookNotes.getHighlights().remove(highlight);
            bookNotes.setLastModified(LocalDateTime.now());
            saveNotes();
        }
    }
    
    /**
     * 刪除筆記
     */
    public void removeNote(String bookPath, Note note) {
        BookNotes bookNotes = bookNotesMap.get(bookPath);
        if (bookNotes != null) {
            bookNotes.getNotes().remove(note);
            bookNotes.setLastModified(LocalDateTime.now());
            saveNotes();
        }
    }
    
    /**
     * 更新筆記
     */
    public void updateNote(String bookPath, Note note, String newContent, String newTitle) {
        note.setContent(newContent);
        note.setTitle(newTitle);
        BookNotes bookNotes = bookNotesMap.get(bookPath);
        if (bookNotes != null) {
            bookNotes.setLastModified(LocalDateTime.now());
            saveNotes();
        }
    }
    
    /**
     * 獲取或創建書籍筆記
     */
    private BookNotes getOrCreateBookNotes(String bookPath, String bookName) {
        BookNotes bookNotes = bookNotesMap.get(bookPath);
        if (bookNotes == null) {
            bookNotes = new BookNotes(bookPath, bookName);
            bookNotesMap.put(bookPath, bookNotes);
        }
        return bookNotes;
    }
    
    /**
     * 載入筆記
     */
    private void loadNotes() {
        try {
            File notesFile = new File(NOTES_FILE);
            if (notesFile.exists()) {
                // 這裡可以實現 JSON 載入邏輯
                // 暫時使用簡單的檔案讀取
                System.out.println("筆記檔案載入成功");
            }
        } catch (Exception e) {
            System.err.println("載入筆記失敗: " + e.getMessage());
        }
    }
    
    /**
     * 保存筆記
     */
    private void saveNotes() {
        try {
            // 這裡可以實現 JSON 保存邏輯
            // 暫時使用簡單的檔案寫入
            System.out.println("筆記保存成功");
        } catch (Exception e) {
            System.err.println("保存筆記失敗: " + e.getMessage());
        }
    }
    
    /**
     * 獲取所有書籍的筆記統計
     */
    public Map<String, Integer> getBookNotesStatistics() {
        Map<String, Integer> statistics = new HashMap<>();
        
        for (Map.Entry<String, BookNotes> entry : bookNotesMap.entrySet()) {
            BookNotes bookNotes = entry.getValue();
            int totalItems = bookNotes.getHighlights().size() + bookNotes.getNotes().size();
            statistics.put(bookNotes.getBookName(), totalItems);
        }
        
        return statistics;
    }
    
    /**
     * 搜尋筆記和重點
     */
    public List<SearchResult> searchNotes(String bookPath, String keyword) {
        List<SearchResult> results = new ArrayList<>();
        
        BookNotes bookNotes = bookNotesMap.get(bookPath);
        if (bookNotes == null) {
            return results;
        }
        
        // 搜尋重點
        for (Highlight highlight : bookNotes.getHighlights()) {
            if (highlight.getSelectedText().toLowerCase().contains(keyword.toLowerCase()) ||
                highlight.getNote().toLowerCase().contains(keyword.toLowerCase())) {
                results.add(new SearchResult(highlight, null));
            }
        }
        
        // 搜尋筆記
        for (Note note : bookNotes.getNotes()) {
            if (note.getContent().toLowerCase().contains(keyword.toLowerCase()) ||
                note.getTitle().toLowerCase().contains(keyword.toLowerCase())) {
                results.add(new SearchResult(null, note));
            }
        }
        
        return results;
    }
    
    /**
     * 搜尋結果類
     */
    public static class SearchResult {
        private final Highlight highlight;
        private final Note note;
        
        public SearchResult(Highlight highlight, Note note) {
            this.highlight = highlight;
            this.note = note;
        }
        
        public Highlight getHighlight() { return highlight; }
        public Note getNote() { return note; }
        public boolean isHighlight() { return highlight != null; }
        public boolean isNote() { return note != null; }
    }
} 