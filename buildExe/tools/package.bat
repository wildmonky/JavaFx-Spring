@echo off
SETLOCAL

:: defaults, override via CLI args: --version x.x  --type msi^|exe  --jpackage ^<path^>  --app-name ^<name^>
SET "app-name=validator-tools"
SET "tools-dir=%~dp0"
SET "log-file=%tools-dir%package.out"
SET "version=3.0"
SET "type=msi"
::>=jdk 21
SET "jpackage=C:\development\enviorment\java\Eclipse Temurin\jdk-21.0.12.1+1\bin\jpackage"

:: ===== debug: script dir vs current dir =====
echo [debug] script dir dp0 = %~dp0 >> "%log-file%"
echo [debug] current dir cd = %cd% >> "%log-file%"

:: ===== base dirs (all relative to this script, not CWD) =====
for %%i in ("%~dp0..")    do SET "build-dir=%%~fi"
for %%i in ("%~dp0..\..") do SET "project-dir=%%~fi"

echo [debug] build-dir = %build-dir% >> "%log-file%"
echo [debug] project-dir = %project-dir% >> "%log-file%"

:: ===== parse command line arguments =====
:parse_args
if "%~1"=="" goto args_done
if /I "%~1"=="--version" (
    if "%~2"=="" ( echo missing value for --version & exit /b 2 )
    SET "version=%~2"
    shift & shift
    goto parse_args
)
if /I "%~1"=="--type" (
    if "%~2"=="" ( echo missing value for --type & exit /b 2 )
    SET "type=%~2"
    shift & shift
    goto parse_args
)
if /I "%~1"=="--jpackage" (
    if "%~2"=="" ( echo missing value for --jpackage & exit /b 2 )
    SET "jpackage=%~2"
    shift & shift
    goto parse_args
)
if /I "%~1"=="--app-name" (
    if "%~2"=="" ( echo missing value for --app-name & exit /b 2 )
    SET "app-name=%~2"
    shift & shift
    goto parse_args
)
echo unknown argument: %~1
echo usage: package.bat [--version 3.0] [--type msi^|exe] [--jpackage "path\to\jpackage"] [--app-name name]
exit /b 2
:args_done

:: ===== validate arguments =====
if /I not "%type%"=="msi" if /I not "%type%"=="exe" (
    echo invalid --type: %type% ^(only support msi or exe^)
    exit /b 3
)
if not exist "%jpackage%" (
    if not exist "%jpackage%.exe" (
        echo jpackage not found: %jpackage%
        exit /b 4
    )
    SET "jpackage=%jpackage%.exe"
)

echo package args: version=%version%, type=%type%, jpackage=%jpackage%, app-name=%app-name% >> "%log-file%"
echo base dirs: build-dir=%build-dir%, project-dir=%project-dir% >> "%log-file%"

echo check jpackage version >> "%log-file%"
"%jpackage%" --version >> "%log-file%"
if not %ERRORLEVEL% EQU 0 (
    echo  jpackage check error %ERRORLEVEL% >> "%log-file%"
    echo please check jpackage config in package.bat: %jpackage% >> "%log-file%"
    exit /b 1001
)

echo remove existed exe >> "%log-file%"
IF EXIST "%build-dir%\%app-name%-%version%.%type%" del /Q /F "%build-dir%\%app-name%-%version%.%type%"

IF EXIST "%project-dir%\target\exec\app.jar" goto mvn_done
echo mvn packaging... >> "%log-file%"
call mvn clean -D"maven.test.skip"=true package -f "%project-dir%\pom.xml" > "%tools-dir%mvn.out" 2>&1
if not exist "%project-dir%\target\exec\app.jar" (
    echo maven package failed, see %tools-dir%mvn.out >> "%log-file%"
    exit /b 2001
)
:mvn_done

IF EXIST "%build-dir%\temp" rmdir /Q /S "%build-dir%\temp"

echo jpackaging... >> "%log-file%"

"%jpackage%" --type %type% ^
    --name %app-name% --app-version %version% ^
    --input "%project-dir%\target\exec" --main-jar app.jar ^
    --icon "%project-dir%\src\main\resources\lizi.ico" ^
    --dest "%build-dir%" --temp "%build-dir%\temp\rule-validator" ^
    --win-dir-chooser ^
    --win-shortcut ^
    --win-per-user-install >> "%tools-dir%jpackage.out" 2>&1

echo jpackage exit code: %ERRORLEVEL% >> "%log-file%"

IF EXIST "%build-dir%\temp" rmdir /Q /S "%build-dir%\temp"

IF EXIST "%build-dir%\%app-name%-%version%.%type%" (
    echo build new exe success: %build-dir%\%app-name%-%version%.%type% >> "%log-file%"
    exit /b
) ELSE (
    echo build new exe fail >> "%log-file%"
    exit /b 3001
)