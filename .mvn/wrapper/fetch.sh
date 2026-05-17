#!/bin/sh
# Pre-fetches maven-wrapper.jar so CI images and Docker builds don't need
# public network access at build time. Run once per environment / cache layer.
set -eu

DIR="$(cd "$(dirname "$0")" && pwd)"
PROPS="$DIR/maven-wrapper.properties"
JAR="$DIR/maven-wrapper.jar"

if [ -f "$JAR" ]; then
  echo "Wrapper already present: $JAR"
  exit 0
fi

URL="$(grep '^wrapperUrl=' "$PROPS" | cut -d= -f2-)"
echo "Downloading maven-wrapper.jar from $URL"
if command -v curl >/dev/null 2>&1; then
  curl -fsSL -o "$JAR" "$URL"
else
  wget -O "$JAR" "$URL"
fi
echo "Wrapper fetched: $JAR"
