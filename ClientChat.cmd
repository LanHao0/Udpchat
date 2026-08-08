@echo off
REM Launcher for ClientChat (fat jar). Windows.
java --enable-native-access=ALL-UNNAMED -jar "%~dp0ClientChat.jar" %*
