#!/bin/sh
# Launcher for ClientChat (fat jar). Works on Linux/macOS.
DIR="$(cd "$(dirname "$0")" && pwd)"
exec java -jar "$DIR/ClientChat.jar" "$@"
