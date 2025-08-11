@echo off
echo 正在測試編譯...
echo.

echo 嘗試使用 Gradle 編譯...
call gradle clean compileJava
if %ERRORLEVEL% EQU 0 (
    echo Gradle 編譯成功！
) else (
    echo Gradle 編譯失敗！
    echo.
    echo 嘗試使用 Maven 編譯...
    call mvn clean compile
    if %ERRORLEVEL% EQU 0 (
        echo Maven 編譯成功！
    ) else (
        echo Maven 編譯也失敗！
        echo 請檢查依賴配置。
    )
)

echo.
echo 編譯測試完成。
pause
