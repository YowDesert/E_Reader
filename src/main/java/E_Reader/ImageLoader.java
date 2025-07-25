package E_Reader;

import javafx.scene.image.Image;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

public class ImageLoader {

    public List<Image> loadImagesFromFolder(File folder) {
        //test
        File[] files = folder.listFiles(f -> {
            String name = f.getName().toLowerCase();
            return name.endsWith(".jpg") || name.endsWith(".jpeg") || name.endsWith(".png");
        });
        if (files == null) return new ArrayList<>();

        Arrays.sort(files, Comparator.comparing(File::getName));
        List<Image> images = new ArrayList<>();
        for (File f : files) {
            images.add(new Image(f.toURI().toString()));
        }
        return images;
    }
}
