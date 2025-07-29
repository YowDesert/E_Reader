package E_Reader.viewer;

import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.geometry.Pos;

import java.util.List;

/**
 * 圖片檢視器 - 負責圖片的顯示和縮放控制
 */
public class ImageViewer {

    private List<Image> images;
    private ImageView imageView = new ImageView();
    private ScrollPane scrollPane;
    private StackPane imageContainer;
    private Label pageLabel = new Label("Page: 0 / 0");
    private int currentIndex = 0;
    private double zoomLevel = 1.0;
    private FitMode fitMode = FitMode.FIT_WIDTH;

    public enum FitMode {
        FIT_WIDTH, FIT_HEIGHT, FIT_PAGE, ORIGINAL_SIZE
    }

    public ImageViewer() {
        initializeComponents();
        setupDefaultSettings();
    }
    
    private void initializeComponents() {
        imageView.setPreserveRatio(true);
        imageView.setSmooth(true);
        imageView.setCache(true);

        // 建立圖片容器並設定置中
        imageContainer = new StackPane();
        imageContainer.setAlignment(Pos.CENTER);
        imageContainer.getChildren().add(imageView);

        // 建立可滾動的容器
        scrollPane = new ScrollPane();
        scrollPane.setContent(imageContainer);
        scrollPane.setFitToWidth(true);
        scrollPane.setFitToHeight(true);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scrollPane.setStyle("-fx-background: #1e1e1e; -fx-background-color: #1e1e1e;");
    }
    
    private void setupDefaultSettings() {
        setFitToWidth();
    }

    public void setImages(List<Image> images) {
        this.images = images;
        currentIndex = 0;
        showImage();
    }
    
    public void clearImages() {
        this.images = null;
        this.currentIndex = 0;
        this.zoomLevel = 1.0;
        this.fitMode = FitMode.FIT_WIDTH;
        imageView.setImage(null);
        imageView.setRotate(0);
        pageLabel.setText("Page: 0 / 0");
    }

    public void nextPage() {
        if (images == null || images.isEmpty()) return;
        if (currentIndex < images.size() - 1) {
            currentIndex++;
            showImage();
        }
    }

    public void prevPage() {
        if (images == null || images.isEmpty()) return;
        if (currentIndex > 0) {
            currentIndex--;
            showImage();
        }
    }

    public void goToFirstPage() {
        if (images == null || images.isEmpty()) return;
        currentIndex = 0;
        showImage();
    }

    public void goToLastPage() {
        if (images == null || images.isEmpty()) return;
        currentIndex = images.size() - 1;
        showImage();
    }

    public void goToPage(int pageIndex) {
        if (images == null || images.isEmpty()) return;
        if (pageIndex >= 0 && pageIndex < images.size()) {
            currentIndex = pageIndex;
            showImage();
        }
    }

    public void zoomIn() {
        zoomLevel *= 1.2;
        applyZoom();
    }

    public void zoomOut() {
        zoomLevel /= 1.2;
        if (zoomLevel < 0.1) zoomLevel = 0.1;
        applyZoom();
    }

    public void resetZoom() {
        zoomLevel = 1.0;
        applySizeMode();
    }

    public void fitToWidth() {
        fitMode = FitMode.FIT_WIDTH;
        applySizeMode();
    }

    public void fitToHeight() {
        fitMode = FitMode.FIT_HEIGHT;
        applySizeMode();
    }

    public void fitToPage() {
        fitMode = FitMode.FIT_PAGE;
        applySizeMode();
    }

    public void originalSize() {
        fitMode = FitMode.ORIGINAL_SIZE;
        applySizeMode();
    }

    public void rotate() {
        imageView.setRotate(imageView.getRotate() + 90);
    }

    private void showImage() {
        if (images == null || images.isEmpty()) {
            imageView.setImage(null);
            pageLabel.setText("Page: 0 / 0");
            return;
        }

        Image currentImage = images.get(currentIndex);
        imageView.setImage(currentImage);
        pageLabel.setText("Page: " + (currentIndex + 1) + " / " + images.size());

        // 套用目前的尺寸模式
        applySizeMode();

        // 確保圖片在容器中置中
        imageContainer.setAlignment(Pos.CENTER);
    }

    private void applySizeMode() {
        if (images == null || images.isEmpty()) return;

        Image currentImage = images.get(currentIndex);
        double imageWidth = currentImage.getWidth();
        double imageHeight = currentImage.getHeight();

        switch (fitMode) {
            case FIT_WIDTH:
                setFitToWidth();
                break;
            case FIT_HEIGHT:
                setFitToHeight();
                break;
            case FIT_PAGE:
                setFitToPage();
                break;
            case ORIGINAL_SIZE:
                imageView.setFitWidth(imageWidth * zoomLevel);
                imageView.setFitHeight(imageHeight * zoomLevel);
                break;
        }

        // 每次調整尺寸後都確保置中
        imageContainer.setAlignment(Pos.CENTER);
    }

    private void setFitToWidth() {
        imageView.setFitWidth(800 * zoomLevel);
        imageView.setFitHeight(0); // 自動計算高度
    }

    private void setFitToHeight() {
        imageView.setFitHeight(600 * zoomLevel);
        imageView.setFitWidth(0); // 自動計算寬度
    }

    private void setFitToPage() {
        imageView.setFitWidth(800 * zoomLevel);
        imageView.setFitHeight(600 * zoomLevel);
    }

    private void applyZoom() {
        applySizeMode();
    }

    // 主題設定
    public void applyTheme(String backgroundColor) {
        scrollPane.setStyle(String.format("-fx-background: %s; -fx-background-color: %s;", 
                backgroundColor, backgroundColor));
    }

    // Getter 方法
    public ImageView getImageView() {
        return imageView;
    }

    public ScrollPane getScrollPane() {
        return scrollPane;
    }

    public Label getPageLabel() {
        return pageLabel;
    }

    public int getCurrentIndex() {
        return currentIndex;
    }

    public int getTotalPages() {
        return images != null ? images.size() : 0;
    }

    public double getZoomLevel() {
        return zoomLevel;
    }

    public FitMode getFitMode() {
        return fitMode;
    }

    public void setFitMode(FitMode fitMode) {
        this.fitMode = fitMode;
        applySizeMode();
    }

    public boolean hasImages() {
        return images != null && !images.isEmpty();
    }

    public boolean canGoNext() {
        return hasImages() && currentIndex < images.size() - 1;
    }

    public boolean canGoPrevious() {
        return hasImages() && currentIndex > 0;
    }

    public StackPane getImageContainer() {
        return imageContainer;
    }
}
