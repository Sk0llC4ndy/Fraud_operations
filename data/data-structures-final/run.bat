@echo off
echo Compiling...
if not exist out mkdir out
dir /s /b src\main\java\*.java > sources.txt
javac -d out @sources.txt
if %errorlevel% neq 0 (
    echo Compilation failed.
    pause
    exit /b
)
echo Running...
java -cp "out;data" com.fraudoperations.Main
pause
