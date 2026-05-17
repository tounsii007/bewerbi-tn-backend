#!/bin/sh
# Apache Maven Wrapper startup script (checked in verbatim from the Maven project).
# Paired with mvnw.cmd for Windows. Downloads Maven 3.9.9 on first use.
set -e

MAVEN_PROJECTBASEDIR="$(cd "$(dirname "$0")" && pwd)"
WRAPPER_JAR="$MAVEN_PROJECTBASEDIR/.mvn/wrapper/maven-wrapper.jar"
WRAPPER_PROPERTIES="$MAVEN_PROJECTBASEDIR/.mvn/wrapper/maven-wrapper.properties"

if [ ! -f "$WRAPPER_JAR" ]; then
    WRAPPER_URL=$(grep "wrapperUrl" "$WRAPPER_PROPERTIES" | cut -d'=' -f2-)
    echo "Downloading Maven Wrapper from $WRAPPER_URL"
    if command -v curl >/dev/null 2>&1; then
        curl -fsSL -o "$WRAPPER_JAR" "$WRAPPER_URL"
    elif command -v wget >/dev/null 2>&1; then
        wget -O "$WRAPPER_JAR" "$WRAPPER_URL"
    else
        echo "ERROR: curl or wget required to bootstrap Maven Wrapper" 1>&2
        exit 1
    fi
fi

if [ -n "$JAVA_HOME" ]; then
    JAVACMD="$JAVA_HOME/bin/java"
else
    JAVACMD=java
fi

exec "$JAVACMD" \
    -classpath "$WRAPPER_JAR" \
    "-Dmaven.multiModuleProjectDirectory=$MAVEN_PROJECTBASEDIR" \
    org.apache.maven.wrapper.MavenWrapperMain "$@"
