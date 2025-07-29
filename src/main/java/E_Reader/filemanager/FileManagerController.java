package E_Reader.filemanager;

import E_Reader.ui.MainController;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 檔案管理控制器 - 類似GoodNotes的檔案管理系統
 */
public class FileManagerController {

    private final Stage primaryStage;
    private final Path libraryPath;
    private final FileManagerData fileManagerData;

    // UI 組件
    private BorderPane mainLayout;
    private VBox folderPanel;
    private ScrollPane fileViewScrollPane;
    private GridPane fileGrid;
    private Label statusLabel;
    private TextField searchField;
    private ComboBox<String> sortComboBox;
    private Label currentPathLabel;
    private ProgressBar importProgressBar;

    // 當前狀態
    private String currentFolderId = "root";
    private List<FileItem> currentFiles = new ArrayList<>();
    private List<FolderItem> currentFolders = new ArrayList<>();

    // 回調函數
    private FileOpenCallback fileOpenCallback;

    public interface FileOpenCallback {
        void onFileOpen(File file);
    }

    public FileManagerController(Stage primaryStage) {
        this.primaryStage = primaryStage;
        this.libraryPath = Paths.get(System.getProperty("user.home"), "E_Reader_Library");
        this.fileManagerData = new FileManagerData(libraryPath);

        // 確保庫目錄存在
        try {
            Files.createDirectories(libraryPath);
        } catch (IOException e) {
            System.err.println("無法創建檔案庫目錄: " + e.getMessage());
        }
    }

    public void initialize(FileOpenCallback callback) {
        this.fileOpenCallback = callback;
        setupMainLayout();
        setupEventHandlers();
        loadCurrentFolder();
    }

    private void setupMainLayout() {
        mainLayout = new BorderPane();
        mainLayout.setPrefSize(1000, 700);

        // 頂部工具欄
        VBox topSection = createTopSection();
        mainLayout.setTop(topSection);

        // 左側資料夾面板
        folderPanel = createFolderPanel();
        ScrollPane folderScrollPane = new ScrollPane(folderPanel);
        folderScrollPane.setPrefWidth(250);
        folderScrollPane.setFitToWidth(true);
        folderScrollPane.setStyle("-fx-background-color: #f5f5f5;");
        mainLayout.setLeft(folderScrollPane);

        // 中央檔案檢視區域
        fileGrid = new GridPane();
        fileGrid.setHgap(15);
        fileGrid.setVgap(15);
        fileGrid.setPadding(new Insets(20));

        fileViewScrollPane = new ScrollPane(fileGrid);
        fileViewScrollPane.setFitToWidth(true);
        fileViewScrollPane.setFitToHeight(true);
        fileViewScrollPane.setStyle("-fx-background-color: white;");
        mainLayout.setCenter(fileViewScrollPane);

        // 底部狀態欄
        statusLabel = new Label("就緒");
        statusLabel.setPadding(new Insets(5, 10, 5, 10));
        statusLabel.setStyle("-fx-background-color: #e9ecef; -fx-border-color: #dee2e6; -fx-border-width: 1 0 0 0;");
        mainLayout.setBottom(statusLabel);

        // 套用樣式
        mainLayout.setStyle("-fx-background-color: white;");
    }

