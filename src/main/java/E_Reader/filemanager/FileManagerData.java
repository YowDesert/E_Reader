package E_Reader.filemanager;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 檔案管理資料類別
 */
public class FileManagerData {
    private final Path libraryPath;
    private final Map<String, FolderItem> folders;
    private final Map<String, FileItem> files;
    
    public FileManagerData(Path libraryPath) {
        this.libraryPath = libraryPath;
        this.folders = new HashMap<>();
        this.files = new HashMap<>();
        initializeData();
    }
    
    private void initializeData() {
        // 確保庫目錄存在
        try {
            Files.createDirectories(libraryPath);
        } catch (IOException e) {
            System.err.println("無法創建資料庫目錄: " + e.getMessage());
        }
        // 載入現有的檔案和資料夾
        loadExistingData();
    }
    
    private void loadExistingData() {
        try {
            scanDirectory(libraryPath.toFile(), "root");
        } catch (Exception e) {
            System.err.println("載入資料時發生錯誤: " + e.getMessage());
        }
    }
    
    private void scanDirectory(File directory, String parentId) {
        if (!directory.exists() || !directory.isDirectory()) {
            return;
        }
        
        File[] files = directory.listFiles();
        if (files == null) return;
        
        for (File file : files) {
            if (file.isDirectory()) {
                // 創建資料夾項目
                String folderId = UUID.randomUUID().toString();
                FolderItem folderItem = new FolderItem(
                    folderId,
                    file.getName(),
                    parentId,
                    LocalDateTime.ofInstant(
                        java.time.Instant.ofEpochMilli(file.lastModified()),
                        ZoneId.systemDefault()
                    )
                );
                folders.put(folderId, folderItem);
                
                // 遞迴掃描子目錄
                scanDirectory(file, folderId);
            } else {
                // 創建檔案項目
                String fileId = UUID.randomUUID().toString();
                String extension = getFileExtension(file.getName());
                
                FileItem fileItem = new FileItem(
                    fileId,
                    file.getName(),
                    file.getAbsolutePath(),
                    extension,
                    file.length(),
                    LocalDateTime.ofInstant(
                        java.time.Instant.ofEpochMilli(file.lastModified()),
                        ZoneId.systemDefault()
                    ),
                    parentId
                );
                this.files.put(fileId, fileItem);
            }
        }
    }
    
    private String getFileExtension(String fileName) {
        int lastDot = fileName.lastIndexOf('.');
        return lastDot > 0 ? fileName.substring(lastDot + 1).toLowerCase() : "";
    }
    
    // 獲取資料夾列表
    public List<FolderItem> getFolders(String parentId) {
        return folders.values().stream()
                .filter(folder -> folder.getParentId().equals(parentId))
                .sorted(Comparator.comparing(FolderItem::getName))
                .collect(Collectors.toList());
    }
    
    // 獲取子資料夾
    public List<FolderItem> getSubFolders(String parentId) {
        return getFolders(parentId);
    }
    
    // 獲取檔案列表
    public List<FileItem> getFiles(String folderId) {
        return files.values().stream()
                .filter(file -> file.getFolderId().equals(folderId))
                .sorted(Comparator.comparing(FileItem::getName))
                .collect(Collectors.toList());
    }
    
    // 獲取資料夾
    public FolderItem getFolder(String folderId) {
        return folders.get(folderId);
    }
    
    // 獲取檔案數量
    public int getFileCount(String folderId) {
        return (int) files.values().stream()
                .filter(file -> file.getFolderId().equals(folderId))
                .count();
    }
    
    // 獲取資料夾路徑
    public String getFolderPath(String folderId) {
        if ("root".equals(folderId)) {
            return "首頁";
        }
        
        FolderItem folder = folders.get(folderId);
        if (folder == null) {
            return "未知路徑";
        }
        
        List<String> pathParts = new ArrayList<>();
        String currentId = folderId;
        
        while (currentId != null && !currentId.equals("root")) {
            FolderItem currentFolder = folders.get(currentId);
            if (currentFolder == null) break;
            
            pathParts.add(0, currentFolder.getName());
            currentId = currentFolder.getParentId();
        }
        
        return "首頁" + (pathParts.isEmpty() ? "" : " > " + String.join(" > ", pathParts));
    }
    
