package E_Reader;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.CollectionType;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class BookmarkManager {

    private final String BOOKMARKS_FILE = "bookmarks.json";
    private ObservableList<Bookmark> bookmarks;
    private ObjectMapper objectMapper;

    public static class Bookmark {
        private String title;
        private String filePath;
        private int pageNumber;
        private LocalDateTime createdTime;
        private String notes;

        // 建構子
        public Bookmark() {}

        public Bookmark(String title, String filePath, int pageNumber, String notes) {
            this.title = title;
            this.filePath = filePath;
            this.pageNumber = pageNumber;
            this.notes = notes;
            this.createdTime = LocalDateTime.now();
        }

        // Getter 和 Setter
        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }

        public String getFilePath() { return filePath; }
        public void setFilePath(String filePath) { this.filePath = filePath; }

        public int getPageNumber() { return pageNumber; }
        public void setPageNumber(int pageNumber) { this.pageNumber = pageNumber; }

        public LocalDateTime getCreatedTime() { return createdTime; }
        public void setCreatedTime(LocalDateTime createdTime) { this.createdTime = createdTime; }

        public String getNotes() { return notes; }
        public void setNotes(String notes) { this.notes = notes; }

        @Override
        public String toString() {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
            return String.format("%s (頁 %d) - %s", title, pageNumber, createdTime.format(formatter));
        }
    }

    public BookmarkManager() {
        this.bookmarks = FXCollections.observableArrayList();
        this.objectMapper = new ObjectMapper();
        this.objectMapper.findAndRegisterModules(); // 支援 LocalDateTime
        loadBookmarks();
    }

    public void addBookmark(String title, String filePath, int pageNumber, String notes) {
        Bookmark bookmark = new Bookmark(title, filePath, pageNumber, notes);
        bookmarks.add(bookmark);
        saveBookmarks();
    }

    public void removeBookmark(Bookmark bookmark) {
        bookmarks.remove(bookmark);
        saveBookmarks();
    }

    public void updateBookmark(Bookmark bookmark, String title, String notes) {
        bookmark.setTitle(title);
        bookmark.setNotes(notes);
        saveBookmarks();
    }

    public ObservableList<Bookmark> getBookmarks() {
        return bookmarks;
    }

    public List<Bookmark> getBookmarksForFile(String filePath) {
        return bookmarks.stream()
                .filter(b -> b.getFilePath().equals(filePath))
                .toList();
    }

    private void loadBookmarks() {
        try {
            File file = new File(BOOKMARKS_FILE);
            if (file.exists()) {
                CollectionType listType = objectMapper.getTypeFactory()
                        .constructCollectionType(List.class, Bookmark.class);
                List<Bookmark> loadedBookmarks = objectMapper.readValue(file, listType);
                bookmarks.setAll(loadedBookmarks);
            }
        } catch (IOException e) {
            System.err.println("無法載入書籤檔案: " + e.getMessage());
        }
    }

    private void saveBookmarks() {
        try {
            objectMapper.writeValue(new File(BOOKMARKS_FILE), bookmarks);
        } catch (IOException e) {
            System.err.println("無法儲存書籤檔案: " + e.getMessage());
        }
    }

    public void showBookmarkDialog(Stage parentStage, String currentFilePath, int currentPage,
                                   BookmarkActionListener listener) {
        Stage bookmarkStage = new Stage();
        bookmarkStage.initModality(Modality.APPLICATION_MODAL);
        bookmarkStage.initOwner(parentStage);
        bookmarkStage.setTitle("書籤管理");
        bookmarkStage.setResizable(true);

        VBox root = new VBox(15);
        root.setPadding(new Insets(20));
        root.setStyle("-fx-background-color: #2b2b2b;");

        // 標題
        Label titleLabel = new Label("書籤管理");
        titleLabel.setStyle("-fx-text-fill: white; -fx-font-size: 18px; -fx-font-weight: bold;");

        // 新增書籤區域
        VBox addBookmarkBox = createAddBookmarkSection(currentFilePath, currentPage);

        // 書籤列表
        VBox bookmarkListBox = createBookmarkListSection(bookmarkStage, listener);

        root.getChildren().addAll(titleLabel, new Separator(), addBookmarkBox,
                new Separator(), bookmarkListBox);

        Scene scene = new Scene(root, 600, 500);
        bookmarkStage.setScene(scene);
        bookmarkStage.show();
    }

    private VBox createAddBookmarkSection(String currentFilePath, int currentPage) {
        VBox addBox = new VBox(10);

        Label addLabel = new Label("新增書籤");
        addLabel.setStyle("-fx-text-fill: white; -fx-font-size: 14px; -fx-font-weight: bold;");

        // 書籤標題輸入
        HBox titleBox = new HBox(10);
        titleBox.setAlignment(Pos.CENTER_LEFT);
        Label titleInputLabel = new Label("標題:");
        titleInputLabel.setStyle("-fx-text-fill: white;");
        titleInputLabel.setPrefWidth(60);

        TextField titleField = new TextField();
        titleField.setPromptText("輸入書籤標題");
        titleField.setStyle("-fx-background-color: #404040; -fx-text-fill: white; " +
                "-fx-border-color: #666666; -fx-border-radius: 3px;");
        titleField.setPrefWidth(300);

        titleBox.getChildren().addAll(titleInputLabel, titleField);

        // 備註輸入
        HBox notesBox = new HBox(10);
        notesBox.setAlignment(Pos.TOP_LEFT);
        Label notesInputLabel = new Label("備註:");
        notesInputLabel.setStyle("-fx-text-fill: white;");
        notesInputLabel.setPrefWidth(60);

        TextArea notesArea = new TextArea();
        notesArea.setPromptText("輸入備註 (選填)");
        notesArea.setStyle("-fx-background-color: #404040; -fx-text-fill: white; " +
                "-fx-border-color: #666666; -fx-border-radius: 3px;");
        notesArea.setPrefRowCount(3);
        notesArea.setPrefWidth(300);

        notesBox.getChildren().addAll(notesInputLabel, notesArea);

        // 頁面資訊
        Label pageInfoLabel = new Label(String.format("目前頁面: %d", currentPage + 1));
        pageInfoLabel.setStyle("-fx-text-fill: #cccccc; -fx-font-size: 12px;");

        // 新增按鈕
        Button addButton = new Button("新增書籤");
        addButton.setStyle("-fx-background-color: #0078d4; -fx-text-fill: white; " +
                "-fx-border-radius: 5; -fx-background-radius: 5; " +
                "-fx-padding: 8 15 8 15;");

        addButton.setOnAction(e -> {
            String title = titleField.getText().trim();
            if (title.isEmpty()) {
                title = String.format("書籤 - 頁面 %d", currentPage + 1);
            }

            String notes = notesArea.getText().trim();
            addBookmark(title, currentFilePath, currentPage, notes);

            titleField.clear();
            notesArea.clear();

            // 重新整理書籤列表
            // 這裡可以觸發列表更新事件
        });

        addBox.getChildren().addAll(addLabel, titleBox, notesBox, pageInfoLabel, addButton);
        return addBox;
    }

    private VBox createBookmarkListSection(Stage parentStage, BookmarkActionListener listener) {
        VBox listBox = new VBox(10);

        Label listLabel = new Label("已儲存的書籤");
        listLabel.setStyle("-fx-text-fill: white; -fx-font-size: 14px; -fx-font-weight: bold;");

        // 書籤列表
        ListView<Bookmark> bookmarkListView = new ListView<>(bookmarks);
        bookmarkListView.setPrefHeight(200);
        bookmarkListView.setStyle("-fx-background-color: #404040; -fx-border-color: #666666;");

        // 自訂列表項目顯示
        bookmarkListView.setCellFactory(lv -> new ListCell<Bookmark>() {
            @Override
            protected void updateItem(Bookmark bookmark, boolean empty) {
                super.updateItem(bookmark, empty);
                if (empty || bookmark == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    VBox content = new VBox(5);

                    Label titleLabel = new Label(bookmark.getTitle());
                    titleLabel.setStyle("-fx-text-fill: white; -fx-font-weight: bold;");

                    Label infoLabel = new Label(String.format("頁面: %d | 建立時間: %s",
                            bookmark.getPageNumber() + 1,
                            bookmark.getCreatedTime().format(DateTimeFormatter.ofPattern("MM-dd HH:mm"))));
                    infoLabel.setStyle("-fx-text-fill: #cccccc; -fx-font-size: 11px;");

                    if (bookmark.getNotes() != null && !bookmark.getNotes().isEmpty()) {
                        Label notesLabel = new Label("備註: " + bookmark.getNotes());
                        notesLabel.setStyle("-fx-text-fill: #aaaaaa; -fx-font-size: 10px;");
                        notesLabel.setWrapText(true);
                        content.getChildren().addAll(titleLabel, infoLabel, notesLabel);
                    } else {
                        content.getChildren().addAll(titleLabel, infoLabel);
                    }

                    setGraphic(content);
                    setStyle("-fx-background-color: transparent;");
                }
            }
        });

        // 按鈕區域
        HBox buttonBox = new HBox(10);
        buttonBox.setAlignment(Pos.CENTER);

        Button goToButton = new Button("跳轉到書籤");
        Button editButton = new Button("編輯");
        Button deleteButton = new Button("刪除");
        Button closeButton = new Button("關閉");

        String buttonStyle = "-fx-background-color: #404040; -fx-text-fill: white; " +
                "-fx-border-radius: 5; -fx-background-radius: 5; " +
                "-fx-padding: 8 15 8 15;";

        goToButton.setStyle(buttonStyle);
        editButton.setStyle(buttonStyle);
        deleteButton.setStyle(buttonStyle + " -fx-background-color: #dc3545;");
        closeButton.setStyle(buttonStyle);

        // 按鈕事件
        goToButton.setOnAction(e -> {
            Bookmark selected = bookmarkListView.getSelectionModel().getSelectedItem();
            if (selected != null && listener != null) {
                listener.onGoToBookmark(selected);
                parentStage.close();
            }
        });

        editButton.setOnAction(e -> {
            Bookmark selected = bookmarkListView.getSelectionModel().getSelectedItem();
            if (selected != null) {
                showEditBookmarkDialog(parentStage, selected);
            }
        });

        deleteButton.setOnAction(e -> {
            Bookmark selected = bookmarkListView.getSelectionModel().getSelectedItem();
            if (selected != null) {
                Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                alert.setTitle("確認刪除");
                alert.setHeaderText(null);
                alert.setContentText("確定要刪除這個書籤嗎？");

                Optional<ButtonType> result = alert.showAndWait();
                if (result.isPresent() && result.get() == ButtonType.OK) {
                    removeBookmark(selected);
                }
            }
        });

        closeButton.setOnAction(e -> parentStage.close());

        buttonBox.getChildren().addAll(goToButton, editButton, deleteButton, closeButton);

        listBox.getChildren().addAll(listLabel, bookmarkListView, buttonBox);
        return listBox;
    }

    private void showEditBookmarkDialog(Stage parentStage, Bookmark bookmark) {
        Stage editStage = new Stage();
        editStage.initModality(Modality.APPLICATION_MODAL);
        editStage.initOwner(parentStage);
        editStage.setTitle("編輯書籤");

        VBox root = new VBox(15);
        root.setPadding(new Insets(20));
        root.setStyle("-fx-background-color: #2b2b2b;");

        // 標題輸入
        Label titleLabel = new Label("標題:");
        titleLabel.setStyle("-fx-text-fill: white;");

        TextField titleField = new TextField(bookmark.getTitle());
        titleField.setStyle("-fx-background-color: #404040; -fx-text-fill: white; " +
                "-fx-border-color: #666666; -fx-border-radius: 3px;");

        // 備註輸入
        Label notesLabel = new Label("備註:");
        notesLabel.setStyle("-fx-text-fill: white;");

        TextArea notesArea = new TextArea(bookmark.getNotes());
        notesArea.setStyle("-fx-background-color: #404040; -fx-text-fill: white; " +
                "-fx-border-color: #666666; -fx-border-radius: 3px;");
        notesArea.setPrefRowCount(4);

        // 按鈕
        HBox buttonBox = new HBox(10);
        buttonBox.setAlignment(Pos.CENTER);

        Button saveButton = new Button("儲存");
        Button cancelButton = new Button("取消");

        String buttonStyle = "-fx-background-color: #404040; -fx-text-fill: white; " +
                "-fx-border-radius: 5; -fx-background-radius: 5; " +
                "-fx-padding: 8 15 8 15;";

        saveButton.setStyle(buttonStyle + " -fx-background-color: #28a745;");
        cancelButton.setStyle(buttonStyle);

        saveButton.setOnAction(e -> {
            updateBookmark(bookmark, titleField.getText().trim(), notesArea.getText().trim());
            editStage.close();
        });

        cancelButton.setOnAction(e -> editStage.close());

        buttonBox.getChildren().addAll(saveButton, cancelButton);

        root.getChildren().addAll(titleLabel, titleField, notesLabel, notesArea, buttonBox);

        Scene scene = new Scene(root, 400, 300);
        editStage.setScene(scene);
        editStage.showAndWait();
    }

    public interface BookmarkActionListener {
        void onGoToBookmark(Bookmark bookmark);
    }
}