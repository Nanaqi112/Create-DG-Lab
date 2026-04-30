#!/bin/sh

# Gradle start up script for POSIX
# Generated for Create Kinetic Link mod

APP_HOME=$( cd "$( dirname "$0" )" && pwd )

CLASSPATH="$APP_HOME/gradle/wrapper/gradle-wrapper.jar"

# Find Java
if [ -n "$JAVA_HOME" ]; then
    JAVA_CMD="$JAVA_HOME/bin/java"
else
    JAVA_CMD="java"
fi

exec "$JAVA_CMD" -Xmx3g -Xms256m -classpath "$CLASSPATH" org.gradle.wrapper.GradleWrapperMain "$@"