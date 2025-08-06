package E_Reader.ui;

import E_Reader.core.NoteManager;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

/**
 * 現代化筆記對話框 - iOS風格設計
 */
public class NoteDialog {
    
    private final Stage dialogStage;
    private final NoteManager noteManager;
    private final String bookPath;
    private final String bookName;
    private final int pageIndex;
    
    private TextArea noteContent;
    private TextField noteTitle;
    private ComboBox<String> highlightColor;
    private TextArea highlightNote;
    
    public NoteDialog(Stage owner, NoteManager noteManager, String bookPath, String bookName, int pageIndex) {
        this.noteManager = noteManager;
        this.bookPath = bookPath;
        this.bookName = bookName;
        this.pageIndex = pageIndex;
        
        this.dialogStage = new Stage();
        this.dialogStage.initOwner(owner);
        this.dialogStage.initModality(Modality.APPLICATION_MODAL);
        this.dialogStage.initStyle(StageStyle.TRANSPARENT);
        
        setupDialog();
    }
    
    private void setupDialog() {
        // 主容器 - 毛玻璃效果
        VBox mainContainer = new VBox(20);
        mainContainer.setPadding(new Insets(30, 40, 30, 40));
        mainContainer.setMaxWidth(600);
        mainContainer.setMaxHeight(700);
        
        // iOS風格背景
        mainContainer.setStyle(
            "-fx-background-color: linear-gradient(135deg, " +
                "rgba(255,255,255,0.95) 0%, " +
                "rgba(248,248,248,0.98) 50%, " +
                "rgba(255,255,255,0.95) 100%); " +
            "-fx-background-radius: 20; " +
            "-fx-border-color: rgba(200,200,200,0.3); " +
            "-fx-border-width: 1; " +
            "-fx-border-radius: 20; " +
            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 20, 0, 0, 10);"
        );
        
        // 標題區域
        HBox titleBar = createTitleBar();
        
        // 標籤頁容器
        TabPane tabPane = createTabPane();
        
        mainContainer.getChildren().addAll(titleBar, tabPane);
        