    private VBox createTopSection() {
        VBox topSection = new VBox();

        // 工具欄
        HBox toolbar = new HBox(10);
        toolbar.setPadding(new Insets(10));
        toolbar.setAlignment(Pos.CENTER_LEFT);
        toolbar.setStyle("-fx-background-color: #f8f9fa; -fx-border-color: #dee2e6; -fx-border-width: 0 0 1 0;");

        // 匯入按鈕已移除 - 進入檔案管理器後不需要再使用匯入功能

        // 新增資料夾按鈕
        Button newFolderBtn = new Button("📂 新增資料夾");
        newFolderBtn.setStyle("-fx-background-color: #28a745; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 8 16;");
        newFolderBtn.setOnAction(e -> showNewFolderDialog());

        // 刷新按鈕
        Button refreshBtn = new Button("🔄 刷新");
        refreshBtn.setStyle("-fx-background-color: #6c757d; -fx-text-fill: white; -fx-padding: 8 16;");
        refreshBtn.setOnAction(e -> loadCurrentFolder());

        // 搜尋框
        searchField = new TextField();
        searchField.setPromptText("搜尋檔案或資料夾...");
        searchField.setPrefWidth(200);
        searchField.textProperty().addListener((obs, oldText, newText) -> filterFiles(newText));

        // 排序選項
        sortComboBox = new ComboBox<>();
        sortComboBox.getItems().addAll("名稱 (A-Z)", "名稱 (Z-A)", "修改時間 (新到舊)", "修改時間 (舊到新)", "檔案大小");
        sortComboBox.setValue("名稱 (A-Z)");
        sortComboBox.setOnAction(e -> sortAndRefreshFiles());

        // 檢視模式切換
        ToggleGroup viewGroup = new ToggleGroup();
        RadioButton gridViewBtn = new RadioButton("🔲 網格檢視");
        RadioButton listViewBtn = new RadioButton("📋 清單檢視");
        gridViewBtn.setToggleGroup(viewGroup);
        listViewBtn.setToggleGroup(viewGroup);
        gridViewBtn.setSelected(true);

        toolbar.getChildren().addAll(
                newFolderBtn, refreshBtn,
                new Separator(), searchField, sortComboBox,
                new Separator(), gridViewBtn, listViewBtn
        );

        // 路徑導航
        currentPathLabel = new Label("首頁");
        currentPathLabel.setPadding(new Insets(5, 10, 5, 10));
        currentPathLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");

        topSection.getChildren().addAll(toolbar, currentPathLabel);
        return topSection;
    }

    private VBox createFolderPanel() {
        VBox panel = new VBox(5);
        panel.setPadding(new Insets(10));

        // 資料夾標題
        Label titleLabel = new Label("資料夾");
        titleLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-padding: 0 0 10 0;");

        // 根目錄項目
        VBox rootItem = createFolderTreeItem("📚 我的資料庫", "root", true);

        panel.getChildren().addAll(titleLabel, rootItem);
        return panel;
    }

    private VBox createFolderTreeItem(String name, String folderId, boolean isRoot) {
        VBox item = new VBox();

        HBox folderRow = new HBox(5);
        folderRow.setPadding(new Insets(5));
        folderRow.setAlignment(Pos.CENTER_LEFT);
        folderRow.setStyle("-fx-background-radius: 5px; -fx-cursor: hand;");

        if (folderId.equals(currentFolderId)) {
            folderRow.setStyle(folderRow.getStyle() + "; -fx-background-color: #e3f2fd;");
        }

        Label folderLabel = new Label(name);
        folderLabel.setStyle("-fx-font-size: 14px;");

        folderRow.getChildren().add(folderLabel);

        // 點擊事件
        folderRow.setOnMouseClicked(e -> {
            if (e.getButton() == MouseButton.PRIMARY) {
                navigateToFolder(folderId);
            } else if (e.getButton() == MouseButton.SECONDARY && !isRoot) {
                showFolderContextMenu(folderRow, folderId, e.getScreenX(), e.getScreenY());
            }
        });

        item.getChildren().add(folderRow);

        // 如果不是根目錄，載入子資料夾
        if (!isRoot) {
            List<FolderItem> subFolders = fileManagerData.getSubFolders(folderId);
            for (FolderItem subFolder : subFolders) {
                VBox subItem = createFolderTreeItem("📁 " + subFolder.getName(), subFolder.getId(), false);
                subItem.setPadding(new Insets(0, 0, 0, 20));
                item.getChildren().add(subItem);
            }
        }

        return item;
    }

    private void setupEventHandlers() {
        // 拖拽支持（簡化版本）
        fileGrid.setOnDragOver(e -> {
            if (e.getDragboard().hasFiles()) {
                e.acceptTransferModes(javafx.scene.input.TransferMode.COPY);
            }
            e.consume();
        });

        fileGrid.setOnDragDropped(e -> {
            if (e.getDragboard().hasFiles()) {
                List<File> files = e.getDragboard().getFiles();
                importFiles(files);
                e.setDropCompleted(true);
            }
            e.consume();
        });
    }

