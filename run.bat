@echo off
setlocal
cd /d "%~dp0"
set "JDK_BIN="
if defined JAVA_HOME if exist "%JAVA_HOME%\bin\javac.exe" set "JDK_BIN=%JAVA_HOME%\bin"
if not defined JDK_BIN for /d %%D in ("%ProgramFiles%\Java\jdk-*") do if exist "%%~fD\bin\javac.exe" set "JDK_BIN=%%~fD\bin"
if not defined JDK_BIN for /f "delims=" %%J in ('where javac 2^>nul') do if not defined JDK_BIN set "JDK_BIN=%%~dpJ"
if not defined JDK_BIN (
  echo Java JDK not found. Install JDK 11 or later and set JAVA_HOME.
  exit /b 1
)
if not exist out mkdir out
"%JDK_BIN%\javac.exe" -encoding UTF-8 -d out src\*.java
if errorlevel 1 exit /b 1
"%JDK_BIN%\java.exe" -cp out Main
endlocal
