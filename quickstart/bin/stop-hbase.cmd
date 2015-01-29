@echo off

setlocal

set CURRENT_DIR="%~dp0"

for %%i in (%0) do (
  if not defined QUICKSTART_BIN_PATH (
    set QUICKSTART_BIN_PATH=%%~dpi
  )
)

if "%QUICKSTART_BIN_PATH:~-1%" == "\" (
  set QUICKSTART_BIN_PATH=%QUICKSTART_BIN_PATH:~0,-1%
)

set QUICKSTART_BASE=%QUICKSTART_BIN_PATH%\..
set QUICKSTART_HBASE_PATH=%QUICKSTART_BASE%\hbase\hbase

cd %QUICKSTART_HBASE_PATH%\bin

%QUICKSTART_HBASE_PATH%\bin\stop-hbase.cmd

cd %CURRENT_DIR%