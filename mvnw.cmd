@echo off
setlocal
if "%JAVA_HOME%"=="" (
    set "JAVA_HOME=C:\Program Files\Java\jdk-25.0.4"
)
set "MAVEN_EXEC=C:\Users\tharu\.m2\wrapper\dists\apache-maven-3.8.5-bin\5i5jha092a3i37g0paqnfr15e0\apache-maven-3.8.5\bin\mvn.cmd"
if not exist "%MAVEN_EXEC%" (
    echo [ERROR] Maven distribution not found at %MAVEN_EXEC%
    exit /b 1
)
"%MAVEN_EXEC%" -f "%~dp0backend\pom.xml" %*
