package E_Reader.filemanager;

import E_Reader.ui.MainController;
import E_Reader.settings.SettingsManager;
import E_Reader.core.TextExtractor;
import E_Reader.utils.AlertHelper;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.*;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.util.Optional;

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

    // 單例模式
    private static FileManagerController instance;

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
    private boolean isGridView = true; // 檢視模式：true=網格，false=清單
    private TreeView<FolderTreeItem> folderTreeView; // 資料夾樹狀檢視

    // 控制樹狀檢視刷新
    private boolean isRefreshingTree = false;

    // 回調函數
    private FileOpenCallback fileOpenCallback;
    
    // 設定相關
    private SettingsManager settingsManager;
    private TextExtractor textExtractor;

    public interface FileOpenCallback {
        void onFileOpen(File file);
    }

    public FileManagerController() {
        this.primaryStage = new Stage();

        // 嘗試在用戶主目錄創建，如果失敗則在程式目錄創建
        Path userHomeLibrary = Paths.get(System.getProperty("user.home"), "E_Reader_Library");
        Path currentDirLibrary = Paths.get("E_Reader_Library");

        Path chosenPath = null;
        try {
            Files.createDirectories(userHomeLibrary);
            chosenPath = userHomeLibrary;
            System.out.println("Using library path: " + userHomeLibrary.toString());
        } catch (IOException e) {
            System.err.println("無法在用戶主目錄創建檔案庫: " + e.getMessage());
            try {
                Files.createDirectories(currentDirLibrary);
                chosenPath = currentDirLibrary;
                System.out.println("Using fallback library path: " + currentDirLibrary.toAbsolutePath().toString());
            } catch (IOException e2) {
                System.err.println("無法創建檔案庫目錄: " + e2.getMessage());
                chosenPath = currentDirLibrary; // 使用預設路徑
            }
        }

        this.libraryPath = chosenPath;
        this.fileManagerData = new FileManagerData(libraryPath);
        
        // 設定管理器將從MainController傳入
    }

    public static FileManagerController getInstance() {
        if (instance == null) {
            instance = new FileManagerController();
        }
        return instance;
    }

    public void initialize(FileOpenCallback callback) {
        this.fileOpenCallback = callback;
        setupMainLayout();
        setupEventHandlers();

        // 創建測試資料夾
        createTestFoldersIfNeeded();

        loadCurrentFolder();
    }

    private void createTestFoldersIfNeeded() {
        try {
            // 創建一些基本資料夾來測試
            if (fileManagerData.getFolders("root").isEmpty()) {
                System.out.println("Creating test folders...");

                // 創建 PDF文件資料夾
                if (fileManagerData.createFolder("PDF文件", "root")) {
                    System.out.println("Created PDF文件 folder");
                }

                // 創建 EPUB電子書資料夾
                if (fileManagerData.createFolder("電子書", "root")) {
                    System.out.println("Created 電子書 folder");
                }

                // 創建圖片資料夾
                if (fileManagerData.createFolder("圖片", "root")) {
                    System.out.println("Created 圖片 folder");
                }

//                // 創建測試資料夾
//                if (fileManagerData.createFolder("測試資料夾", "root")) {
//                    System.out.println("Created 測試資料夾 folder");
//                }

                System.out.println("Test folders created");
            }
        } catch (Exception e) {
            System.err.println("Error creating test folders: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void setupMainLayout() {
        mainLayout = new BorderPane();
        mainLayout.setPrefSize(1000, 700);

        // 頂部工具欄
        VBox topSection = createTopSection();
        mainLayout.setTop(topSection);

        // 左側資料夾面板（使用TreeView）
        folderTreeView = createFolderTreeView();
        ScrollPane folderScrollPane = new ScrollPane(folderTreeView);
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
        HBox bottomBar = createBottomBar();
        mainLayout.setBottom(bottomBar);

        // 套用樣式
        mainLayout.setStyle("-fx-background-color: white;");
    }
    
    /**
     * 建立底部狀態欄（包含設定按鈕）
     */
    private HBox createBottomBar() {
        HBox bottomBar = new HBox();
        bottomBar.setAlignment(Pos.CENTER_LEFT);
        bottomBar.setStyle("-fx-background-color: #e9ecef; -fx-border-color: #dee2e6; -fx-border-width: 1 0 0 0;");
        
        // 狀態標籤
        statusLabel = new Label("就緒");
        statusLabel.setPadding(new Insets(5, 10, 5, 10));
        
        // 在左下角添加設定按鈕
        Button settingsBtn = new Button("⚙️ OCR設定");
        settingsBtn.setStyle("-fx-background-color: #6c757d; -fx-text-fill: white; -fx-font-weight: bold; " +
                            "-fx-padding: 6 12; -fx-font-size: 12px; -fx-background-radius: 4px;");
        settingsBtn.setTooltip(new Tooltip("OCR文字識別設定"));
        settingsBtn.setOnAction(e -> showOcrSettingsDialog());
        
        // 懸停效果
        settingsBtn.setOnMouseEntered(e -> 
            settingsBtn.setStyle(settingsBtn.getStyle() + "; -fx-background-color: #5a6268;"));
        settingsBtn.setOnMouseExited(e -> 
            settingsBtn.setStyle(settingsBtn.getStyle().replace("; -fx-background-color: #5a6268", "")));
        
        // 使用HBox.setHgrow讓狀態標籤占滿剩餘空間
        HBox.setHgrow(statusLabel, Priority.ALWAYS);
        
        bottomBar.getChildren().addAll(statusLabel, settingsBtn);
        bottomBar.setPadding(new Insets(0, 10, 0, 0)); // 右邊留一些空間
        
        return bottomBar;
    }

    private VBox createTopSection() {
        VBox topSection = new VBox();

        // 工具欄
        HBox toolbar = new HBox(10);
        toolbar.setPadding(new Insets(10));
        toolbar.setAlignment(Pos.CENTER_LEFT);
        toolbar.setStyle("-fx-background-color: #f8f9fa; -fx-border-color: #dee2e6; -fx-border-width: 0 0 1 0;");

        // 匯入檔案按鈕（匯入到當前資料夾）
        Button importFileBtn = new Button("📁 匯入檔案");
        importFileBtn.setStyle("-fx-background-color: #007bff; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 10 20; -fx-font-size: 14px;");
        importFileBtn.setOnAction(e -> showImportToCurrentFolderDialog());
        importFileBtn.setTooltip(new Tooltip("選擇檔案並匯入到當前資料夾"));

        // 匯入資料夾按鈕（專門用於匯入整個資料夾）
        Button importFolderBtn = new Button("📂 匯入資料夾");
        importFolderBtn.setStyle("-fx-background-color: #28a745; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 10 20; -fx-font-size: 14px;");
        importFolderBtn.setOnAction(e -> showImportFolderDialog());
        importFolderBtn.setTooltip(new Tooltip("選擇整個資料夾匯入到當前位置"));

        // 快速匯入按鈕（保留原有的分類匯入功能）
        MenuButton quickImportBtn = new MenuButton("⚡ 快速匯入");
        quickImportBtn.setStyle("-fx-background-color: #6c757d; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 8 16; -fx-font-size: 12px;");
        
        MenuItem importPdfItem = new MenuItem("📄 PDF到PDF文件資料夾");
        importPdfItem.setOnAction(e -> showImportPdfDialog());
        
        MenuItem importEpubItem = new MenuItem("📚 EPUB到電子書資料夾");
        importEpubItem.setOnAction(e -> showImportEpubDialog());
        
        MenuItem importImageItem = new MenuItem("🖼️ 圖片到圖片資料夾");
        importImageItem.setOnAction(e -> showImportImageDialog());
        
        quickImportBtn.getItems().addAll(importPdfItem, importEpubItem, importImageItem);

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

        // 檢視模式切換事件
        gridViewBtn.setOnAction(e -> {
            isGridView = true;
            refreshFileView();
        });
        listViewBtn.setOnAction(e -> {
            isGridView = false;
            refreshFileView();
        });

        toolbar.getChildren().addAll(
                importFileBtn, importFolderBtn, quickImportBtn,
                new Separator(), newFolderBtn, refreshBtn,
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

    private TreeView<FolderTreeItem> createFolderTreeView() {
        TreeView<FolderTreeItem> treeView = new TreeView<>();
        TreeItem<FolderTreeItem> rootItem = new TreeItem<>(new FolderTreeItem("📚 我的資料庫", "root"));

        rootItem.setExpanded(true);
        treeView.setRoot(rootItem);
        treeView.setShowRoot(true);

        // 設定樣式
        treeView.setStyle("-fx-background-color: #f5f5f5;");

        // 設定CellFactory僅處理顯示
        treeView.setCellFactory(tv -> {
            TreeCell<FolderTreeItem> cell = new TreeCell<>() {
                @Override
                protected void updateItem(FolderTreeItem item, boolean empty) {
                    super.updateItem(item, empty);
                    setText(empty || item == null ? null : item.getName());
                }
            };
            return cell;
        });

        // 只處理點擊事件，不處理展開事件
        treeView.setOnMouseClicked(e -> {
            TreeItem<FolderTreeItem> selectedItem = treeView.getSelectionModel().getSelectedItem();
            if (selectedItem != null && e.getClickCount() == 1) {
                String folderId = selectedItem.getValue().getId();
                if (!"loading".equals(folderId)) {
                    navigateToFolder(folderId);
                }
            }
        });

        return treeView;
    }

    private void loadChildFolders(TreeItem<FolderTreeItem> parentItem) {
        if (parentItem == null || parentItem.getValue() == null) {
            return;
        }

        FolderTreeItem parentValue = parentItem.getValue();
        String parentId = parentValue.getId();

        // 如果已經有子項目且不是載入中節點，就不要重複載入
        if (!parentItem.getChildren().isEmpty()) {
            boolean hasOnlyLoadingNode = parentItem.getChildren().size() == 1 &&
                    parentItem.getChildren().get(0).getValue() != null &&
                    "loading".equals(parentItem.getChildren().get(0).getValue().getId());

            if (!hasOnlyLoadingNode) {
                System.out.println("Parent " + parentId + " already has real children, skipping load");
                return;
            }
        }

        // 清除載入中節點
        parentItem.getChildren().clear();

        try {
            List<FolderItem> subFolders = fileManagerData.getSubFolders(parentId);
            System.out.println("Loading folders for parent: " + parentId + ", found: " + subFolders.size() + " folders");

            for (FolderItem folder : subFolders) {
                TreeItem<FolderTreeItem> childItem = new TreeItem<>(
                        new FolderTreeItem("📁 " + folder.getName(), folder.getId())
                );

                // 檢查是否有子資料夾，如果有則加 placeholder
                List<FolderItem> grandChildren = fileManagerData.getSubFolders(folder.getId());
                if (!grandChildren.isEmpty()) {
                    childItem.getChildren().add(new TreeItem<>(
                            new FolderTreeItem("載入中...", "loading")
                    ));
                }

                // 為每個子項目添加展開監聽器（只添加一次）
                addExpandListener(childItem);

                parentItem.getChildren().add(childItem);
                System.out.println("Added folder: " + folder.getName() + " with ID: " + folder.getId());
            }

            System.out.println("Folder panel refreshed. Root has " + parentItem.getChildren().size() + " children");
        } catch (Exception e) {
            System.err.println("Error loading child folders: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void addExpandListener(TreeItem<FolderTreeItem> item) {
        // 確保每個項目只有一個監聽器
        item.expandedProperty().addListener((obs, wasExpanded, isExpanded) -> {
            if (isExpanded && !isRefreshingTree) {
                // 檢查是否需要載入子節點
                boolean needsLoading = item.getChildren().isEmpty() ||
                        (item.getChildren().size() == 1 &&
                                item.getChildren().get(0).getValue() != null &&
                                "loading".equals(item.getChildren().get(0).getValue().getId()));

                if (needsLoading) {
                    loadChildFoldersLazily(item);
                }
            }
        });
    }

    private void loadChildFoldersLazily(TreeItem<FolderTreeItem> parentItem) {
        if (parentItem == null || parentItem.getValue() == null || isRefreshingTree) {
            return;
        }

        String parentId = parentItem.getValue().getId();
        System.out.println("Loading child folders for: " + parentId);

        // 檢查是否已經載入過（避免重複載入）
        boolean hasRealChildren = false;

        for (TreeItem<FolderTreeItem> child : parentItem.getChildren()) {
            if (child.getValue() != null && !"loading".equals(child.getValue().getId())) {
                hasRealChildren = true;
                break;
            }
        }

        // 如果已經有真實的子項目，就不需要再載入
        if (hasRealChildren) {
            System.out.println("Already has real children, skipping load for: " + parentId);
            return;
        }

        // 移除載入中節點
        parentItem.getChildren().removeIf(child ->
                child.getValue() != null && "loading".equals(child.getValue().getId()));

        try {
            List<FolderItem> subFolders = fileManagerData.getSubFolders(parentId);
            System.out.println("Found " + subFolders.size() + " subfolders for: " + parentId);

            for (FolderItem folder : subFolders) {
                TreeItem<FolderTreeItem> childItem =
                        new TreeItem<>(new FolderTreeItem("📁 " + folder.getName(), folder.getId()));

                // 檢查是否有子資料夾，如果有則加入載入中節點
                List<FolderItem> grandChildren = fileManagerData.getSubFolders(folder.getId());
                if (!grandChildren.isEmpty()) {
                    childItem.getChildren().add(new TreeItem<>(new FolderTreeItem("載入中...", "loading")));
                }

                // 為新節點添加展開監聽器
                addExpandListener(childItem);

                parentItem.getChildren().add(childItem);
                System.out.println("Added child folder: " + folder.getName());
            }
        } catch (Exception e) {
            System.err.println("Error loading child folders for " + parentId + ": " + e.getMessage());
            e.printStackTrace();
        }
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

            // 只在必要時重新整理資料夾面板
            if (folderTreeView.getRoot().getChildren().isEmpty()) {
                refreshFolderPanel();
            } else {
                // 只更新選中狀態，不重新建立樹
                updateTreeSelection();
            }

            statusLabel.setText("已載入 " + currentFiles.size() + " 個檔案，" + currentFolders.size() + " 個資料夾");
        });
    }

    private void updateTreeSelection() {
        TreeItem<FolderTreeItem> root = folderTreeView.getRoot();
        if (root != null) {
            selectFolderInTree(root, currentFolderId);
        }
    }

    private boolean selectFolderInTree(TreeItem<FolderTreeItem> item, String targetFolderId) {
        if (item.getValue() != null && item.getValue().getId().equals(targetFolderId)) {
            folderTreeView.getSelectionModel().select(item);
            return true;
        }

        for (TreeItem<FolderTreeItem> child : item.getChildren()) {
            if (selectFolderInTree(child, targetFolderId)) {
                return true;
            }
        }

        return false;
    }

    private void refreshFileView() {
        fileGrid.getChildren().clear();

        if (isGridView) {
            refreshGridView();
        } else {
            refreshListView();
        }
    }

    private void refreshGridView() {
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

    private void refreshListView() {
        fileGrid.setHgap(0);
        fileGrid.setVgap(2);

        int row = 0;

        // 先顯示資料夾
        for (FolderItem folder : currentFolders) {
            HBox folderRow = createFolderListItem(folder);
            fileGrid.add(folderRow, 0, row);
            row++;
        }

        // 再顯示檔案
        for (FileItem file : currentFiles) {
            HBox fileRow = createFileListItem(file);
            fileGrid.add(fileRow, 0, row);
            row++;
        }

        // 重置間距設定
        fileGrid.setHgap(15);
        fileGrid.setVgap(15);
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

    private HBox createFolderListItem(FolderItem folder) {
        HBox row = new HBox(10);
        row.setPrefWidth(600);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(8));
        row.setStyle("-fx-background-color: white; -fx-border-color: #e0e0e0; -fx-border-width: 0 0 1 0; -fx-cursor: hand;");

        // 資料夾圖示
        Label iconLabel = new Label("📁");
        iconLabel.setStyle("-fx-font-size: 20px;");
        iconLabel.setPrefWidth(30);

        // 資料夾名稱
        Label nameLabel = new Label(folder.getName());
        nameLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");
        nameLabel.setPrefWidth(200);

        // 檔案數量
        int fileCount = fileManagerData.getFileCount(folder.getId());
        Label countLabel = new Label(fileCount + " 個項目");
        countLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #666666;");
        countLabel.setPrefWidth(100);

        // 創建日期
        Label dateLabel = new Label(folder.getCreatedDate().format(DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm")));
        dateLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #666666;");

        row.getChildren().addAll(iconLabel, nameLabel, countLabel, dateLabel);

        // 懸停效果
        row.setOnMouseEntered(e -> row.setStyle(row.getStyle() + "; -fx-background-color: #f8f9fa;"));
        row.setOnMouseExited(e -> row.setStyle(row.getStyle().replace("; -fx-background-color: #f8f9fa", "")));

        // 點擊事件
        row.setOnMouseClicked(e -> {
            if (e.getButton() == MouseButton.PRIMARY) {
                if (e.getClickCount() == 2) {
                    navigateToFolder(folder.getId());
                }
            } else if (e.getButton() == MouseButton.SECONDARY) {
                showFolderContextMenu(row, folder.getId(), e.getScreenX(), e.getScreenY());
            }
        });

        return row;
    }

    private HBox createFileListItem(FileItem file) {
        HBox row = new HBox(10);
        row.setPrefWidth(600);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(8));
        row.setStyle("-fx-background-color: white; -fx-border-color: #e0e0e0; -fx-border-width: 0 0 1 0; -fx-cursor: hand;");

        // 檔案圖示
        Label iconLabel = new Label(getFileIcon(file.getExtension()));
        iconLabel.setStyle("-fx-font-size: 20px;");
        iconLabel.setPrefWidth(30);

        // 檔案名稱
        Label nameLabel = new Label(file.getName());
        nameLabel.setStyle("-fx-font-size: 14px;");
        nameLabel.setPrefWidth(250);

        // 檔案大小
        Label sizeLabel = new Label(formatFileSize(file.getSize()));
        sizeLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #666666;");
        sizeLabel.setPrefWidth(80);

        // 修改日期
        Label dateLabel = new Label(file.getLastModified().format(DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm")));
        dateLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #666666;");

        row.getChildren().addAll(iconLabel, nameLabel, sizeLabel, dateLabel);

        // 懸停效果
        row.setOnMouseEntered(e -> row.setStyle(row.getStyle() + "; -fx-background-color: #f8f9fa;"));
        row.setOnMouseExited(e -> row.setStyle(row.getStyle().replace("; -fx-background-color: #f8f9fa", "")));

        // 點擊事件
        row.setOnMouseClicked(e -> {
            if (e.getButton() == MouseButton.PRIMARY) {
                if (e.getClickCount() == 2) {
                    openFile(file);
                }
            } else if (e.getButton() == MouseButton.SECONDARY) {
                showFileContextMenu(row, file, e.getScreenX(), e.getScreenY());
            }
        });

        return row;
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
            case "epub": return "📚";
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
        Platform.runLater(() -> {
            try {
                isRefreshingTree = true; // 設置刷新標誌
                System.out.println("Refreshing folder panel...");

                // 保存當前展開狀態
                Set<String> expandedFolders = new HashSet<>();
                saveExpandedState(folderTreeView.getRoot(), expandedFolders);

                // 重新創建樹狀結構
                TreeItem<FolderTreeItem> rootItem = new TreeItem<>(new FolderTreeItem("📚 我的資料庫", "root"));
                rootItem.setExpanded(true);
                folderTreeView.setRoot(rootItem);

                // 載入子資料夾
                loadChildFolders(rootItem);

                // 恢復展開狀態
                restoreExpandedState(rootItem, expandedFolders);

                // 選擇當前資料夾
                selectCurrentFolderInTree(rootItem, currentFolderId);

                System.out.println("Folder panel refreshed. Root has " + rootItem.getChildren().size() + " children");
            } catch (Exception e) {
                System.err.println("更新資料夾面板時發生錯誤: " + e.getMessage());
                e.printStackTrace();
            } finally {
                isRefreshingTree = false; // 清除刷新標誌
            }
        });
    }

    private void saveExpandedState(TreeItem<FolderTreeItem> item, Set<String> expandedFolders) {
        if (item != null && item.getValue() != null && item.isExpanded()) {
            expandedFolders.add(item.getValue().getId());
            for (TreeItem<FolderTreeItem> child : item.getChildren()) {
                saveExpandedState(child, expandedFolders);
            }
        }
    }

    private void restoreExpandedState(TreeItem<FolderTreeItem> item, Set<String> expandedFolders) {
        if (item != null && item.getValue() != null) {
            String folderId = item.getValue().getId();
            if (expandedFolders.contains(folderId)) {
                item.setExpanded(true);
                // 如果需要展開但沒有子項目，則載入
                if (item.getChildren().isEmpty() ||
                        (item.getChildren().size() == 1 &&
                                item.getChildren().get(0).getValue() != null &&
                                "loading".equals(item.getChildren().get(0).getValue().getId()))) {
                    loadChildFoldersLazily(item);
                }
            }

            for (TreeItem<FolderTreeItem> child : item.getChildren()) {
                restoreExpandedState(child, expandedFolders);
            }
        }
    }

    private boolean selectCurrentFolderInTree(TreeItem<FolderTreeItem> item, String targetFolderId) {
        if (item.getValue().getId().equals(targetFolderId)) {
            folderTreeView.getSelectionModel().select(item);
            return true;
        }

        for (TreeItem<FolderTreeItem> child : item.getChildren()) {
            if (selectCurrentFolderInTree(child, targetFolderId)) {
                return true;
            }
        }

        return false;
    }

    // 對話框和功能方法
    
    /**
     * 顯示匯入檔案到當前資料夾的對話框
     */
    private void showImportToCurrentFolderDialog() {
        try {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("選擇要匯入的檔案");
            fileChooser.getExtensionFilters().addAll(
                    new FileChooser.ExtensionFilter("所有支援的檔案", "*.pdf", "*.epub", "*.jpg", "*.jpeg", "*.png", "*.gif", "*.bmp", "*.tiff", "*.webp", "*.txt", "*.doc", "*.docx"),
                    new FileChooser.ExtensionFilter("PDF 檔案", "*.pdf"),
                    new FileChooser.ExtensionFilter("EPUB 檔案", "*.epub"),
                    new FileChooser.ExtensionFilter("圖片檔案", "*.jpg", "*.jpeg", "*.png", "*.gif", "*.bmp", "*.tiff", "*.webp"),
                    new FileChooser.ExtensionFilter("文字檔案", "*.txt", "*.doc", "*.docx"),
                    new FileChooser.ExtensionFilter("所有檔案", "*.*")
            );

            // 設定初始目錄
            String userHome = System.getProperty("user.home");
            File initialDir = new File(userHome, "Desktop");
            if (!initialDir.exists()) {
                initialDir = new File(userHome);
            }
            fileChooser.setInitialDirectory(initialDir);

            List<File> selectedFiles = fileChooser.showOpenMultipleDialog(primaryStage);
            if (selectedFiles != null && !selectedFiles.isEmpty()) {
                // 顯示確認對話框
                String currentFolderName = getCurrentFolderDisplayName();
                Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
                confirmAlert.setTitle("確認匯入");
                confirmAlert.setHeaderText("確認匯入檔案到當前資料夾");
                confirmAlert.setContentText("將匯入 " + selectedFiles.size() + " 個檔案到 '" + currentFolderName + "' 資料夾。\n\n這會複製檔案到您的資料庫中。");

                Optional<ButtonType> result = confirmAlert.showAndWait();
                if (result.isPresent() && result.get() == ButtonType.OK) {
                    importFilesToCurrentFolder(selectedFiles);
                }
            }
        } catch (Exception e) {
            showError("匯入錯誤", "開啟檔案選擇器時發生錯誤: " + e.getMessage());
        }
    }
    
    /**
     * 顯示匯入整個資料夾的對話框
     */
    private void showImportFolderDialog() {
        try {
            javafx.stage.DirectoryChooser directoryChooser = new javafx.stage.DirectoryChooser();
            directoryChooser.setTitle("選擇要匯入的資料夾");
            
            // 設定初始目錄
            String userHome = System.getProperty("user.home");
            File initialDir = new File(userHome, "Desktop");
            if (!initialDir.exists()) {
                initialDir = new File(userHome);
            }
            directoryChooser.setInitialDirectory(initialDir);

            File selectedDirectory = directoryChooser.showDialog(primaryStage);
            if (selectedDirectory != null && selectedDirectory.exists() && selectedDirectory.isDirectory()) {
                // 計算資料夾中的檔案數量
                int fileCount = countFilesInDirectory(selectedDirectory);
                
                // 顯示確認對話框
                String currentFolderName = getCurrentFolderDisplayName();
                Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
                confirmAlert.setTitle("確認匯入資料夾");
                confirmAlert.setHeaderText("確認匯入整個資料夾");
                confirmAlert.setContentText("將匯入資料夾 '" + selectedDirectory.getName() + "' 及其所有內容（約 " + fileCount + " 個檔案）到 '" + currentFolderName + "' 資料夾。\n\n這會複製整個資料夾結構到您的資料庫中。");

                Optional<ButtonType> result = confirmAlert.showAndWait();
                if (result.isPresent() && result.get() == ButtonType.OK) {
                    importDirectoryToCurrentFolder(selectedDirectory);
                }
            }
        } catch (Exception e) {
            showError("匯入錯誤", "開啟資料夾選擇器時發生錯誤: " + e.getMessage());
        }
    }
    
    /**
     * 取得當前資料夾的顯示名稱
     */
    private String getCurrentFolderDisplayName() {
        if ("root".equals(currentFolderId)) {
            return "根目錄";
        }
        FolderItem currentFolder = fileManagerData.getFolder(currentFolderId);
        return currentFolder != null ? currentFolder.getName() : "未知資料夾";
    }
    
    /**
     * 計算目錄中的檔案數量（遞迴）
     */
    private int countFilesInDirectory(File directory) {
        int count = 0;
        File[] files = directory.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isFile()) {
                    count++;
                } else if (file.isDirectory()) {
                    count += countFilesInDirectory(file);
                }
            }
        }
        return count;
    }
    
    /**
     * 匯入檔案到當前資料夾
     */
    private void importFilesToCurrentFolder(List<File> files) {
        String currentFolderName = getCurrentFolderDisplayName();
        statusLabel.setText("正在匯入檔案到 " + currentFolderName + "...");
        showImportProgress();

        Thread importThread = new Thread(() -> {
            int successCount = 0;
            int totalFiles = files.size();
            int errorCount = 0;
            List<String> errorMessages = new ArrayList<>();

            for (int i = 0; i < files.size(); i++) {
                File file = files.get(i);

                // 更新狀態顯示
                final String currentFileName = file.getName();
                final int currentIndex = i + 1;
                Platform.runLater(() -> statusLabel.setText("正在處理: " + currentFileName + " (" + currentIndex + "/" + totalFiles + ")"));

                try {
                    if (fileManagerData.importFile(file, currentFolderId)) {
                        successCount++;
                    } else {
                        errorCount++;
                        errorMessages.add("無法匯入: " + file.getName());
                    }
                } catch (Exception e) {
                    errorCount++;
                    String errorMsg = "匯入 " + file.getName() + " 失敗: " + e.getMessage();
                    errorMessages.add(errorMsg);
                    System.err.println(errorMsg);
                }

                // 更新進度
                final double progress = (double) (i + 1) / totalFiles;
                Platform.runLater(() -> updateImportProgress(progress));
            }

            final int finalSuccessCount = successCount;
            final int finalErrorCount = errorCount;
            final List<String> finalErrorMessages = new ArrayList<>(errorMessages);

            Platform.runLater(() -> {
                hideImportProgress();
                loadCurrentFolder();

                // 構建結果訊息
                StringBuilder resultMessage = new StringBuilder();
                resultMessage.append("成功匯入 ").append(finalSuccessCount).append(" 個檔案到 ").append(currentFolderName);

                if (finalErrorCount > 0) {
                    resultMessage.append("，").append(finalErrorCount).append(" 個檔案匯入失敗");
                }

                statusLabel.setText(resultMessage.toString());

                // 顯示詳細結果對話框
                if (finalSuccessCount > 0 || finalErrorCount > 0) {
                    Alert alert;
                    if (finalErrorCount == 0) {
                        alert = new Alert(Alert.AlertType.INFORMATION);
                        alert.setTitle("匯入完成");
                    } else {
                        alert = new Alert(Alert.AlertType.WARNING);
                        alert.setTitle("匯入完成（有錯誤）");
                    }

                    alert.setHeaderText(resultMessage.toString());

                    if (finalErrorCount > 0 && !finalErrorMessages.isEmpty()) {
                        StringBuilder errorDetails = new StringBuilder("錯誤詳情：\n");
                        for (int i = 0; i < Math.min(5, finalErrorMessages.size()); i++) {
                            errorDetails.append("• ").append(finalErrorMessages.get(i)).append("\n");
                        }
                        if (finalErrorMessages.size() > 5) {
                            errorDetails.append("...還有 ").append(finalErrorMessages.size() - 5).append(" 個錯誤");
                        }
                        alert.setContentText(errorDetails.toString());
                    } else {
                        alert.setContentText("所有檔案已成功匯入到當前資料夾中。");
                    }

                    alert.showAndWait();
                }
            });
        });

        importThread.setDaemon(true);
        importThread.start();
    }
    
    /**
     * 匯入整個資料夾到當前資料夾
     */
    private void importDirectoryToCurrentFolder(File sourceDirectory) {
        String currentFolderName = getCurrentFolderDisplayName();
        statusLabel.setText("正在匯入資料夾 " + sourceDirectory.getName() + " 到 " + currentFolderName + "...");
        showImportProgress();

        Thread importThread = new Thread(() -> {
            try {
                // 先建立對應的資料夾
                String newFolderId = null;
                boolean folderCreated = fileManagerData.createFolder(sourceDirectory.getName(), currentFolderId);
                
                if (folderCreated) {
                    // 找到新建的資料夾ID
                    List<FolderItem> folders = fileManagerData.getFolders(currentFolderId);
                    for (FolderItem folder : folders) {
                        if (folder.getName().equals(sourceDirectory.getName())) {
                            newFolderId = folder.getId();
                            break;
                        }
                    }
                }
                
                if (newFolderId == null) {
                    Platform.runLater(() -> {
                        hideImportProgress();
                        showError("建立資料夾失敗", "無法建立目標資料夾: " + sourceDirectory.getName());
                    });
                    return;
                }
                
                // 遞迴匯入資料夾內容
                ImportResult result = importDirectoryRecursively(sourceDirectory, newFolderId);
                
                Platform.runLater(() -> {
                    hideImportProgress();
                    loadCurrentFolder();
                    
                    StringBuilder resultMessage = new StringBuilder();
                    resultMessage.append("成功匯入資料夾 '").append(sourceDirectory.getName()).append("' 到 ").append(currentFolderName);
                    resultMessage.append("\n檔案: ").append(result.successFiles).append(" 成功");
                    resultMessage.append("，資料夾: ").append(result.successFolders).append(" 成功");
                    
                    if (result.errorFiles > 0 || result.errorFolders > 0) {
                        resultMessage.append("\n錯誤: ").append(result.errorFiles).append(" 檔案，").append(result.errorFolders).append(" 資料夾");
                    }

                    statusLabel.setText("匯入完成: " + result.successFiles + " 檔案，" + result.successFolders + " 資料夾");

                    // 顯示結果對話框
                    Alert alert;
                    if (result.errorFiles == 0 && result.errorFolders == 0) {
                        alert = new Alert(Alert.AlertType.INFORMATION);
                        alert.setTitle("匯入完成");
                    } else {
                        alert = new Alert(Alert.AlertType.WARNING);
                        alert.setTitle("匯入完成（有錯誤）");
                    }

                    alert.setHeaderText("資料夾匯入結果");
                    alert.setContentText(resultMessage.toString());
                    alert.showAndWait();
                });
                
            } catch (Exception e) {
                Platform.runLater(() -> {
                    hideImportProgress();
                    showError("匯入失敗", "匯入資料夾時發生錯誤: " + e.getMessage());
                });
            }
        });

        importThread.setDaemon(true);
        importThread.start();
    }
    
    /**
     * 遞迴匯入資料夾內容
     */
    private ImportResult importDirectoryRecursively(File sourceDir, String targetFolderId) {
        ImportResult result = new ImportResult();
        
        File[] files = sourceDir.listFiles();
        if (files == null) {
            return result;
        }
        
        for (File file : files) {
            if (file.isFile()) {
                try {
                    if (fileManagerData.importFile(file, targetFolderId)) {
                        result.successFiles++;
                    } else {
                        result.errorFiles++;
                    }
                } catch (Exception e) {
                    result.errorFiles++;
                    System.err.println("匯入檔案失敗 " + file.getName() + ": " + e.getMessage());
                }
            } else if (file.isDirectory()) {
                try {
                    // 建立子資料夾
                    boolean folderCreated = fileManagerData.createFolder(file.getName(), targetFolderId);
                    if (folderCreated) {
                        result.successFolders++;
                        
                        // 找到新建的資料夾ID
                        String newSubFolderId = null;
                        List<FolderItem> subFolders = fileManagerData.getFolders(targetFolderId);
                        for (FolderItem folder : subFolders) {
                            if (folder.getName().equals(file.getName())) {
                                newSubFolderId = folder.getId();
                                break;
                            }
                        }
                        
                        if (newSubFolderId != null) {
                            // 遞迴匯入子資料夾內容
                            ImportResult subResult = importDirectoryRecursively(file, newSubFolderId);
                            result.successFiles += subResult.successFiles;
                            result.errorFiles += subResult.errorFiles;
                            result.successFolders += subResult.successFolders;
                            result.errorFolders += subResult.errorFolders;
                        }
                    } else {
                        result.errorFolders++;
                    }
                } catch (Exception e) {
                    result.errorFolders++;
                    System.err.println("建立資料夾失敗 " + file.getName() + ": " + e.getMessage());
                }
            }
        }
        
        return result;
    }
    
    /**
     * 匯入結果統計類別
     */
    private static class ImportResult {
        int successFiles = 0;
        int errorFiles = 0;
        int successFolders = 0;
        int errorFolders = 0;
    }

    private void showImportPdfDialog() {
        try {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("選擇要匯入的PDF檔案");
            fileChooser.getExtensionFilters().addAll(
                    new FileChooser.ExtensionFilter("PDF 檔案", "*.pdf"),
                    new FileChooser.ExtensionFilter("所有檔案", "*.*")
            );

            // 設定初始目錄
            String userHome = System.getProperty("user.home");
            File initialDir = new File(userHome, "Desktop");
            if (!initialDir.exists()) {
                initialDir = new File(userHome);
            }
            fileChooser.setInitialDirectory(initialDir);

            List<File> selectedFiles = fileChooser.showOpenMultipleDialog(primaryStage);
            if (selectedFiles != null && !selectedFiles.isEmpty()) {
                // 顯示確認對話框
                Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
                confirmAlert.setTitle("確認匯入");
                confirmAlert.setHeaderText("確認匯入PDF檔案");
                confirmAlert.setContentText("將匯入 " + selectedFiles.size() + " 個PDF檔案到 'PDF文件' 資料夾。\n\n這會複製檔案到您的資料庫中。");

                Optional<ButtonType> result = confirmAlert.showAndWait();
                if (result.isPresent() && result.get() == ButtonType.OK) {
                    importFilesToSpecialFolder(selectedFiles, "pdf");
                }
            }
        } catch (Exception e) {
            showError("匯入錯誤", "開啟檔案選擇器時發生錯誤: " + e.getMessage());
        }
    }

    private void importFilesToSpecialFolder(List<File> selectedFiles, String folderType) {
        String targetFolderId = ensureSpecialFolderExists(folderType);
        if (targetFolderId != null) {
            importFilesToFolder(selectedFiles, targetFolderId, folderType);
        } else {
            showError("建立資料夾失敗", "無法建立" + folderType + "資料夾");
        }
    }

    private String ensureSpecialFolderExists(String folderType) {
        String folderName;
        switch (folderType.toLowerCase()) {
            case "pdf":
                folderName = "PDF文件";
                break;
            case "epub":
                folderName = "電子書";
                break;
            case "images":
                folderName = "圖片";
                break;
            default:
                return null;
        }

        // 檢查資料夾是否已存在
        List<FolderItem> rootFolders = fileManagerData.getFolders("root");
        for (FolderItem folder : rootFolders) {
            if (folder.getName().equals(folderName)) {
                return folder.getId();
            }
        }

        // 如果不存在，則建立
        if (fileManagerData.createFolder(folderName, "root")) {
            // 重新獲取資料夾列表找到新建的資料夾
            List<FolderItem> updatedFolders = fileManagerData.getFolders("root");
            for (FolderItem folder : updatedFolders) {
                if (folder.getName().equals(folderName)) {
                    return folder.getId();
                }
            }
        }

        return null;
    }

    private void importFilesToFolder(List<File> files, String targetFolderId, String fileType) {
        String folderName;
        switch (fileType.toLowerCase()) {
            case "pdf":
                folderName = "PDF文件";
                break;
            case "epub":
                folderName = "電子書";
                break;
            case "images":
                folderName = "圖片";
                break;
            default:
                folderName = "未知類型";
                break;
        }
        statusLabel.setText("正在導入檔案到 " + folderName + " 資料夾...");
        showImportProgress();

        Thread importThread = new Thread(() -> {
            int successCount = 0;
            int totalFiles = files.size();
            int skippedCount = 0;
            int errorCount = 0;
            List<String> errorMessages = new ArrayList<>();

            for (int i = 0; i < files.size(); i++) {
                File file = files.get(i);

                // 更新狀態顯示
                final String currentFileName = file.getName();
                final int currentIndex = i + 1;
                Platform.runLater(() -> statusLabel.setText("正在處理: " + currentFileName + " (" + currentIndex + "/" + totalFiles + ")"));

                // 檢查檔案類型
                if (!isValidFileType(file, fileType)) {
                    skippedCount++;
                    continue;
                }

                try {
                    if (fileManagerData.importFile(file, targetFolderId)) {
                        successCount++;
                    } else {
                        errorCount++;
                        errorMessages.add("無法匯入: " + file.getName());
                    }
                } catch (Exception e) {
                    errorCount++;
                    String errorMsg = "匯入 " + file.getName() + " 失敗: " + e.getMessage();
                    errorMessages.add(errorMsg);
                    System.err.println(errorMsg);
                }

                // 更新進度
                final double progress = (double) (i + 1) / totalFiles;
                Platform.runLater(() -> updateImportProgress(progress));
            }

            final int finalSuccessCount = successCount;
            final int finalSkippedCount = skippedCount;
            final int finalErrorCount = errorCount;
            final List<String> finalErrorMessages = new ArrayList<>(errorMessages);

            Platform.runLater(() -> {
                hideImportProgress();
                loadCurrentFolder();

                // 構建結果訊息
                StringBuilder resultMessage = new StringBuilder();
                resultMessage.append("成功導入 ").append(finalSuccessCount).append(" 個檔案到 ").append(folderName).append(" 資料夾");

                if (finalSkippedCount > 0) {
                    resultMessage.append("，跳過 ").append(finalSkippedCount).append(" 個不支援的檔案");
                }

                if (finalErrorCount > 0) {
                    resultMessage.append("，").append(finalErrorCount).append(" 個檔案匯入失敗");
                }

                statusLabel.setText(resultMessage.toString());

                // 顯示詳細結果對話框
                if (finalSuccessCount > 0 || finalErrorCount > 0) {
                    Alert alert;
                    if (finalErrorCount == 0) {
                        alert = new Alert(Alert.AlertType.INFORMATION);
                        alert.setTitle("匯入完成");
                    } else {
                        alert = new Alert(Alert.AlertType.WARNING);
                        alert.setTitle("匯入完成（有錯誤）");
                    }

                    alert.setHeaderText(resultMessage.toString());

                    if (finalErrorCount > 0 && !finalErrorMessages.isEmpty()) {
                        StringBuilder errorDetails = new StringBuilder("錯誤詳情：\n");
                        for (int i = 0; i < Math.min(5, finalErrorMessages.size()); i++) {
                            errorDetails.append("• ").append(finalErrorMessages.get(i)).append("\n");
                        }
                        if (finalErrorMessages.size() > 5) {
                            errorDetails.append("...還有 ").append(finalErrorMessages.size() - 5).append(" 個錯誤");
                        }
                        alert.setContentText(errorDetails.toString());
                    } else {
                        alert.setContentText("所有檔案已成功匯入到您的資料庫中。");
                    }

                    alert.showAndWait();
                }
            });
        });

        importThread.setDaemon(true);
        importThread.start();
    }

    private boolean isValidFileType(File file, String fileType) {
        String fileName = file.getName().toLowerCase();

        switch (fileType.toLowerCase()) {
            case "pdf":
                return fileName.endsWith(".pdf");
            case "epub":
                return fileName.endsWith(".epub");
            case "images":
                return fileName.endsWith(".jpg") || fileName.endsWith(".jpeg") ||
                        fileName.endsWith(".png") || fileName.endsWith(".gif") ||
                        fileName.endsWith(".bmp") || fileName.endsWith(".tiff") ||
                        fileName.endsWith(".webp");
            default:
                return true;
        }
    }

    private void showImportEpubDialog() {
        try {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("選擇要匯入的EPUB檔案");
            fileChooser.getExtensionFilters().addAll(
                    new FileChooser.ExtensionFilter("EPUB 檔案", "*.epub"),
                    new FileChooser.ExtensionFilter("所有檔案", "*.*")
            );

            // 設定初始目錄
            String userHome = System.getProperty("user.home");
            File initialDir = new File(userHome, "Desktop");
            if (!initialDir.exists()) {
                initialDir = new File(userHome);
            }
            fileChooser.setInitialDirectory(initialDir);

            List<File> selectedFiles = fileChooser.showOpenMultipleDialog(primaryStage);
            if (selectedFiles != null && !selectedFiles.isEmpty()) {
                // 顯示確認對話框
                Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
                confirmAlert.setTitle("確認匯入");
                confirmAlert.setHeaderText("確認匯入EPUB檔案");
                confirmAlert.setContentText("將匯入 " + selectedFiles.size() + " 個EPUB檔案到 '電子書' 資料夾。\n\n這會複製檔案到您的資料庫中。");

                Optional<ButtonType> result = confirmAlert.showAndWait();
                if (result.isPresent() && result.get() == ButtonType.OK) {
                    importFilesToSpecialFolder(selectedFiles, "epub");
                }
            }
        } catch (Exception e) {
            showError("匯入錯誤", "開啟檔案選擇器時發生錯誤: " + e.getMessage());
        }
    }

    private void showImportImageDialog() {
        try {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("選擇要匯入的圖片檔案");
            fileChooser.getExtensionFilters().addAll(
                    new FileChooser.ExtensionFilter("圖片檔案", "*.jpg", "*.jpeg", "*.png", "*.gif", "*.bmp", "*.tiff", "*.webp"),
                    new FileChooser.ExtensionFilter("JPEG 圖片", "*.jpg", "*.jpeg"),
                    new FileChooser.ExtensionFilter("PNG 圖片", "*.png"),
                    new FileChooser.ExtensionFilter("所有檔案", "*.*")
            );

            // 設定初始目錄
            String userHome = System.getProperty("user.home");
            File initialDir = new File(userHome, "Pictures"); // 圖片目錄
            if (!initialDir.exists()) {
                initialDir = new File(userHome, "Desktop");
                if (!initialDir.exists()) {
                    initialDir = new File(userHome);
                }
            }
            fileChooser.setInitialDirectory(initialDir);

            List<File> selectedFiles = fileChooser.showOpenMultipleDialog(primaryStage);
            if (selectedFiles != null && !selectedFiles.isEmpty()) {
                // 顯示確認對話框
                Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
                confirmAlert.setTitle("確認匯入");
                confirmAlert.setHeaderText("確認匯入圖片檔案");
                confirmAlert.setContentText("將匯入 " + selectedFiles.size() + " 個圖片檔案到 '圖片' 資料夾。\n\n這會複製檔案到您的資料庫中。");

                Optional<ButtonType> result = confirmAlert.showAndWait();
                if (result.isPresent() && result.get() == ButtonType.OK) {
                    importFilesToSpecialFolder(selectedFiles, "images");
                }
            }
        } catch (Exception e) {
            showError("匯入錯誤", "開啟檔案選擇器時發生錯誤: " + e.getMessage());
        }
    }

    private void showImportDialog() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("選擇要匯入的檔案");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("支援的檔案", "*.pdf", "*.epub", "*.jpg", "*.jpeg", "*.png", "*.gif", "*.bmp"),
                new FileChooser.ExtensionFilter("PDF 檔案", "*.pdf"),
                new FileChooser.ExtensionFilter("EPUB 檔案", "*.epub"),
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
            importProgressBar.setPrefWidth(400);
            importProgressBar.setStyle("-fx-accent: #007ACC;");

            Label progressLabel = new Label("正在匯入檔案...");
            progressLabel.setStyle("-fx-text-fill: #333; -fx-font-size: 14px; -fx-font-weight: bold;");

            Label detailLabel = new Label("請稍候...");
            detailLabel.setStyle("-fx-text-fill: #666; -fx-font-size: 12px;");

            VBox progressBox = new VBox(10);
            progressBox.setAlignment(Pos.CENTER);
            progressBox.getChildren().addAll(progressLabel, importProgressBar, detailLabel);
            progressBox.setPadding(new Insets(20));
            progressBox.setStyle("-fx-background-color: rgba(255,255,255,0.95); -fx-background-radius: 10; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.3), 10, 0, 0, 5);");

            StackPane overlay = new StackPane(progressBox);
            overlay.setStyle("-fx-background-color: rgba(0,0,0,0.4);");

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
                refreshFolderPanel();
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
        TreeItem<FolderTreeItem> rootItem = new TreeItem<>(new FolderTreeItem("📁 根資料夾", "root"));
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
                // 檢查是否為E_Reader支援的檔案格式
                if (isSupportedByEReader(file)) {
                    fileOpenCallback.onFileOpen(physicalFile);
                    // 隱藏檔案管理器視窗，不關閉以便未來可以再次開啟
                    primaryStage.hide();
                } else {
                    // 不支援的檔案格式，詢問使用者是否要用系統預設程式開啟
                    showUnsupportedFileDialog(file, physicalFile);
                }
            } else {
                showError("開啟失敗", "檔案不存在或已被移動");
            }
        }
    }

    /**
     * 檢查檔案是否為E_Reader支援的格式
     */
    private boolean isSupportedByEReader(FileItem file) {
        String extension = file.getExtension().toLowerCase();
        return extension.equals("pdf") || 
               extension.equals("epub") || 
               isImageExtension(extension) ||
                extension.equals("txt");
    }


    /**
     * 顯示PDF開啟警告對話框
     */
    private void showPdfOpeningWarning(FileItem fileItem, File physicalFile) {
        Alert warningAlert = new Alert(Alert.AlertType.WARNING);
        warningAlert.setTitle("PDF檔案開啟警告");
        warningAlert.setHeaderText("即將開啟PDF檔案");

        // 獲取檔案大小資訊
        String fileSizeText = formatFileSize(fileItem.getSize());

        StringBuilder content = new StringBuilder();
        content.append("檔案: ").append(fileItem.getName()).append("\n");
        content.append("大小: ").append(fileSizeText).append("\n\n");
        content.append("⚠️ 重要提醒:\n");
        content.append("• PDF檔案可能需要較長的載入時間\n");
        content.append("• 大型PDF檔案會占用較多系統記憶體\n");
        content.append("• 載入過程中請勿關閉程式\n");
        content.append("• 建議確保有足夠的可用記憶體\n\n");

        // 根據檔案大小給予不同建議
        if (fileItem.getSize() > 50 * 1024 * 1024) { // 大於50MB
            content.append("⚠️ 注意：此檔案較大，載入可能需要較長時間\n\n");
        } else if (fileItem.getSize() > 10 * 1024 * 1024) { // 大於10MB
            content.append("ℹ️ 提示：此檔案為中等大小，載入需要一些時間\n\n");
        }

        content.append("確定要開啟此PDF檔案嗎？");

        warningAlert.setContentText(content.toString());

        // 設定自定義按鈕
        warningAlert.getButtonTypes().clear();

        ButtonType confirmButton = new ButtonType("確定開啟", ButtonBar.ButtonData.OK_DONE);
        ButtonType cancelButton = new ButtonType("取消", ButtonBar.ButtonData.CANCEL_CLOSE);

        warningAlert.getButtonTypes().addAll(confirmButton, cancelButton);

        // 設定對話框屬性
        warningAlert.initOwner(primaryStage);
        warningAlert.initModality(Modality.WINDOW_MODAL);
        warningAlert.setResizable(true);

        // 設定對話框樣式
        warningAlert.getDialogPane().setStyle("-fx-font-family: 'Microsoft JhengHei';");

        // 設定按鈕樣式
        Button confirmBtn = (Button) warningAlert.getDialogPane().lookupButton(confirmButton);
        Button cancelBtn = (Button) warningAlert.getDialogPane().lookupButton(cancelButton);

        if (confirmBtn != null) {
            confirmBtn.setStyle("-fx-background-color: #007bff; -fx-text-fill: white; -fx-font-weight: bold;");
        }
        if (cancelBtn != null) {
            cancelBtn.setStyle("-fx-background-color: #6c757d; -fx-text-fill: white;");
        }

        // 顯示對話框並處理結果
        Optional<ButtonType> result = warningAlert.showAndWait();
        if (result.isPresent() && result.get() == confirmButton) {
            // 用戶確認開啟，直接呼叫callback
            openFileDirectly(physicalFile);
        }
        // 如果用戶取消，則不做任何動作
    }

    /**
     * 直接開啟檔案（無警告）
     */
    private void openFileDirectly(File physicalFile) {
        // 更新狀態標籤
        statusLabel.setText("正在開啟檔案: " + physicalFile.getName());

        // 呼叫原本的callback
        fileOpenCallback.onFileOpen(physicalFile);

        // 隱藏檔案管理器視窗
        primaryStage.hide();
    }





    /**
     * 檢查是否為圖片副檔名
     */
    private boolean isImageExtension(String extension) {
        return extension.equals("jpg") || extension.equals("jpeg") ||
               extension.equals("png") || extension.equals("gif") ||
               extension.equals("bmp") || extension.equals("tiff") ||
               extension.equals("webp");
    }

    /**
     * 顯示不支援檔案格式的對話框
     */
    private void showUnsupportedFileDialog(FileItem fileItem, File physicalFile) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("檔案格式不支援");
        alert.setHeaderText("檔案: " + fileItem.getName());
        
        StringBuilder content = new StringBuilder();
        content.append("此檔案類型不支援在E_Reader中直接開啟。\n\n");
        content.append("支援的檔案格式：\n");
        content.append("• PDF檔案 (.pdf)\n");
        content.append("• EPUB電子書 (.epub)\n");
        content.append("• 圖片檔案 (.jpg, .png, .gif, .bmp, .tiff, .webp)\n\n");
        content.append("是否要使用系統預設程式開啟此檔案？");
        
        alert.setContentText(content.toString());

        ButtonType openWithSystemButton = new ButtonType("使用系統程式開啟");
        ButtonType cancelButton = new ButtonType("取消", ButtonBar.ButtonData.CANCEL_CLOSE);
        alert.getButtonTypes().setAll(openWithSystemButton, cancelButton);

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == openWithSystemButton) {
            openWithSystemDefault(physicalFile);
        }
    }

    /**
     * 使用系統預設程式開啟檔案
     */
    private void openWithSystemDefault(File file) {
        try {
            if (java.awt.Desktop.isDesktopSupported()) {
                java.awt.Desktop desktop = java.awt.Desktop.getDesktop();
                if (desktop.isSupported(java.awt.Desktop.Action.OPEN)) {
                    desktop.open(file);
                    statusLabel.setText("已使用系統預設程式開啟: " + file.getName());
                } else {
                    showError("開啟失敗", "系統不支援自動開啟檔案功能");
                }
            } else {
                showError("開啟失敗", "系統不支援桌面整合功能");
            }
        } catch (Exception e) {
            showError("開啟失敗", 
                "無法開啟檔案: " + file.getName() + "\n\n" +
                "錯誤原因: " + e.getMessage() + "\n\n" +
                "建議：請手動使用適當的程式開啟此檔案。");
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
        displayFilteredItems(filteredFolders, filteredFiles);

        statusLabel.setText("找到 " + filteredFiles.size() + " 個檔案，" + filteredFolders.size() + " 個資料夾");
    }

    private void displayFilteredItems(List<FolderItem> folders, List<FileItem> files) {
        fileGrid.getChildren().clear();

        if (isGridView) {
            displayFilteredItemsGrid(folders, files);
        } else {
            displayFilteredItemsList(folders, files);
        }
    }

    private void displayFilteredItemsGrid(List<FolderItem> folders, List<FileItem> files) {
        int column = 0;
        int row = 0;
        int maxColumns = 5;

        // 顯示過濾後的資料夾
        for (FolderItem folder : folders) {
            VBox folderCard = createFolderCard(folder);
            fileGrid.add(folderCard, column, row);

            column++;
            if (column >= maxColumns) {
                column = 0;
                row++;
            }
        }

        // 顯示過濾後的檔案
        for (FileItem file : files) {
            VBox fileCard = createFileCard(file);
            fileGrid.add(fileCard, column, row);

            column++;
            if (column >= maxColumns) {
                column = 0;
                row++;
            }
        }
    }

    private void displayFilteredItemsList(List<FolderItem> folders, List<FileItem> files) {
        fileGrid.setHgap(0);
        fileGrid.setVgap(2);

        int row = 0;

        // 顯示過濾後的資料夾
        for (FolderItem folder : folders) {
            HBox folderRow = createFolderListItem(folder);
            fileGrid.add(folderRow, 0, row);
            row++;
        }

        // 顯示過濾後的檔案
        for (FileItem file : files) {
            HBox fileRow = createFileListItem(file);
            fileGrid.add(fileRow, 0, row);
            row++;
        }

        // 重置間距設定
        fileGrid.setHgap(15);
        fileGrid.setVgap(15);
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
        // 檢查是否已經有 Scene，避免重複創建
        if (primaryStage.getScene() == null) {
            Scene scene = new Scene(mainLayout);
            primaryStage.setScene(scene);
        }
        primaryStage.setTitle("E_Reader - 檔案管理器");

        // 如果視窗未顯示，則顯示它
        if (!primaryStage.isShowing()) {
            primaryStage.show();
        } else {
            // 如果已經顯示，則將其置於前景
            primaryStage.toFront();
        }

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
    
    /**
     * 設定SettingsManager（從MainController傳入）
     */
    public void setSettingsManager(SettingsManager settingsManager) {
        this.settingsManager = settingsManager;
    }
    
    /**
     * 設定TextExtractor（從MainController傳入）
     */
    public void setTextExtractor(TextExtractor textExtractor) {
        this.textExtractor = textExtractor;
    }
    
    /**
     * 顯示OCR設定對話框
     */
    private void showOcrSettingsDialog() {
        // 檢查設定管理器是否已初始化
        if (settingsManager == null || textExtractor == null) {
            showError("設定錯誤", "設定管理器未初始化，請先開啟主程式。");
            return;
        }
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("OCR文字識別設定");
        dialog.setHeaderText("配置OCR文字識別參數");
        dialog.initOwner(primaryStage);
        
        // 建立設定內容
        VBox content = new VBox(15);
        content.setPadding(new Insets(20));
        content.setPrefWidth(450);
        
        // OCR模型選擇
        Label ocrModelLabel = new Label("OCR文字識別模型:");
        ocrModelLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");
        
        ComboBox<SettingsManager.OcrModel> ocrModelCombo = new ComboBox<>();
        ocrModelCombo.getItems().addAll(SettingsManager.OcrModel.values());
        ocrModelCombo.setValue(settingsManager.getOcrModel());
        ocrModelCombo.setPrefWidth(300);
        
        // OCR模型描述標籤
        Label ocrDescLabel = new Label(settingsManager.getOcrModel().getDescription());
        ocrDescLabel.setStyle("-fx-text-fill: #666666; -fx-font-size: 12px; -fx-wrap-text: true;");
        ocrDescLabel.setPrefWidth(400);
        ocrDescLabel.setWrapText(true);
        
        // 更新OCR描述當選擇改變時
        ocrModelCombo.setOnAction(e -> {
            SettingsManager.OcrModel selected = ocrModelCombo.getValue();
            if (selected != null) {
                ocrDescLabel.setText(selected.getDescription());
            }
        });
        
        // OCR狀態顯示
        Label statusLabel = new Label("當前OCR狀態:");
        statusLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");
        
        String ocrStatus = textExtractor.getOcrStatus();
        Label ocrStatusLabel = new Label(ocrStatus);
        ocrStatusLabel.setStyle("-fx-text-fill: " + 
            (textExtractor.isOcrAvailable() ? "#28a745" : "#dc3545") + 
            "; -fx-font-size: 12px; -fx-wrap-text: true;");
        ocrStatusLabel.setPrefWidth(400);
        ocrStatusLabel.setWrapText(true);
        
        // 用法說明
        Label usageLabel = new Label("使用說明:");
        usageLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");
        
        Label usageText = new Label(
            "• 快速模型：識別速度快，適合一般用途\n" +
            "• 最佳模型：識別精度高，適合重要文件\n" +
            "• 識別效果取決於圖片清晰度和文字大小\n" +
            "• 識別失敗時會自動嘗試其他模型"
        );
        usageText.setStyle("-fx-text-fill: #666666; -fx-font-size: 11px; -fx-wrap-text: true;");
        usageText.setPrefWidth(400);
        usageText.setWrapText(true);
        
        // 組裝內容
        content.getChildren().addAll(
            ocrModelLabel, ocrModelCombo, ocrDescLabel,
            new Separator(),
            statusLabel, ocrStatusLabel,
            new Separator(),
            usageLabel, usageText
        );
        
        dialog.getDialogPane().setContent(content);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        
        // 設定按鈕樣式
        Button okButton = (Button) dialog.getDialogPane().lookupButton(ButtonType.OK);
        okButton.setStyle("-fx-background-color: #007bff; -fx-text-fill: white; -fx-font-weight: bold;");
        
        Button cancelButton = (Button) dialog.getDialogPane().lookupButton(ButtonType.CANCEL);
        cancelButton.setStyle("-fx-background-color: #6c757d; -fx-text-fill: white;");
        
        // 處理結果
        Optional<ButtonType> result = dialog.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            SettingsManager.OcrModel newOcrModel = ocrModelCombo.getValue();
            if (newOcrModel != null && newOcrModel != settingsManager.getOcrModel()) {
                // 更新設定
                settingsManager.setOcrModel(newOcrModel);
                settingsManager.saveSettings();
                
                // 更新TextExtractor的模型
                textExtractor.updateOcrModel(newOcrModel);
                
                // 顯示成功訊息
                statusLabel.setText("OCR設定已更新 - 當前使用: " + newOcrModel.getDisplayName());
                
                // 顯示通知對話框
                showOcrUpdateNotification(newOcrModel);
            }
        }
    }
    
    /**
     * 顯示OCR更新通知
     */
    private void showOcrUpdateNotification(SettingsManager.OcrModel newModel) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("OCR設定已更新");
        alert.setHeaderText("OCR模型已切換");
        alert.setContentText(
            "OCR模型已成功切換為: " + newModel.getDisplayName() + "\n\n" +
            "描述: " + newModel.getDescription() + "\n\n" +
            "新設定將在下次文字識別時生效。"
        );
        alert.initOwner(primaryStage);
        
        // 設定對話框樣式
        alert.getDialogPane().setStyle("-fx-font-family: 'Microsoft JhengHei';");
        
        alert.showAndWait();
    }
}