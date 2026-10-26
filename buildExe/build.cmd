@echo off
setlocal enabledelayedexpansion

:: the bat file to execute (args via %* are forwarded to package.bat)
set bat.file=C:\development\repository\javafx\rule-validator\buildExe\tools\package.bat
:: the log file written by package.bat
set bat.log.out=C:\development\repository\javafx\rule-validator\buildExe\tools\package.out

set common.function=%~dp0tools\common.bat

:: delete flag file
if exist done.flag del done.flag
:: animation chars
set "chars=\|/-"
set index=0
set line.out=line.out

:: ===== prepare log files BEFORE starting package.bat =====
if exist "%line.out%" del "%line.out%"
echo 0> "%line.out%"
if exist "%bat.log.out%" del "%bat.log.out%"
type nul > "%bat.log.out%"

:: start background command, redirect output to nul, create flag file when done
start /B "" cmd /c "%bat.file% %* >nul 2>&1 & echo . > done.flag"
echo please waiting....

:loop
:: get current line count
set /p last_line=<"%line.out%" 2>nul || set last_line=0
:: read new content
set line_num=0
if exist %bat.log.out% (
    @REM check file size, skip reading when empty
    for %%F in (%bat.log.out%) do set size= %%~zF
    if !size! gtr 0 (
        if %last_line% == 0 (
            for /f "usebackq delims=" %%a in (%bat.log.out%) do (
                    call :readline %%a
            )
        ) else (
            for /f "usebackq skip=%last_line% delims=" %%a in (%bat.log.out%) do (
                  call :readline %%a
            )
        )
    )

    :: update line counter
    if !line_num! gtr 0 (
        set /a new_line=last_line + line_num
        echo !new_line! > "%line.out%"
    )
)

if exist done.flag (
    del done.flag
    del line.out
    del %bat.log.out%
    echo complete!
    pause
    exit /b
)
:: update animation
set /a "index=(index+1) %% 4"
call set "char=!chars:~%index%,1!"
<nul set /p "=!char!"
ping -n 2 127.0.0.1 >nul
<nul set /p "="
goto :loop

:readline
    set /a line_num+=1
    set line=%*
    call %common.function% :datetime datetimestr
    <nul set /p "=[!datetimestr!] "
    echo !line!
    exit /b