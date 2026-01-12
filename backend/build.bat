@echo off
set JAVA_HOME=C:\Program Files\Microsoft\jdk-17.0.17.10-hotspot
set PATH=%JAVA_HOME%\bin;%PATH%
echo Using Java:
java -version
echo.
echo Building project...
call mvn clean package -DskipTests