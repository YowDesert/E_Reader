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

                // 創建圖片資料夾
                if (fileManagerData.createFolder("圖片", "root")) {
                    System.out.println("Created 圖片 folder");
                }

                // 創建測試資料夾
                if (fileManagerData.createFolder("測試資料夾", "root")) {
                    System.out.println("Created 測試資料夾 folder");
                }

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

        // 匯入PDF按鈕
        Button importPdfBtn = new Button("📄 匯入PDF");
        importPdfBtn.setStyle("-fx-background-color: #dc3545; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 10 20; -fx-font-size: 14px;");
        importPdfBtn.setOnAction(e -> showImportPdfDialog());
        importPdfBtn.setTooltip(new Tooltip("選擇PDF檔案並匯入到PDF文件資料夾"));

        // 匯入圖片按鈕
        Button importImageBtn = new Button("🖼️ 匯入圖片");
        importImageBtn.setStyle("-fx-background-color: #fd7e14; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 10 20; -fx-font-size: 14px;");
        importImageBtn.setOnAction(e -> showImportImageDialog());
        importImageBtn.setTooltip(new Tooltip("選擇圖片檔案並匯入到圖片資料夾"));

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
                importPdfBtn, importImageBtn,
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
        String folderName = "pdf".equals(fileType) ? "PDF文件" : "圖片";
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
            case "images":
                return fileName.endsWith(".jpg") || fileName.endsWith(".jpeg") ||
                        fileName.endsWith(".png") || fileName.endsWith(".gif") ||
                        fileName.endsWith(".bmp") || fileName.endsWith(".tiff") ||
                        fileName.endsWith(".webp");
            default:
                return true;
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
                fileOpenCallback.onFileOpen(physicalFile);
                // 隱藏檔案管理器視窗，不關閉以便未來可以再次開啟
                primaryStage.hide();
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
}