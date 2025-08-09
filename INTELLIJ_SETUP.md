# IntelliJ IDEA Run Configuration Instructions

## Method 1: Using Maven (Recommended)

1. Open terminal in IntelliJ (View → Tool Windows → Terminal)
2. Run: `mvn clean compile javafx:run`

## Method 2: Fix IntelliJ Run Configuration

### Step 1: Check Project Settings
- File → Project Structure → Project
- Project SDK: Java 17 or higher
- Project language level: 17

### Step 2: Edit Run Configuration
- Run → Edit Configurations
- If no configuration exists, click "+" and choose "Application"

### Step 3: Configuration Settings
```
Name: E_Reader
Build and run:
- SDK: Java 17+
- Main class: E_Reader.Main
- Module: ereader

VM options:
--module-path "C:\Users\hpes5\.m2\repository\org\openjfx\javafx-sdk-21.0.8\lib"
--add-modules javafx.controls,javafx.fxml,javafx.swing,javafx.media,javafx.web
--add-opens javafx.graphics/javafx.scene=ALL-UNNAMED

Working directory: C:\E_Reader\E_Reader
Use classpath of module: ereader
```

### Step 4: Alternative VM Options (if path differs)
Replace the module-path with your actual JavaFX location:
```
--module-path "C:\path\to\your\javafx\lib"
--add-modules javafx.controls,javafx.fxml,javafx.swing,javafx.media,javafx.web
```

## Method 3: Run without modules (if still having issues)

Create new configuration:
```
Name: E_Reader_NoModules
Main class: E_Reader.Main
VM options:
--add-exports javafx.graphics/com.sun.javafx.application=ALL-UNNAMED
--add-opens javafx.graphics/javafx.scene=ALL-UNNAMED
```

## Troubleshooting

### Error: "Module ereader not found"
1. Make sure `module-info.java` exists in `src/main/java/`
2. Clean and rebuild project (Build → Clean Project, then Build → Rebuild Project)
3. Try running with Maven first: `mvn javafx:run`

### Error: JavaFX runtime components missing
1. Download JavaFX SDK from https://openjfx.io/
2. Extract to a folder (e.g., C:\javafx-sdk-21)
3. Update VM options module-path to point to the lib folder

### Still not working?
Use the provided batch files:
- `fix_and_run.bat` - Comprehensive fix and run
- `run_app.bat` - Simple Maven run