    private void loadCurrentFolder() {
        Platform.runLater(() -> {
            statusLabel.setText("載入中...");

            // 載入資料夾
            currentFolders = fileManagerData.getFolders(currentFolderId);

            // 載入檔案
            currentFiles = fileManagerData.getFiles(currentFolderId);

            // 更新路徑顯示
            updatePathLabel();

            // 重新整理檢視
            refreshFileView();

            // 重新整理資料夾面板
            refreshFolderPanel();

            statusLabel.setText("已載入 " + currentFiles.size() + " 個檔案，" + currentFolders.size() + " 個資料夾");
        });
    }

    private void refreshFileView() {
        fileGrid.getChildren().clear();

        int column = 0;
        int row = 0;
        int maxColumns = 5; // 每行最多5個項目

        // 先顯示資料夾
        for (FolderItem folder : currentFolders) {
            VBox folderCard = createFolderCard(folder);
            fileGrid.add(folderCard, column, row);

            column++;
            if (column >= maxColumns) {
                column = 0;
                row++;
            }
        }

        // 再顯示檔案
        for (FileItem file : currentFiles) {
            VBox fileCard = createFileCard(file);
            fileGrid.add(fileCard, column, row);

            column++;
            if (column >= maxColumns) {
                column = 0;
                row++;
            }
        }
    }

    private VBox createFolderCard(FolderItem folder) {
        VBox card = new VBox(10);
        card.setPrefSize(160, 140);
        card.setAlignment(Pos.CENTER);
        card.setStyle("-fx-background-color: white; -fx-border-color: #e0e0e0; -fx-border-radius: 8px; " +
                "-fx-background-radius: 8px; -fx-padding: 15; -fx-cursor: hand; " +
                "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 5, 0, 0, 2);");

        // 資料夾圖示
        Label iconLabel = new Label("📁");
        iconLabel.setStyle("-fx-font-size: 48px;");

        // 資料夾名稱
        Label nameLabel = new Label(folder.getName());
        nameLabel.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-alignment: center;");
        nameLabel.setWrapText(true);
        nameLabel.setMaxWidth(130);

        // 檔案數量
        int fileCount = fileManagerData.getFileCount(folder.getId());
        Label countLabel = new Label(fileCount + " 個項目");
        countLabel.setStyle("-fx-font-size: 10px; -fx-text-fill: #666666;");

        card.getChildren().addAll(iconLabel, nameLabel, countLabel);

        // 懸停效果
        card.setOnMouseEntered(e -> card.setStyle(card.getStyle() + "; -fx-background-color: #f8f9fa;"));
        card.setOnMouseExited(e -> card.setStyle(card.getStyle().replace("; -fx-background-color: #f8f9fa", "")));

        // 點擊事件
        card.setOnMouseClicked(e -> {
            if (e.getButton() == MouseButton.PRIMARY) {
                if (e.getClickCount() == 2) {
                    navigateToFolder(folder.getId());
                }
            } else if (e.getButton() == MouseButton.SECONDARY) {
                showFolderContextMenu(card, folder.getId(), e.getScreenX(), e.getScreenY());
            }
        });

        return card;
    }

