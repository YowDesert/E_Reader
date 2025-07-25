package E_Reader;

import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import java.util.List;

public class ImageViewer {

    private List<Image> images;
    private ImageView imageView = new ImageView();
    private Label pageLabel = new Label("Page: 0 / 0");
    private int currentIndex = 0;

    public ImageViewer() {
        imageView.setPreserveRatio(true);
        imageView.setFitWidth(800);
        imageView.setFitHeight(600);
    }

    public void setImages(List<Image> images) {
        this.images = images;
        currentIndex = 0;
        showImage();
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

    private void showImage() {
        if (images == null || images.isEmpty()) {
            imageView.setImage(null);
            pageLabel.setText("Page: 0 / 0");
            return;
        }
        imageView.setImage(images.get(currentIndex));
        pageLabel.setText("Page: " + (currentIndex + 1) + " / " + images.size());
    }

    public ImageView getImageView() {
        return imageView;
    }

    public Label getPageLabel() {
        return pageLabel;
    }
}
