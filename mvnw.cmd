@rem Maven Wrapper startup script for Windows.
@echo off
setlocal

set MAVEN_PROJECTBASEDIR=%~dp0
set WRAPPER_JAR=%MAVEN_PROJECTBASEDIR%.mvn\wrapper\maven-wrapper.jar
set WRAPPER_PROPERTIES=%MAVEN_PROJECTBASEDIR%.mvn\wrapper\maven-wrapper.properties

if not exist "%WRAPPER_JAR%" (
    for /f "tokens=1,* delims==" %%a in ('findstr "wrapperUrl" "%WRAPPER_PROPERTIES%"') do set WRAPPER_URL=%%b
    echo Downloading Maven Wrapper from %WRAPPER_URL%
    powershell -Command "Invoke-WebRequest -Uri '%WRAPPER_URL%' -OutFile '%WRAPPER_JAR%'"
)

if defined JAVA_HOME (
    set JAVACMD="%JAVA_HOME%\bin\java.exe"
) else (
    set JAVACMD=java
)

%JAVACMD% -classpath "%WRAPPER_JAR%" "-Dmaven.multiModuleProjectDirectory=%MAVEN_PROJECTBASEDIR%" org.apache.maven.wrapper.MavenWrapperMain %*

endlocal
