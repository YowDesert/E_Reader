package E_Reader.test;

import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;
import java.awt.image.BufferedImage;
import java.awt.*;

public class Tess4jTest {
    public static void main(String[] args) {
        System.out.println("Testing Tess4j integration...");
        
        try {
            Tesseract tesseract = new Tesseract();
            
            // Test basic initialization
            tesseract.setLanguage("eng");
            System.out.println("✓ Tesseract initialized successfully");
            
            // Test with a simple image
            BufferedImage testImage = new BufferedImage(200, 50, BufferedImage.TYPE_INT_RGB);
            Graphics2D g2d = testImage.createGraphics();
            g2d.setColor(Color.WHITE);
            g2d.fillRect(0, 0, 200, 50);
            g2d.setColor(Color.BLACK);
            g2d.setFont(new Font("Arial", Font.PLAIN, 24));
            g2d.drawString("Test", 10, 35);
            g2d.dispose();
            
            String result = tesseract.doOCR(testImage);
            System.out.println("✓ OCR test result: " + result.trim());
            
            if (result.toLowerCase().contains("test")) {
                System.out.println("✅ Tess4j is working correctly!");
            } else {
                System.out.println("⚠️  Tess4j initialized but OCR may not be accurate");
            }
            
        } catch (Exception e) {
            System.err.println("❌ Tess4j test failed: " + e.getMessage());
            e.printStackTrace();
        }
    }
}