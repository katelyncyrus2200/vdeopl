@echo off
set DIR=%~dp0
set JAVA_CMD=%JAVA_HOME%\bin\java.exe
if exist "%JAVA_CMD%" (
  "%JAVA_CMD%" -jar "%DIR%gradle\wrapper\gradle-wrapper.jar" %*
) else (
  java -jar "%DIR%gradle\wrapper\gradle-wrapper.jar" %*
)
