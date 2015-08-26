@echo off

setlocal

for %%i in (%0) do (
  if not defined QUICKSTART_BIN_PATH (
    set QUICKSTART_BIN_PATH=%%~dpi
  )
)

%QUICKSTART_BIN_PATH%\quickstart.cmd collector start