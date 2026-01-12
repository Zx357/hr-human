@echo off
set JAVA_HOME=C:\Program Files\Microsoft\jdk-17.0.17.10-hotspot
set PATH=%JAVA_HOME%\bin;%PATH%
echo Starting KAdmin Backend...
java -jar target/kadmin-backend-1.0.0.jar