    // 創建資料夾
    public boolean createFolder(String name, String parentId) {
        try {
            // 檢查是否已存在同名資料夾
            boolean exists = folders.values().stream()
                    .anyMatch(folder -> folder.getParentId().equals(parentId) && 
                             folder.getName().equals(name));
            
            if (exists) {
                return false;
            }
            
            // 創建實體目錄
            Path parentPath = getPhysicalPath(parentId);
            Path newFolderPath = parentPath.resolve(name);
            Files.createDirectories(newFolderPath);
            
            // 創建資料夾項目
            String folderId = UUID.randomUUID().toString();
            FolderItem folderItem = new FolderItem(
                folderId, name, parentId, LocalDateTime.now()
            );
            folders.put(folderId, folderItem);
            
            return true;
        } catch (Exception e) {
            System.err.println("創建資料夾失敗: " + e.getMessage());
            return false;
        }
    }
    
    // 重新命名資料夾
    public boolean renameFolder(String folderId, String newName) {
        try {
            FolderItem folder = folders.get(folderId);
            if (folder == null) return false;
            
            // 檢查新名稱是否已存在
            boolean exists = folders.values().stream()
                    .anyMatch(f -> f.getParentId().equals(folder.getParentId()) && 
                             f.getName().equals(newName) && 
                             !f.getId().equals(folderId));
            
            if (exists) return false;
            
            // 重新命名實體目錄
            Path oldPath = getPhysicalPath(folderId);
            Path newPath = oldPath.getParent().resolve(newName);
            Files.move(oldPath, newPath);
            
            // 更新資料夾項目
            folder.setName(newName);
            
            return true;
        } catch (Exception e) {
            System.err.println("重新命名資料夾失敗: " + e.getMessage());
            return false;
        }
    }
    
    // 刪除資料夾
    public boolean deleteFolder(String folderId) {
        try {
            FolderItem folder = folders.get(folderId);
            if (folder == null) return false;
            
            // 刪除實體目錄
            Path folderPath = getPhysicalPath(folderId);
            deleteDirectoryRecursively(folderPath);
            
            // 刪除資料庫中的記錄
            folders.remove(folderId);
            
            // 刪除子資料夾和檔案
            List<String> subFolderIds = folders.values().stream()
                    .filter(f -> f.getParentId().equals(folderId))
                    .map(FolderItem::getId)
                    .collect(Collectors.toList());
            
            for (String subFolderId : subFolderIds) {
                deleteFolder(subFolderId);
            }
            
            List<String> fileIds = files.values().stream()
                    .filter(f -> f.getFolderId().equals(folderId))
                    .map(FileItem::getId)
                    .collect(Collectors.toList());
            
            for (String fileId : fileIds) {
                files.remove(fileId);
            }
            
            return true;
        } catch (Exception e) {
            System.err.println("刪除資料夾失敗: " + e.getMessage());
            return false;
        }
    }
    
    private void deleteDirectoryRecursively(Path directory) throws IOException {
        if (Files.exists(directory)) {
            Files.walk(directory)
                    .sorted(Comparator.reverseOrder())
                    .map(Path::toFile)
                    .forEach(File::delete);
        }
    }
    
