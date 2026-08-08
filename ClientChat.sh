#!/bin/sh
# Launcher for ClientChat (fat jar). Works on Linux/macOS.
DIR="$(cd "$(dirname "$0")" && pwd)"
exec java --enable-native-access=ALL-UNNAMED -jar "$DIR/ClientChat.jar" "$@"
