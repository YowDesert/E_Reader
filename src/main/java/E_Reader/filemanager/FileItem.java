package E_Reader.filemanager;

import java.time.LocalDateTime;

/**
 * 檔案項目類別
 */
public class FileItem {
    private String id;
    private String name;
    private String filePath;
    private String extension;
    private long size;
    private LocalDateTime lastModified;
    private boolean favorite;
    private String folderId;
    private String thumbnailPath;
    
    public FileItem(String id, String name, String filePath, String extension, long size, LocalDateTime lastModified, String folderId) {
        this.id = id;
        this.name = name;
        this.filePath = filePath;
        this.extension = extension;
        this.size = size;
        this.lastModified = lastModified;
        this.favorite = false;
        this.folderId = folderId;
        this.thumbnailPath = null;
    }
    
    // 舊的建構函數，保持向後相容
    public FileItem(String path, String name, String type, long size, LocalDateTime lastModified) {
        this.id = java.util.UUID.randomUUID().toString();
        this.name = name;
        this.filePath = path;
        this.extension = type;
        this.size = size;
        this.lastModified = lastModified;
        this.favorite = false;
        this.folderId = "ALL";
        this.thumbnailPath = null;
    }
    
    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public String getFilePath() { return filePath; }
    public void setFilePath(String filePath) { this.filePath = filePath; }
    
    // 保持向後相容
    public String getPath() { return filePath; }
    public void setPath(String path) { this.filePath = path; }
    
    public String getExtension() { return extension; }
    public void setExtension(String extension) { this.extension = extension; }
    
    // 保持向後相容
    public String getType() { return extension; }
    public void setType(String type) { this.extension = type; }
    
    public long getSize() { return size; }
    public void setSize(long size) { this.size = size; }
    
    public LocalDateTime getLastModified() { return lastModified; }
    public void setLastModified(LocalDateTime lastModified) { this.lastModified = lastModified; }
    
    public boolean isFavorite() { return favorite; }
    public void setFavorite(boolean favorite) { this.favorite = favorite; }
    
    public String getFolderId() { return folderId; }
    public void setFolderId(String folderId) { this.folderId = folderId; }
    
    public String getThumbnailPath() { return thumbnailPath; }
    public void setThumbnailPath(String thumbnailPath) { this.thumbnailPath = thumbnailPath; }
    
    public boolean hasThumbnail() {
        return thumbnailPath != null && !thumbnailPath.isEmpty();
    }
    
    @Override
    public String toString() {
        return name;
    }
}