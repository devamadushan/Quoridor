@echo off
echo Testing Quoridor.exe with diagnostics...
echo.

REM Download and test the exe
curl -L -o "Quoridor-test.exe" "https://github.com/devamadushan/Quoridor/releases/latest/download/Quoridor.exe"

echo.
echo File downloaded. Size:
dir "Quoridor-test.exe"

echo.
echo Testing execution...
echo If it hangs at "gathering info", press Ctrl+C
echo.

REM Run with verbose output
"Quoridor-test.exe" --verbose

echo.
echo Test completed.
pause 