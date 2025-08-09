@echo off
echo Starting E-Reader with Tess4j support...
echo.

REM Set JVM options for Tess4j compatibility
set JVM_OPTS=--add-opens java.base/java.lang=ALL-UNNAMED --add-opens java.desktop/sun.awt.image=ALL-UNNAMED --add-opens javafx.graphics/javafx.scene=ALL-UNNAMED -Dfile.encoding=UTF-8 -Djava.awt.headless=false

REM Run the application
call mvn exec:java -Dexec.mainClass="E_Reader.Main" -Dexec.args="%JVM_OPTS%"

if errorlevel 1 (
    echo ERROR: Application failed to start
    echo This might be due to missing Tesseract installation
    echo Please install Tesseract OCR from: https://github.com/tesseract-ocr/tesseract
    pause
)