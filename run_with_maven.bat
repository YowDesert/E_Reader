@echo off
echo 正在使用 Maven 執行 E-Reader...
cd /d "%~dp0"

REM 清理並編譯
echo 清理並編譯專案...
mvn clean compile

REM 使用 JavaFX Maven 插件執行
echo 啟動應用程式...
mvn javafx:run

pause