    private VBox createFileCard(FileItem file) {
        VBox card = new VBox(10);
        card.setPrefSize(160, 140);
        card.setAlignment(Pos.CENTER);
        card.setStyle("-fx-background-color: white; -fx-border-color: #e0e0e0; -fx-border-radius: 8px; " +
                "-fx-background-radius: 8px; -fx-padding: 15; -fx-cursor: hand; " +
                "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 5, 0, 0, 2);");

        // 檔案縮圖或圖示
        VBox thumbnailContainer = new VBox();
        thumbnailContainer.setAlignment(Pos.CENTER);
        thumbnailContainer.setPrefSize(80, 60);

        if (file.hasThumbnail()) {
            try {
                ImageView thumbnail = new ImageView(new Image(file.getThumbnailPath()));
                thumbnail.setFitWidth(80);
                thumbnail.setFitHeight(60);
                thumbnail.setPreserveRatio(true);
                thumbnail.setSmooth(true);
                thumbnailContainer.getChildren().add(thumbnail);
            } catch (Exception e) {
                Label iconLabel = new Label(getFileIcon(file.getExtension()));
                iconLabel.setStyle("-fx-font-size: 36px;");
                thumbnailContainer.getChildren().add(iconLabel);
            }
        } else {
            Label iconLabel = new Label(getFileIcon(file.getExtension()));
            iconLabel.setStyle("-fx-font-size: 36px;");
            thumbnailContainer.getChildren().add(iconLabel);
        }

        // 檔案名稱
        Label nameLabel = new Label(file.getName());
        nameLabel.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-alignment: center;");
        nameLabel.setWrapText(true);
        nameLabel.setMaxWidth(130);

        // 檔案大小和日期
        String sizeText = formatFileSize(file.getSize());
        String dateText = file.getLastModified().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
        Label infoLabel = new Label(sizeText + " • " + dateText);
        infoLabel.setStyle("-fx-font-size: 10px; -fx-text-fill: #666666;");

        card.getChildren().addAll(thumbnailContainer, nameLabel, infoLabel);

        // 懸停效果
        card.setOnMouseEntered(e -> card.setStyle(card.getStyle() + "; -fx-background-color: #f8f9fa;"));
        card.setOnMouseExited(e -> card.setStyle(card.getStyle().replace("; -fx-background-color: #f8f9fa", "")));

        // 點擊事件
        card.setOnMouseClicked(e -> {
            if (e.getButton() == MouseButton.PRIMARY) {
                if (e.getClickCount() == 2) {
                    openFile(file);
                }
            } else if (e.getButton() == MouseButton.SECONDARY) {
                showFileContextMenu(card, file, e.getScreenX(), e.getScreenY());
            }
        });

        return card;
    }

    private String getFileIcon(String extension) {
        switch (extension.toLowerCase()) {
            case "pdf": return "📄";
            case "jpg":
            case "jpeg":
            case "png":
            case "gif":
            case "bmp": return "🖼️";
            case "txt": return "📝";
            case "doc":
            case "docx": return "📘";
            case "zip":
            case "rar": return "📦";
            default: return "📎";
        }
    }

    private String formatFileSize(long bytes) {
        if (bytes < 1024) return bytes + " B";
        if (bytes < 1024 * 1024) return String.format("%.1f KB", bytes / 1024.0);
        if (bytes < 1024 * 1024 * 1024) return String.format("%.1f MB", bytes / (1024.0 * 1024.0));
        return String.format("%.1f GB", bytes / (1024.0 * 1024.0 * 1024.0));
    }

    private void navigateToFolder(String folderId) {
        currentFolderId = folderId;
        loadCurrentFolder();
    }

    private void updatePathLabel() {
        String pathText = fileManagerData.getFolderPath(currentFolderId);
        currentPathLabel.setText(pathText);
    }

    private void refreshFolderPanel() {
        folderPanel.getChildren().clear();

        Label titleLabel = new Label("資料夾");
        titleLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-padding: 0 0 10 0;");

        VBox rootItem = createFolderTreeWithAllFolders();

        folderPanel.getChildren().addAll(titleLabel, rootItem);
    }

    private VBox createFolderTreeWithAllFolders() {
        return buildFolderTree("root", 0);
    }

