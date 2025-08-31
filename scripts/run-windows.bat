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
echo Running: %JAR%
java -jar "%JAR%"

popd
endlocal
