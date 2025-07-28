package E_Reader.filemanager;

import java.time.LocalDateTime;

/**
 * 資料夾項目類別
 */
public class FolderItem {
    private String id;
    private String name;
    private String parentId;
    private LocalDateTime createdDate;
    
    public FolderItem(String id, String name, String parentId, LocalDateTime createdDate) {
        this.id = id;
        this.name = name;
        this.parentId = parentId;
        this.createdDate = createdDate;
    }
    
    // Getters and Setters
    public String getId() { 
        return id; 
    }
    
    public void setId(String id) { 
        this.id = id; 
    }
    
    public String getName() { 
        return name; 
    }
    
    public void setName(String name) { 
        this.name = name; 
    }
    
    public String getParentId() { 
        return parentId; 
    }
    
    public void setParentId(String parentId) { 
        this.parentId = parentId; 
    }
    
    public LocalDateTime getCreatedDate() { 
        return createdDate; 
    }
    
    public void setCreatedDate(LocalDateTime createdDate) { 
        this.createdDate = createdDate; 
    }
    
    @Override
    public String toString() {
        return name;
    }
}
