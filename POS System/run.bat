@echo off
javac -cp ".;lib/itextpdf-5.5.13.2.jar" pos/*.java
if errorlevel 1 (
    echo Compilation failed!
    pause
    exit /b
)
echo Compilation successful.
java -cp ".;lib/itextpdf-5.5.13.2.jar" pos.AppInitializer
pause