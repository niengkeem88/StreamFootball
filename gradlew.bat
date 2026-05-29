@echo off
set "LOCAL_GRADLE=%USERPROFILE%\.gradle\wrapper\dists\gradle-8.13-bin\5xuhj0ry160q40clulazy9h7d\gradle-8.13\bin\gradle.bat"
if exist "%LOCAL_GRADLE%" (
  "%LOCAL_GRADLE%" %*
  exit /b %ERRORLEVEL%
)
where gradle >nul 2>nul
if %ERRORLEVEL% EQU 0 (
  gradle %*
  exit /b %ERRORLEVEL%
)
echo Gradle 8.13 was not found. Install Gradle or refresh the local wrapper distribution.
exit /b 1
