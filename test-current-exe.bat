@echo off
title Test Current Quoridor EXE

echo Testing current Quoridor.exe with multiple methods...
echo.

cd /d "C:\Users\Yoan\Desktop\Quoridor-Game"

echo [TEST 1] Basic execution with output capture...
Quoridor.exe > test1.log 2>&1
echo Exit code: %ERRORLEVEL%
if exist test1.log (
    echo Log content:
    type test1.log
) else (
    echo No log created
)

echo.
echo [TEST 2] Using start command...
start /wait Quoridor.exe > test2.log 2>&1
echo Exit code: %ERRORLEVEL%

echo.
echo [TEST 3] PowerShell execution...
powershell.exe -Command "& '.\Quoridor.exe' 2>&1 | Out-File -FilePath 'test3.log'"
if exist test3.log type test3.log

echo.
echo [TEST 4] Check file properties...
powershell.exe -Command "Get-ItemProperty 'Quoridor.exe' | Format-List"

echo.
echo [TEST 5] Try with compatibility...
powershell.exe -Command "Start-Process -FilePath '.\Quoridor.exe' -Wait -RedirectStandardOutput 'test5.log' -RedirectStandardError 'test5.log'" 2>nul
if exist test5.log type test5.log

echo.
echo All tests completed. Check for any output above.
pause 