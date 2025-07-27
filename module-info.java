module E_Reader {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.swing;
    requires java.desktop;

    // PDF 處理
    requires org.apache.pdfbox;

    // JSON 處理
    requires com.fasterxml.jackson.core;
    requires com.fasterxml.jackson.databind;
    requires com.fasterxml.jackson.datatype.jsr310;

    // OCR 支援（如果需要）
    requires tess4j;

    exports E_Reader;
}