@echo off
echo ===================================================
echo   Compiling Student Management System (Java)
echo ===================================================

if not exist bin mkdir bin

echo Finding source files...
dir /s /b src\*.java > sources.txt

echo Compiling Java classes with javac...
javac -encoding UTF-8 -cp "lib/*" -d bin @sources.txt

if %errorlevel% neq 0 (
    echo [ERROR] Compilation failed. Please check Java errors above.
    del sources.txt
    pause
    exit /b %errorlevel%
)

del sources.txt
echo [SUCCESS] Compilation completed successfully!
echo Output classes located in: bin\
echo.
pause
