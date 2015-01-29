@echo off

setlocal

for %%i in (%0) do (
  if not defined QUICKSTART_BIN_PATH (
    set QUICKSTART_BIN_PATH=%%~dpi
  )
)

set QUICKSTART_BASE=%QUICKSTART_BIN_PATH%\..
set QUICKSTART_HBASE_PATH=%QUICKSTART_BASE%\hbase\hbase

if not exist %QUICKSTART_HBASE_PATH% (
  echo HBase not exist in the %QUICKSTART_HBASE_PATH%
  goto :eof
)

echo create all tables
call %QUICKSTART_HBASE_PATH%\bin\hbase.cmd shell %QUICKSTART_BASE%\conf\hbase\init-hbase.txt