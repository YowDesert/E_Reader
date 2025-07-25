package E_Reader;

import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class PdfLoader {

    public List<Image> loadImagesFromPdf(File pdfFile) throws IOException {
        //test
        List<Image> images = new ArrayList<>();
        try (PDDocument document = PDDocument.load(pdfFile)) {
            PDFRenderer renderer = new PDFRenderer(document);
            for (int i = 0; i < document.getNumberOfPages(); i++) {
                BufferedImage bim = renderer.renderImageWithDPI(i, 150);
                images.add(SwingFXUtils.toFXImage(bim, null));
            }
        }
        return images;
    }
}
