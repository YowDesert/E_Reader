package E_Reader;

import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;

public class Main extends Application {

    private ImageViewer viewer = new ImageViewer();
    private ImageLoader imageLoader = new ImageLoader();
    private PdfLoader pdfLoader = new PdfLoader();
    private boolean isPdfMode = false;

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("E_Reader 漫畫＆PDF閱讀器");

        BorderPane root = new BorderPane();
        root.setCenter(viewer.getImageView());

        Button openFolderBtn = new Button("📂 開啟圖片資料夾");
        Button openPdfBtn = new Button("📄 開啟 PDF 檔案");
        Button prevBtn = new Button("← 上一頁");
        Button nextBtn = new Button("下一頁 →");
        Label pageLabel = viewer.getPageLabel();

        HBox controls = new HBox(10, openFolderBtn, openPdfBtn, prevBtn, nextBtn, pageLabel);
        controls.setAlignment(Pos.CENTER);
        root.setBottom(controls);

        openFolderBtn.setOnAction(e -> {
            DirectoryChooser dc = new DirectoryChooser();
            dc.setTitle("選擇圖片資料夾");
            File folder = dc.showDialog(primaryStage);
            if (folder != null) {
                var images = imageLoader.loadImagesFromFolder(folder);
                if (!images.isEmpty()) {
                    isPdfMode = false;
                    viewer.setImages(images);
                }
            }
        });

        openPdfBtn.setOnAction(e -> {
            FileChooser fc = new FileChooser();
            fc.setTitle("選擇 PDF 檔案");
            fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF Files", "*.pdf"));
            File pdfFile = fc.showOpenDialog(primaryStage);
            if (pdfFile != null) {
                try {
                    var images = pdfLoader.loadImagesFromPdf(pdfFile);
                    if (!images.isEmpty()) {
                        isPdfMode = true;
                        viewer.setImages(images);
                    }
                } catch (Exception ex) {
                    AlertHelper.showError("無法載入 PDF 檔案", ex.getMessage());
                }
            }
        });

        prevBtn.setOnAction(e -> viewer.prevPage());
        nextBtn.setOnAction(e -> viewer.nextPage());

        Scene scene = new Scene(root, 900, 700);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