    // 匯入檔案
    public boolean importFile(File sourceFile, String folderId) throws IOException {
        Path targetDir = getPhysicalPath(folderId);
        Path targetFile = targetDir.resolve(sourceFile.getName());
        
        // 如果檔案已存在，生成新名稱
        int counter = 1;
        String originalName = sourceFile.getName();
        String baseName = originalName;
        String extension = "";
        
        int lastDot = originalName.lastIndexOf('.');
        if (lastDot > 0) {
            baseName = originalName.substring(0, lastDot);
            extension = originalName.substring(lastDot);
        }
        
        while (Files.exists(targetFile)) {
            String newName = baseName + " (" + counter + ")" + extension;
            targetFile = targetDir.resolve(newName);
            counter++;
        }
        
        // 複製檔案
        Files.copy(sourceFile.toPath(), targetFile, StandardCopyOption.REPLACE_EXISTING);
        
        // 創建檔案項目
        String fileId = UUID.randomUUID().toString();
        String fileExtension = getFileExtension(targetFile.getFileName().toString());
        
        FileItem fileItem = new FileItem(
            fileId,
            targetFile.getFileName().toString(),
            targetFile.toString(),
            fileExtension,
            Files.size(targetFile),
            LocalDateTime.now(),
            folderId
        );
        
        files.put(fileId, fileItem);
        return true;
    }
    
    // 重新命名檔案
    public boolean renameFile(String fileId, String newName) {
        try {
            FileItem file = files.get(fileId);
            if (file == null) return false;
            
            Path oldPath = Paths.get(file.getFilePath());
            Path newPath = oldPath.getParent().resolve(newName);
            
            // 檢查新名稱是否已存在
            if (Files.exists(newPath)) return false;
            
            // 重新命名實體檔案
            Files.move(oldPath, newPath);
            
            // 更新檔案項目
            file.setName(newName);
            file.setFilePath(newPath.toString());
            
            return true;
        } catch (Exception e) {
            System.err.println("重新命名檔案失敗: " + e.getMessage());
            return false;
        }
    }
    
    // 移動檔案
    public boolean moveFile(String fileId, String targetFolderId) {
        try {
            FileItem file = files.get(fileId);
            if (file == null) return false;
            
            Path oldPath = Paths.get(file.getFilePath());
            Path targetDir = getPhysicalPath(targetFolderId);
            Path newPath = targetDir.resolve(oldPath.getFileName());
            
            // 如果目標位置已有同名檔案，生成新名稱
            if (Files.exists(newPath)) {
                String originalName = oldPath.getFileName().toString();
                String baseName = originalName;
                String extension = "";
                
                int lastDot = originalName.lastIndexOf('.');
                if (lastDot > 0) {
                    baseName = originalName.substring(0, lastDot);
                    extension = originalName.substring(lastDot);
                }
                
                int counter = 1;
                do {
                    String newName = baseName + " (" + counter + ")" + extension;
                    newPath = targetDir.resolve(newName);
                    counter++;
                } while (Files.exists(newPath));
            }
            
            // 移動實體檔案
            Files.move(oldPath, newPath);
            
            // 更新檔案項目
            file.setFolderId(targetFolderId);
            file.setFilePath(newPath.toString());
            
            return true;
        } catch (Exception e) {
            System.err.println("移動檔案失敗: " + e.getMessage());
            return false;
        }
    }
    
    // 刪除檔案
    public boolean deleteFile(String fileId) {
        try {
            FileItem file = files.get(fileId);
            if (file == null) return false;
            
            // 刪除實體檔案
            Path filePath = Paths.get(file.getFilePath());
            Files.deleteIfExists(filePath);
            
            // 刪除檔案項目
            files.remove(fileId);
            
            return true;
        } catch (Exception e) {
            System.err.println("刪除檔案失敗: " + e.getMessage());
            return false;
        }
    }
    
    // 獲取父資料夾ID
    public String getParentFolderId(String folderId) {
        FolderItem folder = folders.get(folderId);
        return folder != null ? folder.getParentId() : null;
    }
    
    // 獲取實體路徑
    private Path getPhysicalPath(String folderId) {
        if ("root".equals(folderId)) {
            return libraryPath;
        }
        
        List<String> pathParts = new ArrayList<>();
        String currentId = folderId;
        
        while (currentId != null && !currentId.equals("root")) {
            FolderItem folder = folders.get(currentId);
            if (folder == null) break;
            
            pathParts.add(0, folder.getName());
            currentId = folder.getParentId();
        }
        
        Path result = libraryPath;
        for (String part : pathParts) {
            result = result.resolve(part);
        }
        
        return result;
    }
}