    private VBox buildFolderTree(String parentId, int depth) {
        VBox container = new VBox();

        // 根目錄項目
        if (depth == 0) {
            HBox rootRow = new HBox(5);
            rootRow.setPadding(new Insets(5));
            rootRow.setAlignment(Pos.CENTER_LEFT);
            rootRow.setStyle("-fx-background-radius: 5px; -fx-cursor: hand;");

            if ("root".equals(currentFolderId)) {
                rootRow.setStyle(rootRow.getStyle() + "; -fx-background-color: #e3f2fd;");
            }

            Label rootLabel = new Label("📚 我的資料庫");
            rootLabel.setStyle("-fx-font-size: 14px;");

            rootRow.getChildren().add(rootLabel);
            rootRow.setOnMouseClicked(e -> {
                if (e.getButton() == MouseButton.PRIMARY) {
                    navigateToFolder("root");
                }
            });

            container.getChildren().add(rootRow);
        }

        // 子資料夾
        List<FolderItem> subFolders = fileManagerData.getSubFolders(parentId);
        for (FolderItem folder : subFolders) {
            HBox folderRow = new HBox(5);
            folderRow.setPadding(new Insets(5));
            folderRow.setAlignment(Pos.CENTER_LEFT);
            folderRow.setStyle("-fx-background-radius: 5px; -fx-cursor: hand;");

            // 縮排
            if (depth > 0) {
                folderRow.setPadding(new Insets(5, 5, 5, 5 + (depth * 20)));
            }

            if (folder.getId().equals(currentFolderId)) {
                folderRow.setStyle(folderRow.getStyle() + "; -fx-background-color: #e3f2fd;");
            }

            Label folderLabel = new Label("📁 " + folder.getName());
            folderLabel.setStyle("-fx-font-size: 14px;");

            folderRow.getChildren().add(folderLabel);
            folderRow.setOnMouseClicked(e -> {
                if (e.getButton() == MouseButton.PRIMARY) {
                    navigateToFolder(folder.getId());
                } else if (e.getButton() == MouseButton.SECONDARY) {
                    showFolderContextMenu(folderRow, folder.getId(), e.getScreenX(), e.getScreenY());
                }
            });

            container.getChildren().add(folderRow);

            // 遞迴載入子資料夾
            VBox subTree = buildFolderTree(folder.getId(), depth + 1);
            container.getChildren().add(subTree);
        }

        return container;
    }