        // 場景設定
        Scene scene = new Scene(mainContainer);
        scene.setFill(Color.TRANSPARENT);
        dialogStage.setScene(scene);
    }
    
    private HBox createTitleBar() {
        HBox titleBar = new HBox();
        titleBar.setAlignment(Pos.CENTER_LEFT);
        titleBar.setPadding(new Insets(0, 0, 20, 0));
        
        Label titleLabel = new Label("📝 筆記與重點");
        titleLabel.setStyle(
            "-fx-font-size: 20px; " +
            "-fx-font-weight: bold; " +
            "-fx-text-fill: #2c3e50;"
        );
        
        Button closeButton = new Button("✕");
        closeButton.setStyle(
            "-fx-background-color: transparent; " +
            "-fx-text-fill: #7f8c8d; " +
            "-fx-font-size: 18px; " +
            "-fx-font-weight: bold; " +
            "-fx-cursor: hand; " +
            "-fx-padding: 5;"
        );
        closeButton.setOnAction(e -> dialogStage.close());
        
        HBox.setHgrow(titleLabel, Priority.ALWAYS);
        titleBar.getChildren().addAll(titleLabel, closeButton);
        
        return titleBar;
    }
    
    private TabPane createTabPane() {
        TabPane tabPane = new TabPane();
        tabPane.setStyle(
            "-fx-background-color: transparent; " +
            "-fx-background: transparent; " +
            "-fx-border-color: transparent;"
        );
        
        // 筆記標籤頁
        Tab noteTab = createNoteTab();
        
        // 重點標籤頁
        Tab highlightTab = createHighlightTab();
        
        // 查看標籤頁
        Tab viewTab = createViewTab();
        
        tabPane.getTabs().addAll(noteTab, highlightTab, viewTab);
        
        return tabPane;
    }
    
    private Tab createNoteTab() {
        Tab tab = new Tab("📄 新增筆記");
        tab.setClosable(false);
        
        VBox content = new VBox(15);
        content.setPadding(new Insets(20));
        
        // 筆記標題
        Label titleLabel = new Label("標題:");
        titleLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #2c3e50;");
        
        noteTitle = new TextField();
        noteTitle.setPromptText("輸入筆記標題...");
        noteTitle.setStyle(
            "-fx-background-color: rgba(255,255,255,0.8); " +
            "-fx-border-color: rgba(200,200,200,0.5); " +
            "-fx-border-radius: 8; " +
            "-fx-background-radius: 8; " +
            "-fx-padding: 10; " +
            "-fx-font-size: 14px;"
        );
        
        // 筆記內容
        Label contentLabel = new Label("內容:");
        contentLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #2c3e50;");
        
        noteContent = new TextArea();
        noteContent.setPromptText("輸入筆記內容...");
        noteContent.setPrefRowCount(8);
        noteContent.setWrapText(true);
        noteContent.setStyle(
            "-fx-background-color: rgba(255,255,255,0.8); " +
            "-fx-border-color: rgba(200,200,200,0.5); " +
            "-fx-border-radius: 8; " +
            "-fx-background-radius: 8; " +
            "-fx-padding: 10; " +
            "-fx-font-size: 14px;"
        );
        
        // 按鈕區域
        HBox buttonBar = new HBox(10);
        buttonBar.setAlignment(Pos.CENTER_RIGHT);
        
        Button saveButton = createStyledButton("💾 保存筆記", () -> saveNote());
        Button clearButton = createStyledButton("🗑️ 清空", () -> clearNote());
        
        buttonBar.getChildren().addAll(clearButton, saveButton);
        
        content.getChildren().addAll(titleLabel, noteTitle, contentLabel, noteContent, buttonBar);
        tab.setContent(content);
        
        return tab;
    }
    
    private Tab createHighlightTab() {
        Tab tab = new Tab("🖍️ 畫重點");
        tab.setClosable(false);
        
        VBox content = new VBox(15);
        content.setPadding(new Insets(20));
        
        // 重點顏色選擇
        Label colorLabel = new Label("重點顏色:");
        colorLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #2c3e50;");
        
        highlightColor = new ComboBox<>();
        highlightColor.getItems().addAll("黃色", "綠色", "藍色", "紅色", "紫色");
        highlightColor.setValue("黃色");
        highlightColor.setStyle(
            "-fx-background-color: rgba(255,255,255,0.8); " +
            "-fx-border-color: rgba(200,200,200,0.5); " +
            "-fx-border-radius: 8; " +
            "-fx-background-radius: 8; " +
            "-fx-padding: 8;"
        );
        
        // 重點備註
        Label noteLabel = new Label("重點備註:");
        noteLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #2c3e50;");
        
        highlightNote = new TextArea();
        highlightNote.setPromptText("為這個重點添加備註...");
        highlightNote.setPrefRowCount(4);
        highlightNote.setWrapText(true);
        highlightNote.setStyle(
            "-fx-background-color: rgba(255,255,255,0.8); " +
            "-fx-border-color: rgba(200,200,200,0.5); " +
            "-fx-border-radius: 8; " +
            "-fx-background-radius: 8; " +
            "-fx-padding: 10; " +
            "-fx-font-size: 14px;"
        );
        
        // 按鈕區域
        HBox buttonBar = new HBox(10);
        buttonBar.setAlignment(Pos.CENTER_RIGHT);
        
        Button addHighlightButton = createStyledButton("🖍️ 添加重點", () -> addHighlight());
        Button clearHighlightButton = createStyledButton("🗑️ 清空", () -> clearHighlight());
        
        buttonBar.getChildren().addAll(clearHighlightButton, addHighlightButton);
        
        content.getChildren().addAll(colorLabel, highlightColor, noteLabel, highlightNote, buttonBar);
        tab.setContent(content);
        
        return tab;
    }
    
    private Tab createViewTab() {
        Tab tab = new Tab("📋 查看筆記");
        tab.setClosable(false);
        
        VBox content = new VBox(15);
        content.setPadding(new Insets(20));
        
        // 筆記列表
        Label notesLabel = new Label("📄 筆記列表:");
        notesLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #2c3e50;");
        
        ListView<String> notesList = new ListView<>();
        notesList.setPrefHeight(200);
        notesList.setStyle(
            "-fx-background-color: rgba(255,255,255,0.8); " +
            "-fx-border-color: rgba(200,200,200,0.5); " +
            "-fx-border-radius: 8; " +
            "-fx-background-radius: 8;"
        );
        
        // 重點列表
        Label highlightsLabel = new Label("🖍️ 重點列表:");
        highlightsLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #2c3e50;");
        
        ListView<String> highlightsList = new ListView<>();
        highlightsList.setPrefHeight(200);
        highlightsList.setStyle(
            "-fx-background-color: rgba(255,255,255,0.8); " +
            "-fx-border-color: rgba(200,200,200,0.5); " +
            "-fx-border-radius: 8; " +
            "-fx-background-radius: 8;"
        );
        
        // 載入筆記和重點
        loadNotesAndHighlights(notesList, highlightsList);
        
        content.getChildren().addAll(notesLabel, notesList, highlightsLabel, highlightsList);
        tab.setContent(content);
        
        return tab;
    }
    
    private Button createStyledButton(String text, Runnable action) {
        Button button = new Button(text);
        button.setStyle(
            "-fx-background-color: linear-gradient(to bottom, " +
                "rgba(52,152,219,0.9) 0%, " +
                "rgba(41,128,185,0.9) 100%); " +
            "-fx-border-color: rgba(52,152,219,0.8); " +
            "-fx-border-width: 1; " +
            "-fx-border-radius: 8; " +
            "-fx-background-radius: 8; " +
            "-fx-text-fill: white; " +
            "-fx-font-size: 12px; " +
            "-fx-font-weight: 600; " +
            "-fx-padding: 8 16 8 16; " +
            "-fx-cursor: hand; " +
            "-fx-effect: dropshadow(gaussian, rgba(52,152,219,0.4), 4, 0, 0, 2);"
        );
        button.setOnAction(e -> action.run());
        return button;
    }
    
    private void saveNote() {
        String title = noteTitle.getText().trim();
        String content = noteContent.getText().trim();
        
        if (title.isEmpty() || content.isEmpty()) {
            showAlert("錯誤", "請填寫標題和內容");
            return;
        }
        
        noteManager.addNote(bookPath, bookName, pageIndex, content, title);
        showAlert("成功", "筆記已保存");
        clearNote();
    }
    
    private void clearNote() {
        noteTitle.clear();
        noteContent.clear();
    }
    
    private void addHighlight() {
        String color = highlightColor.getValue();
        String note = highlightNote.getText().trim();
        
        // 這裡需要獲取選中的文字，暫時使用模擬數據
        String selectedText = "選中的文字";
        int startPos = 0;
        int endPos = selectedText.length();
        
        noteManager.addHighlight(bookPath, bookName, pageIndex, startPos, endPos, selectedText, color);
        showAlert("成功", "重點已添加");
        clearHighlight();
    }
    
    private void clearHighlight() {
        highlightNote.clear();
        highlightColor.setValue("黃色");
    }
    
    private void loadNotesAndHighlights(ListView<String> notesList, ListView<String> highlightsList) {
        // 載入筆記
        var notes = noteManager.getNotesForPage(bookPath, pageIndex);
        for (var note : notes) {
            notesList.getItems().add(note.getTitle() + " - " + note.getContent().substring(0, Math.min(50, note.getContent().length())));
        }
        
        // 載入重點
        var highlights = noteManager.getHighlightsForPage(bookPath, pageIndex);
        for (var highlight : highlights) {
            highlightsList.getItems().add(highlight.getSelectedText() + " (" + highlight.getColor() + ")");
        }
    }
    
    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    public void show() {
        dialogStage.showAndWait();
    }
} 