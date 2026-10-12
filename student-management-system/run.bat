@echo off
title Student Management System
echo ===================================================
echo   Launching Student Management System (Java Swing)
echo ===================================================

if not exist bin\com\sms\Main.class (
    echo [INFO] Compiled classes not found. Running build.bat first...
    call build.bat
)

echo Starting application...
start "Student Management System" javaw -cp "bin;lib/*" com.sms.Main

echo App launched in background.