    // 對話框和功能方法
    private void showImportDialog() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("選擇要匯入的檔案");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("支援的檔案", "*.pdf", "*.jpg", "*.jpeg", "*.png", "*.gif", "*.bmp"),
                new FileChooser.ExtensionFilter("PDF 檔案", "*.pdf"),
                new FileChooser.ExtensionFilter("圖片檔案", "*.jpg", "*.jpeg", "*.png", "*.gif", "*.bmp"),
                new FileChooser.ExtensionFilter("所有檔案", "*.*")
        );

        List<File> selectedFiles = fileChooser.showOpenMultipleDialog(primaryStage);
        if (selectedFiles != null && !selectedFiles.isEmpty()) {
            importFiles(selectedFiles);
        }
    }

    private void showImportProgress() {
        if (importProgressBar == null) {
            importProgressBar = new ProgressBar(0);
            importProgressBar.setPrefWidth(300);
            importProgressBar.setStyle("-fx-accent: #007ACC;");

            Label progressLabel = new Label("正在匯入檔案...");
            progressLabel.setStyle("-fx-text-fill: #666; -fx-font-size: 12px;");

            VBox progressBox = new VBox(5);
            progressBox.setAlignment(Pos.CENTER);
            progressBox.getChildren().addAll(progressLabel, importProgressBar);
            progressBox.setPadding(new Insets(10));
            progressBox.setStyle("-fx-background-color: rgba(255,255,255,0.9); -fx-background-radius: 5;");

            StackPane overlay = new StackPane(progressBox);
            overlay.setStyle("-fx-background-color: rgba(0,0,0,0.3);");

            mainLayout.setCenter(overlay);
        }
    }

    private void updateImportProgress(double progress) {
        if (importProgressBar != null) {
            importProgressBar.setProgress(progress);
        }
    }

    private void hideImportProgress() {
        importProgressBar = null;
        mainLayout.setCenter(fileViewScrollPane);
    }

    private void importFiles(List<File> files) {
        statusLabel.setText("匯入中...");
        showImportProgress();

        Thread importThread = new Thread(() -> {
            int successCount = 0;
            int totalFiles = files.size();

            for (int i = 0; i < files.size(); i++) {
                File file = files.get(i);
                try {
                    if (fileManagerData.importFile(file, currentFolderId)) {
                        successCount++;
                    }
                } catch (Exception e) {
                    Platform.runLater(() ->
                            showError("匯入失敗", "無法匯入檔案 " + file.getName() + ": " + e.getMessage()));
                }

                // 更新進度
                final double progress = (double) (i + 1) / totalFiles;
                Platform.runLater(() -> updateImportProgress(progress));
            }

            final int finalSuccessCount = successCount;
            Platform.runLater(() -> {
                hideImportProgress();
                loadCurrentFolder();
                statusLabel.setText("成功匯入 " + finalSuccessCount + " 個檔案");

                if (finalSuccessCount > 0) {
                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                    alert.setTitle("匯入完成");
                    alert.setHeaderText(null);
                    alert.setContentText("成功匯入 " + finalSuccessCount + " 個檔案到當前資料夾");
                    alert.showAndWait();
                }
            });
        });

        importThread.setDaemon(true);
        importThread.start();
    }

    private void showNewFolderDialog() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("新增資料夾");
        dialog.setHeaderText("在當前位置建立新資料夾");
        dialog.setContentText("資料夾名稱:");

        Optional<String> result = dialog.showAndWait();
        if (result.isPresent() && !result.get().trim().isEmpty()) {
            String folderName = result.get().trim();

            if (fileManagerData.createFolder(folderName, currentFolderId)) {
                loadCurrentFolder();
                statusLabel.setText("已建立資料夾: " + folderName);
            } else {
                showError("建立失敗", "無法建立資料夾，可能名稱已存在或包含無效字符");
            }
        }
    }

    private void showFolderContextMenu(javafx.scene.Node source, String folderId, double x, double y) {
        if ("root".equals(folderId)) return; // 根目錄不顯示右鍵選單

        ContextMenu contextMenu = new ContextMenu();

        MenuItem openItem = new MenuItem("開啟");
        openItem.setOnAction(e -> navigateToFolder(folderId));

        MenuItem renameItem = new MenuItem("重新命名");
        renameItem.setOnAction(e -> renameFolderDialog(folderId));

        MenuItem deleteItem = new MenuItem("刪除");
        deleteItem.setOnAction(e -> deleteFolderDialog(folderId));

        contextMenu.getItems().addAll(openItem, renameItem, new SeparatorMenuItem(), deleteItem);
        contextMenu.show(source, x, y);
    }

    private void showFileContextMenu(javafx.scene.Node source, FileItem file, double x, double y) {
        ContextMenu contextMenu = new ContextMenu();

        MenuItem openItem = new MenuItem("開啟");
        openItem.setOnAction(e -> openFile(file));

        MenuItem renameItem = new MenuItem("重新命名");
        renameItem.setOnAction(e -> renameFileDialog(file));

        MenuItem moveItem = new MenuItem("移動到...");
        moveItem.setOnAction(e -> moveFileDialog(file));

        MenuItem deleteItem = new MenuItem("刪除");
        deleteItem.setOnAction(e -> deleteFileDialog(file));

        contextMenu.getItems().addAll(openItem, renameItem, moveItem, new SeparatorMenuItem(), deleteItem);
        contextMenu.show(source, x, y);
    }

    private void renameFolderDialog(String folderId) {
        FolderItem folder = fileManagerData.getFolder(folderId);
        if (folder == null) return;

        TextInputDialog dialog = new TextInputDialog(folder.getName());
        dialog.setTitle("重新命名資料夾");
        dialog.setHeaderText("輸入新的資料夾名稱");
        dialog.setContentText("名稱:");

        Optional<String> result = dialog.showAndWait();
        if (result.isPresent() && !result.get().trim().isEmpty()) {
            String newName = result.get().trim();

            if (fileManagerData.renameFolder(folderId, newName)) {
                loadCurrentFolder();
                statusLabel.setText("已重新命名資料夾: " + newName);
            } else {
                showError("重新命名失敗", "無法重新命名資料夾，可能名稱已存在或包含無效字符");
            }
        }
    }

    private void deleteFolderDialog(String folderId) {
        FolderItem folder = fileManagerData.getFolder(folderId);
        if (folder == null) return;

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("刪除資料夾");
        alert.setHeaderText("確定要刪除這個資料夾嗎？");
        alert.setContentText("資料夾「" + folder.getName() + "」及其所有內容都將被永久刪除。\n\n此操作無法復原！");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            if (fileManagerData.deleteFolder(folderId)) {
                // 如果刪除的是當前資料夾，導航到父資料夾
                if (folderId.equals(currentFolderId)) {
                    String parentId = fileManagerData.getParentFolderId(folderId);
                    navigateToFolder(parentId != null ? parentId : "root");
                } else {
                    loadCurrentFolder();
                }
                statusLabel.setText("已刪除資料夾: " + folder.getName());
            } else {
                showError("刪除失敗", "無法刪除資料夾，可能包含無法刪除的檔案");
            }
        }
    }

    private void renameFileDialog(FileItem file) {
        String nameWithoutExt = file.getName().substring(0, file.getName().lastIndexOf('.'));
        String extension = file.getName().substring(file.getName().lastIndexOf('.'));

        TextInputDialog dialog = new TextInputDialog(nameWithoutExt);
        dialog.setTitle("重新命名檔案");
        dialog.setHeaderText("輸入新的檔案名稱");
        dialog.setContentText("名稱 (不含副檔名):");

        Optional<String> result = dialog.showAndWait();
        if (result.isPresent() && !result.get().trim().isEmpty()) {
            String newName = result.get().trim() + extension;

            if (fileManagerData.renameFile(file.getId(), newName)) {
                loadCurrentFolder();
                statusLabel.setText("已重新命名檔案: " + newName);
            } else {
                showError("重新命名失敗", "無法重新命名檔案，可能名稱已存在或包含無效字符");
            }
        }
    }

    private void moveFileDialog(FileItem file) {
        // 創建資料夾選擇對話框
        Dialog<String> dialog = new Dialog<>();
        dialog.setTitle("移動檔案");
        dialog.setHeaderText("選擇要移動到的資料夾");

        // 創建資料夾樹
        TreeView<FolderTreeItem> treeView = new TreeView<>();
        TreeItem<FolderTreeItem> rootItem = new TreeItem<>(new FolderTreeItem("我的資料庫", "root"));
        rootItem.setExpanded(true);
        treeView.setRoot(rootItem);

        buildFolderTreeForDialog(rootItem, "root");

        treeView.setPrefSize(400, 300);
        dialog.getDialogPane().setContent(treeView);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        // 設置結果轉換器
        dialog.setResultConverter(buttonType -> {
            if (buttonType == ButtonType.OK) {
                TreeItem<FolderTreeItem> selectedItem = treeView.getSelectionModel().getSelectedItem();
                if (selectedItem != null) {
                    return selectedItem.getValue().getId();
                }
            }
            return null;
        });

        Optional<String> result = dialog.showAndWait();
        if (result.isPresent()) {
            String targetFolderId = result.get();

            if (fileManagerData.moveFile(file.getId(), targetFolderId)) {
                loadCurrentFolder();
                statusLabel.setText("已移動檔案: " + file.getName());
            } else {
                showError("移動失敗", "無法移動檔案到目標資料夾");
            }
        }
    }

    private void buildFolderTreeForDialog(TreeItem<FolderTreeItem> parentItem, String parentId) {
        List<FolderItem> subFolders = fileManagerData.getSubFolders(parentId);
        for (FolderItem folder : subFolders) {
            TreeItem<FolderTreeItem> folderItem = new TreeItem<>(new FolderTreeItem(folder.getName(), folder.getId()));
            parentItem.getChildren().add(folderItem);
            buildFolderTreeForDialog(folderItem, folder.getId());
        }
    }

    private void deleteFileDialog(FileItem file) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("刪除檔案");
        alert.setHeaderText("確定要刪除這個檔案嗎？");
        alert.setContentText("檔案「" + file.getName() + "」將被永久刪除。\n\n此操作無法復原！");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            if (fileManagerData.deleteFile(file.getId())) {
                loadCurrentFolder();
                statusLabel.setText("已刪除檔案: " + file.getName());
            } else {
                showError("刪除失敗", "無法刪除檔案");
            }
        }
    }

    private void openFile(FileItem file) {
        if (fileOpenCallback != null) {
            File physicalFile = new File(file.getFilePath());
            if (physicalFile.exists()) {
                fileOpenCallback.onFileOpen(physicalFile);
                // 關閉檔案管理器視窗，回到閱讀器
                primaryStage.close();
            } else {
                showError("開啟失敗", "檔案不存在或已被移動");
            }
        }
    }

    private void filterFiles(String searchText) {
        if (searchText == null || searchText.trim().isEmpty()) {
            // 顯示所有檔案
            refreshFileView();
            return;
        }

        String lowerSearchText = searchText.toLowerCase();

        // 過濾資料夾
        List<FolderItem> filteredFolders = currentFolders.stream()
                .filter(folder -> folder.getName().toLowerCase().contains(lowerSearchText))
                .collect(Collectors.toList());

        // 過濾檔案
        List<FileItem> filteredFiles = currentFiles.stream()
                .filter(file -> file.getName().toLowerCase().contains(lowerSearchText))
                .collect(Collectors.toList());

        // 更新檢視
        fileGrid.getChildren().clear();

        int column = 0;
        int row = 0;
        int maxColumns = 5;

        // 顯示過濾後的資料夾
        for (FolderItem folder : filteredFolders) {
            VBox folderCard = createFolderCard(folder);
            fileGrid.add(folderCard, column, row);

            column++;
            if (column >= maxColumns) {
                column = 0;
                row++;
            }
        }

        // 顯示過濾後的檔案
        for (FileItem file : filteredFiles) {
            VBox fileCard = createFileCard(file);
            fileGrid.add(fileCard, column, row);

            column++;
            if (column >= maxColumns) {
                column = 0;
                row++;
            }
        }

        statusLabel.setText("找到 " + filteredFiles.size() + " 個檔案，" + filteredFolders.size() + " 個資料夾");
    }

    private void sortAndRefreshFiles() {
        String sortOption = sortComboBox.getValue();

        switch (sortOption) {
            case "名稱 (A-Z)":
                currentFiles.sort(Comparator.comparing(FileItem::getName));
                currentFolders.sort(Comparator.comparing(FolderItem::getName));
                break;
            case "名稱 (Z-A)":
                currentFiles.sort(Comparator.comparing(FileItem::getName).reversed());
                currentFolders.sort(Comparator.comparing(FolderItem::getName).reversed());
                break;
            case "修改時間 (新到舊)":
                currentFiles.sort(Comparator.comparing(FileItem::getLastModified).reversed());
                currentFolders.sort(Comparator.comparing(FolderItem::getCreatedDate).reversed());
                break;
            case "修改時間 (舊到新)":
                currentFiles.sort(Comparator.comparing(FileItem::getLastModified));
                currentFolders.sort(Comparator.comparing(FolderItem::getCreatedDate));
                break;
            case "檔案大小":
                currentFiles.sort(Comparator.comparing(FileItem::getSize).reversed());
                break;
        }

        refreshFileView();
    }

    // 錯誤對話框顯示方法
    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    // 顯示檔案管理器
    public void show() {
        Scene scene = new Scene(mainLayout);
        primaryStage.setScene(scene);
        primaryStage.setTitle("E_Reader - 檔案管理器");
        primaryStage.show();

        // 載入初始資料
        loadCurrentFolder();
    }

    // 內部類別
    private static class FolderTreeItem {
        private final String name;
        private final String id;

        public FolderTreeItem(String name, String id) {
            this.name = name;
            this.id = id;
        }

        public String getName() { return name; }
        public String getId() { return id; }

        @Override
        public String toString() {
            return name;
        }
    }

    // 公共方法
    public void setFileOpenCallback(FileOpenCallback callback) {
        this.fileOpenCallback = callback;
    }

    public void hide() {
        primaryStage.hide();
    }

    public Stage getStage() {
        return primaryStage;
    }

    // Getter 方法
    public String getCurrentFolderId() {
        return currentFolderId;
    }

    public Path getLibraryPath() {
        return libraryPath;
    }

    public FileManagerData getFileManagerData() {
        return fileManagerData;
    }
}