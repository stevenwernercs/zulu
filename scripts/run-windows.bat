@echo off
setlocal enabledelayedexpansion

pushd %~dp0\..

for %%f in (target\*-all.jar) do (
  set JAR=%%f
  goto :found
)

echo Fat jar not found. Building...
mvn -DskipTests package || goto :eof
for %%f in (target\*-all.jar) do (
  set JAR=%%f
  goto :found
)

:found
set NATIVES_DIR=target\natives\windows
if not exist %NATIVES_DIR% (
  echo Extracting LWJGL natives...
  mvn -DskipTests package >NUL 2>&1 || echo Build failed. Ensure Maven is installed.
)

echo Running: %JAR%
java -Dorg.lwjgl.librarypath=%NATIVES_DIR% -jar "%JAR%"

popd
endlocal
